package Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DB {

    private static String URL = "jdbc:mysql://localhost";
    private static String DATABASE = "test";
    private static int PORT = 3306;
    private static String UNAME = "root";
    private static String PASSWD = "";
    private static final int LOCALSER = 0;
    
    private static void getInfo() throws FileNotFoundException {
        Path path = Paths.get("file.txt");
        File myObj = new File(path.toAbsolutePath().getParent()+"\\webapps\\ClientREST\\Config\\db.txt");
        try (Scanner myReader = new Scanner(myObj)) {
            int i = 0;
            while (myReader.hasNextLine()) {
                switch (i){
                    case 0: URL = myReader.nextLine();
                    case 1: DATABASE = myReader.nextLine();
                    case 2: PORT = Integer.parseInt(myReader.nextLine());
                    case 4: UNAME = myReader.nextLine();
                    case 5: PASSWD = myReader.nextLine();
                }
                i++;            
            }
        }
    }

    public static Connection getConnection() throws SQLException, ClassNotFoundException, FileNotFoundException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, e);
        }
        if (LOCALSER == 0) getInfo();  
        
        return DriverManager.getConnection(URL + ":" + PORT + "/" + DATABASE + "?characterEncoding=UTF-8", UNAME, PASSWD);
    }
}
