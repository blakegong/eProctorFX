package eproctor_v1;

import com.googlecode.javacv.CanvasFrame;
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
import javax.imageio.ImageIO;

public class VideoServerInterface {

    public static String domain, userCode;
    public static String course_code, session_code;

    public static ServiceSendImage serviceSendImage;
    public static FrameGrabber grabber;

    public static class ServiceSendImage extends Service<Image> {
        
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
                    grabber = FrameGrabber.createDefault(0);
                    grabber.start();
                    IplImage img;
                    BufferedImage buf;

                    while (true) {
                        img = grabber.grab();
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

        public void send(byte[] imageBytes) throws Exception {
            VideoServerInterface.RecordObject recordObject = new VideoServerInterface.RecordObject(me, imageBytes);

            Socket socket = new Socket(ip, port);
            ObjectOutputStream sOutput = new ObjectOutputStream(socket.getOutputStream());
            sOutput.writeObject(recordObject);
            socket.close();
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

        public void setIp(String ip) {
            this.ip = ip;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class RecordObject implements Serializable {

        protected static final long serialVersionUID = 1123L;
        public String userId;
        public String course_code;
        public String session_code;
        public byte[] imageBytes;

        public RecordObject(String userId, byte[] imageBytes) {
            this.userId = userId;
            this.imageBytes = imageBytes;
        }

        public String getUserId() {
            return this.userId;
        }

        public byte[] getImageBytes() {
            return this.imageBytes;
        }
    }

    public static class RequestObject implements Serializable {

        protected static final long serialVersionUID = 1124L;
        public String proctorId;
        public String requestId;

        public RequestObject(String proctorId, String requestId) {
            this.proctorId = proctorId;
            this.requestId = requestId;
        }
    }
}
