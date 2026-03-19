package model;

import java.io.Serializable;

public class Device implements Serializable {

    private String id;
    private String type;
    private String value;

    public Device(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

}