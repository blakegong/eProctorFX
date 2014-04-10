package eproctor.commons;

import eproctor.commons.FrameFormController;
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
     * This method is to set background
     */
    public void setBackground() {
        backgroundPane.setStyle("-fx-background-image: url(\"http://www.seomofo.com/downloads/new-google-logo-knockoff.png\")");
        System.out.println("style: " + backgroundPane.getStyle());
    }

    /**
     * This method is to set frame form by passing in Frame form Controller in class FrameFormController
     * @param frameFormController
     */
    public void setFrameFormController(FrameFormController frameFormController) {
        this.frameFormController = frameFormController;
    }
}