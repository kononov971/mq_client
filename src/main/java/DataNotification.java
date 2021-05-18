import java.io.Serializable;

public class DataNotification implements Serializable {
    private String systemCode;
    private String sourceSys;
    private String targetSys;
    private String objectName;
    private String CRC;
    private String availableDttm;
    private String Description;

    public DataNotification(String systemCode, String sourceSys, String targetSys, String objectName, String CRC,
                            String availableDttm, String description) {
        this.systemCode = systemCode;
        this.sourceSys = sourceSys;
        this.targetSys = targetSys;
        this.objectName = objectName;
        this.CRC = CRC;
        this.availableDttm = availableDttm;
        Description = description;
    }
}
