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
 * Define a set of constraints for a method when used in a generated test case.
 *
 * Created by Andrea Arcuri on 22/05/15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.CONSTRUCTOR})
@Documented
public @interface Constraints {

    /**
     *  Specify that the tagged method can appear in a test case at most once (or never).
     */
    boolean atMostOnce() default false;

    /**
     * Specify that none of the inputs to the tagged method is null
     *
     * @return
     */
    boolean noNullInputs() default false;

    /**
     * If the tagged method is in the test case, none of these other methods specified
     * here can be present in the test at the same time.
     * If any of those methods belongs to another class, then its canonical name should be used,
     * eg 'className'#'methodName'.
     *
     * <p> Note: if a method is overloaded with different input parameters, all of those variants
     * will be excluded.
     *
     * @return
     */
    String[] excludeOthers() default {};

    /**
     * Specify that the tagged method can only be used <i>after</i> this other one.
     * If this other method belongs to another class, then its canonical name should be used,
     * eg 'className'#'methodName'.
     * @return
     */
    String after() default "";


    /**
     * List of properties that should hold to use this tagged method in a test case.
     * The properties are dynamic, and based on previous execution of the test cases.
     *
     * @return
     */
    String[] dependOnProperties() default  {};

    /**
     * Input parameters of this method should not be changed.
     * If those are primitives (e.g., String or int) it is recommended
     * that they become inlined
     *
     * @return
     */
    boolean notMutable() default false;

    /**
     * Specify if this method can be added to a test case as part of the search.
     * If not, it means that it will added only in some special cases, eg as a result
     * of some other events.
     * An example is dependency injection in JavaEE.
     *
     * @return
     */
    boolean noDirectInsertion() default false;
}
