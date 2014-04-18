package eproctor.commons;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import javafx.concurrent.Service;
import javafx.concurrent.Task;


/**
 * This class will handle the fetching message services extending from Service
 * by using polling message from server/database.
 * @author chenliyang
 * @author wangdingcheng
 */
public class MessagePull extends Service<String> {

    private String myUsername;
    private String course_code;
    private String session_code;

    /**
     *
     * @param myUsername
     * @param course_code
     * @param session_code
     */
    public MessagePull(String myUsername, String course_code, String session_code) {
        this.myUsername = myUsername;
        this.course_code = course_code;
        this.session_code = session_code;
    }

    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                int test = 0;
                int status = 0;
                System.out.println("ServiceFetchMsg: me: " + myUsername + ", course_code: " + course_code + ", session_code: " + session_code);
                while (status != 2) {
                    String msg = pullMessage(myUsername, course_code, session_code);
                    this.updateMessage(msg);

                    status = Integer.parseInt(msg.split("#")[1]);
                    this.updateTitle("-fx-background-color: " + statusIntToStatusString(status));
//                    Thread.sleep(1000);
                }

                if (status == 2) {
                    return "ending";
                }
                return "ServiceFetchMsg Ended Unexpected.";
            }
        };
    }

    /**
     *
     * @param status
     * @return
     */
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
     * @param myUsername
     * @param me
     */
    public void setMyUsername(String myUsername) {
        this.myUsername = myUsername;
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

    /**
     * This method is to get message from server (database)
     *
     * @param username id of myself(user_id)
     * @param course_code id of course
     * @param session_code id of exam session
     * @return string of message
     */
    public static String pullMessage(String username, String course_code, String session_code) {
        BasicDBObject orArray[] = new BasicDBObject[2];
        orArray[0] = new BasicDBObject("receiver_code", username);
        orArray[1] = new BasicDBObject("sender_code", username);
        BasicDBObject query = new BasicDBObject("course_code", course_code)
                .append("session_code", session_code)
                .append("$or", orArray);

//        System.out.println("pullMessage: query: " + query);
        DBCursor cur = DatabaseInterface.message.find(query).sort(new BasicDBObject("time", 1));

        String msgAll = "";
        int status = 0; // exam status carried by message, 0: normal, 1: warning, 2: ending // ending = expelled
        while (cur.hasNext()) {
            DBObject temp = cur.next();
//            System.out.println("pullMessage: temp: \n" + temp);

            String msgTemp = "";

            if (status < (int) temp.get("type")) // get a worest status;
            {
                status = (int) temp.get("type");
            }
//            System.out.println("pullMessage: status: " + status);

            msgTemp = "sender_code: " + DatabaseInterface.getName((String) temp.get("sender_code"))
                    + "\nreceiver_code: " + DatabaseInterface.getName((String) temp.get("receiver_code"))
                    + "\ntime; " + temp.get("time")
                    + "\n\t\"" + temp.get("text") + "\"";

            if ((int) temp.get("type") == 1) {
                msgTemp += "\n(it is a warning!)";
            }
            if ((int) temp.get("type") == 2) {
                msgTemp += "\n(you are expelled!)";
//                DatabaseInterface.record.update(new BasicDBObject("student_code", DatabaseInterface.getUser_code(username)), new BasicDBObject("$set", new BasicDBObject("remark", "expelled")));
            }

            msgAll += msgTemp + "\n\n\n";
        }

//        System.out.println("pullMessage: return: \n" + msgAll + "#" + status);
        return msgAll + "#" + status;
    }
}
