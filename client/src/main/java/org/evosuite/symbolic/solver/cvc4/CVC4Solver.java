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
import org.evosuite.symbolic.solver.SmtExprBuilder;
import org.evosuite.symbolic.solver.Solver;
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
import org.evosuite.testcase.execution.EvosuiteError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CVC4Solver extends Solver {

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

		if (Properties.CVC4_PATH == null) {
			String errMsg = "Property CVC4_PATH should be setted in order to use the CVC4 Solver!";
			logger.error(errMsg);
			throw new IllegalStateException(errMsg);
		}

		// CVC4 has very little support for non-linear arithemtics
		// In fact, it cannot even produce models for non-linear theories
		if (!reWriteNonLinearConstraints
				&& hasNonLinearConstraints(constraints)) {
			logger.debug("Skipping query due to (unsupported) non-linear constraints");
			return null;
		}

		long cvcTimeout = Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS * 10;

		long processTimeout = cvcTimeout * 2;

		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		for (Constraint<?> c : constraints) {
			Set<Variable<?>> c_variables = c.getVariables();
			variables.addAll(c_variables);
		}

		SmtCheckSatQuery smtQuery = buildSmtCheckSatQuery(constraints);

		if (smtQuery==null) {
			logger.debug("No variables found during the creation of the SMT quer. Returning NULL as solution");
			return null;
		}
		
		CVC4QueryPrinter printer = new CVC4QueryPrinter();
		String smtQueryStr = printer.print(smtQuery);

		if (smtQueryStr == null) {
			logger.debug("No variables found during constraint solving. Returning NULL as solution");
			return null;
		}

		logger.debug("CVC4 Query:");
		logger.debug(smtQueryStr);

		String cvc4Cmd = buildCVC4cmd(cvcTimeout);

		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		ByteArrayOutputStream stderr = new ByteArrayOutputStream();

		try {
			launchNewProcess(cvc4Cmd, smtQueryStr, (int) processTimeout,
					stdout, stderr);

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

				// check if the found solution is useful
				boolean check = checkSolution(constraints, solution);
				if (!check) {
					logger.debug("CVC4 solution does not solve the original constraint system. ");
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
				logger.debug("CVC4 output was the following " + cvc4ResultStr);
				throw new EvosuiteError(
						"An error (probably an invalid input) occurred while executing CVC4");
			} else {
				logger.debug("The following CVC4 output could not be parsed "
						+ cvc4ResultStr);
				throw new EvosuiteError(
						"CVC4 output is unknown. We are unable to parse it to a proper solution!");
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

	private static SmtCheckSatQuery buildSmtCheckSatQuery(
			Collection<Constraint<?>> constraints) {

		ConstraintToCVC4Visitor v = new ConstraintToCVC4Visitor();
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

		final boolean addCharToInt = funCollector.getOperators().contains(
				Operator.CHAR_TO_INT);
		if (addCharToInt) {
			String charToIntFunction = buildCharToIntFunction();
			SmtFunctionDefinition funcDefinition = new SmtFunctionDefinition(
					charToIntFunction);
			functionDefinitions.add(funcDefinition);
		}

		final boolean addIntToChar = funCollector.getOperators().contains(
				Operator.INT_TO_CHAR);
		if (addIntToChar) {
			String intToCharFunction = buildIntToCharFunction();
			SmtFunctionDefinition funcDefinition = new SmtFunctionDefinition(
					intToCharFunction);
			functionDefinitions.add(funcDefinition);
		}

		List<SmtFunctionDeclaration> functionDeclarations = new LinkedList<SmtFunctionDeclaration>();
		for (SmtVariable var : variables) {
			String varName = var.getName();
			if (var instanceof SmtIntVariable) {
				SmtFunctionDeclaration intVar = SmtExprBuilder
						.mkIntFunctionDeclaration(varName);
				functionDeclarations.add(intVar);

			} else if (var instanceof SmtRealVariable) {
				SmtFunctionDeclaration realVar = SmtExprBuilder
						.mkRealFunctionDeclaration(varName);
				functionDeclarations.add(realVar);

			} else if (var instanceof SmtStringVariable) {
				SmtFunctionDeclaration stringVar = SmtExprBuilder
						.mkStringFunctionDeclaration(varName);
				functionDeclarations.add(stringVar);
			} else {
				throw new RuntimeException("Unknown variable type "
						+ var.getClass().getCanonicalName());
			}
		}

		SmtCheckSatQuery smtQuery = new SmtCheckSatQuery(
				new LinkedList<SmtConstantDeclaration>(), functionDeclarations,
				functionDefinitions, smtAssertions);

		return smtQuery;

	}

	private static String buildCVC4cmd(long cvcTimeout) {
		String cmd = Properties.CVC4_PATH;
		cmd += "  --rewrite-divk"; // rewrite-divk rewrites division (or
									// modulus) by a constant value
		cmd += " --lang smt"; // query language is SMT-LIB
		cmd += " --tlimit=" + cvcTimeout; // set timeout to cvcTimeout
		return cmd;
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
