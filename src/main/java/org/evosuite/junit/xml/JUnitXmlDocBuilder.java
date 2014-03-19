package org.evosuite.junit.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.junit.JUnitFailure;
import org.evosuite.junit.JUnitResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class JUnitXmlDocBuilder {

	// JUnitResult attributes and elements
	public static final String JUNIT_RESULT_ELEMENT_NAME = "JunitResult";
	public static final String FAILURE_COUNT_ATTRIBUTE_NAME = "failureCount";
	public static final String RUN_COUNT_ATTRIBUTE_NAME = "runCount";
	public static final String WAS_SUCCESSFUL_ATTRIBUTE_NAME = "wasSuccessful";

	// JUnitFailure attributes and elements
	public static final String JUNIT_FAILURE_ELEMENT_NAME = "JunitFailure";
	public static final String STACK_TRACE_CALL_ELEMENT_NAME = "stackTraceCall";
	public static final String STACK_TRACE_ELEMENT_NAME = "stackTrace";
	public static final String METHOD_NAME_ELEMENT_NAME = "methodName";
	public static final String CLASS_NAME_ELEMENT_NAME = "className";
	public static final String MESSAGE_ELEMENT_NAME = "message";
	public static final String IS_ASSERTION_ERROR_ATTRIBUTE_NAME = "isAssertionError";
	public static final String TRACE_ELEMENT_NAME = "trace";

	public Document buildDocument(JUnitResult junitResult)
			throws ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();

		Element junitResultElement = doc
				.createElement(JUNIT_RESULT_ELEMENT_NAME);
		doc.appendChild(junitResultElement);

		Attr wasSuccessfulAttr = doc
				.createAttribute(WAS_SUCCESSFUL_ATTRIBUTE_NAME);
		boolean wasSuccessful = junitResult.wasSuccessful();
		wasSuccessfulAttr.setValue(Boolean.toString(wasSuccessful));
		junitResultElement.setAttributeNode(wasSuccessfulAttr);

		Attr failureCountAttr = doc
				.createAttribute(FAILURE_COUNT_ATTRIBUTE_NAME);
		int failureCount = junitResult.getFailureCount();
		failureCountAttr.setValue(Integer.toString(failureCount));
		junitResultElement.setAttributeNode(failureCountAttr);

		Attr runCountAttr = doc.createAttribute(RUN_COUNT_ATTRIBUTE_NAME);
		int runCount = junitResult.getRunCount();
		runCountAttr.setValue(Integer.toString(runCount));
		junitResultElement.setAttributeNode(runCountAttr);

		for (JUnitFailure junitFailure : junitResult.getFailures()) {
			Element junitFailureElement = doc
					.createElement(JUNIT_FAILURE_ELEMENT_NAME);
			junitResultElement.appendChild(junitFailureElement);

			Attr isAssertionErrorAttr = doc
					.createAttribute(IS_ASSERTION_ERROR_ATTRIBUTE_NAME);
			boolean assertionError = junitFailure.isAssertionError();
			isAssertionErrorAttr.setValue(Boolean.toString(assertionError));
			junitFailureElement.setAttributeNode(isAssertionErrorAttr);

			Element messageElement = doc.createElement(MESSAGE_ELEMENT_NAME);
			junitFailureElement.appendChild(messageElement);
			String message = junitFailure.getMessage();
			if (message != null) {
				messageElement.appendChild(doc.createTextNode(message));
			}

			Element traceElement = doc.createElement(TRACE_ELEMENT_NAME);
			junitFailureElement.appendChild(traceElement);
			String trace = junitFailure.getTrace();
			if (trace != null) {
				traceElement.appendChild(doc.createTextNode(trace));
			}

			Element classNameElement = doc
					.createElement(CLASS_NAME_ELEMENT_NAME);
			junitFailureElement.appendChild(classNameElement);
			String exceptionClassName = junitFailure.getExceptionClassName();
			if (exceptionClassName != null) {
				classNameElement.appendChild(doc
						.createTextNode(exceptionClassName));
			}

			Element methodNameElement = doc
					.createElement(METHOD_NAME_ELEMENT_NAME);
			junitFailureElement.appendChild(methodNameElement);
			String descriptionMethodName = junitFailure
					.getDescriptionMethodName();
			if (descriptionMethodName != null) {
				methodNameElement.appendChild(doc
						.createTextNode(descriptionMethodName));
			}

			Element stackTraceElements = doc
					.createElement(STACK_TRACE_ELEMENT_NAME);
			junitFailureElement.appendChild(stackTraceElements);

			for (String stackTraceElementToString : junitFailure
					.getExceptionStackTrace()) {

				Element stackTraceElement = doc
						.createElement(STACK_TRACE_CALL_ELEMENT_NAME);
				stackTraceElements.appendChild(stackTraceElement);

				if (stackTraceElementToString != null) {
					stackTraceElement.appendChild(doc
							.createTextNode(stackTraceElementToString));
				}

			}

		}

		return doc;

	}

}
