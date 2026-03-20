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
    private ObjectOutputStream out;
    private ObjectInputStream in;
    String authenticatedUser;

    // Controllers
    private UserController userController;
    private AttestationControllet attestationController;

    public ClientHandler(Socket socket, ServerState state) {
        this.socket = socket;
        this.state = state;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.userController = new UserController();
        this.attestationController = new AttestationControllet();
    }
    
    public void close() {
        try {
            socket.close();
        } catch (Exception e) {
            System.out.println("Error closing socket: " + e.getMessage());
        }
    }

    public void verifyAttestation(String appName, long appSize) {
        ClientRequest attestationRequest = (ClientRequest) readRequest();
        ServerResponse attestationResponse;

        if (attestationRequest == null || attestationRequest.getCommand() != Command.ATTESTATION) {
            attestationResponse = new ServerResponse(Command.ATTESTATION, ResponseStatus.INVALID_REQUEST);
            sendServerResponse(attestationResponse);
            close(); return;
        } 

        if (attestationController.isAttestationRegistered(appName)) {
            if (!attestationController.verifyAttestation(appName, appSize)) {
                attestationResponse = new ServerResponse(Command.ATTESTATION, ResponseStatus.ATTESTATION_FAILED);
                sendServerResponse(attestationResponse);
                close(); return;
            } else attestationResponse = new ServerResponse(Command.ATTESTATION, ResponseStatus.ATTESTATION_OK);
        } else {
            if (!attestationController.registerAttestation(appName, appSize)) {
                attestationResponse = new ServerResponse(Command.ATTESTATION, ResponseStatus.ATTESTATION_SERVER_ERROR);
                sendServerResponse(attestationResponse);
                close(); return;
            } else attestationResponse = new ServerResponse(Command.ATTESTATION, ResponseStatus.ATTESTATION_OK);
        }
        sendServerResponse(attestationResponse); 
    }
    
    public void authenticateUser() {
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

                if (userController.userExists(user)) {
                    boolean authenticated = userController.authenticate(user, pwd);
                    if (authenticated) {
                        loginResponse = new ServerResponse(Command.LOGIN, ResponseStatus.OK_USER);
                        authenticatedUser = user;
                        isAuthenticated = true;
                    } else {
                        loginResponse = new ServerResponse(Command.LOGIN, ResponseStatus.WRONG_PWD);
                    }
                } else {
                    boolean registered = userController.register(user, pwd);
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
    
    public ClientRequest readRequest() {
        try {
            return (ClientRequest) in.readObject();
        } catch (Exception e) {
            System.out.println("Error reading request: " + e.getMessage());
            return null;
        }
    }
    
    public void sendServerResponse(ServerResponse response) {
        try {
            out.writeObject(response);
            out.flush();
        } catch (Exception e) {
            System.out.println("Error sending response: " + e.getMessage());
        }
    }

    public void run() {
        
        verifyAttestation(appName, appSize);
        authenticateUser();

        while (true) {

            ClientRequest request = readRequest();
            if (request == null) {
                close(); return;
            }
            
            ServerResponse response = processClientRequest(request);
            if (response == null) {
                close(); return;
            }
        }
    }

    public ServerResponse processClientRequest(ClientRequest request) {
        ServerResponse response;
        switch (request.getCommand()) {
            //Criar casa <hm> - utilizador é Owner 
            case CREATE:
        
                String houseName = request.getHome()
                boolean created = state.createCasa(houseName, user);
                
                if (created) {
                    response = new ServerResponse(request.getCommand(), ResponseStatus.OK);
                } else {
                    response = new ServerResponse(request.getCommand(), ResponseStatus.NOK);
                }
                break;

            //Adicionar utilizador <user1> à casa <hm>, seção <s>
            case ADD:
                
                String targetUser = request.getTargetUser();
                String houseName = request.getHome();
                String section = request.getSection();

                String permissionResponse = state.addPermission(user, targetUser, houseName, section);
                if (permissionResponse.equals("OK")) {
                    response = new ServerResponse(request.getCommand(), ResponseStatus.OK);
                } else {
                    response = new ServerResponse(request.getCommand(), ResponseStatus.NOK);
                }
                break;

            //Registar um Dispositivo na casa <hm>, na seção <s>
            case RD:
                break;

            //Enviar valor <int> de estado/temporização, do dispositivo <d> da casa <hm>, para o servidor
            case EC:
                break;

            //Receber a informação sobre o último comando  (estados/temporizações) enviado a cada dispositivo da 
            // casa <hm>, desde que o utilizador tenha permissões
            case RT:
                break;

            //Receber o Histórico (ficheiro de log .csv) de comandos enviados ao dispositivo <d> da casa <hm>, 
            // desde que o utilizador tenha permissões
            case RH:
                break;

            //CTRL+C
            case OUT:
                response = null;
                break;

            default:
                response = new ServerResponse(request.getCommand(), ResponseStatus.INVALID_REQUEST);
                break;
        }
        return response;
    }
       
}