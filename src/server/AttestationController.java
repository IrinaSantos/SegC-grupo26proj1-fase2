package server;
import java.io.*;
import java.util.Scanner;

public class AttestationController {

    private static final String ATTESTATION_FILE = "data/attestation.txt";

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

    public boolean registerAttestation(String appName, long appSize) {
        try (FileWriter writer = new FileWriter(ATTESTATION_FILE, true)) {
            writer.write(appName + ":" + appSize + "\n");
            return true;
        } catch (IOException e) {
            System.out.println("Error writing to attestation file: " + e.getMessage());
            return false;
        }
    }

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