/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.solver.z3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.SmtExprBuilder;
import org.evosuite.symbolic.solver.Solver;
import org.evosuite.symbolic.solver.SolverEmptyQueryException;
import org.evosuite.symbolic.solver.SolverErrorException;
import org.evosuite.symbolic.solver.SolverParseException;
import org.evosuite.symbolic.solver.SolverResult;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.smt.SmtAssertion;
import org.evosuite.symbolic.solver.smt.SmtCheckSatQuery;
import org.evosuite.symbolic.solver.smt.SmtConstantDeclaration;
import org.evosuite.symbolic.solver.smt.SmtExpr;
import org.evosuite.utils.ProcessLauncher;
import org.evosuite.utils.ProcessTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Z3Solver extends Solver {

	public Z3Solver() {
		super();
	}

	public Z3Solver(boolean addMissingVariables) {
		super(addMissingVariables);
	}

	static Logger logger = LoggerFactory.getLogger(Z3Solver.class);

	@Override
	public SolverResult solve(Collection<Constraint<?>> constraints) throws SolverTimeoutException, IOException,
			SolverParseException, SolverEmptyQueryException, SolverErrorException {

		long hard_timeout = Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS;

		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		for (Constraint<?> c : constraints) {
			Set<Variable<?>> c_variables = c.getVariables();
			variables.addAll(c_variables);
		}

		SmtCheckSatQuery smtCheckSatQuery = buildSmtQuery(constraints, variables);

		if (smtCheckSatQuery.getConstantDeclarations().isEmpty()) {
			logger.debug("Z3 SMT query has no variables");
			throw new SolverEmptyQueryException("Z3 SMT query has no variables");
		}

		Z3QueryPrinter printer = new Z3QueryPrinter();
		String smtQueryStr = printer.print(smtCheckSatQuery, hard_timeout);

		logger.debug("Z3 Query:");
		logger.debug(smtQueryStr);

		if (Properties.Z3_PATH == null) {
			String errMsg = "Property Z3_PATH should be setted in order to use the Z3 Solver!";
			logger.error(errMsg);
			throw new IllegalStateException(errMsg);
		}
		String z3Cmd = Properties.Z3_PATH + " -smt2 -in";

		ByteArrayOutputStream stdout = new ByteArrayOutputStream();

		launchNewProcess(z3Cmd, smtQueryStr, (int) hard_timeout, stdout);

		String z3ResultStr = stdout.toString("UTF-8");

		Map<String, Object> initialValues = getConcreteValues(variables);
		Z3ResultParser resultParser;
		if (this.addMissingVariables()) {
			resultParser = new Z3ResultParser(initialValues);
		} else {
			resultParser = new Z3ResultParser();
		}

		SolverResult result = resultParser.parseResult(z3ResultStr);

		return result;
	}

	private static SmtCheckSatQuery buildSmtQuery(Collection<Constraint<?>> constraints, Set<Variable<?>> variables) {
		List<SmtConstantDeclaration> constantDeclarations = new LinkedList<SmtConstantDeclaration>();
		for (Variable<?> v : variables) {
			String varName = v.getName();
			if (v instanceof IntegerVariable) {
				SmtConstantDeclaration intVar = SmtExprBuilder.mkIntConstantDeclaration(varName);
				constantDeclarations.add(intVar);
			} else if (v instanceof RealVariable) {
				SmtConstantDeclaration realVar = SmtExprBuilder.mkRealConstantDeclaration(varName);
				constantDeclarations.add(realVar);

			} else if (v instanceof StringVariable) {
				// ignore string variables
			} else {
				throw new RuntimeException("Unknown variable type " + v.getClass().getCanonicalName());
			}
		}

		List<SmtAssertion> assertions = new LinkedList<SmtAssertion>();
		for (Constraint<?> c : constraints) {
			ConstraintToZ3Visitor v = new ConstraintToZ3Visitor();
			SmtExpr bool_expr = c.accept(v, null);
			if (bool_expr != null && bool_expr.isSymbolic()) {
				SmtAssertion newAssertion = new SmtAssertion(bool_expr);
				assertions.add(newAssertion);
			}
		}

		SmtCheckSatQuery smtCheckSatQuery = new SmtCheckSatQuery(constantDeclarations, assertions);
		return smtCheckSatQuery;
	}

	private static int launchNewProcess(String z3Cmd, String smtQuery, int hard_timeout, OutputStream outputStream)
			throws IOException {

		ByteArrayInputStream input = new ByteArrayInputStream(smtQuery.getBytes());

		ProcessLauncher launcher = new ProcessLauncher(outputStream, input);

		long z3_start_time_millis = System.currentTimeMillis();
		try {
			int exit_code = launcher.launchNewProcess(z3Cmd, hard_timeout);
			if (exit_code == 0) {
				logger.debug("Z3 execution finished normally");
			} else {
				logger.debug("Z3 execution finished abnormally with exit code {}", exit_code);
			}
			return exit_code;
		} catch (IOException ex) {
			logger.debug("An IO Exception occurred while executing Z3");
			return -1;
		} catch (ProcessTimeoutException ex) {
			logger.debug("Z3 execution stopped due to solver timeout");
			return -1;
		} finally {
			long z3_end_time_millis = System.currentTimeMillis();
			long z3_duration_secs = (z3_end_time_millis - z3_start_time_millis) / 1000;
			logger.debug("Z3 execution time was {}s", z3_duration_secs);
		}
	}

}
