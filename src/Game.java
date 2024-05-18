import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Game {

    private List<Socket> userSockets;
    public static Player player1;
    public static Player player2;
    public static CheckersBoard checkersBoard;
    public static Scanner scanner;

    Game(Client client1, Client client2) {
        player1 = new Player(client1);
        player2 = new Player(client2);
        player1.setBlack(true);
        player2.setBlack(false);
    }

    public static void main(String[] args){

    }

    public void start() {
        scanner = new Scanner(System.in);
        checkersBoard = new CheckersBoard();

        Player currPlayer;

        // Game loop
        while (true) {
            //checkersBoard.printBoard();

            if (isGameOver(player1, player2)) {
                break;
            }

            currPlayer = player1.getTurn() ? player1 : player2;

            List<Integer> moves = askMove(currPlayer);

            // Update the board
            checkersBoard.movePiece(moves.get(0), moves.get(1), moves.get(2), moves.get(3));

            // Turn change
            turnChange();
        }
    }

    public static void turnChange(){
        // Change the turn
        player1.setTurn(!player1.getTurn());
        player2.setTurn(!player2.getTurn());
    }

    public static List askMove(Player player){
        List<Integer> move = new ArrayList<>();
        // Ask the player to make a move
        while(true) {
            // Get the move from the player
            System.out.println(player.getUsername() + ", piece's starting row: ");
            int startRow = scanner.nextInt();
            if(startRow < 0 || startRow >= checkersBoard.SIZE - 1){
                System.out.println("Invalid row, try again.");
                continue;
            }
            System.out.println(player.getUsername() + ", piece's starting column: ");
            int startCol = scanner.nextInt();
            if(startCol < 0 || startCol >= checkersBoard.SIZE - 1){
                System.out.println("Invalid column, try again.");
                continue;
            }
            System.out.println(player.getUsername() + ", piece's ending row: ");
            int endRow = scanner.nextInt();
            if(endRow < 0 || endRow >= checkersBoard.SIZE - 1){
                System.out.println("Invalid row, try again.");
                continue;
            }
            System.out.println(player.getUsername() + ", piece's ending column: ");
            int endCol = scanner.nextInt();
            if(endCol < 0 || endCol >= checkersBoard.SIZE - 1){
                System.out.println("Invalid column, try again.");
                continue;
            }
            // Check if the move is valid
            if (checkMove(player, startRow, startCol, endRow, endCol)) {
                move.add(startRow);
                move.add(startCol);
                move.add(endRow);
                move.add(endCol);
                break;
            }

            System.out.println("Invalid move, try again.");
        }

        return move;
    }

    public static boolean checkCorrectPiece(Player player, int startRow, int startCol){
        // Check if the player is moving their own piece
        if(player.getIsBlack() && checkersBoard.board[startRow][startCol] != checkersBoard.BLACK_PIECE && checkersBoard.board[startRow][startCol] != checkersBoard.BLACK_KING){
            return false;
        }
        else if(!player.getIsBlack() && checkersBoard.board[startRow][startCol] != checkersBoard.WHITE_PIECE && checkersBoard.board[startRow][startCol] != checkersBoard.WHITE_KING){
            return false;
        }
        return true;
    }

    // TODO - Implement any move for King pieces
    public static boolean checkIfMoveIsValid(Player player, int startRow, int startCol, int endRow, int endCol){
        // Check if the move is diagonal
        var startPiece = checkersBoard.getPiece(startRow, startCol);
        int rowDiff = endRow - startRow;
        int colDiff = endCol - startCol;
        if(checkersBoard.board[endRow][endCol]!=checkersBoard.INVALID_SQUARE && (rowDiff == colDiff || rowDiff == -colDiff)){
            if((startPiece == checkersBoard.BLACK_PIECE && endRow < startRow) || (startPiece == checkersBoard.WHITE_PIECE && endRow > startRow)){
                return false;
            }
            switch(Math.abs(rowDiff)){
                case 1:
                    return true;
                case 2:
                    // check if the piece is jumping over another piece
                    char piece = checkersBoard.getPiece((startRow + endRow) / 2, (startCol + endCol) / 2);
                    if(piece != checkersBoard.EMPTY_SQUARE){
                        // check if it's the opponent's piece
                        if(player.isBlack){
                            if(piece != checkersBoard.WHITE_PIECE && piece != checkersBoard.WHITE_KING){
                                return false;
                            }
                            checkersBoard.clear((startRow + endRow) / 2, (startCol + endCol) / 2);
                            return true;
                        }
                        else{
                            if(piece != checkersBoard.BLACK_PIECE && piece != checkersBoard.BLACK_KING){
                                return false;
                            }
                            checkersBoard.clear((startRow + endRow) / 2, (startCol + endCol) / 2);
                            return true;
                        }
                    }
                case 4:
                    if(checkDoubleJump(player, startRow, startCol, endRow, endCol)){
                        return true;
                    }
                    return false;
                default:
                    if(startPiece == checkersBoard.BLACK_KING || startPiece == checkersBoard.WHITE_KING){
                        return true;
                    }
                    return false;
            }
        }
        return false;
    }

    public static boolean checkDoubleJump(Player player, int startRow, int startCol, int endRow, int endCol){
        // Check if the player can make a double jump
        int midRow = (startRow + endRow) / 2;
        int midCol = (startCol + endCol) / 2;
        char piece1 = checkersBoard.getPiece((startRow + midRow) / 2, (startCol + midCol) / 2);
        char piece2 = checkersBoard.getPiece((midRow + endRow) / 2, (midCol + endCol) / 2);
        if(piece1 != checkersBoard.EMPTY_SQUARE && piece2 != checkersBoard.EMPTY_SQUARE){
            // check if it's the opponent's piece
            if (player.isBlack) {
                if (piece1 != checkersBoard.WHITE_PIECE && piece1 != checkersBoard.WHITE_KING && piece2 != checkersBoard.WHITE_PIECE && piece2 != checkersBoard.WHITE_KING) {
                    return false;
                }
                checkersBoard.clear((startRow + midRow) / 2, (startCol + midCol) / 2);
                checkersBoard.clear((midRow + endRow) / 2, (midCol + endCol) / 2);
                return true;
            }
            else {
                if (piece1 != checkersBoard.BLACK_PIECE && piece1 != checkersBoard.BLACK_KING && piece2 != checkersBoard.BLACK_PIECE && piece2 != checkersBoard.BLACK_KING) {
                    return false;
                }
                checkersBoard.clear((startRow + midRow) / 2, (startCol + midCol) / 2);
                checkersBoard.clear((midRow + endRow) / 2, (midCol + endCol) / 2);
                return true;
            }
        }

        return false;
    }

    public static boolean checkMove(Player player, int startRow, int startCol, int endRow, int endCol){
        // Check if the move is valid
        if(checkCorrectPiece(player, startRow, startCol) && checkIfMoveIsValid(player, startRow, startCol, endRow, endCol)){
            return true;
        }
        return false;
    }

    public static boolean checkMove2(Player player, int startRow, int startCol, int endRow, int endCol){
        // Check if the move is valid
        if(checkCorrectPiece(player, startRow, startCol) && checkIfMoveIsValid2(player, startRow, startCol, endRow, endCol)){
            return true;
        }
        return false;
    }

    public static boolean checkIfMoveIsValid2(Player player, int startRow, int startCol, int endRow, int endCol){
        // Check if the move is diagonal
        var startPiece = checkersBoard.getPiece(startRow, startCol);
        int rowDiff = endRow - startRow;
        int colDiff = endCol - startCol;
        if(checkersBoard.board[endRow][endCol]!=checkersBoard.INVALID_SQUARE && (rowDiff == colDiff || rowDiff == -colDiff)){
            if((startPiece == checkersBoard.BLACK_PIECE && endRow < startRow) || (startPiece == checkersBoard.WHITE_PIECE && endRow > startRow)){
                return false;
            }
            switch(Math.abs(rowDiff)){
                case 1:
                    return true;
                case 2:
                    // check if the piece is jumping over another piece
                    char piece = checkersBoard.getPiece((startRow + endRow) / 2, (startCol + endCol) / 2);
                    if(piece != checkersBoard.EMPTY_SQUARE){
                        // check if it's the opponent's piece
                        if(player.isBlack){
                            if(piece != checkersBoard.WHITE_PIECE && piece != checkersBoard.WHITE_KING){
                                return false;
                            }
                            return true;
                        }
                        else{
                            if(piece != checkersBoard.BLACK_PIECE && piece != checkersBoard.BLACK_KING){
                                return false;
                            }
                            return true;
                        }
                    }
                case 4:
                    if(checkDoubleJump2(player, startRow, startCol, endRow, endCol)){
                        return true;
                    }
                    return false;
                default:
                    if(startPiece == checkersBoard.BLACK_KING || startPiece == checkersBoard.WHITE_KING){
                        return true;
                    }
                    return false;
            }
        }
        return false;
    }

    public static boolean checkDoubleJump2(Player player, int startRow, int startCol, int endRow, int endCol){
        // Check if the player can make a double jump
        int midRow = (startRow + endRow) / 2;
        int midCol = (startCol + endCol) / 2;
        char piece1 = checkersBoard.getPiece((startRow + midRow) / 2, (startCol + midCol) / 2);
        char piece2 = checkersBoard.getPiece((midRow + endRow) / 2, (midCol + endCol) / 2);
        if(piece1 != checkersBoard.EMPTY_SQUARE && piece2 != checkersBoard.EMPTY_SQUARE){
            // check if it's the opponent's piece
            if (player.isBlack) {
                if (piece1 != checkersBoard.WHITE_PIECE && piece1 != checkersBoard.WHITE_KING && piece2 != checkersBoard.WHITE_PIECE && piece2 != checkersBoard.WHITE_KING) {
                    return false;
                }
                return true;
            }
            else {
                if (piece1 != checkersBoard.BLACK_PIECE && piece1 != checkersBoard.BLACK_KING && piece2 != checkersBoard.BLACK_PIECE && piece2 != checkersBoard.BLACK_KING) {
                    return false;
                }
                return true;
            }
        }

        return false;
    }



    public static List<List<Integer>> playerMoves(Player player){
        // Check all possible moves for the player
        List<List<Integer>> pieceCoords = new ArrayList<>();
        List<List<Integer>> moves = new ArrayList<>();
        if(player.isBlack){
            pieceCoords = checkersBoard.getPieces(checkersBoard.BLACK_PIECE);
        }
        else{
            pieceCoords = checkersBoard.getPieces(checkersBoard.WHITE_PIECE);
        }

        for(var piece: pieceCoords){
            int startRow = piece.get(0);
            int startCol = piece.get(1);

            for(int i = 0; i < checkersBoard.board.length; i++){
                for(int j = 0; j < checkersBoard.board[i].length; j++){
                    if(checkMove2(player, startRow, startCol, i, j)){
                        List<Integer> move = new ArrayList<>();
                        move.add(startRow);
                        move.add(startCol);
                        move.add(i);
                        move.add(j);
                        moves.add(move);
                    }
                }
            }
        }

        return moves;
    }

    public static boolean isGameOver(Player player1, Player player2){
        // Check if the game is over
        if(player1.countPieces() == 0 || player2.countPieces() == 0){
            System.out.println("Game Over, " + (player1.countPieces() == 0 ? player2.getUsername() : player1.getUsername()) + " wins!");
            return true;
        }

        // Check if both players have no moves
        if(playerMoves(player1).size() == 0 && playerMoves(player2).size() == 0){
            System.out.println("Game Over, it's a draw!");
            return true;
        }

        // Check if a player has moves
        if(playerMoves(player1).size() == 0 || playerMoves(player2).size() == 0){
            System.out.println("Game Over, " + (playerMoves(player1).size() == 0 ? player2.getUsername() : player1.getUsername()) + " wins!");
            return true;
        }

        return false;
    }

    static class Player {
        private final String username;
        private boolean isBlack;
        private boolean isTurn;
        private Socket socket;

        Player(Client client){
            client.setInGame(true);
            this.username = client.getUsername();
            this.isTurn = false;
            this.socket = client.getSocket();
        }

        public String getUsername() {
            return this.username;
        }

        public void setTurn(boolean isTurn) {
            this.isTurn = isTurn;
        }

        public boolean getTurn() {
            return this.isTurn;
        }

        public void setBlack(boolean isBlack) {
            this.isBlack = isBlack;
            this.isTurn = isBlack;
        }

        /*public SocketChannel getSocket() {
            return this.socketchannel;
        }*/

        public boolean getIsBlack() {
            return this.isBlack;
        }

        public int countPieces(){
            // Count the number of pieces the player has
            if(this.isBlack){
                return checkersBoard.count(checkersBoard.BLACK_PIECE) + checkersBoard.count(checkersBoard.BLACK_KING);
            }

            return checkersBoard.count(checkersBoard.WHITE_PIECE) + checkersBoard.count(checkersBoard.WHITE_KING);
        }
    }
}
