import java.awt.*;
import javax.swing.ImageIcon;

public class Board {
    public static final int ROWS = 3;
    public static final int COLS = 3;
    public static final int CANVAS_WIDTH = Cell.SIZE * COLS;
    public static final int CANVAS_HEIGHT = Cell.SIZE * ROWS;
    public static final int GRID_WIDTH = 8;
    public static final int GRID_WIDTH_HALF = GRID_WIDTH / 2;
    public static final Color COLOR_GRID = Color.LIGHT_GRAY;
    public static final int Y_OFFSET = 1;

    private Image bgImage;
    Cell[][] cells;
    private int[][] winLine = new int[3][2];
    private boolean hasWinLine = false;

    public Board() {
        bgImage = new ImageIcon(getClass().getResource("/images/bg.png")).getImage();
        initGame();
    }

    public void initGame() {
        cells = new Cell[ROWS][COLS];
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                cells[row][col] = new Cell(row, col);
            }
        }
    }

    public void newGame() {
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                cells[row][col].newGame();
            }
        }
        hasWinLine = false;
    }

    public State stepGame(Seed player, int selectedRow, int selectedCol) {
        cells[selectedRow][selectedCol].content = player;
        hasWinLine = false;
        if (cells[selectedRow][0].content == player && cells[selectedRow][1].content == player && cells[selectedRow][2].content == player) {
            winLine[0][0]=selectedRow; winLine[0][1]=0;
            winLine[1][0]=selectedRow; winLine[1][1]=1;
            winLine[2][0]=selectedRow; winLine[2][1]=2;
            hasWinLine = true;
            return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
        }
        if (cells[0][selectedCol].content == player && cells[1][selectedCol].content == player && cells[2][selectedCol].content == player) {
            winLine[0][0]=0; winLine[0][1]=selectedCol;
            winLine[1][0]=1; winLine[1][1]=selectedCol;
            winLine[2][0]=2; winLine[2][1]=selectedCol;
            hasWinLine = true;
            return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
        }
        if (selectedRow == selectedCol && cells[0][0].content == player && cells[1][1].content == player && cells[2][2].content == player) {
            winLine[0][0]=0; winLine[0][1]=0;
            winLine[1][0]=1; winLine[1][1]=1;
            winLine[2][0]=2; winLine[2][1]=2;
            hasWinLine = true;
            return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
        }
        if (selectedRow + selectedCol == 2 && cells[0][2].content == player && cells[1][1].content == player && cells[2][0].content == player) {
            winLine[0][0]=0; winLine[0][1]=2;
            winLine[1][0]=1; winLine[1][1]=1;
            winLine[2][0]=2; winLine[2][1]=0;
            hasWinLine = true;
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
        for (int r = 0; r < ROWS; r++) {
            if (cells[r][0].content != Seed.NO_SEED &&
                    cells[r][0].content == cells[r][1].content &&
                    cells[r][1].content == cells[r][2].content) {
                return (cells[r][0].content == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
            }
        }
        for (int c = 0; c < COLS; c++) {
            if (cells[0][c].content != Seed.NO_SEED &&
                    cells[0][c].content == cells[1][c].content &&
                    cells[1][c].content == cells[2][c].content) {
                return (cells[0][c].content == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
            }
        }
        if (cells[0][0].content != Seed.NO_SEED &&
                cells[0][0].content == cells[1][1].content &&
                cells[1][1].content == cells[2][2].content) {
            return (cells[0][0].content == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
        }
        if (cells[0][2].content != Seed.NO_SEED &&
                cells[0][2].content == cells[1][1].content &&
                cells[1][1].content == cells[2][0].content) {
            return (cells[0][2].content == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
        }
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (cells[r][c].content == Seed.NO_SEED) {
                    return State.PLAYING;
                }
            }
        }
        return State.DRAW;
    }

    public int[][] getWinLine() {
        return hasWinLine ? winLine : null;
    }
    public boolean hasWinLine() {
        return hasWinLine;
    }

    public void paint(Graphics g) {
        g.setColor(new Color(245, 245, 220));
        g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, null);
        }
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
        boolean[][] highlightMap = new boolean[ROWS][COLS];
        if (hasWinLine) {
            for (int i = 0; i < 3; i++) {
                int r = winLine[i][0];
                int c = winLine[i][1];
                highlightMap[r][c] = true;
            }
        }
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                cells[row][col].paint(g, highlightMap[row][col]);
            }
        }
    }
}