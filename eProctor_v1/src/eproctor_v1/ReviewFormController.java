package eproctor_v1;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ReviewFormController implements Initializable {
    private Stage selfStage;

    @FXML
    private TableView<ServerInterface.RecordTableRow> table;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        updateTable(ServerInterface.getTableCurrentBookings(true));
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
        TableColumn gradeCol = new TableColumn("Grade");
        gradeCol.setMinWidth(20);
        gradeCol.setCellValueFactory(
                new PropertyValueFactory<>("grade"));
        TableColumn remarkCol = new TableColumn("Remark");
        remarkCol.setMinWidth(230);
        remarkCol.setCellValueFactory(
                new PropertyValueFactory<>("remark"));
        
        table.setItems(data);
        table.getColumns().addAll(idCol, courseCol, sessionCol, gradeCol, remarkCol);
    }
    
    public void setStage(Stage stage) {
        selfStage = stage;
    }
}
