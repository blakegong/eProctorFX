/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eproctor.proctor;

import eproctor.commons.DatabaseInterface;
import eproctor.commons.VideoServerInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Yue
 */
public class InvigilateFormController implements Initializable {

    private Stage selfStage;
    private ArrayList<DatabaseInterface.StudentRow> students;
    private String courseCode, sessionCode;

    public void setCourseCode(String courseCode) {
        System.out.println("xxxxxxx: " + courseCode);
        this.courseCode = courseCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public void setStudents(ArrayList<DatabaseInterface.StudentRow> students) {
        this.students = students;
    }

    public void showStudents() {
        for (DatabaseInterface.StudentRow student : students) {
            InfoPane temp = new InfoPane();
            temp.setStudent(student);
            temp.startReceive();
            flowPane.getChildren().add(temp);
        }
    }

    @FXML
    FlowPane flowPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO

    }

    void setStage(Stage stage) {
        selfStage = stage;
    }

    public class InfoPane extends TitledPane {

        private DatabaseInterface.StudentRow student;

        private ImageView imgWebcam;
        private ImageView imgDesktop;
        private Button btnTerminate;
        private TextArea msgReceived;
        private TextField msgToSend;
        private Button btnSend;
        private VideoServerInterface serviceReceiveImage;

        public InfoPane() {
            initializeUI();
        }

        private void initializeUI() {
            btnSend = new Button("Send");
            msgReceived = new TextArea();
            msgReceived.setMinWidth(200);
            msgToSend = new TextField();
            msgToSend.setMinWidth(200);
            VBox chatBox = new VBox();
            chatBox.setAlignment(Pos.BOTTOM_RIGHT);
            chatBox.setMaxSize(200, 100);
            chatBox.getChildren().addAll(msgReceived, msgToSend, btnSend);
            btnTerminate = new Button("Terminate Exam");
            HBox bottom = new HBox(50);
            bottom.setAlignment(Pos.CENTER);
            bottom.setMinSize(400, 100);
            bottom.getChildren().addAll(btnTerminate, chatBox);
            Image image1 = new Image("/eproctor/images/studentHome.png");
            imgWebcam = new ImageView();
            imgWebcam.setImage(image1);
            imgWebcam.setFitWidth(400);
            imgWebcam.setFitHeight(300);
            Image image2 = new Image("/eproctor/images/loginScreen.png");
            imgDesktop = new ImageView();
            imgDesktop.setImage(image2);
            imgDesktop.setFitWidth(400);
            imgDesktop.setFitHeight(300);
            VBox pane = new VBox();
            pane.setMinSize(400, 700);
            pane.getChildren().addAll(imgWebcam, imgDesktop, bottom);
            this.setContent(pane);
            this.setText("gong0025");
        }

        public void setStudent(DatabaseInterface.StudentRow student) {
            this.student = student;
        }

        public void startReceive() {
            System.out.println("student's name: " + student.getName());
            System.out.println("InfoPane: " + DatabaseInterface.userCode + " " + student.getUsername() + " " + "localhost" + 6002 + courseCode + sessionCode);
            serviceReceiveImage = new VideoServerInterface(DatabaseInterface.userCode, student.getUsername(), "localhost", 6002, courseCode, sessionCode);
            imgWebcam.imageProperty().bind(serviceReceiveImage.valueProperty());
            serviceReceiveImage.setOnCancelled(new EventHandler<WorkerStateEvent>() {

                @Override
                public void handle(WorkerStateEvent t) {
                    System.out.println("serviceReceiveImage cancelled.");
                }
            });
            serviceReceiveImage.setOnFailed(new EventHandler<WorkerStateEvent>() {

                @Override
                public void handle(WorkerStateEvent t) {
                    System.out.println("serviceReceiveImage failed.");
                }
            });
            serviceReceiveImage.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                @Override
                public void handle(WorkerStateEvent t) {
                    System.out.println("serviceReceiveImage succeeded.");
                }
            });
            serviceReceiveImage.start();
        }
    }
}
