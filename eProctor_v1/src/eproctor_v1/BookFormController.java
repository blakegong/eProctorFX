package eproctor_v1;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    private void btnDeleteClicked() throws IOException {
        if (table.selectionModelProperty().get().getSelectedItem() == null) {
            return;
        }
        ServerInterface.deleteBooking(table.selectionModelProperty().get().getSelectedItem());
        restartUI();
//        refreshUI();
    }
    
    @FXML
    private void btnBookClicked() throws IOException {
        if (listSessions.getSelectionModel().getSelectedItems() != null) {
            ServerInterface.addBooking(listCourses.getSelectionModel().getSelectedIndex(), listSessions.getSelectionModel().getSelectedIndex());
        }
        restartUI();
//        refreshUI();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        table.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            updateDetails(table.selectionModelProperty().get().getSelectedItem());
        });
        listCourses.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
                int index = listCourses.selectionModelProperty().get().getSelectedIndex();
                updateListSessions(ServerInterface.getListSessions(index));
        });
        refreshUI();
    }
    
    public void restartUI() throws IOException {
        selfStage.close();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BookForm.fxml"));
        Parent root = (Parent)loader.load();
        BookFormController controller = (BookFormController)loader.getController();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        controller.setStage(stage);
        stage.setScene(scene);
        stage.setTitle("ePoctor Student Client");
        stage.setScene(scene);
        stage.show();
    }
    
    public void refreshUI() {
        clearTable();
        updateTable(ServerInterface.getTableRecords(false));
        clearDetails();
        clearLists();
        updateListCourses(ServerInterface.getListCourses());
        
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

    private void clearTable() {
        table.getItems().removeAll();
        table.setItems(table.getItems());
//        table.getItems().clear();
    }
    private void clearLists() {
        listCourses.getItems().removeAll();
        listCourses.setItems(listCourses.getItems());
        listSessions.getItems().removeAll();
        listSessions.setItems(listSessions.getItems());
//        listCourses.setItems(null);
//        listSessions.setItems(null);
//        listCourses.getItems().clear();
//        listSessions.getItems().clear();
    }

}
