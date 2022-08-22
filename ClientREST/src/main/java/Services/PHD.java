package Services;

import Config.DB;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/services")
public class PHD {

    private final static String TABLE_GRAD = "phds_graduationyear";
    private final static String TABLE_ACAD = "phds_academicyear";

    @Path("/phd/getTotalpergradyear")
    @GET
    public Response get1() throws ClassNotFoundException, IOException, SQLException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();

        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT * FROM ").append(TABLE_GRAD).append(";");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            result.append("<thead><tr><th>YEAR</th><th>GRADUATION YEAR</th>")
                    .append("<th>DEPARTMENT</th><th>GENDER</th><th>VALUE</th></tr></thead><tbody>");
            while (res.next() == true) {
                result.append("<tr>")
                        .append("<td>").append(res.getString("YEAR")).append("</td>")
                        .append("<td>").append(res.getString("GRADUATION_YEAR")).append("</td>")
                        .append("<td>").append(res.getString("FACULTY")).append("</td>")
                        .append("<td>").append(res.getString("GENDER")).append("</td>")
                        .append("<td>").append(res.getString("VALUE")).append("</td>")
                        .append("</tr>");
            }
            result.append("</tbody></table>");
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return getResponse(result.toString(), "text/html");
    }

    @Path("/phd/getTotalperacadyear")
    @GET
    public Response get2() throws ClassNotFoundException, IOException, SQLException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();

        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT * FROM ").append(TABLE_ACAD).append(";");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            result.append("<thead><tr><th>YEAR</th><th>ACADEMIC YEAR</th>")
                    .append("<th>DEPARTMENT</th><th>GENDER</th><th>VALUE</th></tr></thead><tbody>");
            while (res.next() == true) {
                result.append("<tr>")
                        .append("<td>").append(res.getString("YEAR")).append("</td>")
                        .append("<td>").append(res.getString("STUDENT_YEAR")).append("</td>")
                        .append("<td>").append(res.getString("FACULTY")).append("</td>")
                        .append("<td>").append(res.getString("GENDER")).append("</td>")
                        .append("<td>").append(res.getString("VALUE")).append("</td>")
                        .append("</tr>");
            }
            result.append("</tbody></table>");
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return getResponse(result.toString(), "text/html");
    }

    @Path("/phd/getGrandTotalofAcademicYears/{year}")
    @GET
    public Response get3(@PathParam("year") String year) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder title = new StringBuilder();
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT STUDENT_YEAR, VALUE FROM ").append(TABLE_ACAD).append(" WHERE GENDER = 'Grand Total' AND YEAR = '")
                    .append(year).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            StringBuilder result = new StringBuilder();
            StringBuilder final_path = new StringBuilder();
            result.append("year,value\n");
            while (res.next() == true) {
                result.append(res.getString("STUDENT_YEAR")).append(",").append(res.getString("VALUE")).append("\n");
            }

            title.append("Total number of PHD students in ").append(year).append(" per academic year=").append("Academic Year");
            java.nio.file.Path path = Paths.get("file.txt");
            final_path.append(path.toAbsolutePath()).append("\\webapps\\ClientREST\\Data\\pay").append(year).append(".csv");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(final_path.toString()))) {
                writer.write(result.substring(0, result.length() - 1));
            } catch (Exception ex) {
                System.out.println(ex );
            }

        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return getResponse(title.toString(), "text/html");
    }

    @Path("/phd/getGrandTotalofGraduationYears/{year}")
    @GET
    public Response get4(@PathParam("year") String year) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder title = new StringBuilder();
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT GRADUATION_YEAR, VALUE FROM ").append(TABLE_GRAD).append(" WHERE GENDER = 'Grand Total' AND YEAR = '")
                    .append(year).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            StringBuilder result = new StringBuilder();
            StringBuilder final_path = new StringBuilder();
            result.append("year,value\n");
            while (res.next() == true) {
                result.append(res.getString("GRADUATION_YEAR")).append(",").append(res.getString("VALUE")).append("\n");
            }
            title.append("Total number of PHD students in ").append(year).append(" per graduation year=").append("Graduation Year");
            java.nio.file.Path path = Paths.get("file.txt");
            final_path.append(path.toAbsolutePath().getParent().getParent()).append("\\webapps\\ClientREST\\Data\\pgy").append(year).append(".csv");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(final_path.toString()))) {
                writer.write(result.substring(0, result.length() - 1));
            } catch (Exception ex) {
                System.out.println(ex );
            }

        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return getResponse(title.toString(), "text/html");
    }

    private Response getResponse(String result, String type) throws IOException {
        return Response.status(Response.Status.OK)
                .header("Content-type", type)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Expose-Headers", "*")
                .header("Access-Control-Allow-Methods", "GET")
                .header("Cache-Control", "max-age=0")
                .entity(result).build();
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
}
