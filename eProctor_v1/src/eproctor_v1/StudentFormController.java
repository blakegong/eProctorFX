/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eproctor_v1;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Yue
 */
public class StudentFormController implements Initializable {
    
    private Stage selfStage;

    @FXML
    private TextArea textAreaInformation;
    
    @FXML
    private TextArea textAreaRecentMessages;
    
    @FXML
    private void openExamForm() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ExamForm.fxml"));
        Parent root = (Parent)loader.load();
        ExamFormController controller = (ExamFormController)loader.getController();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        controller.setStage(stage);
        stage.setScene(scene);
        stage.setTitle("ePoctor Student Client");
        stage.setScene(scene);
        stage.show();
    }
    
    @FXML
    private void openBookForm() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BookForm.fxml"));
        Parent root = (Parent)loader.load();
        BookFormController controller = (BookFormController)loader.getController();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        controller.setStage(stage);
        stage.setScene(scene);
        stage.setTitle("ePoctor Student Client");
        stage.setScene(scene);
        stage.show();
    }
    
    @FXML
    private void openReviewForm() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ReviewForm.fxml"));
        Parent root = (Parent)loader.load();
        ReviewFormController controller = (ReviewFormController)loader.getController();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        controller.setStage(stage);
        stage.setScene(scene);
        stage.setTitle("ePoctor Student Client");
        stage.setScene(scene);
        stage.show();
    }
    
    @FXML
    private void openSettingForm() throws Exception {
        
    }
    
    @FXML
    private void logout() throws Exception {
        selfStage.close();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginForm.fxml"));
        Parent root = (Parent)loader.load();
        LoginFormController controller = (LoginFormController)loader.getController();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        controller.setStage(stage);
        stage.setScene(scene);
        stage.setTitle("ePoctor Student Client");
        stage.setScene(scene);
        stage.show();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        new Thread(() -> {
            Platform.runLater(() -> {
                String temp = ServerInterface.getTextAreaInformation();
                textAreaInformation.setText(temp);
            });
        }).start();
        
        new Thread(() -> {
            Platform.runLater(() -> {
                String temp = ServerInterface.getTextAreaRecentMessages();
                textAreaRecentMessages.setText(temp);
            });
        }).start();
    }
    
    public void setStage(Stage stage) {
        selfStage = stage;
    }
}
