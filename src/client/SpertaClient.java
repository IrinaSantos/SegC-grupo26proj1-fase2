package client;

import common.*;

import java.io.*;
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
        // confirmar se é .class ou .jar
        File classFile = new File("bin/client/SpertaClient.class");
        return classFile.length();
    }
    
    public void establishAtestation() throws Exception {
        //enviar dados da app para atestação
        String appName = "SpertaClient";
        long appSize = getClientAppSize();
        System.out.println("tamanho medio" + appSize);
        ClientRequest req = new ClientRequest(Command.ATTESTATION);
        req.setAppName(appName);
        req.setAppSize(appSize);
        out.writeObject(req);
        out.flush();

        //ler e confirmar resposta da atestação
        //String attestationResponse = (String) in.readObject();
        ServerResponse res = (ServerResponse) in.readObject();
        
        if (res.getStatus()==ResponseStatus.ATTESTATION_OK) {
            System.out.println("ATTESTATION OK");
        } else {
            System.out.println("ATTESTATION FAILED: " + res.getStatus());
            socket.close();
            System.exit(0);
        }
    }
    
    public ServerResponse login(String userID, String password) throws Exception {
        ClientRequest loginReq = new ClientRequest(Command.LOGIN);
        loginReq.setUsername(userID);
        loginReq.setPassword(password);

        out.writeObject(loginReq);
        out.flush();

       // String loginResponse = (String) in.readObject();

        ServerResponse response = (ServerResponse) in.readObject();
        return response;
/*
        if (loginResponse.equals("WRONG-PWD")) {
            return LoginResponse.WRONG_PWD;
        } else if (loginResponse.equals("OK-NEW-USER")) {
            return LoginResponse.OK_NEW_USER;
        } else if (loginResponse.equals("OK-USER")) {
            return LoginResponse.OK_USER;
        } else {
            throw new Exception("Resposta desconhecida do servidor.");
        }*/
    }
    
   /* public static void printCommands() {
        System.out.println("Comandos disponíveis:");
        System.out.println("CREATE <hm>");
        System.out.println("ADD <user1> <hm> <s>");
        System.out.println("RD <hm> <s>");
        System.out.println("EC <hm> <d> <int>");
        System.out.println("RT <hm>");
        System.out.println("RH <hm> <d>");
        System.out.println("OUT");
    }*/
    
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
            case CREATE:
                handleSimpleResponse(response, "Home criado com sucesso.");
                break;
            case ADD:
                handleSimpleResponse(response, "Utilizador adicionado com sucesso.");
                break;
            case RD:
                handleSimpleResponse(response, "Dispositivo adicionado com sucesso.");
                break;
            case EC:
                handleSimpleResponse(response, "Comando executado com sucesso.");
                break;
            case RT:
                //TO-DO
                break; 
            case RH:
                if (response.getStatus().equals(ResponseStatus.OK)) {
                    System.out.println("A receber ficheiro...");
                    receiveFileFromServer("log.csv");//to-do
                } else {
                    System.out.println("Erro ao obter histórico: " + response.getStatus());
                }
                break;
            default:
                System.out.println("Resposta desconhecida do servidor.");
        }
    }

    public void handleSimpleResponse(ServerResponse response, String successMsg){
        if(response.getStatus() == (ResponseStatus.OK)){
            System.out.println(successMsg);
        } else {
            System.out.println("Erro: " + response.getStatus());
        }
    }

    private void receiveFileFromServer(String fileName) {
        //TO-DO
        try {
            long fileSize = in.readLong();
            File file = new File(fileName);

            try (FileOutputStream fos = new FileOutputStream(file)){
                byte[] buffer = new byte[4096];
                long bytesReadTotal = 0;
                int bytesRead;

                while (bytesReadTotal < fileSize && (bytesRead = in.read(buffer, 0, (int)Math.min(buffer.length, fileSize - bytesReadTotal))) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    bytesReadTotal += bytesRead;
                }
            }
            System.out.println("Ficheiro recebido com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao receber ficheiro: " + e.getMessage());
        }
    }
    /**
     * Formato de Execução: java -jar SpertaClient.jar <ip:port> <userID> <password>
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Uso: SpertaClient <ip:port> <userID> <password>");
            return;
        }

        String ipAddr = args[0].split(":")[0];
        int port = args[0].split(":").length == 1 ? 22345 : Integer.parseInt(args[0].split(":")[1]);
        
        SpertaClient client = new SpertaClient(ipAddr, port);
        Scanner sc = new Scanner(System.in);
        client.establishAtestation();

        String userID = args[1]; 
        String pwd = args[2];
        
        while (true) {
           /* ServerResponse loginResponse = client.login(userID, pwd);
            if (loginResponse == LoginResponse.WRONG_PWD) {
                System.out.println("Password incorreta. Por favor, tente novamente.");
                try (Scanner sc = new Scanner(System.in)) {
                    System.out.print("Username: ");
                    userID = sc.nextLine();
                    System.out.print("Password: ");
                    pwd = sc.nextLine();
                } catch (Exception e) {
                    System.out.println("Erro ao ler input: " + e.getMessage());
                    client.close();
                    return;
                }
            } else if (loginResponse == LoginResponse.OK_NEW_USER) {
                System.out.println("Novo utilizador registado e autenticado com sucesso.");
                break;
            } else if (loginResponse == LoginResponse.OK_USER) {
                System.out.println("Utilizador autenticado com sucesso.");
                break;
            } else {
                System.out.println("Resposta desconhecida do servidor. Encerrando cliente.");
                client.close();
                return;
            }*/
           ResponseStatus loginStatus = client.login(userID, pwd).getStatus();
           if(loginStatus == ResponseStatus.OK_USER || loginStatus == ResponseStatus.OK_NEW_USER){
            System.out.println("Login bem-sucedido:" + userID);
            break;
           }else if (loginStatus == ResponseStatus.WRONG_PWD) {
            System.out.println("Password incorreta.");
            System.out.print("Username: ");
            userID = sc.nextLine();
            System.out.print("Password: ");
            pwd = sc.nextLine();
           } else {
            System.out.println("Erro");
            return;
           }
        }
/*
        client.login(userID, pwd);
        
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

    }*/
        System.out.println("Comandos disponíveis:\n CREATE <hm>\n ADD <user1> <hm> <s>\n RD <hm> <s>\n EC <hm> <d> <int>\n RT <hm>\n RH <hm> <d>\n OUT");
  
        while (true){
            System.out.print("> ");
            String input = sc.nextLine();
            if (input.equalsIgnoreCase("OUT")) break;

            String[] line = input.split(" ");
            ClientRequest request = null;
    
            try{
                switch (line[0].toUpperCase()) {
                    case "CREATE":
                        request = new ClientRequest(Command.CREATE);
                        request.setHome(line[1]);
                        break;
                    case "ADD":
                        request = new ClientRequest(Command.ADD);
                        request.setUserIdToAdd(line[1]);
                        request.setHome(line[2]);
                        request.setSection(line[3]);
                        break;
                    case "RD":
                        request = new ClientRequest(Command.RD);
                        request.setHome(line[1]);
                        request.setSection(line[2]);
                        break;
                    case "EC":
                        request = new ClientRequest(Command.EC);
                        request.setHome(line[1]);
                        request.setDeviceId(line[2]);
                        request.setIntValue(Integer.parseInt(line[3]));
                        break;
                    case "RT":
                        request = new ClientRequest(Command.RT);
                        request.setHome(line[1]);
                        break;
                    case "RH":
                        request = new ClientRequest(Command.RH);
                        request.setHome(line[1]);
                        request.setDeviceId(line[2]);
                        break;
                    default:
                        break;
                }

                if(request != null){
                    client.sendRequest(request);
                    client.out.flush();
                    ServerResponse response = client.getResponse();
                    client.handleResponse(response);
                }
            } catch (Exception e) {
                System.out.println("Erro ao processar comando: " + e.getMessage());
            }
        }
        client.socket.close();
        sc.close();
    }
}