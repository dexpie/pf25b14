import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Tic-Tac-Toe: Integrated custom symbol chooser and game panel in one class.
 * The main method displays a chooser interface first, then switches to the game.
 */
public class GameMain extends JPanel {
    private static final long serialVersionUID = 1L;

    // ----- Constants for UI -----
    public static final String TITLE = "Tic Tac Toe";                          // Window title
    public static final Color COLOR_BG = Color.WHITE;                           // Background color
    public static final Color COLOR_BG_STATUS = new Color(216, 216, 216);       // Status bar background
    public static final Color COLOR_CROSS = new Color(239, 105, 80);            // X color
    public static final Color COLOR_NOUGHT = new Color(64, 154, 225);           // O color
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.PLAIN, 14); // Status font

    // ----- Game state variables -----
    private Board board;                // The game board model
    private State currentState;         // Current state (PLAYING, DRAW, CROSS_WON, NOUGHT_WON)
    private Seed currentPlayer;         // Current player's turn
    private Seed startingPlayer = Seed.CROSS; // Chosen starting player (default X)
    private JLabel statusBar;           // Status message display

    /**
     * Constructor: set up the board UI and initial game state.
     */
    public GameMain() {
        // Set layout, borders, and background for game panel
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30));
        setBorder(BorderFactory.createLineBorder(COLOR_BG_STATUS, 2, false));
        setBackground(COLOR_BG);

        // Initialize board model and UI listeners
        board = new Board();               // Create the game board
        installMouseListener();            // Handle clicks on cells
        createStatusBar();                 // Set up the status bar at bottom
        initGame();                        // Prepare initial empty board state
    }

    /**
     * Set up a mouse listener to process clicks and play moves.
     */
    private void installMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = e.getY() / Cell.SIZE;  // Compute clicked row
                int col = e.getX() / Cell.SIZE;  // Compute clicked column

                if (currentState == State.PLAYING) {
                    // If the cell is empty, make a move
                    if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS
                            && board.cells[row][col].content == Seed.NO_SEED) {
                        currentState = board.stepGame(currentPlayer, row, col); // Execute move
                        // Play sound based on new state
                        if (currentState == State.PLAYING) {
                            SoundEffect.EAT_FOOD.play();
                        } else {
                            SoundEffect.DIE.play();
                        }
                        // Switch turn
                        currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                    }
                } else {
                    // Restart game if it is over
                    newGame();
                }
                repaint(); // Redraw board and status bar
            }
        });
    }

    /**
     * Create the status bar label at the bottom of the panel.
     */
    private void createStatusBar() {
        statusBar = new JLabel();                        // Status label
        statusBar.setFont(FONT_STATUS);                  // Set font
        statusBar.setBackground(COLOR_BG_STATUS);        // Set background color
        statusBar.setOpaque(true);                       // Make background visible
        statusBar.setPreferredSize(new Dimension(300, 30)); // Set size
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12)); // Padding
        add(statusBar, BorderLayout.PAGE_END);           // Add to bottom
    }

    /**
     * Initialize the board cells and state for a new game.
     */
    private void initGame() {
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                board.cells[r][c].content = Seed.NO_SEED;
            }
        }
        currentState = State.PLAYING; // Ready to play
    }

    /**
     * Restart the game using the chosen starting player.
     */
    public void newGame() {
        initGame();                          // Reset board state
        currentPlayer = startingPlayer;      // Set the initial turn
    }

    /**
     * Set which player (X or O) starts first.
     * @param seed Seed.CROSS or Seed.NOUGHT
     */
    public void setStartingPlayer(Seed seed) {
        this.startingPlayer = seed;
    }

    /**
     * Render the board and update the status bar text.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(COLOR_BG);
        board.paint(g);                       // Draw grid and any X/O symbols

        // Update status bar based on game state
        if (currentState == State.PLAYING) {
            statusBar.setForeground(Color.BLACK);
            statusBar.setText(currentPlayer == Seed.CROSS ? "X's Turn" : "O's Turn");
        } else if (currentState == State.DRAW) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("It's a Draw! Click to play again.");
        } else if (currentState == State.CROSS_WON) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("'X' Won! Click to play again.");
        } else if (currentState == State.NOUGHT_WON) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("'O' Won! Click to play again.");
        }
    }

    /**
     * Application entry point: display symbol chooser, then game panel.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(TITLE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create a container with CardLayout to swap views
            JPanel container = new JPanel(new CardLayout());

            // Create and add the game panel
            GameMain gamePanel = new GameMain();
            container.add(gamePanel, "game");

            // Inline symbol chooser panel
            JPanel chooserPanel = new JPanel(new BorderLayout());
            chooserPanel.setBackground(COLOR_BG);
            chooserPanel.setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30));

            // Sub-panel to hold buttons in center
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 140));
            btnPanel.setOpaque(false);

            // Load icons and scale to 60x60
            ImageIcon rawX = new ImageIcon(GameMain.class.getResource("/images/cross.png"));
            ImageIcon rawO = new ImageIcon(GameMain.class.getResource("/images/nought.png"));
            ImageIcon iconX = new ImageIcon(rawX.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            ImageIcon iconO = new ImageIcon(rawO.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));

            // Button X
            JButton btnX = new JButton(iconX);
            btnX.setPreferredSize(new Dimension(100, 100));
            btnX.setBorder(BorderFactory.createEmptyBorder());
            btnX.setFocusPainted(false);
            btnX.setToolTipText("Play as X");
            btnX.addActionListener(e -> {
                gamePanel.setStartingPlayer(Seed.CROSS);
                gamePanel.newGame();
                CardLayout cl = (CardLayout) container.getLayout();
                cl.show(container, "game");
                frame.pack();
            });

            // Button O
            JButton btnO = new JButton(iconO);
            btnO.setPreferredSize(new Dimension(100, 100));
            btnO.setBorder(BorderFactory.createEmptyBorder());
            btnO.setFocusPainted(false);
            btnO.setToolTipText("Play as O");
            btnO.addActionListener(e -> {
                gamePanel.setStartingPlayer(Seed.NOUGHT);
                gamePanel.newGame();
                CardLayout cl = (CardLayout) container.getLayout();
                cl.show(container, "game");
                frame.pack();
            });

            // Assemble chooser buttons
            btnPanel.add(btnX);
            btnPanel.add(btnO);
            chooserPanel.add(btnPanel, BorderLayout.CENTER);
            container.add(chooserPanel, "chooser");

            // Show chooser first
            frame.setContentPane(container);
            ((CardLayout) container.getLayout()).show(container, "chooser");

            frame.pack();                           // Size window to content
            frame.setLocationRelativeTo(null);      // Center on screen
            frame.setVisible(true);                 // Display
        });
    }
}
