package common;

/**
 * Representa uma resposta enviada pelo servidor ao cliente.
 */
public class ServerResponse extends Message {

    private ResponseStatus status;

    /**
     * Cria uma resposta com comando e estado associados.
     *
     * @param command comando a que a resposta diz respeito
     * @param status estado devolvido pelo servidor
     */
    public ServerResponse(Command command, ResponseStatus status) {
        super(command);
        this.status = status;
    }

    /**
     * Devolve o estado da resposta.
     *
     * @return estado da resposta
     */
    public ResponseStatus getStatus() {
        return status;
    }
}
