package client;

import common.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Cliente de linha de comandos para interagir com o servidor SpertaServer.
 */
public class SpertaClient {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    /**
     * Cria uma ligacao ao servidor e inicializa os streams de comunicação.
     *
     * @param ipAddr endereco IP do servidor
     * @param port porto do servidor
     * @throws Exception se a ligacao ou os streams nao puderem ser criados
     */
    public SpertaClient(String ipAddr, int port) throws Exception {
        this.socket = new Socket(ipAddr, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }
    
    /**
     * Obtém o tamanho do binario do cliente para ser usado na atestação.
     *
     * @return tamanho do ficheiro compilado do cliente
     */
    private static long getClientAppSize() {
        // vai buscar o tamanho do ficheiro compilado para a atestação
        File classFile = new File("bin/client/SpertaClient.class");
        return classFile.length();
    }
    
    /**
     * Envia para o servidor os dados necessários para a atestação do cliente.
     *
     * @throws Exception caso ocorra uma falha de comunicação ou a atestação falhe
     */
    public void establishAtestation() throws Exception {
        //enviar dados da app para atestação
        String appName = "SpertaClient";
        long appSize = getClientAppSize();
        System.out.println("tamanho medio" + appSize);

        ClientRequest req = new ClientRequest(Command.ATTESTATION);
        req.setAppName(appName);
        req.setAppSize(appSize);
        sendRequest(req);

        ServerResponse res = getResponse();        
        if (res.getStatus()==ResponseStatus.ATTESTATION_OK) {
            System.out.println("ATTESTATION OK");
        } else {
            System.out.println("ATTESTATION FAILED: " + res.getStatus());
            close();
            System.exit(0);
        }
    }

    
    /**
     * Envia um pedido de login ao servidor.
     *
     * @param userID ID do utilizador
     * @param password password do utilizador
     * @return resposta do servidor ao pedido de login
     * @throws Exception se ocorrer um erro de comunicação
     */
    public ServerResponse login(String userID, String password) throws Exception {
        ClientRequest loginReq = new ClientRequest(Command.LOGIN);
        loginReq.setUsername(userID);
        loginReq.setPassword(password);
        sendRequest(loginReq);
        return getResponse();
    }

    /**
     * Fecha os recursos de comunicação associados ao cliente.
     */
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

    /**
     * Envia um pedido ao servidor.
     *
     * @param request pedido a enviar
     * @throws Exception se ocorrer um erro ao escrever no stream
     */
    public void sendRequest(ClientRequest request) throws Exception {
        out.writeObject(request);
        out.flush();
    }
    
    /**
     * Lê uma resposta do servidor.
     *
     * @return resposta recebida
     * @throws Exception se ocorrer um erro de leitura ou desserialização
     */
    public ServerResponse getResponse() throws Exception {
        return (ServerResponse) in.readObject();
    }
    
    /**
     * Interpreta e apresenta ao utilizador a resposta recebida do servidor.
     *
     * @param response resposta a tratar
     */
    public void handleResponse(ServerResponse response) {
        if (response == null) {
            System.out.println("Sem resposta do servidor.");
            return;
        }
        ResponseStatus status = response.getStatus();
        Command cmd = response.getCommand();
        
        if (status != ResponseStatus.OK && status != ResponseStatus.OK_USER && status != ResponseStatus.OK_NEW_USER){
            System.out.println("Erro: " + status);
            return;
        }

        switch (cmd) {
            case CREATE:
                System.out.println("Home criado com sucesso.");
                break;
            case ADD:
                System.out.println("Utilizador adicionado com sucesso.");
                break;
            case RD:
                System.out.println( "Dispositivo adicionado com sucesso.");
                break;
            case EC:
                System.out.println( "Comando executado com sucesso.");
                break;
            case RT: 
            case RH:
                receiveAndPrintFileContent(cmd == Command.RT ? "Estado Atual" : "Historico");
                break;
            default:
                System.out.println("Resposta desconhecida do servidor.");
        }
    }

    /**
     * Recebe e apresenta o conteúdo de um ficheiro enviado pelo servidor.
     *
     * @param title título apresentado antes do conteudo
     */
    private void receiveAndPrintFileContent(String title) {
        try {
            long fileSize = in.readLong();
            System.out.println("\n--- " + title + " ---");
            for (int i = 0; i < fileSize; i++) {
                System.out.print((char) in.readByte());
            }
            System.out.println("\n----------------------------");
        } catch (Exception e) {
            System.out.println("Erro ao ler dados do ficheiro: " + e.getMessage());
        }
    }

    /**
     * Ponto de entrada do cliente de linha de comandos (main).
     *
     * @param args argumentos de arranque
     * @throws Exception se ocorrer um erro nao tratado durante a execucao
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
            client.close();
            return;
           }
        }
        
        System.out.println("Comandos disponí­veis:\n CREATE <hm>\n ADD <user1> <hm> <s>\n RD <hm> <s>\n EC <hm> <d> <int>\n RT <hm>\n RH <hm> <d>\n OUT");
  
        while (true){
            System.out.print("> ");
            String input = sc.nextLine();
            if (input.equalsIgnoreCase("OUT")){
              sc.close();
              break;  
            } 

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
                }

                if(request != null){
                    client.sendRequest(request);
                    ServerResponse response = client.getResponse();
                    client.handleResponse(response);
                }
            } catch (Exception e) {
                System.out.println("Erro ao processar comando: " + e.getMessage());
            }
        }
        client.close();
        sc.close();
    }
}
