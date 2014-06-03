package org.evosuite.junit.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Vector;

import org.evosuite.junit.JUnitResult;

import com.thoughtworks.xstream.XStream;

public class JUnitXmlDocMain {

	public static void main(String[] args) throws ClassNotFoundException,
			IOException {
		if (args.length <= 1) {
			System.err.println("Error: Incorrect Usage of "
					+ JUnitXmlDocMain.class.getCanonicalName());
			System.err
					.println("<Usage> testClassName1 testClassName2 ... xmlFilename");
			throw new IllegalArgumentException(
					"Argument String[] args is not correct");
		}

		ArrayList<String> testClassNames = new ArrayList<String>();
		for (int i = 0; i < args.length - 1; i++) {
			testClassNames.add(args[i]);
		}
		String xmlFilename = args[args.length - 1];
		Vector<Class<?>> testClasses = new Vector<Class<?>>();
		for (String testClassName : testClassNames) {

			try {
				Class<?> testClass = Class.forName(testClassName);
				testClasses.add(testClass);
			} catch (ClassNotFoundException e) {
				System.err.println("Error: could not load test class "
						+ testClassName);
				throw e;
			}
		}

		JUnitExecutor executor = new JUnitExecutor();
		JUnitResult junitResult = executor.execute(testClasses
				.toArray(new Class<?>[0]));

		writeXML(junitResult, xmlFilename);
	}

	private static void writeXML(JUnitResult junitResult, String xmlFilename)
			throws IOException {
		XStream xstream = new XStream();
		String data = xstream.toXML(junitResult);
		File file = new File(xmlFilename);
		FileOutputStream fos = new FileOutputStream(file);
		try {
			Charset charset = Charset.defaultCharset();
			if (data != null) {
				fos.write(data.getBytes(charset));
			}
		} finally {
			fos.close();
		}
	}

}
