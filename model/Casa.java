package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Casa implements Serializable {

    private String name;
    private List<Device> devices = new ArrayList<>();

    public Casa(String name) {
        this.name = name;
    }

    public void addDevice(Device device) {
        devices.add(device);
    }

    public String getName() {
        return name;
    }

    public List<Device> getDevices() {
        return devices;
    }

}