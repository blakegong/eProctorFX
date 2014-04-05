package eproctor_v1;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        showLogin(stage);
        ServerInterface.connectEProctorServer();
        ServerInterface.connectSchoolServer();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void showLogin(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginForm.fxml"));
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
