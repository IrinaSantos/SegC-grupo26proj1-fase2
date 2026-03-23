package common;

public class ClientRequest extends Message{

    // Login Attributes
    private String username;
    private String password;

    // Attestation Attributes
    private String appName;
    private long appSize;

    // General Request Attributes
    private String home;
    private String userIdToAdd; 
   // private String userIdToRemove;
    private String section;
    private String deviceId;
    private int intValue;

    public ClientRequest(Command command) {
        super(command);
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getAppName() {
        return appName;
    }
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    public long getAppSize() {
        return appSize;
    }
    public void setAppSize(long appSize) {
        this.appSize = appSize;
    }
    public String getHome() {
        return home;
    }
    public void setHome(String home) {
        this.home = home;
    }

    public String getUserIdToAdd() {
        return userIdToAdd;
    }
    public void setUserIdToAdd(String userIdToAdd) {
        this.userIdToAdd = userIdToAdd;
    }

    public String getSection() {
        return section;
    }
    public void setSection(String section) {
        this.section = section;
     }
    
    public String getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getIntValue() {
        return intValue;
    }
    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

}