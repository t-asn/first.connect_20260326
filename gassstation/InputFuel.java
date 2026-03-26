import java.util.Scanner;

public class InputFuel {

  /**
   * 給油量を指定するクラス。
   *
   * @param sc ユーザの入力を受け付けるためのScannerオブジェクト。
   * @return 指定された給油量に対応する
   */
  public static double input(Scanner sc) {
    double fuelFull = 0;
    while (true) {
      System.out.println("給油量を入力してください（ /L）");
      fuelFull = sc.nextDouble();

      // 0Lより少なく100L以上の場合例外処理
      if (fuelFull > 0 && fuelFull <= 100) {
        break;
      }
      System.out.println("0～100Lで指定してください");
    }
    return fuelFull;
  }
}
