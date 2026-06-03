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
    public void init() throws ServletException
    {
        try
        {
            con = new DBConnection();
        }
        catch (SQLException e)
        {
            log(e.getMessage(), e);
            throw new ServletException(e);
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletHelper.checkSession(request, response, "uid", this::manageNewSession, ServletHelper::defaultManageExistingSession);
    }
    private void manageNewSession(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        try
        {
            Optional<Long> t;
            if (ServletHelper.checkParams(request, "email", "pass") && (t = con.retrieveNormalUserID(request.getParameter("email"), request.getParameter("pass"))).isPresent())
            {
                request.getSession().setAttribute("uid", t.get());
                response.sendRedirect("load");
            }
            else
                ServletHelper.redirectErrorPage(request, response, HttpServletResponse.SC_FORBIDDEN, "Accesso non riuscito", "login", true);
        }
        catch (SQLException | IOException e)
        {
            log(e.getMessage(), e);
            throw new ServletException(e);
        }
    }
}