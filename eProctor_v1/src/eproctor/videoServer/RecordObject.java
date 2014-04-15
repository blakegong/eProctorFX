package eproctor.videoServer;

import java.io.Serializable;

/**
 *
 * @author dingchengwang
 */
public class RecordObject implements Serializable {

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
    public byte[] cameraBytes;
    //public byte[] screenBytes;

    /**
     *
     * @param userId
     * @param cameraBytes
     */
    public RecordObject(String userId, byte[] cameraBytes) { //, byte[] screenBytes) {
        this.userId = userId;
        this.cameraBytes = cameraBytes;
        //this.screenBytes = screenBytes;
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
    public byte[] getCameraBytes() {
        return this.cameraBytes;
    }
//   
//    public byte[] getScreenBytes() {
//    	return this.screenBytes;
//    }
//    
}
