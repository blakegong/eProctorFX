/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eproctor_v1;

import com.mongodb.*;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Node;
import org.bson.types.ObjectId;

public class DatabaseInterface {

    private static DBCollection user, record, course, session, student, proctor, message;
    public static String domain, username, password, userCode;
//    private static String examCourseCode, examSessionCode, examProctor, examStudent;
    private static List<RecordRow> recordDataStudent;
    private static List<RecordRowProctor> recordDataProctor;
    private static List<CourseRow> courseData;
    
    protected static ServiceSendMsg serviceSendMsg;
    protected static ServiceFetchMsg serviceFetchMsg;

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

    public DatabaseInterface() {

    }

    public static void connectEProctorServer() throws UnknownHostException {
        MongoClientURI uri = new MongoClientURI("mongodb://admin:admin@oceanic.mongohq.com:10014/eProctor");
        MongoClient mongoClient = new MongoClient(uri);
        DB db = mongoClient.getDB("eProctor");
        record = db.getCollection("Record");
        message = db.getCollection("Message");
    }

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

    public static void updateLocalRecordData(SimpleDoubleProperty progress) {
        if (progress != null) // update progress bar...
            progress.set(0);
        
        recordDataStudent = new ArrayList();
        QueryBuilder recordQb = new QueryBuilder();
        //TODO diff domain
        recordQb.put("student_code").is(userCode);             
        DBCursor recordCursor = record.find(recordQb.get());
        
        if (progress != null) // update progress bar...
            progress.set(0.2);
        
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

            recordDataStudent.add(new RecordRow(((ObjectId) recordObj.get("_id")).toString(), courseRow, sessionRow, (String) recordObj.get("student_code"), (String) recordObj.get("grade"), (String) recordObj.get("remark")));
            
            if (progress != null) // update progress bar...
                progress.set(progress.add(0.8/recordCursor.size()).get());
        }
        
        if (progress != null) // update progress bar...
            progress.set(1);
    }
    
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

    public static void updateLocalCourseData(SimpleDoubleProperty progress) {
        if (progress != null) // update progress bar...
            progress.set(0);
        
        courseData = new ArrayList();
        QueryBuilder qbStudent = new QueryBuilder();
        qbStudent.put("user_code").is(userCode);
        DBObject objStudent = student.findOne(qbStudent.get());
        BasicDBList listCourses = (BasicDBList) objStudent.get("enrolledCourses");
        
        if (progress != null) // update progress bar...
            progress.set(0.2);
        
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
                progress.set(progress.add(0.8/listCourses.size()).get());
            System.out.println("progress: " + progress.get());
        }
        
        if (progress != null) // update progress bar...
            progress.set(1);
    }

    public static void updateLocalData() {
        if (domain.equals("Student")) {
            updateLocalRecordData(null);
            updateLocalCourseData(null);
        } else {
            updateLocalRecordDataProctor();
        }
    }

    public static String getTextAreaRecentMessages() {
        //return Messager.pollMsg(Main.user_id, "Student");
        return "Not done yet";
    }

    public static RecordRow addBooking(CourseRow courseRow, SessionRow sessionRow) {
        BasicDBObjectBuilder document = new BasicDBObjectBuilder();
        document.add("course_code", courseRow.code)
                .add("session_code", sessionRow.getCode());
        if (domain.equals("Student"))
            document.add("student_code", userCode).add("grade", "").add("remark", "");
        else
            document.add("proctor_code", userCode);
        record.insert(document.get());
        updateLocalData();
        for (RecordRow recordRow: recordDataStudent) {
            if (recordRow.course.code.equals(courseRow.code) && recordRow.session.getCode().equals(sessionRow.getCode()) && recordRow.student_code.equals(userCode))
                return recordRow;
        }
        return null;
    }
    
    public static void deleteBooking(String id) {
        QueryBuilder qb = new QueryBuilder();
        qb.put("_id").is(new ObjectId(id));
        record.remove(qb.get());
        updateLocalData();
    }

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
    
    public static String pullMessage(String me, String course_code, String session_code) {
        BasicDBObject query = new BasicDBObject("course_code", course_code)
                .append("session_code", session_code)
                .append("$or", new BasicDBObject("receiver_code", me).append("sender_code", me));

        DBCursor cur = message.find(query).sort(new BasicDBObject("time", 1));

        String msgAll = "";
        while (cur.hasNext()) {
            DBObject temp = cur.next();
            String msgTemp = "";

            msgTemp = temp.toString();

            msgAll += msgTemp + "\n";
        }

        return msgAll;
    }

    public static class ServiceFetchMsg extends Service<Void> {

        private String me;
        private String course_code;
        private String session_code;

        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    int test = 0;
                    while (!this.isCancelled()) {
//                        updateMessage(pullMessage(me, course_code, session_code));
                        updateMessage("" + test++);
                        Thread.sleep(1000);
                    }

                    return null;
                }
            };
        }

        public void setMe(String me) {
            this.me = me;
        }

        public void setCourse_code(String course_code) {
            this.course_code = course_code;
        }

        public void setSession_code(String session_code) {
            this.session_code = session_code;
        }
    }

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

        public void setMe(String me) {
            this.me = me;
        }

        public void setCourse_code(String course_code) {
            this.course_code = course_code;
        }

        public void setSession_code(String session_code) {
            this.session_code = session_code;
        }

        public void setProctor_code(String proctor_code) {
            this.proctor_code = proctor_code;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setTime(Date time) {
            this.time = time;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
    
    public static class RecordRow {

        private final String id;
        private final CourseRow course;
        private final SessionRow session;
        private final String student_code;
        private final String grade;
        private final String remark;

        public RecordRow(String id, CourseRow course, SessionRow session, String student_code, String grade, String remark) {
            this.id = id;
            this.course = course;
            this.session = session;
            this.student_code = student_code;
            this.grade = grade;
            this.remark = remark;
        }

        public SessionRow getSession() {
            return session;
        }

        public String getGrade() {
            return grade;
        }

        public String getRemark() {
            return remark;
        }

        public String getId() {
            return id;
        }
        
    }
    
    public static class RecordRowProctor {

        private final String id;
        private final CourseRow course;
        private final SessionRow session;
        private final String proctor_code;

        public RecordRowProctor(String id, CourseRow course, SessionRow session, String proctor_code) {
            this.id = id;
            this.course = course;
            this.session = session;
            this.proctor_code = proctor_code;
        }

        public SessionRow getSession() {
            return session;
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

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public List<SessionRow> getSessions() {
            return sessions;
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

        public Date getStart() {
            return start;
        }

        public Date getEnd() {
            return end;
        }

        public String getId() {
            return id;
        }

        public String getCode() {
            return code;
        }

        public String getLocation() {
            return location;
        }
    }

}
