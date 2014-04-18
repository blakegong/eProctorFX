package eproctor.commons;

import com.googlecode.javacv.FrameGrabber;
import eproctor.commons.VideoServerInterface.ServiceSendImage;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
import javafx.collections.FXCollections;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *Setting form class contains the whole interface concerning setting
 * <p> 
 * @author dingchengwang(javadoc haha)
 * @author liyang
 */
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
    /**
     * choose back ground music
     * <p> user can choose the music in setting
     */
    @FXML
    private void chooseBackgroundMusic() {
        File file = backgroundMusicFC.showOpenDialog(selfStage);
        if (file != null) {
            try {
                desktop.open(file);
//                update("backgroundMusic", file.getPath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    /**
     * the method tests the camera to check if it works
     * <p> user click the button to trigger the action 
     * the method shows the result of testing
     */
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
    /**
     * returns back to user home page
     * <p> return to home page by clicking on the go back button
     */
    @FXML
    private void goBack() {
        if (DatabaseInterface.domain.equals("Student"))
            frameFormController.openStudentForm();
        else
            frameFormController.openProctorForm();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        this.initChoiceBox();
        update("Gong Yue", "hahaha");
//        this.cameraImageView.setImage(webcam_icon);
    }

    /**
     *update all the change of setting to sever(database)
     * <p> after user change the configuration of the setting 
     * @param option option chosen by user
     * @param newValue new configuration value set by user
     */
    public static void update(String option, String newValue) {
        String workingDir = System.getProperty("user.dir");
        String a = System.getProperty("file.separator");
        String b = workingDir + a + "src" + a + "eproctor" + a + "commons" + a;
        String path  = b + "eProctor.configuration";
        System.out.println(path);
        
        File config = new File(path);
        Scanner sc = null;
        try {
            sc = new Scanner(config);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return ;
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
                    temp = op[0] + "=" + op[1] + "\n";
                }
                s += temp;
            }
        }
        System.out.println(s);
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
        pw.flush();
        pw.close();
    }

    /**
     *get setting returns the results by passing in option
     * @param option String 
     * @return
     */
    public static String getSetting(String option) {
        String workingDir = System.getProperty("user.dir");
        String a = System.getProperty("file.separator");
        String b = workingDir + a + "src" + a + "eproctor" + a + "commons" + a;
        String path  = b + "eProctor.configuration";
        System.out.println(path);
        
        File config = new File(path);
        Scanner sc = null;
        try {
            sc = new Scanner(config);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        String s = "";
        String comment = "";
        String result = "";
        while (sc != null && sc.hasNext()) {
            String temp = sc.nextLine();
            if (temp.length() <= 1)
                continue;
            if (temp.charAt(0) == '#') {
                comment += temp + "\n";
            } else {
                String op[] = temp.split("=");
                if (op[0].equals(option)) {
                    result = op[1];
                    temp = op[0] + "=" + op[1] + "\n";
                }
                s += temp + "\n";
            }
        }
        System.out.println(s);
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
        pw.flush();
        pw.close();
        
        return result;
    }
    
    /**
     *setup interface window 
     * <p> This method passes in stage to set the interface window
     * @param stage
     */
    public void setStage(Stage stage) {
        selfStage = stage;
    }

    /**
     *initiate setFrameFormController
     * <p> assign the frameFormController with the passing in frameFormController
     * @param frameFormController
     */
    public void setFrameFormController(FrameFormController frameFormController) {
        this.frameFormController = frameFormController;
    }

    /**
     *initiate choiceBox
     * <p> this method initiate the choiceBox including the items inside and the size of the box
     */
    public void initChoiceBox() {
        fontSizeCB.setItems(FXCollections.observableArrayList(12, 14, 16, 18));
        fontSizeCB.getSelectionModel().selectFirst();
        fontFamilyCB.setItems(FXCollections.observableArrayList("Sans Serif", "Serif", "Monospaced", "Fantasy"));
        fontFamilyCB.getSelectionModel().selectFirst();
    }
}
