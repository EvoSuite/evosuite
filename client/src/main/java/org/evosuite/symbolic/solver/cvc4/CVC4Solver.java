/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.solver.cvc4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.solver.SmtExprBuilder;
import org.evosuite.symbolic.solver.SolverEmptyQueryException;
import org.evosuite.symbolic.solver.SolverErrorException;
import org.evosuite.symbolic.solver.SolverParseException;
import org.evosuite.symbolic.solver.SolverResult;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.SubProcessSolver;
import org.evosuite.symbolic.solver.smt.SmtAssertion;
import org.evosuite.symbolic.solver.smt.SmtCheckSatQuery;
import org.evosuite.symbolic.solver.smt.SmtConstantDeclaration;
import org.evosuite.symbolic.solver.smt.SmtExpr;
import org.evosuite.symbolic.solver.smt.SmtFunctionDeclaration;
import org.evosuite.symbolic.solver.smt.SmtFunctionDefinition;
import org.evosuite.symbolic.solver.smt.SmtIntVariable;
import org.evosuite.symbolic.solver.smt.SmtOperation;
import org.evosuite.symbolic.solver.smt.SmtOperation.Operator;
import org.evosuite.symbolic.solver.smt.SmtOperatorCollector;
import org.evosuite.symbolic.solver.smt.SmtRealVariable;
import org.evosuite.symbolic.solver.smt.SmtStringVariable;
import org.evosuite.symbolic.solver.smt.SmtVariable;
import org.evosuite.symbolic.solver.smt.SmtVariableCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CVC4Solver extends SubProcessSolver {

	private boolean reWriteNonLinearConstraints = false;

	/**
	 * If enabled the translation will approximate non-linear constraints with
	 * concrete values
	 * 
	 * @param rewrite
	 */
	public void setRewriteNonLinearConstraints(boolean rewrite) {
		reWriteNonLinearConstraints = rewrite;
	}

	static Logger logger = LoggerFactory.getLogger(CVC4Solver.class);

	public CVC4Solver(boolean addMissingValues) {
		super(addMissingValues);
	}

	public CVC4Solver() {
		super();
	}

	@Override
	public SolverResult solve(Collection<Constraint<?>> constraints) throws SolverTimeoutException,
			SolverEmptyQueryException, SolverErrorException, SolverParseException, IOException {

		if (Properties.CVC4_PATH == null) {
			String errMsg = "Property CVC4_PATH should be setted in order to use the CVC4 Solver!";
			logger.error(errMsg);
			throw new IllegalStateException(errMsg);
		}

		// CVC4 has very little support for non-linear arithemtics
		// In fact, it cannot even produce models for non-linear theories
		if (!reWriteNonLinearConstraints && hasNonLinearConstraints(constraints)) {
			logger.debug("Skipping query due to (unsupported) non-linear constraints");
			throw new SolverEmptyQueryException("Skipping query due to (unsupported) non-linear constraints");
		}

		long cvcTimeout = Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS;

		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		for (Constraint<?> c : constraints) {
			Set<Variable<?>> c_variables = c.getVariables();
			variables.addAll(c_variables);
		}

		SmtCheckSatQuery smtQuery = buildSmtCheckSatQuery(constraints);

		if (smtQuery == null) {
			logger.debug("No variables found during the creation of the SMT query.");
			throw new SolverEmptyQueryException("No variables found during the creation of the SMT query.");
		}

		if (smtQuery.getAssertions().isEmpty()) {
			Map<String, Object> emptySolution = new HashMap<String, Object>();
			SolverResult emptySAT = SolverResult.newSAT(emptySolution);
			return emptySAT;
		}

		CVC4QueryPrinter printer = new CVC4QueryPrinter();
		String smtQueryStr = printer.print(smtQuery);

		if (smtQueryStr == null) {
			logger.debug("No variables found during constraint solving.");
			throw new SolverEmptyQueryException("No variables found during constraint solving.");
		}

		logger.debug("CVC4 Query:");
		logger.debug(smtQueryStr);

		String cvc4Cmd = buildCVC4cmd(cvcTimeout);

		ByteArrayOutputStream stdout = new ByteArrayOutputStream();

		try {
			launchNewProcess(cvc4Cmd, smtQueryStr, (int) cvcTimeout, stdout);

			String cvc4ResultStr = stdout.toString("UTF-8");

			if (cvc4ResultStr.startsWith("unsat") && cvc4ResultStr.contains(
					"(error \"Cannot get the current model unless immediately preceded by SAT/INVALID or UNKNOWN response.\")")) {
				// UNSAT
				SolverResult unsatResult = SolverResult.newUNSAT();
				return unsatResult;
			}

			if (cvc4ResultStr.contains("error")) {
				String errMsg = "An error occurred while executing CVC4!";
				logger.error(errMsg);
				throw new SolverErrorException(errMsg);
			}

			// parse solution
			Map<String, Object> initialValues = getConcreteValues(variables);
			CVC4ResultParser resultParser;
			if (addMissingVariables()) {
				resultParser = new CVC4ResultParser(initialValues);
			} else {
				resultParser = new CVC4ResultParser();
			}
			SolverResult solverResult = resultParser.parse(cvc4ResultStr);

			if (solverResult.isSAT()) {
				// check if the found solution is useful
				boolean check = checkSAT(constraints, solverResult);
				if (!check) {
					logger.debug("CVC4 solution does not solve the original constraint system. ");
					SolverResult unsatResult = SolverResult.newUNSAT();
					return unsatResult;
				}
			}

			return solverResult;

		} catch (IOException e) {
			if (e.getMessage().contains("Permission denied")) {
				logger.error("No permissions for running CVC4 binary");
			} else {
				logger.error("IO Exception during launching of CVC4 command");
			}
			throw e;

		}

	}

	private static SmtCheckSatQuery buildSmtCheckSatQuery(Collection<Constraint<?>> constraints) {

		ConstraintToCVC4Visitor v = new ConstraintToCVC4Visitor(true);
		SmtVariableCollector varCollector = new SmtVariableCollector();
		SmtOperatorCollector funCollector = new SmtOperatorCollector();

		List<SmtAssertion> smtAssertions = new LinkedList<SmtAssertion>();
		for (Constraint<?> c : constraints) {
			SmtExpr smtExpr = c.accept(v, null);
			if (smtExpr != null) {
				SmtAssertion smtAssertion = new SmtAssertion(smtExpr);
				smtAssertions.add(smtAssertion);
				smtExpr.accept(varCollector, null);
				smtExpr.accept(funCollector, null);
			}
		}

		Set<SmtVariable> variables = varCollector.getSmtVariables();

		if (variables.isEmpty()) {
			return null; // no variables, constraint system is trivial
		}

		List<SmtFunctionDefinition> functionDefinitions = new LinkedList<SmtFunctionDefinition>();

		final boolean addCharToInt = funCollector.getOperators().contains(Operator.CHAR_TO_INT);
		if (addCharToInt) {
			String charToIntFunction = buildCharToIntFunction();
			SmtFunctionDefinition funcDefinition = new SmtFunctionDefinition(charToIntFunction);
			functionDefinitions.add(funcDefinition);
		}

		final boolean addIntToChar = funCollector.getOperators().contains(Operator.INT_TO_CHAR);
		if (addIntToChar) {
			String intToCharFunction = buildIntToCharFunction();
			SmtFunctionDefinition funcDefinition = new SmtFunctionDefinition(intToCharFunction);
			functionDefinitions.add(funcDefinition);
		}

		List<SmtFunctionDeclaration> functionDeclarations = new LinkedList<SmtFunctionDeclaration>();
		for (SmtVariable var : variables) {
			String varName = var.getName();
			if (var instanceof SmtIntVariable) {
				SmtFunctionDeclaration intVar = SmtExprBuilder.mkIntFunctionDeclaration(varName);
				functionDeclarations.add(intVar);

			} else if (var instanceof SmtRealVariable) {
				SmtFunctionDeclaration realVar = SmtExprBuilder.mkRealFunctionDeclaration(varName);
				functionDeclarations.add(realVar);

			} else if (var instanceof SmtStringVariable) {
				SmtFunctionDeclaration stringVar = SmtExprBuilder.mkStringFunctionDeclaration(varName);
				functionDeclarations.add(stringVar);
			} else {
				throw new RuntimeException("Unknown variable type " + var.getClass().getCanonicalName());
			}
		}

		SmtCheckSatQuery smtQuery = new SmtCheckSatQuery(new LinkedList<SmtConstantDeclaration>(), functionDeclarations,
				functionDefinitions, smtAssertions);

		return smtQuery;

	}

	private static String buildCVC4cmd(long cvcTimeout) {
		String cmd = Properties.CVC4_PATH;
		cmd += "  --rewrite-divk"; // rewrite-divk rewrites division (or
									// modulus) by a constant value
		cmd += " --lang smt"; // query language is SMT-LIB
		cmd += " --finite-model-find";
		/**
		 * Option --finite-model-find has two effects:
		 * 
		 * 1. It extends the ground UF solver to
		 * "UF + finite cardinality constraints", as described in this paper:
		 * http://homepage.cs.uiowa.edu/~ajreynol/cav13.pdf This is used to
		 * minimize the size of the interpretation of uninterpreted sorts.
		 * 
		 * 2. It modifies the quantifier instantiation heuristics, as described
		 * in this paper: http://homepage.cs.uiowa.edu/~ajreynol/cade24.pdf In
		 * particular, it disables E-matching and enables
		 * "model-based quantifier instantiation", which is the strategy that
		 * enables CVC4 to answer "SAT" in the presence of universally
		 * quantified formulas.
		 * 
		 * More details on both of these points can be found in Sections 5.2 -
		 * 5.4 of http://homepage.cs.uiowa.edu/~ajreynol/thesis.pdf.
		 */
		cmd += " --tlimit=" + cvcTimeout; // set timeout to cvcTimeout
		return cmd;
	}

	private static boolean hasNonLinearConstraints(Collection<Constraint<?>> constraints) {
		NonLinearConstraintVisitor v = new NonLinearConstraintVisitor();
		for (Constraint<?> constraint : constraints) {
			Boolean ret_val = constraint.accept(v, null);
			if (ret_val) {
				return true;
			}
		}
		return false;
	}

	// private final static int ASCII_TABLE_LENGTH = 256;
	private final static int ASCII_TABLE_LENGTH = 256;

	private static String buildIntToCharFunction() {
		StringBuffer buff = new StringBuffer();
		buff.append(SmtOperation.Operator.INT_TO_CHAR + "((!x Int)) String");
		buff.append("\n");
		for (int i = 0; i < ASCII_TABLE_LENGTH; i++) {
			String hexStr;
			if (i < 16) {
				hexStr = "0" + Integer.toHexString(i);
			} else {
				hexStr = Integer.toHexString(i);
			}
			String escapedHexStr = "\\x" + hexStr;
			if (i < ASCII_TABLE_LENGTH - 1) {
				String iteStr = String.format("(ite (= !x %s) \"%s\"", i, escapedHexStr);
				buff.append(iteStr);
				buff.append("\n");
			} else {
				buff.append(String.format("\"%s\"", escapedHexStr));
			}
		}
		for (int i = 0; i < ASCII_TABLE_LENGTH - 1; i++) {
			buff.append(")");
		}
		return buff.toString();
	}

	private static String buildCharToIntFunction() {
		StringBuffer buff = new StringBuffer();
		buff.append(SmtOperation.Operator.CHAR_TO_INT + "((!x String)) Int");
		buff.append("\n");
		for (int i = 0; i < ASCII_TABLE_LENGTH; i++) {
			String hexStr;
			if (i < 16) {
				hexStr = "0" + Integer.toHexString(i);
			} else {
				hexStr = Integer.toHexString(i);
			}
			String escapedHexStr = "\\x" + hexStr;
			if (i < ASCII_TABLE_LENGTH - 1) {
				String iteStr = String.format("(ite (= !x \"%s\") %s", escapedHexStr, i);
				buff.append(iteStr);
				buff.append("\n");
			} else {
				buff.append(i);
			}
		}
		for (int i = 0; i < ASCII_TABLE_LENGTH - 1; i++) {
			buff.append(")");
		}
		return buff.toString();
	}

}
