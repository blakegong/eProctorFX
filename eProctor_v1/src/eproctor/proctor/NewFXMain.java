/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eproctor.proctor;

import eproctor.proctor.InvigilateFormController;
import java.io.IOException;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Yue
 */
public class NewFXMain extends Application {
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("InvigilateForm.fxml"));
        Parent root = (Parent) loader.load();
        InvigilateFormController controller = (InvigilateFormController) loader.getController();
        
        Scene scene = new Scene(root);
        scene.setFill(null);
        stage.setTitle("eProctor");
        stage.setScene(scene);
        stage.setResizable(false);
//        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
