import java.sql.*;
import java.util.Date;

public class CurrencyDatabase {
    private final Connection conn;

    public CurrencyDatabase(Connection conn) {
        this.conn = conn;
    }

    public void init() {
        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS ExchangeRate");
            st.execute("CREATE TABLE ExchangeRate(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, dateCurr DATE,baseCurrency VARCHAR(3) NOT NULL, currency VARCHAR(3)" +
                    ", saleRateNB DOUBLE, purchaseRateNB DOUBLE, saleRate DOUBLE, purchaseRate DOUBLE )");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void addExchangeRate(Date date, String baseCurrency, String currency, double saleRateNB,
                                double purchaseRateNB, double saleRate, double purchaseRate) {


        try (PreparedStatement pst = conn.prepareStatement("INSERT INTO ExchangeRate (dateCurr, baseCurrency, currency, saleRateNB," +
                "purchaseRateNB, saleRate,  purchaseRate) VALUES (?, ?, ?, ?, ?, ?, ?)")) {

            pst.setDate(1, (java.sql.Date) date);
            pst.setString(2, baseCurrency);
            pst.setString(3, currency);
            pst.setDouble(4, saleRateNB);
            pst.setDouble(5, purchaseRateNB);
            pst.setDouble(6, saleRate);
            pst.setDouble(7, purchaseRate);

            pst.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public double request(String what) {
        try {
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT " + what.toUpperCase() + "(saleRateNB) as " + what + " FROM ExchangeRate")) {
                    rs.next();
                    return rs.getDouble(1);
                }
            }

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

}
