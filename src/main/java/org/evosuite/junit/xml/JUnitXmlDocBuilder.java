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

		Attr wasSuccessful = doc.createAttribute(WAS_SUCCESSFUL_ATTRIBUTE_NAME);
		wasSuccessful.setValue(Boolean.toString(junitResult.wasSuccessful()));
		junitResultElement.setAttributeNode(wasSuccessful);

		Attr failureCount = doc.createAttribute(FAILURE_COUNT_ATTRIBUTE_NAME);
		failureCount.setValue(Integer.toString(junitResult.getFailureCount()));
		junitResultElement.setAttributeNode(failureCount);

		Attr runCount = doc.createAttribute(RUN_COUNT_ATTRIBUTE_NAME);
		runCount.setValue(Integer.toString(junitResult.getRunCount()));
		junitResultElement.setAttributeNode(runCount);
		
		for (JUnitFailure junitFailure : junitResult.getFailures()) {
			Element junitFailureElement = doc
					.createElement(JUNIT_FAILURE_ELEMENT_NAME);
			junitResultElement.appendChild(junitFailureElement);

			Attr isAssertionError = doc
					.createAttribute(IS_ASSERTION_ERROR_ATTRIBUTE_NAME);
			isAssertionError.setValue(Boolean.toString(junitFailure
					.isAssertionError()));
			junitFailureElement.setAttributeNode(isAssertionError);

			Element messageElement = doc.createElement(MESSAGE_ELEMENT_NAME);
			junitFailureElement.appendChild(messageElement);
			messageElement.appendChild(doc.createTextNode(junitFailure
					.getMessage()));

			Element traceElement = doc.createElement(TRACE_ELEMENT_NAME);
			junitFailureElement.appendChild(traceElement);
			traceElement.appendChild(doc.createTextNode(junitFailure
					.getTrace()));
			
			Element classNameElement = doc
					.createElement(CLASS_NAME_ELEMENT_NAME);
			junitFailureElement.appendChild(classNameElement);
			classNameElement.appendChild(doc.createTextNode(junitFailure
					.getExceptionClassName()));

			Element methodNameElement = doc
					.createElement(METHOD_NAME_ELEMENT_NAME);
			junitFailureElement.appendChild(methodNameElement);
			methodNameElement.appendChild(doc.createTextNode(junitFailure
					.getDescriptionMethodName()));

			Element stackTraceElements = doc
					.createElement(STACK_TRACE_ELEMENT_NAME);
			junitFailureElement.appendChild(stackTraceElements);

			for (String stackTraceElementToString : junitFailure
					.getExceptionStackTrace()) {

				Element stackTraceElement = doc
						.createElement(STACK_TRACE_CALL_ELEMENT_NAME);
				stackTraceElements.appendChild(stackTraceElement);
				stackTraceElement.appendChild(doc
						.createTextNode(stackTraceElementToString));

			}

		}

		return doc;

	}

}
