import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class LoginUsername {
    public static class WelcomePanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final JPanel container;
        private final Image bgImage;

        public interface LoginListener {
            void onLogin(String nickname);
        }

        private LoginListener loginListener;

        private static final String DB_USER = "avnadmin";
        private static final String DB_PASSWORD = "AVNS_GcFcyt6nFyhLPEB185w";
        private static final String DB_NAME = "defaultdb";
        private static final String DB_HOST = "mysql-14b22b5s-pf25b14c.c.aivencloud.com";
        private static final String DB_PORT = "16070";
        private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?sslMode=REQUIRED";

        @Override
        public String getName() {
            return super.getName();
        }

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
            JButton loginButton = createStyledButton("LOGIN (DISABLED)");
            loginButton.setEnabled(false);
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
            JOptionPane.showMessageDialog(this, "Fitur login/register sedang dinonaktifkan.");
        }

        private void handleLogin(String username, char[] password) {
            JOptionPane.showMessageDialog(this, "Fitur login/register sedang dinonaktifkan.");
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

        public static boolean consoleLogin() {
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            System.out.print("Enter Username: ");
            String username = scanner.nextLine();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();
            System.out.println("Login berhasil! (dummy mode)");
            return true;
        }
    }
}
