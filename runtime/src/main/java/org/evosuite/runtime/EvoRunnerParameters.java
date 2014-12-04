package org.evosuite.runtime;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation needed to pass parameters to EvoRunner
 * 
 * @author arcuri
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME) 
public @interface EvoRunnerParameters {

	boolean mockJVMNonDeterminism() default false; 

    boolean useVFS() default false;

    boolean resetStaticState() default false;
    
    boolean separateClassLoader() default false;
   
}
