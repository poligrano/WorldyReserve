package edu.fauser.tpsit.progettotpsit.controller;

import edu.fauser.tpsit.progettotpsit.helper.MailHelper;
import edu.fauser.tpsit.progettotpsit.helper.ServletHelper;
import edu.fauser.tpsit.progettotpsit.obj.DBConnection;

import javax.mail.MessagingException;
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
            try
            {
                if (ServletHelper.checkParams(request, "name", "surname", "pass", "email"))
                {
                    request.setAttribute("verify_URL", request.getRequestURL().toString());
                    request.setAttribute("code", con.insertNormalUser(request.getParameter("name"), request.getParameter("surname"), request.getParameter("email"), request.getParameter("pass"), request.getPart("pfp")));
                    MailHelper.sendMail(request.getParameter("email"), "Verifica indirizzo email", request, response, "WEB-INF/email/verify.jsp");
                    ServletHelper.redirectCustomPage(request, response, HttpServletResponse.SC_CREATED, "Verifica", "Ci sei quasi...", "Completa la verifica del tuo indirizzo e-mail " + request.getParameter("email") + " tramite il link inviato");
                }
                else
                    ServletHelper.redirectErrorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, "Completa tutti i campi", "signin", true);
            }
            catch (SQLIntegrityConstraintViolationException e)
            {
                ServletHelper.redirectErrorPage(request, response, HttpServletResponse.SC_CONFLICT, "Utente già registrato", "signin", true);
            }
        }
        catch (IOException | ServletException | SQLException | MessagingException e)
        {
            log(e.getMessage(), e);
        }
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        ServletHelper.checkSession(request, response, "uid", this::verifyUser, this::manageExistingSession);
    }
    private void verifyUser(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            if (ServletHelper.checkParams(request, "code"))
                manageSC(request, response, con.verifyUserMail(request.getParameter("code")));
            else
                ServletHelper.redirectCustomPage(request, response, HttpServletResponse.SC_BAD_REQUEST, "Verifica", "Link invalido", "Controlla che il link utilizzato sia quello corretto");
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
                ServletHelper.redirectCustomPage(request, response, HttpServletResponse.SC_OK, "Verifica", "Verifica completata", "La tua mail è stata verificata correttamente, ora puoi tornare alla pagina di login");
                break;
            case NotFound:
                ServletHelper.redirectCustomPage(request, response, HttpServletResponse.SC_NOT_FOUND, "Verifica", "Verifica non riuscita", "Codice inviato invalido o inesistente");
                break;
            case FoundButExpired:
                ServletHelper.redirectCustomPage(request, response, HttpServletResponse.SC_GONE, "Verifica", "Verifica non riuscita", "Il codice di verifica è scaduto, ritenta la procedura di registrazione");
                break;
        }
    }
}