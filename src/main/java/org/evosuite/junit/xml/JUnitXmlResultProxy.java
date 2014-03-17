package org.evosuite.junit.xml;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.evosuite.junit.JUnitResult;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class JUnitXmlResultProxy {

	public void writeToXmlFile(JUnitResult junitResult, String xmlFileName)
			throws JUnitXmlResultProxyException {
		try {
			JUnitXmlDocBuilder xmlBuilder = new JUnitXmlDocBuilder();
			Document xmlDocument = xmlBuilder.buildDocument(junitResult);

			JUnitXmlDocWriter xmlWriter = new JUnitXmlDocWriter();
			xmlWriter.writeToFile(xmlDocument, xmlFileName);

		} catch (ParserConfigurationException e) {
			throw new JUnitXmlResultProxyException(e);
		} catch (TransformerException e) {
			throw new JUnitXmlResultProxyException(e);
		}
	}

	public JUnitResult readFromXmlFile(String xmlFileName)
			throws JUnitXmlResultProxyException {

		try {
			JUnitXmlDocReader xmlReader = new JUnitXmlDocReader();
			Document xmlDocument = xmlReader.readFromFile(xmlFileName);

			JUnitXmlDocParser xmlParser = new JUnitXmlDocParser();
			JUnitResult junitResult = xmlParser.parse(xmlDocument);
			return junitResult;
		} catch (ParserConfigurationException e) {
			throw new JUnitXmlResultProxyException(e);
		} catch (SAXException e) {
			throw new JUnitXmlResultProxyException(e);
		} catch (IOException e) {
			throw new JUnitXmlResultProxyException(e);
		}
	}
}
