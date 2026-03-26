import javax.swing.*;
import java.awt.*;

public class GasStationGUI extends JFrame {

  private JLabel statusLabel;
  private JLabel salesLabel;
  private int totalSales = 0;
  private JButton hiOctaneButton;
  private JButton regularButton;
  private JButton dieselButton;
  private JProgressBar progressBar;

  public GasStationGUI() {
    setTitle("セルフガソリンスタンド・精算機");
    setSize(500, 350);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout(10, 10));

    // --- 1. パーツの初期化 (1回ずつ!) ---
    statusLabel = new JLabel("いらっしゃいませ！油種を選択してください", JLabel.CENTER);
    statusLabel.setFont(new Font("MS ゴシック", Font.BOLD, 18));
    statusLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

    salesLabel = new JLabel("本日の総売上: 0円", JLabel.RIGHT);
    salesLabel.setFont(new Font("Arial", Font.BOLD, 14));
    salesLabel.setForeground(Color.BLUE);

    progressBar = new JProgressBar(0, 100);
    progressBar.setStringPainted(true);
    progressBar.setPreferredSize(new Dimension(300, 25));

    // ボタン作成
    hiOctaneButton = new JButton("<html>ハイオク<br>"+GasType.HIGH_OCTANE.getPrice()+"円</html>");
    regularButton = new JButton("<html>レギュラー<br>"+GasType.REGULAR.getPrice()+"円</html>");
    dieselButton = new JButton("<html>軽油<br>"+GasType.DIESEL.getPrice()+"円</html>");

    hiOctaneButton.setBackground(new Color(255, 200, 200));
    regularButton.setBackground(new Color(200, 255, 200));
    dieselButton.setBackground(new Color(200, 200, 255));

    // ボタンに命を吹き込む (イベント設定)
    hiOctaneButton.addActionListener(e -> selectGas(GasType.HIGH_OCTANE));
    regularButton.addActionListener(e -> selectGas(GasType.REGULAR));
    dieselButton.addActionListener(e -> selectGas(GasType.DIESEL));

    // --- 2. レイアウトの組み立て ---

    // 中央：ボタンの小部屋
    JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
    buttonPanel.add(hiOctaneButton);
    buttonPanel.add(regularButton);
    buttonPanel.add(dieselButton);

    // 下部：売上とバーの小部屋
    JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 5, 5));
    bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
    bottomPanel.add(salesLabel);
    bottomPanel.add(progressBar);

    // --- 3. ウィンドウ（全体の板）に小部屋を配置 ---
    add(statusLabel, BorderLayout.NORTH);
    add(buttonPanel, BorderLayout.CENTER);
    add(bottomPanel, BorderLayout.SOUTH);

    setVisible(true);
  }

  // --- 以降のメソッド（selectGas, startFueling, showReceipt, setButtonsEnabled）は変更なし ---
  // (ここから下は前のコードと同じなので、そのまま残していればOKです)

  private void selectGas(GasType type) {
    statusLabel.setText(type.name() + "が選択されました！");
    String input = JOptionPane.showInputDialog(this, "給油量を入力してください（L）");
    if (input == null || input.isEmpty()) return;
    try {
      double amount = Double.parseDouble(input);
      String[] options = {"現金", "カード", "プリカ"};
      JOptionPane.showOptionDialog(this, "支払い方法を選んでください",
          "決済選択", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
          null, options, options[0]);
      int unitPrice = type.getDiscountedPrice(amount);
      int finalPrice = (int) (unitPrice * amount);
      startFueling(type, amount, unitPrice, finalPrice);
    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(this, "数字を正しく入力してください。");
    }
  }

  private void startFueling(GasType type, double amount, int unitPrice, int finalPrice) {
    setButtonsEnabled(false);
    statusLabel.setText("給油中... ノズルを離さないでください");
    new Thread(() -> {
      try {
        for (int i = 0; i <= 100; i += 2) {
          final int progress = i;
          SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
          Thread.sleep(50);
        }
        SwingUtilities.invokeLater(() -> {
          statusLabel.setText("給油完了！");
          showReceipt(type, amount, unitPrice, finalPrice);
          totalSales += finalPrice;
          salesLabel.setText("本日の総売上: " + totalSales + "円");
          setButtonsEnabled(true);
          progressBar.setValue(0);
          statusLabel.setText("いらっしゃいませ！油種を選択してください");
        });
      } catch (InterruptedException e) { e.printStackTrace(); }
    }).start();
  }

  private void showReceipt(GasType type, double amount, int unitPrice, int finalPrice) {
    String message = String.format(
        "     【 領収書 】\n" +
            "--------------------------\n" +
            "油種　　: %s\n" +
            "給油量　: %.2f L\n" +
            "単価　　: %d 円\n" +
            "合計金額: %d 円\n" +
            "--------------------------\n" +
            "毎度ありがとうございます！",
        type.name(), amount, unitPrice, finalPrice
    );
    JOptionPane.showMessageDialog(this, message, "レシート発行", JOptionPane.INFORMATION_MESSAGE);
  }

  private void setButtonsEnabled(boolean enabled) {
    hiOctaneButton.setEnabled(enabled);
    regularButton.setEnabled(enabled);
    dieselButton.setEnabled(enabled);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new GasStationGUI());
  }
}