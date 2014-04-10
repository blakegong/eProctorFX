/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eproctor.proctor;

import eproctor.commons.DatabaseInterface;
import static eproctor.commons.Timer.intSecToReadableSecond;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author Gong Yue
 * @author Chen Liyang
 */
public class ProctorFormController implements Initializable {

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
        DatabaseInterface.getInfoDataProctor(this, infoData);
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
        private DatabaseInterface.RecordRowProctor recordRow;
        private Date start;
        private Date end;

        private int count;
        private Timeline timer;

        /**
         * Basic constructor of InfoRow, returning an object of InfoRow
         *
         * @param courseRow
         * @param recordRow
         *
         */
        public InfoRow(DatabaseInterface.RecordRowProctor recordRow) {
            setId("hbox");
            lblCourseCode = new Label(recordRow.getCourse().getCode());
            lblCourseCode.setMinWidth(200);
            lblCourseCode.setId("courseCode");
            lblCourseName = new Label(recordRow.getCourse().getName());
            lblCourseName.setMinWidth(300);
            lblCourseName.setId("courseName");
            lblInfo = new Label();
            lblInfo.setMinWidth(300);
            lblInfo.setId("info");
            button = new Button();
            button.setMinWidth(150);
            button.setId("button");
            
            this.recordRow = recordRow;
            setState();
        }

        /**
         * set current service stage. The state may be book-not-ready state;
         * book-ready state; review state; testing state, which are respecting
         * to different stage of UI.
         */
        private void setState() {
            start = recordRow.getSession().getStart();
            end = recordRow.getSession().getEnd();
            Date current = new Date();
            if (start.after(current)) {
                setStateBookedReady();
            } else if (end.before(current)) {
//                setStateReview();
            } else {
                setStateTesting();
            }
        }

        private void setStateBookedReady() {
            VBox tempVbox = new VBox();
            tempVbox.getChildren().addAll(lblCourseName, lblInfo);
            this.getChildren().remove(0, this.getChildren().size());
            this.getChildren().addAll(lblCourseCode, tempVbox, button, indicator);
            //set timer to count down
            lblInfo.setText("15:08:32 to the exam.");
            button.setText("Invigilate");
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
                    setState();
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
                    openInvigilateForm(recordRow);
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
    }
    
    private void openInvigilateForm(DatabaseInterface.RecordRowProctor recordRow) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("InvigilateForm.fxml"));
        Scene examScene = new Scene(loader.load());
        InvigilateFormController controller = (InvigilateFormController) loader.getController();

        Stage stage = new Stage();
        stage.setTitle("Invigilating");
        stage.setScene(examScene);
        stage.show();
        controller.setStage(stage);
        
    }

    /**
     *
     * @param stage
     */
    public void setStage(Stage stage) {
        selfStage = stage;
    }
}
