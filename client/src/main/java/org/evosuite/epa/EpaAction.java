package org.evosuite.epa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.CONSTRUCTOR, ElementType.METHOD })
public @interface EpaAction {
	public String name();
	public String notEnabledExceptionList() default "";
	public String enabledExceptionList() default "";
}
