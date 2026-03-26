package server;
import common.*;
import java.io.*;
import java.net.Socket;

/**
 * Trata a comunicação com um cliente ligado ao servidor.
 */
public class ClientHandler extends Thread {

    private Socket socket;
    private ServerState state;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String authenticatedUser;

    // Controllers
    private UserController userController;
    private AttestationController attestationController;

    /**
     * Cria um novo handler associado a um socket de cliente.
     *
     * @param socket socket do cliente
     * @param state estado global do servidor
     * @throws IOException se ocorrer um erro ao inicializar os streams
     */
    public ClientHandler(Socket socket, ServerState state) throws IOException {
        this.socket = socket;
        this.state = state;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.userController = new UserController();
        this.attestationController = new AttestationController();
    }
    
    /**
     * Fecha o socket associado ao cliente.
     */
    public void close() {
        try {
            socket.close();
        } catch (Exception e) {
            System.out.println("Error closing socket: " + e.getMessage());
        }
    }

    /**
     * Valida a atestação enviada pelo cliente antes da autenticação.
     *
     * @throws Exception se o pedido de atestação for invalido ou falhar
     */
    public void verifyAttestation() throws Exception {
        ClientRequest attestationRequest = (ClientRequest) readRequest();
        ServerResponse attestationResponse;

        if (attestationRequest == null || attestationRequest.getCommand() != Command.ATTESTATION) {
            attestationResponse = new ServerResponse(Command.ATTESTATION, ResponseStatus.INVALID_REQUEST);
            sendServerResponse(attestationResponse);
            throw new Exception("Invalid attestation request");
        } 

        String appName = attestationRequest.getAppName();
        long appSize = attestationRequest.getAppSize();

        if(attestationController.verifyAttestation(appName, appSize)){
            attestationResponse = new ServerResponse(Command.ATTESTATION, ResponseStatus.ATTESTATION_OK);
            sendServerResponse(attestationResponse);
        }else{
            sendServerResponse(new ServerResponse(Command.ATTESTATION, ResponseStatus.ATTESTATION_FAILED));
            throw new Exception("Attestation failed");
        }
    }
    
    /**
     * Processa o fluxo de autenticação e registo de utilizadores.
     *
     * @throws Exception se ocorrer um erro irrecuperável durante a autenticação
     */
    public void authenticateUser() throws Exception {
        boolean isAuthenticated = false;
        while (!isAuthenticated) { 
            ServerResponse loginResponse;
            try {
                 ClientRequest loginRequest = (ClientRequest) readRequest();
            
                if (loginRequest == null || loginRequest.getCommand() != Command.LOGIN) {
                    loginResponse = new ServerResponse(Command.LOGIN, ResponseStatus.INVALID_REQUEST);
                    continue;
                }
                
                String user = loginRequest.getUsername(); 
                String pwd = loginRequest.getPassword();

                if (user == null || pwd == null) {
                    loginResponse = new ServerResponse(Command.LOGIN, ResponseStatus.INVALID_REQUEST);
                    continue;
                }

                if (userController.isUserRegistered(user)) {
                    boolean authenticated = userController.authenticate(user, pwd);
                    if (authenticated) {
                        loginResponse = new ServerResponse(Command.LOGIN, ResponseStatus.OK_USER);
                        authenticatedUser = user;
                        isAuthenticated = true;
                    } else {
                        loginResponse = new ServerResponse(Command.LOGIN, ResponseStatus.WRONG_PWD);
                    }
                } else {
                    boolean registered = userController.registerUser(user, pwd);
                    if (registered) {
                        loginResponse = new ServerResponse(Command.LOGIN, ResponseStatus.OK_NEW_USER);
                        authenticatedUser = user;
                        isAuthenticated = true;
                    } else {
                        loginResponse = new ServerResponse(Command.LOGIN, ResponseStatus.ERROR);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error during authentication: " + e.getMessage());
                loginResponse = new ServerResponse(Command.LOGIN, ResponseStatus.ERROR);
            }
            sendServerResponse(loginResponse);
        }
    }
    
    /**
     * Lê um pedido enviado pelo cliente.
     *
     * @return pedido recebido ou {@code null} em caso de erro
     */
    public ClientRequest readRequest() {
        try {
            return (ClientRequest) in.readObject();
        } catch (Exception e) {
            System.out.println("Error reading request: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Envia uma resposta ao cliente.
     *
     * @param response resposta a enviar
     */
    public void sendServerResponse(ServerResponse response) {
        try {
            out.writeObject(response);
            out.flush();
        } catch (Exception e) {
            System.out.println("Error sending response: " + e.getMessage());
        }
    }

    /**
     * Executa o ciclo de vida da ligação com o cliente.
     */
    public void run() {
        try{
        verifyAttestation();
        authenticateUser();

        while (true) {
            ClientRequest request = readRequest();
            if (request == null || request.getCommand() == Command.OUT) break;

            if(request.getCommand() == Command.RT || request.getCommand() == Command.RH) {
                handleFileRequest(request);
            }else{
                ServerResponse response = processClientRequest(request);
                sendServerResponse(response);
            }
        }
        } catch (Exception e) {
            System.out.println("Connection closed: " + e.getMessage());
        } finally {
            close();
        }
    }

    /**
     * Processa um pedido funcional do cliente e devolve a resposta apropriada.
     *
     * @param request pedido recebido
     * @return resposta a enviar ao cliente
     */
    public ServerResponse processClientRequest(ClientRequest request) {
        ServerResponse response;
        try{
            switch (request.getCommand()) {
                //Criar casa <hm> - utilizador Ã© Owner 
                case CREATE:
                    String houseName = request.getHome();
                    boolean created = state.createCasa(houseName, authenticatedUser);
                
                    return new ServerResponse(Command.CREATE, created ? ResponseStatus.OK : ResponseStatus.NOK);

                //Adicionar utilizador <user1> à  casa <hm>, secção <s>
                case ADD:
                    
                String resAdd = state.addPermission(authenticatedUser, request.getUserIdToAdd(), request.getHome(), request.getSection());
                return new ServerResponse(Command.ADD, ResponseStatus.valueOf(resAdd));

                //Registar um Dispositivo na casa <hm>, na secção <s>
                case RD:
                    String resRd = state.registerDevice(authenticatedUser, request.getHome(), request.getSection());
                    return new ServerResponse(Command.RD, ResponseStatus.valueOf(resRd));

                //Enviar valor <int> de estado/temporização, do dispositivo <d> da casa <hm>, para o servidor
                case EC:
                    String resEC = state.executeCommand(authenticatedUser, request.getHome(), request.getDeviceId(), request.getIntValue());
                    return new ServerResponse(Command.EC, ResponseStatus.valueOf(resEC));

                //Receber a informação sobre o último comando  (estados/temporizaÃ§Ãµes) enviado a cada dispositivo da 
                // casa <hm>, desde que o utilizador tenha permissÃµes
                
                // EstÃ£o a ser tratados no RUN()
                /*case RT:
                //Receber o HistÃ³rico (ficheiro de log .csv) de comandos enviados ao dispositivo <d> da casa <hm>, 
                // desde que o utilizador tenha permissÃµes
                // EstÃ£o a ser tratados no RUN()
                case RH:
                    return new ServerResponse(request.getCommand(), ResponseStatus.OK);
                */
                //CTRL+C
                case OUT:
                    response = null;

                default:
                    return new ServerResponse(request.getCommand(), ResponseStatus.INVALID_REQUEST);
            }
        }catch (IllegalArgumentException e) {
            return new ServerResponse(request.getCommand(), ResponseStatus.ERROR);
        }
    }

    /**
     * Trata pedidos de transferência de ficheiros associados a casas ou dispositivos.
     *
     * @param request pedido recebido
     * @throws IOException se ocorrer um erro durante a leitura ou envio do ficheiro
     */
    private void handleFileRequest(ClientRequest request) throws IOException {
        File file;
        if (request.getCommand() == Command.RT) {
            file = state.getHouseFile(authenticatedUser, request.getHome());
        } else {
            file = state.getDeviceLogFile(authenticatedUser, request.getHome(), request.getDeviceId());
        }
       
        if (file == null || !file.exists()) {
            sendServerResponse (new ServerResponse(request.getCommand(), ResponseStatus.NOT_FOUND));
            return;
        }
        sendServerResponse(new ServerResponse(request.getCommand(), ResponseStatus.OK));

        out.writeLong(file.length());
        out.flush();

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }   
        }
        out.flush();
    }
}
