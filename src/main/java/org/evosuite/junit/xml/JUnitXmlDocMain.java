package org.evosuite.junit.xml;

import java.util.ArrayList;
import java.util.Vector;

import org.evosuite.junit.JUnitResult;



public class JUnitXmlDocMain {

	public static void main(String[] args) throws ClassNotFoundException, JUnitXmlResultProxyException {
		if (args.length<1) {
			System.err.println("Error: Incorrect Usage of " + JUnitXmlDocMain.class.getCanonicalName());
			System.err.println("<Usage> testClassName1 testClassName2 ... xmlFilename");
			return;
		}
		
		ArrayList<String> testClassNames = new ArrayList<String>();
		for(int i=0;i<args.length-1;i++) {
			testClassNames.add(args[i]);
		}
		String xmlFilename = args[args.length-1];
		Vector<Class<?>> testClasses = new Vector<Class<?>>();
		for (String testClassName : testClassNames) {
			
			try {
				Class<?> testClass= Class.forName(testClassName);
				testClasses.add(testClass);
			} catch (ClassNotFoundException e) {
				System.err.println("Error: could not load test class " + testClassName);
				throw e;
			}
		}
		
		JUnitExecutor executor = new JUnitExecutor();
		JUnitResult junitResult = executor.execute(testClasses.toArray(new Class<?>[0]));
		
		JUnitXmlResultProxy proxy = new JUnitXmlResultProxy();
		try {
			proxy.writeToXmlFile(junitResult, xmlFilename);
		} catch (JUnitXmlResultProxyException e) {
			System.err.println("Error: could not write result of JUnit execution to " + xmlFilename);
			throw e;
		}
	}

}
