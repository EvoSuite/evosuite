package org.evosuite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


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
public @interface EvoSuiteExclude {

}
