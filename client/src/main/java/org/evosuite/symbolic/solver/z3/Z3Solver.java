package org.evosuite.symbolic.solver.z3;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Z3Solver extends Solver {

	public static final String STR_LENGTH = "str_length";
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

		String smtQuery = buildSmtQuery(constraints, variables, timeout);

		logger.debug("Z3 Query:");
		logger.debug(smtQuery);

		if (Properties.Z3_PATH == null) {
			logger.error("Property Z3_PATH should be setted in order to use the Z3 Solver!");
			return null;
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
				Z3ModelParser modelParser = new Z3ModelParser(initialValues);
				Map<String, Object> solution = modelParser.parse(z3ResultStr);

				// check solution is correct
				boolean check = checkSolution(constraints, solution);
				if (!check) {
					logger.warn("Z3 solution does not solve the constraint system!");
					return null;
				}

				return solution;
			} else if (z3ResultStr.startsWith("unsat")) {
				logger.debug("Z3 outcome was UNSAT");
				return null;
			} else {
				logger.error("Z3 output is unknown. We are unable to parse it to a proper solution!");
				return null;
			}

		} catch (IOException e) {
			logger.error("IO Exception during launching of Z3 command");
			return null;

		}
	}

	private static String buildSmtQuery(Collection<Constraint<?>> constraints,
			Set<Variable<?>> variables, long timeout) {
		Map<String, String> stringConstants = new HashMap<String, String>();

		List<String> assertions = new LinkedList<String>();
		for (Constraint<?> c : constraints) {
			ConstraintToZ3Visitor v = new ConstraintToZ3Visitor(stringConstants);
			String bool_expr = c.accept(v, null);
			if (bool_expr != null) {
				assertions.add(bool_expr);
			}
		}

		// add string axioms
		for (String string_constant : stringConstants.keySet()) {
			String arrayExpr = stringConstants.get(string_constant);

			String strLen = Z3ExprBuilder.mkApp(STR_LENGTH, arrayExpr);
			String str_len_axiom = Z3ExprBuilder.mkEq(strLen,
					Z3ExprBuilder.mkIntegerConstant(string_constant.length()));

			assertions.add(str_len_axiom);

			for (int i = 0; i < string_constant.length(); i++) {
				int charV = (int) string_constant.charAt(i);
				String string_i = Z3ExprBuilder.mkEq(
						Z3ExprBuilder.mkSelect(arrayExpr,
								Z3ExprBuilder.mkIntegerConstant(i)),
						Z3ExprBuilder.mkIntegerConstant(charV));
				assertions.add(string_i);
			}
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
				String stringVar = Z3ExprBuilder.mkStringVariable(varName);
				smtQuery.append(stringVar);
				smtQuery.append("\n");
			} else {
				throw new RuntimeException("Unknown variable type "
						+ v.getClass().getCanonicalName());
			}
		}

		for (String string_constant : stringConstants.keySet()) {
			String arrayExpr = stringConstants.get(string_constant);
			smtQuery.append("(declare-const " + arrayExpr
					+ " (Array (Int Int) Int))");
			smtQuery.append("\n");
		}

		Z3Function strLength = createStringLength();
		smtQuery.append(strLength.getFunctionDeclaration());
		smtQuery.append("\n");

		for (String axiom : strLength.getAxioms()) {
			smtQuery.append("(assert " + axiom + ")");
			smtQuery.append("\n");
		}

		for (String formula : assertions) {
			smtQuery.append("(assert " + formula + ")");
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

	private static Z3Function createStringLength() {
		//function declaration
		String arraySort = "(Array (Int) (Int))";
		String arrayLengthSort = "Int";
		String str_length = Z3ExprBuilder.mkFuncDecl(STR_LENGTH, arraySort,
				arrayLengthSort);

		//axioms
		String s = "s";// arraySort
		String length_of_s = Z3ExprBuilder.mkApp(STR_LENGTH, s);
		String body = Z3ExprBuilder.mkGe(length_of_s,
				Z3ExprBuilder.mkIntegerConstant(0));
		String axiom = Z3ExprBuilder.mkForall(new String[] { s },
				new String[] { arraySort }, body);

		Z3Function z3Function = new Z3Function(str_length);
		z3Function.addAxiom(axiom);
		return z3Function;
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

	private static class Z3Function {
		public Z3Function(String fd) {
			this.functionDeclaration = fd;
		}

		public List<String> getAxioms() {
			return axioms;
		}

		public String getFunctionDeclaration() {
			return functionDeclaration;
		}

		public void addAxiom(String axiom) {
			axioms.add(axiom);
		}

		private final String functionDeclaration;
		private final List<String> axioms = new LinkedList<String>();
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
