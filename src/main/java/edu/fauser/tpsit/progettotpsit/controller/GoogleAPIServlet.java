package edu.fauser.tpsit.progettotpsit.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import edu.fauser.tpsit.progettotpsit.helper.GoogleHelper;
import edu.fauser.tpsit.progettotpsit.helper.ServletHelper;
import edu.fauser.tpsit.progettotpsit.obj.DBConnection;
import edu.fauser.tpsit.progettotpsit.singleton.EnvVar;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

@WebServlet(name = "GoogleAPIServlet", value = "/google")
public class GoogleAPIServlet extends HttpServlet
{
    DBConnection con;
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
        manageUser(request, response);
    }
    private void manageUser(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            try
            {
                if (ServletHelper.checkParams(request, "credential"))
                {
                    request.getSession().setAttribute("uid", GoogleHelper.manageGoogleUser(con, request.getParameter("credential")));
                    response.sendRedirect("load");
                }
                else
                    ServletHelper.redirectErrorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, "Accesso con google non riuscito", "login", true);
            }
            catch (SQLIntegrityConstraintViolationException e)
            {
                ServletHelper.redirectErrorPage(request, response, HttpServletResponse.SC_CONFLICT, "Utente già esistente", "login", true);
            }
        }
        catch (SQLException | GeneralSecurityException | IOException | ExecutionException | InterruptedException | ServletException e)
        {
            log(e.getMessage(), e);
        }
    }
}