public enum GasType {

  /**
   * 油種ごとに値段を決めるEnum。
   */
  HIGH_OCTANE(176),
  REGULAR(146),
  DIESEL(100);

  public int price;

  GasType(int price) {
    this.price = price;
  }

  public int getPrice() {
    return price;
  }

  /**
   * 50L以上給油した場合L当たり50円の値引きをする。
   *
   * @param amount 指定された給油量。（L)
   * @return 単価から-50円された金額。
   */
  public int getDiscountedPrice(double amount) {
    if (amount >= 50) {
      return this.price - 50;
    }
    return this.price;
  }
}
