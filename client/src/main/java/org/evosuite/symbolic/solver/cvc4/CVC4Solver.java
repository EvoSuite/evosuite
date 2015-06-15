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
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.SmtStringExprBuilder;
import org.evosuite.symbolic.solver.Solver;
import org.evosuite.symbolic.solver.smt.SmtExpr;
import org.evosuite.symbolic.solver.smt.SmtExprPrinter;
import org.evosuite.symbolic.solver.smt.SmtOperatorCollector;
import org.evosuite.symbolic.solver.smt.SmtIntVariable;
import org.evosuite.symbolic.solver.smt.SmtOperation;
import org.evosuite.symbolic.solver.smt.SmtRealVariable;
import org.evosuite.symbolic.solver.smt.SmtStringVariable;
import org.evosuite.symbolic.solver.smt.SmtVariableCollector;
import org.evosuite.symbolic.solver.smt.SmtVariable;
import org.evosuite.symbolic.solver.smt.SmtOperation.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CVC4Solver extends Solver {

	private static final class TimeoutTask extends TimerTask {
		private final Process process;
		private final long timeout;

		private TimeoutTask(Process process, long timeout) {
			this.process = process;
			this.timeout = timeout;
		}

		@Override
		public void run() {
			logger.debug("CVC4 timeout was reached after " + timeout
					+ " milliseconds ");
			process.destroy();
		}
	}

	private static final String CVC4_LOGIC = "QF_SLIRA";

	static Logger logger = LoggerFactory.getLogger(CVC4Solver.class);

	public CVC4Solver(boolean addMissingValues) {
		super(addMissingValues);
	}

	public CVC4Solver() {
		super();
	}
	
	@Override
	public Map<String, Object> solve(Collection<Constraint<?>> constraints)
			throws ConstraintSolverTimeoutException {

		if (hasNonLinearConstraints(constraints)) {
			return null;
		}

		long cvcTimeout = Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS * 10;

		long processTimeout = cvcTimeout * 2;

		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		for (Constraint<?> c : constraints) {
			Set<Variable<?>> c_variables = c.getVariables();
			variables.addAll(c_variables);
		}

		String smtQuery = buildSmtQuery(constraints);

		if (smtQuery == null) {
			logger.debug("No variables found during constraint solving. Returning NULL as solution");
			return null;
		}

		logger.debug("CVC4 Query:");
		logger.debug(smtQuery);

		if (Properties.CVC4_PATH == null) {
			String errMsg = "Property CVC4_PATH should be setted in order to use the CVC4 Solver!";
			logger.error(errMsg);
			throw new IllegalStateException(errMsg);
		}
		String cvc4Cmd = Properties.CVC4_PATH + "  --lang smt " + " --tlimit="
				+ cvcTimeout;

		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		ByteArrayOutputStream stderr = new ByteArrayOutputStream();

		try {
			launchNewProcess(cvc4Cmd, smtQuery, (int) processTimeout, stdout,
					stderr);

			String cvc4ResultStr = stdout.toString("UTF-8");
			String errorStr = stderr.toString("UTF-8");

			if (errorStr.contains("error")) {
				logger.error("An error occurred while executing CVC4!");
				return null;
			}

			if (cvc4ResultStr.startsWith("sat")) {
				logger.debug("CVC4 outcome was SAT");

				// parse solution
				Map<String, Object> initialValues = getConcreteValues(variables);
				CVC4ModelParser modelParser;
				if (addMissingVariables()) {
				modelParser = new CVC4ModelParser(initialValues);
				} else {
					modelParser = new CVC4ModelParser();
				}
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
			} else if (cvc4ResultStr.startsWith("unknown")) {
				logger.debug("CVC4 outcome was UNKNOWN (probably due to timeout)");
				return null;
			} else if (cvc4ResultStr.startsWith("(error")) {
				logger.error("An error (probably parsing error) occurred while executing CVC4");
				return null;
			} else {
				logger.error("CVC4 output is unknown. We are unable to parse it to a proper solution!");
				return null;
			}

		} catch (IOException e) {
			if (e.getMessage().contains("Permission denied")) {
				logger.error("No permissions for running CVC4 binary");
			} else {
				logger.error("IO Exception during launching of CVC4 command");
			}
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

	private static String buildSmtQuery(Collection<Constraint<?>> constraints) {

		ConstraintToCVC4Visitor v = new ConstraintToCVC4Visitor();
		List<SmtExpr> smtExpressions = new LinkedList<SmtExpr>();
		for (Constraint<?> c : constraints) {
			SmtExpr smtExpr = c.accept(v, null);
			if (smtExpr != null) {
				smtExpressions.add(smtExpr);
			}
		}

		SmtExprPrinter printer = new SmtExprPrinter();
		List<String> cvc4StrAssertions = new LinkedList<String>();
		for (SmtExpr smtExpr : smtExpressions) {
			String smtExprStr = smtExpr.accept(printer, null);
			String assertionStr = SmtStringExprBuilder.mkAssert(smtExprStr);
			cvc4StrAssertions.add(assertionStr);
		}

		SmtVariableCollector varCollector = new SmtVariableCollector();
		for (SmtExpr smtExpr : smtExpressions) {
			smtExpr.accept(varCollector, null);
		}
		Set<SmtVariable> variables = varCollector.getSmtVariables();

		if (variables.isEmpty()) {
			return null; // no variables, constraint system is trivial
		}

		SmtOperatorCollector funCollector = new SmtOperatorCollector();
		for (SmtExpr smtExpr : smtExpressions) {
			smtExpr.accept(funCollector, null);
		}

		boolean addCharToInt = funCollector.getOperators().contains(
				Operator.CHAR_TO_INT);
		boolean addIntToChar = funCollector.getOperators().contains(
				Operator.INT_TO_CHAR);

		return createSmtString(cvc4StrAssertions, variables, addCharToInt,
				addIntToChar);

	}

	private static String createSmtString(List<String> cvc4StrAssertions,
			Set<SmtVariable> variables, boolean addCharToInt,
			boolean addIntToChar) {
		StringBuffer smtQuery = new StringBuffer();
		smtQuery.append("\n");
		smtQuery.append("(set-logic " + CVC4_LOGIC + ")");
		smtQuery.append("\n");
		smtQuery.append("(set-option :produce-models true)");
		smtQuery.append("\n");
		smtQuery.append("(set-option :strings-exp true)");
		smtQuery.append("\n");

		if (addCharToInt) {
			String charToIntFunction = buildCharToIntFunction();
			smtQuery.append(charToIntFunction);
			smtQuery.append("\n");
		}

		if (addIntToChar) {
			String intToCharFunction = buildIntToCharFunction();
			smtQuery.append(intToCharFunction);
			smtQuery.append("\n");
		}
		for (SmtVariable var : variables) {
			String varName = var.getName();
			if (var instanceof SmtIntVariable) {
				String intVar = SmtStringExprBuilder.mkIntFunction(varName);
				smtQuery.append(intVar);
				smtQuery.append("\n");

			} else if (var instanceof SmtRealVariable) {
				String realVar = SmtStringExprBuilder.mkRealFunction(varName);
				smtQuery.append(realVar);
				smtQuery.append("\n");

			} else if (var instanceof SmtStringVariable) {
				String stringVar = SmtStringExprBuilder
						.mkStringFunction(varName);
				smtQuery.append(stringVar);
				smtQuery.append("\n");
			} else {
				throw new RuntimeException("Unknown variable type "
						+ var.getClass().getCanonicalName());
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
			int timeout, OutputStream outputStream, OutputStream errorStream)
			throws IOException {

		final Process process = Runtime.getRuntime().exec(cvc4Cmd);

		InputStream stdout = process.getInputStream();
		InputStream stderr = process.getErrorStream();
		OutputStream stdin = process.getOutputStream();

		stdin.write(smtQuery.getBytes());
		stdin.flush();
		stdin.close();

		logger.debug("Process output:");

		Timer t = new Timer();
		t.schedule(new TimeoutTask(process, timeout), timeout);

		do {
			readInputStream(stdout, outputStream);
			readInputStream(stderr, errorStream);
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

	// private final static int ASCII_TABLE_LENGTH = 256;
	private final static int ASCII_TABLE_LENGTH = 256;

	private static String buildIntToCharFunction() {
		StringBuffer buff = new StringBuffer();
		buff.append("(define-fun " + SmtOperation.Operator.INT_TO_CHAR
				+ "((!x Int)) String");
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
				String iteStr = String.format("(ite (= !x %s) \"%s\"", i,
						escapedHexStr);
				buff.append(iteStr);
				buff.append("\n");
			} else {
				buff.append(String.format("\"%s\"", escapedHexStr));
			}
		}
		for (int i = 0; i < ASCII_TABLE_LENGTH - 1; i++) {
			buff.append(")");
		}
		buff.append(")");
		buff.append("\n");
		return buff.toString();
	}

	private static String buildCharToIntFunction() {
		StringBuffer buff = new StringBuffer();
		buff.append("(define-fun " + SmtOperation.Operator.CHAR_TO_INT
				+ "((!x String)) Int");
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
				String iteStr = String.format("(ite (= !x \"%s\") %s",
						escapedHexStr, i);
				buff.append(iteStr);
				buff.append("\n");
			} else {
				buff.append(i);
			}
		}
		for (int i = 0; i < ASCII_TABLE_LENGTH - 1; i++) {
			buff.append(")");
		}
		buff.append(")");
		buff.append("\n");
		return buff.toString();
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
