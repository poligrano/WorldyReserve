package edu.fauser.tpsit.progettotpsit.helper;

import edu.fauser.tpsit.progettotpsit.interfaces.RequestManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ServletHelper
{
    public static String ERROR_PAGE_FOLDER = "errorPage";
    public static boolean checkParams(HttpServletRequest request, HttpServletResponse response, RequestManager onInvalid, RequestManager onValid, String... params) throws ServletException
    {
        for (String p : params)
            if (request.getParameter(p) == null || request.getParameter(p).isBlank())
            {
                if (onInvalid != null)
                    onInvalid.manage(request, response);
                return false;
            }
        if (onValid != null)
            onValid.manage(request, response);
        return true;
    }
    public static boolean checkSession(HttpServletRequest request, HttpServletResponse response, String att, RequestManager onInvalid, RequestManager onValid) throws ServletException
    {

        if (request.getSession(false) == null || request.getSession(false).getAttribute(att) == null)
        {
            if (onInvalid != null)
                onInvalid.manage(request, response);
            return false;
        }
        if (onValid != null)
            onValid.manage(request, response);
        return true;
    }
    public static boolean checkSession(HttpServletRequest request, String att) throws ServletException
    {
        return checkSession(request, null, att, null, null);
    }
    public static boolean checkParams(HttpServletRequest request, String... params) throws ServletException
    {
        return checkParams(request, null, null, null, params);
    }
    public static void redirectErrorPage(HttpServletRequest request, HttpServletResponse response, int statusCode, String errorMx, String errorContext, boolean invalidateSession) throws ServletException, IOException
    {
        response.setStatus(statusCode);
        request.setAttribute("error_mx", errorMx);
        if (invalidateSession)
            request.getSession().invalidate();
        request.getRequestDispatcher(ERROR_PAGE_FOLDER + "/" + errorContext + "Error.jsp").forward(request, response);
    }
    public static void redirectCustomPage(HttpServletRequest request, HttpServletResponse response, int statusCode, String title, String subtitle, String mx) throws ServletException, IOException
    {
        response.setStatus(statusCode);
        request.setAttribute("title", title);
        request.setAttribute("subtitle", subtitle);
        request.setAttribute("mx", mx);
        request.getRequestDispatcher("custom_page.jsp").forward(request, response);
    }
    public static void defaultManageExistingSession(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        try
        {
            response.sendRedirect("/load");
        }
        catch (IOException e)
        {
            request.getServletContext().log(e.getMessage(), e);
            throw new ServletException(e);
        }
    }
    public static void restRespond(HttpServletResponse response, int sc, String message, ServletContext context)
    {
        restRespond(response, sc, message, "text/plain", context);
    }
    public static void restRespond(HttpServletResponse response, int sc, String message, String contentType, ServletContext context)
    {
        try
        {
            response.setStatus(sc);
            response.setContentType(contentType);
            response.getOutputStream().print(message);
            response.flushBuffer();
        }
        catch (IOException e)
        {
            context.log(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}