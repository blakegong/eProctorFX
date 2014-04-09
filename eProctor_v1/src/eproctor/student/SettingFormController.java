package eproctor.student;

import com.googlecode.javacv.FrameGrabber;
import eproctor.student.VideoServerInterface.ServiceSendImage;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SettingFormController implements Initializable {

    private Stage selfStage;
    private FrameFormController frameFormController;
//    Image webcam_icon = new Image("/images/webcam_icon.gif");

    private FileChooser backgroundMusicFC = new FileChooser();
    private Desktop desktop = Desktop.getDesktop();

    @FXML
    private VBox settingVB;
    @FXML
    private ChoiceBox fontSizeCB;
    @FXML
    private ChoiceBox fontFamilyCB;
    @FXML
    private ToggleButton nightModeTB;
    @FXML
    private Button backgroundMusicButton;
    @FXML
    private ImageView cameraImageView;
    @FXML
    private ToggleButton testCameraTB;
    @FXML
    private Button goBackButton;

    @FXML
    private void chooseBackgroundMusic() {
        File file = backgroundMusicFC.showOpenDialog(selfStage);
        if (file != null) {
            try {
                desktop.open(file);
                update("backgroundMusic", file.getPath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void testCamera() {
        System.out.println("testCamera: pressed.");
        System.out.println("testCamera: status: " + testCameraTB.isSelected());

        if (!testCameraTB.isSelected()) {
            VideoServerInterface.serviceSendImage.cancel();
//            cameraImageView.imageProperty().setValue(webcam_icon);
            testCameraTB.setText("Test Camera");
        } else {
            VideoServerInterface.serviceSendImage = new ServiceSendImage(null, null, 0, null, null);
            VideoServerInterface.serviceSendImage.isLocal = true;
            cameraImageView.imageProperty().bind(VideoServerInterface.serviceSendImage.valueProperty());
            VideoServerInterface.serviceSendImage.setOnCancelled(new EventHandler<WorkerStateEvent>() {

                @Override
                public void handle(WorkerStateEvent t) {
                    try {
                        VideoServerInterface.serviceSendImage.getGrabber().stop();
                        System.out.println("Testing cancelled!");
                    } catch (FrameGrabber.Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            VideoServerInterface.serviceSendImage.setOnFailed(new EventHandler<WorkerStateEvent>() {

                @Override
                public void handle(WorkerStateEvent t) {
                    System.out.println("Testing failed!");
                }
            });
            VideoServerInterface.serviceSendImage.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                @Override
                public void handle(WorkerStateEvent t) {
                    System.out.println("Testing succeeded unexceptedly!");
                }
            });
            VideoServerInterface.serviceSendImage.start();

            testCameraTB.setText("Stop");
        }
    }

    @FXML
    private void goBack() {
        frameFormController.openStudentForm();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        this.initChoiceBox();
//        this.cameraImageView.setImage(webcam_icon);
    }

    public void update(String option, String newValue) {
        String path = "/eProctor/eProctor.configuration";

        File config = new File(path);
        Scanner sc = null;
        try {
            sc = new Scanner(config);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        String s = "";
        String comment = "";
        while (sc != null && sc.hasNext()) {
            String temp = sc.nextLine();
            if (temp.charAt(0) == '#') {
                comment += temp + "\n";
            } else {
                String op[] = temp.split("=");
                if (op[0].equals(option)) {
                    op[1] = newValue;
                    temp = op[0] + op[1] + "\n";
                }
                s += temp;
            }
        }
        sc.close();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(config);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        if (pw != null) {
            pw.print(comment + s);
        }
    }

    public void setStage(Stage stage) {
        selfStage = stage;
    }

    public void setFrameFormController(FrameFormController frameFormController) {
        this.frameFormController = frameFormController;
    }

    public void initChoiceBox() {
        fontSizeCB.setItems(FXCollections.observableArrayList(12, 14, 16, 18));
        fontSizeCB.getSelectionModel().selectFirst();
        fontFamilyCB.setItems(FXCollections.observableArrayList("Sans Serif", "Serif", "Monospaced", "Fantasy"));
        fontFamilyCB.getSelectionModel().selectFirst();
    }
}
