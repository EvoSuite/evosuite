package org.evosuite.junit.xml;

import static org.junit.Assert.assertNotNull;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.junit.FooTestClassLoader;
import org.evosuite.junit.JUnitResult;
import org.junit.Test;
import org.w3c.dom.Document;


public class JUnitXmlDocBuilderTest {

	@Test
	public void testXmlWriter() throws ParserConfigurationException {
		JUnitExecutor executor = new JUnitExecutor();
		Class<?> fooTestClass = new FooTestClassLoader().loadFooTestClass();
		JUnitResult result = executor.execute(fooTestClass);
		JUnitXmlDocBuilder writer = new JUnitXmlDocBuilder();
		Document doc = writer.buildDocument(result);

		assertNotNull(doc);
	}
}
