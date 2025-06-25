import java.awt.*;
import javax.swing.ImageIcon;

/**
 * The Board class models the ROWS-by-COLS game board,
 * now with a custom background behind the grid.
 */
public class Board {
    // Define named constants
    public static final int ROWS = 3;
    public static final int COLS = 3;
    // Canvas dimensions
    public static final int CANVAS_WIDTH = Cell.SIZE * COLS;
    public static final int CANVAS_HEIGHT = Cell.SIZE * ROWS;
    // Grid drawing
    public static final int GRID_WIDTH = 3;
    public static final int GRID_WIDTH_HALF = GRID_WIDTH / 2;
    public static final Color COLOR_GRID = Color.LIGHT_GRAY;
    public static final int Y_OFFSET = 1;

    // Optional: background image
    private Image bgImage;

    // 2D array of Cell instances
    Cell[][] cells;

    /**
     * Constructor: load background image (optional) and initialize game.
     */
    public Board() {
        // Uncomment to use an image background instead of solid color
        bgImage = new ImageIcon(getClass().getResource("/images/bg.png")).getImage();
        initGame();
    }

    /** Initialize the game objects (run once) */
    public void initGame() {
        cells = new Cell[ROWS][COLS];
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                cells[row][col] = new Cell(row, col);
            }
        }
    }

    /** Reset the game board, ready for a new game */
    public void newGame() {
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                cells[row][col].newGame();
            }
        }
    }

    /** Execute a move for the given player, then return the new State */
    public State stepGame(Seed player, int selectedRow, int selectedCol) {
        cells[selectedRow][selectedCol].content = player;
        // ... existing win/draw logic unchanged ...
        if ((cells[selectedRow][0].content == player && cells[selectedRow][1].content == player && cells[selectedRow][2].content == player)
                || (cells[0][selectedCol].content == player && cells[1][selectedCol].content == player && cells[2][selectedCol].content == player)
                || (selectedRow == selectedCol && cells[0][0].content == player && cells[1][1].content == player && cells[2][2].content == player)
                || (selectedRow + selectedCol == 2 && cells[0][2].content == player && cells[1][1].content == player && cells[2][0].content == player)) {
            return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
        }
        for (int r = 0; r < ROWS; ++r) {
            for (int c = 0; c < COLS; ++c) {
                if (cells[r][c].content == Seed.NO_SEED) {
                    return State.PLAYING;
                }
            }
        }
        return State.DRAW;
    }

    public State evaluateState() {
        // Cek baris
        for (int r = 0; r < ROWS; r++) {
            if (cells[r][0].content != Seed.NO_SEED &&
                    cells[r][0].content == cells[r][1].content &&
                    cells[r][1].content == cells[r][2].content) {
                return (cells[r][0].content == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
            }
        }
        // Cek kolom
        for (int c = 0; c < COLS; c++) {
            if (cells[0][c].content != Seed.NO_SEED &&
                    cells[0][c].content == cells[1][c].content &&
                    cells[1][c].content == cells[2][c].content) {
                return (cells[0][c].content == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
            }
        }
        // Cek diagonal utama
        if (cells[0][0].content != Seed.NO_SEED &&
                cells[0][0].content == cells[1][1].content &&
                cells[1][1].content == cells[2][2].content) {
            return (cells[0][0].content == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
        }
        // Cek diagonal anti
        if (cells[0][2].content != Seed.NO_SEED &&
                cells[0][2].content == cells[1][1].content &&
                cells[1][1].content == cells[2][0].content) {
            return (cells[0][2].content == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
        }
        // Cek apakah masih ada sel kosong
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (cells[r][c].content == Seed.NO_SEED) {
                    return State.PLAYING;
                }
            }
        }
        // Jika tidak ada pemenang dan tidak ada sel kosong, maka seri
        return State.DRAW;
    }

    /** Paint the board: background, grid lines, then cells */
    public void paint(Graphics g) {
        // 1) Draw background (solid color)
        g.setColor(new Color(245, 245, 220));  // light beige
        g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Uncomment below to use an image background instead
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, null);
        }

        // 2) Draw the grid-lines
        g.setColor(COLOR_GRID);
        for (int row = 1; row < ROWS; ++row) {
            g.fillRoundRect(
                    0,
                    Cell.SIZE * row - GRID_WIDTH_HALF,
                    CANVAS_WIDTH - 1,
                    GRID_WIDTH,
                    GRID_WIDTH,
                    GRID_WIDTH
            );
        }
        for (int col = 1; col < COLS; ++col) {
            g.fillRoundRect(
                    Cell.SIZE * col - GRID_WIDTH_HALF,
                    Y_OFFSET,
                    GRID_WIDTH,
                    CANVAS_HEIGHT - 1,
                    GRID_WIDTH,
                    GRID_WIDTH
            );
        }

        // 3) Draw all the cells
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                cells[row][col].paint(g);
            }
        }
    }
}