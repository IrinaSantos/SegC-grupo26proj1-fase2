package server;
import java.io.*;
import java.util.Scanner;

/**
 * Gere os dados usados na validacao de atestação da aplicação cliente.
 */
public class AttestationController {

    private static final String ATTESTATION_FILE = "data/attestation.txt";

    /**
     * Inicializa o controlador e garante a existencia do ficheiro de atestação.
     */
    public AttestationController() {
        File file = new File(ATTESTATION_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Error creating attestation file: " + e.getMessage());
            }
        }
    }

    /**
     * Verifica se uma aplicação corresponde a uma atestação registada.
     *
     * @param appName nome da aplicação
     * @param appSize tamanho do binário esperado
     * @return {@code true} se a atestação for valida; caso contrario, {@code false}
     */
    public boolean verifyAttestation(String appName, long appSize) {
        try (Scanner scanner = new Scanner(new File(ATTESTATION_FILE))) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(":");
                if (parts.length == 2 && parts[0].equals(appName) && Long.parseLong(parts[1]) == appSize) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Attestation file not found: " + e.getMessage());
        }
        return false;
    }

    /**
     * Regista uma nova entrada de atestação.
     *
     * @param appName nome da aplicação
     * @param appSize tamanho do binário
     * @return {@code true} se o registo for concluido; caso contrario, {@code false}
     */
    public boolean registerAttestation(String appName, long appSize) {
        try (FileWriter writer = new FileWriter(ATTESTATION_FILE, true)) {
            writer.write(appName + ":" + appSize + "\n");
            return true;
        } catch (IOException e) {
            System.out.println("Error writing to attestation file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se existe alguma atestação registada para a aplicação indicada.
     *
     * @param appName nome da aplicação
     * @return {@code true} se a aplicação estiver registada; caso contrario, {@code false}
     */
    public boolean isAttestationRegistered(String appName) {
        try (Scanner scanner = new Scanner(new File(ATTESTATION_FILE))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(":");
                if (parts.length >= 1 && parts[0].equals(appName)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Attestation file not found: " + e.getMessage());
        }
        return false;
    }
}
