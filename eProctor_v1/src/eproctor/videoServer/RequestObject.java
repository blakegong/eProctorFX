package eproctor.videoServer;

import java.io.Serializable;

public class RequestObject implements Serializable {

    protected static final long serialVersionUID = 1124L;
    public String proctorId;
    public String requestId;
    
    public RequestObject(String proctorId, String requestId) {
        this.proctorId = proctorId;
        this.requestId = requestId;
    }
}