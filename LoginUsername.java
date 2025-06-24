import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class LoginUsername {
    
    
        public class WelcomePanel extends JPanel {
            private static final long serialVersionUID = 1L;
            private final JPanel container;
            private final Image bgImage;
    
            public interface LoginListener {
                void onLogin(String nickname);
            }
            private LoginListener loginListener;
            public void setLoginListener(LoginListener listener) {
                this.loginListener = listener;
            }
    
            // Ganti ke DISABLED jika error SSL
            private static final String DB_URL = "jdbc:mysql://mysql-1fccdfe6-thaliaharnum-4a7b.d.aivencloud.com:18628/defaultdb?sslMode=DISABLED";
            private static final String DB_USER = "avnadmin";
            private static final String DB_PASSWORD = "AVNS_AI_-WVmdV5uALzNILQI";
    
            public WelcomePanel(JPanel container) {
                this.container = container;
                bgImage = new ImageIcon(getClass().getResource("/images/welcome.png")).getImage();
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                setOpaque(false);
                setBorder(BorderFactory.createEmptyBorder(200, 0, 0, 0));
    
                JButton loginButton = new JButton("LOGIN");
                loginButton.setFont(new Font("Arial", Font.BOLD, 20));
                loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                loginButton.setFocusPainted(false);
                loginButton.addActionListener(e -> showLoginForm());
                add(loginButton);
    
                add(Box.createRigidArea(new Dimension(0, 10)));
    
                JButton aboutButton = new JButton("ABOUT US");
                aboutButton.setFont(new Font("Arial", Font.BOLD, 20));
                aboutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                aboutButton.setFocusPainted(false);
                aboutButton.addActionListener(e -> showAboutDialog());
                add(aboutButton);
    
                add(Box.createVerticalGlue());
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
                    String username = usernameField.getText().trim();
                    String password = new String(passwordField.getPassword()).trim();
    
                    if (!username.isEmpty() && !password.isEmpty()) {
                        simpanKeDatabase(username, password);
                    } else {
                        JOptionPane.showMessageDialog(this, "Username dan password tidak boleh kosong.");
                    }
                }
            }
    
            private void simpanKeDatabase(String username, String password) {
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    
                    String sql = "INSERT INTO gameuser (username, password, firstname, lastname, sex, state, won, lose, draw, play) " +
                            "VALUES (?, ?, NULL, NULL, NULL, NULL, 0, 0, 0, 0)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, username);
                    stmt.setString(2, password);
    
                    int rows = stmt.executeUpdate();
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(this, "Login berhasil! Data disimpan ke database.");
                        if (loginListener != null) loginListener.onLogin(username);
                    }
    
                    stmt.close();
                    conn.close();
                } catch (SQLIntegrityConstraintViolationException e) {
                    JOptionPane.showMessageDialog(this, "Username sudah digunakan.");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Gagal koneksi atau simpan: " + e.getMessage());
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
        }
    // tolak tolakk
    //cihuyy
    }
    