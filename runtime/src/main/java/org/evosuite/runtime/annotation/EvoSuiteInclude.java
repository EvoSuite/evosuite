package org.evosuite.runtime.annotation;

import java.lang.annotation.*;

/**
 * Manually specify that the tagged method can be used in the generated test cases.
 * Note: this is only needed when the class is tagged with {@link EvoSuiteClassExclude}
 *
 * Created by Andrea Arcuri on 22/05/15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.CONSTRUCTOR})
@Documented
public @interface EvoSuiteInclude {
}
