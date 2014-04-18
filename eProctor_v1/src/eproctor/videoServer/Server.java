package eproctor.videoServer;

import java.io.*;
import java.util.HashMap;

/**
 *Server class Construct the server object
 * @author Chen Liyang
 */
public class Server {
    private static int port1 = 6001;
    private static int port2 = 6002;

    public static HashMap<String, RecordObject> receivedList;

    /**
     *Main function create VideoServerThread and DisplayServerThread
     * <p> the video operation runs concurrently with the main function
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        receivedList = new HashMap<String, RecordObject>();
        new VideoServerThread(port1, receivedList);
        new DisplayServerThread(port2, receivedList);
    }
}