/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eproctor.proctor;

import java.net.URL;
import java.util.ResourceBundle;
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
    
    @FXML
    FlowPane flowPane;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        InfoPane temp = new InfoPane();
        flowPane.getChildren().add(temp);
    }

    void setStage(Stage stage) {
        selfStage = stage;
    }
    
    public class InfoPane extends TitledPane {
        private ImageView imgWebcam;
        private ImageView imgDesktop;
        private Button btnTerminate;
        private TextArea msgReceived;
        private TextField msgToSend;
        private Button btnSend;
        
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
        
    }
    
}
