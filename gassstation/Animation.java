public class Animation {

  /**
   * メッセージを表示しながら、指定された秒数待機する演出クラス。
   * @param message 表示する案内メッセージ
   * @param seconds 待機時間（秒）
   */
  public static void perform(String message, int seconds) {
    System.out.print(message);

    try {
      for (int i = 0; i < seconds; i++) {
        Thread.sleep(1000);
        System.out.print(".");
      }
      System.out.println(" [完了]");
    } catch (InterruptedException e) {
      System.err.println("演出中にエラーが発生しました。");
      e.printStackTrace();
    }
  }
}
