package common;

import java.io.Serializable;

public abstract class Message implements Serializable {

    private final Command command;

    public Message(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

}