package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Gere o armazenamento simples de utilizadores em ficheiro.
 */
public class UserController {

    private static final String USERS_FILE = "data/users.txt";
    private File fileUS;

    /**
     * Inicializa o controlador e garante a existência do ficheiro de utilizadores.
     */
    public UserController() {
        this.fileUS = new File(USERS_FILE);
        if (!fileUS.exists()) {
            try {
                fileUS.createNewFile();
            } catch (IOException e) {
                System.out.println("Error creating users file: " + e.getMessage());
            }
        }
    }

    /**
     * Autentica um utilizador com base nas credenciais persistidas.
     *
     * @param username nome do utilizador
     * @param password palavra-passe fornecida
     * @return {@code true} se as credenciais forem validas; caso contrario, {@code false}
     */
    public boolean authenticate(String username, String password) {
        try (Scanner scanner = new Scanner(fileUS)) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(":");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Users file not found: " + e.getMessage());
        }
        return false;
    }

    /**
     * Regista um novo utilizador no ficheiro de utilizadores.
     *
     * @param username nome do utilizador
     * @param password palavra-passe do utilizador
     * @return {@code true} se o registo for concluido; caso contrario, {@code false}
     */
    public boolean registerUser(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return false;
        }
        try (FileWriter writer = new FileWriter(fileUS, true)) {
            writer.write(username + ":" + password + "\n");
            return true;
        } catch (IOException e) {
            System.out.println("Error writing to users file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se um utilizador já se encontra registado.
     *
     * @param username nome do utilizador
     * @return {@code true} se o utilizador existir; caso contrário, {@code false}
     */
    public boolean isUserRegistered(String username) {
        try (Scanner scanner = new Scanner(fileUS)) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(":");
                if (parts.length == 2 && parts[0].equals(username)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Users file not found: " + e.getMessage());
        }
        return false;
    }
}
