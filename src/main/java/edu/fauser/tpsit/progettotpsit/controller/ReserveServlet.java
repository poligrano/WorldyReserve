package edu.fauser.tpsit.progettotpsit.controller;

import edu.fauser.tpsit.progettotpsit.helper.MailHelper;
import edu.fauser.tpsit.progettotpsit.helper.ServletHelper;
import edu.fauser.tpsit.progettotpsit.obj.DBConnection;

import javax.mail.MessagingException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;

@WebServlet(name = "ReserveServlet", value = "/reserve")
public class ReserveServlet extends HttpServlet
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        ServletHelper.checkSession(request, response, "uid", this::manageNoSession, this::deleteReservation);
    }
    private void deleteReservation(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            if (ServletHelper.checkParams(request, "id"))
            {
                con.deleteReservation(Long.parseLong(request.getParameter("id")), (Long) request.getSession().getAttribute("uid"));
                ServletHelper.redirectCustomPage(request, response, HttpServletResponse.SC_OK, "Cancellata", "Prenotazione cancellata", "Prenotazione cancellata con successo");
            }
            else
                ServletHelper.redirectCustomPage(request, response, HttpServletResponse.SC_BAD_REQUEST, "Errore", "Link invalido", "Controlla che il link inserito sia quello giusto");
        }
        catch (ServletException | IOException | SQLException e)
        {
            log(e.getMessage(), e);
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        ServletHelper.checkSession(request, response, "uid", this::manageNoSession, this::reservePoi);
    }
    private void manageNoSession(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            ServletHelper.redirectErrorPage(request, response, HttpServletResponse.SC_UNAUTHORIZED, "Accesso richiesto", "login", true);
        }
        catch (IOException | ServletException e)
        {
            log(e.getMessage(), e);
        }
    }
    private void reservePoi(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            try
            {
                if (ServletHelper.checkParams(request, "id", "name", "start", "end"))
                {
                    long id = con.reservePoi(Long.parseLong(request.getParameter("id")), (Long) request.getSession().getAttribute("uid"), Date.valueOf(request.getParameter("start")), Date.valueOf(request.getParameter("end")));
                    request.setAttribute("userName", con.retrieveUserName((Long) request.getSession().getAttribute("uid")));
                    request.setAttribute("name", request.getParameter("name"));
                    request.setAttribute("start", request.getParameter("start"));
                    request.setAttribute("end", request.getParameter("end"));
                    request.setAttribute("id", id);
                    request.setAttribute("verify_URL", request.getRequestURL().toString());
                    MailHelper.sendMail(con.retrieveEmail((Long) request.getSession().getAttribute("uid")), "Prenotazione Completata", request, response, "WEB-INF/email/reservation_success.jsp");
                    ServletHelper.redirectCustomPage(request, response, HttpServletResponse.SC_OK, "Prenotazione Completata", "Grazie per aver prenotato tramite il nostro servizio", "Una email di conferma è stata inviata al tuo indirizzo");
                }
                else
                    ServletHelper.redirectErrorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, "Completa tutti i campi", "reserve", false);
            }
            catch (SQLException | MessagingException e)
            {
                ServletHelper.redirectErrorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage(), "reserve", false);
            }
        }
        catch (IOException | ServletException e)
        {
            log(e.getMessage(), e);
        }
    }
}