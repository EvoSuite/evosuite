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
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.SmtExprBuilder;
import org.evosuite.symbolic.solver.Solver;
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

	public Z3Str2Solver() {
		super();
	}

	public Z3Str2Solver(boolean addMissingVariables) {
		super(addMissingVariables);
	}

	private static File createNewTmpDir() {
		File dir = null;
		String dirName = FileUtils.getTempDirectoryPath() + File.separator
				+ "EvoSuiteZ3Str_" + (dirCounter++) + "_"
				+ System.currentTimeMillis();

		// first create a tmp folder
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

	private static SmtCheckSatQuery buildSmtQuerty(
			Collection<Constraint<?>> constraints) {

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

		Set<SmtVariable> smtVariablesToDeclare = new HashSet<SmtVariable>(
				smtVariables);
		if (addCharToIntFunction) {
			Set<SmtStringVariable> charVariables = buildCharVariables();
			smtVariablesToDeclare.addAll(charVariables);
		}

		List<SmtConstantDeclaration> constantDeclarations = new LinkedList<SmtConstantDeclaration>();

		for (SmtVariable v1 : smtVariablesToDeclare) {
			String varName = v1.getName();
			if (v1 instanceof SmtIntVariable) {
				SmtConstantDeclaration constantDecl = SmtExprBuilder
						.mkIntConstantDeclaration(varName);
				constantDeclarations.add(constantDecl);
			} else if (v1 instanceof SmtRealVariable) {
				SmtConstantDeclaration constantDecl = SmtExprBuilder
						.mkRealConstantDeclaration(varName);
				constantDeclarations.add(constantDecl);
			} else if (v1 instanceof SmtStringVariable) {
				SmtConstantDeclaration constantDecl = SmtExprBuilder
						.mkStringConstantDeclaration(varName);
				constantDeclarations.add(constantDecl);
			} else {
				throw new RuntimeException("Unknown variable type "
						+ v1.getClass().getCanonicalName());
			}
		}

		List<SmtFunctionDefinition> functionDefinitions = new LinkedList<SmtFunctionDefinition>();
		if (addCharToIntFunction) {
			String charToInt = buildCharToIntFunction();
			SmtFunctionDefinition newFunctionDef = new SmtFunctionDefinition(
					charToInt);
			functionDefinitions.add(newFunctionDef);
		}

		SmtCheckSatQuery smtCheckSatQuery = new SmtCheckSatQuery(
				constantDeclarations, functionDefinitions, assertions);

		return smtCheckSatQuery;

	}

	@Override
	public Map<String, Object> solve(Collection<Constraint<?>> constraints)
			throws ConstraintSolverTimeoutException {

		SmtCheckSatQuery smtCheckSatQuery = buildSmtQuerty(constraints);

		if (smtCheckSatQuery.getConstantDeclarations().isEmpty()) {
			logger.debug("Z3-str2 input has no variables");
			logger.debug("returning NULL as default value");
			return null;
		}
		
		Z3Str2QueryPrinter printer = new Z3Str2QueryPrinter();
		String smtQueryStr = printer.print(smtCheckSatQuery);

		System.out.println("Z3-str2 input:");
		System.out.println(smtQueryStr);

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
			Utils.writeFile(smtQueryStr, z3TempFileName);
			String z3Cmd = Properties.Z3_STR2_PATH + " -f " + z3TempFileName;
			ByteArrayOutputStream stdout = new ByteArrayOutputStream();
			launchNewProcess(z3Cmd, smtQueryStr, timeout, stdout);

			String z3ResultStr = stdout.toString("UTF-8");

			if (z3ResultStr.contains("unknown sort")) {
				logger.debug("Z3_str2 output was " + z3ResultStr);
				throw new EvosuiteError(
						"Z3_str2 found an unknown sort for query: "
								+ smtQueryStr);
			}

			if (z3ResultStr.contains("unknown constant")) {
				logger.debug("Z3_str2 output was " + z3ResultStr);
				throw new EvosuiteError(
						"Z3_str2 found an unknown constant for query: "
								+ smtQueryStr);
			}

			if (z3ResultStr.contains("invalid expression")) {
				logger.debug("Z3_str2 output was " + z3ResultStr);
				throw new EvosuiteError(
						"Z3_str2 found an invalid expression for query: "
								+ smtQueryStr);
			}

			if (z3ResultStr.contains("unexpected input")) {
				logger.debug("Z3_str2 output was " + z3ResultStr);
				throw new EvosuiteError(
						"Z3_str2 found an unexpected input for query: "
								+ smtQueryStr);
			}

			Z3Str2ModelParser parser = new Z3Str2ModelParser();
			Set<Variable<?>> variables = getVariables(constraints);
			Map<String, Object> initialValues = getConcreteValues(variables);
			Map<String, Object> solution;
			if (addMissingVariables()) {
				solution = parser.parse(z3ResultStr, initialValues);
			} else {
				solution = parser.parse(z3ResultStr);
			}

			if (solution==null) {
				/*UNSAT or ERROR*/
				return null;
			}
			
			// check solution is correct
			boolean check = checkSolution(constraints, solution);
			if (!check) {
				logger.debug("Z3-str2 solution does not solve the constraint system!");
				return null;
			}

			return solution;

		} catch (UnsupportedEncodingException e) {
			throw new EvosuiteError("UTF-8 should not cause this exception!");
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
