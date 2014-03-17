package org.evosuite.junit.xml;

import java.io.File;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

class JUnitXmlDocWriter {

	private static final String YES = "yes";

	public StreamResult writeToFile(Document document, String fileName)
			throws TransformerException {
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, YES);
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(new File(fileName));
		transformer.transform(source, result);
		return result;
	}
}
