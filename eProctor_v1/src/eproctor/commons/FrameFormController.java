package eproctor.commons;

import eproctor.student.ReviewFormController;
import eproctor.student.StudentFormController;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Frame form Controller class
 *
 * @author CLY
 */
public class FrameFormController implements Initializable {

    private Stage selfStage;
    private AnchorPane studentView, proctorView, coordinatorView, settingView, reviewView;
    private StackPane aboutView;

    @FXML
    private BorderPane mainPane;

    @FXML
    private ImageView settingImageView;

    @FXML
    private ImageView aboutImageView;

    @FXML
    private ImageView logoutImageView;

    @FXML
    private Label settingLabel;
    @FXML
    private Label aboutLabel;
    @FXML
    private Label logoutLabel;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        this.setToolTips();
        this.openStudentForm();

    }

    /**
     *
     */
    @FXML
    public void openStudentForm() {
        if (studentView == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/eproctor/student/StudentForm.fxml"));
            try {
                studentView = loader.load();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            StudentFormController studentFormController = loader.getController();
            studentFormController.setFrameFormController(this);
        }
        mainPane.setCenter(studentView);
    }

    /**
     *
     */
    @FXML
    public void openProctorForm() {
        if (proctorView == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/eproctor/proctor/ProctorForm.fxml"));
            try {
                proctorView = loader.load();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        mainPane.setCenter(proctorView);
    }
    
    /**
     *
     */
    @FXML
    public void openCoordinatorForm() {
        if (coordinatorView == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/eproctor/coordinator/CoordinatorForm.fxml"));
            try {
                coordinatorView = loader.load();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        mainPane.setCenter(coordinatorView);
    }

    @FXML
    private void openSettingForm() throws Exception {
        if (settingView == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/eproctor/commons/SettingForm.fxml"));
            try {
                settingView = loader.load();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            SettingFormController settingFormController = loader.getController();
            settingFormController.setFrameFormController(this);
            settingFormController.setStage(selfStage);
        }
        mainPane.setCenter(settingView);
    }

    @FXML
    private void openAboutForm() {
        if (aboutView == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/eproctor/commons/AboutForm.fxml"));
            try {
                aboutView = loader.load();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            AboutFormController aboutFormController = loader.getController();
            aboutFormController.setFrameFormController(this);
            aboutFormController.setBackground();
        }
        mainPane.setCenter(aboutView);
    }

    @FXML
    private void logout() throws Exception {
        System.out.println("logout");
        selfStage.close();

        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/eproctor/commons/LoginForm.fxml"));
        Parent root = (Parent) loader.load();
        LoginFormController controller = (LoginFormController) loader.getController();
        controller.setStage(stage);
        Scene scene = new Scene(root);
        scene.setFill(null);
        stage.setTitle("eProctor");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
    }

    public void openReviewView(DatabaseInterface.RecordRowStudent recordRow, DatabaseInterface.CourseRow courseRow) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/eproctor/student/ReviewForm.fxml"));
        try {
            reviewView = loader.load();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ReviewFormController controller = (ReviewFormController) loader.getController();
        controller.setFrameFormController(this);
        controller.setRecordRow(recordRow);
        controller.setCourseRow(courseRow);
        controller.updateDetails();

        mainPane.setCenter(reviewView);
    }

    public void closeReviewView() {
        mainPane.setBottom(null);
        this.openStudentForm();
    }

    public void setToolTips() {
//        this.settingLabel.setTooltip(new Tooltip("Open setting view"));
//        this.aboutLabel.setTooltip(new Tooltip("Open about view"));
//        this.logoutLabel.setTooltip(new Tooltip("Log out"));
    }

    /**
     *
     */
    public void setBackground() {
        mainPane.setStyle("-fx-background-image: url(\"/eproctor/images/studentHome.png\");"); // not working
        System.out.println("mainPane style: " + mainPane.getStyle());
    }

    /**
     *
     * @param selfStage
     */
    public void setSelfStage(Stage selfStage) {
        this.selfStage = selfStage;
    }
}
