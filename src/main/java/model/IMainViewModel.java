package main.java.model;

import java.util.List;

import main.java.bean.IPerson;

public interface IMainViewModel {

  /**
   * Detect human faces in an image and create a table of persons (with
   * informations like the id, gender, smile, ...) based on those faces.
   *
   * @param imagePath
   *          - The path of the image to detect
   * @return A successful call returns a list of persons ranked by face
   *         rectangle size in descending order. An empty response indicates no
   *         faces detected.
   */
  public List<IPerson> detectFaces(String imagePath);

  /**
   * Identify unknown faces from a person group. PRE : The "detectFaces" method
   * has do be called before calling this one.
   *
   * @param imagePath
   *          - The path of the image to identify
   * @return A successful call returns a list of known persons identified. An
   *         empty response indicates no known faces.
   */
  public List<IPerson> identifyFaces(String imagePath);

  /**
   * Create a new person by adding a representative face, a name and if desired
   * some data to a person for identification.
   *
   * @param imagePath
   *          - The path of the image which represents the person's face.
   * @param name
   *          - The name of the person
   * @param userData
   *          - Some data about the user (optionnal)
   * @return true if everything went good
   */
  public boolean addFace(String imagePath, String name, String userData);
}
