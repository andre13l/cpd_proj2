import java.net.Socket;
public class Client {

    private final String username;
    private final String password;
    private int rank;
    private Socket socket;
    private int timeInQueue = 0;
    private boolean inGame;
    private final String token;
    private boolean signedIn;

    public Client(String username, String password, int rank, Socket socket, String token, boolean signedIn) {
        this.username = username;
        this.password = password;
        this.rank = rank;
        this.socket = socket;
        this.inGame = false;
        this.token = token;
        this.signedIn = false;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {return this.password;}

    public int getRank() {
        return this.rank;
    }

    public void incrementRank(int value) {
        this.rank += value;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public boolean getInGame() {
        return this.inGame;
    }

    public boolean getSignedIn() {
        return this.signedIn;
    }

    public void setSignedIn(boolean signedIn) {
        this.signedIn = signedIn;
    }

    public boolean equals(Client client) {
        return this.username.equals(client.getUsername());
    }
}