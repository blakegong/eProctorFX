package eproctor.videoServer;

import java.io.Serializable;

/**
 *This class is to build a Request Object
 * @author Chen Liyang
 * @author dingchengwang
 * @author Peng Lunan
 * @author Chen Desheng
 */
public class RequestObject implements Serializable {

    protected static final long serialVersionUID = 1124L;

    public String proctorId;

    public String requestId;
    
    /**
     *Constructor of RequestObject
     * @param proctorId Id of the proctor
     * @param requestId Id of the request
     */
    public RequestObject(String proctorId, String requestId) {
        this.proctorId = proctorId;
        this.requestId = requestId;
    }
}