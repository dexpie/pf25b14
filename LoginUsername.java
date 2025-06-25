import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class LoginUsername {
    // Ubah inner class menjadi static
    public static class WelcomePanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final JPanel container;
        private final Image bgImage;

        public interface LoginListener {
            void onLogin(String nickname);
        }

        private LoginListener loginListener;

        // Database configuration
        private static final String DB_URL = "jdbc:mysql://mysql-1fccdfe6-thaliaharnum-4a7b.d.aivencloud.com:18628/defaultdb?sslMode=DISABLED";
        private static final String DB_USER = "avnadmin";
        private static final String DB_PASSWORD = "AVNS_AI_-WVmdV5uALzNILQI";

        public WelcomePanel(JPanel container) {
            this.container = container;
            this.bgImage = new ImageIcon(getClass().getResource("/images/welcome.png")).getImage();
            initializeUI();
        }

        private void initializeUI() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(200, 0, 0, 0));

            addLoginButton();
            addAboutButton();
            add(Box.createVerticalGlue());
        }

        private void addLoginButton() {
            JButton loginButton = createStyledButton("LOGIN");
            loginButton.addActionListener(e -> showLoginForm());
            add(loginButton);
            add(Box.createRigidArea(new Dimension(0, 10)));
        }

        private void addAboutButton() {
            JButton aboutButton = createStyledButton("ABOUT US");
            aboutButton.addActionListener(e -> showAboutDialog());
            add(aboutButton);
        }

        private JButton createStyledButton(String text) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.BOLD, 20));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setFocusPainted(false);
            return button;
        }

        private void showLoginForm() {
            JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
            JTextField usernameField = new JTextField();
            JPasswordField passwordField = new JPasswordField();

            panel.add(new JLabel("Username:"));
            panel.add(usernameField);
            panel.add(new JLabel("Password:"));
            panel.add(passwordField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Login Form", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                handleLogin(usernameField.getText().trim(), passwordField.getPassword());
            }
        }

        private void handleLogin(String username, char[] password) {
            if (username.isEmpty() || password.length == 0) {
                JOptionPane.showMessageDialog(this, "Username dan password tidak boleh kosong.");
                return;
            }
            saveToDatabase(username, new String(password));
        }

        private void saveToDatabase(String username, String password) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                Class.forName("com.mysql.cj.jdbc.Driver");

                String sql = "INSERT INTO gameuser (username, password, firstname, lastname, sex, state, won, lose, draw, play) " +
                        "VALUES (?, ?, NULL, NULL, NULL, NULL, 0, 0, 0, 0)";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);

                    if (stmt.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(this, "Login berhasil! Data disimpan ke database.");
                        if (loginListener != null) {
                            loginListener.onLogin(username);
                        }
                    }
                }
            } catch (SQLIntegrityConstraintViolationException e) {
                JOptionPane.showMessageDialog(this, "Username sudah digunakan.");
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Driver database tidak ditemukan.");
                e.printStackTrace();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Gagal koneksi ke database: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                e.printStackTrace();
            }
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
            if (bgImage != null) {
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        }

        public void setLoginListener(LoginListener listener) {
            this.loginListener = listener;
        }
    }
}