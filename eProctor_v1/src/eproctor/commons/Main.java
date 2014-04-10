package eproctor.commons;

//<<<<<<< HEAD:eProctor_v1/src/eproctor/commons/Main.java
import java.io.File;
import java.io.FileNotFoundException;
//=======
//>>>>>>> 0afa6450b8606b1fe257fbb847646e730d0bfe8c:eProctor_v1/src/eproctor/commons/Main.java
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

/**
 * Main class of Application initiate from login Action
 * 
 * @author chenliyang
 * @author gongyue
 */
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
//<<<<<<< HEAD:eProctor_v1/src/eproctor/commons/Main.java
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/eproctor/commons/LoginForm.fxml"));
//=======
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginForm.fxml"));
//>>>>>>> 0afa6450b8606b1fe257fbb847646e730d0bfe8c:eProctor_v1/src/eproctor/commons/Main.java
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
