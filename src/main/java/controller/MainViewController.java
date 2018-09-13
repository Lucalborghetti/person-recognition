package main.java.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.bean.IPerson;
import main.java.model.IMainViewModel;
import main.java.model.VideoGrabber;
import main.java.model.amazon.MainViewModelAmazon;
import main.java.utils.FileDateComparator;
import main.java.utils.Utils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_videoio.VideoCapture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static main.java.constants.Constants.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;

/**
 * This class is the main controller of the application. It controls all actions
 * and choose wich API will be use between Amazon and Microsoft
 */
public class MainViewController {

    // --------------------------------------------------------------
    // Attributs
    // --------------------------------------------------------------

    @FXML
    private ImageView currentFrame;

    @FXML
    private CheckBox autorefreshCheckBox;

    @FXML
    private Button backwardButton;

    @FXML
    private Button forwardButton;

    @FXML
    private ImageView webcamStream;

    @FXML
    private Label timeLabel;

    @FXML
    private Button pauseButton;

    @FXML
    private Button playButton;

    @FXML
    private Label nameLabel;

    @FXML
    private Label informationsLabel;

    @FXML
    private Label ageLabel;

    @FXML
    private Label genderLabel;

    @FXML
    private Label smileLabel;

    @FXML
    private Label glassesLabel;

    @FXML
    private Label nbFacesLabel;

    @FXML
    private Label currentFrameTime;

    @FXML
    private Button addFaceButton;

    @FXML
    private Button detectButton;

    @FXML
    private Button identifyButton;

    @FXML
    private Button browseButton;

    @FXML
    private VBox detectedFacesBox;

    // --------------------------------------------------------------

    /**
     * The model used to detect and identify people
     **/
    private IMainViewModel model;

    /**
     * The absolute path of the current frame
     **/
    private String currentFramePath;

    /**
     * The absolute path of the selected face
     **/
    private String currentFacePath;

    /**
     * The list of all persons
     **/
    private List<IPerson> allPersons;

    /**
     * The list of persons per frame, used for select person on a picture
     **/
    private HashMap<Integer, List<IPerson>> peopleFrames;

    /**
     * The main image file
     **/
    private File mainImageFile;

    /**
     * The file where the faces are detected and drawed
     **/
    private File fileDrawed;

    /**
     * The thread used to detect faces, calls the model
     **/
    private Thread threadDetect;

    /**
     * The thread used to identify faces, calls the model
     **/
    private Thread threadIdentify;

    /**
     * The timer for acquiring the current time
     **/
    private ScheduledExecutorService currentTimeTimer;

    /**
     * The hashmap of apparitions
     **/
    private HashMap<String, ArrayList<String>> appears;

    /**
     * The timer for acquiring the video stream
     **/
    private ScheduledExecutorService timerStream;

    /**
     * The timer for acquiring the video stream
     **/
    private ScheduledExecutorService timerCapture;

    /**
     * The OpenCV object that realizes the video capture
     **/
    private VideoCapture capture;

    /**
     * The flag to change the button behavior
     **/
    private boolean cameraActive;

    /**
     * The id of the camera to be used
     **/
    private static int cameraId;

    /**
     * Thread used to listen video files
     */
    private VideoGrabber videoGrabber;

    /**
     * Used to know if we are in video mode or webcam mode
     */
    private boolean videoMode;

    /**
     * Thread used to cut video in some frames
     */
    private Thread videoThread;

    private int currentFrameIndex;

    private String videoName;

    private FileDateComparator fileDateComparator;
    // --------------------------------------------------------------
    // Methods
    // --------------------------------------------------------------

    /**
     * This constructor initialize the different used lists and clean the temp
     * file directory and choose wich API will be use. It initialize the camera id
     * too.
     */
    public MainViewController() {
        // ------------------------
        model = new MainViewModelAmazon();
        // ------------------------
        allPersons = new ArrayList<>();
        appears = new HashMap<>();
        capture = new VideoCapture();
        cameraActive = false;
        cameraId = 1;
        fileDateComparator = new FileDateComparator();
        peopleFrames = new HashMap<>();
    }

    /**
     * This method initialize the different bindings between buttons
     */
    @FXML
    void initialize() {
        // Webcam
        pauseButton.setDisable(true);
        // Informations
        addFaceButton.setDisable(true);
        identifyButton.setDisable(true);
    }

    @FXML
    /**
     * This method is called when the user click on the browse button. It has two
     * behavior the first one is for an image -> The image is showed on the right
     * view. the second one is for a video -> The video is played in the left view
     * and in the second view, like a direct movie
     */
    private void browse(ActionEvent event) {
        allPersons = new ArrayList<IPerson>();
        final FileChooser dialog = new FileChooser();
        mainImageFile = dialog.showOpenDialog(null);
        if (mainImageFile == null)
            return;
        currentFramePath = mainImageFile.getAbsolutePath();
        String type = getFileExtension(currentFramePath);
        if (type.equalsIgnoreCase("jpg") || type.equalsIgnoreCase("png")
                || type.equalsIgnoreCase("jpeg")) {
            Image image = new Image(new File(currentFramePath).toURI().toString());
            currentFrame.setImage(image);
        }
        else if (type.equalsIgnoreCase("mp4") || type.equalsIgnoreCase("mpg")) {
            videoMode = true;
            videoName = mainImageFile.getName().replace('.', '_');
            preparePlayer();
            launchVideo();
        }
    }

    /**
     * This method start the thread managing the video
     */
    private void launchVideo() {
        videoThread.start();
    }

    /**
     * This method prepare the thread for cutting a video into some frame images.
     */
    private void preparePlayer() {
        videoGrabber = new VideoGrabber(currentFramePath, this, currentFrame, 1500);
        videoGrabber.setNeedSave(true);
        videoThread = new Thread(videoGrabber);

    }

    /**
     * This method is used to obtains the extension of a file name
     *
     * @param fullName the name of the file, absolut or relatif.
     * @return a String representing the extension of the fullName
     */
    private String getFileExtension(String fullName) {
        String fileName = new File(fullName).getName();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1);
    }

    /**
     * PRE-CONDITON : a picture is already showed on the right view This method is
     * called when the user click on the detect button. It create a thread who it
     * asks the model to obtain a list of detected persons from the current frame
     * path and call the method to draw Rectangle on the faces.
     *
     * @param event not used
     */
    @FXML
    void detect(ActionEvent event) {
        Runnable runner = () -> {
            List<IPerson> persons = model.detectFaces(currentFramePath);
            if (persons.size() > 0) {
                peopleFrames.put(currentFrameIndex, persons);
                Platform.runLater(() -> {
                    drawRectangleAllPersons(persons);
                });
                addFaceButton.setDisable(false);
                identifyButton.setDisable(false);
            }
        };
        threadDetect = new Thread(runner);
        threadDetect.start();
    }

    /**
     * PRE-CONDITON : a picture is already showed on the right view and the faces
     * on this picture are already detected This method is called when the user
     * click on the identify button. It create a thread who it asks the model to
     * obtain a list of known persons from the current frame path and call the
     * method to draw Rectangle on the known faces. It updates the list of persons
     * and update the FX list with the new known persons.
     *
     * @param event not used
     */
    @FXML
    void identify(ActionEvent event) {
        identify(currentFrameTime.getText());
    }

    private void identify(String time){
        Runnable runner = () -> {
            List<IPerson> knownPersons = model.identifyFaces(currentFramePath);
            if (!knownPersons.isEmpty()) {
                Platform.runLater(() -> {
                    // If there is no one, add everyone
                    if (allPersons.isEmpty())
                        for (IPerson person : knownPersons) {
                            person.getAppears().add(time);
                            allPersons.add(person);
                        }
                    else {
                        boolean alreadyExists;
                        IPerson alreadyIdentifiedPerson = null;
                        // Else, add the person only if he isnt already in
                        List<IPerson> thingsToBeAdd = new ArrayList<>();
                        for (IPerson newIdentifiedPerson : knownPersons) {
                            alreadyExists = false;
                            for (Iterator<IPerson> it = allPersons.iterator(); it
                                    .hasNext(); ) {
                                alreadyIdentifiedPerson = it.next();
                                if (newIdentifiedPerson.getName()
                                        .equals(alreadyIdentifiedPerson.getName())) {
                                    alreadyExists = true;
                                    break;
                                }
                            }
                            if (alreadyExists)
                                alreadyIdentifiedPerson.getAppears()
                                        .add(time);
                            else {
                                newIdentifiedPerson.getAppears().add(time);
                                thingsToBeAdd.add(newIdentifiedPerson);
                            }
                        }
                        allPersons.addAll(thingsToBeAdd);
                    }
                    //   drawRectangleKnownPersons(knownPersons);
                    detectedFacesBox.getChildren().clear();
                    System.out.println("AllPersons Size : " + allPersons.size());
                    for (IPerson person : allPersons) {
                        if (person.isKnown()) {
                            addNewDetectedFace(person);
                        }
                    }
                });
            }
        };
        threadIdentify = new Thread(runner);
        threadIdentify.start();
    }

    /**
     * PRE-CONDITION: A face is selected by the user before This action triggered
     * by pushing the addFace button on the GUI.
     *
     * @param event
     */
    @FXML
    private void addFace(ActionEvent event) {
        openAddFaceDialog();
    }

    /**
     * The action triggered by pushing the start button on the GUI.
     *
     * @param event not used
     */
    @FXML
    void play(ActionEvent event) {
        startCamera();
        displayCurrentTime();

    }

    /**
     * The action triggered by pushing the pause button on the GUI.
     *
     * @param event not used
     */
    @FXML
    private void pause(ActionEvent event) {
        try {
            stopCamera();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * The action triggered by pushing the backward button on the GUI.
     *
     * @param event not used
     */
    @FXML
    private void backward(ActionEvent event) {
        displayPreviousImage();
    }

    /**
     * The action triggered by pushing the forward button on the GUI.
     *
     * @param event not used
     */
    @FXML
    private void forward(ActionEvent event) {
        displayNextImage();
    }

    /**
     * This method display the right image on the right box
     *
     * @param id used to find the right file in the working directory
     */
    public void displayImage(String id) {
        String path;
        if (videoMode) {
            int newid = Integer.parseInt(id.substring(1));
            path = USER_WORKING_DIRECTORY + "/" + videoName + "_Frame" + newid
                    + ".jpg";
            currentFrameIndex = newid;
        }
        else
            path = USER_WORKING_DIRECTORY + "/" + formattedDateToFilename(id)
                    + ".jpg";
        if (SystemUtils.IS_OS_WINDOWS)
            path = path.replace("/", "\\");
        File imageToDisplay = new File(path);
        if (imageToDisplay != null && imageToDisplay.isFile())
            updateCurrentFrame(imageToDisplay);
    }

    /**
     * This method show the previous picture on the right box and update the index
     * of the current frame.
     */
    private void displayPreviousImage() {
        File[] files = new File(USER_WORKING_DIRECTORY).listFiles();
        Arrays.sort(files, fileDateComparator);
        int indexInFiles = -1;
        if (videoMode) {
            if (currentFrameIndex < 2)
                return;
            currentFrameIndex--;
            indexInFiles = currentFrameIndex;
        }
        else
            indexInFiles = retrieveFileIndex(currentFramePath);
        if (indexInFiles > 0)
            updateCurrentFrame(files[--indexInFiles]);
    }

    /**
     * This method show the next picture on the right box and update the index of
     * the current frame.
     */
    private void displayNextImage() {
        File[] files = new File(USER_WORKING_DIRECTORY).listFiles();
        Arrays.sort(files, fileDateComparator);
        if (videoMode) {
            if (currentFrameIndex > files.length - 1)
                return;
            currentFrameIndex++;
            updateCurrentFrame(files[currentFrameIndex - 1]);
            return;
        }
        int currentFramIndex = retrieveFileIndex(currentFramePath);
        if (currentFramIndex < files.length - 1)
            updateCurrentFrame(files[++currentFramIndex]);
    }

    /**
     * This method retrieves the index of the given file path in its directory.
     *
     * @param filePath The file path
     * @return The index of the file in the directory
     */
    private int retrieveFileIndex(String filePath) {
        boolean isWindowSys = SystemUtils.IS_OS_WINDOWS;
        File[] files = new File(USER_WORKING_DIRECTORY).listFiles();
        String name;
        if (isWindowSys)
            name = filePath.replace("/", "\\");
        else
            name = filePath;
        int currentFramIndex = Integer.MAX_VALUE;
        for (int i = 0; i < files.length; i++)
            if (files[i].isFile() && files[i].getAbsolutePath().equals(name)) {
                currentFramIndex = i;
                break;
            }
        return currentFramIndex;
    }

    /**
     * This method updates the current frame links by replacing them by the new
     * file.
     *
     * @param newFile The new file which is displayed in the current frame
     */
    private void updateCurrentFrame(File newFile) {
        mainImageFile = newFile;
        currentFramePath = newFile.getAbsolutePath();
        currentFrame.setImage(new Image(newFile.toURI().toString()));
        if (videoMode)
            currentFrameTime.setText("#" + currentFrameIndex);
        else
            currentFrameTime.setText(
                    filenameToFormattedDate(FilenameUtils.getBaseName(currentFramePath)));
    }

    /**
     * Transforme the formatted date received into the filename format.
     *
     * @param date the date with format "yyyy/MM/dd HH:mm:ss"
     * @return the date with format "yyyyMMdd_HHmmss"
     */
    private String formattedDateToFilename(String date) {
        String year = date.substring(0, 4);
        String month = date.substring(5, 7);
        String day = date.substring(8, 10);
        String hour = date.substring(11, 13);
        String min = date.substring(14, 16);
        String sec = date.substring(17, 19);
        return year + month + day + "_" + hour + min + sec;
    }

    /**
     * Transform the filename date received into the formatted format.
     *
     * @param date the date with format "yyyyMMdd_HHmmss"
     * @return the date with format "yyyy/MM/dd HH:mm:ss"
     */
    private String filenameToFormattedDate(String date) {
        String year = date.substring(0, 4);
        String month = date.substring(4, 6);
        String day = date.substring(6, 8);
        String hour = date.substring(9, 11);
        String min = date.substring(11, 13);
        String sec = date.substring(13, 15);
        return year + "/" + month + "/" + day + " " + hour + ":" + min + ":" + sec;
    }

    /**
     * This method create a detectedFace controller and add it into the
     * detectedFacesBox.
     *
     * @param iPerson the known person who will add on the bottom view view his
     *                detectedFace controller.
     */
    private void addNewDetectedFace(IPerson iPerson) {
        try {
            DetectedFacesController controller = new DetectedFacesController(this,
                    iPerson);
            FXMLLoader loader = new FXMLLoader();
            loader.setController(controller);
            loader
                    .setLocation(getClass().getResource("/view/DetectedFacesView.fxml"));
            HBox newDetectedFace = (HBox) loader.load();
            // Add the detected face
            detectedFacesBox.getChildren().add(newDetectedFace);
            detectedFacesBox.getChildren().add(new Separator(Orientation.HORIZONTAL));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * This method create a runnable who displays the current time into the label
     * and launch it.
     */
    private void displayCurrentTime() {
        DateFormat currentTime = new SimpleDateFormat(DISPLAY_DATE_FORMAT);
        int interval = 1000;
        Runnable currentTimeRunner = () -> {
            Platform.runLater(() -> {
                timeLabel.setText(currentTime.format(Calendar.getInstance().getTime()));
            });
        };
        currentTimeTimer = Executors.newSingleThreadScheduledExecutor();
        currentTimeTimer.scheduleAtFixedRate(currentTimeRunner, 0, interval,
                TimeUnit.MILLISECONDS);
    }

    /**
     * Stop the threads used to detect and identfy people.
     */
    private void stopThreads() {
        if (videoGrabber != null)
            videoGrabber.setRunning(false);
        if (threadDetect != null && threadDetect.isAlive())
            try {
                threadDetect.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        if (threadIdentify != null && threadIdentify.isAlive())
            try {
                threadIdentify.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        if (currentTimeTimer != null && !currentTimeTimer.isShutdown())
            try {
                // stop the timer
                currentTimeTimer.shutdown();
                currentTimeTimer.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // log any exception
                System.err.println(
                        "Exception in stopping the current time timer, trying to release it now... "
                                + e);
            }
    }

    /**
     * This method is triggered when a user click on the picture on the right box.
     * If the click is on a detected face, the method create a sub image with the
     * face of the person.
     *
     * @param event mouseEvent used to get the X and respectively the Y of click
     *              position.
     * @throws IOException
     */
    @FXML
    private void getMousePosition(MouseEvent event) throws IOException {
        double x = event.getX() / getScaleXRatio();
        double y = event.getY() / getScaleYRatio();
        if (peopleFrames.get(currentFrameIndex).size() > 0)
            for (IPerson person : peopleFrames.get(currentFrameIndex))
                if (person.getFaceRectangle().contains(x, y)) {
                    // Display face's informations
                    displayPersonInformations(person);
                    // Get the selected face
                    BufferedImage img = ImageIO.read(new File(currentFramePath));
                    Rectangle2D faceCut = person.getFaceRectangle();
                    int minX = (int) faceCut.getMinX() < 0 ? 0 : (int) faceCut.getMinX();
                    int minY = (int) faceCut.getMinY() < 0 ? 0 : (int) faceCut.getMinY();
                    int exceedX = (int) (faceCut.getMinX() + faceCut.getWidth()
                            - img.getWidth());
                    int exceedY = (int) (faceCut.getMinY() + faceCut.getHeight()
                            - img.getHeight());
                    if (exceedX < 0)
                        exceedX = 0;
                    if (exceedY < 0)
                        exceedY = 0;
                    BufferedImage subImg = img.getSubimage(minX, minY,
                            (int) faceCut.getWidth() - exceedX,
                            (int) faceCut.getHeight() - exceedY);
                    currentFacePath = USER_FRAMESTEMP_DIRECTORY + "subImage.jpg";
                    ImageIO.write(subImg, "jpg", new File(currentFacePath));
                }
    }

    /**
     * This method is used to get the right ratio in horizontal axis between the
     * displayed ImageView and the reel image.
     *
     * @return the horizontal ratio between the ImageView and the real image.
     */
    public double getScaleXRatio() {
        return currentFrame.getBoundsInParent().getWidth()
                / currentFrame.getImage().getWidth();
    }

    /**
     * This method is used to get the right ratio in vertical axis between the
     * displayed ImageView and the reel image.
     *
     * @return the vertical ratio between the ImageView and the real image.
     */
    public double getScaleYRatio() {
        return currentFrame.getBoundsInParent().getHeight()
                / currentFrame.getImage().getHeight();
    }

    /**
     * This method show the detail informations of somebody into the labels
     *
     * @param person The person to be displayed
     */
    public void displayPersonInformations(IPerson person) {
        nameLabel.setText("" + person.getName());
        ageLabel.setText("" + person.getAge());
        genderLabel.setText("" + person.getGender());
        smileLabel.setText("" + person.getSmile());
        glassesLabel.setText("" + person.getGlasses());
        informationsLabel.setText("" + person.getInformations());
    }

    /**
     * This method create and show an AddView face with the current selected face.
     */
    public void openAddFaceDialog() {
        try {
            AddPersonController controller = new AddPersonController(model);
            URL url = Thread.currentThread().getContextClassLoader()
                    .getResource("view/AddPersonView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            fxmlLoader.setController(controller);
            Parent dialog = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(dialog));
            stage.setTitle("Add new person");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(
                    new Image(getClass().getResource("/img/default.png").toString()));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method draw a green rectangle on each detected face.
     *
     * @param persons a list of detected people.
     */
    public void drawRectangleAllPersons(List<IPerson> persons) {
        BufferedImage imgDrawed = null;
        try {
            imgDrawed = ImageIO.read(mainImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        nbFacesLabel.setText("" + persons.size());
        for (IPerson person : persons) {
            Graphics2D g2d = imgDrawed.createGraphics();
            g2d.setStroke(new BasicStroke(5));
            g2d.setColor(Color.GREEN);
            g2d.drawRect((int) person.getFaceRectangle().getMinX(),
                    (int) person.getFaceRectangle().getMinY(),
                    (int) person.getFaceRectangle().getWidth(),
                    (int) person.getFaceRectangle().getHeight());
            g2d.dispose();
        }
        // Write new image
        String modifiedJpg = USER_FRAMESTEMP_DIRECTORY + "capturedFrame.jpg";
        try {
            fileDrawed = new File(modifiedJpg);
            ImageIO.write(imgDrawed, "jpg", fileDrawed);
            Image image = new Image(fileDrawed.toURI().toString());
            currentFrame.setImage(image);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method draw a red rectangle on each identified face.
     *
     * @param persons a list of detected people.
     */
    public void drawRectangleKnownPersons(List<IPerson> persons) {
        BufferedImage imgBoxes = null;
        try {
            imgBoxes = ImageIO.read(fileDrawed);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (IPerson person : persons) {
            // ajout dans l hashmap
            assert person.getName() != null;
            ArrayList<String> list = appears.get(person.getName());
            if (list == null) {
                list = new ArrayList<>();
                if (videoMode)
                    list.add("Frame #" + currentFrameIndex);
                else {

                    list.add(filenameToFormattedDate(currentFrameTime.getText()));
                    appears.put(person.getName(), list);
                }
            }
            else
                list.add(filenameToFormattedDate(currentFrameTime.getText()));
            Graphics2D g2d = imgBoxes.createGraphics();
            g2d.setStroke(new BasicStroke(10));
            g2d.setColor(Color.RED);
            g2d.drawRect((int) person.getFaceRectangle().getMinX(),
                    (int) person.getFaceRectangle().getMinY(),
                    (int) person.getFaceRectangle().getWidth(),
                    (int) person.getFaceRectangle().getHeight());
            g2d.dispose();
        }
        // Write new image
        String modifiedJpg2 = USER_FRAMESTEMP_DIRECTORY + "modified2.jpg";
        try {
            File last = new File(modifiedJpg2);
            ImageIO.write(imgBoxes, "jpg", last);
            Image image = new Image(last.toURI().toString());
            currentFrame.setImage(image);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * The action triggered by pushing the start button on the GUI. It create the
     * webcam stream with two threads to grab the frames. The first will display
     * the live on the left box and the second display on image per 3 seconds on
     * the right box.
     */
    protected void startCamera() {
        if (!cameraActive) {
            // start the video capture
            videoMode = false;
            capture.open(cameraId);
            // is the video stream available?
            if (capture.isOpened()) {
                cameraActive = true;
                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = () -> {
                    // grab and process a single frame, convert and show it
                    Image imageToShow = Utils.mat2Image(grabWebcamFrame());
                    updateImageView(webcamStream, imageToShow);
                };
                timerStream = Executors.newSingleThreadScheduledExecutor();
                timerStream.scheduleAtFixedRate(frameGrabber, 0, 33,
                        TimeUnit.MILLISECONDS);

                // grab a frame every 2 s
                Runnable frameGrabber2 = () -> {
                    if (autorefreshCheckBox.isSelected()) {
                        // effectively grab and process a single frame
 
                        Mat frame = grabCaptureFrame();

                        Image imageToShow = Utils.mat2Image(frame);
                        updateImageView(currentFrame, imageToShow);

                    }

                };
                timerCapture = Executors.newSingleThreadScheduledExecutor();
                timerCapture.scheduleAtFixedRate(frameGrabber2, 0, 7, TimeUnit.SECONDS);

                // enable/disable the buttons
                playButton.setDisable(true);
                pauseButton.setDisable(false);
            }
            else
                // log the error
                System.err.println("Impossible to open the camera connection...");
        }
    }

    /**
     * The action triggered by pushing the stop button on the GUI It stop the
     * camera if it running and update the buttons status.
     */
    protected void stopCamera() {
        if (cameraActive) {
            // the camera is not active at this point
            cameraActive = false;
            // enable/disable the buttons
            playButton.setDisable(false);
            pauseButton.setDisable(true);
            // stop the timer
            stopCameraAcquisition();
        }
    }

    /**
     * Get a frame from the opened video stream (if any)
     *
     * @return the {@link Mat} to show
     */
    private Mat grabWebcamFrame() {
        // init everything
        Mat frame = new Mat();
        // check if the capture is open
        if (capture.isOpened())
            try {
                // read the current frame
                capture.read(frame);
            } catch (Exception e) {
                // log the error
                System.err.println("Exception during the image elaboration: " + e);
            }
        return frame;
    }

    /**
     * Get a frame from the opened video stream (if any) and update the time
     * label.
     *
     * @return the {@link Mat} to show
     */
    private Mat grabCaptureFrame() {
        // init everything
        Mat frame = new Mat();
        // check if the capture is open
        if (capture.isOpened())
            try {
                // read the current frame
                capture.read(frame);
                // display current time
                Platform.runLater(() -> {
                    currentFrameTime.setText(timeLabel.getText());
                });
                // if the frame is not empty, process it
                if (!frame.empty())
                    saveFrame(frame);
            } catch (Exception e) {
                // log the error
                System.err.println("Exception during the image elaboration: " + e);
            }
        return frame;
    }

    /**
     * Save a mat in a jpg image. It saves it with the currentTime name.
     *
     * @param frame the Mat to save in a file.
     */
    public void saveFrame(Mat frame) {
        DateFormat currentTime = new SimpleDateFormat(FILENAME_DATE_FORMAT);
        // Write new image
        String grabbedFrame = USER_WORKING_DIRECTORY + "/"
                + currentTime.format(Calendar.getInstance().getTime()) + ".jpg";
        currentFramePath = grabbedFrame;
        imwrite(grabbedFrame, frame);
        try {
            mainImageFile = new File(currentFramePath);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Save a mat in a jpg image. It used in video mode and saves the picture with
     * is frame number.
     *
     * @param frame     the Mat to save in a file.
     * @param frameNumb the number of the frame to save.
     */
    public void saveVideoFrame(Mat frame, int frameNumb) {
        String grabbedFrame = USER_WORKING_DIRECTORY + "/" + videoName + "_Frame"
                + frameNumb + ".jpg";
        currentFramePath = grabbedFrame;
        imwrite(grabbedFrame, frame);
        try {
            mainImageFile = new File(currentFramePath);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }

    /**
     * Stop the acquisition from the camera and release all the resources
     */
    private void stopCameraAcquisition() {
        if (timerStream != null && !timerStream.isShutdown())
            try {
                // stop the timer
                timerStream.shutdown();
                timerStream.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // log any exception
                System.err.println(
                        "Exception in stopping the frame capture, trying to release the camera now... "
                                + e);
            }
        if (timerCapture != null && !timerCapture.isShutdown())
            try {
                // stop the timer
                timerCapture.shutdown();
                timerCapture.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // log any exception
                System.err.println(
                        "Exception in stopping the frame capture, trying to release the camera now... "
                                + e);
            }
        if (capture != null && capture.isOpened())
            // release the camera
            try {
                capture.release();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     *
     * @param view  the {@link ImageView} to update
     * @param image the {@link Image} to show
     */
    public void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    /**
     * Triggered when the user click on the exit icon. It calls the method to
     * clean directories and stop threads.
     *
     * @param event
     */
    @FXML
    public void exitApplication(ActionEvent event) {
        stopThreads();
        stopCameraAcquisition();
        cleanTempFiles();

    }

    /**
     * This method remove all files into the working directory.
     */
    private void cleanTempFiles() {
        File[] files = new File(USER_WORKING_DIRECTORY).listFiles();
        for (int i = 0; i < files.length; i++)
            files[i].delete();
    }

    /**
     * Check the autorefreshBox and send it is status
     *
     * @return true if the refresh box is checked, false otherwise.
     */
    public boolean isBoxSelected() {
        return autorefreshCheckBox.isSelected();
    }

    /**
     * This method is used in video mode, it update the time label with the
     * current frame and update the frame index.
     *
     * @param counter the diplayed frame index.
     */
    public void updateTimeLabelFrameNumber(int counter) {
        currentFrameIndex = counter;
        currentFrameTime.setText("#" + counter);
    }
}