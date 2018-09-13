package main.java.model.amazon;

import com.amazonaws.AbortedException;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rekognition.model.FaceMatch;
import javafx.geometry.Rectangle2D;
import main.java.bean.IPerson;
import main.java.bean.amazon.PersonAmazon;
import main.java.model.IMainViewModel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static main.java.constants.Constants.ACCESS_KEY;
import static main.java.constants.Constants.SECRET_KEY;

/**
 * This class is the main model of the Amazon part. It receive calls from the controller and return the asked data.
 */
public class MainViewModelAmazon implements IMainViewModel {
    /**
     * subclass used to do the calls to the Amazon API
     */
    private AwsCaller awsCaller;

    /**
     * List of detected faces
     */
    private List<IPerson> allPersons;
    /**
     * List of identified faces
     */
    private List<IPerson> knownPersons;
    /**
     * current working image
     */
    private BufferedImage currImage;

    /**
     * Constructor initializes the authentication params and create the api caller.
     */
    public MainViewModelAmazon() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                ACCESS_KEY, SECRET_KEY);
        try {
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. "
                            + "Please make sure that your credentials file is at the correct "
                            + "location (/Users/userid.aws/credentials), and is in a valid format.",
                    e);
        }
        awsCaller = new AwsCaller(awsCredentials, this);
        knownPersons = new ArrayList<>();
    }

    @Override
    public List<IPerson> detectFaces(String imagePath) {
        try {
            currImage = ImageIO.read(new File(imagePath));
            allPersons = awsCaller.findFacesOnPicture(currImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // catch quand le thread est arrete alors que l on attend la rep d amazon, rien de particulier a faire
        catch (AbortedException ae) {

        }

        return allPersons;
    }

    @Override
    public List<IPerson> identifyFaces(String imagePath) {
        knownPersons.clear();
        if(allPersons == null) return new ArrayList<>();
        for (int index = 0; index < allPersons.size(); index++) {
            Rectangle2D faceCut = allPersons.get(index).getFaceRectangle();
            int minX = ((int) faceCut.getMinX() < 0) ? 0 : (int) faceCut.getMinX();
            int minY = ((int) faceCut.getMinY() < 0) ? 0 : (int) faceCut.getMinY();
            int exceedX = (int) (faceCut.getMinX() + faceCut.getWidth() - currImage.getWidth());
            int exceedY = (int) (faceCut.getMinY() + faceCut.getHeight() - currImage.getHeight());
            if (exceedX < 0) exceedX = 0;
            if (exceedY < 0) exceedY = 0;
            BufferedImage subImg = currImage.getSubimage(minX,
                    minY, (int) faceCut.getWidth() - exceedX,
                    (int) faceCut.getHeight() - exceedY);
            List<FaceMatch> matches = null;
            try {
                matches = awsCaller.getMatch(subImg);
            }
            // catch quand le thread est arrete alors que l on attend la rep d amazon, rien de particulier a faire
            catch (AbortedException ae) {
                return knownPersons;
            }
            if (matches.size() != 0 && index < allPersons.size()) {
                IPerson persoAdd = allPersons.get(index);
                persoAdd.setFace(subImg);
                persoAdd.setKnown(true);
                persoAdd.setName(matches.get(0).getFace().getExternalImageId());
                knownPersons.add(persoAdd);
            }
        }
        return knownPersons;
    }


    @Override
    public boolean addFace(String imagePath, String name, String userData) {
        return awsCaller.addFace(pathToBuffered(imagePath), name);
    }

    /**
     * Method used to know the height of the current picture.
     * @return the height of the current picture
     */
    public double getHeight() {
        return currImage.getHeight();
    }
    /**
     * Method used to know the width of the current picture.
     * @return the width of the current picture
     */
    public double getWidth() {
        return currImage.getWidth();
    }

    /**
     * The method create a BufferedImage from a file Path.
     * @param path the path of the image
     * @return the created buffered image
     */
    private BufferedImage pathToBuffered(String path) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }
}
