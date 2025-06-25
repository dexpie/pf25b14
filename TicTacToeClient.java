import java.io.*;
import java.net.*;

public class TicTacToeClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private GameMain gameMain;
    private Seed mySeed;

    public TicTacToeClient(GameMain gameMain, Seed mySeed, String serverAddress) throws IOException {
        this.gameMain = gameMain;
        this.mySeed = mySeed;
        int port = 5000;
        socket = new Socket(serverAddress, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        gameMain.onClientConnected();
    }

    public void start() {
        out.println("NICK " + gameMain.getNickname());
        new Thread(() -> listen()).start();
    }

    public void sendMove(int row, int col) {
        out.println("MOVE " + row + " " + col);
    }

    public void sendChat(String msg) {
        out.println("CHAT " + msg);
    }

    public void sendRematch() {
        out.println("REMATCH");
    }

    private void listen() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("MOVE")) {
                    String[] parts = line.split(" ");
                    int row = Integer.parseInt(parts[1]);
                    int col = Integer.parseInt(parts[2]);
                    Seed seed = (mySeed == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                    gameMain.applyRemoteMove(row, col, seed);
                } else if (line.startsWith("START")) {
                } else if (line.startsWith("OPPONENT_CONNECTED")) {
                    gameMain.onOpponentConnected();
                } else if (line.startsWith("OPPONENT_NICK ")) {
                    String oppNick = line.substring(14);
                    gameMain.onOpponentNickname(oppNick);
                } else if (line.startsWith("MESSAGE")) {
                    System.out.println(line.substring(8));
                } else if (line.startsWith("CHAT ")) {
                    String chatMsg = line.substring(5);
                    gameMain.appendChat(chatMsg);
                } else if (line.startsWith("REMATCH")) {
                    gameMain.onOpponentRematch();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
