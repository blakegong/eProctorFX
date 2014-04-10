package eproctor.commons;

import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;
import java.util.Date;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author CLY
 */
public class MessageSend extends Service<Void> {

    private String me;
    private String course_code;
    private String session_code;
    private String receiver_code;
    private String text;
    private Date time;
    private int type;

    public MessageSend(String me, String receiver_code, String course_code, String session_code, String text, Date time, int type) {
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
                boolean result = sendMessage(me, receiver_code, course_code, session_code, text, time, type);
                return null;
            }
        };
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
        WriteResult wr = DatabaseInterface.message.insert(new BasicDBObject().append("sender_code", sender_code)
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

}
