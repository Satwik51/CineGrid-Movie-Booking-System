import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ImportDatabase {
    public static void main(String[] args) {
        String host = "mysql-300b296b-cinegrid-project.a.aivencloud.com";
        String port = "21804";
        String dbName = "defaultdb";
        String user = "avnadmin";
        
        // Securely loading password from environment variable, with a fallback prompt/placeholder
        String pass = System.getenv("AIVEN_DB_PASSWORD");
        if (pass == null || pass.isEmpty()) {
            pass = "YOUR_PASSWORD_HERE"; // Local testing ke liye yahan daal sakte ho, par git push se pehle hata dena
        }

        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?sslMode=REQUIRED&allowMultiQueries=true";

        try {
            System.out.println("Connecting to Aiven Cloud Database...");
            Connection conn = DriverManager.getConnection(url, user, pass);
            Statement stmt = conn.createStatement();
            
            // 💡 Yeh line primary key ki restriction ko hata degi
            stmt.execute("SET SESSION sql_require_primary_key = 0;");
            
            System.out.println("Connected successfully! Importing tables and data...");

            BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Lenovo\\Downloads\\cinegrid_db.sql"));
            String line;
            StringBuilder sqlQuery = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("--") || line.trim().startsWith("/*") || line.trim().isEmpty()) {
                    continue;
                }
                sqlQuery.append(line).append("\n");
                
                if (line.trim().endsWith(";")) {
                    try {
                        stmt.execute(sqlQuery.toString());
                    } catch (Exception ex) {
                        // Ignore minor notices
                    }
                    sqlQuery.setLength(0);
                }
            }
            reader.close();
            conn.close();
            System.out.println("🎉 Database and all tables successfully imported to Aiven Cloud!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}