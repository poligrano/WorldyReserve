package edu.fauser.tpsit.progettotpsit.controller;

import edu.fauser.tpsit.progettotpsit.helper.MailHelper;
import edu.fauser.tpsit.progettotpsit.helper.ServletHelper;
import edu.fauser.tpsit.progettotpsit.obj.DBConnection;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

@WebServlet(name = "ChangePasswordServlet", value = "/change")
public class ChangePasswordServlet extends HttpServlet
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
        sendCode(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        ServletHelper.checkSession(request, response, "uid", this::changePass, this::manageExistingSession);
    }
    private void changePass(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            if (ServletHelper.checkParams(request, "email", "pass", "code"))
                manageSC(request, response, con.changePassword(request.getParameter("email"), request.getParameter("code"), request.getParameter("pass")));
            else
                ServletHelper.redirectErrorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, "Completa tutti i campi", "change_password", true);
        }
        catch (IOException | ServletException | SQLException e)
        {
            log(e.getMessage(), e);
        }
    }
    private void manageSC(HttpServletRequest request, HttpServletResponse response, DBConnection.SCVerify sc) throws ServletException, IOException
    {
        switch (sc)
        {
            case Found:
                ServletHelper.redirectErrorPage(request, response, HttpServletResponse.SC_OK, "Password cambiata con successo", "login", true);
                break;
            case NotFound:
                ServletHelper.redirectErrorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, "Codice invalido", "change_password", true);
                break;
            case FoundButExpired:
                ServletHelper.redirectErrorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, "Codice scaduto", "change_password", true);
        }
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
    private void sendCode(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            if (ServletHelper.checkParams(request, "email") && !request.getParameter("email").isBlank())
            {
                request.setAttribute("code", con.setChangePassword(request.getParameter("email")));
                MailHelper.sendMail(request.getParameter("email"), "Codice cambio password", request, response, "WEB-INF/email/change_code.jsp");
                restRespond(response, HttpServletResponse.SC_OK, "Codice inviato!");
            }
            else
                restRespond(response, HttpServletResponse.SC_BAD_REQUEST, "Parametro \"email\" mancante!");
        }
        catch (SendFailedException e)
        {
            log(e.getMessage(), e);
            restRespond(response, HttpServletResponse.SC_BAD_GATEWAY, "Errore durante l'invio della mail!");
        }
        catch (IOException | ServletException | SQLException | NoSuchAlgorithmException | MessagingException e)
        {
            log(e.getMessage(), e);
            restRespond(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore del Server!");
        }
    }
    private void restRespond(HttpServletResponse response, int sc, String message)
    {
        try
        {
            response.setStatus(sc);
            response.setContentType("text/plain");
            response.getOutputStream().println(message);
            response.flushBuffer();
        }
        catch (IOException e)
        {
            log(e.getMessage(), e);
        }
    }
}