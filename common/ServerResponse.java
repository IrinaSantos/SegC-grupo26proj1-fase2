public class ServerResponse extends Message {

    private ResponseStatus status;

    public ServerResponse(Command command, ResponseStatus status) {
        super(command);
        this.status = status;
    }

    public ResponseStatus getStatus() {
        return status;
    }
}