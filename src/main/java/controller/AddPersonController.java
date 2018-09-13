package main.java.controller;

import static main.java.constants.Constants.USER_FRAMESTEMP_DIRECTORY;
import static main.java.constants.Constants.USER_WORKING_DIRECTORY;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import main.java.model.IMainViewModel;

/**
 * This class is the controller to add person.
 */
public class AddPersonController {

  @FXML
  private TextField      nameTxtField;

  @FXML
  private TextField      userDataTxtField;

  @FXML
  private Button         cancelButton;

  @FXML
  private Button         confirmButton;

  @FXML
  private ImageView      faceImageView;

  // --------------------------------------------------------------------------

  /** The main view model **/
  private IMainViewModel model;

  /** The image (face) path **/
  private String         imagePath;

  /** The image (face) **/
  private Image          faceImage;

  // --------------------------------------------------------------
  // Methods
  // --------------------------------------------------------------

  /**
   * Constructor of the AddPersonController class.
   *
   * @param model
   *          - The main view model reference
   */
  public AddPersonController(IMainViewModel model) {
    super();
    this.model = model;
    imagePath = USER_FRAMESTEMP_DIRECTORY + "subImage.jpg";
    File picture = new File(imagePath);
    faceImage = new Image(picture.toURI().toString());
  }

  /**
   * Initialize method.
   */
  @FXML
  public void initialize() {
    faceImageView.setImage(faceImage);
    initBindings();
  }

  /**
   * Bindings between components.
   */
  private void initBindings() {
    // Bindings here
    confirmButton.disableProperty().bind(nameTxtField.textProperty().isEmpty());
  }

  /**
   * Confirm the action.
   *
   * @param event
   *          - The event
   */
  @FXML
  private void confirm(ActionEvent event) {
    // Add the new person
    String name = nameTxtField.getText();
    String userData = userDataTxtField.getText();
    model.addFace(imagePath, name, userData);
    // Close the window
    cancel(null);
  }

  /**
   * Cancel the action.
   *
   * @param event
   *          - The event
   */
  @FXML
  private void cancel(ActionEvent event) {
    // Empty the text fields
    nameTxtField.setText("");
    userDataTxtField.setText("");
    // Close the window
    Stage stage = (Stage) cancelButton.getScene().getWindow();
    stage.close();
  }

}
