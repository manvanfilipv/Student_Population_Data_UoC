package main;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellUtil;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "UploadServlet", urlPatterns = {"/UploadServlet"})
public class UploadServlet extends HttpServlet {

    private boolean isMultipart;
    private String filePath;
    private String PassPath;
    private final int maxFileSize = 1000 * 1024;
    private final int maxMemSize = 4 * 1024;
    private File file;

    @Override
    public void init() {
        Path path = Paths.get("file.txt");
        filePath = path.toAbsolutePath().getParent().getParent()+"\\webapps\\Upload\\";
        PassPath = path.toAbsolutePath().getParent().getParent()+"\\webapps\\Upload\\Config\\password.txt";
    }
    

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {

        // Check that we have a file upload request
        isMultipart = ServletFileUpload.isMultipartContent(request);
        response.setContentType("text/html");
        java.io.PrintWriter out = response.getWriter();
        
        if (!isMultipart) {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet upload</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<p>No file uploaded</p>");
            out.println("</body>");
            out.println("</html>");
            return;
        }

        DiskFileItemFactory factory = new DiskFileItemFactory();

        // maximum size that will be stored in memory
        factory.setSizeThreshold(maxMemSize);

        // Location to save data that is larger than maxMemSize.
        factory.setRepository(new File(filePath));

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // maximum file size to be uploaded.
        upload.setSizeMax(maxFileSize);

        try {
            // Parse the request to get file items.
            List fileItems = upload.parseRequest(request);

            // Process the uploaded file items
            Iterator i = fileItems.iterator();

            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet upload</title>");
            out.println("</head>");
            out.println("<body>");
            Boolean flag = false;
            while (i.hasNext()) {
                FileItem fi = (FileItem) i.next();
                if (fi.isFormField()) {
                    if (fi.getFieldName().equals("pwd")) {
                        String password = fi.getString();
                        if (password.equals(getPassword())){
                            flag = true;
                        }
                    }
                }
            }
            if (!flag) {
                out.println("#DB: Authentication failed");
                return;
            }

            i = fileItems.iterator();
            while (i.hasNext()) {
                FileItem fi = (FileItem) i.next();
                if (!fi.isFormField()) {
                    // Get the uploaded file parameters
                    //String fieldName = fi.getFieldName();
                    String fileName = fi.getName();
                    //String contentType = fi.getContentType();
                    //boolean isInMemory = fi.isInMemory();
                    //long sizeInBytes = fi.getSize();
                    file = new File(filePath + fileName);
                    fi.write(file);
                    out.println("Uploaded Filename: " + fileName + "<br><br>");
                    out.println(REST_ToDB(fileName));
                    file.delete();
                }
            }
            out.println("</body>");
            out.println("</html>");
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    private String REST_ToDB(String name) throws ClassNotFoundException, FileNotFoundException {
        boolean output = true;
        checkTableExistence(name.substring(0, name.length() - 5).toLowerCase());
        try (InputStream inp = new FileInputStream(filePath + name)) {
            Workbook wb = WorkbookFactory.create(inp);
            int i;
            UploadServlet app = new UploadServlet();
            for (i = 0; i < wb.getNumberOfSheets(); i++) {
                app.newconvertToDB(name.substring(0, name.length() - 5).toLowerCase(), wb.getSheetAt(i));
                //System.exit(0);
            }
            //System.gc();
        } catch (Exception ex) {
            output = false;
        }
        if (output) {
            return "#DB: File successfully converted to table";
        } else {
            return "#DB: File failed to converted to table";
        }

    }
    
    private String getPassword() throws FileNotFoundException, IOException {
        String content = "";
        try {
            content = new String (Files.readAllBytes(Paths.get(PassPath)));
        } 
        catch (IOException e) {
        }
        return content;
    }

    private String department_mapping(String dep) {
        switch (dep) {
            case "Σύνολα":
                dep = "All";
                break;
            case "Σύνολο":
                dep = "All";
                break;
            case "Π.Τ.Δ.Ε.":
                dep = "Department of Primary Education";
                break;
            case "Π.Τ.Π.Ε.":
                dep = "Department of Preschool Education";
                break;
            case "ΙΣΤ. Α.":
                dep = "Department of History and Archaeology";
                break;
            case "ΦΙΛ":
                dep = "Department of Philology";
                break;
            case "ΦΚΣ":
                dep = "Department of Philosophy and Social Studies";
                break;
            case "ΦΥΣΙΚΗΣ":
                dep = "Department of Physics";
                break;
            case "ΒΙΟΛ":
                dep = "Department of Biology";
                break;
            case "ΥΠΟΛ":
                dep = "Computer Science Department";
                break;
            case "ΤΕΜ":
                dep = "Applied Mathematics";
                break;
            case "ΜΑΘΗΜ":
                dep = "Mathematics";
                break;
            case "ΤΕΤΥ":
                dep = "Department of Materials Science and Technology";
                break;
            case "ΧΗΜΕΙΑ":
                dep = "Department of Chemistry";
                break;
            case "Ιατρική Σχολή":
                dep = "School of Medicine";
                break;
            case "ΙΑΤΡΙΚΗ":
                dep = "School of Medicine";
                break;
            case "ΨΥΧΟΛ":
                dep = "Department of Psychology";
                break;
            case "ΠΟΛ":
                dep = "Department Of Political Science";
                break;
            case "ΚΟΙΝΩΝ":
                dep = "Department of Sociology";
                break;
            case "ΟΙΚΟΝ":
                dep = "Department of Economics";
                break;
        }
        return dep;
    }

    private String number_mapping(String number) {
        switch (number) {
            case "Σύνολο":
                number = "Total";
                break;
            case "ΑΗ":
                number = "Unkwown Age";
                break;
            case "K":
                number = "K";
                break;
            case "Κ+1":
                number = "K+1";
                break;
            case "Κ+2":
                number = "K+2";
                break;
            case "Κ+3":
                number = "K+3";
                break;
            case " Κ+4":
                number = "K+4";
                break;
            case "Κ+4":
                number = "K+4";
                break;
            case "> Κ+4":
                number = "> K+4";
                break;
            case "K+5":
                number = "K+5";
                break;
            case "K+6":
                number = "K+6";
                break;
            case "> Κ+6":
                number = "> K+6";
                break;
            case ">K+4":
                number = ">K+4";
                break;
            case ">K+1":
                number = ">K+1";
                break;
        }
        return number;
    }

    private String gender_mapping(String dep, String gender) {
        switch (gender) {
            case "Α":
                gender = "Male";
                break;
            case "Γ":
                gender = "Female";
                break;
            case "Θ":
                gender = "Female";
                break;
            case "Σύνολο":
                if (dep.equals("All")) {
                    gender = "Grand Total";
                } else {
                    gender = "Total";
                }
                break;
        }
        return gender;
    }

    private void newconvertToDB(String table_name, Sheet sheet) {
        Iterator<Row> rowIterator = sheet.iterator();
        int col = 3;
        int title_col = 1;
        int column_counter = 1;
        int counter = 0;
        Row row;
        String temp;
        String temp2;
        String temp3;
        String year = sheet.getSheetName();
        String faculty = "";
        String gender = "";
        while (col < 72) {
            while (rowIterator.hasNext()) {
                row = rowIterator.next();
                if (CellUtil.getCell(row, col).getCellType() == CellType.BLANK) {
                    continue;
                }
                if (CellUtil.getCell(row, col).getCellType() == CellType.NUMERIC) {
                    if (CellUtil.getCell(row, title_col).getCellType() == CellType.NUMERIC) {
                        try {
                            temp = String.valueOf(CellUtil.getCell(row, title_col).getNumericCellValue());
                            temp2 = String.valueOf(CellUtil.getCell(row, col).getNumericCellValue());
                            temp3 = department_mapping(faculty);
                            addRecord(table_name, year, temp.substring(0, temp.length() - 2), temp3, gender_mapping(temp3, gender), temp2.substring(0, temp2.length() - 2));
                        } catch (ClassNotFoundException | FileNotFoundException ex) {
                            System.out.println("1");
                        }
                    } else if (CellUtil.getCell(row, title_col).getCellType() == CellType.STRING) {
                        try {
                            temp2 = String.valueOf(CellUtil.getCell(row, col).getNumericCellValue());
                            temp3 = department_mapping(faculty);
                            addRecord(table_name, year, number_mapping(CellUtil.getCell(row, title_col).getStringCellValue()), temp3, gender_mapping(temp3, gender), temp2.substring(0, temp2.length() - 2));
                        } catch (ClassNotFoundException | FileNotFoundException ex) {
                            System.out.println("2");
                        }
                    }
                } else if (CellUtil.getCell(row, col).getCellType() == CellType.STRING) {
                    if (CellUtil.getCell(row, col).getStringCellValue().equals("no data")) {
                        if (CellUtil.getCell(row, title_col).getCellType() == CellType.NUMERIC) {
                            try {
                                temp = String.valueOf(CellUtil.getCell(row, title_col).getNumericCellValue());
                                temp3 = department_mapping(faculty);
                                addRecord(table_name, year, temp.substring(0, temp.length() - 2), temp3, gender_mapping(temp3, gender), CellUtil.getCell(row, col).getStringCellValue());
                            } catch (ClassNotFoundException | FileNotFoundException ex) {
                                System.out.println("3");
                            }
                        } else if (CellUtil.getCell(row, title_col).getCellType() == CellType.STRING) {
                            try {
                                temp3 = department_mapping(faculty);
                                addRecord(table_name, year, number_mapping(CellUtil.getCell(row, title_col).getStringCellValue()), temp3, gender_mapping(temp3, gender), CellUtil.getCell(row, col).getStringCellValue());
                            } catch (ClassNotFoundException | FileNotFoundException ex) {
                                System.out.println("4");
                            }
                        }
                    }
                    if (counter == 0) {
                        faculty = CellUtil.getCell(row, col).getStringCellValue();
                        counter = 1;
                    } else if (counter == 1 && !CellUtil.getCell(row, col).getStringCellValue().equals("no data")) {
                        gender = CellUtil.getCell(row, col).getStringCellValue();
                    }
                }
            }
            column_counter++;
            col++;
            if (column_counter == 4) {
                counter = 0;
                col++;
                column_counter = 1;
            }
            rowIterator = sheet.iterator();
        }
    }

    private static String decide_ColumnName(String table_name) throws ClassNotFoundException {
        String result = table_name.toLowerCase();
        if (result.equals("phds_graduationyear")
                || result.equals("graduatestudents_graduationyear")
                || result.equals("undergraduatestudents_graduationyear")) {
            result = "GRADUATION_YEAR";
        } else if (result.equals("undergraduatestudents_age")) {
            result = "AGE";
        } else if (result.equals("graduatestudents_academicyear")
                || result.equals("undergraduatestudents_academicyear")
                || result.equals("phds_academicyear")) {
            result = "STUDENT_YEAR";
        }

        return result;
    }

    private static void checkTableExistence(String table_name) throws ClassNotFoundException, FileNotFoundException {
        Statement stmt = null;
        Connection con = null;
        try {
            con = DB.getConnection();
            DatabaseMetaData md = con.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            Boolean exists = false;
            while (rs.next()) {
                if (rs.getString(3).equals(table_name)) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                deleteTable(table_name);
            }
            addTable(table_name);
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
    }

    private static void addRecord(String table_name, String year, String number, String faculty, String gender, String value) throws ClassNotFoundException, FileNotFoundException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder insQuery = new StringBuilder();
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            
            insQuery.append("INSERT INTO ")
                    .append(table_name)
                    .append(" (YEAR, ")
                    .append(decide_ColumnName(table_name))
                    .append(", FACULTY, GENDER, VALUE)")
                    .append(" VALUES (")
                    .append("'").append(year).append("',")
                    .append("'").append(number).append("',")
                    .append("'").append(faculty).append("',")
                    .append("'").append(gender).append("',")
                    .append("'").append(value).append("');");
            
            stmt.execute(insQuery.toString());
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
    }

    private static void addTable(String table_name) throws ClassNotFoundException, FileNotFoundException {
        Statement stmt = null;
        Connection con = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("CREATE TABLE ").append(table_name).append(" (YEAR text, ")
                    .append(decide_ColumnName(table_name)).append(" text, FACULTY text, GENDER text, VALUE text);");
            stmt.execute(insQuery.toString());
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
    }

    private static void deleteTable(String table_name) throws ClassNotFoundException, FileNotFoundException {
        Statement stmt = null;
        Connection con = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("DROP TABLE ").append(table_name).append(";");
            stmt.execute(insQuery.toString());

        } catch (SQLException ex) {
            Logger.getLogger(Connection.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
    }

    private static void closeDBConnection(Statement stmt, Connection con) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {

        throw new ServletException("GET method used with "
                + getClass().getName() + ": POST method required.");
    }
}
