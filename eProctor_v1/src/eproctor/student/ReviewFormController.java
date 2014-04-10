/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eproctor.student;

import eproctor.commons.DatabaseInterface;
import eproctor.commons.FrameFormController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author Yue
 */
public class ReviewFormController implements Initializable {

    private FrameFormController frameFormController;
    private DatabaseInterface.RecordRowStudent recordRow;
    private DatabaseInterface.CourseRow courseRow;

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
//    @FXML
//    private Label lblLocation;
    @FXML
    private Label lblStartTime;

    @FXML
    private Label lblEndTime;

    @FXML
    private Label lblGrade;

    @FXML
    private Label lblRemark;

    @FXML
    private Button goBackButton;

    @FXML
    private void goBack() {
        frameFormController.openStudentForm();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void updateDetails() {
        lblId.setText(recordRow.getId());
        lblCourseCode.setText(courseRow.getCode());
        lblCourse.setText(courseRow.getName());
        lblSession.setText(recordRow.getSession().getCode());
        lblStartTime.setText(recordRow.getSession().getStart().toString());
        lblEndTime.setText(recordRow.getSession().getEnd().toString());
        lblGrade.setText(recordRow.getGrade());
        lblRemark.setText(recordRow.getRemark());
    }

    public void setRecordRow(DatabaseInterface.RecordRowStudent recordRow) {
        this.recordRow = recordRow;
    }

    public void setCourseRow(DatabaseInterface.CourseRow courseRow) {
        this.courseRow = courseRow;
    }

    public void setFrameFormController(FrameFormController frameFormController) {
        this.frameFormController = frameFormController;
    }
}
