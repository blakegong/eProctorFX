package eproctor;

import eproctor.student.DatabaseInterface;
import eproctor.student.SettingFormController;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        File config = new File("/eproctor/eProctor.configuration");
        Scanner sc = null;
        try {
            sc = new Scanner(config);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SettingFormController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        while (sc != null && sc.hasNext()) {
            System.out.println(sc.next());
        }
        System.out.println("no more.");
        
        
        showLogin(stage);
        DatabaseInterface.connectEProctorServer();
        DatabaseInterface.connectSchoolServer();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void showLogin(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/eproctor/LoginForm.fxml"));
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
