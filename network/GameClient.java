// 実行時は　java GameClient ＜ホスト名＞　＜ポート番号＞を入力
package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameClient {
    private final String host;
    private final int    port;

    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try (Socket socket = new Socket(host, port);
             BufferedReader in    = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter   out   = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Connected to server at " + host + ":" + port);
            String guess;
            while (true) {
                // 入力受付
                System.out.print("Enter 5-letter> ");
                guess = stdin.readLine();
                if (guess == null || guess.length() != 5) {
                    System.out.println("Please enter exactly 5 letters.");
                    continue;
                }
                // サーバーへ予想を送信
                out.println(guess);

                // サーバーからヒントを受信
                String hint = in.readLine();
                System.out.println(hint);

                // 勝敗通知
                if ("YOU_WIN".equals(hint)) {
                    break;
                } else if ("YOU_LOSE".equals(hint)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : "localhost";
        int    port = (args.length > 1) ? Integer.parseInt(args[1]) : 3000;
        new GameClient(host, port).start();
    }
}
