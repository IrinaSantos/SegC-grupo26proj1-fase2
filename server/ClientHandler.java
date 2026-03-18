package server;

import common.Message;
import common.Command;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
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
            
            //dados da app
            String appName = (String) in.readObject();
            long appSize = (Long) in.readObject();

            //verificar attestation
            boolean attested = verifyAttestation(appName, appSize);

            if (attested) {
                out.writeObject("ATTESTATION OK");
                out.flush();
            } else {
                out.writeObject("ATTESTATION FAILED");
                out.flush();
                socket.close();
                return;
            }

            //dados de login
            String user = (String) in.readObject();
            String pwd = (String) in.readObject();

            //autenticação e registo do utilizador
            String loginResponse = state.login(user, pwd);
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

                    case CREATE:
                        String[] createParts = msg.getData().trim().split(" "); //secalhar trocar para split("\\s+"") para lidar com múltiplos espaços

                        if (createParts.length != 2) {
                            out.writeObject(new Message(Command.NOK, "Invalid CREATE command"));
                            break;
                        }

                        String houseName = createParts[1];
                        boolean created = state.createCasa(houseName, user);

                        if (created) {
                            out.writeObject(new Message(Command.OK, "House created"));
                        } else {
                            out.writeObject(new Message(Command.NOK, "House not created"));
                        }
                        break;

                    case ADD:
                        out.writeObject(new Message(Command.OK, "Permission added"));
                        break;

                    case RD:
                        out.writeObject(new Message(Command.OK, "Device created"));
                        break;

                    case EC:
                        out.writeObject(new Message(Command.OK, "State updated"));
                        break;

                    case RT:
                        out.writeObject(new Message(Command.INFO, "Temp file"));
                        break;

                    case RH:
                        out.writeObject(new Message(Command.INFO, "History file"));
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

    private boolean verifyAttestation(String appName, long appSize) {
        //try que garante que o reader é fechado após a leitura do ficheiro, mesmo que ocorra um erro
        try (BufferedReader reader = new BufferedReader(new FileReader("attestation.txt"))) {

            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");

                //verifica se a linha tem o formato correto (nome:tamanho) e ignora se não tiver
                if (parts.length != 2) {
                    continue;
                }

                String storedAppName = parts[0].trim();
                long storedAppSize = Long.parseLong(parts[1].trim());

                //verifica se existe uma entrada com esse nome e tamanho armazenada
                if (storedAppName.equals(appName) && storedAppSize == appSize) {
                    return true;
                }
            }

        } catch (Exception e) {
            System.out.println("Erro ao ler attestation.txt");
        }

        return false;
    }
}