/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eproctor_v1;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Yue
 */
public class ExamFormController implements Initializable {

    private Stage selfStage;
    
    @FXML
    private WebView browser;
    
    @FXML
    private void action() {
        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
//        browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.load("https://www.google.com.sg/#q=nanshen+memeda");
    }    
    
    public void setStage(Stage stage) {
        selfStage = stage;
    }
}
