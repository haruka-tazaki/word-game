//ゲーム進行

package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
//非同期処理を行うスレッドを管理するため、ExecutorServiceクラスを使用する
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
        //各プレイヤーごとに非同期タスクを実行
        pool.execute(() -> handlePlayer(playerA, playerB));
        pool.execute(() -> handlePlayer(playerB, playerA));
    }


    public void handlePlayer(Socket self, Socket opponent) {
        try (
          BufferedReader in  = new BufferedReader(new InputStreamReader(self.getInputStream()));
          PrintWriter    out = new PrintWriter(self.getOutputStream(), true);
          PrintWriter    out_op = new PrintWriter(opponent.getOutputStream(), true);
        ) {
            String guess;
            while((guess = in.readLine()) != null) {
                if ("exit".equalsIgnoreCase(guess)){
                    out.println("End Game");
                    out_op.println("End Game");
                    break;
                }

                //ヒント返却
                List<Integer> position = GameLogic.findCorrectPositions(answer, guess);
                List<Character> included = GameLogic.findIncludedLetters(answer, guess);

                out.println("correct position: " + position);
                out.println("correct letter: " + included);

                //相手に自分が送った単語を送る
                out_op.println(guess);

                // 勝敗判定
                if (guess.equalsIgnoreCase(answer)) {
                    out.println("YOU WIN!!\n" + "The answer was " + answer);
                    out_op.println("YOU LOSE...\n"+ "The answer was " + answer);
                    break;
                }
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
