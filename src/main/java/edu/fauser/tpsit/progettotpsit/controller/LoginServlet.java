package edu.fauser.tpsit.progettotpsit.controller;

import edu.fauser.tpsit.progettotpsit.obj.DBConnection;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "LoginServlet", value = "/login")
public class LoginServlet extends HttpServlet
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (request.getSession().getAttribute("uid") != null)
            response.sendRedirect("load");
        else
        {
            try
            {
                con.retrieveUserID(request.getParameter("email"), request.getParameter("password")).ifPresentOrElse(
                        (id) -> {
                            request.getSession().setAttribute("uid", id);
                            try
                            {
                                response.sendRedirect("load");
                            }
                            catch (IOException e)
                            {
                                log(e.getMessage(), e);
                            }
                        },
                        () -> {
                            try
                            {
                                request.getSession().invalidate();
                                response.sendRedirect("error.jsp");
                            }
                            catch (IOException e)
                            {
                                log(e.getMessage(), e);
                            }
                        });
            }
            catch (SQLException e)
            {
                log(e.getMessage(), e);
            }
        }
    }
}