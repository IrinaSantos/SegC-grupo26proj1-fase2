package common;

import java.io.Serializable;

/**
 * Classe base serializavel para mensagens trocadas entre cliente e servidor.
 */
public abstract class Message implements Serializable {

    private final Command command;

    /**
     * Cria uma mensagem associada a um comando.
     *
     * @param command comando da mensagem
     */
    public Message(Command command) {
        this.command = command;
    }

    /**
     * Devolve o comando associado a esta mensagem.
     *
     * @return comando da mensagem
     */
    public Command getCommand() {
        return command;
    }

}
