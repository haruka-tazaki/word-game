// Gameclient.java
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
import java.util.concurrent.BlockingQueue;//マルチスレッドで値を安全に受け渡しするためのライブラリ
import java.util.concurrent.LinkedBlockingQueue;

import util.WordLoader;

public class GameClient {
    private final String host;
    private final int    port;
    private final Set<String> validWords; //有効単語の定義
    private volatile boolean gameEnded = false;

    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.validWords = loadWordSet("wordles.txt");  // 単語リスト読み込み
    }

    public void start() {
        BlockingQueue<String> hints = new LinkedBlockingQueue<>();

        try (Socket socket = new Socket(host, port);
             BufferedReader in    = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter   out   = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Connected to server at " + host + ":" + port);
            
            // 受信専用スレッドを起動
            // YOU LOSEを受信する場合・相手の送信ワードを受信する場合・ヒントを受信する場合で分ける
            Thread listener = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {//in.readLineがnullを返す=ストリームを閉じる
                        if (line.startsWith("[opponent]")){
                            // 相手の送信をいつでも受け取って、表示させる
                            System.out.println(line);
                            System.out.print("Enter 5-letter> ");
                        } else if (line.startsWith("YOU LOSE")) { //YOU LOSEを受信したらゲーム終了
                            System.out.println(line);
                            gameEnded = true;
                            break; // listenerスレッドを抜ける
                        } else { //ヒントを受信したら、mainスレッドに渡す
                            hints.put(line);//mainスレッド用のヒントとして保存
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            });
            listener.setDaemon(true);
            listener.start();
            
            //mainスレッド(自分の入力受付処理)
            while (true) {

                if (gameEnded) break; //Listnerスレッドでゲーム終了がセットされたらmainスレッドからも抜ける

                // 入力受付
                System.out.print("Enter 5-letter> ");
                String guess = stdin.readLine();
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
                String hint;
                try {
                    hint = hints.take();
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted, exiting.");
                    Thread.currentThread().interrupt();
                    break; // ループから抜ける場合
                }

                // 当たっていたらこのループから抜ける
                if (hint.startsWith("YOU WIN")) {
                    System.out.println(hint);
                    break;
                } 

                System.out.println(hint);
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

