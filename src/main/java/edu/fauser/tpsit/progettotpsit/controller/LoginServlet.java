package edu.fauser.tpsit.progettotpsit.controller;

import edu.fauser.tpsit.progettotpsit.helper.ServletHelper;
import edu.fauser.tpsit.progettotpsit.obj.DBConnection;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        ServletHelper.checkSession(request, response, "uid", this::manageNewSession, this::manageExistingSession);
    }
    private void manageExistingSession(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            response.sendRedirect("load");
        }
        catch (IOException e)
        {
            log(e.getMessage(), e);
        }
    }
    private void manageNewSession(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            Optional<Long> t;
            if (ServletHelper.checkParams(request, "email", "pass") && (t = con.retrieveUserID(request.getParameter("email"), request.getParameter("pass"))).isPresent())
            {
                request.getSession().setAttribute("uid", t.get());
                response.sendRedirect("load");
            }
            else
            {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.getSession().invalidate();
                request.setAttribute("error_mx", "Utente non trovato");
                request.getRequestDispatcher("error.jsp").forward(request, response);
            }
        }
        catch (SQLException | IOException | ServletException e)
        {
            log(e.getMessage(), e);
        }
    }
}