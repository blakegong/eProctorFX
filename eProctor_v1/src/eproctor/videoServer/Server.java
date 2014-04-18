package eproctor.videoServer;

import java.io.*;
import java.util.HashMap;

/**
 *
 * @author dingchengwang
 */
public class Server {
    private static int port1 = 6001;
    private static int port2 = 6002;

    /**
     *
     */
    public static HashMap<String, RecordObject> receivedList;

    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        receivedList = new HashMap<String, RecordObject>();
        new VideoServerThread(port1, receivedList);
        new DisplayServerThread(port2, receivedList);
    }
}