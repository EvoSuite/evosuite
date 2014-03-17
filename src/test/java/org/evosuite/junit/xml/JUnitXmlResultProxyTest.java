package org.evosuite.junit.xml;

import static org.junit.Assert.*;

import java.io.File;

import org.evosuite.junit.JUnitResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.junit.FooTest;

public class JUnitXmlResultProxyTest {

	private static final String JUNIT_RESULT_FILENAME = "junitresult.xml";

	@Before
	public void setUp() throws Exception {
		cleanFileSystem();
	}

	@After
	public void tearDown() throws Exception {
		cleanFileSystem();
	}

	@Test
	public void test() throws JUnitXmlResultProxyException {
		assertFalse(checkFileExists());
		JUnitExecutor executor = new JUnitExecutor();
		JUnitResult result = executor.execute(FooTest.class);
		JUnitXmlResultProxy proxy = new JUnitXmlResultProxy();
		
		proxy.writeToXmlFile(result, JUNIT_RESULT_FILENAME);
		assertTrue(checkFileExists());
		
		JUnitResult result_from_xml_file = proxy.readFromXmlFile(JUNIT_RESULT_FILENAME);
	
		assertEquals(result, result_from_xml_file);
	}

	private void cleanFileSystem() {
		File junitResult = new File(JUNIT_RESULT_FILENAME);
		if (junitResult.exists()) {
			junitResult.delete();
		}
	}
	
	private boolean checkFileExists() {
		File file = new File(JUNIT_RESULT_FILENAME);
		return file.exists();
		
	}

}
