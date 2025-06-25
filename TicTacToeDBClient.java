import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class TicTacToeDBClient {
    private final String url = "jdbc:mysql://mysql-14b22b5s-pf25b14c.c.aivencloud.com:16070/defaultdb?sslMode=REQUIRED";
    private final String user = "avnadmin";
    private final String password = "AVNS_GcFcyt6nFyhLPEB185w";
    private final String gameId;
    private final String player;
    private int lastMove = 0;
    private GameMain gameMain;

    public TicTacToeDBClient(GameMain gameMain, String gameId, String player) {
        this.gameMain = gameMain;
        this.gameId = gameId;
        this.player = player;
    }

    public void sendMove(int row, int col) throws Exception {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            int moveNumber = getLastMoveNumber(conn) + 1;
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO moves (game_id, move_number, player, row, col) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, gameId);
            ps.setInt(2, moveNumber);
            ps.setString(3, player);
            ps.setInt(4, row);
            ps.setInt(5, col);
            ps.executeUpdate();
        }
    }

    public void startPolling() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                try (Connection conn = DriverManager.getConnection(url, user, password)) {
                    PreparedStatement ps = conn.prepareStatement(
                        "SELECT move_number, player, row, col FROM moves WHERE game_id=? AND move_number>? ORDER BY move_number ASC");
                    ps.setString(1, gameId);
                    ps.setInt(2, lastMove);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        int moveNum = rs.getInt("move_number");
                        String movePlayer = rs.getString("player");
                        int row = rs.getInt("row");
                        int col = rs.getInt("col");
                        if (!movePlayer.equals(player)) {
                            gameMain.applyRemoteMove(row, col, movePlayer.equals("X") ? Seed.CROSS : Seed.NOUGHT);
                        }
                        lastMove = moveNum;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);
    }

    private int getLastMoveNumber(Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT MAX(move_number) FROM moves WHERE game_id=?");
        ps.setString(1, gameId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 0;
    }
}
