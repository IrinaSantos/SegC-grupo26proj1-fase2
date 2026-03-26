package model;

import java.io.Serializable;

/**
 * Representa um dispositivo associado a uma casa.
 */
public class Device implements Serializable {

    private String id;
    private String type;
    private String value;

    /**
     * Cria um dispositivo com ID e tipo.
     *
     * @param id ID do dispositivo
     * @param type tipo do dispositivo
     */
    public Device(String id, String type) {
        this.id = id;
        this.type = type;
    }

    /**
     * Atualiza o valor atual do dispositivo.
     *
     * @param value novo valor do dispositivo
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Devolve o ID do dispositivo.
     *
     * @return ID do dispositivo
     */
    public String getId() {
        return id;
    }

    /**
     * Devolve o tipo do dispositivo.
     *
     * @return tipo do dispositivo
     */
    public String getType() {
        return type;
    }

    /**
     * Devolve o valor atual do dispositivo.
     *
     * @return valor atual
     */
    public String getValue() {
        return value;
    }

}
