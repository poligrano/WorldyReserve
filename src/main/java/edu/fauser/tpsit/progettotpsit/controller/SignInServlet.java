package edu.fauser.tpsit.progettotpsit.controller;

import edu.fauser.tpsit.progettotpsit.helper.ServletHelper;
import edu.fauser.tpsit.progettotpsit.obj.DBConnection;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

@MultipartConfig
@WebServlet(name = "SignInServlet", value = "/signin")
public class SignInServlet extends HttpServlet
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
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
            try
            {
                if (ServletHelper.checkParams(request, "name", "surname", "pass", "email"))
                {
                    con.insertNormalUser(request.getParameter("name"), request.getParameter("surname"), request.getParameter("email"), request.getParameter("pass"), request.getPart("pfp"));
                    response.sendRedirect("index.jsp");
                }
                else
                    ServletHelper.redirectErrorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, "Completa tutti i campi", "signin", true);
            }
            catch (SQLIntegrityConstraintViolationException e)
            {
                ServletHelper.redirectErrorPage(request, response, HttpServletResponse.SC_CONFLICT, "Utente già registrato", "signin", true);
            }
        }
        catch (IOException | ServletException | SQLException e)
        {
            log(e.getMessage(), e);
        }
    }
}