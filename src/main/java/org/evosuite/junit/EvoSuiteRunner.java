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
	    	InstrumentingClassLoader classLoader = new InstrumentingClassLoader();
	        return Class.forName(clazz.getName(), true, classLoader);
	    } catch (ClassNotFoundException e) {
	        throw new InitializationError(e);
	    }
	}
	
}
