/*
 * From JBoss, Apache License Apache License, Version 2.0
 */
package com.examples.with.different.packagename.jee.injection.wildfly;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@WebServlet("/BMT")
public class TransactionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    static String PAGE_HEADER = "<html><head><title>bmt</title></head><body>";

    static String PAGE_CONTENT = "<h1>Stepping Outside the Container (with JPA and JTA)</h1>"
        + "<form>"
        + "<input checked type=\"checkbox\" name=\"strategy\" value=\"managed\" /> Use bean managed Entity Managers <br />"
        + "Key: <input type=\"text\" name=\"key\" /><br />"
        + "Value: <input type=\"text\" name=\"value\" /><br />"
        + "<input type=\"submit\" value=\"Submit\" /><br />"
        + "</form>";

    static String PAGE_FOOTER = "</body></html>";

    @Inject
    ManagedComponent managedBean;

    @Inject
    UnManagedComponent unManagedBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        String responseText;
        String key = req.getParameter("key");
        String value = req.getParameter("value");
        String txStrategy = req.getParameter("strategy");

        if ("managed".equalsIgnoreCase(txStrategy))
            responseText = managedBean.updateKeyValueDatabase(key, value);
        else
            responseText = unManagedBean.updateKeyValueDatabase(key, value);

        writer.println(PAGE_HEADER);
        writer.println(PAGE_CONTENT);
        writer.println("<p>" + responseText + "</p>");
        writer.println(PAGE_FOOTER);

        writer.close();
    }

}
