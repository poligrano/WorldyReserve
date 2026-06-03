package edu.fauser.tpsit.progettotpsit.controller;

import edu.fauser.tpsit.progettotpsit.helper.ServletHelper;
import edu.fauser.tpsit.progettotpsit.obj.DBConnection;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "LoaderServlet", value = "/load")
public class LoaderServlet extends HttpServlet
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletHelper.checkSession(request, response, "uid", this::manageNoSession, this::load);
    }
    private void load(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        try
        {
            request.setAttribute("name", con.retrieveUserName((Long) request.getSession().getAttribute("uid")));
            request.setAttribute("saved", con.getUserSaved((Long) request.getSession().getAttribute("uid")));
            request.getRequestDispatcher("map.jsp").forward(request, response);
        }
        catch (SQLException | IOException e)
        {
            log(e.getMessage(), e);
            throw new ServletException(e);
        }
    }
    private void manageNoSession(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        try
        {
            ServletHelper.redirectErrorPage(request, response, HttpServletResponse.SC_UNAUTHORIZED, "Accesso richiesto", "login", true);
        }
        catch (IOException e)
        {
            log(e.getMessage(), e);
            throw new ServletException(e);
        }
    }
}