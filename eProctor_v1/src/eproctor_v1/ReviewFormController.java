package eproctor_v1;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ReviewFormController implements Initializable {
    private Stage selfStage;

    @FXML
    private TableView<ServerInterface.RecordTableRow> table;
    
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
    private Label lblGrade;
    
    @FXML
    private Label lblRemark;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        table.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            updateDetails(table.selectionModelProperty().get().getSelectedItem());
        });
        updateTable(ServerInterface.getTableRecords(true));
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
        lblGrade.setText(data.getGrade());
        lblRemark.setText(data.getRemark());
    }
    
    public void updateTable(ObservableList<ServerInterface.RecordTableRow> data) {
        table.getColumns().clear();
        TableColumn idCol = new TableColumn("Record ID");
        idCol.setMinWidth(200);
        idCol.setCellValueFactory(
                new PropertyValueFactory<>("id"));
        TableColumn courseCol = new TableColumn("Course");
        courseCol.setMinWidth(200);
        courseCol.setCellValueFactory(
                new PropertyValueFactory<>("course"));
        TableColumn sessionCol = new TableColumn("Session");
        sessionCol.setMinWidth(200);
        sessionCol.setCellValueFactory(
                new PropertyValueFactory<>("session"));
        TableColumn gradeCol = new TableColumn("Grade");
        gradeCol.setMinWidth(70);
        gradeCol.setCellValueFactory(
                new PropertyValueFactory<>("grade"));
        TableColumn remarkCol = new TableColumn("Remark");
        remarkCol.setMinWidth(100);
        remarkCol.setCellValueFactory(
                new PropertyValueFactory<>("remark"));
        
        table.setItems(data);
        table.getColumns().addAll(idCol, courseCol, sessionCol, gradeCol, remarkCol);
    }
    
    public void setStage(Stage stage) {
        selfStage = stage;
    }
}
