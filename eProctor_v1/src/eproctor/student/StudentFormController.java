/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eproctor.student;

import static eproctor.student.Timer.intSecToReadableSecond;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import jfx.messagebox.MessageBox;

/**
 * FXML Controller class
 *
 * @author Gong Yue
 * @author Chen Liyang
 */
public class StudentFormController implements Initializable {

    private Stage selfStage;
    private ObservableList<Node> infoData;

    @FXML
    VBox vbox;

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
        infoData = FXCollections.observableArrayList();
        Bindings.bindContentBidirectional(infoData, vbox.getChildren());
        DatabaseInterface.getInfoData(this, infoData);
    }

    /**
     * This class is a basic information container used to provide combined
     * information of a row in table.
     */
    public class InfoRow extends HBox {

        private Label lblCourseCode;
        private Label lblCourseName;
        private Label lblInfo;
        private ChoiceBox choiceBox;
        private Button button;
        private ProgressIndicator indicator;
        private DatabaseInterface.CourseRow courseRow;
        private DatabaseInterface.RecordRow recordRow;
        private Date start;
        private Date end;
        private int state;

        private int count;
        private Timeline timer;

        /**
         * Basic constructor of InfoRow, returning an object of InfoRow
         *
         * @param courseRow
         * @param recordRow
         *
         */
        public InfoRow(DatabaseInterface.CourseRow courseRow, DatabaseInterface.RecordRow recordRow) {
            setId("hbox");
            lblCourseCode = new Label(courseRow.getCode());
            lblCourseCode.setMinWidth(200);
            lblCourseCode.setId("courseCode");
            lblCourseName = new Label(courseRow.getName());
            lblCourseName.setMinWidth(300);
            lblCourseName.setId("courseName");
            lblInfo = new Label();
            lblInfo.setMinWidth(300);
            lblInfo.setId("info");
            choiceBox = new ChoiceBox();
            choiceBox.setMinWidth(200);
            choiceBox.setId("choiceBox");
            button = new Button();
            button.setMinWidth(150);
            button.setId("button");
            indicator = new ProgressIndicator();
            indicator.setVisible(false);
//            indicator.setMinSize(30, 30);
            indicator.setPrefSize(25, 25);
            indicator.setProgress(80);
            indicator.setId("indicator");

            this.courseRow = courseRow;
            this.recordRow = recordRow;
            setState();
            Service<Void> timeService = new Service<Void>() {

                private Date start;
                private Date end;

                @Override
                protected Task<Void> createTask() {
                    return null;
                }

            };
        }

        /**
         * set current service stage. The state may be book-not-ready state;
         * book-ready state; review state; testing state, which are respecting
         * to different stage of UI.
         */
        private void setState() {
            if (recordRow == null) {
                state = 0;
                setStateNotBooked();
                return;
            }
            start = recordRow.getSession().getStart();
            end = recordRow.getSession().getEnd();
            Date current = new Date();
            if (start.after(current)) {
                //the entrance opens 30 minutes before exam
                if (start.getTime() - current.getTime() > 1000 * 30 * 60) {
                    state = 1;
                    setStateBookedNotReady();
                } else {
                    state = 2;
                    setStateBookedReady();
                }
            } else if (end.before(current)) {
                state = 3;
                setStateReview();
            } else {
                state = 4;
                setStateTesting();
            }
        }

        private void setStateNotBooked() {
            DatabaseInterface.getListSessions(choiceBox.getItems(), courseRow);
            choiceBox.getSelectionModel().selectFirst();
            VBox tempVbox = new VBox();
            tempVbox.getChildren().addAll(lblCourseName, choiceBox);
            getChildren().remove(0, getChildren().size());
            getChildren().addAll(lblCourseCode, tempVbox, button, indicator);
            lblInfo.setText("This means something wrong");
            button.setText("Book");
            button.setOnAction((ActionEvent e) -> {
                if (choiceBox.getSelectionModel().getSelectedIndex() < 0) {
                    return;
                }
                Task<Void> bookTask = new Task<Void>() {
                    @Override
                    protected void succeeded() {
                        super.succeeded();
                        indicator.setVisible(false);
                        indicator.progressProperty().unbind();
                        button.setDisable(false);
                        setState();
                    }

                    @Override
                    protected void failed() {
                        super.failed();
                        MessageBox.show(selfStage,
                                "Your operation was not successful, please try again!",
                                "Warning",
                                MessageBox.ICON_INFORMATION);
                        button.setDisable(false);
                    }

                    @Override
                    protected Void call() throws Exception {
                        int sessionIndex = choiceBox.getSelectionModel().getSelectedIndex();
                        recordRow = DatabaseInterface.addBooking(courseRow, courseRow.getSessions().get(sessionIndex));
                        System.out.println(recordRow);
                        return null;
                    }
                };
                indicator.progressProperty().bind(bookTask.progressProperty());
                indicator.setVisible(true);
                button.setDisable(true);
                new Thread(bookTask).start();
            });
        }

        private void setStateBookedNotReady() {
            VBox tempVbox = new VBox();
            tempVbox.getChildren().addAll(lblCourseName, lblInfo);
            getChildren().remove(0, getChildren().size());
            getChildren().addAll(lblCourseCode, tempVbox, button, indicator);
            lblInfo.setText("starts at: " + start.toString());
            //set timer to change to bookedReady state
            button.setText("Change Session");
            button.setOnAction((ActionEvent e) -> {
                Task<Void> deleteTask = new Task<Void>() {
                    @Override
                    protected void succeeded() {
                        super.succeeded();
                        recordRow = null;
                        indicator.setVisible(false);
                        indicator.progressProperty().unbind();
                        button.setDisable(false);
                        setState();
                    }

                    @Override
                    protected void failed() {
                        super.failed();
                        MessageBox.show(selfStage,
                                "Your operation was not successful, please try again!",
                                "Warning",
                                MessageBox.ICON_INFORMATION);
                        button.setDisable(false);
                    }

                    @Override
                    protected Void call() throws Exception {
                        DatabaseInterface.deleteBooking(recordRow.getId());
                        return null;
                    }
                };
                indicator.progressProperty().bind(deleteTask.progressProperty());
                indicator.setVisible(true);
                button.setDisable(true);
                new Thread(deleteTask).start();
            });

            // = = = = = = =
            // start a countDOWN timer
//            count = (int) ((start.getTime() - new Date().getTime()) / 1000);
            count = 30 * 60 + 15; // for testing
            timer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (count < 30 * 60) {
                        System.out.println("Exam entrance opened.");
                        timer.stop();
                    }
                    count--;
                    System.out.println("count: " + count);
                    int level = 3;
                    if (count > 60 * 60) {
                        level = 2;
                    }
                    lblInfo.setText("time to exam: " + intSecToReadableSecond(count, level));
                }
            }));
            timer.setCycleCount(count);
            timer.setOnFinished(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent arg0) {
                    setStateBookedReady();
                }
            });
            timer.play();
            // = = = = = = =
        }

        private void setStateBookedReady() {
            VBox tempVbox = new VBox();
            tempVbox.getChildren().addAll(lblCourseName, lblInfo);
            this.getChildren().remove(0, this.getChildren().size());
            this.getChildren().addAll(lblCourseCode, tempVbox, button, indicator);
            //set timer to count down
            lblInfo.setText("15:08:32 to the exam.");
            button.setText("Get Ready");
            button.setDisable(true);
            button.setOnAction((ActionEvent e) -> {
                //open exam
            });

            
            // = = = = = = =
            // start a countDOWN timer
//            count = (int) ((start.getTime() - new Date().getTime()) / 1000);
            count = 30 * 60 + 15; // for testing
            timer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    count--;
                    System.out.println("count: " + count);
                    lblInfo.setText("time to exam: " + intSecToReadableSecond(count, 4));
                }
            }));
            timer.setCycleCount(count);
            timer.setOnFinished(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent arg0) {
                    setStateBookedReady();
                }
            });
            timer.play();
            // = = = = = = =
        }

        private void setStateTesting() {
            VBox tempVbox = new VBox();
            tempVbox.getChildren().addAll(lblCourseName, lblInfo);
            this.getChildren().remove(0, this.getChildren().size());
            this.getChildren().addAll(lblCourseCode, tempVbox, button, indicator);
            lblInfo.setText("00:43:54 until the exam ends");
            button.setText("Exam");
            button.setOnAction((ActionEvent e) -> {
                try {
                    openExamForm(courseRow, recordRow.getSession());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            // = = = = = = =
            // start a countUP timer
//            count = (int) ((new Date().getTime() - start.getTime() ) / 1000);
            count = 60 * 14 + 40; // for testing
            timer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    count++;
                    System.out.println("count: " + count);
                    lblInfo.setText("exam has started for " + intSecToReadableSecond(count, 4));

                    if (count >= 60 * 15) {
                        lblInfo.setText("Exam extrance has closed. (15 mins passed)");
                        System.out.println("timer should stop.");
                        timer.stop();
                    }
                }
            }));
            timer.setCycleCount(Timeline.INDEFINITE);
            timer.setOnFinished(new EventHandler<ActionEvent>() {

                @Override
                public void handle(ActionEvent arg0) {
                    System.out.println("timer stopped. Entrance closed.");
//                    button.setDisable(true);
                }
            });
            timer.play();
            // = = = = = = =
        }

        private void setStateReview() {
            VBox tempVbox = new VBox();
            tempVbox.getChildren().addAll(lblCourseName, lblInfo);
            this.getChildren().remove(0, this.getChildren().size());
            this.getChildren().addAll(lblCourseCode, tempVbox, button, indicator);
            if (recordRow.getGrade().equals("")) {
                lblInfo.setText("result not released yet");
            } else {
                lblInfo.setText("result is " + recordRow.getGrade());
            }
            button.setText("Review");
            button.setOnAction((ActionEvent e) -> {
                try {
//                    openReviewPane(recordRow, courseRow);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

//    private void openReviewPane(DatabaseInterface.RecordRow recordRow, DatabaseInterface.CourseRow courseRow) throws Exception {
//        System.out.println("inside");
//        AnchorPane pane = new AnchorPane();
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("ReviewForm.fxml"));
//        ReviewFormController controller = (ReviewFormController) loader.getController();
//        controller.setRecordRow(recordRow);
//        controller.setCourseRow(courseRow);
//        controller.updateDetails();
//        try {
//            pane = loader.load();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        rightPane.getChildren().setAll(pane);
//    }
    
    private void openExamForm(DatabaseInterface.CourseRow courseRow, DatabaseInterface.SessionRow sessionRow) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ExamForm.fxml"));
        Scene examScene = new Scene(loader.load());
        ExamFormController controller = (ExamFormController) loader.getController();

        Stage stage = new Stage();
        stage.setTitle("Examing");
        stage.setScene(examScene);
        stage.show();
        controller.setStage(stage);

        controller.startServiceFetchMsg(courseRow, sessionRow);
        controller.startServiceSendImage(DatabaseInterface.userCode, courseRow.getCode(), sessionRow.getCode(), "localhost", 6002, controller.videoImageView);
    }

    /**
     *
     * @param stage
     */
    public void setStage(Stage stage) {
        selfStage = stage;
    }
}
