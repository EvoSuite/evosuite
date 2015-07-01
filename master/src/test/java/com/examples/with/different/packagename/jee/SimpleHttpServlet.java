package com.examples.with.different.packagename.jee;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Andrea Arcuri on 01/07/15.
 */
public class SimpleHttpServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(req!=null && resp!=null){
            System.out.println("req and resp were properly created");
        }
    }
}
