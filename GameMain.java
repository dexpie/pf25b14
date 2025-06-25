import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
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
    private boolean isOnline = false; // Tambahkan variabel mode online
    // Tambahan: referensi ke client
    private TicTacToeClient client;
    private boolean isClientConnected = false; // Status koneksi client
    private boolean isOpponentConnected = false; // Status lawan sudah join

    // Komponen chat
    private JTextArea chatArea;
    private JTextField chatInput;
    private JButton sendButton;
    private JPanel chatPanel;

    // Tambahan: variabel nickname
    private String nickname = "Player";
    private String opponentNickname = "Opponent";

    // Tambahan: seed milik client ini (X atau O)
    private Seed mySeed;

    // Tambahan: panel rematch
    private JPanel rematchPanel;

    // Tambahan: flag rematch online
    private boolean isRematchRequested = false;
    private boolean isOpponentRematchRequested = false;

    // Tambahan: leaderboard panel
    private LeaderboardPanel leaderboardPanel;

    // Tambahan: level AI
    private String aiLevel = "medium"; // default

    // Tambahan: tombol kembali dan label statistik
    private JButton backButton;
    private JLabel statsLabel;
    private JLabel resultLabel; // Label hasil game

    // Tambahan: flag untuk menampilkan popup hasil
    private boolean hasShownResultPopup = false;

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

        leaderboardPanel = new LeaderboardPanel();
        // Tambahkan tombol leaderboard di sudut atas
        JButton leaderboardButton = new JButton("Leaderboard");
        leaderboardButton.addActionListener(e -> showLeaderboardDialog());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setOpaque(false);
        topPanel.add(leaderboardButton);
        add(topPanel, BorderLayout.NORTH);
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

                if (isOnline) {
                    if (!isClientConnected || !isOpponentConnected) {
                        // Jangan izinkan klik sebelum client dan lawan terhubung
                        return;
                    }
                    // Hanya boleh klik jika giliran sendiri
                    if (currentPlayer != mySeed) return;
                    // Hanya jika sel masih kosong dan status PLAYING
                    if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS
                            && board.cells[row][col].content == Seed.NO_SEED
                            && currentState == State.PLAYING) {
                        // Update board lokal
                        currentState = board.stepGame(currentPlayer, row, col);
                        if (currentPlayer == Seed.CROSS) {
                            SoundEffect.EAT_FOOD.play();
                        } else {
                            SoundEffect.EXPLODE.play();
                        }
                        if (currentState != State.PLAYING) {
                            SoundEffect.DIE.play();
                        }
                        // Kirim langkah ke lawan/server
                        sendMoveOnline(row, col);
                        // Ganti giliran (lock input)
                        currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                        repaint();
                        showResultPopupIfNeeded();
                    }
                    return;
                }

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
                        showResultPopupIfNeeded();

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
        statusBar = new JLabel();
        statusBar.setFont(FONT_STATUS);
        statusBar.setBackground(COLOR_BG_STATUS);
        statusBar.setOpaque(true);
        statusBar.setPreferredSize(new Dimension(300, 30));
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));
        // Tampilkan IP lokal di status bar saat awal
        String localIp = getLocalIpAddress();
        statusBar.setText("Your IP: " + localIp + " | Nickname: " + nickname);
        add(statusBar, BorderLayout.PAGE_END);

        // Tambahan: tombol kembali dan statistik
        backButton = new JButton("Kembali");
        backButton.setVisible(false);
        backButton.addActionListener(e -> onBackToMenu());
        statsLabel = new JLabel();
        statsLabel.setVisible(false);
        resultLabel = new JLabel();
        resultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resultLabel.setVisible(false);
        rematchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        rematchPanel.setOpaque(false);
        rematchPanel.add(backButton);
        rematchPanel.add(resultLabel);
        rematchPanel.add(statsLabel);
        add(rematchPanel, BorderLayout.CENTER);

        // Tambahkan panel chat di bawah status bar
        chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder("Chat"));
        chatArea = new JTextArea(5, 30);
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        JPanel inputPanel = new JPanel(new BorderLayout());
        chatInput = new JTextField();
        sendButton = new JButton("Send");
        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        add(chatPanel, BorderLayout.AFTER_LAST_LINE);
        chatPanel.setVisible(false); // Hanya tampil saat online

        // Event kirim chat
        sendButton.addActionListener(e -> sendChatMessage());
        chatInput.addActionListener(e -> sendChatMessage());
    }

    // Tampilkan chat panel jika online
    private void setChatPanelVisible(boolean visible) {
        if (chatPanel != null) chatPanel.setVisible(visible);
    }

    // Kirim pesan chat ke client
    private void sendChatMessage() {
        String msg = chatInput.getText().trim();
        if (!msg.isEmpty() && client != null && isOnline) {
            client.sendChat(nickname + ": " + msg);
            appendChat(nickname + ": " + msg);
            chatInput.setText("");
        }
    }

    // Tampilkan pesan chat di area chat
    public void appendChat(String msg) {
        chatArea.append(msg + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    // Fungsi untuk mendapatkan IP lokal
    private String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                if (nic.isLoopback() || !nic.isUp()) continue;
                Enumeration<InetAddress> addrs = nic.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(":") == -1) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            // abaikan
        }
        return "Tidak ditemukan";
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
        hasShownResultPopup = false; // Reset flag agar popup hanya muncul sekali per game
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
        if (isOnline && !isClientConnected) {
            statusBar.setForeground(Color.BLUE);
            statusBar.setText("Menghubungkan ke server... | Nickname: " + nickname);
        } else if (isOnline && isClientConnected && !isOpponentConnected) {
            statusBar.setForeground(Color.BLUE);
            statusBar.setText("Menunggu lawan terhubung... | Nickname: " + nickname);
        } else if (isOnline && isClientConnected && isOpponentConnected) {
            statusBar.setForeground(Color.BLACK);
            statusBar.setText("You: " + nickname + " vs " + opponentNickname + (currentState == State.PLAYING ? (currentPlayer == startingPlayer ? " (Your Turn)" : " (Opponent's Turn)") : ""));
        } else if (currentState == State.PLAYING) {
            statusBar.setForeground(Color.BLACK);
            statusBar.setText(currentPlayer == Seed.CROSS ? "X's Turn" : "O's Turn");
        } else if (currentState == State.DRAW) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("It's a Draw! Click to play again.");
            LeaderboardUtil.addResult(nickname, "draw");
        } else if (currentState == State.CROSS_WON) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("'X' Won! Click to play again.");
            if (currentPlayer == Seed.NOUGHT) {
                LeaderboardUtil.addResult(nickname, "win");
            } else {
                LeaderboardUtil.addResult(nickname, "lose");
            }
        } else if (currentState == State.NOUGHT_WON) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("'O' Won! Click to play again.");
            if (currentPlayer == Seed.CROSS) {
                LeaderboardUtil.addResult(nickname, "win");
            } else {
                LeaderboardUtil.addResult(nickname, "lose");
            }
        }

        // Tampilkan tombol rematch, kembali, dan statistik jika game selesai
        boolean gameOver = (currentState == State.DRAW || currentState == State.CROSS_WON || currentState == State.NOUGHT_WON);
        backButton.setVisible(false); // Sembunyikan tombol kembali
        statsLabel.setVisible(false); // Jangan tampilkan statistik
        resultLabel.setVisible(false); // Tidak perlu label lagi, pakai popup
        if (gameOver) {
            // statsLabel.setText(getPlayerStatsText()); // Hilangkan pengisian teks statistik
        }
        rematchPanel.revalidate();
        rematchPanel.repaint();
    }

    private void showResultPopupIfNeeded() {
        boolean gameOver = (currentState == State.DRAW || currentState == State.CROSS_WON || currentState == State.NOUGHT_WON);
        if (gameOver && !hasShownResultPopup) {
            String msg;
            if (currentState == State.DRAW) {
                msg = "Seri!";
            } else if ((currentState == State.CROSS_WON && currentPlayer == Seed.NOUGHT) || (currentState == State.NOUGHT_WON && currentPlayer == Seed.CROSS)) {
                msg = "Menang!";
            } else {
                msg = "Kalah!";
            }
            JOptionPane.showMessageDialog(this, msg, "Hasil Permainan", JOptionPane.INFORMATION_MESSAGE);
            int opt = JOptionPane.showOptionDialog(this, "Apa yang ingin Anda lakukan?", "Game Selesai",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new String[]{"Rematch", "Back"}, "Rematch");
            if (opt == 1) { // Back
                onBackToMenu();
            } else if (opt == 0) { // Rematch
                SwingUtilities.invokeLater(this::newGame);
            }
            hasShownResultPopup = true;
        } else if (!gameOver) {
            hasShownResultPopup = false;
        }
    }

    /**
     * Gerakan AI menggunakan algoritma Minimax.
     */
    private void aiMove() {
        if (currentState == State.PLAYING) {
            int[] move = null;
            if ("easy".equals(aiLevel)) {
                move = randomMove();
            } else if ("medium".equals(aiLevel)) {
                if (Math.random() < 0.5) move = randomMove();
                else move = findBestMove(currentPlayer);
            } else { // hard
                move = findBestMove(currentPlayer);
            }
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
                showResultPopupIfNeeded();
            }
        }
    }

    // Fungsi random move untuk AI easy/medium
    private int[] randomMove() {
        java.util.List<int[]> empty = new java.util.ArrayList<>();
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                if (board.cells[r][c].content == Seed.NO_SEED) {
                    empty.add(new int[]{r, c});
                }
            }
        }
        if (empty.isEmpty()) return null;
        return empty.get((int)(Math.random() * empty.size()));
    }

    // Fungsi Minimax untuk AI
    private int[] findBestMove(Seed aiSeed) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        Seed opponent = (aiSeed == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                if (board.cells[r][c].content == Seed.NO_SEED) {
                    board.cells[r][c].content = aiSeed;
                    int score = minimax(0, false, aiSeed, opponent);
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
        SoundEffect.init();
        SoundEffect.setVolume(SoundEffect.Volume.MEDIUM);
        SoundEffect.BACKSOUND.playLoop();
        // Console login before showing any GUI
        boolean loginSuccess = false;
        while (!loginSuccess) {
            loginSuccess = LoginUsername.WelcomePanel.consoleLogin();
            if (!loginSuccess) {
                System.out.println("Coba lagi atau tekan Ctrl+C untuk keluar.");
            }
        }
        // Setelah login sukses, baru tampilkan GUI
        SwingUtilities.invokeLater(() -> {
            final JFrame frame = new JFrame(TITLE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            final JPanel container = new JPanel(new CardLayout());
            final GameMain gamePanel = new GameMain();
            container.add(gamePanel, "game");

            // Inline symbol chooser panel with custom background
            JPanel chooserPanel = new JPanel(new BorderLayout()) {
                private Image bg = new ImageIcon(
                        GameMain.class.getResource("/images/choose.jpg")
                ).getImage();
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (bg != null) {
                        g.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
                    } else {
                        g.setColor(new Color(240, 240, 255));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                }
            };
            chooserPanel.setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30));

            // Tambahkan label judul di atas tombol
            JLabel chooseLabel = new JLabel("Choose your Character", SwingConstants.CENTER);
            chooseLabel.setFont(new Font("Arial", Font.BOLD, 24));
            chooseLabel.setForeground(Color.RED);
            chooseLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            chooserPanel.add(chooseLabel, BorderLayout.NORTH);

            // Sub-panel to hold buttons in center
            JPanel btnPanel = new JPanel(new GridBagLayout());
            btnPanel.setOpaque(false);
            ImageIcon rawX = new ImageIcon(GameMain.class.getResource("/images/cross.png"));
            ImageIcon rawO = new ImageIcon(GameMain.class.getResource("/images/nought.png"));
            ImageIcon iconX = new ImageIcon(rawX.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            ImageIcon iconO = new ImageIcon(rawO.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            JButton btnX = new JButton(iconX);
            btnX.setPreferredSize(new Dimension(100, 100));
            btnX.setBorder(BorderFactory.createEmptyBorder());
            btnX.setFocusPainted(false);
            btnX.setToolTipText("Play as X");
            JButton btnO = new JButton(iconO);
            btnO.setPreferredSize(new Dimension(100, 100));
            btnO.setBorder(BorderFactory.createEmptyBorder());
            btnO.setFocusPainted(false);
            btnO.setToolTipText("Play as O");
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 10, 0, 10);
            btnPanel.add(btnX, gbc);
            gbc.gridx = 1;
            btnPanel.add(btnO, gbc);
            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.setOpaque(false);
            wrapper.add(btnPanel, new GridBagConstraints());
            chooserPanel.add(wrapper, BorderLayout.CENTER);
            container.add(chooserPanel, "chooser");

            // Handler untuk tombol X dan O
            ActionListener characterSelectListener = e -> {
                if (e.getSource() == btnX) {
                    gamePanel.setStartingPlayer(Seed.CROSS);
                    gamePanel.setMySeed(Seed.CROSS);
                } else {
                    gamePanel.setStartingPlayer(Seed.NOUGHT);
                    gamePanel.setMySeed(Seed.NOUGHT);
                }
                // Pop-up pilihan mode
                String[] modes = {"Player vs Player", "Player vs Computer", "Player vs Online"};
                int mode = JOptionPane.showOptionDialog(frame, "Pilih mode permainan:", "Mode Permainan",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, modes, modes[0]);
                if (mode == 0) {
                    gamePanel.setOnline(false);
                    gamePanel.setVsComputer(false);
                } else if (mode == 1) {
                    gamePanel.setOnline(false);
                    gamePanel.setVsComputer(true);
                    // Popup pilih level AI
                    String[] levels = {"Easy", "Medium", "Hard"};
                    int ai = JOptionPane.showOptionDialog(frame, "Pilih tingkat kesulitan AI:", "AI Level",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, levels, levels[1]);
                    if (ai == 0) gamePanel.setAiLevel("easy");
                    else if (ai == 1) gamePanel.setAiLevel("medium");
                    else if (ai == 2) gamePanel.setAiLevel("hard");
                } else if (mode == 2) {
                    gamePanel.setOnline(true);
                    gamePanel.setVsComputer(false);
                }
                CardLayout cl = (CardLayout) container.getLayout();
                cl.show(container, "game");
            };
            btnX.addActionListener(characterSelectListener);
            btnO.addActionListener(characterSelectListener);

            WelcomePanel welcomePanel = new WelcomePanel(container);
            welcomePanel.setLoginListener(nickname -> {
                gamePanel.setNickname(nickname);
                LeaderboardUtil.ensureNicknameExists(nickname);
                CardLayout cl = (CardLayout) container.getLayout();
                cl.show(container, "chooser");
            });
            container.add(welcomePanel, "welcome");

            frame.setContentPane(container);
            ((CardLayout) container.getLayout()).show(container, "welcome");
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // Placeholder: implementasikan pengiriman langkah ke server/client
    private void sendMoveOnline(int row, int col) {
        // Kirim langkah ke client jika sudah ada
        if (client != null) {
            client.sendMove(row, col);
        } else {
            System.out.println("Client belum terhubung, tidak bisa kirim langkah.");
        }
    }

    // Dipanggil dari client saat menerima langkah lawan
    public void applyRemoteMove(int row, int col, Seed seed) {
        if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS) {
            if (board.cells[row][col].content == Seed.NO_SEED && currentState == State.PLAYING) {
                currentState = board.stepGame(seed, row, col);
                // Mainkan sound effect jika perlu
                if (seed == Seed.CROSS) {
                    SoundEffect.EAT_FOOD.play();
                } else {
                    SoundEffect.EXPLODE.play();
                }
                if (currentState != State.PLAYING) {
                    SoundEffect.DIE.play();
                }
                // Ganti giliran
                currentPlayer = (seed == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                repaint();
            }
        }
    }

    // Dipanggil dari client saat menerima nickname lawan
    public void onOpponentNickname(String nickname) {
        setOpponentNickname(nickname);
        repaint();
    }

    // Tambahkan setter client
    public void setClient(TicTacToeClient client) {
        this.client = client;
        // Jangan reset isClientConnected di sini, biarkan event client yang mengatur
    }

    // Dipanggil dari client saat koneksi berhasil
    public void onClientConnected() {
        this.isClientConnected = true;
        repaint();
    }

    // Dipanggil dari client saat lawan sudah join
    public void onOpponentConnected() {
        this.isOpponentConnected = true;
        repaint();
    }

    // Tampilkan chat panel saat online
    public void setOnline(boolean online) {
        this.isOnline = online;
        setChatPanelVisible(online);
    }

    // Setter dan getter untuk nickname
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getNickname() {
        return nickname;
    }

    // Setter dan getter untuk nickname lawan
    public void setOpponentNickname(String nickname) {
        this.opponentNickname = nickname;
    }
    public String getOpponentNickname() {
        return opponentNickname;
    }

    // Setter untuk mySeed
    public void setMySeed(Seed mySeed) {
        this.mySeed = mySeed;
    }
    public Seed getMySeed() {
        return mySeed;
    }

    // Setter untuk aiLevel
    public void setAiLevel(String level) {
        this.aiLevel = level;
    }

    private void onRematchClicked() {
        isRematchRequested = true;
        if (isOnline && client != null) {
            client.sendRematch();
        }
        if (isRematchRequested && isOpponentRematchRequested) {
            doRematchReset();
        } else if (!isOnline) {
            doRematchReset();
        }
    }

    // Dipanggil dari client saat menerima sinyal rematch dari lawan
    public void onOpponentRematch() {
        isOpponentRematchRequested = true;
        if (isRematchRequested && isOpponentRematchRequested) {
            doRematchReset();
        }
    }

    // Reset board dan flag rematch
    private void doRematchReset() {
        isRematchRequested = false;
        isOpponentRematchRequested = false;
        newGame();
        repaint();
    }

    // Tampilkan leaderboard dialog
    private void showLeaderboardDialog() {
        leaderboardPanel.refreshLeaderboard();
        JOptionPane.showMessageDialog(this, leaderboardPanel, "Leaderboard", JOptionPane.PLAIN_MESSAGE);
    }

    // Fungsi kembali ke menu utama
    private void onBackToMenu() {
        // Cari parent JFrame dan CardLayout, lalu kembali ke welcome/chooser
        java.awt.Container parent = this.getParent();
        while (parent != null && !(parent instanceof JFrame)) {
            parent = parent.getParent();
        }
        if (parent != null) {
            JFrame frame = (JFrame) parent;
            java.awt.Container content = frame.getContentPane();
            if (content instanceof JPanel && ((JPanel) content).getLayout() instanceof CardLayout) {
                CardLayout cl = (CardLayout) ((JPanel) content).getLayout();
                cl.show((JPanel) content, "welcome");
            }
        }
    }

    // Fungsi statistik player (dummy, bisa diambil dari LeaderboardUtil)
    private String getPlayerStatsText() {
        int win = LeaderboardUtil.getWinCount(nickname);
        int lose = LeaderboardUtil.getLoseCount(nickname);
        int draw = LeaderboardUtil.getDrawCount(nickname);
        return String.format("Statistik: %s | Menang: %d | Kalah: %d | Seri: %d", nickname, win, lose, draw);
    }
}
