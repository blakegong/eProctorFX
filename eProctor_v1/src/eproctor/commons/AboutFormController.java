package eproctor.commons;

import eproctor.commons.FrameFormController;
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
        backgroundPane.setStyle("-fx-background-image: url(\"http://www.seomofo.com/downloads/new-google-logo-knockoff.png\")");
        System.out.println("style: " + backgroundPane.getStyle());
    }

    public void setFrameFormController(FrameFormController frameFormController) {
        this.frameFormController = frameFormController;
    }
}