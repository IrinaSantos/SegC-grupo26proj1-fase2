package server;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Ponto de entrada do servidor Sperta.
 */
public class SpertaServer {


    /**
     * Inicia o servidor, aceita ligações e cria handlers para cada cliente.
     *
     * @param args argumentos de arranque; opcionalmente contem o porto
     * @throws Exception se ocorrer um erro ao iniciar o servidor
     */
    public static void main(String[] args) throws Exception {
        int port;
        if(args.length == 0){
            port = 22345; //  Por omissão, o servidor deve usar o porto 22345
        } else{
            port = Integer.parseInt(args[0]);
        }

        ServerSocket serverSocket = new ServerSocket(port);

        ServerState state = new ServerState();

        System.out.println("Server started on port " + port);

        while (true) {

            Socket client = serverSocket.accept();

            ClientHandler handler = new ClientHandler(client, state);

            handler.start();

        }

    }

}
