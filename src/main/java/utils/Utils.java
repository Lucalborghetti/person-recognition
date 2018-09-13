package main.java.utils;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * Provide general purpose methods for handling OpenCV-JavaFX data conversion.
 * Moreover, expose some "low level" methods for matching few JavaFX behavior.
 *
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @author <a href="http://max-z.de">Maximilian Zuleger</a>
 * @version 1.0 (2016-09-17)
 * @since 1.0
 *
 */
public final class Utils {
  private static OpenCVFrameConverter.ToMat matConv = new OpenCVFrameConverter.ToMat();
  private static Java2DFrameConverter       biConv  = new Java2DFrameConverter();

  /**
   * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
   *
   * @param frame
   *          the {@link opencv_core.Mat} representing the current frame
   * @return the {@link Image} to show
   */
  public static Image mat2Image(opencv_core.Mat frame) {
    return SwingFXUtils.toFXImage(
        biConv.getBufferedImage(matConv.convert(frame).clone()), null);
  }

  /**
   * Generic method for putting element running on a non-JavaFX thread on the
   * JavaFX thread, to properly update the UI
   *
   * @param property
   *          a {@link ObjectProperty}
   * @param value
   *          the value to set for the given {@link ObjectProperty}
   */
  public static <T> void onFXThread(final ObjectProperty<T> property,
      final T value) {
    Platform.runLater(() -> {
      property.set(value);
    });
  }

}