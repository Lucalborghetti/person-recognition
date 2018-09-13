package main.java.bean;

import java.awt.image.BufferedImage;
import java.util.List;

import javafx.geometry.Rectangle2D;

/**
 * This interface defines the method who a Person need to have to be compatible with the controller.
 */
public interface IPerson {

  // Person Id

  /**
   * @return the id of the person
   */
  public String getId();

  /**
   * @param id
   *          set the uniq id of the person
   */
  public void setId(String id);

  // Person Name

  /**
   * @return the name of the person
   */
  public String getName();

  /**
   * @param name
   *          set the name of the person
   */
  public void setName(String name);

  // Person Age

  /**
   * @return the (estimated) age of the person
   */
  public double getAge();

  /**
   * @param age
   *          set the (estimated) age
   */
  public void setAge(double age);

  // Person Gender

  /**
   * @return the gender of the person
   */
  public String getGender();

  /**
   * @param gender
   *          set the gender of this person
   */
  public void setGender(String gender);

  // Person Glasses

  /**
   * @return informations if the person is wearing a type of glasses.
   */
  public String getGlasses();

  /**
   * @param glasses
   *          set information if the detected user is wearing some glasses
   */
  public void setGlasses(String glasses);

  // Person Smile

  /**
   * @return a value from 0 to 100 contening the smiling rating of the person
   */
  public String getSmile();

  /**
   * @param smile
   *          set value from 0 to 100 containing the smiling rating of the
   *          person
   */
  public void setSmile(String smile);

  // Person Informations

  /**
   * @return some extra informations(in String)
   */
  public String getInformations();

  /**
   * @param informations
   *          set some extra informations
   */
  public void setInformations(String informations);

  // Person Face Rectangle

  /**
   * @return the informations containing the postion of the person face on
   *         picture (in Rectangle)
   */
  public Rectangle2D getFaceRectangle();

  /**
   *
   * @param rectangle
   *          set the informations containing the postion of the person face on
   *          picture
   */
  public void setFaceRectangle(Rectangle2D rectangle);

  // Person appearances

  /**
   * @return the list of the apparitions of the persons on some objects(frames,
   *         or pictures,...)
   */
  public List<String> getAppears();

  /**
   * @param appears
   *          set the list of the apparitions of the persons on some objects
   */
  public void setAppears(List<String> appears);

  // Person face

  /**
   * @return an image containing the face of the person
   */
  public BufferedImage getFace();

  /**
   * @param face
   *          set an image containing the face of the person
   */
  public void setFace(BufferedImage face);

  // Person is known

  /**
   * @return true if the person is known of false otherwise
   */
  public boolean isKnown();

  /**
   * @param known
   *          set if the person is known(true if the person is known of false
   *          otherwise)
   */
  public void setKnown(boolean known);

}
