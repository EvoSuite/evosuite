package org.evosuite.junit.xml;

import static org.junit.Assert.assertNotNull;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.junit.JUnitResult;
import org.junit.Test;
import org.w3c.dom.Document;

import com.examples.with.different.packagename.junit.FooTest;

public class JUnitXmlDocBuilderTest {

	@Test
	public void testXmlWriter() throws ParserConfigurationException {
		JUnitExecutor executor = new JUnitExecutor();
		JUnitResult result = executor.execute(FooTest.class);
		JUnitXmlDocBuilder writer = new JUnitXmlDocBuilder();
		Document doc = writer.buildDocument(result);

		assertNotNull(doc);
	}
}
