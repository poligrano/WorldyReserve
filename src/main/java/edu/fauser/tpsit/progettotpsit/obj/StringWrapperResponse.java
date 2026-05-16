package edu.fauser.tpsit.progettotpsit.obj;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class StringWrapperResponse extends HttpServletResponseWrapper
{
    private final CharArrayWriter content;
    public StringWrapperResponse(HttpServletResponse response)
    {
        super(response);
        content = new CharArrayWriter();
    }
    @Override
    public PrintWriter getWriter() throws IOException
    {
        return new PrintWriter(content);
    }
    public String getOutput()
    {
        return content.toString();
    }
}