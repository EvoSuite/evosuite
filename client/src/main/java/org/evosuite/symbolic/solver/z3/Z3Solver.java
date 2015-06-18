package org.evosuite.symbolic.solver.z3;

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
import org.evosuite.symbolic.solver.Solver;
import org.evosuite.symbolic.solver.smt.SmtExpr;
import org.evosuite.symbolic.solver.smt.SmtExprPrinter;
import org.evosuite.testcase.execution.EvosuiteError;
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
	public Map<String, Object> solve(Collection<Constraint<?>> constraints)
			throws ConstraintSolverTimeoutException {

		long timeout = Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS;

		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		for (Constraint<?> c : constraints) {
			Set<Variable<?>> c_variables = c.getVariables();
			variables.addAll(c_variables);
		}

		List<SmtExpr> assertions = new LinkedList<SmtExpr>();
		for (Constraint<?> c : constraints) {
			ConstraintToZ3Visitor v = new ConstraintToZ3Visitor();
			SmtExpr bool_expr = c.accept(v, null);
			if (bool_expr != null && bool_expr.isSymbolic()) {
				assertions.add(bool_expr);
			}
		}

		String smtQuery = buildSmtQuery(assertions, variables, timeout);

		if (smtQuery == null) {
			logger.debug("Empty SMT query to Z3");
			logger.debug("Returning NULL as solution");
			return null;
		}

		logger.debug("Z3 Query:");
		logger.debug(smtQuery);

		if (Properties.Z3_PATH == null) {
			String errMsg = "Property Z3_PATH should be setted in order to use the Z3 Solver!";
			logger.error(errMsg);
			throw new IllegalStateException(errMsg);
		}
		String z3Cmd = Properties.Z3_PATH + " -smt2 -in";

		ByteArrayOutputStream stdout = new ByteArrayOutputStream();

		try {
			launchNewProcess(z3Cmd, smtQuery.toString(), (int) timeout, stdout);

			String z3ResultStr = stdout.toString("UTF-8");
			if (z3ResultStr.startsWith("sat")) {
				logger.debug("Z3 outcome was SAT");

				// parse solution
				Map<String, Object> initialValues = getConcreteValues(variables);

				Z3ModelParser modelParser;
				if (this.addMissingVariables()) {
					modelParser = new Z3ModelParser(initialValues);
				} else {
					modelParser = new Z3ModelParser();
				}
				Map<String, Object> solution = modelParser.parse(z3ResultStr);

				boolean checkSmt = checkSolution(assertions, solution);
				if (!checkSmt) {
					throw new EvosuiteError(
							"The returned solution does not solve the SMT query!");
				}

				// check solution is correct
				boolean check = checkSolution(constraints, solution);
				if (!check) {
					logger.debug("Z3 solution does not solve the constraint system!");
					return null;
				}

				return solution;
			} else if (z3ResultStr.startsWith("unsat")) {
				logger.debug("Z3 outcome was UNSAT");
				return null;
			} else {
				logger.debug("Z3 output was " + z3ResultStr);
				throw new EvosuiteError(
						"Z3 output is unknown. We are unable to parse it to a proper solution!");
			}

		} catch (IOException e) {
			logger.error("IO Exception during launching of Z3 command");
			return null;

		}
	}

	private static String buildSmtQuery(Collection<SmtExpr> assertions,
			Set<Variable<?>> variables, long timeout) {

		if (assertions.isEmpty()) {
			logger.debug("Translation to Z3 model has no variables");
			return null;
		}

		logger.debug("Creating new Z3 Solver");
		logger.debug("Setting Z3 soft_timeout to " + timeout + " ms");

		StringBuffer smtQuery = new StringBuffer();
		smtQuery.append("(set-option :timeout " + timeout + ")");
		smtQuery.append("\n");

		for (Variable<?> v : variables) {
			String varName = v.getName();
			if (v instanceof IntegerVariable) {
				String intVar = Z3ExprBuilder.mkIntVariable(varName);
				smtQuery.append(intVar);
				smtQuery.append("\n");
			} else if (v instanceof RealVariable) {
				String realVar = Z3ExprBuilder.mkRealVariable(varName);
				smtQuery.append(realVar);
				smtQuery.append("\n");
			} else if (v instanceof StringVariable) {
				// ignore string variables
			} else {
				throw new RuntimeException("Unknown variable type "
						+ v.getClass().getCanonicalName());
			}
		}

		SmtExprPrinter printer = new SmtExprPrinter();
		for (SmtExpr formula : assertions) {
			String formulaStr = formula.accept(printer, null);
			smtQuery.append("(assert " + formulaStr + ")");
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

	private static int launchNewProcess(String z3Cmd, String smtQuery,
			int timeout, OutputStream outputStream) throws IOException {

		final Process process = Runtime.getRuntime().exec(z3Cmd);

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
