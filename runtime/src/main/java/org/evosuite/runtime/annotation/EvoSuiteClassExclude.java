package org.evosuite.runtime.annotation;

import java.lang.annotation.*;

/**
 * Specify that no method in the tagged class can used in the test cases, unless
 * they are manually tagged. Note: if a class is not going to be used, it would simply
 * not added to the TestCluster. Using this annotation is useful when one wants to use
 * the class, but the majority of its methods should be excluded
 *
 * Created by Andrea Arcuri on 22/05/15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface EvoSuiteClassExclude {
}
