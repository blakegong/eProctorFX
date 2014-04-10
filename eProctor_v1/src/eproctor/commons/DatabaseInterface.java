/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eproctor.commons;

import com.mongodb.*;
import eproctor.student.StudentFormController;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
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

    private static DBCollection user, record, course, session, student, proctor, message;
    public static String domain, username, password, userCode;
//    private static String examCourseCode, examSessionCode, examProctor, examStudent;
    private static List<RecordRowStudent> recordDataStudent;
    private static List<RecordRowProctor> recordDataProctor;
    private static List<CourseRow> courseData;
    public static ServiceSendMsg serviceSendMsg;
    public static ServiceFetchMsg serviceFetchMsg;

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
        if (domain.equals("Student")) {
            updateLocalRecordDataStudent(null);
            updateLocalCourseDataStudent(null);
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

    /**
     * This method is to send message
     *
     * @param sender_code id of sender
     * @param receiver_code id of receiver
     * @param courseCode id of course
     * @param sessionCode id of exam session
     * @param text content of the message
     * @param date time of the sending action
     * @param type different type of message (normal message,warning message)
     * @return true indicate successful sending message, else fail to send
     * message
     */
    public static boolean sendMessage(String sender_code, String receiver_code, String courseCode, String sessionCode, String text, Date date, int type) {
        WriteResult wr = message.insert(new BasicDBObject().append("sender_code", sender_code)
                .append("receiver_code", receiver_code)
                .append("course_code", courseCode)
                .append("session_code", sessionCode)
                .append("text", text)
                .append("time", date)
                .append("type", type)
                .append("isRead", false));
        if (wr.getError() != null) {
            System.out.println("sendMessage failed. \n" + wr.getError());
            return false;
        }
        System.out.println("sendMessage succeeded.");
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
        BasicDBObject orArray[] = new BasicDBObject[2];
        orArray[0] = new BasicDBObject("receiver_code", me);
        orArray[1] = new BasicDBObject("sender_code", me);
        BasicDBObject query = new BasicDBObject("course_code", course_code)
                                        .append("session_code", session_code)
                                        .append("$or", orArray);
        
//        System.out.println("pullMessage: query: " + query);
        DBCursor cur = message.find(query).sort(new BasicDBObject("time", 1));

        String msgAll = "";
        int status = 0; // exam status carried by message, 0: normal, 1: warning, 2: ending // ending = expelled
        while (cur.hasNext()) {
            DBObject temp = cur.next();
//            System.out.println("pullMessage: temp: \n" + temp);

            String msgTemp = "";

            if (status < (int) temp.get("type")) // get a worest status;
                status = (int) temp.get("type");
//            System.out.println("pullMessage: status: " + status); 

            msgTemp = "sender_code: " + getName((String)temp.get("sender_code"))
                    + "\nreceiver_code: " + getName((String)temp.get("receiver_code"))
                    + "\ntime; " + temp.get("time")
                    + "\n\t\"" + temp.get("text") + "\"";
            
            if ((int)temp.get("type") == 1)
                msgTemp += "\n(it is a warning!)";
            if ((int)temp.get("type") == 2)
                msgTemp += "\n(you are expelled!)";

            msgAll += msgTemp + "\n\n\n";
        }
        
//        System.out.println("pullMessage: return: \n" + msgAll + "#" + status);
        return msgAll + "#" + status;
    }
    
    public static boolean isProctor() {
        return true;
    }
    
    public static String getName(String user_code) {
//        System.out.println("getName: here");
//        System.out.println("user_code: " + user_code);
        DBObject result = student.findOne(new BasicDBObject("user_code", user_code));
//        System.out.println("result: " + result);
        if (result == null)
            result = proctor.findOne(new BasicDBObject("user_code", user_code));
//        System.out.println("getName: name: " + (String)result.get("name"));
        return (String)result.get("name");
        
        
//        System.out.println("getName: here");
//        DBObject result = proctor.findOne(new BasicDBObject("user_code", "NTUU1220495H"));
//        System.out.println("result: " + result);
//        if (result == null)
//            result = student.findOne(new BasicDBObject("user_code", "NTUU1220495H"));
//        System.out.println("getName: name: " + (String)result.get("name"));
//        return (String)result.get("name");
    }

    /**
     * This class will handle the fetching message services extending from
     * Service by using polling message from server/database.
     */
    public static class ServiceFetchMsg extends Service<String> {

        private String me;
        private String course_code;
        private String session_code;

        public ServiceFetchMsg(String me, String course_code, String session_code) {
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
                    System.out.println("ServiceFetchMsg: me: " + me + ", course_code: " + course_code + ", session_code: " + session_code);
                    while (status != 2) {
                        String msg = pullMessage(me, course_code, session_code);
                        this.updateMessage(msg);
//                        this.updateMessage(test++ + " test wrapping test wrapping test wrapping test wrapping test wrapping\n test scrolling\n test scrolling\n test scrolling\n test scrolling\n test scrolling\n test scrolling\n test scrolling\n");

                        status = Integer.parseInt(msg.split("#")[1]);
                        System.out.println("ServiceFetchMsg: status: " + status);
//                        status = test % 2;
//                        if (test == 5)
//                            status = 2;

                        this.updateTitle("-fx-background-color: " + statusIntToStatusString(status));

                        Thread.sleep(3000);
                    }

                    if (status == 2) {
                        return "ending";
                    }

                    return "ServiceFetchMsg Ended Unexpected.";
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
        private String receiver_code;
        private String text;
        private Date time;
        private int type;
        
        public ServiceSendMsg(String me, String receiver_code, String course_code, String session_code, String text, Date time, int type) {
            this.me = me;
            this.course_code = course_code;
            this.session_code = session_code;
            this.receiver_code = receiver_code;
            this.text = text;
            this.time = time;
            this.type = type;
        }

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
//                    for (int i = 0; i < 10; i++) {
//                        Thread.sleep(100);
//                    }
                    boolean result = sendMessage(me, course_code, session_code, receiver_code, text, time, type);                 
                    return null;
                }
            };
        }
    }

    /**
     * This class is the data container of student review UI table row.
     * <p>
 one RecordRowStudent object contains record id, course code, session code,
 student id, student grade and exam remark.</p>
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

        public CourseRow getCourse() {
            return course;
        }

    }

    /**
     * This class is the data container of proctor review UI table row.
     * <p>
 one RecordRowStudent object contains record id, course code, session code and
 proctor id.</p>
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
 one RecordRowStudent object contains object id, course code, course name and an
 arrayList of exam sessions.</p>
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
 one RecordRowStudent object contains object id, course code, start time, end
 time and exam location</p>
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
