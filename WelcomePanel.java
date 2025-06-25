import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * WelcomePanel: Menampilkan layar sambutan dengan gambar latar
 * dan beberapa pilihan tombol (LOGIN, PLAY, SETTING, ABOUT US/How To Play),
 * dengan padding atas dan jarak antar tombol yang bisa diatur.
 */
public class WelcomePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JPanel container;
    private final Image bgImage;
    private float volume = 0.8f; // Volume default (80%)

    // Tambahkan interface callback untuk login
    public interface LoginListener {
        void onLogin(String nickname);
    }
    private LoginListener loginListener;
    public void setLoginListener(LoginListener listener) {
        this.loginListener = listener;
    }

    public WelcomePanel(JPanel container) {
        this.container = container;
        // Muat gambar background dari resources
        bgImage = new ImageIcon(getClass().getResource("/images/welcome.gif")).getImage();

        // Layout vertikal dan transparan agar gambar latar terlihat
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        // Tambahkan padding atas sebesar 200px
        setBorder(BorderFactory.createEmptyBorder(200, 0, 0, 0));

        // Tombol PLAY
        JButton playButton = new JButton("PLAY");
        playButton.setFont(new Font("Arial", Font.BOLD, 20));
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.setFocusPainted(false);
        playButton.addActionListener(e -> showChooser()); // langsung ke pilih karakter
        add(playButton);

        // Jarak antar tombol: 10px
        add(Box.createRigidArea(new Dimension(0, 10)));

        // Tombol HOW TO PLAY
        JButton howToPlayButton = new JButton("How To Play");
        howToPlayButton.setFont(new Font("Arial", Font.BOLD, 20));
        howToPlayButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        howToPlayButton.setFocusPainted(false);
        howToPlayButton.addActionListener(e -> showHowToPlayDialog());
        add(howToPlayButton);

        // Jarak antar tombol: 10px
        add(Box.createRigidArea(new Dimension(0, 10)));

        // Tombol ABOUT US
        JButton aboutButton = new JButton("About Us");
        aboutButton.setFont(new Font("Arial", Font.BOLD, 20));
        aboutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        aboutButton.setFocusPainted(false);
        aboutButton.addActionListener(e -> showAboutDialog());
        add(aboutButton);

        // Sisa ruang di bawah
        add(Box.createVerticalGlue());
    }

    private void showSettingDialog() {
        // Buat panel untuk dialog setting
        JPanel settingPanel = new JPanel();
        settingPanel.setLayout(new BoxLayout(settingPanel, BoxLayout.Y_AXIS));
        settingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Label volume
        JLabel volumeLabel = new JLabel("Volume: " + (int)(volume * 100) + "%");
        volumeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingPanel.add(volumeLabel);

        // Slider volume
        JSlider volumeSlider = new JSlider(0, 100, (int)(volume * 100));
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setAlignmentX(Component.CENTER_ALIGNMENT);

        volumeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                volume = volumeSlider.getValue() / 100f;
                volumeLabel.setText("Volume: " + volumeSlider.getValue() + "%");
                // Di sini Anda bisa menambahkan kode untuk mengatur volume audio
                // Contoh: AudioManager.setVolume(volume);
            }
        });

        settingPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        settingPanel.add(volumeSlider);

        // Tampilkan dialog
        JOptionPane.showMessageDialog(
                this,
                settingPanel,
                "Audio Settings",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private void showLoginDialog() {
        String nickname = JOptionPane.showInputDialog(this, "Masukkan nickname:", "Player");
        if (nickname == null || nickname.trim().isEmpty()) nickname = "Player";
        if (loginListener != null) loginListener.onLogin(nickname.trim());
    }

    private void showChooser() {
        CardLayout cl = (CardLayout) container.getLayout();
        cl.show(container, "chooser");
    }

    private void showHowToPlayDialog() {
        JOptionPane.showMessageDialog(
                this,
                "Cara bermain:\n1. Pilih mode dan karakter.\n2. Klik pada kotak untuk mengisi giliran Anda.\n3. Menangkan dengan membuat garis 3 simbol.\n4. Nikmati permainannya!",
                "How To Play",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
                this,
                "Tic Tac Toe Game\nDeveloped by Your Name",
                "About Us",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Gambar background memenuhi ukuran panel
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}