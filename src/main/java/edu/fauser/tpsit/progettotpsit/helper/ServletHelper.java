package edu.fauser.tpsit.progettotpsit.helper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.function.BiConsumer;

public class ServletHelper
{
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
    public static boolean checkParams(HttpServletRequest request, String... params)
    {
        return checkParams(request, null, null, null, params);
    }
}