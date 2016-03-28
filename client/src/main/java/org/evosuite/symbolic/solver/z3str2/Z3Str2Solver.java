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
package org.evosuite.symbolic.solver.z3str2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
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
import org.evosuite.symbolic.solver.smt.SmtFunctionDefinition;
import org.evosuite.symbolic.solver.smt.SmtIntVariable;
import org.evosuite.symbolic.solver.smt.SmtOperation;
import org.evosuite.symbolic.solver.smt.SmtOperation.Operator;
import org.evosuite.symbolic.solver.smt.SmtOperatorCollector;
import org.evosuite.symbolic.solver.smt.SmtRealVariable;
import org.evosuite.symbolic.solver.smt.SmtStringVariable;
import org.evosuite.symbolic.solver.smt.SmtVariable;
import org.evosuite.symbolic.solver.smt.SmtVariableCollector;
import org.evosuite.symbolic.solver.z3.Z3Solver;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.utils.FileIOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Z3Str2Solver extends SubProcessSolver {

	private static final String EVOSUITE_Z3_STR_FILENAME = "evosuite.z3";

	static Logger logger = LoggerFactory.getLogger(Z3Solver.class);

	private static int dirCounter = 0;

	public Z3Str2Solver() {
		super();
	}

	public Z3Str2Solver(boolean addMissingVariables) {
		super(addMissingVariables);
	}

	private static File createNewTmpDir() {
		File dir = null;
		String dirName = FileUtils.getTempDirectoryPath() + File.separator + "EvoSuiteZ3Str_" + (dirCounter++) + "_"
				+ System.currentTimeMillis();

		// first create a tmp folder
		dir = new File(dirName);
		if (!dir.mkdirs()) {
			logger.error("Cannot create tmp dir: " + dirName);
			return null;
		}

		if (!dir.exists()) {
			logger.error(
					"Weird behavior: we created folder, but Java cannot determine if it exists? Folder: " + dirName);
			return null;
		}

		return dir;
	}

	private static SmtCheckSatQuery buildSmtQuerty(Collection<Constraint<?>> constraints) {

		ConstraintToZ3Str2Visitor v = new ConstraintToZ3Str2Visitor();
		List<SmtAssertion> assertions = new LinkedList<SmtAssertion>();

		SmtVariableCollector varCollector = new SmtVariableCollector();
		SmtOperatorCollector opCollector = new SmtOperatorCollector();

		for (Constraint<?> c : constraints) {
			SmtExpr smtExpr = c.accept(v, null);
			if (smtExpr != null) {
				SmtAssertion newAssertion = new SmtAssertion(smtExpr);
				assertions.add(newAssertion);
				smtExpr.accept(varCollector, null);
				smtExpr.accept(opCollector, null);
			}
		}

		Set<SmtVariable> smtVariables = varCollector.getSmtVariables();
		Set<Operator> smtOperators = opCollector.getOperators();

		boolean addCharToIntFunction;
		if (smtOperators.contains(SmtOperation.Operator.CHAR_TO_INT)) {
			addCharToIntFunction = true;
		} else {
			addCharToIntFunction = false;
		}

		Set<SmtVariable> smtVariablesToDeclare = new HashSet<SmtVariable>(smtVariables);
		if (addCharToIntFunction) {
			Set<SmtStringVariable> charVariables = buildCharVariables();
			smtVariablesToDeclare.addAll(charVariables);
		}

		List<SmtConstantDeclaration> constantDeclarations = new LinkedList<SmtConstantDeclaration>();

		for (SmtVariable v1 : smtVariablesToDeclare) {
			String varName = v1.getName();
			if (v1 instanceof SmtIntVariable) {
				SmtConstantDeclaration constantDecl = SmtExprBuilder.mkIntConstantDeclaration(varName);
				constantDeclarations.add(constantDecl);
			} else if (v1 instanceof SmtRealVariable) {
				SmtConstantDeclaration constantDecl = SmtExprBuilder.mkRealConstantDeclaration(varName);
				constantDeclarations.add(constantDecl);
			} else if (v1 instanceof SmtStringVariable) {
				SmtConstantDeclaration constantDecl = SmtExprBuilder.mkStringConstantDeclaration(varName);
				constantDeclarations.add(constantDecl);
			} else {
				throw new RuntimeException("Unknown variable type " + v1.getClass().getCanonicalName());
			}
		}

		List<SmtFunctionDefinition> functionDefinitions = new LinkedList<SmtFunctionDefinition>();
		if (addCharToIntFunction) {
			String charToInt = buildCharToIntFunction();
			SmtFunctionDefinition newFunctionDef = new SmtFunctionDefinition(charToInt);
			functionDefinitions.add(newFunctionDef);
		}

		SmtCheckSatQuery smtCheckSatQuery = new SmtCheckSatQuery(constantDeclarations, functionDefinitions, assertions);

		return smtCheckSatQuery;

	}

	@Override
	public SolverResult solve(Collection<Constraint<?>> constraints) throws SolverTimeoutException, IOException,
			SolverParseException, SolverEmptyQueryException, SolverErrorException {

		SmtCheckSatQuery smtCheckSatQuery = buildSmtQuerty(constraints);

		if (smtCheckSatQuery.getConstantDeclarations().isEmpty()) {
			logger.debug("Z3-str2 input has no variables");
			throw new SolverEmptyQueryException("Z3-str2 input has no variables");
		}

		if (smtCheckSatQuery.getAssertions().isEmpty()) {
			Map<String, Object> emptySolution = new HashMap<String, Object>();
			SolverResult emptySAT = SolverResult.newSAT(emptySolution);
			return emptySAT;
		}
		
		Z3Str2QueryPrinter printer = new Z3Str2QueryPrinter();
		String smtQueryStr = printer.print(smtCheckSatQuery);

		logger.debug("Z3-str2 input:");
		logger.debug(smtQueryStr);

		int timeout = (int) Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS;

		File tempDir = createNewTmpDir();
		String z3TempFileName = tempDir.getAbsolutePath() + File.separatorChar + EVOSUITE_Z3_STR_FILENAME;

		if (Properties.Z3_STR2_PATH == null) {
			String errMsg = "Property Z3_STR_PATH should be setted in order to use the Z3StrSolver!";
			logger.error(errMsg);
			throw new IllegalStateException(errMsg);
		}

		try {
			FileIOUtils.writeFile(smtQueryStr, z3TempFileName);
			String z3Cmd = Properties.Z3_STR2_PATH + " -f " + z3TempFileName;
			ByteArrayOutputStream stdout = new ByteArrayOutputStream();
			launchNewProcess(z3Cmd, smtQueryStr, timeout, stdout);

			String z3str2ResultStr = stdout.toString("UTF-8");

			Z3Str2ResultParser parser = new Z3Str2ResultParser();
			Set<Variable<?>> variables = getVariables(constraints);
			Map<String, Object> initialValues = getConcreteValues(variables);
			SolverResult solverResult;
			if (addMissingVariables()) {
				solverResult = parser.parse(z3str2ResultStr, initialValues);
			} else {
				solverResult = parser.parse(z3str2ResultStr);
			}

			if (solverResult.isSAT()) {
				// check if solution is correct, otherwise return UNSAT
				boolean check = checkSAT(constraints, solverResult);
				if (!check) {
					logger.debug("Z3-str2 solution does not solve the constraint system!");
					SolverResult unsatResult = SolverResult.newUNSAT();
					return unsatResult;
				}
			}

			return solverResult;

		} catch (UnsupportedEncodingException e) {
			throw new EvosuiteError("UTF-8 should not cause this exception!");
		} finally {
			File tempFile = new File(z3TempFileName);
			if (tempFile.exists()) {
				tempFile.delete();
			}
		}
	}

	private final static int ASCII_TABLE_LENGTH = 90;

	private static Set<SmtStringVariable> buildCharVariables() {
		Set<SmtStringVariable> charVariables = new HashSet<SmtStringVariable>();

		for (int i = 0; i < ASCII_TABLE_LENGTH; i++) {
			char c = (char) i;
			String str = String.valueOf(c);
			String encodedStr = ExprToZ3Str2Visitor.encodeString(str);
			SmtStringVariable v = new SmtStringVariable(encodedStr);
			charVariables.add(v);
		}
		return charVariables;
	}

	private static String buildCharToIntFunction() {
		StringBuffer buff = new StringBuffer();
		buff.append(SmtOperation.Operator.CHAR_TO_INT + "((x!1 String)) Int");
		buff.append("\n");
		for (int i = 0; i < ASCII_TABLE_LENGTH; i++) {
			char c = (char) i;
			String str = String.valueOf(c);
			String encodedStr = ExprToZ3Str2Visitor.encodeString(str);
			if (i < ASCII_TABLE_LENGTH - 1) {
				String iteStr = String.format("(ite (= x!1 %s) %s", encodedStr, i);
				buff.append(iteStr);
				buff.append("\n");
			} else {
				buff.append(i);
			}
		}
		for (int i = 0; i < ASCII_TABLE_LENGTH - 1; i++) {
			buff.append(")");
		}
		buff.append("\n");
		return buff.toString();
	}

}
