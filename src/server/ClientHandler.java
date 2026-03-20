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

    public ClientHandler(Socket socket, ServerState state) {
        this.socket = socket;
        this.state = state;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.userController = new UserController();
    }
    
    public void close() {
        try {
            socket.close();
        } catch (Exception e) {
            System.out.println("Error closing socket: " + e.getMessage());
        }
    }
    
    public void authenticateUser() {
        String currentUser = null; 
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
                        currentUser = user;
                        isAuthenticated = true;
                    } else {
                        loginResponse = new ServerResponse(Command.LOGIN, ResponseStatus.WRONG_PWD);
                    }
                } else {
                    boolean registered = userController.register(user, pwd);
                    if (registered) {
                        loginResponse = new ServerResponse(Command.LOGIN, ResponseStatus.OK_NEW_USER);
                        currentUser = user;
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
        authenticatedUser = currentUser;
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
        
        //verificar attestation
        boolean attested = verifyAttestation(appName, appSize);

        if (attested) {
            out.writeObject("ATTESTATION OK");
            out.flush();
        } else {
            out.writeObject("ATTESTATION FAILED");
            out.flush();
            close();
            return;
        }
        
        authenticateUser();

        while (true) {

            ClientRequest request = readRequest();
            if (request == null) {
                close();
                return;
            }
            
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
                    out.writeObject(new Message(Command.OK, "Device created"));
                    break;

                //Enviar valor <int> de estado/temporização, do dispositivo <d> da casa <hm>, para o servidor
                case EC:
                    out.writeObject(new Message(Command.OK, "State updated"));
                    break;

                //Receber a informação sobre o último comando  (estados/temporizações) enviado a cada dispositivo da 
                // casa <hm>, desde que o utilizador tenha permissões
                case RT:
                    out.writeObject(new Message(Command.INFO, "Temp file"));
                    break;

                //Receber o Histórico (ficheiro de log .csv) de comandos enviados ao dispositivo <d> da casa <hm>, 
                // desde que o utilizador tenha permissões
                case RH:
                    out.writeObject(new Message(Command.INFO, "History file"));
                    break;

                //CTRL+C
                case OUT:
                    close();
                    return;

                default:
                    out.writeObject(new Message(Command.NOK, "Unknown command"));
            }

            sendServerResponse(response);

            }
    }

    private boolean verifyAttestation(String appName, long appSize) {
       //dados da app 
        String appName = (String) in.readObject();
        long appSize = (Long) in.readObject();
       
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