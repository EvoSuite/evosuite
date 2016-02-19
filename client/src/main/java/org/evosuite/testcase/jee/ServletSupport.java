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
