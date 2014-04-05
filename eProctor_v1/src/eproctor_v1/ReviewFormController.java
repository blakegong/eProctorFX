package eproctor_v1;

import java.net.URL;
import java.util.ResourceBundle;
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
    private TableView<DatabaseInterface.RecordTableRow> table;
    
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
        updateTable();
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
        lblGrade.setText(data.getGrade());
        lblRemark.setText(data.getRemark());
    }
    
    public void updateTable() {
        DatabaseInterface.getTableRecords(table.getItems(), true);
        table.getColumns().get(0).setCellValueFactory(
                new PropertyValueFactory<>("id"));
        table.getColumns().get(1).setCellValueFactory(
                new PropertyValueFactory<>("course"));
        table.getColumns().get(2).setCellValueFactory(
                new PropertyValueFactory<>("session"));
        table.getColumns().get(3).setCellValueFactory(
                new PropertyValueFactory<>("grade"));
        table.getColumns().get(4).setCellValueFactory(
                new PropertyValueFactory<>("remark"));
    }
    
    public void setStage(Stage stage) {
        selfStage = stage;
    }
}
