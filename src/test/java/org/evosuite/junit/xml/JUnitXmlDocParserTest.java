package org.evosuite.junit.xml;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.evosuite.junit.FooTestClassLoader;
import org.evosuite.junit.JUnitFailure;
import org.evosuite.junit.JUnitResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class JUnitXmlDocParserTest extends JUnitXmlDocParser {

	private static final String JUNIT_RESULT_FILENAME = "junitresult.xml";

	@Before
	public void initTest() {
		cleanFileSystem();
	}

	private void cleanFileSystem() {
		File junitResult = new File(JUNIT_RESULT_FILENAME);
		if (junitResult.exists()) {
			junitResult.delete();
		}
	}

	@Test
	public void test() throws ParserConfigurationException,
			TransformerException, SAXException, IOException {

		File junitResultFile = new File(JUNIT_RESULT_FILENAME);
		assertFalse(junitResultFile.exists());
		JUnitExecutor executor = new JUnitExecutor();
		Class<?> fooTestClass = new FooTestClassLoader().loadFooTestClass();

		JUnitResult result = executor.execute(fooTestClass);
		JUnitFailure failure= result.getFailures().get(0);
		
		JUnitXmlDocBuilder builder = new JUnitXmlDocBuilder();
		Document doc = builder.buildDocument(result);

		JUnitXmlDocWriter writer = new JUnitXmlDocWriter();
		writer.writeToFile(doc, JUNIT_RESULT_FILENAME);
		assertTrue(junitResultFile.exists());

		JUnitXmlDocReader reader = new JUnitXmlDocReader();
		Document doc_from_file = reader.readFromFile(JUNIT_RESULT_FILENAME);

		JUnitXmlDocParser parser = new JUnitXmlDocParser();
		JUnitResult result_from_file = parser.parse(doc_from_file);

		assertFalse(result_from_file.wasSuccessful());
		assertEquals(1, result_from_file.getFailureCount());
		assertEquals(1, result_from_file.getFailures().size());
		JUnitFailure failure_from_file = result_from_file.getFailures().get(0);
		assertTrue(failure_from_file.isAssertionError());

		assertEquals(AssertionError.class.toString(), failure_from_file.getExceptionClassName());

		assertEquals(failure, failure_from_file);
		assertEquals(result, result_from_file);
	}

	@After
	public void afterTest() {
		cleanFileSystem();
	}

}
