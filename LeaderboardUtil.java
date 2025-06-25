import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardUtil {
    private static final String DB_URL = "jdbc:mysql://mysql-14b22b5s-pf25b14c.c.aivencloud.com:16070/defaultdb?sslMode=REQUIRED";
    private static final String DB_USER = "avnadmin";
    private static final String DB_PASS = "AVNS_GcFcyt6nFyhLPEB185w";

    public static void addWin(String nickname) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "INSERT INTO leaderboard (nickname, score) VALUES (?, 1) ON DUPLICATE KEY UPDATE score = score + 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nickname);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addResult(String nickname, String result) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "INSERT INTO leaderboard (nickname, win, draw, lose) VALUES (?, 0, 0, 0) ON DUPLICATE KEY UPDATE nickname=nickname";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nickname);
                ps.executeUpdate();
            }
            String updateSql = null;
            if ("win".equals(result)) updateSql = "UPDATE leaderboard SET win = win + 1 WHERE nickname = ?";
            else if ("draw".equals(result)) updateSql = "UPDATE leaderboard SET draw = draw + 1 WHERE nickname = ?";
            else if ("lose".equals(result)) updateSql = "UPDATE leaderboard SET lose = lose + 1 WHERE nickname = ?";
            if (updateSql != null) {
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, nickname);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String[]> getTopPlayers(int limit) {
        List<String[]> result = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT nickname, win, draw, lose FROM leaderboard ORDER BY win DESC, draw DESC, lose ASC, nickname ASC LIMIT ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(new String[]{
                            rs.getString("nickname"),
                            String.valueOf(rs.getInt("win")),
                            String.valueOf(rs.getInt("draw")),
                            String.valueOf(rs.getInt("lose"))
                        });
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Tambahkan method untuk insert nickname ke database jika belum ada
    public static void ensureNicknameExists(String nickname) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "INSERT INTO leaderboard (nickname, win, draw, lose) VALUES (?, 0, 0, 0) ON DUPLICATE KEY UPDATE nickname=nickname";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nickname);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
