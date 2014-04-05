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
import java.util.Date;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javax.imageio.ImageIO;

public class VideoServerInterface {
    public static String domain, userCode;
    public static String course_code, session_code;
    
    public static class ServiceSendImage extends Service<Void>{
        private String me;
        private String ip;
        private int port;
        private String course_code;
        private String session_code;
        
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    for (int i = 0; i < 10; i++)
                        Thread.sleep(100);
                    
                    
                    FrameGrabber grabber = null;
                    try {
                        grabber = FrameGrabber.createDefault(0);
                        grabber.start();
                        IplImage img;
                        BufferedImage buf;
                        while (!this.isCancelled()) {
                            img = grabber.grab();
                            buf = img.getBufferedImage();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(buf, "jpg", baos);
                            baos.flush();
                            byte[] imageBytes = baos.toByteArray();
                            baos.close();

                            if (img != null) {
                                cvFlip(img, img, 1);// l-r = 90_degrees_steps_anti_clockwise
        //                        videoBox.setIcon(new ImageIcon(img.getBufferedImage()));
                                canvas.showImage(img);
                                send(imageBytes);
                            }
                        }
                    } catch (Exception e) {
                    }
                    
                    
                    return null;
                }
            };
        }
        
        public void send(byte[] imageBytes) throws Exception {
            VideoServerInterface.RecordObject recordObject = new VideoServerInterface.RecordObject(me, imageBytes);
            
            Socket socket = new Socket("localhost", port);
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
    
    
    public class GrabberShow implements Runnable {
        IplImage image;
        public CanvasFrame canvas = new CanvasFrame("Web Cam");
        public boolean shouldEnd = false;
        private ObjectOutputStream sOutput;
        private final int port;
        private final String user_code;
        private Socket socket;

        public GrabberShow(int port, String user_code) {
            this.port = port;
            this.user_code = user_code;
        }

        public void run() {
            FrameGrabber grabber = null;
            try {
                grabber = FrameGrabber.createDefault(0);
                grabber.start();
                IplImage img;
                BufferedImage buf;
                while (!shouldEnd) {
                    img = grabber.grab();
                    buf = img.getBufferedImage();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(buf, "jpg", baos);
                    baos.flush();
                    byte[] imageBytes = baos.toByteArray();
                    baos.close();

                    if (img != null) {
                        cvFlip(img, img, 1);// l-r = 90_degrees_steps_anti_clockwise
//                        videoBox.setIcon(new ImageIcon(img.getBufferedImage()));
                        canvas.showImage(img);
                        send(imageBytes);
                    }
                }
            } catch (Exception e) {
            }
        }

        public void send(byte[] imageBytes) throws Exception {
            socket = new Socket("localhost", port);
            sOutput = new ObjectOutputStream(socket.getOutputStream());
            RecordObject recordObject = new RecordObject(this.user_code, imageBytes);
            sOutput.writeObject(recordObject);
            socket.close();
        }
    } 
        
    public static class RecordObject implements Serializable {
        protected static final long serialVersionUID = 1123L;
        public String userId;
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
