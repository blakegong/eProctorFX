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
 * This class is an FXML Controller class. It consists exclusively of methods
 * that operate on ExamFromUI or return java.lang.Object. It contains
 * communication with ServerInterface.
 * <p>
 * This class updates ExamForm.fxml</p>
 *
 * @author Gong Yue
 * @author Chen Liyang
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
//            Logger.getLogger(ExamFormController.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
//        exitButton.getScene().getWindow();
    }
    
    /**
     * This method initialize ExamFormUI by setting up exam paper.
     *
     * @param url The location used to resolve relative paths for the root
     * object, or null if the location is not known.
     * @param rb The resources used to localize the root object, or null if the
     * root object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
//        browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.load("https://www.google.com.sg/#q=nanshen+memeda");
    }

    /**
     *
     * @param stage
     */
    public void setStage(Stage stage) {
        selfStage = stage;
    }

    /**
     *
     * @param sessionRow
     */
    public void setSessionRow(DatabaseInterface.SessionRow sessionRow) {
        this.sessionRow = sessionRow;
    }

    /**
     *
     * @param courseRow
     */
    public void setCourseRow(DatabaseInterface.CourseRow courseRow) {
        this.courseRow = courseRow;
    }

    /**
     * This method starts message fetching service.
     *
     * @param courseRow row of courses
     * @param sessionRow row of sessions
     */
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

    /**
     * This method starts the image sending service.
     *
     * @param user_code
     * @param course_code needed course code in order to store in database
     * @param session_code needed exam session code in order to store in
     * database
     * @param ip ip address
     * @param port connection port
     * @param videoImageView ImageView object
     */
    public void startServiceSendImage(String user_code, String course_code, String session_code, String ip, int port, ImageView videoImageView) {
        VideoServerInterface.serviceSendImage = new VideoServerInterface.ServiceSendImage();
        try {
            VideoServerInterface.serviceSendImage.setGrabber(FrameGrabber.createDefault(0));
        } catch (FrameGrabber.Exception ex) {
//            Logger.getLogger(ExamFormController.class.getName()).log(Level.SEVERE, null, ex);
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
