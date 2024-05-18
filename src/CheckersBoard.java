import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;

public class CheckersBoard {

    public static final int SIZE = 8; // Size of the board (8x8 for standard Checkers)
    public static final char EMPTY_SQUARE = ' '; // Symbol for an empty square
    public static final char INVALID_SQUARE = '\\'; // Symbol for an invalid square
    public static final char BLACK_PIECE = 'B'; // Symbol for player 1's piece
    public static final char WHITE_PIECE = 'W'; // Symbol for player 2's piece
    public static final char BLACK_KING = 'K'; // Symbol for player 1's king piece
    public static final char WHITE_KING = 'Q'; // Symbol for player 2's king piece

    public char[][] board; // 2D array to represent the Checkers board
    public CheckersBoard() {
        this.board = new char[SIZE][SIZE];
        initializeBoard();
    }

    public List<List<Integer>> getPieces(char piece){
        if(piece == BLACK_PIECE){
            return getBlackPieces();
        }
        else if(piece == WHITE_PIECE){
            return getWhitePieces();
        }
        else {
            return null;
        }
    }

    public List<List<Integer>> getBlackPieces(){
        List<List<Integer>> blackPieces = new ArrayList<>();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if(board[row][col] == BLACK_PIECE || board[row][col] == BLACK_KING){
                    List<Integer> piece = new ArrayList<>();
                    piece.add(row);
                    piece.add(col);
                    blackPieces.add(piece);
                }
            }
        }
        return blackPieces;
    }

    public List<List<Integer>> getWhitePieces(){
        List<List<Integer>> whitePieces = new ArrayList<>();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if(board[row][col] == WHITE_PIECE || board[row][col] == WHITE_KING){
                    List<Integer> piece = new ArrayList<>();
                    piece.add(row);
                    piece.add(col);
                    whitePieces.add(piece);
                }
            }
        }
        return whitePieces;
    }

    public int count(char piece){
        // Count the number of pieces on the board
        int count = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if(board[row][col] == piece){
                    count++;
                }
            }
        }
        return count;
    }

    public void clear(int row, int col){
        // Clear a square on the board
        board[row][col] = EMPTY_SQUARE;
    }

    public void movePiece(int startRow, int startCol, int endRow, int endCol){
        // Move a piece from one square to another
        if(board[startRow][startCol] == BLACK_PIECE && endRow == SIZE - 1){
            board[endRow][endCol] = BLACK_KING;
        }
        else if(board[startRow][startCol] == WHITE_PIECE && endRow == 0){
            board[endRow][endCol] = WHITE_KING;
        }
        else{
            board[endRow][endCol] = board[startRow][startCol];
        }
        board[startRow][startCol] = EMPTY_SQUARE;
    }

    public boolean isEmpty(int row, int col){
        // Check if a square is empty
        return board[row][col] == EMPTY_SQUARE;
    }

    public char getPiece(int row, int col){
        // Get the piece at a given square
        return board[row][col];
    }

    // Initialize the board with initial piece positions
    private void initializeBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if ((row + col) % 2 != 0) {
                    if (row < 3) {
                        board[row][col] = BLACK_PIECE; // Player 1's pieces
                    } else if (row > 4) {
                        board[row][col] = WHITE_PIECE; // Player 2's pieces
                    } else {
                        board[row][col] = EMPTY_SQUARE; // Empty squares
                    }
                } else if ((row + col) % 2 == 0){
                    board[row][col] = INVALID_SQUARE; // Invalid squares
                } else {
                    board[row][col] = EMPTY_SQUARE; // Empty squares
                }
            }
        }
    }

    // Print the current state of the board to the command line
    public void printBoard(PrintWriter out) {
    StringBuilder boardString = new StringBuilder();
    boardString.append("  ");
    for (int i = 0; i < SIZE; i++) {
        boardString.append("  ").append(i).append(" ");
    }
    boardString.append("\n");
    for (int row = 0; row < SIZE; row++) {
        boardString.append("  ");
        for (int i = 0; i < SIZE; i++) {
            boardString.append("+---");
        }
        boardString.append("+\n");
        boardString.append(row).append(" ");
        boardString.append("|");
        for (int col = 0; col < SIZE; col++) {
            boardString.append(" ").append(board[row][col]).append(" |");
        }
        boardString.append("\n");
    }
    boardString.append("  ");
    for (int i = 0; i < SIZE; i++) {
        boardString.append("+---");
    }
    boardString.append("+\n");

    // Send the entire board string to the PrintWriter
    out.print(boardString.toString());
    out.flush(); // Ensure the data is sent immediately
}
}