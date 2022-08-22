package Services;

import Config.DB;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@Path("/services")
public class Graduate {

    @Context
    HttpHeaders http;

    private final static String STYLE = "<!DOCTYPE html><html><head><style>{ font-family: Arial, Helvetica, sans-serif; border-collapse: collapse; width: 100%;}td, th { border: 1px solid #ddd; padding: 8px;}tr:nth-child(even){background-color: #f2f2f2;}tr:hover {background-color: #ddd;}th { padding-top: 12px; padding-bottom: 12px; text-align: left; background-color: #4CAF50; color: white;}</style></head><body>";
    private final static String TABLE_GRAD = "graduatestudents_graduationyear";
    private final static String TABLE_ACAD = "graduatestudents_academicyear";

    @Path("/grad/getRecordsperGraduationYear/{year}/{grad_year}")
    @GET
    public Response get1(@Context HttpHeaders http, @PathParam("year") String year, @PathParam("grad_year") String grad_year) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();
        Response resp = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT FACULTY, GENDER, VALUE FROM ").append(TABLE_GRAD).append(" WHERE GRADUATION_YEAR = '")
                    .append(grad_year)
                    .append("' AND YEAR = '")
                    .append(year).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            int choice = chooseformat(http.getAcceptableMediaTypes().get(0).toString());
            if (choice == 1) {
                int counter = 0;
                result.append("[");
                while (res.next() == true) {
                    if (counter == 0) {
                        result.append("{")
                                .append("\"Department\":\"").append(res.getString("FACULTY")).append("\",")
                                .append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\",");
                        counter++;
                    } else if (counter == 1) {
                        result.append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\",");
                        counter++;
                    } else if (counter == 2) {
                        result.append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\"},");
                        counter = 0;
                    }
                }
                resp = getResponse(result.substring(0, result.length() - 1) + "}]", "application/json");
            } else if (choice == 2) {
                int counter = 0;
                result.append("Department;Male;Female;Total\n");
                while (res.next() == true) {
                    if (res.getString("FACULTY").equals("All")){
                        result.append(res.getString("FACULTY")).append(";-1;-1;").append(res.getString("VALUE")).append("\n");
                        counter = 0;
                    } else {
                        if (counter == 0) {
                            result.append(res.getString("FACULTY")).append(";").append(res.getString("VALUE")).append(";");
                            counter++;
                        } else if (counter == 1) {
                            result.append(res.getString("VALUE")).append(";");
                            counter++;
                        } else if (counter == 2) {
                            result.append(res.getString("VALUE")).append("\n");
                            counter = 0;
                        }
                    }
                }
                resp = getResponse(result.substring(0, result.length() - 1), "text/plain");
            } else if (choice == 3) {
                result.append(STYLE).append("<table><tr><th>DEPARTMENT</th><th>GENDER</th><th>VALUE</th></tr>");
                while (res.next() == true) {
                    result.append("<tr>")
                            .append("<td>").append(res.getString("FACULTY")).append("</td>")
                            .append("<td>").append(res.getString("GENDER")).append("</td>")
                            .append("<td>").append(res.getString("VALUE")).append("</td>").append("</tr>");
                }
                result.append("</table></body></html>");
                Document doc = Jsoup.parseBodyFragment(result.toString());
                resp = getResponse(doc.html(), "text/html");
            } else if (choice == 4) {
                result.append("<").append(TABLE_GRAD).append(" year=\"")
                        .append(year).append("\" grad_year=\"")
                        .append(grad_year).append("\">");
                int counter = 0;
                while (res.next() == true) {
                    if (counter == 0) {
                        result.append("<department name=\"").append(res.getString("FACULTY")).append("\">")
                                .append("<").append(res.getString("GENDER").replaceAll("\\s+", "")).append(">").append(res.getString("VALUE"))
                                .append("</").append(res.getString("GENDER").replaceAll("\\s+", "")).append(">");
                        counter++;
                    } else if (counter == 1) {
                        result.append("<").append(res.getString("GENDER")).append(">").append(res.getString("VALUE"))
                                .append("</").append(res.getString("GENDER")).append(">");
                        counter++;
                    } else if (counter == 2) {
                        result.append("<").append(res.getString("GENDER")).append(">")
                                .append(res.getString("VALUE")).append("</").append(res.getString("GENDER"))
                                .append(">").append("</department>");
                        counter = 0;
                    }
                }
                result.append("</department>").append("</").append(TABLE_GRAD).append(">");
                resp = getResponse(result.toString(), "application/xml");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return resp;
    }

    @Path("/grad/getRecordsperAcademicYear/{year}/{academic_year}")
    @GET
    public Response get2(@Context HttpHeaders http, @PathParam("year") String year, @PathParam("academic_year") String academic_year) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();
        Response resp = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT FACULTY, GENDER, VALUE FROM ").append(TABLE_ACAD).append(" WHERE STUDENT_YEAR = '")
                    .append(academic_year)
                    .append("' AND YEAR = '")
                    .append(year).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            int choice = chooseformat(http.getAcceptableMediaTypes().get(0).toString());
            if (choice == 1) {
                int counter = 0;
                result.append("[");
                while (res.next() == true) {
                    if (counter == 0) {
                        result.append("{")
                                .append("\"Department\":\"").append(res.getString("FACULTY")).append("\",")
                                .append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\",");
                        counter++;
                    } else if (counter == 1) {
                        result.append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\",");
                        counter++;
                    } else if (counter == 2) {
                        result.append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\"},");
                        counter = 0;
                    }
                }
                resp = getResponse(result.substring(0, result.length() - 1) + "}]", "application/json");
            } else if (choice == 2) {
                int counter = 0;
                result.append("Department;Male;Female;Total\n");
                while (res.next() == true) {
                    if (res.getString("FACULTY").equals("All")){
                        result.append(res.getString("FACULTY")).append(";-1;-1;").append(res.getString("VALUE")).append("\n");
                        counter = 0;
                    } else {
                        if (counter == 0) {
                            result.append(res.getString("FACULTY")).append(";").append(res.getString("VALUE")).append(";");
                            counter++;
                        } else if (counter == 1) {
                            result.append(res.getString("VALUE")).append(";");
                            counter++;
                        } else if (counter == 2) {
                            result.append(res.getString("VALUE")).append("\n");
                            counter = 0;
                        }
                    }
                }
                resp = getResponse(result.substring(0, result.length() - 1), "text/plain");
            } else if (choice == 3) {
                result.append(STYLE).append("<table><tr><th>DEPARTMENT</th><th>GENDER</th><th>VALUE</th></tr>");
                while (res.next() == true) {
                    result.append("<tr>")
                            .append("<td>").append(res.getString("FACULTY")).append("</td>")
                            .append("<td>").append(res.getString("GENDER")).append("</td>")
                            .append("<td>").append(res.getString("VALUE")).append("</td>").append("</tr>");
                }
                result.append("</table></body></html>");

                Document doc = Jsoup.parseBodyFragment(result.toString());
                resp = getResponse(doc.html(), "text/html");
            } else if (choice == 4) {
                result.append("<").append(TABLE_GRAD).append(" year=\"")
                        .append(year).append("\" grad_year=\"")
                        .append(academic_year).append("\">");
                int counter = 0;
                while (res.next() == true) {
                    if (counter == 0) {
                        result.append("<department name=\"").append(res.getString("FACULTY")).append("\">")
                                .append("<").append(res.getString("GENDER").replaceAll("\\s+", "")).append(">").append(res.getString("VALUE"))
                                .append("</").append(res.getString("GENDER").replaceAll("\\s+", "")).append(">");
                        counter++;
                    } else if (counter == 1) {
                        result.append("<").append(res.getString("GENDER")).append(">").append(res.getString("VALUE"))
                                .append("</").append(res.getString("GENDER")).append(">");
                        counter++;
                    } else if (counter == 2) {
                        result.append("<").append(res.getString("GENDER")).append(">")
                                .append(res.getString("VALUE")).append("</").append(res.getString("GENDER"))
                                .append(">").append("</department>");
                        counter = 0;
                    }
                }
                result.append("</department>").append("</").append(TABLE_GRAD).append(">");
                resp = getResponse(result.toString(), "application/xml");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return resp;
    }

    @Path("/grad/getRecordsperGraduationYearOnlyGrad/{grad_year}")
    @GET
    public Response get3(@Context HttpHeaders http, @PathParam("grad_year") String grad_year) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();
        Response resp = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT YEAR, FACULTY, GENDER, VALUE FROM ").append(TABLE_GRAD).append(" WHERE GRADUATION_YEAR = '")
                    .append(grad_year).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            int choice = chooseformat(http.getAcceptableMediaTypes().get(0).toString());
            if (choice == 1) {
                int counter = 0;
                result.append("[");
                while (res.next() == true) {
                    if (counter == 0) {
                        result.append("{").append("\"Year\": \"").append(res.getString("YEAR")).append("\",")
                                .append("\"Department\":\"").append(res.getString("FACULTY")).append("\",")
                                .append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\",");
                        counter++;
                    } else if (counter == 1) {
                        result.append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\",");
                        counter++;
                    } else if (counter == 2) {
                        result.append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\"},");
                        counter = 0;
                    }
                }
                resp = getResponse(result.substring(0, result.length() - 1) + "}]", "application/json");
            } else if (choice == 2) {
                int counter = 0;
                result.append("Department;Year;Male;Female;Total\n");
                while (res.next() == true) {
                    if (res.getString("FACULTY").equals("All")){
                        result.append(res.getString("FACULTY")).append(";").append(res.getString("YEAR")).append(";-1;-1;").append(res.getString("VALUE")).append("\n");
                        counter = 0;
                    } else {
                        if (counter == 0) {
                            result.append(res.getString("FACULTY")).append(";").append(res.getString("YEAR")).append(";").append(res.getString("VALUE")).append(";");
                            counter++;
                        } else if (counter == 1) {
                            result.append(res.getString("VALUE")).append(";");
                            counter++;
                        } else if (counter == 2) {
                            result.append(res.getString("VALUE")).append("\n");
                            counter = 0;
                        }
                    }
                }
                resp = getResponse(result.substring(0, result.length() - 1), "text/plain");
            } else if (choice == 3) {
                result.append(STYLE).append("<table><tr><th>YEAR</th><th>DEPARTMENT</th><th>GENDER</th><th>VALUE</th></tr>");
                while (res.next() == true) {
                    result.append("<tr>")
                            .append("<td>").append(res.getString("YEAR")).append("</td>")
                            .append("<td>").append(res.getString("FACULTY")).append("</td>")
                            .append("<td>").append(res.getString("GENDER")).append("</td>")
                            .append("<td>").append(res.getString("VALUE")).append("</td>").append("</tr>");
                }
                result.append("</table></body></html>");
                Document doc = Jsoup.parseBodyFragment(result.toString());
                resp = getResponse(doc.html(), "text/html");
            } else if (choice == 4) {
                result.append("<").append(TABLE_GRAD).append(" grad_year=\"")
                        .append(grad_year).append("\">");
                int counter = 0;
                while (res.next() == true) {
                    if (counter == 0) {
                        result.append("<department name=\"").append(res.getString("FACULTY")).append("\" year=\"")
                                .append(res.getString("YEAR")).append("\">")
                                .append("<").append(res.getString("GENDER").replaceAll("\\s+", "")).append(">")
                                .append(res.getString("VALUE"))
                                .append("</").append(res.getString("GENDER").replaceAll("\\s+", "")).append(">");
                        counter++;
                    } else if (counter == 1) {
                        result.append("<").append(res.getString("GENDER")).append(">").append(res.getString("VALUE"))
                                .append("</").append(res.getString("GENDER")).append(">");
                        counter++;
                    } else if (counter == 2) {
                        result.append("<").append(res.getString("GENDER")).append(">")
                                .append(res.getString("VALUE")).append("</").append(res.getString("GENDER"))
                                .append(">").append("</department>");
                        counter = 0;
                    }
                }
                result.append("</department>").append("</").append(TABLE_GRAD).append(">");
                resp = getResponse(result.toString(), "application/xml");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return resp;
    }

    @Path("/grad/getRecordsperAcademicYearOnlyAcad/{academic_year}")
    @GET
    public Response get4(@Context HttpHeaders http, @PathParam("academic_year") String academic_year) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();
        Response resp = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT YEAR, FACULTY, GENDER, VALUE FROM ").append(TABLE_ACAD).append(" WHERE STUDENT_YEAR = '")
                    .append(academic_year).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            int choice = chooseformat(http.getAcceptableMediaTypes().get(0).toString());
            if (choice == 1) {
                int counter = 0;
                result.append("[");
                while (res.next() == true) {
                    if (counter == 0) {
                        result.append("{").append("\"Year\": \"").append(res.getString("YEAR")).append("\",")
                                .append("\"Department\":\"").append(res.getString("FACULTY")).append("\",")
                                .append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\",");
                        counter++;
                    } else if (counter == 1) {
                        result.append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\",");
                        counter++;
                    } else if (counter == 2) {
                        result.append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\"},");
                        counter = 0;
                    }
                }
                resp = getResponse(result.substring(0, result.length() - 1) + "}]", "application/json");
            } else if (choice == 2) {
                int counter = 0;
                result.append("Department;Year;Male;Female;Total\n");
                while (res.next() == true) {
                    if (res.getString("FACULTY").equals("All")){
                        result.append(res.getString("FACULTY")).append(";").append(res.getString("YEAR")).append(";-1;-1;").append(res.getString("VALUE")).append("\n");
                        counter = 0;
                    } else {
                        if (counter == 0) {
                            result.append(res.getString("FACULTY")).append(";").append(res.getString("YEAR")).append(";").append(res.getString("VALUE")).append(";");
                            counter++;
                        } else if (counter == 1) {
                            result.append(res.getString("VALUE")).append(";");
                            counter++;
                        } else if (counter == 2) {
                            result.append(res.getString("VALUE")).append("\n");
                            counter = 0;
                        }
                    }
                }
                resp = getResponse(result.substring(0, result.length() - 1), "text/plain");
            } else if (choice == 3) {
                result.append(STYLE).append("<table><tr><th>YEAR</th><th>DEPARTMENT</th><th>GENDER</th><th>VALUE</th></tr>");
                while (res.next() == true) {
                    result.append("<tr>")
                            .append("<td>").append(res.getString("YEAR")).append("</td>")
                            .append("<td>").append(res.getString("FACULTY")).append("</td>")
                            .append("<td>").append(res.getString("GENDER")).append("</td>")
                            .append("<td>").append(res.getString("VALUE")).append("</td>").append("</tr>");
                }
                result.append("</table></body></html>");
                Document doc = Jsoup.parseBodyFragment(result.toString());
                resp = getResponse(doc.html(), "text/html");
            } else if (choice == 4) {
                result.append("<").append(TABLE_GRAD).append(" acad_year=\"")
                        .append(academic_year).append("\">");
                int counter = 0;
                while (res.next() == true) {
                    if (counter == 0) {
                        result.append("<department name=\"").append(res.getString("FACULTY")).append("\" year=\"")
                                .append(res.getString("YEAR")).append("\">")
                                .append("<").append(res.getString("GENDER").replaceAll("\\s+", "")).append(">")
                                .append(res.getString("VALUE"))
                                .append("</").append(res.getString("GENDER").replaceAll("\\s+", "")).append(">");
                        counter++;
                    } else if (counter == 1) {
                        result.append("<").append(res.getString("GENDER")).append(">").append(res.getString("VALUE"))
                                .append("</").append(res.getString("GENDER")).append(">");
                        counter++;
                    } else if (counter == 2) {
                        result.append("<").append(res.getString("GENDER")).append(">")
                                .append(res.getString("VALUE")).append("</").append(res.getString("GENDER"))
                                .append(">").append("</department>");
                        counter = 0;
                    }
                }
                result.append("</department>").append("</").append(TABLE_GRAD).append(">");
                resp = getResponse(result.toString(), "application/xml");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return resp;
    }

    @Path("/grad/getRecordValueperGraduationYear/{year}/{grad_year}/{department}/{gender}")
    @GET
    public Response get5(@Context HttpHeaders http, @PathParam("year") String year, @PathParam("grad_year") String grad_year,
            @PathParam("department") String department, @PathParam("gender") String gender) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();
        Response resp = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT VALUE FROM ").append(TABLE_GRAD).append("")
                    .append(" WHERE YEAR = '").append(year).append("' and ")
                    .append(" GRADUATION_YEAR = '").append(grad_year)
                    .append("' and FACULTY = '").append(department)
                    .append("' and GENDER = '").append(gender).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            int choice = chooseformat(http.getAcceptableMediaTypes().get(0).toString());
            if (choice == 1) {
                result.append("{");
                while (res.next() == true) {
                    result.append("\"Year\":\"").append(year).append("\",")
                            .append("\"Department\":\"").append(department).append("\",")
                            .append("\"").append(gender).append("\":\"").append(res.getString("VALUE")).append("\",")
                            .append("\"").append("Graduation Year").append("\":\"").append(grad_year).append("\",");
                }
                resp = getResponse(result.substring(0, result.length() - 1) + "}", "application/json");
            } else if (choice == 2) {
                while (res.next() == true) {
                    result.append("Department;Year;").append(gender).append(";GraduationYear\n").append(department).append(";").append(year).append(";")
                            .append(res.getString("VALUE")).append(";").append(grad_year);
                }
                resp = getResponse(result.toString(), "text/plain");
            } else if (choice == 3) {
                result.append(STYLE).append("<table><tr><th>YEAR</th><th>GRADUATION YEAR").append("</th><th>DEPARTMENT</th>")
                        .append("<th>GENDER</th><th>VALUE</th></tr>");
                while (res.next() == true) {
                    result.append("<tr>")
                            .append("<td>").append(year).append("</td>")
                            .append("<td>").append(grad_year).append("</td>")
                            .append("<td>").append(department).append("</td>")
                            .append("<td>").append(gender).append("</td>")
                            .append("<td>").append(res.getString("VALUE")).append("</td>")
                            .append("</tr></table></body></html></table></body></html>");
                }
                Document doc = Jsoup.parseBodyFragment(result.toString());
                resp = getResponse(doc.html(), "text/html");
            } else if (choice == 4) {
                result.append("<department name=\"").append(department).append("\" year=\"").append(year).append("\" grad_year=\"")
                        .append(grad_year).append("\">");
                while (res.next() == true) {
                    result.append("<").append(gender).append(">").append(res.getString("VALUE")).append("</").append(gender).append(">");
                }
                result.append("</department>");
                resp = getResponse(result.toString(), "application/xml");
            }

        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return resp;
    }

    @Path("/grad/getRecordValueperAcademicYear/{year}/{academic_year}/{department}/{gender}")
    @GET
    public Response get6(@Context HttpHeaders http, @PathParam("year") String year, @PathParam("academic_year") String academic_year,
            @PathParam("department") String department, @PathParam("gender") String gender) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();
        Response resp = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT VALUE FROM ").append(TABLE_ACAD).append("")
                    .append(" WHERE YEAR = '").append(year).append("' and ")
                    .append(" STUDENT_YEAR = '").append(academic_year)
                    .append("' and FACULTY = '").append(department)
                    .append("' and GENDER = '").append(gender).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            int choice = chooseformat(http.getAcceptableMediaTypes().get(0).toString());
            if (choice == 1) {
                result.append("{");
                while (res.next() == true) {
                    result.append("\"Year\":\"").append(year).append("\",")
                            .append("\"Department\":\"").append(department).append("\",")
                            .append("\"").append(gender).append("\":\"").append(res.getString("VALUE")).append("\",")
                            .append("\"").append("Academic Year").append("\":\"").append(academic_year).append("\",");
                }
                resp = getResponse(result.substring(0, result.length() - 1) + "}", "application/json");
            } else if (choice == 2) {
                while (res.next() == true) {
                    result.append("Department;Year;").append(gender).append(";AcademicYear\n").append(department).append(";").append(year).append(";")
                            .append(res.getString("VALUE")).append(";").append(academic_year);
                }
                resp = getResponse(result.toString(), "text/plain");
            } else if (choice == 3) {
                result.append(STYLE).append("<table><tr><th>YEAR</th><th>ACADEMIC YEAR").append("</th><th>DEPARTMENT</th>")
                        .append("<th>GENDER</th><th>VALUE</th></tr>");
                while (res.next() == true) {
                    result.append("<tr>")
                            .append("<td>").append(year).append("</td>")
                            .append("<td>").append(academic_year).append("</td>")
                            .append("<td>").append(department).append("</td>")
                            .append("<td>").append(gender).append("</td>")
                            .append("<td>").append(res.getString("VALUE")).append("</td>")
                            .append("</tr></table></body></html></table></body></html>");
                }
                Document doc = Jsoup.parseBodyFragment(result.toString());
                resp = getResponse(doc.html(), "text/html");
            } else if (choice == 4) {
                result.append("<department name=\"").append(department).append("\" year=\"").append(year).append("\" acad_year=\"")
                        .append(academic_year).append("\">");
                while (res.next() == true) {
                    result.append("<").append(gender).append(">").append(res.getString("VALUE")).append("</").append(gender).append(">");
                }
                result.append("</department>");
                resp = getResponse(result.toString(), "application/xml");
            }

        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return resp;
    }

    @Path("/grad/getRecordsperGraduationYearperDepartment/{department}/{grad_year}")
    @GET
    public Response get7(@Context HttpHeaders http, @PathParam("department") String department, @PathParam("grad_year") String grad_year) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();
        Response resp = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT YEAR, GENDER, VALUE FROM ").append(TABLE_GRAD).append(" WHERE GRADUATION_YEAR = '")
                    .append(grad_year)
                    .append("' AND FACULTY = '")
                    .append(department).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            int choice = chooseformat(http.getAcceptableMediaTypes().get(0).toString());
            if (choice == 1) {
                int counter = 0;
                result.append("[");
                while (res.next() == true) {
                    if (counter == 0) {
                        result.append("{")
                                .append("\"Year\":\"").append(res.getString("YEAR")).append("\",")
                                .append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\",");
                        counter++;
                    } else if (counter == 1) {
                        result.append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\",");
                        counter++;
                    } else if (counter == 2) {
                        result.append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\"},");
                        counter = 0;
                    }
                }
                resp = getResponse(result.substring(0, result.length() - 1) + "]", "application/json");
            } else if (choice == 2) {
                int counter = 0;
                result.append("Male;Female;Total;Year\n");
                while (res.next() == true) {
                    if (counter == 0) {
                            result.append(res.getString("VALUE")).append(";");
                            counter++;
                        } else if (counter == 1) {
                            result.append(res.getString("VALUE")).append(";");
                            counter++;
                        } else if (counter == 2) {
                            result.append(res.getString("VALUE")).append(";").append(res.getString("YEAR")).append("\n");
                            counter = 0;
                        }
                    }
                resp = getResponse(result.substring(0, result.length() - 1), "text/plain");
            } else if (choice == 3) {
                result.append(STYLE).append("<table><tr><th>YEAR</th><th>GENDER</th><th>VALUE</th></tr>");
                while (res.next() == true) {
                    result.append("<tr>")
                            .append("<td>").append(res.getString("YEAR")).append("</td>")
                            .append("<td>").append(res.getString("GENDER")).append("</td>")
                            .append("<td>").append(res.getString("VALUE")).append("</td>").append("</tr>");
                }
                result.append("</table></body></html>");

                Document doc = Jsoup.parseBodyFragment(result.toString());
                resp = getResponse(doc.html(), "text/html");
            } else if (choice == 4) {

                result.append("<").append(TABLE_GRAD).append(" department=\"")
                        .append(department).append("\" grad_year=\"")
                        .append(grad_year).append("\">");
                int counter = 0;
                while (res.next() == true) {
                    if (counter == 0) {
                        result.append("<year name=\"").append(res.getString("YEAR")).append("\">")
                                .append("<").append(res.getString("GENDER").replaceAll("\\s+", "")).append(">").append(res.getString("VALUE"))
                                .append("</").append(res.getString("GENDER").replaceAll("\\s+", "")).append(">");
                        counter++;
                    } else if (counter == 1) {
                        result.append("<").append(res.getString("GENDER")).append(">").append(res.getString("VALUE"))
                                .append("</").append(res.getString("GENDER")).append(">");
                        counter++;
                    } else if (counter == 2) {
                        result.append("<").append(res.getString("GENDER")).append(">")
                                .append(res.getString("VALUE")).append("</").append(res.getString("GENDER"))
                                .append(">").append("</year>");
                        counter = 0;
                    }
                }
                result.append("</").append(TABLE_GRAD).append(">");
                resp = getResponse(result.toString(), "application/xml");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return resp;
    }

    @Path("/grad/getRecordsperAcademicYearperDepartment/{department}/{academic_year}")
    @GET
    public Response get8(@Context HttpHeaders http, @PathParam("department") String department, @PathParam("academic_year") String academic_year) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();
        Response resp = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT YEAR, GENDER, VALUE FROM ").append(TABLE_ACAD).append(" WHERE STUDENT_YEAR = '")
                    .append(academic_year)
                    .append("' AND FACULTY = '")
                    .append(department).append("';");
            stmt.execute(insQuery.toString());
            System.out.println(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            int choice = chooseformat(http.getAcceptableMediaTypes().get(0).toString());
            if (choice == 1) {
                int counter = 0;
                result.append("[");
                while (res.next() == true) {
                    if (counter == 0) {
                        result.append("{")
                                .append("\"Year\":\"").append(res.getString("YEAR")).append("\",")
                                .append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\",");
                        counter++;
                    } else if (counter == 1) {
                        result.append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\",");
                        counter++;
                    } else if (counter == 2) {
                        result.append("\"").append(res.getString("GENDER")).append("\": \"")
                                .append(res.getString("VALUE")).append("\"},");
                        counter = 0;
                    }
                }
                resp = getResponse(result.substring(0, result.length() - 1) + "]", "application/json");
            } else if (choice == 2) {
                int counter = 0;
                result.append("Male;Female;Total;Year\n");
                while (res.next() == true) {
                    if (counter == 0) {
                            result.append(res.getString("VALUE")).append(";");
                            counter++;
                        } else if (counter == 1) {
                            result.append(res.getString("VALUE")).append(";");
                            counter++;
                        } else if (counter == 2) {
                            result.append(res.getString("VALUE")).append(";").append(res.getString("YEAR")).append("\n");
                            counter = 0;
                        }
                    }
                resp = getResponse(result.substring(0, result.length() - 1), "text/plain");
            } else if (choice == 3) {
                result.append(STYLE).append("<table><tr><th>YEAR</th><th>GENDER</th><th>VALUE</th></tr>");
                while (res.next() == true) {
                    result.append("<tr>")
                            .append("<td>").append(res.getString("YEAR")).append("</td>")
                            .append("<td>").append(res.getString("GENDER")).append("</td>")
                            .append("<td>").append(res.getString("VALUE")).append("</td>").append("</tr>");
                }
                result.append("</table></body></html>");

                Document doc = Jsoup.parseBodyFragment(result.toString());
                resp = getResponse(doc.html(), "text/html");
            } else if (choice == 4) {

                result.append("<").append(TABLE_ACAD).append(" department=\"")
                        .append(department).append("\" grad_year=\"")
                        .append(academic_year).append("\">");
                int counter = 0;
                while (res.next() == true) {
                    if (counter == 0) {
                        result.append("<year name=\"").append(res.getString("YEAR")).append("\">")
                                .append("<").append(res.getString("GENDER").replaceAll("\\s+", "")).append(">").append(res.getString("VALUE"))
                                .append("</").append(res.getString("GENDER").replaceAll("\\s+", "")).append(">");
                        counter++;
                    } else if (counter == 1) {
                        result.append("<").append(res.getString("GENDER")).append(">").append(res.getString("VALUE"))
                                .append("</").append(res.getString("GENDER")).append(">");
                        counter++;
                    } else if (counter == 2) {
                        result.append("<").append(res.getString("GENDER")).append(">")
                                .append(res.getString("VALUE")).append("</").append(res.getString("GENDER"))
                                .append(">").append("</year>");
                        counter = 0;
                    }
                }
                result.append("</").append(TABLE_ACAD).append(">");
                resp = getResponse(result.toString(), "application/xml");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return resp;
    }

    @Path("/grad/getGrandTotalofAcademicYears/{year}")
    @GET
    public Response get9(@PathParam("year") String year) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();
        Response resp = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT STUDENT_YEAR, VALUE FROM ").append(TABLE_ACAD).append(" WHERE GENDER = 'Grand Total' AND YEAR = '")
                    .append(year).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            int choice = chooseformat(http.getAcceptableMediaTypes().get(0).toString());
            if (choice == 1) {
                result.append("{");
                while (res.next() == true) {
                    result.append("\"").append(res.getString("STUDENT_YEAR")).append("\":\"").append(res.getString("VALUE")).append("\",");
                }
                resp = getResponse(result.substring(0, result.length() - 1) + "}", "application/json");
            } else if (choice == 2) {
                result.append("AcademicYear;Value\n");
                while (res.next() == true) {
                    result.append(res.getString("STUDENT_YEAR")).append(";").append(res.getString("VALUE")).append("\n");
                }
                resp = getResponse(result.substring(0, result.length() - 1), "text/plain");
            } else if (choice == 3) {
                result.append(STYLE).append("<table><tr><th>STUDENT YEAR</th><th>VALUE</th></tr>");
                while (res.next() == true) {
                    result.append("<tr>").append("<td>").append(res.getString("STUDENT_YEAR")).append("</td>")
                            .append("<td>").append(res.getString("VALUE")).append("</td>").append("</tr>");
                }
                result.append("</table></body></html>");
                Document doc = Jsoup.parseBodyFragment(result.toString());
                resp = getResponse(doc.html(), "text/html");
            } else if (choice == 4) {
                result.append("<").append(TABLE_ACAD).append(" year=\"").append(year).append("\">");
                while (res.next() == true) {
                    result.append("<AcademicYear name=\"").append(res.getString("STUDENT_YEAR")).append("\">").append(res.getString("VALUE"))
                            .append("</AcademicYear>");
                }
                result.append("</").append(TABLE_ACAD).append(">");
                resp = getResponse(result.toString(), "application/xml");
            } else if (choice == 5) {
                LinkedList<Number> temp = new LinkedList();
                LinkedList<String> column = new LinkedList();
                while (res.next() == true) {
                    temp.add(Integer.valueOf(res.getString("VALUE")));
                    column.add(res.getString("STUDENT_YEAR"));
                }
                Charts.createChart("Total number of people in " + year, year, "Value", temp, column);
                return getResponse(result.toString(), "image/png");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return resp;
    }

    @Path("/grad/getGrandTotalofGraduationYears/{year}")
    @GET
    public Response get10(@PathParam("year") String year) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();
        Response resp = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT GRADUATION_YEAR, VALUE FROM ").append(TABLE_GRAD).append(" WHERE GENDER = 'Grand Total' AND YEAR = '")
                    .append(year).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            int choice = chooseformat(http.getAcceptableMediaTypes().get(0).toString());
            if (choice == 1) {
                result.append("{");
                while (res.next() == true) {
                    result.append("\"").append(res.getString("GRADUATION_YEAR")).append("\":\"").append(res.getString("VALUE")).append("\",");
                }
                System.out.println(result.substring(0, result.length() - 1) + "}");
                resp = getResponse(result.substring(0, result.length() - 1) + "}", "application/json");
            } else if (choice == 2) {
                result.append("GraduationYear;Value\n");
                while (res.next() == true) {
                    result.append(res.getString("GRADUATION_YEAR")).append(";").append(res.getString("VALUE")).append("\n");
                }
                resp = getResponse(result.substring(0, result.length() - 1), "text/plain");
            } else if (choice == 3) {
                result.append(STYLE).append("<table><tr><th>GRADUATION YEAR</th><th>VALUE</th></tr>");
                while (res.next() == true) {
                    result.append("<tr>").append("<td>").append(res.getString("GRADUATION_YEAR")).append("</td>")
                            .append("<td>").append(res.getString("VALUE")).append("</td>").append("</tr>");
                }
                result.append("</table></body></html>");
                Document doc = Jsoup.parseBodyFragment(result.toString());
                resp = getResponse(doc.html(), "text/html");
            } else if (choice == 4) {
                result.append("<").append(TABLE_GRAD).append(" year=\"").append(year).append("\">");
                while (res.next() == true) {
                    result.append("<GraduationYear name=\"").append(res.getString("GRADUATION_YEAR")).append("\">").append(res.getString("VALUE"))
                            .append("</GraduationYear>");
                }
                result.append("</").append(TABLE_GRAD).append(">");
                resp = getResponse(result.toString(), "application/xml");
            } else if (choice == 5) {
                LinkedList<Number> temp = new LinkedList();
                LinkedList<String> column = new LinkedList();
                while (res.next() == true) {
                    temp.add(Integer.valueOf(res.getString("VALUE")));
                    column.add(res.getString("GRADUATION_YEAR"));
                }
                Charts.createChart("Total number of people in " + year, year, "Value", temp, column);
                resp = getResponse(result.toString(), "image/png");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return resp;
    }

    @Path("/grad/getGrandTotalofAcademicYearsPlusDep/{department}/{year}")
    @GET
    public Response get11(@Context HttpHeaders http, @PathParam("department") String department, @PathParam("year") String year) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();
        Response resp = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT STUDENT_YEAR, VALUE FROM ").append(TABLE_ACAD).append(" WHERE GENDER = 'Total' AND YEAR = '")
                    .append(year).append("' AND FACULTY = '").append(department).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            int choice = chooseformat(http.getAcceptableMediaTypes().get(0).toString());
            if (choice == 1) {
                result.append("{");
                while (res.next() == true) {
                    result.append("\"").append(res.getString("STUDENT_YEAR")).append("\":\"").append(res.getString("VALUE")).append("\",");
                }
                resp = getResponse(result.substring(0, result.length() - 1) + "}", "application/json");
            } else if (choice == 2) {
                result.append("AcademicYear;Value\n");
                while (res.next() == true) {
                    result.append(res.getString("STUDENT_YEAR")).append(";").append(res.getString("VALUE")).append("\n");
                }
                resp = getResponse(result.substring(0, result.length() - 1), "text/plain");
            } else if (choice == 3) {
                result.append(STYLE).append("<table><tr><th>STUDENT YEAR</th><th>VALUE</th></tr>");
                while (res.next() == true) {
                    result.append("<tr>").append("<td>").append(res.getString("STUDENT_YEAR")).append("</td>")
                            .append("<td>").append(res.getString("VALUE")).append("</td>").append("</tr>");
                }
                result.append("</table></body></html>");
                Document doc = Jsoup.parseBodyFragment(result.toString());
                resp = getResponse(doc.html(), "text/html");
            } else if (choice == 4) {
                result.append("<").append(TABLE_ACAD).append(" year=\"").append(year).append("\" department=\"").append(department).append("\">");
                while (res.next() == true) {
                    result.append("<AcademicYear name=\"").append(res.getString("STUDENT_YEAR")).append("\">").append(res.getString("VALUE"))
                            .append("</AcademicYear>");
                }
                result.append("</").append(TABLE_ACAD).append(">");
                resp = getResponse(result.toString(), "application/xml");
            } else if (choice == 5) {
                LinkedList<Number> temp = new LinkedList();
                LinkedList<String> column = new LinkedList();
                while (res.next() == true) {
                    temp.add(Integer.valueOf(res.getString("VALUE")));
                    column.add(res.getString("STUDENT_YEAR"));
                }
                Charts.createChart("Total number of people in " + year + " of " + department, year, "Value", temp, column);
                resp = getResponse(result.toString(), "image/png");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return resp;
    }

    @Path("/grad/getGrandTotalofGraduationYearsPlusDep/{department}/{year}")
    @GET
    public Response get12(@Context HttpHeaders http, @PathParam("department") String department, @PathParam("year") String year) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();
        Response resp = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT GRADUATION_YEAR, VALUE FROM ").append(TABLE_GRAD).append(" WHERE GENDER = 'Total' AND YEAR = '")
                    .append(year).append("' AND FACULTY = '").append(department).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            int choice = chooseformat(http.getAcceptableMediaTypes().get(0).toString());
            if (choice == 1) {
                result.append("{");
                while (res.next() == true) {
                    result.append("\"").append(res.getString("GRADUATION_YEAR")).append("\":\"").append(res.getString("VALUE")).append("\",");
                }
                resp = getResponse(result.substring(0, result.length() - 1) + "}", "application/json");
            } else if (choice == 2) {
                result.append("GraduationYear;Value\n");
                while (res.next() == true) {
                    result.append(res.getString("GRADUATION_YEAR")).append(";").append(res.getString("VALUE")).append("\n");
                }
                resp = getResponse(result.substring(0, result.length() - 1), "text/plain");
            } else if (choice == 3) {
                result.append(STYLE).append("<table><tr><th>GRADUATION YEAR</th><th>VALUE</th></tr>");
                while (res.next() == true) {
                    result.append("<tr>").append("<td>").append(res.getString("GRADUATION_YEAR")).append("</td>")
                            .append("<td>").append(res.getString("VALUE")).append("</td>").append("</tr>");
                }
                result.append("</table></body></html>");
                Document doc = Jsoup.parseBodyFragment(result.toString());
                resp = getResponse(doc.html(), "text/html");
            } else if (choice == 4) {
                result.append("<").append(TABLE_GRAD).append(" year=\"").append(year).append("\" department=\"").append(department).append("\">");
                while (res.next() == true) {
                    result.append("<GraduationYear name=\"").append(res.getString("GRADUATION_YEAR")).append("\">").append(res.getString("VALUE"))
                            .append("</GraduationYear>");
                }
                result.append("</").append(TABLE_GRAD).append(">");
                resp = getResponse(result.toString(), "application/xml");
            } else if (choice == 5) {
                LinkedList<Number> temp = new LinkedList();
                LinkedList<String> column = new LinkedList();
                while (res.next() == true) {
                    temp.add(Integer.valueOf(res.getString("VALUE")));
                    column.add(res.getString("GRADUATION_YEAR"));
                }
                Charts.createChart("Total number of people in " + year + " of " + department, year, "Value", temp, column);
                resp = getResponse(result.toString(), "image/png");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return resp;
    }

    @Path("/grad/getMaleGrandTotalofAcademicYears/{department}/{year}")
    @GET
    public Response get13(@Context HttpHeaders http, @PathParam("department") String department, @PathParam("year") String year) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();
        Response resp = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT STUDENT_YEAR, VALUE FROM ").append(TABLE_ACAD).append(" WHERE GENDER = 'Male' AND YEAR = '")
                    .append(year).append("' AND FACULTY = '").append(department).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            int choice = chooseformat(http.getAcceptableMediaTypes().get(0).toString());
            if (choice == 1) {
                result.append("{");
                while (res.next() == true) {
                    result.append("\"").append(res.getString("STUDENT_YEAR")).append("\":\"").append(res.getString("VALUE")).append("\",");
                }
                resp = getResponse(result.substring(0, result.length() - 1) + "}", "application/json");
            } else if (choice == 2) {
                result.append("AcademicYear;Value\n");
                while (res.next() == true) {
                    result.append(res.getString("STUDENT_YEAR")).append(";").append(res.getString("VALUE")).append("\n");
                }
                resp = getResponse(result.substring(0, result.length() - 1), "text/plain");
            } else if (choice == 3) {
                result.append(STYLE).append("<table><tr><th>STUDENT YEAR</th><th>VALUE</th></tr>");
                while (res.next() == true) {
                    result.append("<tr>").append("<td>").append(res.getString("STUDENT_YEAR")).append("</td>")
                            .append("<td>").append(res.getString("VALUE")).append("</td>").append("</tr>");
                }
                result.append("</table></body></html>");
                Document doc = Jsoup.parseBodyFragment(result.toString());
                resp = getResponse(doc.html(), "text/html");
            } else if (choice == 4) {
                result.append("<").append(TABLE_ACAD).append(" year=\"").append(year).append("\" gender=\"Male").append("\" department=\"").append(department).append("\">");
                while (res.next() == true) {
                    result.append("<AcademicYear name=\"").append(res.getString("STUDENT_YEAR")).append("\">").append(res.getString("VALUE"))
                            .append("</AcademicYear>");
                }
                result.append("</").append(TABLE_ACAD).append(">");
                resp = getResponse(result.toString(), "application/xml");
            } else if (choice == 5) {
                LinkedList<Number> temp = new LinkedList();
                LinkedList<String> column = new LinkedList();
                while (res.next() == true) {
                    temp.add(Integer.valueOf(res.getString("VALUE")));
                    column.add(res.getString("STUDENT_YEAR"));
                }
                Charts.createChart("Total number of men in " + year + " of " + department, year, "Value", temp, column);
                resp = getResponse(result.toString(), "image/png");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return resp;
    }

    @Path("/grad/getMaleGrandTotalofGraduationYears/{department}/{year}")
    @GET
    public Response get14(@Context HttpHeaders http, @PathParam("department") String department, @PathParam("year") String year) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();
        Response resp = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT GRADUATION_YEAR, VALUE FROM ").append(TABLE_GRAD).append(" WHERE GENDER = 'Male' AND YEAR = '")
                    .append(year).append("' AND FACULTY = '").append(department).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            int choice = chooseformat(http.getAcceptableMediaTypes().get(0).toString());
            if (choice == 1) {
                result.append("{");
                while (res.next() == true) {
                    result.append("\"").append(res.getString("GRADUATION_YEAR")).append("\":\"").append(res.getString("VALUE")).append("\",");
                }
                resp = getResponse(result.substring(0, result.length() - 1) + "}", "application/json");
            } else if (choice == 2) {
                result.append("GraduationYear;Value\n");
                while (res.next() == true) {
                    result.append(res.getString("GRADUATION_YEAR")).append(";").append(res.getString("VALUE")).append("\n");
                }
                resp = getResponse(result.substring(0, result.length() - 1), "text/plain");
            } else if (choice == 3) {
                result.append(STYLE).append("<table><tr><th>GRADUATION YEAR</th><th>VALUE</th></tr>");
                while (res.next() == true) {
                    result.append("<tr>").append("<td>").append(res.getString("GRADUATION_YEAR")).append("</td>")
                            .append("<td>").append(res.getString("VALUE")).append("</td>").append("</tr>");
                }
                result.append("</table></body></html>");
                Document doc = Jsoup.parseBodyFragment(result.toString());
                resp = getResponse(doc.html(), "text/html");
            } else if (choice == 4) {
                result.append("<").append(TABLE_GRAD).append(" year=\"").append(year).append("\" gender=\"Male").append("\" department=\"").append(department).append("\">");
                while (res.next() == true) {
                    result.append("<GraduationYear name=\"").append(res.getString("GRADUATION_YEAR")).append("\">").append(res.getString("VALUE"))
                            .append("</GraduationYear>");
                }
                result.append("</").append(TABLE_GRAD).append(">");
                resp = getResponse(result.toString(), "application/xml");
            } else if (choice == 5) {
                LinkedList<Number> temp = new LinkedList();
                LinkedList<String> column = new LinkedList();
                while (res.next() == true) {
                    temp.add(Integer.valueOf(res.getString("VALUE")));
                    column.add(res.getString("GRADUATION_YEAR"));
                }
                Charts.createChart("Total number of male in " + year + " of " + department, year, "Value", temp, column);
                resp = getResponse(result.toString(), "image/png");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return resp;
    }

    @Path("/grad/getFemaleGrandTotalofAcademicYears/{department}/{year}")
    @GET
    public Response get15(@Context HttpHeaders http, @PathParam("department") String department, @PathParam("year") String year) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();
        Response resp = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT STUDENT_YEAR, VALUE FROM ").append(TABLE_ACAD).append(" WHERE GENDER = 'Female' AND YEAR = '")
                    .append(year).append("' AND FACULTY = '").append(department).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            int choice = chooseformat(http.getAcceptableMediaTypes().get(0).toString());
            if (choice == 1) {
                result.append("{");
                while (res.next() == true) {
                    result.append("\"").append(res.getString("STUDENT_YEAR")).append("\":\"").append(res.getString("VALUE")).append("\",");
                }
                resp = getResponse(result.substring(0, result.length() - 1) + "}", "application/json");
            } else if (choice == 2) {
                result.append("AcademicYear;Value\n");
                while (res.next() == true) {
                    result.append(res.getString("STUDENT_YEAR")).append(";").append(res.getString("VALUE")).append("\n");
                }
                resp = getResponse(result.substring(0, result.length() - 1), "text/plain");
            } else if (choice == 3) {
                result.append(STYLE).append("<table><tr><th>STUDENT YEAR</th><th>VALUE</th></tr>");
                while (res.next() == true) {
                    result.append("<tr>").append("<td>").append(res.getString("STUDENT_YEAR")).append("</td>")
                            .append("<td>").append(res.getString("VALUE")).append("</td>").append("</tr>");
                }
                result.append("</table></body></html>");
                Document doc = Jsoup.parseBodyFragment(result.toString());
                resp = getResponse(doc.html(), "text/html");
            } else if (choice == 4) {
                result.append("<").append(TABLE_ACAD).append(" year=\"").append(year).append("\" gender=\"Female").append("\" department=\"").append(department).append("\">");
                while (res.next() == true) {
                    result.append("<AcademicYear name=\"").append(res.getString("STUDENT_YEAR")).append("\">").append(res.getString("VALUE"))
                            .append("</AcademicYear>");
                }
                result.append("</").append(TABLE_ACAD).append(">");
                resp = getResponse(result.toString(), "application/xml");
            } else if (choice == 5) {
                LinkedList<Number> temp = new LinkedList();
                LinkedList<String> column = new LinkedList();
                while (res.next() == true) {
                    temp.add(Integer.valueOf(res.getString("VALUE")));
                    column.add(res.getString("STUDENT_YEAR"));
                }
                Charts.createChart("Total number of women in " + year + " of " + department, year, "Value", temp, column);
                resp = getResponse(result.toString(), "image/png");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return resp;
    }

    @Path("/grad/getFemaleGrandTotalofGraduationYears/{department}/{year}")
    @GET
    public Response get16(@Context HttpHeaders http, @PathParam("department") String department, @PathParam("year") String year) throws ClassNotFoundException, IOException {
        Statement stmt = null;
        Connection con = null;
        StringBuilder result = new StringBuilder();
        Response resp = null;
        try {
            con = DB.getConnection();
            stmt = con.createStatement();
            StringBuilder insQuery = new StringBuilder();
            insQuery.append("SELECT GRADUATION_YEAR, VALUE FROM ").append(TABLE_GRAD).append(" WHERE GENDER = 'Female' AND YEAR = '")
                    .append(year).append("' AND FACULTY = '").append(department).append("';");
            stmt.execute(insQuery.toString());
            ResultSet res = stmt.getResultSet();
            int choice = chooseformat(http.getAcceptableMediaTypes().get(0).toString());
            if (choice == 1) {
                result.append("{");
                while (res.next() == true) {
                    result.append("\"").append(res.getString("GRADUATION_YEAR")).append("\":\"").append(res.getString("VALUE")).append("\",");
                }
                resp = getResponse(result.substring(0, result.length() - 1) + "}", "application/json");
            } else if (choice == 2) {
                result.append("GraduationYear;Value\n");
                while (res.next() == true) {
                    result.append(res.getString("GRADUATION_YEAR")).append(";").append(res.getString("VALUE")).append("\n");
                }
                resp = getResponse(result.substring(0, result.length() - 1), "text/plain");
            } else if (choice == 3) {
                result.append(STYLE).append("<table><tr><th>GRADUATION YEAR</th><th>VALUE</th></tr>");
                while (res.next() == true) {
                    result.append("<tr>").append("<td>").append(res.getString("GRADUATION_YEAR")).append("</td>")
                            .append("<td>").append(res.getString("VALUE")).append("</td>").append("</tr>");
                }
                result.append("</table></body></html>");
                Document doc = Jsoup.parseBodyFragment(result.toString());
                resp = getResponse(doc.html(), "text/html");
            } else if (choice == 4) {
                result.append("<").append(TABLE_GRAD).append(" year=\"").append(year).append("\" gender=\"Female").append("\" department=\"").append(department).append("\">");
                while (res.next() == true) {
                    result.append("<GraduationYear name=\"").append(res.getString("GRADUATION_YEAR")).append("\">").append(res.getString("VALUE"))
                            .append("</GraduationYear>");
                }
                result.append("</").append(TABLE_GRAD).append(">");
                resp = getResponse(result.toString(), "application/xml");
            } else if (choice == 5) {
                LinkedList<Number> temp = new LinkedList();
                LinkedList<String> column = new LinkedList();
                while (res.next() == true) {
                    temp.add(Integer.valueOf(res.getString("VALUE")));
                    column.add(res.getString("GRADUATION_YEAR"));
                }
                Charts.createChart("Total number of female in " + year + " of " + department, year, "Value", temp, column);
                resp = getResponse(result.toString(), "image/png");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }
        return resp;
    }

    private int chooseformat(String choice) {
        if (choice.equals(MediaType.APPLICATION_JSON)) {
            return 1;
        } else if (choice.equals(MediaType.APPLICATION_XML)) {
            return 4;
        } else if (choice.equals(MediaType.TEXT_HTML)) {
            return 3;
        } else if (choice.equals(MediaType.TEXT_PLAIN)) {
            return 2;
        } else if (choice.equals("image/png")) {
            return 5;
        }
        return -1;
    }

    private Response getResponse(String result, String type) throws IOException {
        if (type.equals("image/png")) {
            BufferedImage img = null;
            File temp = new File("a.png");
            try {
                img = ImageIO.read(temp);
            } catch (IOException e) {
                System.out.println(e);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            temp.delete();
            return Response.status(Response.Status.OK)
                    .header("Content-type", type)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Expose-Headers", "*")
                    .header("Access-Control-Allow-Methods", "GET")
                    .header("Cache-Control", "max-age=0")
                    .entity(new ByteArrayInputStream(baos.toByteArray())).build();
        } else {
            return Response.status(Response.Status.OK)
                    .header("Content-type", type)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Expose-Headers", "*")
                    .header("Access-Control-Allow-Methods", "GET")
                    .header("Cache-Control", "max-age=0")
                    .entity(result).build();
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
}
