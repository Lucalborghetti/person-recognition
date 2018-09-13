package main.java.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import main.java.bean.IPerson;

/**
 * This class is the controller of the detected faces.
 */
public class DetectedFacesController {

  // --------------------------------------------------------------
  // Attributs
  // --------------------------------------------------------------

  @FXML
  private ImageView          faceImageView;

  @FXML
  private Label              nameLabel;

  @FXML
  private Button             showKnownInfoButton;

  @FXML
  private ComboBox<String>   apparitionsCmb;

  // --------------------------------------------------------------

  /** The main controller **/
  private MainViewController controller;

  /** The face/person displayed **/
  private IPerson            person;

  // --------------------------------------------------------------
  // Methods
  // --------------------------------------------------------------

  /**
   * Constructor of the DetectedFacesController class.
   *
   * @param controller
   *          - Reference to the main controller
   * @param person
   *          - The detected person
   */
  public DetectedFacesController(MainViewController controller,
      IPerson person) {
    super();
    this.person = person;
    this.controller = controller;
    Platform.runLater(() -> {
      if (this.person != null)
        display();
    });
  }

  /**
   * Set person's informations into the view.
   */
  private void display() {
    if (person.getFace() != null)
      faceImageView.setImage(SwingFXUtils.toFXImage(person.getFace(), null));
    if (person.getName() != null)
      nameLabel.setText(person.getName());
    if (person.getAppears() != null)
      apparitionsCmb.getItems()
          .addAll(FXCollections.observableArrayList(person.getAppears()));
  }

  /**
   * On button click, show the current person's personal informations.
   *
   * @param event
   *          - The event
   */
  @FXML
  void showInformations(ActionEvent event) {
    controller.displayPersonInformations(person);
  }

  /**
   * On combo box selection, shows the selected frame.
   *
   * @param event
   *          - The event
   */
  @FXML
  void showSelectedFrame(ActionEvent event) {
    controller.displayImage(apparitionsCmb.getValue());
  }
}
