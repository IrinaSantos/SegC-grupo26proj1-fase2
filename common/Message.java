package common;

import java.io.Serializable;

public class Message implements Serializable {

    private Command command;
    private String data;

    public Message(Command command, String data) {
        this.command = command;
        this.data = data;
    }

    public Command getCommand() {
        return command;
    }

    public String getData() {
        return data;
    }

}