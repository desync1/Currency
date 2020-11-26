import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class GetCurrencyThread implements Runnable {

    private LocalDate localDate;
    private CurrencyDatabase db;
    private String proxyString;

    public GetCurrencyThread(LocalDate localDate, CurrencyDatabase db, String proxyString) {
        this.localDate = localDate;
        this.db = db;
        this.proxyString = proxyString;
    }


    @Override
    public void run() {
        try {
            String[] proxyS = proxyString.split("[:]");
            String ip = proxyS[0];
            int port = Integer.parseInt(proxyS[1]);
            InetSocketAddress proxyAddr = new InetSocketAddress(ip, port);
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddr);
            Date date = java.sql.Date.valueOf(localDate);
            StringBuilder dates = new StringBuilder();
            dates.append(localDate.getDayOfMonth())
                    .append(".")
                    .append(localDate.getMonthValue())
                    .append(".")
                    .append(localDate.getYear());
            URL url = new URL("https://api.privatbank.ua/p24api/exchange_rates?json&date=" + dates.toString());
            HttpURLConnection http = (HttpURLConnection) url.openConnection(proxy);
            try (InputStream is = http.getInputStream()) {
                byte[] buf = Helper.responseBodyToArray(is);
                String strBuf = new String(buf, StandardCharsets.UTF_8);
                Answer answer = Answer.fromJSON(strBuf);
                List<ExchangeRate> rates = answer.getExchangeRate();
                for (ExchangeRate rate : rates) {

                    if (rate.getCurrency() == null) {
                        continue;
                    }
                    if (rate.getCurrency().equals("USD")) {
                        String baseCurrency = rate.getBaseCurrency();
                        String currency = rate.getCurrency();
                        double saleRateNB = rate.getSaleRateNB();
                        double purchaseRateNB = rate.getPurchaseRateNB();
                        double saleRate = rate.getSaleRate();
                        double purchaseRate = rate.getPurchaseRate();
                        db.addExchangeRate(date, baseCurrency, currency, saleRateNB, purchaseRateNB, saleRate, purchaseRate);
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
