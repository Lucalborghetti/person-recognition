package main.java.bean.microsoft;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Rectangle2D;
import main.java.bean.IPerson;

/**
 * This class represents a Person used by the Microsoft model. It saves the api information into a person bean.
 */
public class PersonMicrosoft implements IPerson {

  // --------------------------------------------------------------
  // Attributs
  // --------------------------------------------------------------

  /** Unique faceId created by detection API **/
  private String        id;

  /** A rectangle area for the face location on image **/
  private Rectangle2D   faceRectangle;

  /** The name of the person's face (the user give it) **/
  private String        name;

  /** An estimated age in years. **/
  private double        age;

  /** Male or female **/
  private String        gender;

  /** Smile intensity, a number between [0,1] **/
  private String        smile;

  /** Glasses type **/
  private String        glasses;

  /** Personnal informations **/
  private String        informations;

  /** Appearances **/
  private List<String>  appears;

  /** Image of the person's face **/
  private BufferedImage face;

  /** Person is known **/
  private boolean       known;

  // --------------------------------------------------------------
  // Methods
  // --------------------------------------------------------------

  /**
   * Constructor of the PersonMicrosoft class.
   *
   * @param id
   *          Unique faceId created by detection API
   * @param faceRectangle
   *          A rectangle area for the face location on image
   * @param name
   *          The name of the person's face (the user give it)
   * @param age
   *          An estimated age in years
   * @param gender
   *          Male or female
   * @param smile
   *          Smile intensity, a number between [0,1]
   * @param glasses
   *          Glasses type
   * @param informations
   *          Personnal informations
   * @param face
   *          Image of the person's face
   */
  public PersonMicrosoft(String id, Rectangle2D faceRectangle, String name,
      double age, String gender, String smile, String glasses,
      String informations, BufferedImage face) {
    this.id = id;
    this.faceRectangle = faceRectangle;
    this.name = name;
    this.age = age;
    this.gender = gender;
    this.smile = smile;
    this.glasses = glasses;
    this.informations = informations;
    this.face = face;
    appears = new ArrayList<>();
    known = false;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;

  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;

  }

  @Override
  public double getAge() {
    return age;
  }

  @Override
  public void setAge(double age) {
    this.age = age;
  }

  @Override
  public String getGender() {
    return gender;
  }

  @Override
  public void setGender(String gender) {
    this.gender = gender;
  }

  @Override
  public String getGlasses() {
    return glasses;
  }

  @Override
  public void setGlasses(String glasses) {
    this.glasses = glasses;

  }

  @Override
  public String getSmile() {
    return smile;
  }

  @Override
  public void setSmile(String smile) {
    this.smile = smile;

  }

  @Override
  public Rectangle2D getFaceRectangle() {
    return faceRectangle;
  }

  @Override
  public void setFaceRectangle(Rectangle2D rectangle) {
    faceRectangle = rectangle;

  }

  @Override
  public List<String> getAppears() {
    return appears;
  }

  @Override
  public void setAppears(List<String> appears) {
    this.appears = appears;
  }

  @Override
  public String getInformations() {
    return informations;
  }

  @Override
  public void setInformations(String informations) {
    this.informations = informations;
  }

  @Override
  public BufferedImage getFace() {
    return face;
  }

  @Override
  public void setFace(BufferedImage face) {
    this.face = face;
  }

  @Override
  public boolean isKnown() {
    return known;
  }

  @Override
  public void setKnown(boolean known) {
    this.known = known;
  }
}
