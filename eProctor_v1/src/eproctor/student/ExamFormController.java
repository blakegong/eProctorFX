package eproctor.student;

import com.googlecode.javacv.FrameGrabber;
import eproctor.commons.DatabaseInterface;
import eproctor.commons.MessagePull;
import eproctor.commons.MessageSend;
import eproctor.commons.Timer;
import static eproctor.commons.Timer.intSecToReadableSecond;
import eproctor.commons.VideoServerInterface;
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
import jfx.messagebox.MessageBox;

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
    private String proctorRandomed;

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

    /**
     *
     */
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

        ////////////////////////////////////////////////////////////
        // random a proctor
        if (proctorRandomed == null) {
            proctorRandomed = DatabaseInterface.randomProctor(courseRow.getCode(), sessionRow.getCode());
        }
        ////////////////////////////////////////////////////////////

        MessageSend serviceSendMsg = new MessageSend(DatabaseInterface.username, proctorRandomed, courseRow.getCode(), sessionRow.getCode(), msgToSend.getText(), new Date(), 0);
        serviceSendMsg.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("message sent.");
                msgToSend.setText("");
                msgProgressIndicator.setProgress(100);
                msgSendButton.setDisable(false);
            }
        });
        serviceSendMsg.setOnFailed(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("Connection lost!");
                msgProgressIndicator.setProgress(0);
                msgSendButton.setDisable(false);
            }
        });
        msgProgressIndicator.setProgress(-1);
        serviceSendMsg.start();
    }

    @FXML
    private void exitSession() {
        try {
            VideoServerInterface.serviceSendImage.getGrabber().stop();
        } catch (FrameGrabber.Exception ex) {
            ex.printStackTrace();
        }
        proctorRandomed = null;
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
        WebEngine webEngine = browser.getEngine();
        webEngine.load("https://docs.google.com/forms/d/1rY8Ic7LRMJFYSHLSN7luMuIv78iC6mywRgstBwd9lgs/viewform");
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

        MessagePull serviceFetchMsg = new MessagePull(DatabaseInterface.username, courseRow.getCode(), sessionRow.getCode());
        serviceFetchMsg.setOnFailed(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("serviceFetchMsg failed unexpectedly. Restarting...");
                startServiceFetchMsg(courseRow, sessionRow);
            }
        });
        serviceFetchMsg.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                if (serviceFetchMsg.getValue().compareTo("ending") == 0) { // if he is kicked out.

                    System.out.println("Your are disqualified from current exam session.");
                    VideoServerInterface.serviceSendImage.cancel();
                    try {
                        VideoServerInterface.serviceSendImage.getGrabber().stop();
                    } catch (FrameGrabber.Exception ex) {
                        ex.printStackTrace();
                    }
                    exitButton.setDisable(true);
                    msgSendButton.setDisable(true);
                    timeLabel.setWrapText(true);
                    timeLabel.setText("Your are disqualified from current exam session.");
                    browser.setDisable(true);
                    browser.setVisible(false);

                    MessageBox.show(selfStage,
                            "Your are disqualified from current exam session. This exam window will close.",
                            "Sorry.",
                            MessageBox.ICON_INFORMATION);

//                    // = = = = = = =
//                    // close previous timer
//                    timer.stop();
//                    // start a countDOWN timer
//                    count = 5; // give 5 seconds for a last look
//                    timer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
//                        @Override
//                        public void handle(ActionEvent event) {
//                            count--;
//                            timeLabel.setWrapText(true);
//                            timeLabel.setText("Your are disqualified from current exam session.Your exam session will end in " + count + " seconds.");
//                        }
//                    }));
//                    timer.setCycleCount(count);
//                    timer.setOnFinished(new EventHandler<ActionEvent>() {
//
//                        @Override
//                        public void handle(ActionEvent arg0) {
                    selfStage.close(); // 5 seconds passed. close exam window
//                        }
//                    });
//                    timer.play();
                    // = = = = = = =
                }
            }
        });
        msgReceived.setWrapText(true);
        msgReceived.textProperty().bind(serviceFetchMsg.messageProperty());
        statusLabel.styleProperty().bind(serviceFetchMsg.titleProperty());
        serviceFetchMsg.start();
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
    public void startServiceSendImage(String username, String course_code, String session_code, String ip, int port, ImageView videoImageView) {
        VideoServerInterface.serviceSendImage = new VideoServerInterface.ServiceSendImage(username, ip, port, course_code, session_code);
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

    /**
     *
     * @param start
     * @param end
     */
    public void startTimer(Date start, Date end) {
//        exitButton.setDisable(true);
        browser.setVisible(false);
        if (new Date().before(start)) {
            count = (int) ((new Date().getTime() - start.getTime()) / 1000);
            timer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    count--;
                    int level = 3;
                    timeLabel.setText("time to exam:\n\t" + intSecToReadableSecond(count, level));
                }
            }));
            timer.setCycleCount(count);
            timer.setOnFinished(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent arg0) {
                    startTimer(start, end);
                }
            });
            timer.play();
        } else {
            browser.setVisible(true);
            count = (int) ((end.getTime() - new Date().getTime()) / 1000);
            timer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    count--;
                    int level = 3;
                    timeLabel.setText("time left:\n\t" + intSecToReadableSecond(count, level));
                }
            }));
            timer.setCycleCount(count);
            timer.setOnFinished(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent arg0) {

                    timeLabel.setText("Exam ended. Thank you Doctor. Leedham *^_^*");
                    MessageBox.show(selfStage,
                            "\tExam ended. Good Job.\t",
                            "Exam ended. Thank you Dr.Leedham *^_^*",
                            MessageBox.ICON_INFORMATION);

//                    exitButton.setDisable(false);
                }
            });
            timer.play();
        }
    }

}
