import java.util.Scanner;

public class PaySelect {

  /**
   * 決済方法を選択するクラス。
   *
   * @param sc ユーザの入力を受け付けるためのScannerオブジェクト。
   * @return 選択された決済方法に対応する。
   */
  public static PaymentMethod input(Scanner sc) {
    int choice = 0;
    while (true) {
      System.out.println("お支払方法を選択してください（1：現金 ,2：カード ,3：プリカ");
      choice = sc.nextInt();

      // 例外処理
      if (choice >= 1 && choice <= 3) {
        break;
      }
      System.out.println("正しい番号を入力してください。");
    }

    PaymentMethod method = switch (choice) {

      // 現金
      case 1 -> PaymentMethod.CASH;

      // クレジットカード
      case 2 -> PaymentMethod.CARD;

      // プリペイドカード
      case 3 -> PaymentMethod.PREPAID;
      default -> null;
    };

    System.out.println(method.getDisplayName() + "にて給油開始します。");
    return method;
  }
}
