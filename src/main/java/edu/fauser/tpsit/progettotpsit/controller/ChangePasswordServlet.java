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
        ServletHelper.checkSession(request, response, "uid", this::sendCode, ServletHelper::defaultManageExistingSession);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletHelper.checkSession(request, response, "uid", this::changePass, ServletHelper::defaultManageExistingSession);
    }
    private void changePass(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        try
        {
            if (ServletHelper.checkParams(request, "email", "pass", "code"))
                manageSC(request, response, con.changePassword(request.getParameter("email"), request.getParameter("code"), request.getParameter("pass")));
            else
                ServletHelper.redirectErrorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, "Completa tutti i campi", "change_password", true);
        }
        catch (IOException | SQLException e)
        {
            log(e.getMessage(), e);
            throw new ServletException(e);
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
    private void sendCode(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            try
            {
                if (ServletHelper.checkParams(request, "email") && !request.getParameter("email").isBlank())
                {
                    request.setAttribute("code", con.setChangePassword(request.getParameter("email")));
                    MailHelper.sendMail(request.getParameter("email"), "Codice cambio password", request, response, "WEB-INF/email/change_code.jsp");
                    ServletHelper.restRespond(response, HttpServletResponse.SC_OK, "Codice inviato!", getServletContext());
                }
                else
                    ServletHelper.restRespond(response, HttpServletResponse.SC_BAD_REQUEST, "Completa il campo email", getServletContext());
            }
            catch (SQLIntegrityConstraintViolationException e)
            {
                ServletHelper.restRespond(response, HttpServletResponse.SC_NOT_FOUND, "Utente non trovato", getServletContext());
            }
        }
        catch (SendFailedException e)
        {
            log(e.getMessage(), e);
            ServletHelper.restRespond(response, HttpServletResponse.SC_BAD_GATEWAY, "Errore durante l'invio della mail!", getServletContext());
        }
        catch (IOException | ServletException | SQLException | NoSuchAlgorithmException | MessagingException e)
        {
            log(e.getMessage(), e);
            ServletHelper.restRespond(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore del Server!", getServletContext());
        }
    }
}