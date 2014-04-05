package eproctor_v1;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.cpp.opencv_core;
import static com.googlecode.javacv.cpp.opencv_core.cvFlip;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.imageio.ImageIO;
import jfx.messagebox.MessageBox;

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

    @FXML
    private void login(ActionEvent event) throws Exception {
        if (DatabaseInterface.isUser(choiceType.getValue().toString(), username.getText(), getMD5(password.getText(), true))) {
            buttonLogin.setDisable(true);
            Task<Void> progressTask = new Task<Void>() {
                @Override
                protected void succeeded() {
                    super.succeeded();
                    try {
                        openStudentForm();
                    } catch (Exception ex) {
                        Logger.getLogger(LoginFormController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                @Override
                protected Void call() throws Exception {
                    updateProgress(10, 100);
                    DatabaseInterface.updateLocalRecordData();
                    updateProgress(50, 100);
                    DatabaseInterface.updateLocalCourseData();
                    updateProgress(100, 100);
                    return null;
                }
            };
            bar.progressProperty().bind(progressTask.progressProperty());
            new Thread(progressTask).start();
        } else {
            MessageBox.show(selfStage,
                    "The username / password you entered is incorrect.\nPlease try again.",
                    "Please re-enter your username / password",
                    MessageBox.ICON_INFORMATION);
            buttonLogin.setDisable(false);
        }
    }

    @FXML
    private void exit(ActionEvent event) {
        System.exit(0);
    }

    private double dragInitialX, dragInitialY;

    @FXML
    private void mousePressedHandler(MouseEvent me) {
        if (me.getButton() != MouseButton.MIDDLE) {
            dragInitialX = me.getSceneX();
            dragInitialY = me.getSceneY();
        }
    }

    @FXML
    private void mouseDraggedHandler(MouseEvent me) {
        if (me.getButton() != MouseButton.MIDDLE) {
            selfStage.setX(me.getScreenX() - dragInitialX);
            selfStage.setY(me.getScreenY() - dragInitialY);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void setStage(Stage stage) {
        selfStage = stage;
    }

    private void openStudentForm() throws Exception {
        selfStage.close();
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("StudentForm.fxml"));
        Parent root = (Parent) loader.load();
        StudentFormController controller = (StudentFormController) loader.getController();
        controller.setStage(stage);
        Scene scene = new Scene(root);
        stage.setTitle("eProctor Student Client");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

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
