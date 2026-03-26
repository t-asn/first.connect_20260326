import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class GasStationWebServer {
  // --- 店舗データ蓄積用変数 ---
  private static int totalSales = 0;
  private static int customerCount = 0;
  private static double totalLiters = 0;
  private static List<String> history = new ArrayList<>();

  public static void main(String[] args) throws IOException {
    // ポート8080でサーバー起動
    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
    server.createContext("/", new MyHandler());

    System.out.println("========================================");
    System.out.println("   セルフガソリンスタンドシステム 起動中");
    System.out.println("========================================");
    System.out.println("▶ お客さま用URL: http://localhost:8080");
    System.out.println("▶ 管理者用URL  : http://localhost:8080/admin");
    System.out.println("========================================");

    server.start();
  }

  static class MyHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      String path = exchange.getRequestURI().getPath();
      String query = exchange.getRequestURI().getQuery();
      String response = "";

      if (path.equals("/")) {
        response = getFormHtml(); // メイン画面
      } else if (path.equals("/result")) {
        Map<String, String> params = parseQuery(query);
        response = processAndGetReceipt(params); // レシート画面
      } else if (path.equals("/admin")) {
        response = getAdminHtml(); // 管理画面
      }

      exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
      byte[] responseBytes = response.getBytes("UTF-8");
      exchange.sendResponseHeaders(200, responseBytes.length);
      OutputStream os = exchange.getResponseBody();
      os.write(responseBytes);
      os.close();
    }

    // --- 1. エネオス風メイン画面 (決済→油種→数量) ---
    private String getFormHtml() {
      return "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
          "<style>" +
          "  body { font-family: 'Hiragino Kaku Gothic ProN', 'Meiryo', sans-serif; text-align: center; background: #f0f2f5; margin: 0; }" +
          "  header { background: #e60012; color: white; padding: 15px; font-weight: bold; font-size: 20px; box-shadow: 0 4px 10px rgba(0,0,0,0.2); }" +
          "  .container { padding: 20px; max-width: 500px; margin: auto; }" +
          "  .guide { background: #fffde7; border: 2px solid #fff176; padding: 15px; margin-bottom: 25px; font-weight: bold; font-size: 18px; border-radius: 10px; color: #333; }" +
          "  .btn-grid { display: grid; grid-template-columns: 1fr; gap: 15px; }" +
          "  .btn { padding: 25px; border: none; border-radius: 15px; color: white; font-size: 24px; font-weight: bold; cursor: pointer; box-shadow: 0 6px 0 rgba(0,0,0,0.15); transition: 0.1s; width: 100%; }" +
          "  .btn:active { transform: translateY(4px); box-shadow: 0 2px 0 rgba(0,0,0,0.15); }" +
          "  .pay-btn { background: #546e7a; } .hi-oct { background: #e60012; } .reg { background: #ff7d00; } .dsl { background: #2e7d32; }" +
          "  .next-btn { background: #1e88e5; margin-top: 20px; }" +
          "  .num-input { width: 80%; font-size: 50px; text-align: center; border: 3px solid #ccc; border-radius: 10px; margin: 20px 0; padding: 10px; }" +
          "  .step { display: none; } .active { display: block; }" +
          "  .bar-bg { width: 100%; height: 40px; background: #eee; border-radius: 20px; overflow: hidden; border: 2px solid #ddd; }" +
          "  .bar-fill { width: 0%; height: 100%; background: linear-gradient(90deg, #ff7d00, #e60012); }" +
          "  .price { display: block; font-size: 16px; opacity: 0.9; }" +
          "</style></head>" +
          "<body>" +
          "  <header>ASANOS セルフ給油システム</header>" +
          "  <div class='container'>" +
          "    <div class='guide' id='guide'>お支払い方法を選んでください</div>" +
          "    " +
          "    <div id='s1' class='step active btn-grid'>" +
          "      <button class='btn pay-btn' onclick='go2(\"現金\")'>現 金</button>" +
          "      <button class='btn pay-btn' onclick='go2(\"クレジットカード\")'>クレジットカード</button>" +
          "      <button class='btn pay-btn' onclick='go2(\"プリペイド\")'>プリペイドカード</button>" +
          "    </div>" +
          "    " +
          "    <div id='s2' class='step btn-grid'>" +
          "      <button class='btn hi-oct' onclick='go3(\"HIGH_OCTANE\")'>ハイオク<span class='price'>￥" + GasType.HIGH_OCTANE.getPrice() + "</span></button>" +
          "      <button class='btn reg' onclick='go3(\"REGULAR\")'>レギュラー<span class='price'>￥" + GasType.REGULAR.getPrice() + "</span></button>" +
          "      <button class='btn dsl' onclick='go3(\"DIESEL\")'>軽油<span class='price'>￥" + GasType.DIESEL.getPrice() + "</span></button>" +
          "    </div>" +
          "    " +
          "    <div id='s3' class='step'>" +
          "      <h2 id='disp-t'></h2>" +
          "      <input type='number' id='amt' class='num-input' value='20'> <span style='font-size:24px'>L</span><br>" +
          "      <button class='btn next-btn' onclick='start()'>給油開始</button>" +
          "    </div>" +
          "    " +
          "    <div id='s4' class='step'>" +
          "      <h2>給油中...</h2>" +
          "      <div class='bar-bg'><div id='bar' class='bar-fill'></div></div>" +
          "      <p style='font-weight:bold; color:#e60012;'>満タン自動停止までお待ちください</p>" +
          "    </div>" +
          "  </div>" +
          "  <script>" +
          "    let d = { p: '', t: '', a: 0 };" +
          "    function sw(o, n, m) {" +
          "      document.getElementById(o).classList.remove('active');" +
          "      document.getElementById(n).classList.add('active');" +
          "      document.getElementById('guide').innerText = m;" +
          "    }" +
          "    function go2(p) { d.p = p; sw('s1', 's2', '油種を選んでください'); }" +
          "    function go3(t) { " +
          "      d.t = t; " +
          "      document.getElementById('disp-t').innerText = (t=='HIGH_OCTANE'?'ハイオク':t=='REGULAR'?'レギュラー':'軽油') + ' 給油確認';" +
          "      sw('s2', 's3', '数量を入力してください'); " +
          "    }" +
          "    function start() { " +
          "      d.a = document.getElementById('amt').value;" +
          "      sw('s3', 's4', 'ただいま給油中です');" +
          "      let p = 0; const b = document.getElementById('bar');" +
          "      const timer = setInterval(() => {" +
          "        p += 2; b.style.width = p + '%';" +
          "        if(p >= 100){ clearInterval(timer); location.href = '/result?type='+d.t+'&amount='+d.a+'&pay='+encodeURIComponent(d.p); }" +
          "      }, 50);" +
          "    }" +
          "  </script></body></html>";
    }

    // --- 2. 蓄積処理 ＋ レシート表示 ---
    private String processAndGetReceipt(Map<String, String> params) {
      try {
        String typeStr = params.get("type");
        double amount = Double.parseDouble(params.get("amount"));
        String payMethod = params.getOrDefault("pay", "現金");

        GasType type = GasType.valueOf(typeStr);
        int unitPrice = type.getDiscountedPrice(amount);
        int finalPrice = (int)(unitPrice * amount);

        totalSales += finalPrice;
        customerCount++;
        totalLiters += amount;

        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        history.add(0, String.format("[%s] %s | %.1fL | ￥%,d (%s)", time, type.name(), amount, finalPrice, payMethod));

        return "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<style>" +
            "  body { background: #90a4ae; font-family: monospace; display: flex; justify-content: center; padding: 20px; }" +
            "  .receipt { background: #fff; width: 300px; padding: 20px; box-shadow: 0 10px 20px rgba(0,0,0,0.2); border-top: 10px solid #333; position: relative; }" +
            "  .line { border-bottom: 1px dashed #ccc; margin: 15px 0; }" +
            "  .item { display: flex; justify-content: space-between; margin: 5px 0; }" +
            "  .total { font-size: 30px; font-weight: bold; text-align: right; margin-top: 15px; }" +
            "  .btn-back { display: block; text-align: center; margin-top: 30px; padding: 10px; background: #eee; color: #333; text-decoration: none; border-radius: 5px; font-family: sans-serif; }" +
            "</style></head><body><div class='receipt'>" +
            "  <h2 style='text-align:center;'>領 収 書</h2>" +
            "  <p style='text-align:center; font-size:12px;'>ENEOS風 セルフ給油システム</p>" +
            "  <div class='line'></div>" +
            "  <div class='item'><span>決済方法:</span><span>" + payMethod + "</span></div>" +
            "  <div class='line'></div>" +
            "  <div class='item'><span>油種:</span><span>" + type.name() + "</span></div>" +
            "  <div class='item'><span>数量:</span><span>" + amount + " L</span></div>" +
            "  <div class='item'><span>単価:</span><span>￥" + unitPrice + "</span></div>" +
            "  <div class='line'></div>" +
            "  <div class='item' style='font-weight:bold;'>合計金額:</div>" +
            "  <div class='total'>￥" + String.format("%,d", finalPrice) + "</div>" +
            "  <div style='text-align:center; margin-top:20px; font-size:12px;'>毎度ありがとうございます</div>" +
            "  <a href='/' class='btn-back'>トップに戻る</a>" +
            "</div></body></html>";
      } catch (Exception e) {
        return "エラー: " + e.getMessage();
      }
    }

    // --- 3. 管理者ダッシュボード ---
    private String getAdminHtml() {
      StringBuilder histHtml = new StringBuilder();
      if (history.isEmpty()) {
        histHtml.append("<li style='color:#888; text-align:center;'>まだ取引履歴はありません</li>");
      } else {
        for(int i = 0; i < Math.min(history.size(), 10); i++) {
          histHtml.append("<li>").append(history.get(i)).append("</li>");
        }
      }

      return "<html><head><meta charset='UTF-8'>" +
          "<style>" +
          "  body { font-family: sans-serif; background: #1a1a1a; color: #eee; padding: 20px; }" +
          "  .dashboard { max-width: 800px; margin: auto; }" +
          "  .grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin: 20px 0; }" +
          "  .stat { background: #2d2d2d; padding: 20px; border-radius: 10px; text-align: center; border-bottom: 4px solid #00e5ff; }" +
          "  .val { font-size: 28px; font-weight: bold; color: #00e5ff; }" +
          "  h1 { border-left: 8px solid #e60012; padding-left: 15px; }" +
          "  ul { background: #2d2d2d; padding: 15px; border-radius: 10px; list-style: none; }" +
          "  li { border-bottom: 1px solid #3d3d3d; padding: 8px 0; font-family: monospace; }" +
          "  button { background: #444; color: white; border: none; padding: 10px 20px; cursor: pointer; border-radius: 5px; }" +
          "</style></head><body><div class='dashboard'>" +
          "  <h1>📊 店舗管理ダッシュボード</h1>" +
          "  <button onclick='location.reload()'>🔄 データを更新</button>" +
          "  <div class='grid'>" +
          "    <div class='stat'><div>本日売上</div><div class='val'>￥" + String.format("%,d", totalSales) + "</div></div>" +
          "    <div class='stat'><div>客数</div><div class='val'>" + customerCount + " 名</div></div>" +
          "    <div class='stat'><div>合計給油量</div><div class='val'>" + String.format("%.1f", totalLiters) + " L</div></div>" +
          "  </div>" +
          "  <h2>最近の取引履歴 (最新10件)</h2>" +
          "  <ul>" + histHtml.toString() + "</ul>" +
          "</div></body></html>";
    }

    private Map<String, String> parseQuery(String query) {
      Map<String, String> res = new HashMap<>();
      if (query != null) {
        for (String p : query.split("&")) {
          String[] kv = p.split("=");
          if (kv.length > 1) res.put(kv[0], URLDecoder.decode(kv[1]));
        }
      }
      return res;
    }
  }
}