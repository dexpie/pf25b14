import java.io.*;
import java.net.*;

public class TicTacToeServer {
    public static final int PORT = 5000;
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started. Waiting for players...");
        // Terima dua client
        Socket player1 = serverSocket.accept();
        System.out.println("Player 1 connected: " + player1.getInetAddress());
        Socket player2 = serverSocket.accept();
        System.out.println("Player 2 connected: " + player2.getInetAddress());

        BufferedReader in1 = new BufferedReader(new InputStreamReader(player1.getInputStream()));
        PrintWriter out1 = new PrintWriter(player1.getOutputStream(), true);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(player2.getInputStream()));
        PrintWriter out2 = new PrintWriter(player2.getOutputStream(), true);

        // Variabel untuk menyimpan nickname
        final String[] nick1 = {"Player1"};
        final String[] nick2 = {"Player2"};

        // Thread untuk relay dan parsing nickname
        Thread t1 = new Thread(() -> {
            try {
                String line;
                while ((line = in1.readLine()) != null) {
                    if (line.startsWith("NICK ")) {
                        nick1[0] = line.substring(5);
                        out2.println("OPPONENT_NICK " + nick1[0]);
                    } else if (line.startsWith("REMATCH")) {
                        // Relay permintaan rematch ke player 2
                        out2.println("REMATCH");
                    } else {
                        out2.println(line);
                    }
                }
            } catch (IOException e) {
                System.out.println("Player 1 disconnected.");
            }
        });
        Thread t2 = new Thread(() -> {
            try {
                String line;
                while ((line = in2.readLine()) != null) {
                    if (line.startsWith("NICK ")) {
                        nick2[0] = line.substring(5);
                        out1.println("OPPONENT_NICK " + nick2[0]);
                    } else if (line.startsWith("REMATCH")) {
                        // Relay permintaan rematch ke player 1
                        out1.println("REMATCH");
                    } else {
                        out1.println(line);
                    }
                }
            } catch (IOException e) {
                System.out.println("Player 2 disconnected.");
            }
        });
        // Notifikasi ke client bahwa lawan sudah terhubung
        out1.println("OPPONENT_CONNECTED");
        out2.println("OPPONENT_CONNECTED");
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            // abaikan
        }
        player1.close();
        player2.close();
        serverSocket.close();
        System.out.println("Server stopped.");
    }
}
