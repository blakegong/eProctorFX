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
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfx.messagebox.MessageBox;

/**
 * FXML Controller class
 *
 * @author Yue
 */
public class StudentFormController implements Initializable {

    private Stage selfStage;
    private ArrayList<InfoRow> infoData;

    @FXML
    VBox vbox;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        infoData = new ArrayList();
        ServerInterface.getInfoData(this, infoData);
        for (InfoRow infoRow : infoData) {
            vbox.getChildren().add(infoRow);
        }
    }

    public void refreshUI() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("StudentForm.fxml"));
        Parent root = (Parent)loader.load();
        StudentFormController controller = (StudentFormController)loader.getController();
        Scene scene = new Scene(root);
        controller.setStage(selfStage);
        selfStage.setScene(scene);
        selfStage.show();
    }

    public class InfoRow extends HBox {

        private Label lblCourseCode;
        private Label lblCourseName;
        private Label lblInfo;
        private Button button;
        private ProgressIndicator indicator;
        private ServerInterface.CourseRow courseRow;
        private ServerInterface.RecordRow recordRow;
        private Date start;
        private Date end;

        public InfoRow(ServerInterface.CourseRow courseRow, ServerInterface.RecordRow recordRow) {
            this.setId("hbox");
            lblCourseCode = new Label(courseRow.getCode());
            lblCourseCode.setMinWidth(200);
            lblCourseCode.setId("courseCode");
            lblCourseName = new Label(courseRow.getName());
            lblCourseName.setMinWidth(300);
            lblCourseName.setId("courseName");
            lblInfo = new Label();
            lblInfo.setMinWidth(300);
            lblInfo.setId("info");
            
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
            if (recordRow == null) {
                setStateNotBooked();
                return;
            }
            start = recordRow.getSession().getStart();
            end = recordRow.getSession().getEnd();
            Date current = new Date();
            if (start.after(current)) {
                //the entrance opens one hour before exam
                if (start.getTime() - current.getTime() > 1000 * 60 * 60) {
                    setStateBookedNotReady();
                } else {
                    setStateBookedReady();
                }
            } else if (end.before(current)) {
                setStateTesting();
            } else {
                setStateReview();
            }

        }

        private void setStateNotBooked() {
            VBox tempVbox = new VBox();
            tempVbox.getChildren().addAll(lblCourseName, lblInfo);
            this.getChildren().remove(0, this.getChildren().size());
            this.getChildren().addAll(lblCourseCode, tempVbox, button, indicator);
            lblInfo.setText("Please book this course first.");
            button.setText("Book");
            button.setOnAction((ActionEvent e) -> {
                try {
                    openBookForm();
                } catch (Exception ex) {
                }
            });
        }

        private void setStateBookedNotReady() {
            VBox tempVbox = new VBox();
            tempVbox.getChildren().addAll(lblCourseName, lblInfo);
            this.getChildren().remove(0, this.getChildren().size());
            this.getChildren().addAll(lblCourseCode, tempVbox, button, indicator);
            lblInfo.setText("starts at: " + start.toString());
            //set timer to change to bookedReady state
            button.setText("Change Session");
            button.setOnAction((ActionEvent e) -> {
                int index = vbox.getChildren().indexOf(this);
                Task<Void> deleteTask = new Task<Void>() {
                    @Override
                    protected void succeeded() {
                        super.succeeded();
                        InfoRow.this.indicator.setVisible(false);
                        InfoRow.this.button.setDisable(false);
                        InfoRow.this.setStateNotBooked();
                        vbox.getChildren().set(index, InfoRow.this);
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
                        ServerInterface.deleteBooking(recordRow);
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
                    openExamForm();
                } catch (Exception ex) {
                    Logger.getLogger(StudentFormController.class.getName()).log(Level.SEVERE, null, ex);
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
                try {
                    openReviewForm();
                } catch (Exception ex) {
                    Logger.getLogger(StudentFormController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }

    private void openBookForm() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BookForm.fxml"));
        Parent root = (Parent) loader.load();
        BookFormController controller = (BookFormController) loader.getController();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        controller.setStage(stage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(selfStage);
        stage.setResizable(false);
        stage.setTitle("Booking Management");
        stage.setScene(scene);
        stage.show();
    }

    private void openExamForm() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ExamForm.fxml"));
        Parent root = (Parent) loader.load();
        ExamFormController controller = (ExamFormController) loader.getController();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        controller.setStage(stage);
        stage.setScene(scene);
        stage.setTitle("ePoctor Student Client");
        stage.setScene(scene);
        stage.show();
    }

    private void openReviewForm() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ReviewForm.fxml"));
        Parent root = (Parent) loader.load();
        ReviewFormController controller = (ReviewFormController) loader.getController();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        controller.setStage(stage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(selfStage);
        stage.setResizable(false);
        stage.setTitle("Review Exam Results");
        stage.setScene(scene);
        stage.show();
    }

    public void setStage(Stage stage) {
        selfStage = stage;
    }
}
