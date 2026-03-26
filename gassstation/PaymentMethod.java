public enum PaymentMethod {

  /**
   * 支払方法を定義するEnum
   */
  CASH("現金"),
  CARD("クレジットカード"),
  PREPAID("プリペイドカード");

  private final String displayName;

  PaymentMethod(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return this.displayName;
  }
}
