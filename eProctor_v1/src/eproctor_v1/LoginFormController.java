/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eproctor_v1;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import jfx.messagebox.MessageBox;

/**
 * FXML Controller class
 *
 * @author Yue
 */
public class LoginFormController implements Initializable {

    private Stage selfStage;

    @FXML
    TextField username;

    @FXML
    TextField password;

    @FXML
    ChoiceBox choiceType;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void login(ActionEvent event) throws Exception {
        if (ServerInterface.isUser(choiceType.getValue().toString(), username.getText(), getMD5(password.getText(), true))) {
            openStudentForm();
            ServerInterface.updateLocalRecordData();
            ServerInterface.updateLocalCourseData();
        } else {
            MessageBox.show(selfStage,
                "Login Error",
                "Login Fails",
                MessageBox.ICON_INFORMATION);
        }
    }

    @FXML
    private void exit(ActionEvent event) {
        // selfStage.close();
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

    public void setStage(Stage stage) {
        selfStage = stage;
    }

    private void openStudentForm() throws IOException {
        selfStage.close();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("StudentForm.fxml"));
        Parent root = (Parent)loader.load();
        StudentFormController controller = (StudentFormController)loader.getController();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        controller.setStage(stage);
        stage.setScene(scene);
        stage.setTitle("ePoctor Student Client");
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
