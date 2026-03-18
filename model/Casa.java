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

    public Casa(String name, String owner) {
        this.name = name;
        this.owner = owner;
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
}