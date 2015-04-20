package org.evosuite.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention( value = RetentionPolicy.RUNTIME)
@Target( value = { ElementType.METHOD})
public @interface EvoSuiteTest {
}
