package eproctor.commons;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

/**
 * Application Form Controller class contains the method to set background
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

    /**
     * This method is to set frame form by passing in Frame form Controller in
     * class FrameFormController
     *
     * @param frameFormController
     */
    public void setFrameFormController(FrameFormController frameFormController) {
        this.frameFormController = frameFormController;
    }
}
