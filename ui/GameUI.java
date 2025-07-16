package ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import network.GameClient;

public class GameUI extends JFrame {

    private final GameClient client;
    private final JTextField guessInput;
    private final JButton guessButton;
    private final JLabel statusLabel;

    private static final int MAX_GUESSES = 6;

    private final LinkedList<GuessResult> myGuesses = new LinkedList<>();
    private final LinkedList<GuessResult> opponentGuesses = new LinkedList<>();

    private final JLabel[][] myGrid = new JLabel[MAX_GUESSES][5];
    private final JLabel[][] opponentGrid = new JLabel[MAX_GUESSES][5];

    private final Color COLOR_CORRECT = new Color(106, 170, 100);
    private final Color COLOR_INCLUDED = new Color(201, 180, 88);
    private final Color COLOR_WRONG = new Color(120, 124, 126);
    private final Color COLOR_DEFAULT_BG = Color.BLACK;
    private final Color COLOR_DEFAULT_FG = Color.WHITE;
    private final Border BORDER_DEFAULT = BorderFactory.createLineBorder(Color.DARK_GRAY, 2);

    private static class GuessResult {
        String guess;
        String hint;
        GuessResult(String guess, String hint) {
            this.guess = guess;
            this.hint = hint;
        }
    }

    public GameUI(String host, int port) {
        this.client = new GameClient(host, port, this);

        setTitle("Word Game");
        setSize(700, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(COLOR_DEFAULT_BG);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainPanel.setBackground(COLOR_DEFAULT_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(createBoardPanel("Your Guesses", myGrid));
        mainPanel.add(createBoardPanel("Opponent's Guesses", opponentGrid));

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(COLOR_DEFAULT_BG);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        guessInput = new JTextField();
        guessInput.setFont(new Font("Monospaced", Font.BOLD, 24));
        guessInput.setBackground(new Color(50, 50, 50));
        guessInput.setForeground(COLOR_DEFAULT_FG);
        guessInput.setCaretColor(COLOR_DEFAULT_FG);
        guessInput.setHorizontalAlignment(JTextField.CENTER);

        guessButton = new JButton("Guess");
        guessButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        guessButton.addActionListener(e -> submitGuess());

        inputPanel.add(guessInput, BorderLayout.CENTER);
        inputPanel.add(guessButton, BorderLayout.EAST);

        statusLabel = new JLabel("Connecting to server...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        statusLabel.setForeground(COLOR_DEFAULT_FG);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        add(mainPanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);
        
        setLocationRelativeTo(null);
        setVisible(true);

        new Thread(client::start).start();
        
        guessInput.requestFocusInWindow();
        guessInput.addActionListener(e -> submitGuess());
    }

    private JPanel createBoardPanel(String title, JLabel[][] grid) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_DEFAULT_BG);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(COLOR_DEFAULT_FG);
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(MAX_GUESSES, 5, 5, 5));
        gridPanel.setBackground(COLOR_DEFAULT_BG);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (int row = 0; row < MAX_GUESSES; row++) {
            for (int col = 0; col < 5; col++) {
                grid[row][col] = new JLabel("", SwingConstants.CENTER);
                grid[row][col].setOpaque(true);
                grid[row][col].setBackground(COLOR_DEFAULT_BG);
                grid[row][col].setForeground(COLOR_DEFAULT_FG);
                grid[row][col].setFont(new Font("Monospaced", Font.BOLD, 36));
                grid[row][col].setBorder(BORDER_DEFAULT);
                gridPanel.add(grid[row][col]);
            }
        }
        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }

    private void submitGuess() {
        String guess = guessInput.getText().trim().toLowerCase();
        client.sendGuess(guess);
        guessInput.setText("");
    }
    
    private void redrawGrid(JLabel[][] grid, LinkedList<GuessResult> guesses) {
        for (int row = 0; row < MAX_GUESSES; row++) {
            for (int col = 0; col < 5; col++) {
                grid[row][col].setText("");
                grid[row][col].setBackground(COLOR_DEFAULT_BG);
            }
        }
        
        int rowToDraw = 0;
        for (GuessResult result : guesses) {
            if (rowToDraw >= MAX_GUESSES) continue;

            String guess = result.guess;
            String hint = result.hint;
            List<Integer> correctPositions = parseHintPart(hint, "position");
            List<Character> includedLetters = parseCharacters(hint, "letter");

            for (int i = 0; i < 5; i++) {
                char c = guess.toUpperCase().charAt(i);
                final JLabel cell = grid[rowToDraw][i];
                cell.setText(String.valueOf(c));

                if (correctPositions.contains(i)) {
                    cell.setBackground(COLOR_CORRECT);
                } else if (includedLetters.contains(Character.toLowerCase(c))) {
                    cell.setBackground(COLOR_INCLUDED);
                    includedLetters.remove(Character.valueOf(Character.toLowerCase(c)));
                } else {
                    cell.setBackground(COLOR_WRONG);
                }
            }
            rowToDraw++;
        }
    }

    public void updateMyGrid(String guess, String hint) {
        SwingUtilities.invokeLater(() -> {
            myGuesses.add(new GuessResult(guess, hint));
            if (myGuesses.size() > MAX_GUESSES) {
                myGuesses.removeFirst();
            }
            redrawGrid(myGrid, myGuesses);
        });
    }

    public void updateOpponentGrid(String guess, String hint) {
        SwingUtilities.invokeLater(() -> {
            opponentGuesses.add(new GuessResult(guess, hint));
            if (opponentGuesses.size() > MAX_GUESSES) {
                opponentGuesses.removeFirst();
            }
            redrawGrid(opponentGrid, opponentGuesses);
        });
    }

    public void setStatus(String text) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(text));
    }
    
    public void setGameEnded(String finalMessage) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(finalMessage);
            guessInput.setEnabled(false);
            guessButton.setEnabled(false);
        });
    }

    private List<Integer> parseHintPart(String hint, String partName) {
        List<Integer> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(partName + ": \\[(\\d+(?:, \\d+)*)\\]");
        Matcher matcher = pattern.matcher(hint);
        if (matcher.find()) {
            String[] numbers = matcher.group(1).split(", ");
            for (String num : numbers) {
                if (!num.isEmpty()) result.add(Integer.parseInt(num));
            }
        }
        return result;
    }

    private List<Character> parseCharacters(String hint, String partName) {
        List<Character> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(partName + ": \\[(.*?)\\]");
        Matcher matcher = pattern.matcher(hint);
        if (matcher.find()) {
            String[] chars = matcher.group(1).split(", ");
            for (String c : chars) {
                if (!c.isEmpty()) result.add(c.charAt(0));
            }
        }
        return result;
    }
}