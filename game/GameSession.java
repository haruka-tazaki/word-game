//ゲーム進行
//GameSession.java
package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutionException;

//非同期処理を行うスレッドを管理するため、ExecutorServiceクラスを使用する
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
public class GameSession {
    private final Socket playerA;
    private final Socket playerB;
    private final String answer;
    //２つのスレッド
    private final ExecutorService pool = Executors.newFixedThreadPool(2);

    //コンストラクタ
    public GameSession(Socket playerA, Socket playerB, String answer) {
        this.playerA = playerA;
        this.playerB = playerB;
        this.answer  = answer;
    }

    public void start() {
        // 非同期タスクをFutureで取得
        Future<?> taskA = pool.submit(() -> handlePlayer(playerA, playerB));
        Future<?> taskB = pool.submit(() -> handlePlayer(playerB, playerA));
        try {
            // 両方のタスクが完了するまで待機
            taskA.get();
            taskB.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            // 全タスク完了後にプールをシャットダウン
            pool.shutdownNow();
        }
    }


    public void handlePlayer(Socket self, Socket opponent) {
        try (
          BufferedReader in  = new BufferedReader(new InputStreamReader(self.getInputStream()));
          PrintWriter    out = new PrintWriter(self.getOutputStream(), true);
          PrintWriter    out_op = new PrintWriter(opponent.getOutputStream(), true);
        ) {
            String guess;
            while(true) {
                guess = in.readLine();

                // 勝敗判定
                if (guess.equalsIgnoreCase(answer)) {
                    out.println("YOU WIN!!   The answer was " + answer + ".");
                    out_op.println("YOU LOSE...   The answer was " + answer + ".");
                    break;
                }

                //ヒント返却
                List<Integer> position = GameLogic.findCorrectPositions(answer, guess);
                List<Character> included = GameLogic.findIncludedLetters(answer, guess);

                out.println("correct position: " + position + "correct letter: " + included);
                //相手に自分が送った単語とそのヒントを送る
                out_op.println("[opponent] "+ guess+"  correct position: " + position + "correct letter: " + included);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdownNow();
            try { playerA.close(); } catch (IOException ignored) {}
            try { playerB.close(); } catch (IOException ignored) {}
        }   
    }
}
