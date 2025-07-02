//実行時は java GameServer <ポート番号> を入力

package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import game.GameSession;

public class GameServer {
    private final int port;

    public GameServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Waiting for Player 1...");
            Socket player1 = serverSocket.accept();
            System.out.println("Player 1 connected: " + player1.getRemoteSocketAddress());

            System.out.println("Waiting for Player 2...");
            Socket player2 = serverSocket.accept();
            System.out.println("Player 2 connected: " + player2.getRemoteSocketAddress());

            // ゲームの進行は GameSession に任せる。
            String answer = "APPLE";  // 正解ワード。とりあえずAPPLE
            GameSession session = new GameSession(player1, player2, answer);
            
            session.run();

            System.out.println("Session finished, shutting down server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 3000;//デフォルトのポート番号は3000
        new GameServer(port).start();
    }
}