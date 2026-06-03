package edu.fauser.tpsit.progettotpsit.controller;

import edu.fauser.tpsit.progettotpsit.helper.ServletHelper;
import edu.fauser.tpsit.progettotpsit.obj.DBConnection;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "GetMetaServlet", value = "/getmeta")
public class GetMetaServlet extends HttpServlet
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
        ServletHelper.checkSession(request, response, "uid", (req, resp) -> ServletHelper.restRespond(resp, HttpServletResponse.SC_UNAUTHORIZED, "Nessuna sessione trovata!", getServletContext()), this::sendMeta);
    }
    private void sendMeta(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        try
        {
            if (ServletHelper.checkParams(request, "id"))
            {
                ServletHelper.restRespond(response, HttpServletResponse.SC_OK, con.getUserPoiMeta(Long.parseLong(request.getParameter("id")), (Long) request.getSession().getAttribute("uid")).toJSON().toString(), "application/json", getServletContext());
            }
            else
                ServletHelper.restRespond(response, HttpServletResponse.SC_BAD_REQUEST, "Parametro id non trovato!", getServletContext());
        }
        catch (SQLException e)
        {
            log(e.getMessage(), e);
            ServletHelper.restRespond(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore del Server!", getServletContext());
        }
    }
}