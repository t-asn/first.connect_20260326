import java.util.Scanner;

public class SelectGasType {

  /**
   * 給油する油種を選択するクラス。
   *
   * @param sc ユーザの入力を受け付けるためのScannerオブジェクト。
   * @return 指定された油種に対応する。
   */
  public static GasType select(Scanner sc) {

    int choice;
    while (true) {
      System.out.println("油種を選択してください (1:ハイオク 2:レギュラー 3:軽油)");
      choice = sc.nextInt();

      // 1~3以外が入力された場合の例外処理
      if (choice == 1 || choice == 2 || choice == 3) {
        break;
      }
      System.out.println("正しい番号を入力してください");
    }

    if (choice == 1) {
      return GasType.HIGH_OCTANE;
    }
    if (choice == 2) {
      return GasType.REGULAR;
    }
    return GasType.DIESEL;
  }
}