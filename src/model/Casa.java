package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Casa implements Serializable {

    private String name;
    private String owner;
    private List<Device> devices = new ArrayList<>();
    private Map<String, List<String>> permissions = new HashMap<>();
    private Map<String, Integer> sectionCounters = new HashMap<>();

    public Casa(String name, String owner) {
        this.name = name;
        this.owner = owner;
        //iniciar os contadores a 1
        sectionCounters.put("E", 1); 
        sectionCounters.put("G", 1);
        sectionCounters.put("L", 1); 
        sectionCounters.put("M", 1);
        sectionCounters.put("P", 1); 
        sectionCounters.put("S", 1);
    }

    public void addDevice(Device device) {
        devices.add(device);
    }

    public void addPermission(String username, String section) {
        permissions.putIfAbsent(username, new ArrayList<>());
        permissions.get(username).add(section);
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public Map<String, List<String>> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(String user){
        return owner.equals(user) || permissions.containsKey(user);
    }

    public boolean hasPermissionForSection(String user, String section){
        if (owner.equals(user)) return true;

        List<String> userPerms = permissions.get(user);
        if(userPerms == null) return false;
        return userPerms.contains("all") || userPerms.contains(section);
    }
    
    public boolean hasPermissionForDevice(String user, String deviceId) {
        if (owner.equals(user)) return true;

        if (deviceId == null || deviceId.isEmpty()) return false;
        String section = deviceId.substring(0, 1);
        return hasPermissionForSection(user, section);
    }    

    public synchronized String incrementSectionCounter(String section) {
        if (!sectionCounters.containsKey(section)) {
            return null;
        }
        int currentCounter = sectionCounters.get(section);
        String deviceId = section + currentCounter;
        sectionCounters.put(section, currentCounter + 1);
        return deviceId;
    }

    public void setCounter(String section, int value) {
        sectionCounters.put(section, value);
    }

    public String toPersistenceString(){
        return name + ";" + owner + ";" + permissions.toString() + ";" + sectionCounters.toString();
    }
}