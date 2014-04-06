package eproctor_v1;

import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_core.cvFlip;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * This class is a video server interface class runs on client side. It has
 * communication with video server, which will receive student client side
 * recording object and then dispatch them to proctor client side during exam
 * period.
 *
 * @author Chen Liyang
 * @author Gong Yue
 */
public class VideoServerInterface {

    public static String domain, userCode;
    public static String course_code, session_code;

    public static ServiceSendImage serviceSendImage;
    

    /**
     * This is a static class extends from Service. One object of this class
     * will handle sending Image service assisting VideoSeverInterface as a
     * subroutine.
     */
    public static class ServiceSendImage extends Service<Image> {

        private FrameGrabber grabber;
        private String me;
        private String ip;
        private int port;
        private String course_code;
        private String session_code;

        @Override
        protected Task<Image> createTask() {
            return new Task<Image>() {
                @Override
                protected Image call() throws Exception {
                    if (getGrabber() == null) {
                        super.failed();
                        return null;
                    }
                    
                    grabber.start();
                    IplImage img;
                    BufferedImage buf;

                    while (true) {
                        img = getGrabber().grab();
                        cvFlip(img, img, 1);// l-r = 90_degrees_steps_anti_clockwise
                        buf = img.getBufferedImage();
                        this.updateValue(SwingFXUtils.toFXImage(buf, null));

//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        ImageIO.write(buf, "jpg", baos);
//                        baos.flush();
//                        byte[] imageBytes = baos.toByteArray();
//                        baos.close();
//                        send(imageBytes);
                    }
                }
            };
        }

        /**
         * This method sends an image data in the format of array of byte. The
         * image will be constructed to an recordObject and sent through socket,
         * which is set up between client and video server.
         *
         * @param imageBytes image object in array of byte format
         * @throws Exception socket may throw IOException
         */
        public void send(byte[] imageBytes) throws Exception {
            VideoServerInterface.RecordObject recordObject = new VideoServerInterface.RecordObject(me, imageBytes);

            Socket socket = new Socket(ip, port);
            ObjectOutputStream sOutput = new ObjectOutputStream(socket.getOutputStream());
            sOutput.writeObject(recordObject);
            socket.close();
        }

        /**
         *
         * @param me
         */
        public void setMe(String me) {
            this.me = me;
        }

        /**
         *
         * @param course_code
         */
        public void setCourse_code(String course_code) {
            this.course_code = course_code;
        }

        /**
         *
         * @param session_code
         */
        public void setSession_code(String session_code) {
            this.session_code = session_code;
        }

        /**
         *
         * @param ip
         */
        public void setIp(String ip) {
            this.ip = ip;
        }

        /**
         *
         * @param port
         */
        public void setPort(int port) {
            this.port = port;
        }

        public void setGrabber(FrameGrabber grabber) {
            this.grabber = grabber;
        }

        public FrameGrabber getGrabber() {
            return grabber;
        }
    }

    /**
     * This is the basic serializable record data container class. One
     * RecordObject object will be sent to video server through socket. Proctor
     * client side will receive RecordObject objects dispatched from video
     * server.
     */
    public static class RecordObject implements Serializable {

        /**
         *
         */
        protected static final long serialVersionUID = 1123L;

        /**
         *
         */
        public String userId;

        /**
         *
         */
        public String course_code;

        /**
         *
         */
        public String session_code;

        /**
         *
         */
        public byte[] imageBytes;

        /**
         *
         * @param userId
         * @param imageBytes
         */
        public RecordObject(String userId, byte[] imageBytes) {
            this.userId = userId;
            this.imageBytes = imageBytes;
        }

        /**
         *
         * @return
         */
        public String getUserId() {
            return this.userId;
        }

        /**
         *
         * @return
         */
        public byte[] getImageBytes() {
            return this.imageBytes;
        }
    }

    /**
     * This is the basic serializable request data container class. One
     * RequestObject object will be sent to video server through socket from
     * proctor client side. Server analyze RequestObject object and dispatch
     * RecordObject objects accordingly.
     */
    public static class RequestObject implements Serializable {

        protected static final long serialVersionUID = 1124L;
        public String proctorId;
        public String requestId;

        /**
         * Basic constructor of RequestObject.
         *
         * @param proctorId
         * @param requestId
         */
        public RequestObject(String proctorId, String requestId) {
            this.proctorId = proctorId;
            this.requestId = requestId;
        }
    }
}
