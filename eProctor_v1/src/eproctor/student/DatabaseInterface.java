/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eproctor.student;

import com.mongodb.*;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
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

    private static DBCollection user, record, course, session, student, proctor, message;
    public static String domain, username, password, userCode;
//    private static String examCourseCode, examSessionCode, examProctor, examStudent;
    private static List<RecordRow> recordDataStudent;
    private static List<RecordRowProctor> recordDataProctor;
    private static List<CourseRow> courseData;
    protected static ServiceSendMsg serviceSendMsg;
    protected static ServiceFetchMsg serviceFetchMsg;

    /**
     * This is the Constructor of DatabaseInterface class Suppresses default
     * constructor, ensuring non-instantiation.
     */
    public DatabaseInterface() {
    }

    /**
     * This method sets courseData into appropriate format and pass it to UI and
     * its controller.
     *
     * @param controller Controller for Student form
     * @param infoData Observablelist of student information
     */
    public static void getInfoData(StudentFormController controller, ObservableList<Node> infoData) {
        for (CourseRow courseRow : courseData) {
            RecordRow recordRow = null;
            for (RecordRow tempRecord : recordDataStudent) {
                if (courseRow.code.equals(tempRecord.course.code)) {
                    recordRow = tempRecord;
                }
            }
            StudentFormController.InfoRow infoRow = controller.new InfoRow(courseRow, recordRow);
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
    public static void updateLocalRecordData(SimpleDoubleProperty progress) {
        if (progress != null) // update progress bar...
        {
            progress.set(0);
        }

        recordDataStudent = new ArrayList();
        QueryBuilder recordQb = new QueryBuilder();
        //TODO diff domain
        recordQb.put("student_code").is(userCode);
        DBCursor recordCursor = record.find(recordQb.get());

        if (progress != null) // update progress bar...
        {
            progress.set(0.2);
        }

        while (recordCursor.hasNext()) {
//            BasicDBObject queryOrOr = new BasicDBObject();
//            while (recordCursor.hasNext()) {
//                DBObject temp = recordCursor.next();
//                queryOrOr.append("code", temp.get("course_code"));
//            }
//            BasicDBObject query = new BasicDBObject("$or", queryOrOr);
//            DBCursor cur = course.findOne(query);
            
            ////
            DBObject recordObj = recordCursor.next();
            QueryBuilder courseQb = new QueryBuilder();
            courseQb.put("code").is(recordObj.get("course_code"));
            DBObject courseObj = course.findOne(courseQb.get());
            CourseRow courseRow = new CourseRow(((ObjectId) courseObj.get("_id")).toString(), (String) courseObj.get("code"), (String) courseObj.get("name"), null);

            QueryBuilder sessionQb = new QueryBuilder();
            sessionQb.put("code").is(recordObj.get("session_code"));
            DBObject sessionObj = session.findOne(sessionQb.get());
            SessionRow sessionRow = new SessionRow(((ObjectId) sessionObj.get("_id")).toString(), (String) sessionObj.get("code"), (Date) sessionObj.get("start"), (Date) sessionObj.get("end"), (String) sessionObj.get("location"));

            recordDataStudent.add(new RecordRow(((ObjectId) recordObj.get("_id")).toString(), courseRow, sessionRow, (String) recordObj.get("student_code"), (String) recordObj.get("grade"), (String) recordObj.get("remark")));

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

            recordDataProctor.add(new RecordRowProctor(((ObjectId) recordObj.get("_id")).toString(), courseRow, sessionRow, (String) recordObj.get("proctor_code")));
        }
    }

    /**
     * This method is to update local course record data to (server) database
     * Method add data onto database if there is any data on user side to be
     * updated.
     *
     * @param progress
     */
    public static void updateLocalCourseData(SimpleDoubleProperty progress) {
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
    public static void updateLocalData() {
        if (domain.equals("Student")) {
            updateLocalRecordData(null);
            updateLocalCourseData(null);
        } else {
            updateLocalRecordDataProctor();
        }
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
    public static RecordRow addBooking(CourseRow courseRow, SessionRow sessionRow) {
        BasicDBObjectBuilder document = new BasicDBObjectBuilder();
        document.add("course_code", courseRow.code)
                .add("session_code", sessionRow.getCode());
        if (domain.equals("Student")) {
            document.add("student_code", userCode).add("grade", "").add("remark", "");
        } else {
            document.add("proctor_code", userCode);
        }
        record.insert(document.get());
        updateLocalData();
        for (RecordRow recordRow : recordDataStudent) {
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
    public static void deleteBooking(String id) {
        QueryBuilder qb = new QueryBuilder();
        qb.put("_id").is(new ObjectId(id));
        record.remove(qb.get());
        updateLocalData();
    }

    /**
     * This method is to form a list of exam sessions of course
     *
     * @param list ObservableList String list of formated exam session
     * @param courseRow course info
     */
    public static void getListSessions(ObservableList<String> list, CourseRow courseRow) {
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

    /**
     * This method is to send message
     *
     * @param receiverCode id of receiver
     * @param courseCode id of course
     * @param sessionCode id of exam session
     * @param text content of the message
     * @param date time of the sending action
     * @param type different type of message (normal message,warning message)
     * @return true indicate successful sending message, else fail to send
     * message
     */
    public static boolean sendMessage(String receiverCode, String courseCode, String sessionCode, String text, Date date, String type) {
        WriteResult wr = message.insert(new BasicDBObject().append("sender_code", userCode)
                .append("receiver_code", receiverCode)
                .append("course_code", courseCode)
                .append("session_code", sessionCode)
                .append("text", text)
                .append("time", date)
                .append("type", type)
                .append("isRead", false));
        if (wr.getError() != null) {
            System.out.println(wr.getError());
            return false;
        }
        return true;
    }

    /**
     * This method is to get message from server (database)
     *
     * @param me id of myself(user_id)
     * @param course_code id of course
     * @param session_code id of exam session
     * @return string of message
     */
    public static String pullMessage(String me, String course_code, String session_code) {
        int status = 0; // exam status carried by message, 0: normal, 1: warning, 2: ending

        BasicDBObject query = new BasicDBObject("course_code", course_code)
                .append("session_code", session_code)
                .append("$or", new BasicDBObject("receiver_code", me).append("sender_code", me));

        DBCursor cur = message.find(query).sort(new BasicDBObject("time", 1));

        String msgAll = "";
        while (cur.hasNext()) {
            DBObject temp = cur.next();
            String msgTemp = "";

            if (status < (int) temp.get("status")) // get a worest status;
            {
                status = (int) temp.get("status");
            }

            msgTemp = temp.toString();

            msgAll += msgTemp + "\n";
        }

        return msgAll + "#" + status;
    }

    /**
     * This class will handle the fetching message services extending from
     * Service by using polling message from server/database.
     */
    public static class ServiceFetchMsg extends Service<String> {
        private String me;
        private String course_code;
        private String session_code;
        
        protected ServiceFetchMsg(String me, String course_code, String session_code) {
            this.setMe(me);
            this.setCourse_code(course_code);
            this.setSession_code(session_code);
        }

        @Override
        protected Task<String> createTask() {
            return new Task<String>() {
                @Override
                protected String call() throws Exception {
                    int test = 0;
                    int status = 0;
                    while (!this.isCancelled() && status != 3) {
//                        String msg = pullMessage(me, course_code, session_code);
//                        this.updateMessage(msg);
                        this.updateMessage("" + test++);
                        
//                        status = Integer.parseInt(msg.split("#")[0]);
                        status = test % 3;
                        
//                        this.updateValue(statusIntToStatusString());
                        this.updateTitle("-fx-background-color: " + statusIntToStatusString(status));

                        Thread.sleep(1000);
                    }
                    
                    if (status == 3) {
                        return "ending";
                    }
                    
                    return "ServiceFetchMsg Ended...";
                }
            };
        }

        public String statusIntToStatusString(int status) {
            if (status == 0) {
                return "green";
            } else if (status == 1) {
                return "yellow";
            } else if (status == 2) {
                return "red";
            } else {
                return "black";
            }
        }

        /**
         * This is to set me
         *
         * @param me
         */
        public void setMe(String me) {
            this.me = me;
        }

        /**
         * This is to set course _code
         *
         * @param course_code
         */
        public void setCourse_code(String course_code) {
            this.course_code = course_code;
        }

        /**
         * This is to set session_code
         *
         * @param session_code
         */
        public void setSession_code(String session_code) {
            this.session_code = session_code;
        }
    }

    /**
     * This class will handle the sending message services extending from
     * Service by using push message to server/database.
     */
    public static class ServiceSendMsg extends Service<Void> {
        private String me;
        private String course_code;
        private String session_code;
        private String proctor_code;
        private String text;
        private Date time;
        private String type;

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    for (int i = 0; i < 10; i++) {
                        Thread.sleep(100);
                    }
//                    boolean result = sendMessage(me, course_code, session_code, proctor_code, text, time, type);                 
                    return null;
                }
            };
        }

        /**
         * This is to set me
         *
         * @param me
         */
        public void setMe(String me) {
            this.me = me;
        }

        /**
         * This is to set course_code
         *
         * @param course_code
         */
        public void setCourse_code(String course_code) {
            this.course_code = course_code;
        }

        /**
         * This is to set session_code
         *
         * @param session_code
         */
        public void setSession_code(String session_code) {
            this.session_code = session_code;
        }

        /**
         * This is to set proctor_code
         *
         * @param proctor_code
         */
        public void setProctor_code(String proctor_code) {
            this.proctor_code = proctor_code;
        }

        /**
         * This is to set text
         *
         * @param text
         */
        public void setText(String text) {
            this.text = text;
        }

        /**
         * This is to set time
         *
         * @param time
         */
        public void setTime(Date time) {
            this.time = time;
        }

        /**
         * This is to set type
         *
         * @param type
         */
        public void setType(String type) {
            this.type = type;
        }
    }

    /**
     * This class is the data container of student review UI table row.
     * <p>
     * one RecordRow object contains record id, course code, session code,
     * student id, student grade and exam remark.</p>
     */
    public static class RecordRow {

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
        public RecordRow(String id, CourseRow course, SessionRow session, String student_code, String grade, String remark) {
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

        public CourseRow getCourse() {
            return course;
        }

    }

    /**
     * This class is the data container of proctor review UI table row.
     * <p>
     * one RecordRow object contains record id, course code, session code and
     * proctor id.</p>
     */
    public static class RecordRowProctor {

        private final String id;
        private final CourseRow course;
        private final SessionRow session;
        private final String proctor_code;

        /**
         * This is constructor for RecordRowProctor
         *
         * @param id
         * @param course
         * @param session
         * @param proctor_code
         */
        public RecordRowProctor(String id, CourseRow course, SessionRow session, String proctor_code) {
            this.id = id;
            this.course = course;
            this.session = session;
            this.proctor_code = proctor_code;
        }

        /**
         * This is getter of sessionRow
         *
         * @return
         */
        public SessionRow getSession() {
            return session;
        }
    }

    /**
     * This class is the data container of student booking UI course table row.
     * <p>
     * one RecordRow object contains object id, course code, course name and an
     * arrayList of exam sessions.</p>
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
     * one RecordRow object contains object id, course code, start time, end
     * time and exam location</p>
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

}
