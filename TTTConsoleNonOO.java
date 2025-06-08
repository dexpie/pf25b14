import java.util.Scanner;
/**
 * Tic-Tac-Toe: Two-player, console-based, non-graphics, non-OO version.
 * All variables/methods are declared as static (i.e., class)
 *  in this non-OO version.
 */
public class TTTConsoleNonOO {

    // Papan permainan sekarang menyimpan objek Seed
    public static final int ROWS = 3, COLS = 3;

    public static Seed[][] board = new Seed[ROWS][COLS];
    // The current player
    public static Seed currentPlayer;
    // The current state of the game
    public static State currentState;
    public static Scanner in = new Scanner(System.in); // the input Scanner

    /**
     * The entry main method (the program starts here)
     */
    public static void main(String[] args) {
        // Initialize the board, currentState and currentPlayer
        do {
            initGame();

            // Play the game once
            do {
                // currentPlayer makes a move
                // Update board[selectedRow][selectedCol] and currentState
                stepGame();
                // Refresh the display
                paintBoard();
                // Print message if game over
                if (currentState == State.CROSS_WON) {
                    System.out.println("'X' won!\nBye!");
                } else if (currentState == State.NOUGHT_WON) {
                    System.out.println("'O' won!\nBye!");
                } else if (currentState == State.DRAW) {
                    System.out.println("It's a Draw!\nBye!");
                }
                // Switch currentPlayer
                currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
            } while (currentState == State.PLAYING);

            System.out.print("Play again (y/n)? ");
            char ans = in.next().charAt(0);
            if (ans != 'y' && ans != 'Y') {
                System.out.println("Bye!");
                break;
            }

        } while (true);
    }


    /**
     * Initialize the board[][], currentState and currentPlayer for a new game
     */
    public static void initGame() {
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                board[row][col] = Seed.NO_SEED;  // all cells empty
            }
        }
        currentPlayer = Seed.CROSS;   // cross plays first
        currentState = State.PLAYING; // ready to play
    }


    /**
     * The currentPlayer makes one move (one step).
     * Update board[selectedRow][selectedCol] and currentState.
     */
    public static void stepGame() {
        boolean validInput = false;  // for input validation
        do {
            if (currentPlayer == Seed.CROSS) {
                System.out.print("Player 'X', enter your move (row[1-3] column[1-3]): ");
            } else {
                System.out.print("Player 'O', enter your move (row[1-3] column[1-3]): ");
            }
            int row = in.nextInt() - 1;  // array index starts at 0 instead of 1
            int col = in.nextInt() - 1;
            if (row >= 0 && row < ROWS && col >= 0 && col < COLS
                    && board[row][col] == Seed.NO_SEED) {
                // Update board[][] and return the new game state after the move
                currentState = stepGameUpdate(currentPlayer, row, col);
                validInput = true;  // input okay, exit loop
            } else {
                System.out.println("This move at (" + (row + 1) + "," + (col + 1)
                        + ") is not valid. Try again...");
            }
        } while (!validInput);  // repeat if input is invalid
    }

    /**
     * Helper function of stepGame().
     * The given player makes a move at (selectedRow, selectedCol).
     * Update board[selectedRow][selectedCol]. Compute and return the
     * new game state (PLAYING, DRAW, CROSS_WON, NOUGHT_WON).
     *
     * @return new game state
     */
    public static State stepGameUpdate(Seed player, int selectedRow, int selectedCol) {
        // Update game board
        board[selectedRow][selectedCol] = player;

        // Compute and return the new game state
        if (board[selectedRow][0] == player       // 3-in-the-row
                && board[selectedRow][1] == player
                && board[selectedRow][2] == player
                || board[0][selectedCol] == player // 3-in-the-column
                && board[1][selectedCol] == player
                && board[2][selectedCol] == player
                || selectedRow == selectedCol      // 3-in-the-diagonal
                && board[0][0] == player
                && board[1][1] == player
                && board[2][2] == player
                || selectedRow + selectedCol == 2  // 3-in-the-opposite-diagonal
                && board[0][2] == player
                && board[1][1] == player
                && board[2][0] == player) {
            return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
        } else {
            // Nobody win. Check for DRAW (all cells occupied) or PLAYING.
            for (int row = 0; row < ROWS; ++row) {
                for (int col = 0; col < COLS; ++col) {
                    if (board[row][col] == Seed.NO_SEED) {
                        return State.PLAYING; // still have empty cells
                    }
                }
            }
            return State.DRAW; // no empty cell, it's a draw
        }
    }

    /**
     * Print the game board
     */
    public static void paintBoard() {
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                paintCell(board[row][col]); // print each of the cells
                if (col != COLS - 1) {
                    System.out.print("|");   // print vertical partition
                }
            }
            System.out.println();
            if (row != ROWS - 1) {
                System.out.println("-----------"); // print horizontal partition
            }
        }
        System.out.println();
    }

    /**
     * Print a cell with the given content
     */
    public static void paintCell(Seed content) {
        // Gunakan switch untuk memeriksa nilai dari enum Seed
        switch (content) {
            case CROSS:
                System.out.print(" X ");
                break;
            case NOUGHT:
                System.out.print(" O ");
                break;
            case NO_SEED:
                System.out.print("   ");
                break;
        }
    }
}