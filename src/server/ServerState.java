package server;

import model.User;
import model.Casa;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.time.*;
import java.time.format.DateTimeFormatter;;

public class ServerState {

    private final String HOUSES_FILE = "data/houses.txt";
    private final String USERS_FILE = "data/users.txt";
    private final String ATTESTATION_FILE = "data/attestation.txt";

    private Map<String, User> users = new HashMap<>();
    private Map<String, Casa> casas = new HashMap<>();

    public ServerState(){
        loadHouses();
        loadUsers();
        loadAttestations();
    }
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
        if (!userExists(username)) {
            users.put(username, new User(username, password));
            return "OK-NEW-USER";
        }

        if (users.get(username).getPassword().equals(password)) {
            return "OK-USER";
        }

        return "WRONG-PWD";
    }

    public synchronized boolean userExists(String username) {
        return users.containsKey(username);
    }

    public synchronized boolean createCasa(String houseName, String owner) {
        if (casas.containsKey(houseName)) {
            return false;
        }

        addCasa(new Casa(houseName, owner));
        return true;
    }

    public synchronized Casa getCasa(String houseName) {
        return casas.get(houseName);
    } 

    public synchronized void addCasa(Casa casa) {
        casas.put(casa.getName(), casa);
    }

    public synchronized boolean houseExists(String name){
        return casas.containsKey(name);
    }
    
    public synchronized String addPermission(String requester, String targetUser, String houseName, String section) {
        loadUsers();
        if (!casas.containsKey(houseName)) {
            return "NOHM";
        }

        if (!users.containsKey(targetUser)) {
            return "NOUSER";
        }

        Casa casa = casas.get(houseName);

        if (!casa.getOwner().equals(requester)) {
            return "NOPERM";
        }

        casa.addPermission(targetUser, section);
        return "OK";
    }

    public synchronized File getHouseFile(String user, String houseName){
        if (!casas.containsKey(houseName)) {
            return null;
        }
        Casa casa = casas.get(houseName);
         if(!casa.getOwner().equals(user) && !casa.hasPermission(user)){
             return null;
         }
         return new File ("data/" + houseName + "_state.txt");
    }

    public synchronized File getDeviceLogFile(String user, String houseName, String deviceId){
        if(!casas.containsKey(houseName)) {
            return null;
        }
        Casa casa = casas.get(houseName);

        if(!casa.getOwner().equals(user) && !casa.hasPermissionForDevice(user, deviceId)){
            return null;
        }
        return new File ("data/" + houseName + "_" + deviceId + "_log.csv");
    }

    private synchronized void loadHouses() {
        File file = new File(HOUSES_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length < 4) continue;
                String name = parts[0].trim();
                String owner = parts[1].trim();
                String permsData = parts[2].trim();
                String countersData = parts[3].trim();

                Casa casa = new Casa(name, owner);
                if (!permsData.equals("{}") && !permsData.isEmpty()){
                    parsePermissions(casa, permsData);
                }

                parseCounters(casa, countersData);
                casas.put(name, casa);
            }
        }catch (IOException e){
            System.out.println("Error loading houses: " + e.getMessage());
        }
    }

    private void parsePermissions(Casa casa, String data){
        //data format: user1:E,G|user2:L
        String[] userEntries = data.split("\\|");
        for (String entry : userEntries) {
            String[] userAndPerms = entry.split(":");
            if (userAndPerms.length == 2){
                String user = userAndPerms[0];
                String[] perms = userAndPerms[1].split(",");
                for (String p : perms) {
                    casa.addPermission(user, p);
                }
            }
        }
    }

    private void parseCounters(Casa casa, String data){
        // data format E:1,G:2,L:1,M:1,P:1,S:1
        String[] sections = data.split(",");
        for (String s : sections){
            String[] pair = s.split(":");
            if (pair.length == 2){
                casa.setCounter(pair[0], Integer.parseInt(pair[1]));
            }
        }
    }

    private void loadUsers(){
        File file = new File(USERS_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            while ((line = br.readLine()) != null) {
                line=line.trim();
                if(line.isEmpty()) continue;

                String[] parts = line.split(":");
                if(parts.length >=2){
                users.put(parts[0], new User(parts[0], parts[1]));
                }else{
                    System.out.println("Lina ignorada em users.txt, formato inválido: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    public synchronized String registerDevice(String requester, String houseName, String section){
        if (!casas.containsKey(houseName)) {
            return "NOHM";
        }

        Casa casa = casas.get(houseName);

        if (!casa.getOwner().equals(requester)) {
            return "NOPERM";
        }

        String deviceId = casa.incrementSectionCounter(section);
        casa.updateDeviceState(deviceId, 0);
        
        try{
            File logFile = new File("data/" + houseName + "_" + deviceId + "_log.csv");
            if(!logFile.exists()){
            logFile.createNewFile();
            }
        }catch (IOException e){
            System.out.println("Error creating log file: " + e.getMessage());
            return "ERROR";
        }
        saveHouses();
        System.out.println("Device " + deviceId + " registered in house " + houseName);
        return "OK";
    }

    public synchronized String getHouseSummary(String user, String houseName){
        if (!casas.containsKey(houseName)) return "NOHM";

        Casa casa = casas.get(houseName);

        if (!casa.getOwner().equals(user) && !casa.hasPermission(user)){
            return "NOPERM";
        }

        Map<String, Integer> states = casa.getDeviceStates();
        if(states.isEmpty()){
            return "No devices registered in this house.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("House: ").append(casa.getName()).append("\n");
        sb.append("Owner: ").append(casa.getOwner()).append("\n");
        sb.append("Devices:\n");
        for (String deviceId : casa.getDeviceStates().keySet()){
            sb.append(" - ").append(deviceId).append(": ").append(casa.getDeviceStates().get(deviceId)).append("\n");
        }
        return sb.toString();   
    }

    private synchronized void saveHouses(){
        try (PrintWriter pw = new PrintWriter(new FileWriter(HOUSES_FILE))){
            for (Casa c : casas.values()){
                pw.println(c.toPersistenceString());
            }
        } catch (IOException e) {
            System.out.println("Error saving houses: " + e.getMessage());
        }
    }

    private void loadAttestations(){
        // Implementar se necessário para a fase 2
    }

    public synchronized void saveData(){
        try (PrintWriter pw = new PrintWriter(new FileWriter(HOUSES_FILE))){
            for (Casa c : casas.values()){
                pw.println(c.toString());
            }
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    public synchronized String executeCommand (String user, String houseName, String deviceId, int value){
        if (!casas.containsKey(houseName)) return "NOHM";
        
        Casa casa = casas.get(houseName);
    
        if (!casa.hasDevice(deviceId)) return "NOD";

        if (!casa.hasPermissionForDevice(user, deviceId)) return "NOPERM";
        
        if (value < 0 || value > 600) return "NOK";
        
        writeToDeviceLog(houseName, deviceId, value);
        updateHouseState(houseName, deviceId, value);

        casa.updateDeviceState(deviceId, value);
        return "OK";
    }

    private void writeToDeviceLog(String houseName, String deviceId, int value){
        String logFileName = "data/" + houseName + "_" + deviceId + "_log.csv";
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (PrintWriter pw = new PrintWriter(new FileWriter(logFileName, true))){
            pw.println(timeStamp + "," + deviceId + "," + value);
        } catch (IOException e) {
            System.out.println("Error writing to device log: " + e.getMessage());
        }
    }

    private void updateHouseState(String houseName, String deviceId, int value){
        File stateFile = new File("data/" + houseName + "_state.txt");
        Map<String, String> deviceStates = new HashMap<>();

        if (stateFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(stateFile))){
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        deviceStates.put(parts[0], parts[1]);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading house state: " + e.getMessage());
            }
        }

        deviceStates.put(deviceId, String.valueOf(value));

        try (PrintWriter pw = new PrintWriter(new FileWriter(stateFile))){
            for (Map.Entry<String, String> entry : deviceStates.entrySet()){
                pw.println(entry.getKey() + ":" + entry.getValue());
            }
        } catch (IOException e) {
            System.out.println("Error saving house state: " + e.getMessage());
        }
    }
}