package common;

public class ClientRequest extends Message{

    private String home;
    private String userIdToAdd;
    private String userIdToRemove;
    private String section;
    // faltam mais

    public ClientRequest(Command command) {
        super(command);
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    // etc
}