package client;

import common.Command;
import common.Message;

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
    
    public void establishAtestation() throws Exception {
        //enviar dados da app para atestação
        String appName = "SpertaClient";
        long appSize = getClientAppSize();
        out.writeObject(appName);
        out.writeObject(appSize);
        out.flush();

        //ler e confirmar resposta da atestação
        String attestationResponse = (String) in.readObject();

        if (attestationResponse.equals("ATTESTATION FAILED")) {
            System.out.println("ATTESTATION FAILED");
            socket.close();
            return;
        } else if (attestationResponse.equals("ATTESTATION OK")) {
            System.out.println("ATTESTATION OK");
        } else {
            System.out.println("Resposta inválida do servidor.");
            socket.close();
            return;
        }
    }
    
    public LoginResponse login(String userID, String password) throws Exception {
        out.writeObject(userID);
        out.writeObject(password);
        out.flush();

        String loginResponse = (String) in.readObject();

        if (loginResponse.equals("WRONG-PWD")) {
            return LoginResponse.WRONG_PWD;
        } else if (loginResponse.equals("OK-NEW-USER")) {
            return LoginResponse.OK_NEW_USER;
        } else if (loginResponse.equals("OK-USER")) {
            return LoginResponse.OK_USER;
        } else {
            throw new Exception("Resposta desconhecida do servidor.");
        }
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
    
    public void close() throws Exception {
        in.close();
        out.close();
        socket.close();
    }
    
    public void sendRequest(ClientRequest request) throws Exception {
        out.writeObject(request);
        out.flush();
    }
    
    public ServerResponse getResponse() throws Exception {
        return (ServerResponse) in.readObject();
    }
    
    public void handleResponse(ServerResponse response) {
        switch (response.getCommand()) {
            case "CREATE":
                if rsponse.getStatus().equals("OK")) {
                    System.out.println("Home criado com sucesso.");
                } else {
                    System.out.println("Erro ao criar home: " + response.getStatus());
                }
                break;
        }
    }

    /**
     * Formato de Execução: java -jar SpertaClient.jar <ip:port> <userID> <password>
     */
    public static void main(String[] args) throws Exception {
        String ipAddr = args[0].split(":")[0];
        int port = args[0].split(":").length == 1 ? 22345 : Integer.parseInt(args[0].split(":")[1]);
        
        SpertaClient client = new SpertaClient(ipAddr, port);
        client.establishAtestation();

        String userID = args[1];
        String pwd = args[2];
        
        LoginResponse loginResult = client.login(userID, pwd);
        if (loginResult == LoginResponse.WRONG_PWD) {
            System.out.println("Senha incorreta. Encerrando cliente.");
            client.close();
            return;
        } else if (loginResult == LoginResponse.OK_NEW_USER) {
            System.out.println("Novo usuário criado: " + userID);
        } else if (loginResult == LoginResponse.OK_USER) {
            System.out.println("Bem-vindo de volta, " + userID);
        }
        
        printCommands();
        try (Scanner sc = new Scanner(System.in)) { 
            while (true) {
                System.out.print("> ");
                String[] line = sc.nextLine().split(" ");
                String command = line[0];
                ClientRequest request; 
                switch (command) {
                    case "CREATE":
                        if (line.length != 2) {
                            System.out.println("Uso: CREATE <hm>");
                            continue;
                        }
                        String home = line[1];
                        request = new ClientRequest(Command.CREATE);
                        request.setHome(home);
                        break;
                    case "ADD":
                        if (line.length != 4) {
                            System.out.println("Uso: ADD <user1> <hm> <s>");
                            continue;
                        }
                        request = new ClientRequest(Command.ADD);
                        request.setHome(line[2]); request.setUserIdToAdd(line[1]); request.setSection(line[3]);
                        break;
                    case "RD":
                        if (line.length != 3) {
                            System.out.println("Uso: RD <hm> <s>");
                            continue;
                        }
                        request = new ClientRequest(Command.RD);
                        request.setHome(line[1]); request.setSection(line[2]);
                        break;
                    case "EC":
                        if (line.length != 4) {
                            System.out.println("Uso: EC <hm> <d> <int>");
                            continue;
                        }
                        request = new ClientRequest(Command.EC);
                        request.setHome(line[1]); request.setData(line[2]); request.setIntValue(Integer.parseInt(line[3]));
                        break;
                    case "RT":
                        if (line.length != 2) {
                            System.out.println("Uso: RT <hm>");
                            continue;
                        }
                        request = new ClientRequest(Command.RT);
                        request.setHome(line[1]);
                        break;
                    case "RH":
                        if (line.length != 3) {
                            System.out.println("Uso: RH <hm> <d>");
                            continue;
                        }
                        request = new ClientRequest(Command.RH);
                        request.setHome(line[1]); request.setData(line[2]);
                        break;
                    case "OUT":
                        // Enviar msg ao servidor a dizer que o cliente desconectou-se
                        break;
                    default:
                        System.out.println("Comando inválido.");
                        continue;
                }

                if (cmd == Command.OUT) {
                    break;
                }
                
                client.sendRequest(request);
                ServerResponse response = client.getResponse();
                client.handleResponse(response);
            }
            
        }

        client.close();

    }

}