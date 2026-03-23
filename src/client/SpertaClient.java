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

    /* commit irina 
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
    }*/
    
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
    
    /*Commit irina 
    public ResponseStatus login(String userID, String password) throws Exception {
        ClientRequest request = new ClientRequest(Command.LOGIN);
        request.setUsername(userID);
        request.setPassword(password);

        sendRequest(request);
        ServerResponse response = getResponse();
        return response.getStatus();
    }
    */
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
        /*commit irina 
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
    }*/
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