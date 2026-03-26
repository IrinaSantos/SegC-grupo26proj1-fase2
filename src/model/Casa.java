package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Representa uma casa, incluindo dispositivos, permissões e estados.
 */
public class Casa implements Serializable {

    private String name;
    private String owner;
    private List<Device> devices = new ArrayList<>();
    private Map<String, List<String>> permissions = new HashMap<>();
    private Map<String, Integer> sectionCounters = new HashMap<>();
    private Map<String, Integer> deviceStates = new HashMap<>();


    /**
     * Cria uma nova casa com nome e dono associados.
     *
     * @param name nome da casa
     * @param owner dono da casa
     */
    public Casa(String name, String owner) {
        this.name = name;
        this.owner = owner;
    }

    /**
     * Atualiza o estado atual de um dispositivo.
     *
     * @param deviceId ID do dispositivo
     * @param value novo valor do dispositivo
     */
    public void updateDeviceState(String deviceId, int value) {
        deviceStates.put(deviceId, value);
    }

    /**
     * Verifica se a casa contém um dispositivo com o ID indicado.
     *
     * @param deviceId ID do dispositivo
     * @return {@code true} se o dispositivo existir; caso contrario, {@code false}
     */
    public boolean hasDevice(String deviceId) {
        return deviceStates.containsKey(deviceId);
    }

    /**
     * Devolve o mapa de estados dos dispositivos.
     *
     * @return mapa entre ID de dispositivo e valor atual
     */
    public Map<String, Integer> getDeviceStates(){
        return deviceStates;
    }

    /**
     * Adiciona um dispositivo à lista da casa.
     *
     * @param device dispositivo a adicionar
     */
    public void addDevice(Device device) {
        devices.add(device);
    }

    /**
     * Associa uma permissão de secção a um utilizador.
     *
     * @param username utilizador autorizado
     * @param section secção permitida
     */
    public void addPermission(String username, String section) {
        permissions.putIfAbsent(username, new ArrayList<>());
        permissions.get(username).add(section);
    }

    /**
     * Devolve o nome da casa.
     *
     * @return nome da casa
     */
    public String getName() {
        return name;
    }

    /**
     * Devolve o dono da casa.
     *
     * @return nome do dono
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Devolve a lista de dispositivos da casa.
     *
     * @return lista de dispositivos
     */
    public List<Device> getDevices() {
        return devices;
    }

    /**
     * Devolve o mapa de permissões por utilizador.
     *
     * @return mapa entre utilizadores e secoes autorizadas
     */
    public Map<String, List<String>> getPermissions() {
        return permissions;
    }

    /**
     * Verifica se um utilizador tem qualquer acesso à casa.
     *
     * @param user utilizador a validar
     * @return {@code true} se tiver acesso; caso contrario, {@code false}
     */
    public boolean hasPermission(String user){
        return owner.equals(user) || permissions.containsKey(user);
    }

    /**
     * Verifica se um utilizador pode aceder a uma secção específica.
     *
     * @param user utilizador a validar
     * @param section secção a verificar
     * @return {@code true} se o acesso for permitido; caso contrario, {@code false}
     */
    public boolean hasPermissionForSection(String user, String section){
        if (owner.equals(user)) return true;

        List<String> userPerms = permissions.get(user);
        if(userPerms == null) return false;
        return userPerms.contains("all") || userPerms.contains(section);
    }
    
    /**
     * Verifica se um utilizador pode aceder a um dispositivo específico.
     *
     * @param user utilizador a validar
     * @param deviceId ID do dispositivo
     * @return {@code true} se o acesso for permitido; caso contrario, {@code false}
     */
    public boolean hasPermissionForDevice(String user, String deviceId) {
        if (owner.equals(user)) return true;

        if (deviceId == null || deviceId.isEmpty()) return false;
        String section = deviceId.substring(0, 1);
        return hasPermissionForSection(user, section);
    }    

    /**
     * Incrementa o contador da secção e devolve o ID do novo dispositivo.
     *
     * @param section secção onde o dispositivo sera criado
     * @return ID do novo dispositivo
     */
    public synchronized String incrementSectionCounter(String section) {
        section = section.toUpperCase();
        int currentCounter = sectionCounters.getOrDefault(section, 0);
        int nextCounter = currentCounter + 1;
        String deviceId = section + nextCounter;
        sectionCounters.put(section, nextCounter);
        deviceStates.put(deviceId, 0); // Inicializa o estado do novo dispositivo a 0
        return deviceId;
    }

    /**
     * Define manualmente o valor do contador de uma secção.
     *
     * @param section secção a atualizar
     * @param value valor do contador
     */
    public void setCounter(String section, int value) {
        sectionCounters.put(section, value);
    }

    /**
     * Produz a representacao textual usada para persistencia da casa.
     *
     * @return representacao textual persistivel
     */
    public String toPersistenceString(){
        return name + ";" + owner + ";" + permissions.toString() + ";" + sectionCounters.toString();
    }
}
