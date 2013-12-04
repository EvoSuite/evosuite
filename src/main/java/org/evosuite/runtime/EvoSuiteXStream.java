package org.evosuite.runtime;

import com.thoughtworks.xstream.XStream;

public class EvoSuiteXStream {

	/**
	 * This wrapper is used to allow use of XStream from the evosuite
	 * dependencies rather than making XStream a dependency of the SUT
	 * 
	 * @param value
	 * @return
	 */
	public static Object fromString(String value) {
		XStream xstream = new XStream();
		return xstream.fromXML(value);
	}
}
