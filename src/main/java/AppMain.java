package main.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import main.java.controller.MainViewController;

public class AppMain extends Application {

  // --------------------------------------------------------------
  // Attributs
  // --------------------------------------------------------------

  /** Controller of the main view **/
  private MainViewController controller;

  // --------------------------------------------------------------
  // Methods
  // --------------------------------------------------------------

  /**
   * Main method
   *
   * @param args
   *          optionnal args
   */
  public static void main(String[] args) {
    launch(args);
  }

  /**
   * On start application
   */
  @Override
  public void start(Stage primaryStage) throws Exception {
    try {
      controller = new MainViewController();
      FXMLLoader loader = new FXMLLoader();
      loader.setController(controller);
      loader.setLocation(getClass().getResource("/view/MainView.fxml"));
      BorderPane root = (BorderPane) loader.load();
      Scene scene = new Scene(root);
      primaryStage.setScene(scene);
      primaryStage.getIcons().add(
          new Image(getClass().getResource("/img/default.png").toString()));
      primaryStage.setTitle("Identification et reconnaissance d'individus");
      primaryStage.setResizable(false);
      primaryStage.show();
      setUserAgentStylesheet(STYLESHEET_CASPIAN);
      root.requestFocus();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * On stop application
   */
  @Override
  public void stop() {
    controller.exitApplication(null);
  }

}