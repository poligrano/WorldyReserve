package edu.fauser.tpsit.progettotpsit.helper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.BiConsumer;

public class ServletHelper
{
    public static String ERROR_PAGE_FOLDER = "errorPage";
    public static boolean checkParams(HttpServletRequest request, HttpServletResponse response, BiConsumer<HttpServletRequest, HttpServletResponse> onInvalid, BiConsumer<HttpServletRequest, HttpServletResponse> onValid, String... params)
    {
        for (String p : params)
            if (request.getParameter(p) == null)
            {
                if (onInvalid != null)
                    onInvalid.accept(request, response);
                return false;
            }
        if (onValid != null)
            onValid.accept(request, response);
        return true;
    }
    public static boolean checkSession(HttpServletRequest request, HttpServletResponse response, String att, BiConsumer<HttpServletRequest, HttpServletResponse> onInvalid, BiConsumer<HttpServletRequest, HttpServletResponse> onValid)
    {

        if (request.getSession(false) == null || request.getSession(false).getAttribute(att) == null)
        {
            if (onInvalid != null)
                onInvalid.accept(request, response);
            return false;
        }
        if (onValid != null)
            onValid.accept(request, response);
        return true;
    }
    public static boolean checkSession(HttpServletRequest request, String att)
    {
        return checkSession(request, null, att, null, null);
    }
    public static boolean checkParams(HttpServletRequest request, String... params)
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
    public static void defaultManageExistingSession(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            response.sendRedirect("/load");
        }
        catch (IOException e)
        {
            request.getServletContext().log(e.getMessage(), e);
        }
    }
}