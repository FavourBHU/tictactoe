package basics;                             // ❶  Keep project structure tidy
import javax.swing.*;                       // ❷  Swing UI toolkit
import javax.swing.border.EmptyBorder;      // ❸  Extra spacing without nested panels
import java.awt.*;                          // ❹  Core AWT layout / color / font classes
import java.awt.event.*;                    // ❺  Mouse / Action adapters
import java.util.ArrayList;                 // ❻  Simple lists for moves
import java.util.List;
/**
 * XandOImproved – a Swing‑based Tic‑Tac‑Toe game
 * with animations, player‑name entry and a cleaner UI.
 */
public class XandO extends JFrame {
    /* ----------  THEME CONSTANTS  ---------- */
    private static final Color PRIMARY   = new Color(30, 144, 255);   // Dodger‑blue
    private static final Color SECONDARY = Color.WHITE;
    private static final Font  TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final Font  BTN_FONT   = new Font("SansSerif", Font.BOLD, 40);

    /* ----------  GAME STATE  ---------- */
    private final List<Integer> playerOne = new ArrayList<>();
    private final List<Integer> playerTwo = new ArrayList<>();
    private int turn = 0;                         // 0 = Player‑1, 1 = Player‑2
    private String playerOneName = "Player 1";
    private String playerTwoName = "Player 2";

    /* ----------  UI COMPONENTS  ---------- */
    private final CardLayout cards = new CardLayout();   // swap Welcome ↔ Board
    private final JPanel root   = new JPanel(cards);     // top‑level container
    private final JButton[][] cells = new JButton[3][3]; // 9 buttons, easy grid
    private final JPanel gridPanel = new JPanel(new GridLayout(3, 3, 5, 5)); // gap=5px

    /* =========================================================== *
     *  CONSTRUCTOR – builds both screens then shows the welcome   *
     * =========================================================== */
    public XandO() {
        super("Tic‑Tac‑Toe ✨");              // Window title w/ emoji sparkle
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);         // Center on screen
        setResizable(false);

        buildWelcome();   // panel “welcome”
        buildGrid();      // panel “game”

        add(root);        // only ONE component gets added to JFrame
        cards.show(root, "welcome");
    }

    /* -------------------  WELCOME (NAME ENTRY)  ------------------- */
    /* ------------------------------------------------------------------
     *  TWO‑STEP WELCOME FLOW
     *  1) introPanel  (card name = "intro")
     *  2) namePanel   (card name = "name")
     *  Both added to the existing CardLayout `root`.
     * ------------------------------------------------------------------ */
    private void buildWelcome() {

        /* ----------  1️⃣  INTRO PANEL  ---------- */
        JPanel introPanel = new JPanel();
        introPanel.setLayout(new BoxLayout(introPanel, BoxLayout.Y_AXIS));
        introPanel.setBorder(new EmptyBorder(80, 40, 40, 40));
        introPanel.setBackground(PRIMARY);

        JLabel hello = new JLabel("Welcome to X and O!");
        hello.setFont(new Font("Segoe UI Black", Font.BOLD, 28)); // <-- Catchy and bold
        hello.setForeground(SECONDARY);
        hello.setAlignmentX(Component.CENTER_ALIGNMENT);


        JButton nextBtn = new JButton("Next ➜");
        styliseButton(nextBtn);
        nextBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        introPanel.add(hello);
        introPanel.add(Box.createVerticalStrut(50));
        introPanel.add(nextBtn);

        /* ----------  2️⃣  NAME‑ENTRY PANEL  ---------- */
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        namePanel.setBackground(PRIMARY);

        JLabel prompt = new JLabel("Enter players names");
        prompt.setFont(TITLE_FONT);
        prompt.setForeground(SECONDARY);
        prompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField p1Field = new JTextField();
        JTextField p2Field = new JTextField();
        styliseTextField(p1Field, "Player 1 Name");
        styliseTextField(p2Field, "Player 2 Name");

        JButton startBtn = new JButton("Start Game 🚀");
        styliseButton(startBtn);
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        namePanel.add(prompt);
        namePanel.add(Box.createVerticalStrut(25));
        namePanel.add(p1Field);
        namePanel.add(Box.createVerticalStrut(15));
        namePanel.add(p2Field);
        namePanel.add(Box.createVerticalStrut(30));
        namePanel.add(startBtn);

        /* ----------  CARD NAVIGATION  ---------- */
        root.add(introPanel, "intro");
        root.add(namePanel,  "name");

        // clicking “Next” flips intro → name‑entry
        nextBtn.addActionListener(e -> cards.show(root, "name"));

        // clicking “Start” captures names then flips to the game board
        startBtn.addActionListener(e -> {
            playerOneName = p1Field.getText().trim().isEmpty() ? "Player 1" : p1Field.getText().trim();
            playerTwoName = p2Field.getText().trim().isEmpty() ? "Player 2" : p2Field.getText().trim();
            resetGame();
            popupTransition();
            cards.show(root, "game");
        });
    }
    /* -------------------  3 × 3 GAME BOARD  ------------------- */
    private void buildGrid() {
        gridPanel.setBackground(SECONDARY);
        gridPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                JButton cell = new JButton("");
                cell.setFont(BTN_FONT);
                cell.setFocusPainted(false);
                cell.setBackground(SECONDARY);
                final int pos = r * 3 + c + 1;          // 1‑9 mapping
                cell.addActionListener(e -> handleMove(cell, pos));
                addHoverEffect(cell);
                cells[r][c] = cell;
                gridPanel.add(cell);
            }
        }
        JButton restart = new JButton("↩ Restart");
        styliseButton(restart);
        restart.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this, "Restart game?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) resetGame();
        });

        JPanel game = new JPanel(new BorderLayout());
        game.add(gridPanel, BorderLayout.CENTER);
        game.add(restart, BorderLayout.SOUTH);
        root.add(game, "game");
    }

    /* ======================================================= *
     *        GAME LOGIC + CLICK/ANIMATION HANDLERS            *
     * ======================================================= */
    /** Handle an individual move, then test for win/draw */
    private void handleMove(JButton btn, int pos) {
        if (turn == 0) {                               // Player 1
            playerOne.add(pos);
            btn.setText("X");
        } else {                                       // Player 2
            playerTwo.add(pos);
            btn.setText("O");
        }
        animateClick(btn);                             // subtle flash
        btn.setEnabled(false);

        if (checkWin(playerOne))      showWinner(playerOneName);
        else if (checkWin(playerTwo)) showWinner(playerTwoName);
        else if (playerOne.size() + playerTwo.size() == 9)
            showWinner("No one");  // draw
        else turn ^= 1;                                // swap player (0 ↔ 1)
    }
    /** Quick background flash (10 frames × 40 ms) */
    private void animateClick(JButton btn) {
        Timer t = new Timer(40, null);
        final int[] step = {0};
        t.addActionListener(e -> {
            float k = step[0] / 10f;                                // 0‑1
            btn.setBackground(new Color(255, (int) (255 - 155 * k), (int) (255 - 155 * k)));
            if (++step[0] > 10) t.stop();
        });
        t.start();
    }
    /** Tiny “Let’s play!” pop‑up that slides 120 px downward */
    private void popupTransition() {
        JDialog splash = new JDialog(this, false);
        splash.setUndecorated(true);
        splash.setSize(200, 60);
        splash.setLocation(getX() + getWidth() / 2 - 100, getY() - 60);

        JLabel msg = new JLabel("  Let's Play!  ", SwingConstants.CENTER);
        msg.setFont(new Font("SansSerif", Font.BOLD, 18));
        msg.setOpaque(true);
        msg.setBackground(PRIMARY);
        msg.setForeground(SECONDARY);
        splash.add(msg);
        splash.setVisible(true);

        Timer t = new Timer(15, null);
        final int[] dy = {0};
        t.addActionListener(e -> {
            splash.setLocation(splash.getX(), splash.getY() + 4);
            if ((dy[0] += 4) >= 120) { t.stop(); splash.dispose(); }
        });
        t.start();
    }
    /** All eight win combos in one nested loop */
    private boolean checkWin(List<Integer> moves) {
        int[][] c = { {1,2,3}, {4,5,6}, {7,8,9},
                {1,4,7}, {2,5,8}, {3,6,9},
                {1,5,9}, {3,5,7} };
        for (int[] trio : c)
            if (moves.contains(trio[0]) && moves.contains(trio[1]) && moves.contains(trio[2]))
                return true;
        return false;
    }

    /** Animated winner dialog (bounces 180 px downward) */
    private void showWinner(String name) {
        String msg = name.equals("No one") ? "It's a draw!" : name + " wins!";
        JDialog d = new JDialog(this, "Result", true);
        d.setSize(260, 120);
        d.setLayout(new BorderLayout());
        JLabel lbl = new JLabel(msg, SwingConstants.CENTER);
        lbl.setFont(TITLE_FONT.deriveFont(20f));
        d.add(lbl, BorderLayout.CENTER);

        JButton ok = new JButton("Play again");
        styliseButton(ok);
        ok.addActionListener(e -> { d.dispose(); resetGame(); });
        d.add(ok, BorderLayout.SOUTH);

        d.setUndecorated(true);
        d.setLocation(getX() + getWidth() / 2 - 130, getY() - 150);
        d.setVisible(true);

        Timer t = new Timer(15, null);
        final int[] y = {0};
        t.addActionListener(e -> {
            d.setLocation(d.getX(), d.getY() + 5);
            if ((y[0] += 5) >= 180) t.stop();
        });
        t.start();
    }

    /** Clear arrays, re‑enable & blank buttons, reset turn */
    private void resetGame() {
        playerOne.clear();
        playerTwo.clear();
        turn = 0;
        for (JButton[] row : cells)
            for (JButton b : row) {
                b.setText("");
                b.setEnabled(true);
                b.setBackground(SECONDARY);
            }
    }

    /* ----------  SMALL UI HELPERS  ---------- */

    private void addHoverEffect(JButton b) {
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (b.isEnabled()) b.setBackground(new Color(220, 240, 255)); }
            @Override public void mouseExited (MouseEvent e) { if (b.isEnabled()) b.setBackground(SECONDARY); }
        });
    }

    private void styliseButton(JButton b) {
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setBackground(PRIMARY);
        b.setForeground(SECONDARY);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    }

    private void styliseTextField(JTextField t, String hint) {
        t.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        t.setFont(new Font("SansSerif", Font.PLAIN, 16));
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 2),
                new EmptyBorder(5, 10, 5, 10)));
        t.setToolTipText(hint);          // shows on hover (acts as placeholder)
    }

    /* -------------------  MAIN ENTRY  ------------------- */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new XandO().setVisible(true));
    }
}
