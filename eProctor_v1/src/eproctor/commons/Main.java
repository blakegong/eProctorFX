package eproctor.commons;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Main class of Application initiate from login Action
 * 
 * @author chenliyang
 * @author gongyue
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {    
        String ip = SettingFormController.getSetting("ip");
        
        showLogin(stage);
        DatabaseInterface.connectEProctorServer();
        DatabaseInterface.connectSchoolServer();
        
        VideoServerInterface.ipFromConfig = ip;
    }

    /**
     *This launches main class
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
    /**
    * This method is to show login UI interface for user to login
    * @param stage
    * @throws IOException 
    */
    private void showLogin(Stage stage) throws IOException {
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
}
