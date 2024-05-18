import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This program demonstrates a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */
public class TimeClient {

    // Method to hash the password using SHA-256
    public static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(password.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static boolean authenticate(BufferedReader reader, PrintWriter writer) throws IOException {
        // Prompt user for username and password
        System.out.println("Enter username:");
        String username = System.console().readLine();
        System.out.println("Enter password:");
        String password = new String(System.console().readPassword());

        // Send username and password to server
        writer.println(username);
        try {
            writer.println(hashPassword(password));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Receive authentication response from server
        String response = reader.readLine();

        // Check authentication response
        return Boolean.parseBoolean(response);
    }

    public static boolean clientGUI(Socket socket, String hostname, int port) {
    while (true) {
        try {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            // Show game menu options
            System.out.println("Game Menu:");
            System.out.println("1. Play");
            System.out.println("2. Quit");

            // Read user input for menu selection
            System.out.println("Enter your choice (1, or 2):");
            String choice = System.console().readLine();

            switch (choice) {
                case "1":
                    // Player selected to play
                    writer.println("join_queue");
                    break;
                case "2":
                    // Player selected to quit
                    writer.println("quit");
                    break;
                default:
                    System.out.println("Invalid choice!");
                    continue; // Continue to show menu
            }

            // Wait for response from server
            String response = reader.readLine();
            System.out.println("Server response: " + response);

        } catch (IOException e) {
            System.out.println("Error handling client connection: " + e.getMessage());
            break; // Exit the loop and close the connection
        }
    }
    return true;
}

    public static void main(String[] args) {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostname, port)) {
            while(true) {
                // Send a request to the server

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                if(authenticate(reader, writer)){
                    System.out.println("Authentication successful.");
                    if(!clientGUI(socket, hostname, port)){
                        break;
                    }
                    break;
                }
                else {
                    System.out.println("Authentication failed. Please try again.");
                }

            }

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}