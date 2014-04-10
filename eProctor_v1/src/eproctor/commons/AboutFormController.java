package eproctor.commons;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author CLY
 */
public class AboutFormController implements Initializable {
    private FrameFormController frameFormController;

    @FXML
    private Button backBtn;
    @FXML
    private Pane backgroundPane;
    
    @FXML
    private void backPressed() {
        frameFormController.openStudentForm();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
    
    public void setBackground() {
        backgroundPane.setStyle("-fx-background-size: no-repeat; -fx-background-image: url(\"http://files.softicons.com/download/internet-icons/social-sketches-icons-by-an-phan-van/png/256/google.png\")");
    }

    public void setFrameFormController(FrameFormController frameFormController) {
        this.frameFormController = frameFormController;
    }
}