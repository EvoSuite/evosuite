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
package org.evosuite.runtime.annotation;

import java.lang.annotation.*;

/**
 * Specify that the given method is bound to the given input variable.
 * The method cannot be removed as long as that variable exists.
 * If that variables is removed, then this method should be removed as well.
 *
 * Created by Andrea Arcuri on 30/05/15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Documented
public @interface BoundInputVariable {

    /**
     * Specify that the given method, where this annotation is used, is an
     * initializer for the bound variable.
     * This means it can be used only directly after a "new" and before
     * any other method
     *
     * @return
     */
    boolean initializer() default false;


    /**
     * Specify that the given method can only be called once on the bounded variable
     *
     * @return
     */
    boolean atMostOnce() default false;

    /**
     * Specify that the given method can only be called once with same
     * parameters on the bounded variable.
     * In other words, the method can be called more than once, but then
     * there should be at least on parameter which is different
     *
     * @return
     */
    boolean atMostOnceWithSameParameters() default false;

}
