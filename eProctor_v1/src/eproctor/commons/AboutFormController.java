package eproctor.commons;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

/**
 * About Form Controller class contains the method to set background
 *<p>This method initialates the essential elements like the frame of application, background color.
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
        if (eproctor.commons.DatabaseInterface.domain.equals("Student"))
            frameFormController.openStudentForm();
        else
            frameFormController.openProctorForm();
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
