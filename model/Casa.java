package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Casa implements Serializable {

    private String name;
    private String owner;
    private List<Device> devices = new ArrayList<>();

    public Casa(String name, String owner) {
        this.name = name;
        this.owner = owner;
    }

    public void addDevice(Device device) {
        devices.add(device);
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

}