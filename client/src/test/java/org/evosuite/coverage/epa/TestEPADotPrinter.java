package org.evosuite.coverage.epa;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assume;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TestEPADotPrinter {

	@Test
	public void testListItr() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "ListItr.xml");
		final File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());

		EPA automata = EPAFactory.buildEPA(xmlFilename);

		int stateCount = automata.getStates().size();
		assertEquals(8, stateCount);

		int transitionCount = automata.getTransitions().size();
		assertEquals(69, transitionCount);

		EPADotPrinter printer = new EPADotPrinter();
		String dot = printer.toDot(automata);

		System.out.println(dot);
	}

	@Test
	public void testMyBoundedStack()
			throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "MyBoundedStack.xml");
		final File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());

		EPA epa = EPAFactory.buildEPA(xmlFilename);

		int stateCount = epa.getStates().size();
		assertEquals(4, stateCount);

		int transitionCount = epa.getTransitions().size();
		assertEquals(7, transitionCount);
		

		EPADotPrinter printer = new EPADotPrinter();
		String dot = printer.toDot(epa);

		System.out.println(dot);
	}

}
