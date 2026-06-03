package edu.fauser.tpsit.progettotpsit.obj;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@FunctionalInterface
public interface RequestManager
{
    void manage(HttpServletRequest request, HttpServletResponse response) throws ServletException;
}