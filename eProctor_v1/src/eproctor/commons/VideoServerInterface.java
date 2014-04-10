package eproctor.commons;

import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_core.cvFlip;
import eproctor.videoServer.RecordObject;
import eproctor.videoServer.RequestObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;

/**
 * This class is a video server interface class runs on client side. It has
 * communication with video server, which will receive student client side
 * recording object and then dispatch them to proctor client side during exam
 * period.
 *
 * @author Chen Liyang
 * @author Gong Yue
 */
public class VideoServerInterface extends Service<Image> {

    public static ServiceSendImage serviceSendImage;

    /**
     * This is a static class extends from Service. One object of this class
     * will handle sending Image service assisting VideoSeverInterface as a
     * subroutine.
     */
    public static class ServiceSendImage extends Service<Image> {

        private FrameGrabber grabber;
        private String username;
        private String ip;
        private int port;
        private String course_code;
        private String session_code;
        private boolean isLocal;

        public ServiceSendImage(String username, String ip, int port, String course_code, String session_code) {
            this.username = username;
            this.ip = ip;
            this.port = port;
            this.course_code = course_code;
            this.session_code = session_code;
            if (username == null || ip == null || port == 0 || course_code == null || session_code == null) {
                this.isLocal = true;
            }
        }

        @Override
        protected Task<Image> createTask() {
            return new Task<Image>() {
                @Override
                protected Image call() throws Exception {
                    initGrabber();

                    if (grabber == null) {
                        super.failed();
                        System.out.println("ServiceSendImage: super.failed()");
                        return null;
                    }

                    grabber.start();
                    IplImage img;
                    BufferedImage buf;

                    System.out.println("video: islocal: " + isLocal);
                    int c = 0;
                    while (true) {
//                        System.out.println("video: c: " + c++);

                        img = grabber.grab();
                        cvFlip(img, img, 1);// l-r = 90_degrees_steps_anti_clockwise
                        buf = img.getBufferedImage();

                        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                        BufferedImage bufScreen = new Robot().createScreenCapture(screenRect);
                        BufferedImage combined = joinBufferedImage(buf, bufScreen);

                        this.updateValue(SwingFXUtils.toFXImage(buf, null));

                        if (isLocal) {
                            continue;
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream(); // use baos.reset() ?
                        ImageIO.write(combined, "jpg", baos);
                        baos.flush();
                        byte[] imageBytes = baos.toByteArray();
                        baos.close();
                        send(imageBytes);
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
            RecordObject recordObject = new RecordObject(username, imageBytes);
            System.out.println("ServiceSendImage: username; " + username);
            Socket socket = new Socket(ip, port);
            ObjectOutputStream sOutput = new ObjectOutputStream(socket.getOutputStream());
            sOutput.writeObject(recordObject);
            socket.close();
        }

        public static BufferedImage joinBufferedImage(BufferedImage img1, BufferedImage img2) {

            int type = img2.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : img2.getType();
            BufferedImage resizedImage = new BufferedImage(img1.getWidth(), img1.getHeight(), type);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(img2, 0, 0, img1.getWidth(), img1.getHeight(), null);
            g.dispose();

            int wid = img1.getWidth();
            int height = img1.getHeight() + img2.getHeight();

            //create a new buffer and draw two image into the new image
            BufferedImage newImage = new BufferedImage(wid, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = newImage.createGraphics();
            Color oldColor = g2.getColor();
            //fill background
            g2.setPaint(Color.WHITE);
            g2.fillRect(0, 0, wid, height);
            //draw image
            g2.setColor(oldColor);
            g2.drawImage(img1, null, 0, 0);
            g2.drawImage(img2, null, 0, img1.getHeight());
            g2.dispose();
            return newImage;
        }

        public void initGrabber() {
            try {
                grabber = FrameGrabber.createDefault(0);
            } catch (FrameGrabber.Exception ex) {
                ex.printStackTrace();
            }
        }

        public FrameGrabber getGrabber() {
            return grabber;
        }
    }

    private String username;
    private String domain;
    private String wanted_code;
    private String ip;
    private int port;
    private String course_code;
    private String session_code;
    public boolean isLocal;

    public VideoServerInterface(String username, String wanted_code, String ip, int port, String course_code, String session_code) {
        this.username = username;
        this.wanted_code = wanted_code;
        this.ip = ip;
        this.port = port;
        this.course_code = course_code;
        this.session_code = session_code;

        if (username == null || ip == null || port == 0 || course_code == null || session_code == null) {
            this.isLocal = true;
        }
    }

    @Override
    protected Task<Image> createTask() {
        return new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                System.out.println("VideoServerInterface: isLocal: " + isLocal);
                int c = 0;
                while (true) {
                    if (isLocal) {
                        continue;
                    }
//                    System.out.println("VideoServerInterface: c: " + c++);
                    RequestObject requestObject = new RequestObject(username, wanted_code);

                    System.out.println("VideoServerInterfaceRequest: wanted_code; " + wanted_code);

//                    System.out.println("VideoServerInterface: c: " + c++);
                    Socket socket = new Socket("localhost", port);
//                    System.out.println("VideoServerInterface: c: " + c++);
                    ObjectOutputStream sOutput = new ObjectOutputStream(socket.getOutputStream());
//                    System.out.println("VideoServerInterface: c: " + c++);
                    sOutput.writeObject(requestObject);
//                    System.out.println("VideoServerInterface: c: " + c++);
//                    System.out.println("socket.isConnected() == " + socket.isConnected());
//                    System.out.println("socket.isClosed() == " + socket.isClosed());

                    ObjectInputStream sInput = new ObjectInputStream(socket.getInputStream());
//                    System.out.println("VideoServerInterface: c: " + c++);
                    RecordObject recordObject = (RecordObject) sInput.readObject();
//                    System.out.println("VideoServerInterface: c: " + c++);
                    socket.close();
                    if (recordObject.getUserId() == null || recordObject.getCameraBytes() == null) {
                        System.out.println("received an empty recordObject");
                        continue;
                    }
//                    System.out.println("VideoServerInterface: c: " + c++);
                    byte[] imageBytes = recordObject.getCameraBytes();
//                    System.out.println("VideoServerInterface: c: " + c++);
                    BufferedImage buf = ImageIO.read(new ByteArrayInputStream(imageBytes));
//                    System.out.println("VideoServerInterface: c: " + c++);

                    this.updateValue(SwingFXUtils.toFXImage(buf, null));
                }
            }
        };
    }
}
