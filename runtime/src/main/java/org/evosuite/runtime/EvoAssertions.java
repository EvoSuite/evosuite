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
package org.evosuite.runtime;

import org.junit.internal.AssumptionViolatedException;

import java.lang.annotation.Annotation;

/**
 * Created by Andrea Arcuri on 19/08/15.
 */
public class EvoAssertions {

    /**
     * Check if the given exception was thrown in the given class.
     * In some special cases, the exception is rather rethrown
     *
     */
    public static void verifyException(String sourceClass, Throwable t)throws AssertionError{

        // this can happen in false positives for PAFM
        if(t instanceof AssumptionViolatedException){
            throw (AssumptionViolatedException) t;
        }

        //non functional requirement exceptions are handled specially in the generated tests
        if(t instanceof TooManyResourcesException){
            throw (TooManyResourcesException) t;
        }

        assertThrownBy(sourceClass, t);
    }

    /**
     * Check if the given exception was thrown in the given class
     *
     * @param sourceClass
     * @param t
     * @throws AssertionError
     */
    public static void assertThrownBy(String sourceClass, Throwable t) throws AssertionError{
        StackTraceElement[] stackTrace = t.getStackTrace();

        // TODO: Force mocked exceptions to always have stack trace
        if(stackTrace.length == 0)
            return;

        StackTraceElement el = stackTrace[0];

        if(sourceClass==null){
            return; //can this even happen?
        }


        String name = el.getClassName();
        if(sourceClass.equals(name)){
            return; //OK, same class, as expected
        }

        /*
            Edge case: exception is thrown in a superclass method that is not overriden...
            need to check hierarchy, interfaces included (as they might have code since Java 8)
         */

        Class<?> klass;

        try {
           klass = EvoAssertions.class.getClassLoader().loadClass(sourceClass);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Cannot load/analyze class "+sourceClass);
        }

        for(Annotation annotation : klass.getAnnotations()){
            if(annotation.getClass().getName().equals(name)){
                return;
            }
        }

        while(klass != null){
            klass = klass.getSuperclass();
            if(klass != null && klass.getName().equals(name)){
                return;
            }
        }

        throw new AssertionError("Exception was not thrown in "+sourceClass +" but in "+el+": "+t);
    }
}
