package org.evosuite.symbolic.solver.cvc4;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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

import org.evosuite.Properties;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.SmtLibExprBuilder;
import org.evosuite.symbolic.solver.Solver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CVC4Solver extends Solver {

	private static final class TimeoutTask extends TimerTask {
		private final Process process;

		private TimeoutTask(Process process) {
			this.process = process;
		}

		@Override
		public void run() {
			process.destroy();
		}
	}

	static Logger logger = LoggerFactory.getLogger(CVC4Solver.class);
	public static final String STR_LENGTH = "str.len";
	public static final String STR_AT = "str.at";
	public static final String STR_SUBSTR = "str.substr";
	public static final String STR_PREFIXOF = "str.prefixof";
	public static final String STR_SUFFIXOF = "str.suffixof";
	public static final String STR_REPLACE = "str.replace";
	public static final String STR_INDEXOF = "str.indexof";
	public static final String STR_CONTAINS = "str.contains";

	@Override
	public Map<String, Object> solve(Collection<Constraint<?>> constraints)
			throws ConstraintSolverTimeoutException {

		if (hasNonLinearConstraints(constraints)) {
			return null;
		}

		long timeout = Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS * 10;

		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		for (Constraint<?> c : constraints) {
			Set<Variable<?>> c_variables = c.getVariables();
			variables.addAll(c_variables);
		}

		String smtQuery = buildSmtQuery(cvc4SolverLogic, constraints,
				variables, timeout);

		logger.debug("CVC4 Query:");
		logger.debug(smtQuery);

		if (Properties.CVC4_PATH == null) {
			String errMsg = "Property CVC4_PATH should be setted in order to use the CVC4 Solver!";
			logger.error(errMsg);
			throw new IllegalStateException(errMsg);
		}
		String cvc4Cmd = Properties.CVC4_PATH + "  --lang smt --strings-exp ";

		ByteArrayOutputStream stdout = new ByteArrayOutputStream();

		try {
			launchNewProcess(cvc4Cmd, smtQuery, (int) timeout, stdout);

			String cvc4ResultStr = stdout.toString("UTF-8");
			if (cvc4ResultStr.startsWith("sat")) {
				logger.debug("CVC4 outcome was SAT");

				// parse solution
				Map<String, Object> initialValues = getConcreteValues(variables);
				CVC4ModelParser modelParser = new CVC4ModelParser(initialValues);
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

	private static boolean hasNonLinearConstraints(
			Collection<Constraint<?>> constraints) {
		NonLinearConstraintVisitor v = new NonLinearConstraintVisitor();
		for (Constraint<?> constraint : constraints) {
			Boolean ret_val = constraint.accept(v, null);
			if (ret_val) {
				return true;
			}
		}
		return false;
	}

	private enum CVC4Logic {
		QF_S, QF_SLIRA
	};

	private CVC4Logic cvc4SolverLogic = CVC4Logic.QF_SLIRA;

	private static String toString(CVC4Logic logic) {
		switch (logic) {
		case QF_S:
			return "QF_S";
		case QF_SLIRA:
			return "QF_SLIRA";
		default:
			throw new IllegalArgumentException("Unknown CVC4 Logic!" + logic);
		}
	}

	private static String buildSmtQuery(CVC4Logic cvc4SolverLogic,
			Collection<Constraint<?>> constraints, Set<Variable<?>> variables,
			long timeout) {

		StringBuffer smtQuery = new StringBuffer();
		//		smtQuery.append("(set-logic QF_S)");
		//		smtQuery.append("\n");
		smtQuery.append("(set-logic " + toString(cvc4SolverLogic) + ")");

		smtQuery.append("\n");
		smtQuery.append("(set-option :produce-models true)");

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

		ConstraintToCVC4Visitor v = new ConstraintToCVC4Visitor();
		List<String> cvc4StrAssertions = new LinkedList<String>();
		for (Constraint<?> c : constraints) {
			String constraintStr = c.accept(v, null);
			if (constraintStr != null) {
				String cvc4Assert = SmtLibExprBuilder.mkAssert(constraintStr);
				cvc4StrAssertions.add(cvc4Assert);
			}
		}

		for (String cvc4assert : cvc4StrAssertions) {
			smtQuery.append(cvc4assert);
			smtQuery.append("\n");
		}

		smtQuery.append("(check-sat)");
		smtQuery.append("\n");

		smtQuery.append("(get-model)");
		smtQuery.append("\n");

		smtQuery.append("(exit)");
		smtQuery.append("\n");
		return smtQuery.toString();

	}

	private static int launchNewProcess(String cvc4Cmd, String smtQuery,
			int timeout, OutputStream outputStream) throws IOException {

		final Process process = Runtime.getRuntime().exec(cvc4Cmd);

		InputStream stdout = process.getInputStream();
		InputStream stderr = process.getErrorStream();
		OutputStream stdin = process.getOutputStream();

		stdin.write(smtQuery.getBytes());
		stdin.flush();
		stdin.close();

		logger.debug("Process output:");

		Timer t = new Timer();
		t.schedule(new TimeoutTask(process), timeout);

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
