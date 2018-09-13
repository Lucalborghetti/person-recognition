package main.java.model.microsoft;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.geometry.Rectangle2D;
import main.java.bean.IPerson;
import main.java.bean.microsoft.PersonMicrosoft;
import main.java.model.IMainViewModel;
import main.java.model.microsoft.api.Face_API;
import main.java.model.microsoft.api.PersonGroup_API;
import main.java.model.microsoft.api.Person_API;

/**
 * This class is the model implemantation of Microsoft.
 */
public class MainViewModelMicrosoft implements IMainViewModel {

  // --------------------------------------------------------------
  // Attributs
  // --------------------------------------------------------------

  /** All persons list **/
  private List<IPerson> allPersons;

  /** Known persons list **/
  private List<IPerson> knownPersons;

  // --------------------------------------------------------------
  // Methods
  // --------------------------------------------------------------

  /**
   * Constructor of the Microsoft Model.
   */
  public MainViewModelMicrosoft() {
  }

  @Override
  public List<IPerson> detectFaces(String imagePath) {
    // Create a new table of allPersons
    allPersons = new ArrayList<IPerson>();
    // Detect the faces
    JSONArray jsonArray = Face_API.detectFaces(imagePath);
    if (jsonArray != null && jsonArray.length() > 0) {
      int nbFaces = jsonArray.length();
      if (nbFaces > 0)
        for (int i = 0; i < nbFaces; i++) {
          // FACE ID
          String id = jsonArray.getJSONObject(i).getString("faceId");
          // FACE RECTANGLE
          JSONObject jsonObjFaceRectangle = jsonArray.getJSONObject(i)
              .getJSONObject("faceRectangle");
          int left = jsonObjFaceRectangle.getInt("left");
          int top = jsonObjFaceRectangle.getInt("top");
          int width = jsonObjFaceRectangle.getInt("width");
          int height = jsonObjFaceRectangle.getInt("height");
          Rectangle2D faceRectangle = new Rectangle2D(left, top, width, height);
          // FACE NAME
          String name = "Unknown"; // By default
          // FACE INFORMATIONS
          String informations = "No informations"; // By default
          // FACE ATTRIBUTES
          JSONObject jsonObjFaceAttributes = jsonArray.getJSONObject(i)
              .getJSONObject("faceAttributes");
          double age = jsonObjFaceAttributes.getDouble("age");
          String gender = jsonObjFaceAttributes.getString("gender");
          String smile = "" + jsonObjFaceAttributes.getDouble("smile");
          String glasses = jsonObjFaceAttributes.getString("glasses");
          // FACE
          BufferedImage face = null;
          try {
            BufferedImage img = ImageIO.read(new File(imagePath));
            face = img.getSubimage(left, top, width, height);
          } catch (IOException e) {
            e.printStackTrace();
          }
          // Add the new face into the table of faces
          allPersons.add(new PersonMicrosoft(id, faceRectangle, name, age,
              gender, smile, glasses, informations, face));
        }
    }
    // Return value
    return allPersons;
  }

  @Override
  public List<IPerson> identifyFaces(String imagePath) {
    // Create a new table of knownPersons
    knownPersons = new ArrayList<IPerson>();
    // Get the personId of every detected persons
    String[] faceIds = new String[allPersons.size()];
    for (int i = 0; i < allPersons.size(); i++)
      faceIds[i] = allPersons.get(i).getId();
    // The maximum number of candidates
    int maxNumOfCandidatesReturned = 5;
    // The confidence threshold
    double confidenceThreshold = 0.5;
    // Identify the faces
    JSONArray jsonArray = Face_API.identifyFaces(faceIds,
        maxNumOfCandidatesReturned, confidenceThreshold);
    if (jsonArray != null && jsonArray.length() > 0)
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONArray jsonArrayCandidates = jsonArray.getJSONObject(i)
            .getJSONArray("candidates");
        if (jsonArrayCandidates.length() > 0) {
          String personId = jsonArrayCandidates.getJSONObject(0)
              .getString("personId");
          double confidence = jsonArrayCandidates.getJSONObject(0)
              .getDouble("confidence");
          if (confidence > 0.5) {
            // Get the person who matched
            IPerson p = getPerson(personId);
            if (p != null) {
              System.out
                  .println(p.getId() + " ::: " + allPersons.get(i).getId());
              p.setId(allPersons.get(i).getId());
              p.setFaceRectangle(allPersons.get(i).getFaceRectangle());
              p.setAge(allPersons.get(i).getAge());
              p.setGender(allPersons.get(i).getGender());
              p.setSmile(allPersons.get(i).getSmile());
              p.setGlasses(allPersons.get(i).getGlasses());
              p.setAppears(allPersons.get(i).getAppears());
              p.setFace(allPersons.get(i).getFace());
              p.setKnown(true);
              knownPersons.add(p);
            }
          }
        }
      }
    // Return value
    return knownPersons;
  }

  @Override
  public boolean addFace(String imagePath, String name, String userData) {
    // Create a new person, with a name and some data (optionnal)
    JSONObject jsonObject = Person_API.createPerson(name, userData);
    if (jsonObject != null && jsonObject.has("personId")) {
      // Add a face to this new person
      JSONObject jsonObject2 = Person_API.addPersonFace(imagePath,
          jsonObject.getString("personId"), userData);
      if (jsonObject2 != null && jsonObject2.has("persistedFaceId")) {
        PersonGroup_API.trainPersonGroup();
        return true;
      }
    }
    return false;
  }

  /**
   * Retrieve a person's information, including registered persisted faces, name
   * and userData.
   *
   * @param personId
   *          - Specifying the target person
   * @return A successful call returns the person's information.
   */
  private IPerson getPerson(String personId) {
    // Get the person depending on the given personId
    JSONObject jsonObject = Person_API.getPerson(personId);
    if (jsonObject != null) {
      // Get the name and user data
      String name = "";
      if (jsonObject.has("name"))
        name = jsonObject.getString("name");
      String userData = "";
      if (jsonObject.has("userData") && !jsonObject.isNull("userData"))
        userData = jsonObject.getString("userData");
      // Return a person with the right name et user data
      return new PersonMicrosoft(personId, new Rectangle2D(0.0, 0.0, 0.0, 0.0),
          name, 0.0, "", "", "", userData, null);
    } else
      return null;
  }

}
