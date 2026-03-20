package common;

public class ClientRequest extends Message{

    // Login Attributes
    private String username;
    private String password;

    private String home;
    private String userIdToAdd; 
    private String userIdToRemove;  //??
    private String section;
    // faltam mais

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
    // etc
}