package client;

import common.ClientRequest;
import common.Command;
import common.ResponseStatus;
import common.ServerResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.net.Socket;
import java.util.Scanner;

public class SpertaClient {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    public SpertaClient(String ipAddr, int port) throws Exception {
        this.socket = new Socket(ipAddr, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }
    
    private static long getClientAppSize() {
        File classFile = new File("client/SpertaClient.class");
        return classFile.length();
    }
    
    public boolean establishAttestation() throws Exception {
        ClientRequest request = new ClientRequest(Command.ATTESTATION);
        request.setAppName("SpertaClient");
        request.setAppSize(getClientAppSize());

        sendRequest(request);
        ServerResponse response = getResponse();

        if (response.getStatus() == ResponseStatus.ATTESTATION_OK) {
            System.out.println("ATTESTATION OK");
            return true;
        }

        if (response.getStatus() == ResponseStatus.ATTESTATION_FAILED) {
            System.out.println("ATTESTATION FAILED");
            return false;
        }

        System.out.println("Resposta inválida de atestação: " + response.getStatus());
        return false;
    }
    
    public ResponseStatus login(String userID, String password) throws Exception {
        ClientRequest request = new ClientRequest(Command.LOGIN);
        request.setUsername(userID);
        request.setPassword(password);

        sendRequest(request);
        ServerResponse response = getResponse();
        return response.getStatus();
    }
    
    public static void printCommands() {
        System.out.println("Comandos disponíveis:");
        System.out.println("CREATE <hm>");
        System.out.println("ADD <user1> <hm> <s>");
        System.out.println("RD <hm> <s>");
        System.out.println("EC <hm> <d> <int>");
        System.out.println("RT <hm>");
        System.out.println("RH <hm> <d>");
        System.out.println("OUT");
    }
    
    public void close() {
        try {
            in.close();
        } catch (Exception ignored) { }
        try {
            out.close();
        } catch (Exception ignored) { }
        try {
            socket.close();
        } catch (Exception ignored) { }
    }

    public void sendRequest(ClientRequest request) throws Exception {
        out.writeObject(request);
        out.flush();
    }
    
    public ServerResponse getResponse() throws Exception {
        return (ServerResponse) in.readObject();
    }
    
    public void handleResponse(ServerResponse response) {
        if (response == null) {
            System.out.println("Sem resposta do servidor.");
            return;
        }

        Command cmd = response.getCommand();
        ResponseStatus status = response.getStatus();

        switch (cmd) {
            case CREATE:
                if (status == ResponseStatus.OK) {
                    System.out.println("Casa criada com sucesso.");
                } else {
                    System.out.println("Erro ao criar casa: " + status);
                }
                break;

            case ADD:
                if (status == ResponseStatus.OK) {
                    System.out.println("Permissão adicionada com sucesso.");
                } else {
                    System.out.println("Erro no ADD: " + status);
                }
                break;

            case RD:
                System.out.println("Resposta RD: " + status);
                break;

            case EC:
                System.out.println("Resposta EC: " + status);
                break;

            case RT:
                System.out.println("Resposta RT: " + status);
                break;

            case RH:
                System.out.println("Resposta RH: " + status);
                break;

            default:
                System.out.println("Resposta: " + status);
        }
    }

    public ClientRequest buildRequestFromInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String[] parts = input.trim().split("\\s+");
        String commandText = parts[0].toUpperCase();

        Command command;
        try {
            command = Command.valueOf(commandText);
        } catch (IllegalArgumentException e) {
            return null;
        }

        ClientRequest request;

        //só o create e add é que tão 100% implementados, os outros tão mais para teste do servidor
        switch (command) {
            case CREATE:
                if (parts.length != 2) return null;
                request = new ClientRequest(Command.CREATE);
                request.setHome(parts[1]);
                return request;
            case ADD:
                if (parts.length != 4) return null;
                request = new ClientRequest(Command.ADD);
                request.setTargetUser(parts[1]);
                request.setHome(parts[2]);
                request.setSection(parts[3]);
                return request;
            case RD:
                if (parts.length != 3) return null;
                request = new ClientRequest(Command.RD);
                request.setHome(parts[1]);
                request.setSection(parts[2]);
                return request;
            case EC:
                if (parts.length != 4) return null;
                request = new ClientRequest(Command.EC);
                request.setHome(parts[1]);
                request.setDevice(parts[2]);
                try {
                    request.setValue(Integer.parseInt(parts[3]));
                } catch (NumberFormatException e) {
                    return null;
                }
                return request;
            case RT:
                if (parts.length != 2) return null;
                request = new ClientRequest(Command.RT);
                request.setHome(parts[1]);
                return request;
            case RH:
                if (parts.length != 3) return null;
                request = new ClientRequest(Command.RH);
                request.setHome(parts[1]);
                request.setDevice(parts[2]);
                return request;
            case OUT:
                return new ClientRequest(Command.OUT);
            default:
                return null;
        }
    }

    /**
     * Formato de Execução: java -jar SpertaClient.jar <ip:port> <userID> <password>
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Uso: java -jar SpertaClient.jar <ip[:port]> <userID> <password>");
            return;
        }

        String[] hostParts = args[0].split(":");
        String ipAddr = hostParts[0];
        int port = hostParts.length == 1 ? 22345 : Integer.parseInt(hostParts[1]);

        String userID = args[1];
        String pwd = args[2];

        SpertaClient client = new SpertaClient(ipAddr, port);

        try (Scanner sc = new Scanner(System.in)) {

            if (!client.establishAttestation()) {
                return;
            }

            boolean loggedIn = false;
            while (!loggedIn) {
                ResponseStatus loginStatus = client.login(userID, pwd);

                if (loginStatus == ResponseStatus.WRONG_PWD) {
                    System.out.println("WRONG-PWD");
                    System.out.print("Password: ");
                    pwd = sc.nextLine();
                } else if (loginStatus == ResponseStatus.OK_NEW_USER) {
                    System.out.println("OK-NEW-USER");
                    loggedIn = true;
                } else if (loginStatus == ResponseStatus.OK_USER) {
                    System.out.println("OK-USER");
                    loggedIn = true;
                } else {
                    System.out.println("Erro no login: " + loginStatus);
                    return;
                }
            }

            printCommands();

            while (true) {
                System.out.print("> ");
                String input = sc.nextLine();

                ClientRequest request = client.buildRequestFromInput(input);
                if (request == null) {
                    System.out.println("Comando inválido.");
                    continue;
                }

                client.sendRequest(request);

                if (request.getCommand() == Command.OUT) {
                    break;
                }

                ServerResponse response = client.getResponse();
                client.handleResponse(response);
            }

        } finally {
            client.close();
        }
    }
}