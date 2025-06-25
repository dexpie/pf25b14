import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import javax.swing.*;

public class GameMain extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final String TITLE = "Tic Tac Toe";                          
    public static final Color COLOR_BG = Color.WHITE;                           
    public static final Color COLOR_BG_STATUS = new Color(216, 216, 216);       
    public static final Color COLOR_CROSS = new Color(239, 105, 80);            
    public static final Color COLOR_NOUGHT = new Color(64, 154, 225);           
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.PLAIN, 14); 

    private Board board;                
    private State currentState;         
    private Seed currentPlayer;         
    private Seed startingPlayer = Seed.CROSS; 
    private JLabel statusBar;           

    private boolean isVsComputer = false;
    private boolean isOnline = false; 
    
    private TicTacToeClient client;
    private boolean isClientConnected = false; 
    private boolean isOpponentConnected = false; 

    private JTextArea chatArea;
    private JTextField chatInput;
    private JButton sendButton;
    private JPanel chatPanel;

    private String nickname = "Player";
    private String opponentNickname = "Opponent";

    private Seed mySeed;

    private JPanel rematchPanel;

    private boolean isRematchRequested = false;
    private boolean isOpponentRematchRequested = false;

    private LeaderboardPanel leaderboardPanel;

    private String aiLevel = "medium"; 

    private JButton backButton;
    private JLabel statsLabel;
    private JLabel resultLabel; 

    private boolean hasShownResultPopup = false;

    public GameMain() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30));
        setBorder(BorderFactory.createLineBorder(COLOR_BG_STATUS, 2, false));
        setBackground(COLOR_BG);

        board = new Board();               
        installMouseListener();            
        createStatusBar();                 
        initGame();                        

        leaderboardPanel = new LeaderboardPanel();
        JButton leaderboardButton = new JButton("Leaderboard");
        leaderboardButton.addActionListener(e -> showLeaderboardDialog());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setOpaque(false);
        topPanel.add(leaderboardButton);
        add(topPanel, BorderLayout.NORTH);
    }

    private void installMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = e.getY() / Cell.SIZE;
                int col = e.getX() / Cell.SIZE;

                if (isOnline) {
                    if (!isClientConnected || !isOpponentConnected) {
                        return;
                    }
                    if (currentPlayer != mySeed) return;
                    if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS
                            && board.cells[row][col].content == Seed.NO_SEED
                            && currentState == State.PLAYING) {
                        currentState = board.stepGame(currentPlayer, row, col);
                        if (currentPlayer == Seed.CROSS) {
                            SoundEffect.EAT_FOOD.play();
                        } else {
                            SoundEffect.EXPLODE.play();
                        }
                        if (currentState != State.PLAYING) {
                            SoundEffect.DIE.play();
                        }
                        sendMoveOnline(row, col);
                        currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                        repaint();
                        showResultPopupIfNeeded();
                    }
                    return;
                }

                if (currentState == State.PLAYING) {
                    if (row >= 0 && row < Board.ROWS
                            && col >= 0 && col < Board.COLS
                            && board.cells[row][col].content == Seed.NO_SEED) {
                        currentState = board.stepGame(currentPlayer, row, col);

                        if (currentPlayer == Seed.CROSS) {
                            SoundEffect.EAT_FOOD.play();
                        } else {
                            SoundEffect.EXPLODE.play();
                        }

                        if (currentState != State.PLAYING) {
                            SoundEffect.DIE.play();
                        }

                        currentPlayer = (currentPlayer == Seed.CROSS)
                                ? Seed.NOUGHT
                                : Seed.CROSS;
                        repaint();
                        showResultPopupIfNeeded();

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
                    newGame();
                }
                repaint();  
            }
        });
    }

    private void createStatusBar() {
        statusBar = new JLabel();
        statusBar.setFont(FONT_STATUS);
        statusBar.setBackground(COLOR_BG_STATUS);
        statusBar.setOpaque(true);
        statusBar.setPreferredSize(new Dimension(300, 30));
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));
        String localIp = getLocalIpAddress();
        statusBar.setText("Your IP: " + localIp + " | Nickname: " + nickname);
        add(statusBar, BorderLayout.PAGE_END);

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
        chatPanel.setVisible(false); 

        sendButton.addActionListener(e -> sendChatMessage());
        chatInput.addActionListener(e -> sendChatMessage());
    }

    private void setChatPanelVisible(boolean visible) {
        if (chatPanel != null) chatPanel.setVisible(visible);
    }

    private void sendChatMessage() {
        String msg = chatInput.getText().trim();
        if (!msg.isEmpty() && client != null && isOnline) {
            client.sendChat(nickname + ": " + msg);
            appendChat(nickname + ": " + msg);
            chatInput.setText("");
        }
    }

    public void appendChat(String msg) {
        chatArea.append(msg + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

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
        }
        return "Tidak ditemukan";
    }

    private void initGame() {
        for (int r = 0; r < Board.ROWS; r++) {
            for (int c = 0; c < Board.COLS; c++) {
                board.cells[r][c].content = Seed.NO_SEED;
            }
        }
        currentState = State.PLAYING; 
    }

    public void newGame() {
        hasShownResultPopup = false; 
        initGame();                          
        currentPlayer = startingPlayer;      
    }

    public void setStartingPlayer(Seed seed) {
        this.startingPlayer = seed;
    }

    public void setVsComputer(boolean vsComputer) {
        this.isVsComputer = vsComputer;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(COLOR_BG);
        board.paint(g);                       

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

        boolean gameOver = (currentState == State.DRAW || currentState == State.CROSS_WON || currentState == State.NOUGHT_WON);
        backButton.setVisible(false); 
        statsLabel.setVisible(false); 
        resultLabel.setVisible(false); 
        if (gameOver) {
        }
        rematchPanel.revalidate();
        rematchPanel.repaint();
    }

    private void showResultPopupIfNeeded() {
        boolean gameOver = (currentState == State.DRAW || currentState == State.CROSS_WON || currentState == State.NOUGHT_WON);
        if (gameOver && !hasShownResultPopup) {
            String msg;
            if (currentState == State.DRAW) {
                msg = "Draw!";
            } else if (currentState == State.CROSS_WON) {
                msg = "X Win!";
            } else if (currentState == State.NOUGHT_WON) {
                msg = "O Win!";
            } else {
                msg = "Game Over!";
            }
            JOptionPane.showMessageDialog(this, msg, "Hasil Permainan", JOptionPane.INFORMATION_MESSAGE);
            int opt = JOptionPane.showOptionDialog(this, "Apa yang ingin Anda lakukan?", "Game Selesai",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new String[]{"Rematch", "Back"}, "Rematch");
            if (opt == 1) { 
                onBackToMenu();
            } else if (opt == 0) { 
                SwingUtilities.invokeLater(this::newGame);
            }
            hasShownResultPopup = true;
        } else if (!gameOver) {
            hasShownResultPopup = false;
        }
    }

    private void aiMove() {
        if (currentState == State.PLAYING) {
            int[] move = null;
            if ("easy".equals(aiLevel)) {
                move = randomMove();
            } else if ("medium".equals(aiLevel)) {
                if (Math.random() < 0.5) move = randomMove();
                else move = findBestMove(currentPlayer);
            } else { 
                move = findBestMove(currentPlayer);
            }
            if (move != null) {
                currentState = board.stepGame(currentPlayer, move[0], move[1]);
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

    public static void main(String[] args) {
        SoundEffect.init();
        SoundEffect.setVolume(SoundEffect.Volume.MEDIUM);
        SoundEffect.BACKSOUND.playLoop();
        boolean loginSuccess = false;
        while (!loginSuccess) {
            loginSuccess = LoginUsername.WelcomePanel.consoleLogin();
            if (!loginSuccess) {
                System.out.println("Coba lagi atau tekan Ctrl+C untuk keluar.");
            }
        }
        SwingUtilities.invokeLater(() -> {
            final JFrame frame = new JFrame(TITLE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            final JPanel container = new JPanel(new CardLayout());
            final GameMain gamePanel = new GameMain();
            container.add(gamePanel, "game");

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

            JLabel chooseLabel = new JLabel("Choose your Character", SwingConstants.CENTER);
            chooseLabel.setFont(new Font("Arial", Font.BOLD, 24));
            chooseLabel.setForeground(Color.RED);
            chooseLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            chooserPanel.add(chooseLabel, BorderLayout.NORTH);

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

            ActionListener characterSelectListener = e -> {
                if (e.getSource() == btnX) {
                    gamePanel.setStartingPlayer(Seed.CROSS);
                    gamePanel.setMySeed(Seed.CROSS);
                } else {
                    gamePanel.setStartingPlayer(Seed.NOUGHT);
                    gamePanel.setMySeed(Seed.NOUGHT);
                }
                String[] modes = {"Player vs Player", "Player vs Computer", "Player vs Online"};
                int mode = JOptionPane.showOptionDialog(frame, "Pilih mode permainan:", "Mode Permainan",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, modes, modes[0]);
                if (mode == 0) {
                    gamePanel.setOnline(false);
                    gamePanel.setVsComputer(false);
                } else if (mode == 1) {
                    gamePanel.setOnline(false);
                    gamePanel.setVsComputer(true);
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

    private void sendMoveOnline(int row, int col) {
        if (client != null) {
            client.sendMove(row, col);
        } else {
            System.out.println("Client belum terhubung, tidak bisa kirim langkah.");
        }
    }

    public void applyRemoteMove(int row, int col, Seed seed) {
        if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS) {
            if (board.cells[row][col].content == Seed.NO_SEED && currentState == State.PLAYING) {
                currentState = board.stepGame(seed, row, col);
                if (seed == Seed.CROSS) {
                    SoundEffect.EAT_FOOD.play();
                } else {
                    SoundEffect.EXPLODE.play();
                }
                if (currentState != State.PLAYING) {
                    SoundEffect.DIE.play();
                }
                currentPlayer = (seed == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                repaint();
            }
        }
    }

    public void onOpponentNickname(String nickname) {
        setOpponentNickname(nickname);
        repaint();
    }

    public void setClient(TicTacToeClient client) {
        this.client = client;
    }

    public void onClientConnected() {
        this.isClientConnected = true;
        repaint();
    }

    public void onOpponentConnected() {
        this.isOpponentConnected = true;
        repaint();
    }

    public void setOnline(boolean online) {
        this.isOnline = online;
        setChatPanelVisible(online);
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getNickname() {
        return nickname;
    }

    public void setOpponentNickname(String nickname) {
        this.opponentNickname = nickname;
    }
    public String getOpponentNickname() {
        return opponentNickname;
    }

    public void setMySeed(Seed mySeed) {
        this.mySeed = mySeed;
    }
    public Seed getMySeed() {
        return mySeed;
    }

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

    public void onOpponentRematch() {
        isOpponentRematchRequested = true;
        if (isRematchRequested && isOpponentRematchRequested) {
            doRematchReset();
        }
    }

    private void doRematchReset() {
        isRematchRequested = false;
        isOpponentRematchRequested = false;
        newGame();
        repaint();
    }

    private void showLeaderboardDialog() {
        leaderboardPanel.refreshLeaderboard();
        JOptionPane.showMessageDialog(this, leaderboardPanel, "Leaderboard", JOptionPane.PLAIN_MESSAGE);
    }

    private void onBackToMenu() {
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

    private String getPlayerStatsText() {
        int win = LeaderboardUtil.getWinCount(nickname);
        int lose = LeaderboardUtil.getLoseCount(nickname);
        int draw = LeaderboardUtil.getDrawCount(nickname);
        return String.format("Statistik: %s | Menang: %d | Kalah: %d | Seri: %d", nickname, win, lose, draw);
    }
}
