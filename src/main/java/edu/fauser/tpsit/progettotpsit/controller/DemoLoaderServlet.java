package edu.fauser.tpsit.progettotpsit.controller;

import edu.fauser.tpsit.progettotpsit.obj.DBConnection;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "DemoLoaderServlet", value = "/load")
public class DemoLoaderServlet extends HttpServlet
{
    private DBConnection con;
    @Override
    public void init()
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (request.getSession(false) == null || request.getSession(false).getAttribute("uid") == null)
            response.sendRedirect("errore.jsp");
        else
        {
            try
            {
                request.setAttribute("name", con.retrieveUserName((Long) request.getSession().getAttribute("uid")).get());
                request.getRequestDispatcher("map.jsp").forward(request, response);
            }
            catch (SQLException e)
            {
                log(e.getMessage(), e);
            }
        }
    }
}