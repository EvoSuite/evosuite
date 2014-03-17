package org.evosuite.junit.xml;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.evosuite.junit.JUnitResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.examples.with.different.packagename.junit.FooTest;

public class JUnitXmlDocReaderTest extends JUnitXmlDocReader {

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

		File junitResult = new File(JUNIT_RESULT_FILENAME);
		assertFalse(junitResult.exists());
		JUnitExecutor executor = new JUnitExecutor();
		JUnitResult result = executor.execute(FooTest.class);
		JUnitXmlDocBuilder builder = new JUnitXmlDocBuilder();
		Document doc = builder.buildDocument(result);

		JUnitXmlDocWriter writer = new JUnitXmlDocWriter();
		writer.writeToFile(doc, JUNIT_RESULT_FILENAME);
		assertTrue(junitResult.exists());

		JUnitXmlDocReader reader = new JUnitXmlDocReader();
		reader.readFromFile(JUNIT_RESULT_FILENAME);
	}

	@After
	public void afterTest() {
		cleanFileSystem();
	}

}
