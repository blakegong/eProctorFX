package eproctor_v1;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import jfx.messagebox.MessageBox;

public class BookFormController implements Initializable {

    private Stage selfStage;

    @FXML
    private TableView<DatabaseInterface.RecordTableRow> table;

    @FXML
    private ListView<String> listCourses;

    @FXML
    private ListView<String> listSessions;

    @FXML
    private Label lblId;

    @FXML
    private Label lblCourseCode;

    @FXML
    private Label lblCourse;

    @FXML
    private Label lblSession;

    @FXML
    private Label lblProctor;

    @FXML
    private Label lblLocation;

    @FXML
    private Label lblStartTime;

    @FXML
    private Label lblEndTime;

    @FXML
    private void btnDeleteClicked() throws IOException {
        if (table.selectionModelProperty().get().getSelectedItem() == null) {
            return;
        }
        DatabaseInterface.deleteBooking(table.selectionModelProperty().get().getSelectedItem());
        MessageBox.show(selfStage,
                "The booking was successfully deleted.",
                "",
                MessageBox.ICON_INFORMATION);
        restartUI();
    }
    
    @FXML
    private void btnBookClicked() throws IOException {
        if (listSessions.getSelectionModel().getSelectedItems() != null) {
            DatabaseInterface.addBooking(listCourses.getSelectionModel().getSelectedIndex(), listSessions.getSelectionModel().getSelectedIndex());
        }
        MessageBox.show(selfStage,
                "The booking was successfully added.",
                "",
                MessageBox.ICON_INFORMATION);
        restartUI();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        table.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            updateDetails(table.selectionModelProperty().get().getSelectedItem());
        });
        listCourses.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            updateListSessions();
        });
        updateTable();
        updateListCourses();
    }
    
    public void restartUI() throws IOException {
//        selfStage.close();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BookForm.fxml"));
        Parent root = (Parent)loader.load();
        BookFormController controller = (BookFormController)loader.getController();
        Scene scene = new Scene(root);
        Stage stage = selfStage;
        controller.setStage(stage);
        stage.setTitle("ePoctor Student Client");
        stage.setScene(scene);
        stage.show();
    }

    public void updateDetails(DatabaseInterface.RecordTableRow data) {
        lblId.setText(data.getId());
        lblCourseCode.setText(data.getCourseCode());
        lblCourse.setText(data.getCourse());
        lblSession.setText(data.getSession());
        lblProctor.setText(data.getProctor());
        lblLocation.setText(data.getLocation());
        lblStartTime.setText(data.getStartTime());
        lblEndTime.setText(data.getEndTime());
    }

    public void updateTable() {
        DatabaseInterface.getTableRecords(table.getItems(), false);
        table.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        table.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("course"));
        table.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("session"));
    }

    public void updateListCourses() {
        DatabaseInterface.getListCourses(listCourses.getItems());
    }

    private void updateListSessions() {
        DatabaseInterface.getListSessions(listSessions.getItems(), listCourses.selectionModelProperty().get().getSelectedIndex());
    }

    public void setStage(Stage stage) {
        selfStage = stage;
    }
}
