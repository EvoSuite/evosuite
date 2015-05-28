package org.evosuite.runtime.annotation;

import java.lang.annotation.*;


/**
 * Annotation used on methods that the user wants to exclude from the search, ie generate no test
 * case using such methods.
 *
 * <p>
 * This is also useful internally inside EvoSuite for example on mock objects (eg no point in calling
 * a sleep() on thread in a test case).
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface EvoSuiteExclude {

}
