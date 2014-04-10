package eproctor.student;

//<<<<<<< HEAD
import eproctor.commons.Timer;
//=======
//>>>>>>> 0afa6450b8606b1fe257fbb847646e730d0bfe8c
import eproctor.commons.DatabaseInterface;
import com.googlecode.javacv.FrameGrabber;
import static eproctor.commons.Timer.intSecToReadableSecond;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

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
    private Timeline timer;
    private int count;

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
    private Label statusLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Button exitButton;

    @FXML
    private void sendMsg() {
        msgSendButton.setDisable(true);
        // random a proctor
        String receiver_code = "NTUP3891093T";
        DatabaseInterface.serviceSendMsg = new DatabaseInterface.ServiceSendMsg(DatabaseInterface.userCode, receiver_code,courseRow.getCode(), sessionRow.getCode(), msgToSend.getText(), new Date(), 0);
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
            ex.printStackTrace();
        }
        selfStage.close();
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

        DatabaseInterface.serviceFetchMsg = new DatabaseInterface.ServiceFetchMsg(DatabaseInterface.userCode, courseRow.getCode(), sessionRow.getCode());
        DatabaseInterface.serviceFetchMsg.setOnFailed(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("serviceFetchMsg failed unexpectedly. Restarting...");
                startServiceFetchMsg(courseRow, sessionRow);
            }
        });
        DatabaseInterface.serviceFetchMsg.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                if (DatabaseInterface.serviceFetchMsg.getValue().compareTo("ending") == 0) { // if he is kicked out.
                    System.out.println("Your are disqualified from current exam session. Your exam session will end in 5 seconds.");
                    VideoServerInterface.serviceSendImage.cancel();
                    try {
                        VideoServerInterface.serviceSendImage.getGrabber().stop();
                    } catch (FrameGrabber.Exception ex) {
                        ex.printStackTrace();
                    }
                    exitButton.setDisable(true);
                    msgSendButton.setDisable(true);
                    browser.setDisable(true);
                    timeLabel.setWrapText(true);
                    timeLabel.setText("Your are disqualified from current exam session.Your exam session will end in 5 seconds.");

                    // = = = = = = =
                    // close previous timer
                    timer.stop();
                    // start a countDOWN timer
                    count = 5; // give 5 seconds for a last look
                    timer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            count--;
                            timeLabel.setWrapText(true);
                            timeLabel.setText("Your are disqualified from current exam session.Your exam session will end in " + count + " seconds.");
                        }
                    }));
                    timer.setCycleCount(count);
                    timer.setOnFinished(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent arg0) {
                            selfStage.close(); // 5 seconds passed. close exam window
                        }
                    });
                    timer.play();
                    // = = = = = = =
                }
            }
        });
        msgReceived.setWrapText(true);
        msgReceived.textProperty().bind(DatabaseInterface.serviceFetchMsg.messageProperty());
        statusLabel.styleProperty().bind(DatabaseInterface.serviceFetchMsg.titleProperty());

        DatabaseInterface.serviceFetchMsg.start();
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
        VideoServerInterface.serviceSendImage = new VideoServerInterface.ServiceSendImage(user_code, ip, port, course_code, session_code);
        videoImageView.imageProperty().bind(VideoServerInterface.serviceSendImage.valueProperty());
        VideoServerInterface.serviceSendImage.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("startServiceSendImage succeeded..");
            }
        });
        VideoServerInterface.serviceSendImage.setOnCancelled(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("startServiceSendImage cancelled.");
            }
        });
        VideoServerInterface.serviceSendImage.setOnFailed(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("startServiceSendImage failed.");
            }
        });
        VideoServerInterface.serviceSendImage.start();
    }

    public void startTimer(int examDuration) {

        // = = = = = = =
        // start a countDOWN timer
        count = examDuration;
        timer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                count--;
                timeLabel.setWrapText(true);
                timeLabel.setText("Time left: " + Timer.intSecToReadableSecond(count, 4));
            }
        }));
        timer.setCycleCount(count);
        timer.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                try {
                    VideoServerInterface.serviceSendImage.getGrabber().stop();
                } catch (FrameGrabber.Exception ex) {
                    ex.printStackTrace();
                }
                exitButton.setDisable(true);
                msgSendButton.setDisable(true);
                browser.setDisable(true);
                timeLabel.setText("Exam ended. Your this window will close in 5 seconds.");

                // = = = = = = =
                // start a countDOWN timer
                count = 5; // give 5 seconds for a last look
                timer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        count--;
                        timeLabel.setText("Exam ended. Your this window will close in " + count + " seconds.");
                    }
                }));
                timer.setCycleCount(count);
                timer.setOnFinished(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent arg0) {
                        selfStage.close(); // 5 seconds passed. close exam window
                    }
                });
                timer.play();
                // = = = = = = =
            }
        });
        timer.play();
        // = = = = = = =
    }
}
