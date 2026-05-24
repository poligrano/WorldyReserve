package edu.fauser.tpsit.progettotpsit.controller;

import edu.fauser.tpsit.progettotpsit.helper.ServletHelper;
import edu.fauser.tpsit.progettotpsit.obj.DBConnection;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

@WebServlet(name = "ImageServlet", value = "/image")
public class ImageServlet extends HttpServlet
{
    private DBConnection con;
    @Override
    public void init() throws ServletException
    {
        try
        {
            con = new DBConnection();
        }
        catch (SQLException e)
        {
            log(e.getMessage(), e);
        }
    }
    @Override
    public void destroy()
    {
        try
        {
            con.close();
        }
        catch (SQLException e)
        {
            log(e.getMessage(), e);
        }
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        ServletHelper.checkSession(request, response, "uid", this::manageNoSession, this::sendImage);
    }
    private void sendImage(HttpServletRequest request, HttpServletResponse response)
    {
        try (InputStream is = getPfp(con.getUserPfp((Long) request.getSession().getAttribute("uid"))))
        {
            byte[] pfp = is.readAllBytes();
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("image/*");
            response.setContentLength(pfp.length);
            response.getOutputStream().write(pfp);
            response.flushBuffer();
        }
        catch (SQLException | IOException e)
        {
            log(e.getMessage(), e);
        }
    }
    private InputStream getPfp(Blob pfp) throws SQLException
    {
        return (pfp == null ? getClass().getClassLoader().getResourceAsStream("pfp/default.jpg") : pfp.getBinaryStream());
    }
    private void manageNoSession(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/plain");
            response.getOutputStream().println("Nessuna sessione trovata!");
        }
        catch (IOException e)
        {
            log(e.getMessage(), e);
        }
    }
}