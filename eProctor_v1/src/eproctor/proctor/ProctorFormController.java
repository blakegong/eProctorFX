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
 * The Proctor form Controller class control the whole proctor interface
 * <p> contains all the functions
 *
 * @author Gong Yue
 * @author Chen Liyang
 * @author Lu ShengLiang
 * @author Yuan Zijie
 * @author Li Zixuan
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
            indicator = new ProgressIndicator();
            indicator.setVisible(false);
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
                setStateCanNotEnter();
            } else if (end.before(current)) {
                System.out.println("haha exam ended haha");
//                setStateReview();
            } else {
                setStateCanEnter();
            }
        }

        /**
         * set the state of the whole exam
         * <p> set the state in stateCanNotEnter if the start time is after the current time
         */
        private void setStateCanNotEnter() {
            VBox tempVbox = new VBox();
            tempVbox.getChildren().addAll(lblCourseName, lblInfo);
            this.getChildren().remove(0, this.getChildren().size());
            this.getChildren().addAll(lblCourseCode, tempVbox, button, indicator);
            button.setText("Haven't Started");
            button.setDisable(true);
            
            // = = = = = = =
            // start a countDOWN timer
            count = (int) ((start.getTime() - new Date().getTime()) / 1000);
            timer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (count < 30 * 60) {
                        timer.stop();
                    }
                    count--;
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
                    setState();
                }
            });
            timer.play();
            // = = = = = = =
        }

        /**
         * set the state of the whole exam
         * <p> set the state in setStateCanEnter if the start time is after the current time
         */
        private void setStateCanEnter() {
            VBox tempVbox = new VBox();
            tempVbox.getChildren().addAll(lblCourseName, lblInfo);
            this.getChildren().remove(0, this.getChildren().size());
            this.getChildren().addAll(lblCourseCode, tempVbox, button, indicator);
            button.setText("Invigilate");
            button.setDisable(false);
            button.setOnAction((ActionEvent e) -> {
                try {
                    timer.stop();
                    openInvigilateForm(recordRow, start, end);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            // = = = = = = =
            // start a count timer
            count = (int) ((new Date().getTime() - start.getTime() ) / 1000);
            timer = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    count++;
                    if (count < 0) {
                        lblInfo.setText("time to exam:\n\t" + intSecToReadableSecond(-count, 4));
                    } else if (count > 0) {
                        lblInfo.setText("exam has started for\n\t" + intSecToReadableSecond(count, 4));
                    } else {
                        timer.stop();
                        setState();
                    }
                }
            }));
            timer.setCycleCount(Timeline.INDEFINITE);
            timer.play();
            // = = = = = = =
        }
    }

    /**
     * trigger the invigilate interface to start the exam
     * @param recordRow the exam object
     * @param start start time
     * @param end end time
     * @throws Exception 
     */
    private void openInvigilateForm(DatabaseInterface.RecordRowProctor recordRow, Date start, Date end) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("InvigilateForm.fxml"));
        Scene invigilateScene = new Scene(loader.load());
        InvigilateFormController controller = (InvigilateFormController) loader.getController();

        Stage stage = new Stage();
        stage.setTitle("Invigilating");
        stage.setScene(invigilateScene);
        stage.show();
        controller.setStage(stage);
        controller.setCourseCode(recordRow.getCourse().getCode());
        controller.setSessionCode(recordRow.getSession().getCode());
        controller.setStudents(recordRow.getStudentList());
        controller.showStudents();
        controller.startTimer(start, end);
    }

    /**
     *set the window 
     * @param stage 
     */
    public void setStage(Stage stage) {
        selfStage = stage;
    }
}
