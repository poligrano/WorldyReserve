package edu.fauser.tpsit.progettotpsit.helper;

import edu.fauser.tpsit.progettotpsit.obj.StringWrapperResponse;
import edu.fauser.tpsit.progettotpsit.singleton.EnvVar;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class MailHelper
{
    private static final String HOST = "smtp.gmail.com";
    private static final int PORT = 587;
    private static final String SSL_PROTOCOL = "TLSv1.2";
    private static final String SENDER_ADDR = EnvVar.getInstance().getEnv().get("EMAIL");
    private static final String SENDER_PSW = EnvVar.getInstance().getEnv().get("EMAIL_PSW");
    private static final Session sess = getSession();
    private static Properties getProperties()
    {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", HOST);
        prop.put("mail.smtp.port", String.valueOf(PORT));
        prop.put("mail.smtp.ssl.protocols", SSL_PROTOCOL);
        return prop;
    }
    private static Session getSession()
    {
        return Session.getInstance(getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(SENDER_ADDR, SENDER_PSW);
            }
        });
    }
    private static Message getMessage(String recipients, String subject, String body) throws UnsupportedEncodingException, MessagingException
    {
        Message mx = new MimeMessage(sess);
        mx.setFrom(new InternetAddress(SENDER_ADDR, "no-reply"));
        mx.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients, false));
        mx.setSubject(subject);
        mx.setContent(body, "text/html");
        return mx;
    }
    public static void sendMail(String recipients, String subject, String body) throws MessagingException, UnsupportedEncodingException
    {
        Transport.send(getMessage(recipients, subject, body));
    }
    public static void sendMail(String recipients, String subject, HttpServletRequest request, HttpServletResponse response, String jspPath) throws ServletException, IOException, MessagingException
    {
        StringWrapperResponse body = new StringWrapperResponse(response);
        request.getRequestDispatcher(jspPath).forward(request, body);
        sendMail(recipients, subject, body.getOutput());
    }
}