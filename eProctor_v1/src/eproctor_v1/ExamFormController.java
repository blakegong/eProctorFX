/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eproctor_v1;

import com.googlecode.javacv.FrameGrabber;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Yue
 */
public class ExamFormController implements Initializable {
    
    private DatabaseInterface.SessionRow sessionRow;
    private DatabaseInterface.CourseRow courseRow;
    
    private Stage selfStage;
    private Scene selfScene;
    
    @FXML
    private WebView browser;
    
    @FXML
    private TextArea msgReceived;
    
    @FXML
    private TextField msgToSend;
    
    @FXML
    private ProgressIndicator msgProgressIndicator;
    
    @FXML
    private Button msgSendButton;
    
    @FXML
    protected ImageView videoImageView;
    
    @FXML
    private Button exitButton;
    
    @FXML
    private void sendMsg() {
        msgSendButton.setDisable(true);
        DatabaseInterface.serviceSendMsg = new DatabaseInterface.ServiceSendMsg();
        DatabaseInterface.serviceSendMsg.setMe(DatabaseInterface.userCode);
        DatabaseInterface.serviceSendMsg.setCourse_code(courseRow.getCode());
        DatabaseInterface.serviceSendMsg.setSession_code(sessionRow.getCode());
        DatabaseInterface.serviceSendMsg.setText(msgToSend.getText());
        DatabaseInterface.serviceSendMsg.setTime(new Date());
        DatabaseInterface.serviceSendMsg.setType("MSG");
        DatabaseInterface.serviceSendMsg.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("message sent.");
                msgToSend.setText("");
                msgProgressIndicator.setProgress(100);
                msgSendButton.setDisable(false);
            }
        });
        DatabaseInterface.serviceSendMsg.setOnFailed(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("Connection lost!");
                msgProgressIndicator.setProgress(0);
                msgSendButton.setDisable(false);
            }
        });
        msgProgressIndicator.setProgress(-1);
        DatabaseInterface.serviceSendMsg.start();
    }

    @FXML
    private void exitSession() {
        try {
            VideoServerInterface.serviceSendImage.getGrabber().stop();
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(ExamFormController.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
//        exitButton.getScene().getWindow();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
//        browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.load("https://www.google.com.sg/#q=nanshen+memeda");
    }    
    
    public void setStage(Stage stage) {
        selfStage = stage;
    }

    public void setSessionRow(DatabaseInterface.SessionRow sessionRow) {
        this.sessionRow = sessionRow;
    }

    public void setCourseRow(DatabaseInterface.CourseRow courseRow) {
        this.courseRow = courseRow;
    }
    
    public void startServiceFetchMsg(DatabaseInterface.CourseRow courseRow, DatabaseInterface.SessionRow sessionRow) {
        this.courseRow = courseRow;
        this.sessionRow = sessionRow;
        
        DatabaseInterface.serviceFetchMsg = new DatabaseInterface.ServiceFetchMsg();
        DatabaseInterface.serviceFetchMsg.setMe(DatabaseInterface.userCode);
        DatabaseInterface.serviceFetchMsg.setCourse_code(courseRow.getCode());
        DatabaseInterface.serviceFetchMsg.setSession_code(sessionRow.getCode());
        DatabaseInterface.serviceFetchMsg.start();
        msgReceived.textProperty().bind(DatabaseInterface.serviceFetchMsg.messageProperty());
    }
    
    public void startServiceSendImage(String user_code, String course_code, String session_code, String ip, int port, ImageView videoImageView) {
        VideoServerInterface.serviceSendImage = new VideoServerInterface.ServiceSendImage();
        try {
            VideoServerInterface.serviceSendImage.setGrabber(FrameGrabber.createDefault(0));
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(ExamFormController.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        VideoServerInterface.serviceSendImage.setMe(user_code);
        VideoServerInterface.serviceSendImage.setCourse_code(course_code);
        VideoServerInterface.serviceSendImage.setSession_code(session_code);
        VideoServerInterface.serviceSendImage.setIp(ip);
        VideoServerInterface.serviceSendImage.setPort(port);
        videoImageView.imageProperty().bind(VideoServerInterface.serviceSendImage.valueProperty());
        VideoServerInterface.serviceSendImage.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("succeeded..");
            }
        });
        VideoServerInterface.serviceSendImage.setOnCancelled(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("cancelled.");
            }
        });
        VideoServerInterface.serviceSendImage.start();
    }
}
