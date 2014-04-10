package eproctor.commons;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import jfx.messagebox.MessageBox;

/**
 * This class is login Controller class. Control the whole login process It
 * consists exclusively of methods that operate on LoginUI or return
 * java.lang.Object. It contains polymorphic algorithm that operate on
 * collections.
 * <p>
 * The methods of this class all throw a <tt>NullPointerException</tt>
 * if the collections or class objects provided to them are null.
 *
 * @author Gong Yue
 * @author Chen Liyang
 */
public class LoginFormController implements Initializable {

    private Stage selfStage;

    @FXML
    TextField username;

    @FXML
    TextField password;

    @FXML
    ChoiceBox choiceType;

    @FXML
    private Button buttonLogin;

    @FXML
    private Font x1;

    @FXML
    private Button buttonExit;

    @FXML
    ProgressBar bar;

    /**
     * This method handle the login event.
     * <p>
     * If login is successful then UI will switch to another.</p>
     * <p>
     * else, message box will be filled with certain information.</p>
     *
     * @param event the event to be handle
     * @throws Exception if overwrite error
     * @see LoginForm.fxml
     */
    @FXML
    private void login(ActionEvent event) throws Exception {
        buttonLogin.setDisable(true);

        SimpleDoubleProperty progress0 = new SimpleDoubleProperty(0);
        SimpleDoubleProperty progress1 = new SimpleDoubleProperty(0);
        SimpleDoubleProperty progress2 = new SimpleDoubleProperty(0);

        Task<Void> progressTask = new Task<Void>() {

            @Override
            protected void succeeded() {
                super.succeeded();
                try {
                    openFrameForm();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            protected void failed() {
                MessageBox.show(selfStage,
                            "The username / password you entered is incorrect.\nPlease try again.",
                            "Please re-enter your username / password",
                            MessageBox.ICON_INFORMATION);
                    buttonLogin.setDisable(false);
            }
            
            @Override
            protected Void call() throws Exception {
                if (DatabaseInterface.isUser(choiceType.getValue().toString(), username.getText(), getMD5(password.getText(), true))) {
                    progress0.set(1);
                    switch (DatabaseInterface.domain) {
                        case "Student":
                            DatabaseInterface.updateLocalRecordDataStudent(progress1);
                            DatabaseInterface.updateLocalCourseDataStudent(progress2);
                            break;
                        case "Proctor":
                            DatabaseInterface.updateLocalRecordDataProctor();
                            break;
//                        case "Coordinator":
//                            DatabaseInterface.updateLocalRecordDataCoordinator();
//                            break;
                    }
                } else {
                    this.failed();
                }

                return null;
            }
        };

        bar.progressProperty().bind(progress0.multiply(0.1).add(progress1.multiply(0.45).add(progress2.multiply(0.45))));
        new Thread(progressTask).start();
    }

    /**
     * This method handle the exit operation.
     * <p>
     * destroy objects which contains user or exam information.</p>
     * <p>
     * close UI and close the software.</p>
     *
     * @param event
     */
    @FXML
    private void exit(ActionEvent event) {
        System.exit(0);
    }

    private double dragInitialX, dragInitialY;

    /**
     * This method handles mouse click/press event
     *
     * @param me short for MouseEvent
     */
    @FXML
    private void mousePressedHandler(MouseEvent me) {
        if (me.getButton() != MouseButton.MIDDLE) {
            dragInitialX = me.getSceneX();
            dragInitialY = me.getSceneY();
        }
    }

    /**
     * This method handles mouse drag event
     *
     * @param me short for MouseEvent
     */
    @FXML
    private void mouseDraggedHandler(MouseEvent me) {
        if (me.getButton() != MouseButton.MIDDLE) {
            selfStage.setX(me.getScreenX() - dragInitialX);
            selfStage.setY(me.getScreenY() - dragInitialY);
        }
    }

    /**
     * This method initialize url.
     *
     * @param url The location used to resolve relative paths for the root
     * object, or null if the location is not known.
     * @param rb The resources used to localize the root object, or null if the
     * root object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        buttonLogin.setDefaultButton(true);
    }

    /**
     * This method set selfStage to stage.
     *
     * @param stage Stage
     */
    public void setStage(Stage stage) {
        selfStage = stage;
    }

    private void openFrameForm() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FrameForm.fxml"));
        BorderPane mainPane = loader.load();
        FrameFormController controller = (FrameFormController) loader.getController();

        int num = 0;
        if (DatabaseInterface.domain.equals("Student"))
            num = DatabaseInterface.courseData.size();
        else
            num = DatabaseInterface.recordDataProctor.size();
        num = num < 5 ? 5 : num;
        Scene frameScene = new Scene(mainPane, 700, 150 + 100 * num);
        Stage frameStage = new Stage();
        frameStage.setScene(frameScene);
        frameStage.setResizable(false);
        frameStage.show();
        controller.setSelfStage(frameStage);
        switch (DatabaseInterface.domain) {
            case "Student":
                frameStage.setTitle("eProctor Student Client");
                controller.setToolTips();
                controller.openStudentForm();
                break;
            case "Proctor":
                frameStage.setTitle("eProctor Proctor Client");
                controller.setToolTips();
                controller.openProctorForm();
                break;
        }
        selfStage.close();
    }

    /**
     * This method implements MD5 generating algorithm.
     *
     * @param input String for user password
     * @param getHex boolean
     * @return MD5 MD5 string respect to input String
     * @throws NoSuchAlgorithmException throws
     */
    public static String getMD5(String input, boolean getHex)
            throws NoSuchAlgorithmException {
        MessageDigest md;
        byte[] bytesOfMessage = input.getBytes(StandardCharsets.UTF_8);
        md = MessageDigest.getInstance("MD5");
        byte[] thedigest = md.digest(bytesOfMessage);
        if (!getHex) {
            return new String(thedigest, StandardCharsets.UTF_8);
        } else {
            return bytesToHex(thedigest);
        }
    }

    /**
     * This is to reformate passing-in bytes
     *
     * @param bytes desired bytes
     * @return hexChars the re-formated bytes as a String
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
