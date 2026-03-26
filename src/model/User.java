package model;

import java.io.Serializable;

/**
 * Representa um utilizador do sistema.
 */
public class User implements Serializable {

    private String username;
    private String password;

    /**
     * Cria um novo utilizador com as credenciais fornecidas.
     *
     * @param username nome do utilizador
     * @param password palavra-passe do utilizador
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Devolve o nome do utilizador.
     *
     * @return nome do utilizador
     */
    public String getUsername() {
        return username;
    }

    /**
     * Devolve a palavra-passe do utilizador.
     *
     * @return palavra-passe do utilizador
     */
    public String getPassword() {
        return password;
    }

}
