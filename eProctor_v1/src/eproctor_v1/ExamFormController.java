/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eproctor_v1;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
    private void sendMsg() {
        msgSendButton.setDisable(true);
        DatabaseInterface.serveiceSendMsg = new DatabaseInterface.ServiceSendMsg();
        DatabaseInterface.serveiceSendMsg.setMe(DatabaseInterface.userCode);
        DatabaseInterface.serveiceSendMsg.setCourse_code(courseRow.getCode());
        DatabaseInterface.serveiceSendMsg.setSession_code(sessionRow.getCode());
        DatabaseInterface.serveiceSendMsg.setText(msgToSend.getText());
        DatabaseInterface.serveiceSendMsg.setTime(new Date());
        DatabaseInterface.serveiceSendMsg.setType("MSG");
        DatabaseInterface.serveiceSendMsg.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("message sent.");
                msgToSend.setText("");
                msgProgressIndicator.setProgress(100);
                msgSendButton.setDisable(false);
            }
        });
        DatabaseInterface.serveiceSendMsg.setOnFailed(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("Connection lost!");
                msgProgressIndicator.setProgress(0);
                msgSendButton.setDisable(false);
            }
        });
        msgProgressIndicator.setProgress(-1);
        DatabaseInterface.serveiceSendMsg.start();
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
        
        DatabaseInterface.serveiceFetchMsg = new DatabaseInterface.ServiceFetchMsg();
        DatabaseInterface.serveiceFetchMsg.setMe(DatabaseInterface.userCode);
        DatabaseInterface.serveiceFetchMsg.setCourse_code(courseRow.getCode());
        DatabaseInterface.serveiceFetchMsg.setSession_code(sessionRow.getCode());
        DatabaseInterface.serveiceFetchMsg.start();
        msgReceived.textProperty().bind(DatabaseInterface.serveiceFetchMsg.messageProperty());
    }
}
