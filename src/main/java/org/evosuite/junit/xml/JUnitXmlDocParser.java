package org.evosuite.junit.xml;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.junit.JUnitFailure;
import org.evosuite.junit.JUnitResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class JUnitXmlDocParser {

	public JUnitResult parse(Document doc) {
		Element documentElement = doc.getDocumentElement();
		return parseResult(documentElement);
	}

	private JUnitResult parseResult(Element resultElement) {
		String wasSuccessfulAttrValue = resultElement
				.getAttribute(JUnitXmlDocBuilder.WAS_SUCCESSFUL_ATTRIBUTE_NAME);
		boolean wasSuccessful = Boolean.valueOf(wasSuccessfulAttrValue);

		String failureCountAttrValue = resultElement
				.getAttribute(JUnitXmlDocBuilder.FAILURE_COUNT_ATTRIBUTE_NAME);
		int failureCount = Integer.valueOf(failureCountAttrValue);

		String runCountAttrValue = resultElement
				.getAttribute(JUnitXmlDocBuilder.RUN_COUNT_ATTRIBUTE_NAME);
		int runCount = Integer.valueOf(runCountAttrValue);

		JUnitResult result = new JUnitResult(wasSuccessful, failureCount,
				runCount);

		NodeList junitResultChildren = resultElement.getChildNodes();
		for (int i = 0; i < junitResultChildren.getLength(); i++) {
			Node junitResultChild = junitResultChildren.item(i);
			if (junitResultChild.getNodeName().equals(
					JUnitXmlDocBuilder.JUNIT_FAILURE_ELEMENT_NAME)) {
				JUnitFailure junitFailure = parseFailure((Element) junitResultChild);
				result.addFailure(junitFailure);
			}
		}
		return result;
	}

	private JUnitFailure parseFailure(Element failureElement) {
		String isAssertionErrorAttrValue = failureElement
				.getAttribute(JUnitXmlDocBuilder.IS_ASSERTION_ERROR_ATTRIBUTE_NAME);
		boolean isAssertionError = Boolean.valueOf(isAssertionErrorAttrValue);

		String message = null;
		String className = null;
		String methodName = null;
		String trace = null;
		List<String> failureStackTrace = null;
		for (int i = 0; i < failureElement.getChildNodes().getLength(); i++) {
			Node childNode = failureElement.getChildNodes().item(i);
			String nodeName = childNode.getNodeName();
			if (nodeName.equals(JUnitXmlDocBuilder.MESSAGE_ELEMENT_NAME)) {
				message = childNode.getTextContent();
			} else if (nodeName
					.equals(JUnitXmlDocBuilder.CLASS_NAME_ELEMENT_NAME)) {
				className = childNode.getTextContent();
			} else if (nodeName
					.equals(JUnitXmlDocBuilder.METHOD_NAME_ELEMENT_NAME)) {
				methodName = childNode.getTextContent();
			} else if (nodeName.equals(JUnitXmlDocBuilder.TRACE_ELEMENT_NAME)) {
				trace = childNode.getTextContent();
			} else if (nodeName
					.equals(JUnitXmlDocBuilder.STACK_TRACE_ELEMENT_NAME)) {
				failureStackTrace = parseStackTrace((Element) childNode);
			}
		}

		JUnitFailure failure = new JUnitFailure(message, className, methodName,
				isAssertionError, trace);
		if (failureStackTrace != null) {
			for (String stackTraceElem : failureStackTrace) {
				failure.addToExceptionStackTrace(stackTraceElem);
			}
		}
		return failure;
	}

	private List<String> parseStackTrace(Element stackTraceElement) {
		ArrayList<String> failureStackTrace = new ArrayList<String>();
		NodeList childNodes = stackTraceElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {

			Node childNode = childNodes.item(i);
			if (childNode.getNodeName().equals(
					JUnitXmlDocBuilder.STACK_TRACE_CALL_ELEMENT_NAME)) {
				failureStackTrace.add(childNode.getTextContent());
			}

		}
		return failureStackTrace;
	}

}
