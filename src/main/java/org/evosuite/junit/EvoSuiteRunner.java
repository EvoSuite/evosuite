package org.evosuite.junit;

import java.io.InputStream;

import org.evosuite.EvoSuite;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.utils.LoggingUtils;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class EvoSuiteRunner extends BlockJUnit4ClassRunner {

	
	public EvoSuiteRunner(Class<?> klass) throws InitializationError {
		super(getFromEvoSuiteClassloader(klass));
	}

	
	private static Class<?> getFromEvoSuiteClassloader(Class<?> clazz) throws InitializationError {
	    try {
	    	/*
	    	 *  properties like REPLACE_CALLS will be set directly in the JUnit files
	    	 */

	    	LoggingUtils.loadLogbackForEvoSuite();

	    	/*
	    	 * TODO: this approach does throw away all the possible instrumentation done on the input clazz,
	    	 * eg code coverage of Emma, Cobertura, Javalanche, etc.
	    	 * 
	    	 * maybe an option would be to use java agents:
	    	 * 
	    	 * http://dhruba.name/2010/02/07/creation-dynamic-loading-and-instrumentation-with-javaagents/
	    	 * http://www.eclemma.org/jacoco/trunk/doc/implementation.html
	    	 * http://osi.fotap.org/2008/06/27/dynamically-installing-agents-in-java-6/
	    	 * http://docs.oracle.com/javase/7/docs/api/java/lang/instrument/package-summary.html
	    	 */
	    	
	    	InstrumentingClassLoader classLoader = new InstrumentingClassLoader();
	        return Class.forName(clazz.getName(), true, classLoader);
	    } catch (ClassNotFoundException e) {
	        throw new InitializationError(e);
	    }
	}
	
}
