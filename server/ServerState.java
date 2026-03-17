package server;

import model.User;
import model.Casa;

import java.util.HashMap;
import java.util.Map;

public class ServerState {

    private Map<String, User> users = new HashMap<>();
    private Map<String, Casa> casas = new HashMap<>();

    public synchronized void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    public synchronized boolean authenticate(String username, String password) {
        if (!users.containsKey(username))
            return false;

        return users.get(username).getPassword().equals(password);
    }
    
    //faz autenticação e regista o utilizador se for novo
    public synchronized String login(String username, String password) {
        if (!users.containsKey(username)) {
            users.put(username, new User(username, password));
            return "OK-NEW-USER";
        }

        if (users.get(username).getPassword().equals(password)) {
            return "OK-USER";
        }

        return "WRONG-PWD";
    }

    public synchronized void addCasa(Casa casa) {
        casas.put(casa.getName(), casa);
    }

}