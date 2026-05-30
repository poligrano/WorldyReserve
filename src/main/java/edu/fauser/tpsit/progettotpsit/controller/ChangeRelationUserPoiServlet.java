package edu.fauser.tpsit.progettotpsit.controller;

import edu.fauser.tpsit.progettotpsit.helper.ServletHelper;
import edu.fauser.tpsit.progettotpsit.obj.DBConnection;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "ChangeRelationUserPoiServlet", value = "/updatemeta")
public class ChangeRelationUserPoiServlet extends HttpServlet
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
        ServletHelper.checkSession(request, response, "uid", (req, resp) -> ServletHelper.restRespond(resp, HttpServletResponse.SC_UNAUTHORIZED, "Nessuna sessione trovata", getServletContext()), this::changeLikeFavRelation);
    }
    private void changeLikeFavRelation(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            if (ServletHelper.checkParams(request, "id", "does_like", "favourite"))
            {
                con.changeUserPoiRel(Long.parseLong(request.getParameter("id")), (Long) request.getSession().getAttribute("uid"), Boolean.parseBoolean(request.getParameter("does_like")), Boolean.parseBoolean(request.getParameter("favourite")));
                ServletHelper.restRespond(response, HttpServletResponse.SC_OK, "Relazione cambiata correttamente", getServletContext());
            }
            else
                ServletHelper.restRespond(response, HttpServletResponse.SC_BAD_REQUEST, "Parametri mancanti", getServletContext());
        }
        catch (SQLException e)
        {
            log(e.getMessage(), e);
            ServletHelper.restRespond(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore del Server!", getServletContext());
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            if (ServletHelper.checkParams(request, "id", "comment"))
            {
                con.postComment(Long.parseLong(request.getParameter("id")), (Long) request.getSession().getAttribute("uid"), request.getParameter("comment"));
                ServletHelper.restRespond(response, HttpServletResponse.SC_OK, "Commento postato", getServletContext());
            }
            else
                ServletHelper.restRespond(response, HttpServletResponse.SC_BAD_REQUEST, "Parametri mancanti", getServletContext());
        }
        catch (SQLException e)
        {
            log(e.getMessage(), e);
            ServletHelper.restRespond(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore del Server!", getServletContext());
        }
    }
}