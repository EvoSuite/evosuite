package org.evosuite.symbolic.z3str;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
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
import org.evosuite.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Z3StrSolver extends Solver {

	static Logger logger = LoggerFactory.getLogger(Z3StrSolver.class);

	@Override
	public Map<String, Object> solve(Collection<Constraint<?>> constraints)
			throws ConstraintSolverTimeoutException {

		StringBuffer buff = new StringBuffer();

		Set<Variable<?>> variables = getVariables(constraints);

		for (Variable<?> v : variables) {
			String varName = v.getName();
			if (v instanceof IntegerVariable) {
				String intVar = Z3ExprBuilder.mkIntVariable(varName);
				buff.append(intVar);
				buff.append("\n");
			} else if (v instanceof RealVariable) {
				String realVar = Z3ExprBuilder.mkRealVariable(varName);
				buff.append(realVar);
				buff.append("\n");
			} else if (v instanceof StringVariable) {
				String stringVar = Z3ExprBuilder.mkStringVariable(varName);
				buff.append(stringVar);
				buff.append("\n");
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
				String z3Assert = Z3ExprBuilder.mkAssert(constraintStr);
				z3StrAssertions.add(z3Assert);
			}
		}

		Set<String> stringConstants = v.getStringConstants();
		for (String string : stringConstants) {
			String encodedStringConstant = Z3ExprBuilder.encodeString(string);
			String constDecl = Z3ExprBuilder
					.mkStringVariable(encodedStringConstant);
			buff.append(constDecl);
			buff.append("\n");
		}

		for (String z3StrAssertion : z3StrAssertions) {
			buff.append(z3StrAssertion);
			buff.append("\n");
		}

		buff.append("(check-sat)");
		buff.append("\n");
		// buff.append("(get-model)");
		// buff.append("\n");
		// buff.append("(exit)");
		// buff.append("\n");

		System.out.println("Z3 input:");
		String smtQuery = buff.toString();
		System.out.println(smtQuery);

		int timeout = (int) Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS;

		String fileName = "/home/galeotti/z3-str/evosuite";
		Utils.writeFile(smtQuery, fileName);

		try {
			ByteArrayOutputStream stdout = new ByteArrayOutputStream();
			launchNewProcess("/home/galeotti/z3-str/str -f " + fileName,
					smtQuery, timeout, stdout);

			String z3ResultStr = stdout.toString("UTF-8");
			Z3StrModelParser parser = new Z3StrModelParser();
			Map<String, Object> initialValues = getConcreteValues(variables);

			if (z3ResultStr.contains("unknown sort")
					|| z3ResultStr.contains("unknown constant")
					|| z3ResultStr.contains("invalid expression")
					|| z3ResultStr.contains("unexpected input")) {
				return null;
			}

			Map<String, Object> solution = parser.parse(z3ResultStr,
					initialValues);

			if (solution != null && checkSolution(constraints, solution))
				return solution;
			else
				return null;

		} catch (UnsupportedEncodingException e) {
			logger.error("UTF-8 should not cause this exception!");
			return null;
		} catch (IOException e) {
			logger.error("IO exception during Z3 invocation!");
			return null;
		}
	}

	private static int launchNewProcess(String z3Cmd, String smtQuery,
			int timeout, OutputStream outputStream) throws IOException {

		final Process process = Runtime.getRuntime().exec(z3Cmd);

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
