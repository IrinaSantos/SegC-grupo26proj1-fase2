package client;

import common.Command;
import common.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class SpertaClient {

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
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeObject(userID);
            out.writeObject(pwd);
            out.flush(); //envia user e password para o servidor confirmar

            String resposta = (String) in.readObject(); //recebe a info do servidor

            if(resposta.equals("WRONG-PWD")){
                System.out.println("Password inválida!");

            } else if(resposta.equals("OK-NEW-USER")){

                System.out.println("Novo utilizador Registado!");
            } else if(resposta.equals("OK-USER")){
                
                System.out.println("Bem-Vindo!");
            }
    
            Scanner sc = new Scanner(System.in);
    
            while (true) {
    
                System.out.print("> ");
                String line = sc.nextLine();
    
                String[] parts = line.split(" ");
    
                Command cmd = Command.valueOf(parts[0]);
    
                Message msg = new Message(cmd, line);
    
                out.writeObject(msg);
    
                Message response = (Message) in.readObject();
    
                System.out.println(response.getCommand() + " " + response.getData());
    
                if (cmd == Command.OUT)
                    break;
    
            }
    
            socket.close();

        } catch (Exception e) {
            System.out.println(e.getMessage()); 
        }
    }

}