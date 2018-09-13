package main.java.model.amazon;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognitionAsync;
import com.amazonaws.services.rekognition.AmazonRekognitionAsyncClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import javafx.geometry.Rectangle2D;
import main.java.bean.IPerson;
import main.java.bean.amazon.PersonAmazon;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static main.java.constants.Constants.COLLECTION_ID;

/**
 * This class handles calls to the Amazon API. It is a subclass of MainViewModel.
 */
public class AwsCaller {

    /**
     * Amazon object used to do the calls, it deals the authentification too.
     */
    private AmazonRekognitionAsync amazonRekognition;

    /**
     * the Amazon modele used to do some callback when calls are finished.
     */
    private MainViewModelAmazon modele;


    /**
     * Constructor, who initialize the model and create the amazon object.
     * @param credentials authentification params
     * @param modele the modele used for the callback.
     */
    public AwsCaller(BasicAWSCredentials credentials,
                     MainViewModelAmazon modele) {
        this.modele = modele;
        amazonRekognition = AmazonRekognitionAsyncClientBuilder.standard()
                .withRegion(Regions.US_WEST_2)
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    /**
     * This method get an Amazon Image from the S3 Amazon Bucket from his bucket place, and is name(key).
     * @param bucket the name of the bucket who the image is saved.
     * @param key uniq to find the right picture.
     * @return
     */
    private Image getImageUtil(String bucket, String key) {
        Image img = new Image()
                .withS3Object(new S3Object().withBucket(bucket).withName(key));
        img.getS3Object();
        System.out.println(img.getS3Object().getVersion());
        return img;
    }

    /**
     * This method calls the API with an image an get a list of detected person and return it.
     * @param img the picture on which faces will be detected.
     * @return a List of detected people.
     */
    public List<IPerson> findFacesOnPicture(BufferedImage img) {
        Image imageAws = createAwsImage(img);
        DetectFacesRequest request = new DetectFacesRequest().withImage(imageAws)
                .withAttributes(Attribute.ALL);
        DetectFacesResult res = amazonRekognition.detectFaces(request);
        List<IPerson> persons = convertToPersons(res.getFaceDetails(),
                modele.getHeight(), modele.getWidth());
        return persons;
    }

    /**
     * This method creates a list of IPerson from a list of FaceDetail(Amazon object)
     * @param faceDetails list of AmazonFaceDetails - this list will be converted-
     * @param heightPic the height of the origin picture used to have the right ratio with the image
     * @param widthPic the width of the origin picture used to have the right ratio with the image
     * @return a List of IPerson from the FaceDetail list.
     */
    private List<IPerson> convertToPersons(List<FaceDetail> faceDetails,
                                           double heightPic, double widthPic) {
        List<IPerson> persons = new ArrayList<>();
        for (FaceDetail faceDetail : faceDetails)
            persons.add(createPersonFromFaceDetail(faceDetail, heightPic, widthPic));
        return persons;
    }

    /**
     * This method create a Rectangle from a Bouding box(ratio normalized) and an image dimension)
     * @param box the scale of a face.
     * @param width the width of the image
     * @param height the height of the image
     * @return a Rectangle 2D representing a face on a picture with the pixel value.
     */
    public Rectangle2D boxToRectangle(BoundingBox box, double width,
                                      double height) {
        int x, y;
        x = (int) (box.getLeft() * width);
        y = (int) (box.getTop() * height);
        width = (int) (box.getWidth() * width);
        height = (int) (box.getHeight() * height);
        Rectangle2D tr = new Rectangle2D(x, y, width, height);

        return tr;
    }

    /**
     * This method create a IPerson Bean from a FaceDetail(Amazon library object).
     * @param faceDetail the orignal details of the face
     * @param heightPic the height of the treated picture
     * @param widthPic the width of the treated picture
     * @return the created person.
     */
    private IPerson createPersonFromFaceDetail(FaceDetail faceDetail,
                                               double heightPic, double widthPic) {
        Rectangle2D rect = boxToRectangle(faceDetail.getBoundingBox(), widthPic,
                heightPic);
        // PersonAmazon(String id, Rectangle2D faceRectangle, String name, double
        // age, String gender, String smile, String glasses)
        boolean glasse = faceDetail.getEyeglasses().getValue().booleanValue()
                || faceDetail.getSunglasses().getValue().booleanValue();


        String glasses = glasse ? "Glasses" : "noGlasses";
        PersonAmazon person = new PersonAmazon("no id", rect, "no name",
                (faceDetail.getAgeRange().getLow() + faceDetail.getAgeRange().getHigh())
                        / 2,
                faceDetail.getGender().getValue(),
                faceDetail.getSmile().getConfidence() + "", glasses, "no info", null);
        return person;
    }

    /**
     * This method queries the API to know if it has faces with a great similarity
     * with the face passed as a parameter.
     *
     * @param subImg An image containing a single face
     * @return The list of faces with a great similarity
     */
    public List<FaceMatch> getMatch(BufferedImage subImg) {
        Float threshold = 70F;
        int maxFaces = 1;

        Image awsImage = createAwsImage(subImg);
        SearchFacesByImageResult res = null;
        try {
            SearchFacesByImageRequest searchFacesByImageRequest = new SearchFacesByImageRequest()
                    .withCollectionId(COLLECTION_ID).withImage(awsImage)
                    .withFaceMatchThreshold(threshold).withMaxFaces(maxFaces);

            res = amazonRekognition.searchFacesByImage(searchFacesByImageRequest);
            return res.getFaceMatches();

        } catch (Exception e) {
            System.out.println("Problem to test the people");
            return new ArrayList<FaceMatch>();
        }
    }

    /**
     * This method adds a person in the amazon collection provides in the constant
     * file.
     *
     * @param imgBuff An image containing a single face of the person to add
     * @param name    The name to assign to the added person
     * @return returns true if the addition was successful. Returns false if there
     * is a problem during the add.
     */
    public boolean addFace(BufferedImage imgBuff, String name) {
        Image imageAws = createAwsImage(imgBuff);
        try {
            IndexFacesResult res = callAddFace(COLLECTION_ID, name, "ALL", imageAws);
            if (res != null)return true;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * This method makes api calls for adding a face on the database
     *
     * @param collectionId    the collection who the face will be add
     * @param externalImageId the id of the added/modified face(the name in this app)
     * @param attributes      defines which parameters should be kept(here ALL)
     * @param image           of type Amazon n which will be based the new person or the
     *                        improvement of a person
     * @return
     */
    private IndexFacesResult callAddFace(String collectionId,
                                         String externalImageId, String attributes, Image image) {
        IndexFacesRequest indexFacesRequest = new IndexFacesRequest()
                .withImage(image).withCollectionId(collectionId)
                .withExternalImageId(externalImageId)
                .withDetectionAttributes(attributes);
        return amazonRekognition.indexFaces(indexFacesRequest);
    }

    /**
     * This method creates an Amazon-like image that allows API calls from a
     * BufferedImage image
     *
     * @param imgBuff image going to be converted into an amazon image
     * @return An Amazon image
     */
    private Image createAwsImage(BufferedImage imgBuff) {
        Image awsImage = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(imgBuff, "jpg", baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            awsImage = new Image().withBytes(ByteBuffer.wrap(imageInByte));
        } catch (IOException e) {
            System.err.println("Error to add the image");
            System.err.println(e.getMessage());
        }
        return awsImage;
    }

}
