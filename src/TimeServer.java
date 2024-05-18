import java.io.*;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

public class TimeServer {

    private final int port;
    private final int mode;
    private final List<Client> clients;
    private final Queue<Socket> queue;
    private ServerSocketChannel serverSocket;

    public TimeServer(int port, int mode) {
        this.port = port;
        this.mode = mode;
        this.clients = CSVReader.read();
        this.queue = new LinkedList<>();
    }

    public void start() throws IOException {
        this.serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));
        System.out.println("Server is listening on port " + port + " with " + (mode == 1 ? "rank" : "simple") + " mode");
    }

    public void run() {
        while (true) {
            try {
                // Accept incoming client connection
                Socket clientSocket = serverSocket.accept().socket();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostName());

                // Handle client connection in a new thread
                new Thread(() -> {
                    try {
                        handleClientConnection(clientSocket);
                    } catch (IOException e) {
                        System.out.println("Error handling client connection: " + e.getMessage());
                    } catch (NullPointerException e) {
                        System.out.println("Client disconnected");
                    }
                }).start();

            } catch (IOException e) {
                System.out.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }

    private void handleClientConnection(Socket clientSocket) throws IOException {
        // Create BufferedReader and PrintWriter for client communication
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        // Read username and hashed password from the client
        String username = in.readLine();
        String hashedPassword = in.readLine();

        // Authenticate the user
        boolean isAuthenticated = authenticateUser(username, hashedPassword, clientSocket);

        // Send authentication result back to the client
        out.println(isAuthenticated);

        if (isAuthenticated) {
            System.out.println("User " + username + " authenticated successfully.");
            handleClientRequest(clientSocket);
        }
    }

    // Method to handle client request
    private boolean handleClientRequest(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);


        while (true) {
            // Read client request
            String request = in.readLine();
            if (request == null) {
                break;
            }

            // Process client request
            switch (request) {
                case "join_queue":  
                    synchronized (queue) {
                        queue.add(clientSocket);
                        checkQueue();
                    }
                    break;
                case "quit":
                    out.println("Goodbye!");
                    clientSocket.close();
                    return false;
                default:
                    out.println("Invalid command");
                    break;
            }
        }
        return true;
    }

    public void checkQueue(){
        
        if(queue.size() >= 2){
            Socket player1 = queue.poll();
            Socket player2 = queue.poll();
            if(player1 != player2){
                queue.clear();
                startGame(player1, player2);
            } else{
                queue.remove(player2);
            }

        }
    }

    public void startGame(Socket player1Socket, Socket player2Socket) {
        new Thread(() -> {

                try {
                BufferedReader in1 = new BufferedReader(new InputStreamReader(player1Socket.getInputStream()));
                PrintWriter out1 = new PrintWriter(player1Socket.getOutputStream(), true);

                BufferedReader in2 = new BufferedReader(new InputStreamReader(player2Socket.getInputStream()));
                PrintWriter out2 = new PrintWriter(player2Socket.getOutputStream(), true);
                
                Client player1 = findClientBySocket(player1Socket);
                Client player2 = findClientBySocket(player2Socket);
                if (player1 != null && player2 != null) {
                    Game game = new Game(player1, player2);
                    game.checkersBoard = new CheckersBoard();
                    
                    System.out.print("Game started between " + player1.getUsername() + " and " + player2.getUsername() + "\n");
                    Game.Player currPlayer;

                    while(!game.isGameOver(game.player1, game.player2)){
                        currPlayer= game.player1.getTurn() ? game.player1 : game.player2;

                        game.checkersBoard.printBoard(out1);
                        game.checkersBoard.printBoard(out2);

                        List<Integer> moves = game.askMove(currPlayer);
                        game.checkersBoard.movePiece(moves.get(0), moves.get(1), moves.get(2), moves.get(3));
                        game.turnChange();                        
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Client findClientBySocket(Socket socket) {
        for (Client client : clients) {
            if (client.getSocket().equals(socket)) {
                return client;
            }
        }
        return null;
    }

    // Method to add a new user
    private void addUser(String username, String hashedPassword, Socket socket) {
        Client client = new Client(username, hashedPassword, 200, socket, "", true);
        clients.add(client);

        // Write the new user to the file
        try (FileWriter writer = new FileWriter("../doc/users.csv", true);
             BufferedWriter bw = new BufferedWriter(writer)) {

            bw.write(client.getUsername() + "," + client.getPassword() + "," + client.getRank());
            bw.newLine();

        } catch (IOException e) {
            System.out.println("Error writing new user to file: " + e.getMessage());
        }
    }

    // Method to authenticate user
    private boolean authenticateUser(String username, String hashedPassword, Socket socket) {
        for (Client client : clients) {
            if (client.getUsername().equals(username)) {
                if(client.getSignedIn()){
                    return false; // User already signed in
                }
                String storedHashedPassword = client.getPassword();
                if(storedHashedPassword.equals(hashedPassword)){
                    client.setSignedIn(true);
                    client.setSocket(socket);
                    return true;
                }
                else{
                    return false;
                }
            }
        }
        // Username not found, create new user
        addUser(username, hashedPassword, socket);
        return true;
    }

    public static void main(String[] args) {

        // Check if there are enough arguments
        if (args.length < 2) return;

        // Parse port and host arguments and create a Server object
        int port = Integer.parseInt(args[0]);
        int mode = Integer.parseInt(args[1]);

        if (mode != 0 && mode != 1) {
            return;
        }

        // Start the connection
        try {
            TimeServer server = new TimeServer(port, mode);
            server.start();
            server.run();
        } catch (IOException exception) {
            System.out.println("Server exception: " + exception.getMessage());
        }
    }
}
