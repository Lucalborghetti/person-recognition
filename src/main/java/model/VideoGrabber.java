package main.java.model;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.*;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import main.java.controller.MainViewController;
import main.java.utils.Utils;

/**
 * Thread class used to cut a video in some pictures.
 */
public class VideoGrabber implements Runnable {

  /**
   * path of the video who will be cutted.
   */
  private final String       videoPath;
  /**
   * The imageView to update with the cutted frame.
   */
  private final ImageView    view;
  /**
   * The desired number of frame per second.
   */
  private final int          framePerSecond;
  /**
   * index of the current frame in the video
   */
  private int                cFrame         = 0;
  /**
   * number of cutted frames
   */
  private int                counter        = 0;
  /**
   * OpenCV converter used to create Mat.
   */
  OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
  /**
   * Ref to the control to update views and informations.
   */
  private MainViewController ctrl;
  /**
   * Grabber used to control the video and obtains images.
   */
  FrameGrabber               grabber;
  /**
   * boolean used to stop the thread
   */
  private volatile boolean   running        = true;
  /**
   * boolean used to save the cutted picture or not
   */
  private boolean            needSave;

  /**
   * This constructor assigns the basic attributes of the grabber.
   * @param videoPath the path of the video going to be cut
   * @param ctrl a ref to the ctrl to update views
   * @param view the view who need the update with the new picture
   * @param framesPerSecond the desired frame per second for the video
   */
  public VideoGrabber(String videoPath, MainViewController ctrl,
      ImageView view, int framesPerSecond) {
    cFrame = 0;
    this.videoPath = videoPath;
    this.ctrl = ctrl;
    this.view = view;
     grabber = new OpenCVFrameGrabber(videoPath);
    //grabber = new FFmpegFrameGrabber(mainImageFile);
    framePerSecond = framesPerSecond;
  }

  /**
   * This thread cut a video in multiple picture and update the view with the new image.
   *
   */
  @Override
  public void run() {
    try {
      grabber.start();
      long time = grabber.getLengthInTime();
      long frames = grabber.getLengthInFrames();
      double oneFrame2seconds = framePerSecond / grabber.getFrameRate();
      int frameGap = (int) oneFrame2seconds;
      int nbFrames = grabber.getLengthInFrames();
      // Declare img as IplImage
      opencv_core.IplImage img;

      // inser grabed video fram to IplImage img
      while (running) {
        Thread.sleep(framePerSecond);
        if (ctrl.isBoxSelected()) {
          Frame frame = grabber.grab();
          cFrame += frameGap;
          if (cFrame > nbFrames) { // For the last call
            grabber.setFrameNumber(nbFrames - 20);
            frame = grabber.grab();
            createImageAndSout(frame);
            break;
          }
          createImageAndSout(frame);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * This method create a mat from a frame and pass it to the controller.
   * @param frame the origin frame
   */
  private void createImageAndSout(Frame frame) {
    try {
      grabber.setFrameNumber(cFrame);
    } catch (FrameGrabber.Exception e) {
      e.printStackTrace();
    }
    opencv_core.Mat image = converterToMat.convert(frame);
    if (image != null) {
      ++counter;
      Image imageToShow = Utils.mat2Image(image);
      if (needSave)
        ctrl.saveVideoFrame(image, counter);
      Platform.runLater(() -> {
        ctrl.updateImageView(view, imageToShow);
        ctrl.updateTimeLabelFrameNumber(counter);
      });
    }
  }

  /**
   * Simple getter of thread running status.
   * @return true if the thread is running, false otherwise
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Simple setter of thread running status.
   * @param running set true or false to change thread status and stop it.
   */
  public void setRunning(boolean running) {
    this.running = running;
  }

  /**
   * Simple getter to know if the pictures will be saved.
   * @return true if the pictures will be save, false otherwise
   */
  public boolean isNeedSave() {
    return needSave;
  }

  /**
   * Simple setter to change the save status.
   * @param needSave set true if the frames will be saved, false otherwise.
   */
  public void setNeedSave(boolean needSave) {
    this.needSave = needSave;
  }
}
