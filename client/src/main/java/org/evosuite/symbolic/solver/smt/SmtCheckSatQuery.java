package org.evosuite.symbolic.solver.smt;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SmtCheckSatQuery {

	static Logger logger = LoggerFactory.getLogger(SmtCheckSatQuery.class);

	private final List<SmtConstantDeclaration> constantDeclarations;

	private final List<SmtFunctionDeclaration> functionDeclarations;

	private final List<SmtFunctionDefinition> functionDefinitions;

	private final List<SmtAssertion> assertions;

	public SmtCheckSatQuery(List<SmtConstantDeclaration> constantDeclarations,
			List<SmtFunctionDeclaration> functionDeclarations,
			List<SmtFunctionDefinition> functionDefinitions,
			List<SmtAssertion> assertions) {
		this.constantDeclarations = constantDeclarations;
		this.functionDeclarations = functionDeclarations;
		this.functionDefinitions = functionDefinitions;
		this.assertions = assertions;
	}

	public SmtCheckSatQuery(List<SmtConstantDeclaration> constantDeclarations,
			List<SmtAssertion> assertions) {
		this(constantDeclarations, new LinkedList<SmtFunctionDeclaration>(),
				new LinkedList<SmtFunctionDefinition>(), assertions);
	}

	public SmtCheckSatQuery(List<SmtConstantDeclaration> constantDeclarations,
			List<SmtFunctionDefinition> functionDefinitions,
			List<SmtAssertion> assertions) {
		this(constantDeclarations, new LinkedList<SmtFunctionDeclaration>(),
				functionDefinitions, assertions);
	}

	public List<SmtAssertion> getAssertions() {
		return assertions;
	}

	public List<SmtConstantDeclaration> getConstantDeclarations() {
		return constantDeclarations;
	}

	public List<SmtFunctionDefinition> getFunctionDefinitions() {
		return this.functionDefinitions;
	}

	public List<SmtFunctionDeclaration> getFunctionDeclarations() {
		return this.functionDeclarations;
	}
}
