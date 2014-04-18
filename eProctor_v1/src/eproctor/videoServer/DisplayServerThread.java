package eproctor.videoServer;

import java.io.ObjectInputStream;
import java.util.*;
import java.io.*;
import java.net.*;

/**
 *DisplayServerThread create the thread to display the image 
 * <p> thread are woke period to display the image as form of video 
 * @author Chen Liyang
 * @author Gong Yue
 * @author Chen Desheng
 * @author Peng Lunan
 */
public class DisplayServerThread implements Runnable {

    private HashMap<String, RecordObject> receivedList;

    private int port;
    private static ServerSocket serverSocket;
    private Socket socket;
    private ObjectInputStream sInput;

    /**
     *Display the image by create thread
     * <p> pass in port and recivedList contains the image, and display the video
     * @param port 
     * @param receivedList a hashmap which maps string to RecordObject
     */
    public DisplayServerThread(int port, HashMap<String, RecordObject> receivedList) {
        this.port = port;
        this.receivedList = receivedList;
        new Thread(this, "displayServer").start();
    }

    @Override
     /**
     *Setup the connection between Student and Proctor
     * <p> so that the image of Student side are sent to Proctor side
     * 
     */
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("DisplayServerThread_ip: " + serverSocket.getInetAddress());
            System.out.println("Start dispatching image...");

            while (true) {
                socket = serverSocket.accept();
                System.out.println("A connection setup.");
                sInput = new ObjectInputStream(socket.getInputStream());
                RequestObject requestObject = (RequestObject) sInput.readObject();
                String requestId = requestObject.requestId;
                System.out.println("DisplayServerThread: requestId; " + requestId);
//                SendVideoThread t = new SendVideoThread(socket);
                RecordObject recordObject = receivedList.get(requestId);
                if (recordObject != null) {
                    recordObject = recordObject;
//                    t.start();
                } else {
                    recordObject = new RecordObject(null, null);
//                    t.start();
                }

                ObjectOutputStream sOutput = new ObjectOutputStream(socket.getOutputStream());
                sOutput.writeObject(recordObject);
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
/**
 *Proctor request the video
 * <p> VideoRequest pass in the proctorId so that the video will be sent back to Proctor
 * 
 */
class VideoRequest {

    public String proctorId;
    public String requestId;
    public SendVideoThread dt;

    public VideoRequest(String proctorId, String requestId, SendVideoThread t) {
        this.proctorId = proctorId;
        this.requestId = requestId;
        this.dt = t;
    }

}
/**
 *Once the video images are captured, they will be send to Proctor side
 * <p> sendVideoThread inherits Thread so that the thread runs concurrently with the main application
 */
class SendVideoThread extends Thread {

    public RecordObject recordObject;
    public Socket socket;
    public ObjectOutputStream sOutput;

    public SendVideoThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            sOutput = new ObjectOutputStream(socket.getOutputStream());
            sOutput.writeObject(recordObject);
            socket.close();
            this.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
