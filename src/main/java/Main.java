import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    static final String DB_CONNECTION = "jdbc:mysql://localhost:3306/currencydb?serverTimezone=Europe/Kiev";
    static final String DB_USER = "root";
    static final String DB_PASS = "aZmOrS,88";

    public static void main(String[] args) {
        List<String> proxy = new ArrayList<>();
        try (Scanner s = new Scanner(new FileReader(new File("src/main/resources/proxy.txt")))) {
            while (s.hasNext()) {
                proxy.add(s.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        LocalDate dateNow = LocalDate.now();
        try {
            Connection conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASS);
            CurrencyDatabase db = new CurrencyDatabase(conn);
            db.init();


            List<Thread> threads = new ArrayList<>();

            for (int i = 0; i < 365; i++) {
                LocalDate change = dateNow.minusDays(i);
                Thread th = new Thread(new GetCurrencyThread(change, db, proxy.get(i)));
                th.start();
                threads.add(th);
            }

            for (Thread th : threads) {
                try {
                    th.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            double avg = db.request("avg");
            double max = db.request("max");
            double min = db.request("min");
            System.out.println("max currency - " + max);
            System.out.println("average currency - " + avg);
            System.out.println("min currency - " + min);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }


    }


}
