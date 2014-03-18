package org.evosuite.junit.xml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.evosuite.junit.FooTestClassLoader;
import org.evosuite.junit.JUnitResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;


public class JUnitXmlDocWriterTest extends JUnitXmlDocWriter {

	private static final String JUNIT_RESULT_FILENAME = "junitresult.xml";

	@Test
	public void test() throws ParserConfigurationException,
			TransformerException {

		File junitResult = new File(JUNIT_RESULT_FILENAME);
		assertFalse(junitResult.exists());
		JUnitExecutor executor = new JUnitExecutor();
		Class<?> fooTestClass = new FooTestClassLoader().loadFooTestClass();
		JUnitResult result = executor.execute(fooTestClass);
		JUnitXmlDocBuilder builder = new JUnitXmlDocBuilder();
		Document doc = builder.buildDocument(result);

		JUnitXmlDocWriter writer = new JUnitXmlDocWriter();
		writer.writeToFile(doc, JUNIT_RESULT_FILENAME);
		assertTrue(junitResult.exists());
	}

	@Before
	public void initTest() {
		cleanFileSystem();
	}

	@After
	public void afterTest() {
		cleanFileSystem();
	}
	
	private void cleanFileSystem() {
		File junitResult = new File(JUNIT_RESULT_FILENAME);
		if (junitResult.exists()) {
			junitResult.delete();
		}
	}

}
