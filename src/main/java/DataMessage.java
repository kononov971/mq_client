import java.io.Serializable;
import java.util.Base64;

public class DataMessage implements Serializable {
    private String systemCode;
    private String sourceSys;
    private String targetSys;
    private String objectName;
    private String CRC;
    private String description;
    private byte[] body;

    public DataMessage(String systemCode, String sourceSys, String targetSys, String objectName, String CRC,
                       String description, byte[] body) {
        this.systemCode = systemCode;
        this.sourceSys = sourceSys;
        this.targetSys = targetSys;
        this.objectName = objectName;
        this.CRC = CRC;
        this.description = description;
        this.body = Base64.getEncoder().encode(body);
    }
}
