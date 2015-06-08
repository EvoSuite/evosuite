package org.evosuite.symbolic.solver.z3str2;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.SmtExprBuilder;
import org.evosuite.symbolic.solver.Solver;
import org.evosuite.symbolic.solver.smt.SmtConstant;
import org.evosuite.symbolic.solver.smt.SmtConstantCollector;
import org.evosuite.symbolic.solver.smt.SmtExpr;
import org.evosuite.symbolic.solver.smt.SmtExprPrinter;
import org.evosuite.symbolic.solver.smt.SmtIntVariable;
import org.evosuite.symbolic.solver.smt.SmtOperation;
import org.evosuite.symbolic.solver.smt.SmtOperation.Operator;
import org.evosuite.symbolic.solver.smt.SmtOperatorCollector;
import org.evosuite.symbolic.solver.smt.SmtRealVariable;
import org.evosuite.symbolic.solver.smt.SmtStringConstant;
import org.evosuite.symbolic.solver.smt.SmtStringVariable;
import org.evosuite.symbolic.solver.smt.SmtVariableCollector;
import org.evosuite.symbolic.solver.smt.SmtVariable;
import org.evosuite.symbolic.solver.z3.Z3Solver;
import org.evosuite.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Z3Str2Solver extends Solver {

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

	private static final String EVOSUITE_Z3_STR_FILENAME = "evosuite.z3";

	static Logger logger = LoggerFactory.getLogger(Z3Solver.class);

	private static int dirCounter = 0;

	private static File createNewTmpDir() {
		File dir = null;
		String dirName = FileUtils.getTempDirectoryPath() + File.separator
				+ "EvoSuiteZ3Str_" + (dirCounter++) + "_"
				+ System.currentTimeMillis();

		//first create a tmp folder
		dir = new File(dirName);
		if (!dir.mkdirs()) {
			logger.error("Cannot create tmp dir: " + dirName);
			return null;
		}

		if (!dir.exists()) {
			logger.error("Weird behavior: we created folder, but Java cannot determine if it exists? Folder: "
					+ dirName);
			return null;
		}

		return dir;
	}

	@Override
	public Map<String, Object> solve(Collection<Constraint<?>> constraints)
			throws ConstraintSolverTimeoutException {

		Set<Variable<?>> variables = getVariables(constraints);

		String smtQuery = buildSmtQuery(constraints);

		if (smtQuery == null) {
			logger.warn("No variables found during constraint solving. Returning NULL as solution");
			return null;
		}

		System.out.println("Z3 input:");
		System.out.println(smtQuery);

		int timeout = (int) Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS;

		File tempDir = createNewTmpDir();
		String z3TempFileName = tempDir.getAbsolutePath() + File.separatorChar
				+ EVOSUITE_Z3_STR_FILENAME;

		if (Properties.Z3_STR2_PATH == null) {
			String errMsg = "Property Z3_STR_PATH should be setted in order to use the Z3StrSolver!";
			logger.error(errMsg);
			throw new IllegalStateException(errMsg);
		}

		try {
			Utils.writeFile(smtQuery, z3TempFileName);
			String z3Cmd = Properties.Z3_STR2_PATH + " -f " + z3TempFileName;
			ByteArrayOutputStream stdout = new ByteArrayOutputStream();
			launchNewProcess(z3Cmd, smtQuery, timeout, stdout);

			String z3ResultStr = stdout.toString("UTF-8");
			Z3Str2ModelParser parser = new Z3Str2ModelParser();
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
		} finally {
			File tempFile = new File(z3TempFileName);
			if (tempFile.exists()) {
				tempFile.delete();
			}
		}
	}

	private static String mkAssert(String constraintStr) {
		return "(assert " + constraintStr + ")";
	}

	private static String declareStringConst(String varName) {
		return "(declare-const " + varName + " String)";
	}

	private static String declareRealConst(String varName) {
		return "(declare-const " + varName + " Real)";
	}

	private static String declareIntConst(String varName) {
		return "(declare-const " + varName + " Int)";
	}

	private static String buildSmtQuery(Collection<Constraint<?>> constraints) {

		ConstraintToZ3Str2Visitor v = new ConstraintToZ3Str2Visitor();
		List<SmtExpr> assertions = new LinkedList<SmtExpr>();
		for (Constraint<?> c : constraints) {
			SmtExpr smtExpr = c.accept(v, null);
			if (smtExpr != null) {
				assertions.add(smtExpr);
			}
		}

		SmtVariableCollector varCollector = new SmtVariableCollector();
		for (SmtExpr smtExpr : assertions) {
			smtExpr.accept(varCollector, null);
		}
		Set<SmtVariable> smtVariables = varCollector.getSmtVariables();

		if (smtVariables.isEmpty()) {
			return null; // no variables, constraint system is trivial
		}

		SmtConstantCollector constantCollector = new SmtConstantCollector();
		for (SmtExpr smtExpr : assertions) {
			smtExpr.accept(varCollector, null);
		}
		Set<SmtConstant> smtConstants = constantCollector.getSmtConstants();

		SmtOperatorCollector opCollector = new SmtOperatorCollector();
		for (SmtExpr smtExpr : assertions) {
			smtExpr.accept(opCollector, null);
		}
		Set<Operator> smtOperators = opCollector.getOperators();
		boolean addCharToIntFunction;
		if (smtOperators.contains(SmtOperation.Operator.CHAR_TO_INT)) {
			addCharToIntFunction = true;
		} else {
			addCharToIntFunction = false;
		}

		return createSmtQuery(smtVariables, smtConstants, assertions,
				addCharToIntFunction);
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
		buff.append("(define-fun " + SmtOperation.Operator.CHAR_TO_INT
				+ "((x!1 String)) Int");
		buff.append("\n");
		for (int i = 0; i < ASCII_TABLE_LENGTH; i++) {
			char c = (char) i;
			String str = String.valueOf(c);
			String encodedStr = ExprToZ3Str2Visitor.encodeString(str);
			if (i < ASCII_TABLE_LENGTH - 1) {
				String iteStr = String.format("(ite (= x!1 %s) %s", encodedStr,
						i);
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

	private static String createSmtQuery(Set<SmtVariable> smtVariables,
			Set<SmtConstant> smtConstants, List<SmtExpr> smtAssertions,
			boolean addCharToIntFunction) {

		Set<SmtVariable> smtVariablesoDeclare = new HashSet<SmtVariable>(smtVariables);
		if (addCharToIntFunction) {
			Set<SmtStringVariable> charVariables = buildCharVariables();
			smtVariablesoDeclare.addAll(charVariables);
		}

		StringBuffer buff = new StringBuffer();
		for (SmtVariable v1 : smtVariablesoDeclare) {
			String varName = v1.getName();
			if (v1 instanceof SmtIntVariable) {
				String intConst = declareIntConst(varName);
				buff.append(intConst);
				buff.append("\n");
			} else if (v1 instanceof SmtRealVariable) {
				String realConst = declareRealConst(varName);
				buff.append(realConst);
				buff.append("\n");
			} else if (v1 instanceof SmtStringVariable) {
				String stringConst = declareStringConst(varName);
				buff.append(stringConst);
				buff.append("\n");
			} else {
				throw new RuntimeException("Unknown variable type "
						+ v1.getClass().getCanonicalName());
			}
		}

		if (addCharToIntFunction) {
			String charToInt = buildCharToIntFunction();
			buff.append(charToInt);
		}
		
		SmtExprPrinter printer = new SmtExprPrinter();
		for (SmtExpr smtExpr : smtAssertions) {
			String smtExprString = smtExpr.accept(printer, null);
			String assertion = mkAssert(smtExprString);
			buff.append(assertion);
			buff.append("\n");
		}

		buff.append("(check-sat)");
		buff.append("\n");

		return buff.toString();
	}

	private static int launchNewProcess(String z3StrCmd, String smtQuery,
			int timeout, OutputStream outputStream) throws IOException {

		final Process process = Runtime.getRuntime().exec(z3StrCmd);

		InputStream stdout = process.getInputStream();
		InputStream stderr = process.getErrorStream();

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
