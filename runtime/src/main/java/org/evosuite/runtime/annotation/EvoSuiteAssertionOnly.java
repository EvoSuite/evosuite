package org.evosuite.runtime.annotation;

import java.lang.annotation.*;

/**
 * Specify that the tagged method can only be used during assertion generation.
 * It must be pure.
 *
 * Created by Andrea Arcuri on 22/05/15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface EvoSuiteAssertionOnly {
}
