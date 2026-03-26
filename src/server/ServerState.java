package server;

import model.User;
import model.Casa;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.time.*;
import java.time.format.DateTimeFormatter;;

/**
 * Mantem o estado global do servidor, incluindo utilizadores, casas e dispositivos.
 */
public class ServerState {

    private final String HOUSES_FILE = "data/houses.txt";
    private final String USERS_FILE = "data/users.txt";
    private final String ATTESTATION_FILE = "data/attestation.txt";

    private Map<String, User> users = new HashMap<>();
    private Map<String, Casa> casas = new HashMap<>();

    /**
     * Cria uma nova instância do estado do servidor e carrega os dados persistidos.
     */
    public ServerState(){
        loadHouses();
        loadUsers();
        loadAttestations();
    }

    /**
     * Adiciona um utilizador ao estado em memória.
     *
     * @param user utilizador a adicionar
     */
    public synchronized void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    /**
     * Valida as credenciais de um utilizador existente.
     *
     * @param username nome do utilizador
     * @param password password fornecida
     * @return {@code true} se as credenciais coincidirem; caso contrario, {@code false}
     */
    public synchronized boolean authenticate(String username, String password) {
        if (!users.containsKey(username))
            return false;

        return users.get(username).getPassword().equals(password);
    }
    
    //faz autenticaÃ§Ã£o e regista o utilizador se for novo
    /**
     * Efetua login ou regista automaticamente um novo utilizador.
     *
     * @param username nome do utilizador
     * @param password password fornecida
     * @return codigo textual com o resultado da operacao
     */
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

    /**
     * Verifica se um utilizador existe no estado atual.
     *
     * @param username nome do utilizador
     * @return {@code true} se o utilizador existir; caso contrario, {@code false}
     */
    public synchronized boolean userExists(String username) {
        return users.containsKey(username);
    }

    /**
     * Cria uma nova casa para um determinado dono.
     *
     * @param houseName nome da casa
     * @param owner utilizador dono da casa
     * @return {@code true} se a casa foi criada; caso contrario, {@code false}
     */
    public synchronized boolean createCasa(String houseName, String owner) {
        if (casas.containsKey(houseName)) {
            return false;
        }

        addCasa(new Casa(houseName, owner));
        return true;
    }

    /**
     * Devolve a casa associada ao nome fornecido.
     *
     * @param houseName nome da casa
     * @return instância da casa ou {@code null} se nao existir
     */
    public synchronized Casa getCasa(String houseName) {
        return casas.get(houseName);
    } 

    /**
     * Adiciona uma casa ao estado em memória.
     *
     * @param casa casa a adicionar
     */
    public synchronized void addCasa(Casa casa) {
        casas.put(casa.getName(), casa);
    }

    /**
     * Verifica se uma casa existe.
     *
     * @param name nome da casa
     * @return {@code true} se a casa existir; caso contrario, {@code false}
     */
    public synchronized boolean houseExists(String name){
        return casas.containsKey(name);
    }
    
    /**
     * Concede permissao de acesso a uma secao da casa a um utilizador.
     *
     * @param requester utilizador que faz o pedido
     * @param targetUser utilizador que recebe a permissao
     * @param houseName nome da casa
     * @param section secao autorizada
     * @return codigo textual com o resultado da operacao
     */
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

    /**
     * Obtém o ficheiro com o estado atual de uma casa, se o utilizador tiver acesso.
     *
     * @param user utilizador autenticado
     * @param houseName nome da casa
     * @return ficheiro de estado da casa ou {@code null} se o acesso for negado ou a casa nao existir
     */
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

    /**
     * Obtém o ficheiro de log de um dispositivo, se o utilizador tiver permissao.
     *
     * @param user utilizador autenticado
     * @param houseName nome da casa
     * @param deviceId identificador do dispositivo
     * @return ficheiro de log ou {@code null} se o acesso for negado ou a casa nao existir
     */
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

    /**
     * Carrega as casas persistidas no armazenamento local.
     */
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

    /**
     * Interpreta a representacao textual das permissoes e aplica-a a uma casa.
     *
     * @param casa casa a atualizar
     * @param data dados serializados das permissoes
     */
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

    /**
     * Interpreta e carrega os contadores de secoes de uma casa.
     *
     * @param casa casa a atualizar
     * @param data dados serializados dos contadores
     */
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

    /**
     * Carrega os utilizadores persistidos no armazenamento local.
     */
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
                    System.out.println("Lina ignorada em users.txt, formato invÃ¡lido: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    /**
     * Regista um novo dispositivo numa casa e cria o respetivo ficheiro de log.
     *
     * @param requester utilizador que pede o registo
     * @param houseName nome da casa
     * @param section secao onde o dispositivo sera registado
     * @return codigo textual com o resultado da operacao
     */
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

    /**
     * Gera um resumo textual do estado atual de uma casa.
     *
     * @param user utilizador que pede a informacao
     * @param houseName nome da casa
     * @return resumo textual da casa ou codigo textual de erro
     */
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

    /**
     * Persiste a lista de casas no armazenamento local.
     */
    private synchronized void saveHouses(){
        try (PrintWriter pw = new PrintWriter(new FileWriter(HOUSES_FILE))){
            for (Casa c : casas.values()){
                pw.println(c.toPersistenceString());
            }
        } catch (IOException e) {
            System.out.println("Error saving houses: " + e.getMessage());
        }
    }

    /**
     * Carrega os dados de atestacao persistidos.
     */
    private void loadAttestations(){
        // Implementar se necessÃ¡rio para a fase 2
    }

    /**
     * Persiste os dados atuais das casas no armazenamento local.
     */
    public synchronized void saveData(){
        try (PrintWriter pw = new PrintWriter(new FileWriter(HOUSES_FILE))){
            for (Casa c : casas.values()){
                pw.println(c.toString());
            }
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    /**
     * Executa um comando sobre um dispositivo, validando acessos e intervalo de valores.
     *
     * @param user utilizador autenticado
     * @param houseName nome da casa
     * @param deviceId identificador do dispositivo
     * @param value valor a aplicar ao dispositivo
     * @return codigo textual com o resultado da operacao
     */
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

    /**
     * Regista no ficheiro de log o valor enviado para um dispositivo.
     *
     * @param houseName nome da casa
     * @param deviceId identificador do dispositivo
     * @param value valor registado
     */
    private void writeToDeviceLog(String houseName, String deviceId, int value){
        String logFileName = "data/" + houseName + "_" + deviceId + "_log.csv";
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (PrintWriter pw = new PrintWriter(new FileWriter(logFileName, true))){
            pw.println(timeStamp + "," + deviceId + "," + value);
        } catch (IOException e) {
            System.out.println("Error writing to device log: " + e.getMessage());
        }
    }

    /**
     * Atualiza o ficheiro com o estado atual dos dispositivos de uma casa.
     *
     * @param houseName nome da casa
     * @param deviceId identificador do dispositivo
     * @param value novo valor do dispositivo
     */
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
