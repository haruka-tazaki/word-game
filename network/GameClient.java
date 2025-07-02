// 実行時は　java GameClient ＜ホスト名＞　＜ポート番号＞を入力
package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import util.WordLoader;

public class GameClient {
    private final String host;
    private final int    port;
    private final Set<String> validWords; //有効単語の定義

    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.validWords = loadWordSet("wordles.txt");  // 単語リスト読み込み
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

                // 単語リストに存在するかチェック
                if (!validWords.contains(guess)) {
                    System.out.println("Invalid word. Please enter a valid 5-letter word.");
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

    //単語リストから使用可能単語を抽出
    private Set<String> loadWordSet(String filename) {
        try {
            String[] words = WordLoader.loadWords(filename);
            return new HashSet<>(Arrays.asList(words));
        } catch (IOException e) {
            System.err.println("Failed to load words.txt: " + e.getMessage());
            return new HashSet<>();  // 空のセットにして無限拒否にならないように
        }
    }

    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : "localhost";
        int    port = (args.length > 1) ? Integer.parseInt(args[1]) : 3000;
        new GameClient(host, port).start();
    }
}

