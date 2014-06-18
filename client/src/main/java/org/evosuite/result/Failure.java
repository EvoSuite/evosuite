package org.evosuite.result;

import java.io.Serializable;
import java.util.Arrays;

import org.evosuite.Properties;
import org.evosuite.contracts.ContractViolation;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.TestCase;

public class Failure implements Serializable {

	private static final long serialVersionUID = -6308624160029658643L;

	private String className;
	
	private String methodName;
	
	private String exceptionName;

	private String exceptionMessage;

	private StackTraceElement[] stackTrace;

	private int lineNo;
	
	public Failure(ContractViolation violation) {
		this.className = Properties.TARGET_CLASS;
		this.lineNo = violation.getPosition();
		initializeFromContractViolation(violation);
	}
	
	public Failure(Throwable t, int position, TestCase test) {
		this.className = Properties.TARGET_CLASS;
		this.methodName = getMethodName(test, position);
		this.exceptionName = t.getClass().getName();
		this.exceptionMessage = t.getMessage();
		this.stackTrace = t.getStackTrace();		
		this.lineNo = position;
	}
	
	private String getMethodName(TestCase test, int position) {
		StatementInterface statement = test.getStatement(position);
		if(statement instanceof MethodStatement) {
			return ((MethodStatement)statement).getMethod().getName();
		} else if(statement instanceof ConstructorStatement) {
			return ((ConstructorStatement)statement).getConstructor().getName();
		} else {
			return "";
		}
	}
	
	private void initializeFromContractViolation(ContractViolation violation) {
		// TODO
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getExceptionName() {
		return exceptionName;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

	public StackTraceElement[] getStackTrace() {
		return stackTrace;
	}

	public int getLineNo() {
		return lineNo;
	}

	@Override
	public String toString() {
		return "Failure [className=" + className + ", methodName=" + methodName
				+ ", exceptionName=" + exceptionName + ", exceptionMessage="
				+ exceptionMessage + ", stackTrace="
				+ Arrays.toString(stackTrace) + ", lineNo=" + lineNo + "]";
	}
	
	
}
