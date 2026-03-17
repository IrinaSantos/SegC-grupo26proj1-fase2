package server;

import common.Message;
import common.Command;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {

    private Socket socket;
    private ServerState state;

    public ClientHandler(Socket socket, ServerState state) {
        this.socket = socket;
        this.state = state;
    }

    public void run() {

        try {

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            //out.flush(); //envia user e password para o servidor confirmar (linha em standby cause unsure)
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            
            String user = (String) in.readObject();
            String psswd = (String) in.readObject();

            //autenticação e registo do utilizador
            String loginResponse = state.login(user, psswd);
            out.writeObject(loginResponse);
            out.flush();

            //se a password estiver errada, fecha a conexão
            if (loginResponse.equals("WRONG-PWD")) {
                socket.close();
                return;
            }

            while (true) {

                Message msg = (Message) in.readObject();

                switch (msg.getCommand()) {

                    case AU:
                        out.writeObject(new Message(Command.OK, "Authenticated"));
                        break;

                    case AD:
                        out.writeObject(new Message(Command.OK, "Device added"));
                        break;

                    case RD:
                        out.writeObject(new Message(Command.INFO, "Device created"));
                        break;

                    case ET:
                        out.writeObject(new Message(Command.OK, "Temperature stored"));
                        break;

                    case EI:
                        out.writeObject(new Message(Command.OK, "Image stored"));
                        break;

                    case RT:
                        out.writeObject(new Message(Command.INFO, "Temp file"));
                        break;

                    case RH:
                        out.writeObject(new Message(Command.INFO, "Image file"));
                        break;

                    case OUT:
                        socket.close();
                        return;

                    default:
                        out.writeObject(new Message(Command.NOK, "Unknown command"));
                }

                out.flush();

            }

        } catch (Exception e) {
            System.out.println("Client disconnected");
        }

    }

}