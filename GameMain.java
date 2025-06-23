
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

    // Tambahan: mode permainan
    private boolean isVsComputer = false;

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
                int row = e.getY() / Cell.SIZE;
                int col = e.getX() / Cell.SIZE;

                if (currentState == State.PLAYING) {
                    // Hanya jika sel masih kosong
                    if (row >= 0 && row < Board.ROWS
                            && col >= 0 && col < Board.COLS
                            && board.cells[row][col].content == Seed.NO_SEED) {
                        // Jalankan langkah dan update status
                        currentState = board.stepGame(currentPlayer, row, col);

                        // 1) X bergerak → play EAT_FOOD
                        if (currentPlayer == Seed.CROSS) {
                            SoundEffect.EAT_FOOD.play();
                        } else {
                            SoundEffect.EXPLODE.play();
                        }

                        // Jika setelah langkah status bukan PLAYING → play DIE
                        if (currentState != State.PLAYING) {
                            SoundEffect.DIE.play();
                        }

                        // 3) Ganti giliran
                        currentPlayer = (currentPlayer == Seed.CROSS)
                                ? Seed.NOUGHT
                                : Seed.CROSS;
                        repaint();

                        // Jika mode vs komputer dan giliran AI, AI bergerak otomatis dengan delay
                        if (isVsComputer && currentState == State.PLAYING && currentPlayer == Seed.NOUGHT) {
                            Timer aiTimer = new Timer(500, evt -> {
                                aiMove();
                                ((Timer)evt.getSource()).stop();
                            });
                            aiTimer.setRepeats(false);
                            aiTimer.start();
                        }
                    }
                } else {
                    // Jika sudah selesai, klik ulang untuk restart
                    newGame();
                }
                repaint();  // Segarkan tampilan
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
     * Set apakah permainan melawan komputer atau tidak.
     * @param vsComputer true jika melawan komputer, false jika pemain vs pemain
     */
    public void setVsComputer(boolean vsComputer) {
        this.isVsComputer = vsComputer;
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
     * Gerakan AI menggunakan algoritma Minimax.
     */
    private void aiMove() {
        // AI (Minimax) hanya main jika masih PLAYING dan ada langkah
        if (currentState == State.PLAYING) {
            int[] move = findBestMove(currentPlayer);
            if (move != null) {
                currentState = board.stepGame(currentPlayer, move[0], move[1]);
                // Play sound effect jika perlu
                if (currentPlayer == Seed.CROSS) {
                    SoundEffect.EAT_FOOD.play();
                } else {
                    SoundEffect.EXPLODE.play();
                }
                if (currentState != State.PLAYING) {
                    SoundEffect.DIE.play();
                }
                currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                repaint();
            }
        }
    }

    // Fungsi Minimax untuk AI
    private int[] findBestMove(Seed aiSeed) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                if (board.cells[r][c].content == Seed.NO_SEED) {
                    board.cells[r][c].content = aiSeed;
                    int score = minimax(0, false, aiSeed, (aiSeed == Seed.CROSS ? Seed.NOUGHT : Seed.CROSS));
                    board.cells[r][c].content = Seed.NO_SEED;
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = new int[]{r, c};
                    }
                }
            }
        }
        return bestMove;
    }

    private int minimax(int depth, boolean isMax, Seed aiSeed, Seed opponent) {
        State eval = board.evaluateState();
        if (eval == State.CROSS_WON) return (aiSeed == Seed.CROSS) ? 10 - depth : depth - 10;
        if (eval == State.NOUGHT_WON) return (aiSeed == Seed.NOUGHT) ? 10 - depth : depth - 10;
        if (eval == State.DRAW) return 0;
        int best = isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                if (board.cells[r][c].content == Seed.NO_SEED) {
                    board.cells[r][c].content = isMax ? aiSeed : opponent;
                    int score = minimax(depth + 1, !isMax, aiSeed, opponent);
                    board.cells[r][c].content = Seed.NO_SEED;
                    if (isMax) best = Math.max(best, score);
                    else best = Math.min(best, score);
                }
            }
        }
        return best;
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
            // Inline symbol chooser panel with a custom background
            JPanel chooserPanel = new JPanel(new BorderLayout()) {
                private Image bg = new ImageIcon(
                        GameMain.class.getResource("/images/bg choose your character.png")
                ).getImage();

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // 1) draw your background (either a solid fill or an image)
                    if (bg != null) {
                        g.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
                    } else {
                        g.setColor(new Color(240, 240, 255));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                }
            };
            chooserPanel.setPreferredSize(
                    new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30)
            );

            // Tambahkan label judul di atas tombol
            JLabel chooseLabel = new JLabel("Choose your Character", SwingConstants.CENTER);
            chooseLabel.setFont(new Font("Arial", Font.BOLD, 24));
            chooseLabel.setForeground(Color.RED);
            chooseLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            chooserPanel.add(chooseLabel, BorderLayout.NORTH);

            // Tambahkan panel pilihan mode
            JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            modePanel.setOpaque(false);
            JRadioButton rbPvP = new JRadioButton("Player vs Player");
            JRadioButton rbPvC = new JRadioButton("Player vs Computer");
            rbPvP.setSelected(true);
            ButtonGroup modeGroup = new ButtonGroup();
            modeGroup.add(rbPvP);
            modeGroup.add(rbPvC);
            modePanel.add(rbPvP);
            modePanel.add(rbPvC);
            chooserPanel.add(modePanel, BorderLayout.SOUTH);

            // Sub-panel to hold buttons in center
            JPanel btnPanel = new JPanel(new GridBagLayout());
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
                gamePanel.setVsComputer(rbPvC.isSelected());
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
                gamePanel.setVsComputer(rbPvC.isSelected());
                CardLayout cl = (CardLayout) container.getLayout();
                cl.show(container, "game");
                frame.pack();
            });

            // Tambahkan tombol ke btnPanel dengan GridBagLayout
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 10, 0, 10);
            btnPanel.add(btnX, gbc);
            gbc.gridx = 1;
            btnPanel.add(btnO, gbc);

            JPanel choosePanel = new JPanel();
            chooserPanel.setOpaque(false);
            chooserPanel.setLayout(new BoxLayout(chooserPanel, BoxLayout.Y_AXIS));
            chooserPanel.setPreferredSize(
                    new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30)
            );

// 1) title
            chooseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            chooserPanel.add(Box.createVerticalStrut(20));
            chooserPanel.add(chooseLabel);

// 2) icon buttons
            JPanel iconWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            iconWrapper.setOpaque(false);
            iconWrapper.add(btnX);
            iconWrapper.add(btnO);
            iconWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
            chooserPanel.add(Box.createVerticalStrut(20));
            chooserPanel.add(iconWrapper);

// 3) mode radios
            modePanel.setOpaque(false);
            modePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            chooserPanel.add(Box.createVerticalStrut(20));
            chooserPanel.add(modePanel);
            chooserPanel.add(Box.createVerticalGlue());

            // Assemble chooser buttons
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
