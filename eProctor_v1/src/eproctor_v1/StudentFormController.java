/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eproctor_v1;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
            indicator.setMinSize(30, 30);
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
                //the entrance opens one hour before exam
                if (start.getTime() - current.getTime() > 1000 * 60 * 60) {
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
//                    Logger.getLogger(StudentFormController.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }
            });
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
            });
        }
    }

    private void openExamForm(DatabaseInterface.CourseRow courseRow, DatabaseInterface.SessionRow sessionRow) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ExamForm.fxml"));
        Parent root = (Parent) loader.load();
        ExamFormController controller = (ExamFormController) loader.getController();
        Scene scene = new Scene(root,1024, 768);
        Stage stage = new Stage();
        controller.setStage(stage);
        stage.setScene(scene);
        stage.setTitle("ePoctor Student Client");
        stage.setScene(scene);
        stage.show();

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
