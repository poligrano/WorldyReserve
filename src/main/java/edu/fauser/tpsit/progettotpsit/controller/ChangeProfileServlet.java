package edu.fauser.tpsit.progettotpsit.controller;

import edu.fauser.tpsit.progettotpsit.entity.User;
import edu.fauser.tpsit.progettotpsit.helper.GoogleHelper;
import edu.fauser.tpsit.progettotpsit.helper.ServletHelper;
import edu.fauser.tpsit.progettotpsit.obj.DBConnection;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;

@MultipartConfig(maxFileSize = DBConnection.MAX_PFP_SIZE)
@WebServlet(name = "ChangeProfileServlet", value = "/changeprofile")
public class ChangeProfileServlet extends HttpServlet
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
        ServletHelper.checkSession(request, response, "uid", this::manageNoSession, this::sendToChangePage);
    }
    private void sendToChangePage(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        try
        {
            User u = con.retrieveUser((Long) request.getSession().getAttribute("uid"));
            request.setAttribute("u", u);
            request.setAttribute("mx", request.getParameter("mx"));
            request.getRequestDispatcher("change_profile.jsp").forward(request, response);
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
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletHelper.checkSession(request, response, "uid", this::manageNoSession, this::updateProfile);
    }
    private void updateProfile(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        try
        {
            con.updateUser((Long) request.getSession().getAttribute("uid"), request.getParameter("name"), request.getParameter("surname"), request.getPart("pfp"));
            response.sendRedirect(response.encodeRedirectURL("changeprofile?mx=Modifica avvenuta"));
        }
        catch (IOException | SQLException e)
        {
            log(e.getMessage(), e);
            throw new ServletException(e);
        }
    }
}