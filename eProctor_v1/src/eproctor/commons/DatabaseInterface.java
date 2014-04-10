package eproctor.commons;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;
import eproctor.proctor.ProctorFormController;
import eproctor.student.StudentFormController;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Node;
import org.bson.types.ObjectId;

/**
 * This class is a server/database interface class runs on client side. It has
 * communication with server through different DBCollections.
 *
 * @author Gong Yue
 * @author Chen Liyang
 */
public class DatabaseInterface {

    public static DBCollection user, record, course, session, student, proctor, message;
    public static String domain, username, password, userCode;
    private static List<RecordRowStudent> recordDataStudent;
    public static List<RecordRowProctor> recordDataProctor;
    public static List<CourseRow> courseData;

    /**
     * This method sets courseData into appropriate format and pass it to UI and
     * its controller.
     *
     * @param controller Controller for Student form
     * @param infoData ObservableList of student information
     */
    public static void getInfoDataStudent(StudentFormController controller, ObservableList<Node> infoData) {
        for (CourseRow courseRow : courseData) {
            RecordRowStudent recordRow = null;
            for (RecordRowStudent tempRecord : recordDataStudent) {
                if (courseRow.code.equals(tempRecord.course.code)) {
                    recordRow = tempRecord;
                }
            }
            StudentFormController.InfoRow infoRow = controller.new InfoRow(courseRow, recordRow);
            infoData.add(infoRow);
        }
    }

    /**
     *
     * @param controller
     * @param infoData
     */
    public static void getInfoDataProctor(ProctorFormController controller, ObservableList<Node> infoData) {
        for (RecordRowProctor tempRecord : recordDataProctor) {
            ProctorFormController.InfoRow infoRow = controller.new InfoRow(tempRecord);
            infoData.add(infoRow);
        }
    }
    
    /**
     * This method is to connect EProctor application to mongodb Server
     *
     * @throws UnknownHostException
     */
    public static void connectEProctorServer() throws UnknownHostException {
        MongoClientURI uri = new MongoClientURI("mongodb://admin:admin@oceanic.mongohq.com:10014/eProctor");
        MongoClient mongoClient = new MongoClient(uri);
        DB db = mongoClient.getDB("eProctor");
        record = db.getCollection("Record");
        message = db.getCollection("Message");
    }

    /**
     * This method is to connect EProctor application to School Server(i.e. NTU
     * Server)
     *
     * @throws UnknownHostException
     */
    public static void connectSchoolServer() throws UnknownHostException {
        MongoClientURI uri = new MongoClientURI("mongodb://admin:admin@oceanic.mongohq.com:10015/NTU_Server");
        MongoClient mongoClient = new MongoClient(uri);
        DB db = mongoClient.getDB("NTU_Server");
        user = db.getCollection("User");
        course = db.getCollection("Course");
        session = db.getCollection("Session");
        student = db.getCollection("Student");
        proctor = db.getCollection("Proctor");
    }

    /**
     * This method is to verify the account of user (student, proctor,
     * supervisor )
     *
     * @param domain type of user (student, proctor, supervisor)
     * @param username username of the user
     * @param password password of the user which can be verified by server
     * @return
     */
    public static boolean isUser(String domain, String username, String password) {
        QueryBuilder qb = new QueryBuilder();
        qb.put("username").is(username).put("password").is(password);
        DBObject obj;
        switch (domain) {
            case "Student":
                obj = student.findOne(qb.get());
                break;
            case "Proctor":
                obj = proctor.findOne(qb.get());
                break;
            default://search in a non exist db, so can return null
                obj = record.findOne(qb.get());
                break;
        }
        if (obj == null) {
            System.out.println("login failed");
            return false;
        } else {
            userCode = (String) obj.get("user_code");
            DatabaseInterface.domain = domain;
            DatabaseInterface.username = username;
            DatabaseInterface.password = password;
            System.out.println(userCode);
            return true;
        }
    }

    /**
     * This method is to update student local record data to (server) database
     * Method add data onto database if there is any data on student side to be
     * updated.
     *
     * @param progress
     */
    public static void updateLocalRecordDataStudent(SimpleDoubleProperty progress) {
        if (progress != null) // update progress bar...
        {
            progress.set(0);
        }

        recordDataStudent = new ArrayList();
        QueryBuilder recordQb = new QueryBuilder();
        //TODO diff domain
        recordQb.put("student_code").is(userCode);

        System.out.println("updateLocalRecordData: query: " + recordQb.get());
        DBCursor recordCursor = record.find(recordQb.get());

        if (progress != null) // update progress bar...
        {
            progress.set(0.2);
        }

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

            recordDataStudent.add(new RecordRowStudent(((ObjectId) recordObj.get("_id")).toString(), courseRow, sessionRow, (String) recordObj.get("student_code"), (String) recordObj.get("grade"), (String) recordObj.get("remark")));

            if (progress != null) { // update progress bar...
                progress.set(progress.add(0.8 / recordCursor.size()).get());
            }
        }

        if (progress != null) { // update progress bar...
            progress.set(1);
        }
    }

    /**
     * This method is to update Proctor local record data to (server) database
     * <p>
     * Method add data onto database if there is any data on Proctor side to be
     * updated.
     *
     */
    public static void updateLocalRecordDataProctor() {
        recordDataProctor = new ArrayList();
        QueryBuilder recordQb = new QueryBuilder();
        //TODO diff domain
        recordQb.put("proctor_code").is(userCode);
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

            ArrayList<StudentRow> studentList = new ArrayList();
            BasicDBObject queryTemp = new BasicDBObject("course_code", recordObj.get("course_code")).append("session_code", recordObj.get("session_code"))
                    .append("student_code", new BasicDBObject("$exists", true));
            System.out.println("queryTemp: \n" + queryTemp);
            DBCursor studentCursor = record.find(queryTemp);
            while (studentCursor.hasNext()) {
                DBObject studentObj = studentCursor.next();
                QueryBuilder qbStudent = new QueryBuilder();
                qbStudent.put("user_code").is(studentObj.get("student_code"));
                System.out.println("qbStudent: \n" + qbStudent.get());
                DBObject dboStudent = student.findOne(qbStudent.get());
                System.out.println("updateLocalRecordDataProctor: studentObj: " + studentObj);
                System.out.println("updateLocalRecordDataProctor: dboStudent: " + dboStudent);
                StudentRow temp = new StudentRow((String) dboStudent.get("username"), (String) dboStudent.get("name"));
                studentList.add(temp);
            }
            recordDataProctor.add(new RecordRowProctor(((ObjectId) recordObj.get("_id")).toString(), courseRow, sessionRow, (String) recordObj.get("proctor_code"), studentList));
        }
    }

    /**
     * This method is to update local course record data to (server) database
     * Method add data onto database if there is any data on user side to be
     * updated.
     *
     * @param progress
     */
    public static void updateLocalCourseDataStudent(SimpleDoubleProperty progress) {
        if (progress != null) // update progress bar...
        {
            progress.set(0);
        }

        courseData = new ArrayList();
        QueryBuilder qbStudent = new QueryBuilder();
        qbStudent.put("user_code").is(userCode);
        DBObject objStudent = student.findOne(qbStudent.get());
        BasicDBList listCourses = (BasicDBList) objStudent.get("enrolledCourses");

        if (progress != null) // update progress bar...
        {
            progress.set(0.2);
        }

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

            if (progress != null) // update progress bar...
            {
                progress.set(progress.add(0.8 / listCourses.size()).get());
            }
        }

        if (progress != null) // update progress bar...
        {
            progress.set(1);
        }
    }
    
    /**
     * This method works as an interface to update user data onto database
     * <p>
     * data contains student info, course info and proctor info
     */
    public static void updateLocalDataStudent() {
        updateLocalRecordDataStudent(null);
        updateLocalCourseDataStudent(null);
    }

    /**
     *
     */
    public static void updateLocalDataProctor() {
        updateLocalRecordDataProctor();
    }

    /**
     * This method is to get the message (course info) and displaying on the
     * screen
     * <p>
     * This will display courses student take for Student or courses proctor
     * invigilate for Proctor
     *
     * @return String contains the courses
     */
    public static String getTextAreaRecentMessages() {
        //return Messager.pollMsg(Main.user_id, "Student");
        return "Not done yet";
    }

    /**
     * This is to book exams of courses for student
     *
     * @param courseRow course information
     * @param sessionRow exam information
     * @return recordRow if the exam session can be found else return null
     */
    public static RecordRowStudent addBookingStudent(CourseRow courseRow, SessionRow sessionRow) {
        BasicDBObjectBuilder document = new BasicDBObjectBuilder();
        document.add("course_code", courseRow.code)
                .add("session_code", sessionRow.getCode());
        if (domain.equals("Student")) {
            document.add("student_code", userCode).add("grade", "").add("remark", "");
        } else {
            document.add("proctor_code", userCode);
        }
        record.insert(document.get());
        updateLocalDataStudent();
        for (RecordRowStudent recordRow : recordDataStudent) {
            if (recordRow.course.code.equals(courseRow.code) && recordRow.session.getCode().equals(sessionRow.getCode()) && recordRow.student_code.equals(userCode)) {
                return recordRow;
            }
        }
        return null;
    }

    /**
     * This method is to delete the exams student have booked
     *
     * @param id exam record id
     */
    public static void deleteBookingStudent(String id) {
        QueryBuilder qb = new QueryBuilder();
        qb.put("_id").is(new ObjectId(id));
        record.remove(qb.get());
        updateLocalDataStudent();
    }

    /**
     * This method is to form a list of exam sessions of course
     *
     * @param list ObservableList String list of formated exam session
     * @param courseRow course info
     */
    public static void getListSessionsStudent(ObservableList<String> list, CourseRow courseRow) {
        while (!list.isEmpty()) {
            list.remove(0);
        }
        for (SessionRow sessionRow : courseRow.sessions) {
            SimpleDateFormat startFormat = new SimpleDateFormat(
                    "dd.MM.yyyy E kk:mm");
            SimpleDateFormat endFormat = new SimpleDateFormat("'-'kk:mm");
            list.add(startFormat.format(sessionRow.start) + endFormat.format(sessionRow.end));
        }
    }

    public static String getName(String username) {
        DBObject result = DatabaseInterface.student.findOne(new BasicDBObject("username", username));
        if (result == null) {
            result = DatabaseInterface.proctor.findOne(new BasicDBObject("username", username));
        }
        return (String) result.get("name");
    }

    public static String randomProctor(String courseCode, String sessionCode) {
        BasicDBObject query = new BasicDBObject("course_code", courseCode)
                                                .append("session_code", sessionCode)
                                                .append("proctor_code", new BasicDBObject("$exists", true));
        System.out.println("randomProctor: query:\n" + query);
        DBCursor cur = record.find(query);
        int no = 0;
        System.out.println("cur.size(): " + cur.size() + ", no: " + no);
        Random rd = new Random();
        no = rd.nextInt(cur.size());
        System.out.println("cur.size(): " + cur.size() + ", no: " + no);
        for (int i = 0; i < no; i++) {
            cur.next();
        }
        String temp = (String)cur.next().get("proctor_code");
        System.out.println("temp: " + temp);
        BasicDBObject query2 = new BasicDBObject("user_code", temp);
        String temp2 = (String) proctor.findOne(query2).get("username");
        System.out.println("temp2: " + temp2);
        return temp2;
    }
    
    /**
     * This is the Constructor of DatabaseInterface class Suppresses default
     * constructor, ensuring non-instantiation.
     */
    public DatabaseInterface() {
        
    }


    /**
     * This class is the data container of student review UI table row.
     * <p>
     * one RecordRowStudent object contains record id, course code, session
     * code, student id, student grade and exam remark.</p>
     */
    public static class RecordRowStudent {

        private final String id;
        private final CourseRow course;
        private final SessionRow session;
        private final String student_code;
        private final String grade;
        private final String remark;

        /**
         * This is to set RecordRow (student exam information)
         *
         * @param id
         * @param course
         * @param session
         * @param student_code
         * @param grade
         * @param remark
         */
        public RecordRowStudent(String id, CourseRow course, SessionRow session, String student_code, String grade, String remark) {
            this.id = id;
            this.course = course;
            this.session = session;
            this.student_code = student_code;
            this.grade = grade;
            this.remark = remark;
        }

        /**
         * This is getter of sessionRow
         *
         * @return
         */
        public SessionRow getSession() {
            return session;
        }

        /**
         * This is getter of getGrade
         *
         * @return
         */
        public String getGrade() {
            return grade;
        }

        /**
         * This is getter of getRemark
         *
         * @return
         */
        public String getRemark() {
            return remark;
        }

        /**
         * This is getter of id
         *
         * @return
         */
        public String getId() {
            return id;
        }

        /**
         *
         * @return
         */
        public CourseRow getCourse() {
            return course;
        }

    }

    /**
     * This class is the data container of proctor review UI table row.
     * <p>
     * one RecordRowStudent object contains record id, course code, session code
     * and proctor id.</p>
     */
    public static class RecordRowProctor {

        private final String id;
        private final CourseRow course;
        private final SessionRow session;
        private final String proctor_code;
        private final ArrayList<StudentRow> studentList;

        /**
         * This is constructor for RecordRowProctor
         *
         * @param id
         * @param course
         * @param session
         * @param proctor_code
         * @param studentList
         */
        public RecordRowProctor(String id, CourseRow course, SessionRow session, String proctor_code, ArrayList<StudentRow> studentList) {
            this.id = id;
            this.course = course;
            this.session = session;
            this.proctor_code = proctor_code;
            this.studentList = studentList;
        }

        /**
         * This is getter of sessionRow
         *
         * @return
         */
        public SessionRow getSession() {
            return session;
        }

        /**
         *
         * @return
         */
        public String getId() {
            return id;
        }

        /**
         *
         * @return
         */
        public CourseRow getCourse() {
            return course;
        }

        /**
         *
         * @return
         */
        public String getProctor_code() {
            return proctor_code;
        }

        /**
         *
         * @return
         */
        public ArrayList<StudentRow> getStudentList() {
            return studentList;
        }

    }

    /**
     * This class is the data container of student booking UI course table row.
     * <p>
     * one RecordRowStudent object contains object id, course code, course name
     * and an arrayList of exam sessions.</p>
     */
    public static class CourseRow {

        private final String id;
        private final String code;
        private final String name;
        private final List<SessionRow> sessions;

        /**
         * This is constructor for CourseRow
         *
         * @param id
         * @param code
         * @param name
         * @param sessions
         */
        public CourseRow(String id, String code, String name, List<SessionRow> sessions) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.sessions = sessions;
        }

        /**
         * This is getter for code
         *
         * @return
         */
        public String getCode() {
            return code;
        }

        /**
         * This is getter name
         *
         * @return
         */
        public String getName() {
            return name;
        }

        /**
         * This is getter for session
         *
         * @return
         */
        public List<SessionRow> getSessions() {
            return sessions;
        }

    }

    /**
     * This class is the data container of student booking UI exam session table
     * row.
     * <p>
     * one RecordRowStudent object contains object id, course code, start time,
     * end time and exam location</p>
     */
    public static class SessionRow {

        private final String id;
        private final String code;
        private final Date start;
        private final Date end;
        private final String location;

        /**
         * This is constructor of SessionRow
         *
         * @param id
         * @param code
         * @param start
         * @param end
         * @param location
         */
        public SessionRow(String id, String code, Date start, Date end, String location) {
            this.id = id;
            this.code = code;
            this.start = start;
            this.end = end;
            this.location = location;
        }

        /**
         * This is getter for start
         *
         * @return
         */
        public Date getStart() {
            return start;
        }

        /**
         * This is getter for end
         *
         * @return
         */
        public Date getEnd() {
            return end;
        }

        /**
         * This is getter for id
         *
         * @return
         */
        public String getId() {
            return id;
        }

        /**
         * This is getter for code
         *
         * @return
         */
        public String getCode() {
            return code;
        }

        /**
         * This is getter for location
         *
         * @return
         */
        public String getLocation() {
            return location;
        }
    }

    /**
     * This is to set student info including username and password
     */
    public static class StudentRow {

        private String username;
        private String name;

        /**
         * Set username and name
         *
         * @param username a String user use to login
         * @param name a String which is name of user
         */
        public StudentRow(String username, String name) {
            this.username = username;
            this.name = name;
        }

        /**
         * actuator for username
         *
         * @return user's username
         */
        public String getUsername() {
            return username;
        }

        /**
         * actuator for name
         *
         * @return name of User
         */
        public String getName() {
            return name;
        }

    }

}
