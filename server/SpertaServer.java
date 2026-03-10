package server;

import java.net.ServerSocket;
import java.net.Socket;

public class SpertaServer {

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(args[0]);

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