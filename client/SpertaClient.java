package client;

import common.Command;
import common.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class SpertaClient {

    public static void main(String[] args) throws Exception {

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        Socket socket = new Socket(host, port);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        Scanner sc = new Scanner(System.in);

        while (true) {

            System.out.print("> ");
            String line = sc.nextLine();

            String[] parts = line.split(" ");

            Command cmd = Command.valueOf(parts[0]);

            Message msg = new Message(cmd, line);

            out.writeObject(msg);

            Message response = (Message) in.readObject();

            System.out.println(response.getCommand() + " " + response.getData());

            if (cmd == Command.OUT)
                break;

        }

        socket.close();
    }

}