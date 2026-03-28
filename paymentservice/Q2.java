import java.sql.*;
public class Q2 {
  public static void main(String[] a) throws Exception {
    try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/paymentdb?user=root&password=root");
         Statement s = c.createStatement();
         ResultSet r = s.executeQuery("select payment_id,order_id,user_id,payment_mode,payment_status,transaction_id from payments order by payment_id desc limit 10")) {
      while (r.next()) {
        System.out.println(r.getLong(1)+"|"+r.getLong(2)+"|"+r.getString(3)+"|"+r.getString(4)+"|"+r.getString(5)+"|"+r.getString(6));
      }
    }
  }
}
