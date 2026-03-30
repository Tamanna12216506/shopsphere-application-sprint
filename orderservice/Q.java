import java.sql.*;
public class Q {
  public static void main(String[] a) throws Exception {
        try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3307/orderdb?user=root&password=root");
         Statement s = c.createStatement();
         ResultSet r = s.executeQuery("select order_id,user_id,order_status,payment_mode,payment_id,total_amount from orders order by order_id desc limit 10")) {
      while (r.next()) {
        System.out.println(r.getLong(1)+"|"+r.getString(2)+"|"+r.getString(3)+"|"+r.getString(4)+"|"+r.getString(5)+"|"+r.getBigDecimal(6));
      }
    }
  }
}
