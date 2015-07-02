package org.evosuite.testcase.jee;

import org.evosuite.runtime.javaee.javax.servlet.EvoServletState;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;

/**
 * Created by Andrea Arcuri on 29/06/15.
 */
public class ServletSupport {

    private static final Logger logger = LoggerFactory.getLogger(InjectionSupport.class);

    private static volatile GenericMethod servletInit;

    public static GenericMethod getServletInit(){
        if(servletInit == null){
            try {
                servletInit = new GenericMethod(
                        EvoServletState.class.getDeclaredMethod("initServlet",Servlet.class)
                        , EvoServletState.class);
            } catch (NoSuchMethodException e) {
                logger.error("Reflection failed in ServletSupport: "+e.getMessage());
                return null;
            }
        }
        return servletInit;
    }
}
