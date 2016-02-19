/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.javaee.javax.servlet;

import org.evosuite.runtime.annotation.*;
import org.evosuite.runtime.javaee.TestDataJavaEE;
import org.evosuite.runtime.javaee.javax.servlet.http.EvoHttpServletRequest;
import org.evosuite.runtime.javaee.javax.servlet.http.EvoHttpServletResponse;

import javax.servlet.AsyncContext;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

/**
 * Class used in the JUnit test cases to access the state of the servlet
 *
 * Created by Andrea Arcuri on 21/05/15.
 */
@EvoSuiteClassExclude
public class EvoServletState {

    /*
        Note: they need to be static, as called directly from tests
     */

    private static EvoServletConfig config;
    private static EvoHttpServletRequest req;
    private static EvoHttpServletResponse resp;

    /**
     * Usually, this will be the SUT
     */
    private static Servlet servlet;

    @EvoSuiteExclude
    public static void reset(){
        config = null;
        req = null;
        resp = null;
        servlet = null;
    }

    @EvoSuiteExclude
    public static Servlet getServlet() {
        return servlet;
    }

    /*
        Note: the constraints here imply that at most one servlet can be tested in  single test case
     */

    @EvoSuiteInclude
    @Constraints(atMostOnce = true, noNullInputs = true, noDirectInsertion = true)
    public static <T extends Servlet> T initServlet(
            @BoundInputVariable(initializer = true, atMostOnce = true) T servlet)
            throws IllegalStateException, IllegalArgumentException, ServletException {

        if(servlet == null){
            throw new IllegalArgumentException("Null servlet");
        }
        if(EvoServletState.servlet != null){
            throw new IllegalStateException("Should only be one servlet per test");
        }
        EvoServletState.servlet = servlet;
        servlet.init(getConfiguration());
        TestDataJavaEE.getInstance().setWasAServletInitialized(true);
        return servlet;
    }

    @EvoSuiteInclude
    @Constraints(atMostOnce = true, after = "initServlet")
    public static EvoServletConfig getConfiguration() throws IllegalStateException{
        checkInit();
        if(config == null){
            config = new EvoServletConfig();
        }
        return config;
    }

    @EvoSuiteInclude
    @Constraints(atMostOnce = true, after = "initServlet")
    public static EvoHttpServletRequest getRequest() throws IllegalStateException{
        checkInit();
        if(req == null){
            req = new EvoHttpServletRequest();
        }
        return req;
    }

    @EvoSuiteInclude
    @Constraints(atMostOnce = true, after = "initServlet")
    public static EvoHttpServletResponse getResponse() throws IllegalStateException{
        checkInit();
        if(resp == null){
            resp = new EvoHttpServletResponse();
        }
        return resp;
    }

    @EvoSuiteInclude
    @Constraints(atMostOnce = true, after = "initServlet")
    public static AsyncContext getAsyncContext() throws IllegalStateException{
        checkInit();
        if(getRequest().isAsyncStarted()){
            return getRequest().getAsyncContext();
        } else {
            return getRequest().startAsync();
        }
    }

    private static void checkInit() throws IllegalStateException{
        if(servlet == null){
            throw new IllegalStateException("Servlet is not initialized");
        }
    }
}
