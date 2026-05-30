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
        ServletHelper.checkSession(request, response, "uid", (req, resp) -> ServletHelper.restRespond(resp, HttpServletResponse.SC_UNAUTHORIZED, "Nessuna sessione trovata!", getServletContext()), this::manageSendImage);
    }
    private void manageSendImage(HttpServletRequest request, HttpServletResponse response)
    {
        if (ServletHelper.checkParams(request, "id"))
            sendImage(request, response, Long.parseLong(request.getParameter("id")));
        else
            sendImage(request, response, (Long) request.getSession().getAttribute("uid"));
    }
    private void sendImage(HttpServletRequest request, HttpServletResponse response, long id)
    {
        try (InputStream is = getPfp(con.getUserPfp(id)))
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
            ServletHelper.restRespond(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore del Server!", getServletContext());
        }
    }
    private InputStream getPfp(Blob pfp) throws SQLException
    {
        return (pfp == null ? getClass().getClassLoader().getResourceAsStream("pfp/default.jpg") : pfp.getBinaryStream());
    }
}