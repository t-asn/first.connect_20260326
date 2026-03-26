import java.util.Scanner;

class Main {

  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);

    int totalSales = 0;
    int customerCount = 0;

    // 24時間営業にするためのwhile文
    while (true) {
      System.out.println("============================================");
      System.out.println(
          "いらっしゃいませ。携行缶、又は自走しない製品への給油は法律で禁止されております");

      // 現在の各油種ごとの価格を表示
      System.out.println("ハイオク" + GasType.HIGH_OCTANE.getPrice() + "円");
      System.out.println("レギュラー" + GasType.REGULAR.getPrice() + "円");
      System.out.println("軽油" + GasType.DIESEL.getPrice() + "円");

      // 油種選択
      GasType type = SelectGasType.select(sc);

      // 給油量を指定
      double fuelFull = InputFuel.input(sc);

      // 50L以上の給油の場合50円割引
      int gassPrice = type.getDiscountedPrice(fuelFull);

      // 決済方法選択
      PaymentMethod payMethod = PaySelect.input(sc);

      // 静電気除去パッド案内として1秒待たせる
      Animation.perform("静電気除去パッドに触れてから給油を開始してください", 1);

      // 給油中として5秒待たせる
      Animation.perform("給油中", 5);

      // 金額処理
      double finalPrice = gassPrice * fuelFull;
      System.out.println("金額は" + (int) finalPrice + "円です。");

      // 売上記録
      totalSales += (int) finalPrice;
      customerCount++;

      Animation.perform("キャップの締め忘れにご注意ください", 1);

      // もし5000円以上給油した場合クーポンを発行する
      if (finalPrice >= 5000) {
        Animation.perform("クーポンを発行しました！", 2);
      }

      // 清算終了の客が入れ替わるまで7秒待つ
      Animation.perform("ご利用誠にありがとうございました！", 7);

      /**
       * 締め作業時のパスコード付きループ終了画面。
       * 0000と入力すると終了する。
       */
      System.out.println("1:次へ");
      int shopStatus = sc.nextInt();

      // 以下隠し画面
      if (shopStatus == 1234) {
        System.out.println("本日の営業を終了します。売上を集計中....");
        break;
      }
    }
    System.out.println("============================================");
    System.out.println("【本日の売上】");
    System.out.println("総客数　: " + customerCount + " 名");
    System.out.println("総売上　: " + totalSales + " 円");
    System.out.println("============================================");
  }
}