/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eproctor_v1;

import com.mongodb.*;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.bson.types.ObjectId;

public class ServerInterface {

    private static DBCollection user, record, course, session, student, proctor, message;
    public static String domain, username, password, userCode;
    private static List<RecordRow> recordData;
    private static List<CourseRow> courseData;
    private static List<CourseRow> notBookedCoursesData;

    public ServerInterface() {

    }

    public static void connectEProctorServer() throws UnknownHostException {
        System.out.println("MongoHQServer connecting");
        MongoClientURI uri = new MongoClientURI("mongodb://admin:admin@oceanic.mongohq.com:10014/eProctor");
        MongoClient mongoClient = new MongoClient(uri);
        DB db = mongoClient.getDB("eProctor");
        record = db.getCollection("Record");
        message = db.getCollection("Message");
        System.out.println("MongoHQServer connected");
    }

    public static void connectSchoolServer() throws UnknownHostException {
        System.out.println("ValidationServer connecting");
        MongoClientURI uri = new MongoClientURI("mongodb://admin:admin@oceanic.mongohq.com:10015/NTU_Server");
        MongoClient mongoClient = new MongoClient(uri);
        DB db = mongoClient.getDB("NTU_Server");
        user = db.getCollection("User");
        course = db.getCollection("Course");
        session = db.getCollection("Session");
        student = db.getCollection("Student");
        proctor = db.getCollection("Proctor");
        System.out.println("ValidationServer connected");
    }

    public static boolean isUser(String domain, String username, String password) {
        QueryBuilder qb = new QueryBuilder();
        qb.put("username").is(username).put("password").is(password);
        DBObject obj;
        if (domain.equals("Student")) {
            obj = student.findOne(qb.get());
        } else {
            obj = proctor.findOne(qb.get());
        }
        if (obj == null) {
            System.out.println("login failed");
            return false;
        } else {
            userCode = (String) obj.get("user_code");
            ServerInterface.domain = domain;
            ServerInterface.username = username;
            ServerInterface.password = password;
            System.out.println(userCode);
            return true;
        }
    }

    public static void updateLocalRecordData() {
        recordData = new ArrayList();
        QueryBuilder recordQb = new QueryBuilder();
        //TODO diff domain
        recordQb.put("student_code").is(userCode);
        DBCursor recordCursor = record.find(recordQb.get());
        while (recordCursor.hasNext()) {
            DBObject recordObj = recordCursor.next();
            QueryBuilder courseQb = new QueryBuilder();
            courseQb.put("code").is(recordObj.get("course_code"));
            DBObject courseObj = course.findOne(courseQb.get());
            CourseRow courseRow = new CourseRow(((ObjectId) courseObj.get("_id")).toString(), (String) courseObj.get("code"), (String) courseObj.get("name"), null);

            QueryBuilder sessionQb = new QueryBuilder();
            sessionQb.put("code").is(recordObj.get("session_code"));
            DBObject sessionObj = session.findOne(sessionQb.get());
            SessionRow sessionRow = new SessionRow(((ObjectId) sessionObj.get("_id")).toString(), (String) sessionObj.get("code"), (Date) sessionObj.get("start"), (Date) sessionObj.get("end"), (String) sessionObj.get("location"));

            recordData.add(new RecordRow(((ObjectId) recordObj.get("_id")).toString(), courseRow, sessionRow, (String) recordObj.get("student_code"), (String) recordObj.get("proctor_code"), (boolean) recordObj.get("takenStatus"), (String) recordObj.get("grade"), (String) recordObj.get("remark")));
        }
    }

    public static void updateLocalCourseData() {
        courseData = new ArrayList();
        QueryBuilder qbStudent = new QueryBuilder();
        qbStudent.put("user_code").is(userCode);
        DBObject objStudent = student.findOne(qbStudent.get());
        BasicDBList listCourses = (BasicDBList) objStudent.get("enrolledCourses");
        for (Object course_code : listCourses) {
            List<SessionRow> sessionData = new ArrayList();
            QueryBuilder qbCourse = new QueryBuilder();
            qbCourse.put("code").is((String) course_code);
            DBObject objCourse = course.findOne(qbCourse.get());
            BasicDBList listSessions = (BasicDBList) objCourse.get("sessions");
            for (Object session_code : listSessions) {
                QueryBuilder qbSession = new QueryBuilder();
                qbSession.put("code").is((String) session_code);
                DBObject objSession = session.findOne(qbSession.get());
                SessionRow sessionRow = new SessionRow(((ObjectId) objSession.get("_id")).toString(), (String) objSession.get("code"), (Date) objSession.get("start"), (Date) objSession.get("end"), (String) objSession.get("location"));
                sessionData.add(sessionRow);
            }
            CourseRow courseRow = new CourseRow(((ObjectId) objCourse.get("_id")).toString(), (String) objCourse.get("code"), (String) objCourse.get("name"), sessionData);
            courseData.add(courseRow);
        }
    }

    public static void updateLocalData() {
        updateLocalRecordData();
        updateLocalCourseData();
    }

    public static String getTextAreaInformation() {
        String newInfo = new String();
        newInfo += "Hello " + username + "\n\n";
        newInfo += "You have " + recordData.size() + " exams left:\n\n";
        for (RecordRow recordRow : recordData) {
            newInfo += recordRow.course.code + " " + recordRow.course.name + ":\n" + recordRow.session.start + "\n\n";
        }
        return newInfo;
    }

    public static String getTextAreaRecentMessages() {
        //return Messager.pollMsg(Main.user_id, "Student");
        return "Not done yet";
    }

    public static ObservableList<RecordTableRow> getTableRecords(boolean takenStatus) {
        List<RecordTableRow> list = new ArrayList();
        for (RecordRow row : recordData) {
            if (row.takenStatus != takenStatus) {
                continue;
            }
            String strSession, strStartTime, strEndTime;
            SimpleDateFormat startFormat = new SimpleDateFormat(
                    "dd.MM.yyyy E kk:mm");
            SimpleDateFormat endFormat = new SimpleDateFormat("'-'kk:mm");
            strSession = startFormat.format(row.session.start)
                    + endFormat.format(row.session.end);
            strStartTime = startFormat.format(row.session.start);
            strEndTime = startFormat.format(row.session.end);

            list.add(new RecordTableRow(row.id, row.course.code, row.course.name, strSession, row.proctor_code, row.session.location, strStartTime, strEndTime, row.grade, row.remark));
        }
        return FXCollections.observableList(list);
    }

    public static void addBooking(int courseIndex, int sessionIndex) {
        BasicDBObjectBuilder document = new BasicDBObjectBuilder();
        document.add("course_code", notBookedCoursesData.get(courseIndex).code)
                .add("session_code", notBookedCoursesData.get(courseIndex).sessions.get(sessionIndex).code)
                .add("student_code", userCode).add("proctor_code", "")
                .add("takenStatus", false).add("grade", "").add("remark", "");
        record.insert(document.get());
        updateLocalData();
    }

    public static void deleteBooking(RecordTableRow data) {
        QueryBuilder qb = new QueryBuilder();
        qb.put("_id").is(new ObjectId(data.getId()));
        record.remove(qb.get());
        updateLocalData();
    }

    public static ObservableList<String> getListCourses() {
        notBookedCoursesData = new ArrayList();
        List<String> list = new ArrayList();
        boolean isBooked = false;
        for (CourseRow courseRow : courseData) {
            isBooked = false;
            for (RecordRow recordRow : recordData) {
                if (recordRow.course.code.equals(courseRow.code)) {
                    isBooked = true;
                    break;
                }
            }
            if (!isBooked) {
                notBookedCoursesData.add(courseRow);
                list.add(courseRow.code + " " + courseRow.name);
            }
        }
        return FXCollections.observableList(list);
    }

    public static ObservableList<String> getListSessions(int index) {
        List<String> list = new ArrayList();
        for (SessionRow sessionRow : notBookedCoursesData.get(index).sessions) {
            SimpleDateFormat startFormat = new SimpleDateFormat(
                    "dd.MM.yyyy E kk:mm");
            SimpleDateFormat endFormat = new SimpleDateFormat("'-'kk:mm");
            list.add(startFormat.format(sessionRow.start) + endFormat.format(sessionRow.end));
        }
        return FXCollections.observableList(list);
    }

    public static class RecordRow {

        private final String id;
        private final CourseRow course;
        private final SessionRow session;
        private final String student_code;
        private final String proctor_code;
        private final boolean takenStatus;
        private final String grade;
        private final String remark;

        public RecordRow(String id, CourseRow course, SessionRow session, String student_code, String proctor_code, boolean takenStatus, String grade, String remark) {
            this.id = id;
            this.course = course;
            this.session = session;
            this.student_code = student_code;
            this.proctor_code = proctor_code;
            this.takenStatus = takenStatus;
            this.grade = grade;
            this.remark = remark;
        }

    }

    public static class CourseRow {

        private final String id;
        private final String code;
        private final String name;
        private final List<SessionRow> sessions;

        public CourseRow(String id, String code, String name, List<SessionRow> sessions) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.sessions = sessions;
        }
    }

    public static class SessionRow {

        private final String id;
        private final String code;
        private final Date start;
        private final Date end;
        private final String location;

        public SessionRow(String id, String code, Date start, Date end, String location) {
            this.id = id;
            this.code = code;
            this.start = start;
            this.end = end;
            this.location = location;
        }
    }

    public static class RecordTableRow {

        private final SimpleStringProperty id;
        private final SimpleStringProperty courseCode;
        private final SimpleStringProperty course;
        private final SimpleStringProperty session;
        private final SimpleStringProperty proctor;
        private final SimpleStringProperty location;
        private final SimpleStringProperty startTime;
        private final SimpleStringProperty endTime;
        private final SimpleStringProperty grade;
        private final SimpleStringProperty remark;

        public RecordTableRow(String id, String courseCode, String course, String session, String proctor, String location, String startTime, String endTime, String grade, String remark) {
            this.id = new SimpleStringProperty(id);
            this.courseCode = new SimpleStringProperty(courseCode);
            this.course = new SimpleStringProperty(course);
            this.session = new SimpleStringProperty(session);
            this.proctor = new SimpleStringProperty(proctor);
            this.location = new SimpleStringProperty(location);
            this.startTime = new SimpleStringProperty(startTime);
            this.endTime = new SimpleStringProperty(endTime);
            this.grade = new SimpleStringProperty(grade);
            this.remark = new SimpleStringProperty(remark);
        }

        public String getId() {
            return id.get();
        }

        public String getCourseCode() {
            return courseCode.get();
        }

        public String getCourse() {
            return course.get();
        }

        public String getSession() {
            return session.get();
        }

        public String getProctor() {
            return proctor.get();
        }

        public String getLocation() {
            return location.get();
        }

        public String getStartTime() {
            return startTime.get();
        }

        public String getEndTime() {
            return endTime.get();
        }

        public String getGrade() {
            return grade.get();
        }

        public String getRemark() {
            return remark.get();
        }

    }
}
