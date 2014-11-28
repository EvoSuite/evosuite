package org.evosuite.symbolic.solver.cvc4;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.evosuite.Properties;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.SmtLibExprBuilder;
import org.evosuite.symbolic.solver.Solver;
import org.evosuite.symbolic.solver.z3.Z3ExprBuilder;
import org.evosuite.symbolic.solver.z3str.Z3StrExprBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CVC4Solver extends Solver {

	static Logger logger = LoggerFactory.getLogger(CVC4Solver.class);
	public static final String STR_LENGTH = "str.len";
	public static final String STR_AT = "str.at";
	public static final String STR_SUBSTR = "str.substr";

	@Override
	public Map<String, Object> solve(Collection<Constraint<?>> constraints)
			throws ConstraintSolverTimeoutException {

		long timeout = Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS;

		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		for (Constraint<?> c : constraints) {
			Set<Variable<?>> c_variables = c.getVariables();
			variables.addAll(c_variables);
		}

		String smtQuery = buildSmtQuery(constraints, variables, timeout);

		logger.debug("CVC4 Query:");
		logger.debug(smtQuery);

		if (Properties.CVC4_PATH == null) {
			logger.error("Property CVC4_PATH should be setted in order to use the CVC4 Solver!");
			return null;
		}
		String cvc4Cmd = Properties.CVC4_PATH + "  --lang smt --strings-exp ";

		ByteArrayOutputStream stdout = new ByteArrayOutputStream();

		try {
			launchNewProcess(cvc4Cmd, smtQuery.toString(), (int) timeout,
					stdout);

			String cvc4ResultStr = stdout.toString("UTF-8");
			if (cvc4ResultStr.startsWith("sat")) {
				logger.debug("CVC4 outcome was SAT");

				// parse solution
				Map<String, Object> initialValues = getConcreteValues(variables);
				CVCModelParser modelParser = new CVCModelParser(initialValues);
				Map<String, Object> solution = modelParser.parse(cvc4ResultStr);

				// check solution is correct
				boolean check = checkSolution(constraints, solution);
				if (!check) {
					logger.warn("CVC4 solution does not solve the constraint system!");
					return null;
				}

				return solution;
			} else if (cvc4ResultStr.startsWith("unsat")) {
				logger.debug("CVC4 outcome was UNSAT");
				return null;
			} else {
				logger.error("CVC4 output is unknown. We are unable to parse it to a proper solution!");
				return null;
			}

		} catch (IOException e) {
			logger.error("IO Exception during launching of CVC4 command");
			return null;

		}

	}

	private String buildSmtQuery(Collection<Constraint<?>> constraints,
			Set<Variable<?>> variables, long timeout) {

		StringBuffer smtQuery = new StringBuffer();
		smtQuery.append("(set-logic QF_S)");
		smtQuery.append("\n");

		for (Variable<?> v : variables) {
			String varName = v.getName();
			if (v instanceof IntegerVariable) {
				String intVar = SmtLibExprBuilder.mkIntFunction(varName);
				smtQuery.append(intVar);
				smtQuery.append("\n");
			} else if (v instanceof RealVariable) {
				String realVar = SmtLibExprBuilder.mkRealFunction(varName);
				smtQuery.append(realVar);
				smtQuery.append("\n");
			} else if (v instanceof StringVariable) {
				String stringVar = SmtLibExprBuilder.mkStringFunction(varName);
				smtQuery.append(stringVar);
				smtQuery.append("\n");
			} else {
				throw new RuntimeException("Unknown variable type "
						+ v.getClass().getCanonicalName());
			}
		}

		ConstraintToZ3StrVisitor v = new ConstraintToZ3StrVisitor();
		List<String> z3StrAssertions = new LinkedList<String>();
		for (Constraint<?> c : constraints) {
			String constraintStr = c.accept(v, null);
			if (constraintStr != null) {
				String z3Assert = Z3StrExprBuilder.mkAssert(constraintStr);
				z3StrAssertions.add(z3Assert);
			}
		}

		smtQuery.append("(check-sat)");
		smtQuery.append("\n");

		smtQuery.append("(get-model)");
		smtQuery.append("\n");

		smtQuery.append("(exit)");
		smtQuery.append("\n");
		return smtQuery.toString();

	}

	private static int launchNewProcess(String z3StrCmd, String smtQuery,
			int timeout, OutputStream outputStream) throws IOException {

		final Process process = Runtime.getRuntime().exec(z3StrCmd);

		InputStream stdout = process.getInputStream();
		InputStream stderr = process.getErrorStream();

		logger.debug("Process output:");

		Timer t = new Timer();
		t.schedule(new TimerTask() {

			@Override
			public void run() {
				process.destroy();
			}
		}, timeout);

		do {
			readInputStream(stdout, outputStream);
			readInputStream(stderr, null);
		} while (!isFinished(process));

		int exitValue = process.exitValue();
		return exitValue;
	}

	private static void readInputStream(InputStream in, OutputStream out)
			throws IOException {
		InputStreamReader is = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(is);
		String read = br.readLine();
		while (read != null) {
			logger.debug(read);
			if (out != null) {
				byte[] bytes = (read + "\n").getBytes();
				out.write(bytes);
			}
			read = br.readLine();
		}
	}

	private static boolean isFinished(Process process) {
		try {
			process.exitValue();
			return true;
		} catch (IllegalThreadStateException ex) {
			return false;
		}
	}

}
