//Gameserver.java
//実行時は java GameServer <ポート番号> を入力

package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

import game.GameSession;
import util.WordLoader;

public class GameServer {
    private final int port;

    private String[] wordList;

    public GameServer(int port) {
        this.port = port;

        try {
            // 単語ファイルを読み込む（ファイル名は適宜変更）
            wordList = WordLoader.loadWords("wordles.txt");
        } catch (IOException e) {
            System.err.println("単語リストの読み込みに失敗しました。");
            e.printStackTrace();
            wordList = new String[] { "APPLE" }; // 失敗したらapple
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Waiting for Player 1...");
            Socket player1 = serverSocket.accept();
            System.out.println("Player 1 connected: " + player1.getRemoteSocketAddress());

            System.out.println("Waiting for Player 2...");
            Socket player2 = serverSocket.accept();
            System.out.println("Player 2 connected: " + player2.getRemoteSocketAddress());
            
            String answer = getRandomWord();
            System.out.println("Answer word selected: " + answer);//デバッグ用、適宜削除
            
            // ゲームの進行は GameSession に任せる。
            // 非同期セッション開始
            new GameSession(player1, player2, answer).start();

            System.out.println("Session finished, shutting down server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //配列内ランダム取り出し
    private String getRandomWord() {
        int index = ThreadLocalRandom.current().nextInt(wordList.length);
        return wordList[index];
    }

    public static void main(String[] args) {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 3000;//デフォルトのポート番号は3000
        new GameServer(port).start();
    }
}
