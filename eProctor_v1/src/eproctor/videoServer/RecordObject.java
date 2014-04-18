package eproctor.videoServer;

import java.io.Serializable;

/**
 *This class is to build a Record Object
 * @author Chen Liyang
 * @author dingchengwang
 */
public class RecordObject implements Serializable {

    protected static final long serialVersionUID = 1123L;

    public String userId;

    public byte[] cameraBytes;
    //public byte[] screenBytes;

    /**
     *Constructor of RecordObject
     * @param userId Id of the user
     * @param cameraBytes Camera data in bytes
     */
    public RecordObject(String userId, byte[] cameraBytes) { //, byte[] screenBytes) {
        this.userId = userId;
        this.cameraBytes = cameraBytes;
        //this.screenBytes = screenBytes;
    }

    /**
     *Actuator of UserId
     * @return
     */
    public String getUserId() {
        return this.userId;
    }
    
    /**
     *Actuator of CameraBytes
     * @return
     */
    public byte[] getCameraBytes() {
        return this.cameraBytes;
    }
//   
//    public byte[] getScreenBytes() {
//    	return this.screenBytes;
//    }
//    
}
