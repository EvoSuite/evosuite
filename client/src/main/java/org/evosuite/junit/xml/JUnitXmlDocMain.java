/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
