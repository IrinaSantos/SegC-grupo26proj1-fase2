package client;

import common.Command;
import common.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.net.Socket;
import java.util.Scanner;

public class SpertaClient {

    private static long getClientAppSize() {
        File classFile = new File("client/SpertaClient.class");
        return classFile.length();
    }

    public static void main(String[] args) throws Exception {
        try {
            int port;
            String[] host = args[0].split(":");
            String ipAddr = host[0];
            String userID = args[1];
            String pwd = args[2];

            if(host.length == 1){
                port = 22345;
            } else{
                port = Integer.parseInt(host[1]);
            }
            
            Socket socket = new Socket(ipAddr, port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            //out.flush(); (linha em standby cause unsure (--))
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

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

            out.writeObject(userID);
            out.writeObject(pwd);
            out.flush(); //envia user e password para o servidor confirmar

            String loginResponse = (String) in.readObject(); //recebe a info do servidor

            if(loginResponse.equals("WRONG-PWD")){
                System.out.println("Password inválida.");
                socket.close();
                return;

            } else if(loginResponse.equals("OK-NEW-USER")){
                System.out.println("Novo utilizador Registado!");

            } else if(loginResponse.equals("OK-USER")){
                
                System.out.println("Bem-Vindo!");
            } else {
                System.out.println("Resposta desconhecida do servidor.");
                socket.close();
                return;
            }
    
            //menu 
            System.out.println("Comandos disponíveis:");
            System.out.println("CREATE <hm>");
            System.out.println("ADD <user1> <hm> <s>");
            System.out.println("RD <hm> <s>");
            System.out.println("EC <hm> <d> <int>");
            System.out.println("RT <hm>");
            System.out.println("RH <hm> <d>");
            System.out.println("OUT");

            Scanner sc = new Scanner(System.in);
    
            while (true) {
    
                System.out.print("> ");
                String line = sc.nextLine();
    
                //verifica se input está vazio 
                if (line.trim().isEmpty()) {
                    System.out.println("Comando inválido.");
                    continue;
                }

                String[] parts = line.split(" ");
    
                //verifica se o input é um comando válido
                Command cmd;
                try {
                    cmd = Command.valueOf(parts[0]);
                } catch (IllegalArgumentException e) {
                    System.out.println("Comando inválido.");
                    continue;
                }
    
                Message msg = new Message(cmd, line);
    
                out.writeObject(msg);
                out.flush();
    
                Message response = (Message) in.readObject();
    
                System.out.println(response.getCommand() + " " + response.getData());
    
                if (cmd == Command.OUT)
                    break;
    
            }
    
            sc.close();
            socket.close();

        } catch (Exception e) {
            System.out.println(e.getMessage()); 
        }
    }

}