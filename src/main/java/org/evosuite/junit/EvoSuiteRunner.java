package org.evosuite.junit;

import java.io.InputStream;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.javaagent.InstrumentingClassLoader;
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

	private static void setLogLevel() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		// Only overrule default configurations
		// TODO: Find better way to allow external logback configuration
		if (context.getName().equals("default")) {
			try {
				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext(context);
				InputStream f = EvoSuite.class.getClassLoader().getResourceAsStream("logback-evosuite.xml");
				if (f == null) {
					System.err.println("logback-evosuite.xml not found on classpath");
				}
				context.reset();
				configurator.doConfigure(f);
				} catch (JoranException je) {
					// StatusPrinter will handle this
				}
				StatusPrinter.printInCaseOfErrorsOrWarnings(context);
			}
	}
	
	private static Class<?> getFromEvoSuiteClassloader(Class<?> clazz) throws InitializationError {
	    try {
	    	// TODO: Is the default configuration at runtime any different? 
	    	Properties.REPLACE_CALLS = true;

	    	// TODO: Do we need to know the target class or not?
	    	//Properties.TARGET_CLASS = TimeOperation.class.getName();

	    	setLogLevel();
	    	InstrumentingClassLoader classLoader = new InstrumentingClassLoader();
	        return Class.forName(clazz.getName(), true, classLoader);
	    } catch (ClassNotFoundException e) {
	        throw new InitializationError(e);
	    }
	}
	
}
