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

import org.evosuite.runtime.util.Inputs;

import javax.servlet.http.HttpServlet;
import java.util.*;

/**
 * In some cases, there are classes we want to instantiate only once, eg an HTTP Servlet,
 * where having more than one of it would make no sense from a unit testing point of view.
 * Note, this is independent on whether it is the CUT or not.
 *
 * Created by Andrea Arcuri on 29/06/15.
 */
public class InstanceOnlyOnce {

    private static final Set<Class<?>> classes = Collections.unmodifiableSet(
            new HashSet<Class<?>>(){{
                add(HttpServlet.class);
            }}
    );

    public static boolean canInstantiateOnlyOnce(Class<?> klass) throws IllegalArgumentException{
        Inputs.checkNull(klass);
        for(Class<?> c : classes) {
            if(c.isAssignableFrom(klass)){
                return true;
            }
        }
        return false;
    }
}
