package eproctor_v1;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.bson.types.ObjectId;

public class BookFormController implements Initializable {

    private Stage selfStage;

    @FXML
    private TableView<ServerInterface.RecordTableRow> table;

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
    private void tableClicked() {
        if (table.selectionModelProperty().get().getSelectedItem() == null) {
            return;
        }
        updateDetails(table.selectionModelProperty().get().getSelectedItem());
    }

    @FXML
    private void btnDeleteClicked() {
        if (table.selectionModelProperty().get().getSelectedItem() == null) {
            return;
        }
        ServerInterface.deleteBooking(table.selectionModelProperty().get().getSelectedItem());
        refreshUI();
    }

    @FXML
    private void listCoursesClicked() {
        int index = listCourses.selectionModelProperty().get().getSelectedIndex();
        if (index < 0) {
            return;
        }
        updateListSessions(ServerInterface.getListSessions(index));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        refreshUI();
    }
    
    public void refreshUI() {
        updateTable(ServerInterface.getTableRecords(false));
        clearDetails();
        updateListCourses(ServerInterface.getListCourses());
        clearListSessions();
    }

    public void updateDetails(ServerInterface.RecordTableRow data) {
        lblId.setText(data.getId());
        lblCourseCode.setText(data.getCourseCode());
        lblCourse.setText(data.getCourse());
        lblSession.setText(data.getSession());
        lblProctor.setText(data.getProctor());
        lblLocation.setText(data.getLocation());
        lblStartTime.setText(data.getStartTime());
        lblEndTime.setText(data.getEndTime());
    }

    public void updateTable(ObservableList<ServerInterface.RecordTableRow> data) {
        table.getColumns().clear();
        TableColumn idCol = new TableColumn("Record ID");
        idCol.setMinWidth(200);
        idCol.setCellValueFactory(
                new PropertyValueFactory<>("id"));
        TableColumn courseCol = new TableColumn("Course");
        courseCol.setMinWidth(300);
        courseCol.setCellValueFactory(
                new PropertyValueFactory<>("course"));
        TableColumn sessionCol = new TableColumn("Session");
        sessionCol.setMinWidth(230);
        sessionCol.setCellValueFactory(
                new PropertyValueFactory<>("session"));
        table.setItems(data);
        table.getColumns().addAll(idCol, courseCol, sessionCol);
    }

    public void updateListCourses(ObservableList<String> items) {
        if (items.size() == 0) {
            return;
        }
        listCourses.setItems(items);
    }

    private void updateListSessions(ObservableList<String> items) {
        if (items.size() == 0) {
            return;
        }
        listSessions.setItems(items);
    }

    public void setStage(Stage stage) {
        selfStage = stage;
    }

    public void clearDetails() {
        lblId.setText(null);
        lblCourseCode.setText(null);
        lblCourse.setText(null);
        lblSession.setText(null);
        lblProctor.setText(null);
        lblLocation.setText(null);
        lblStartTime.setText(null);
        lblEndTime.setText(null);
    }

    private void clearListSessions() {
        listSessions.getItems().clear();
    }

}
