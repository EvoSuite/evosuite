/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.junit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.evosuite.Properties;
import org.evosuite.junit.TestExtractingVisitor.TestReader;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.TestCase;
import org.evosuite.utils.LoggingUtils;

public class JUnitTestReader implements TestReader {

	private static class MethodsExtractor extends ASTVisitor {
		private final List<String> testMethods = new ArrayList<String>();
		private String className;

		public String getClassName() {
			return className;
		}

		public List<String> getMethods() {
			return testMethods;
		}

		@Override
		public boolean visit(MethodDeclaration methodDeclaration) {
			testMethods.add(methodDeclaration.getName().getIdentifier());
			return false;
		}

		@Override
		public boolean visit(TypeDeclaration typeDeclaration) {
			if (className != null) {
				throw new RuntimeException("Only one class declaration supported!");
			}
			className = typeDeclaration.getName().getIdentifier();
			return true;
		}
	}

	/**
	 * <p>
	 * main
	 * </p>
	 * 
	 * @param args
	 *            a {@link java.lang.String} object.
	 */
	public static void main(String... args) {
		ExecutionTracer.enable();
		String[] classpath = Properties.CLASSPATH;
		String[] sourcepath = Properties.SOURCEPATH;
		JUnitTestReader testReader = new JUnitTestReader(classpath, sourcepath);
		List<File> javaTestFiles = getAllJavaFiles(new File(args[0]));
		Map<File, Map<String, TestCase>> allTests = new HashMap<File, Map<String, TestCase>>();
		for (File test : javaTestFiles) {
			Map<String, TestCase> allTestsInFile = testReader.readTests(test.getAbsolutePath());
			allTests.put(test, allTestsInFile);
			// TODO Execute test
			// ExecutionResult tmpResult = JUnitUtils.runTest(testCase);
			// TODO Find classfile
			// String testName =
			// TODO Execute classfile via JUnit
			// TestRun testRun = JUnitUtils.runTest(testName);
			// TODO Compare results for individual tests
		}
	}

	private static List<File> getAllJavaFiles(File file) {
		if (file.isDirectory()) {
			List<File> result = new ArrayList<File>();
			for (File subFile : file.listFiles()) {
				result.addAll(getAllJavaFiles(subFile));
			}
			return result;
		}
		if (!file.getName().endsWith(".java")) {
			return Collections.emptyList();
		}
		return Collections.singletonList(file);
	}

	private final String SOURCE_JAVA_VERSION = JavaCore.VERSION_1_6;

	private final String ENCODING = "UTF-8";
	protected final String[] sources;
	protected final String[] classpath;

	protected CompilationUnit compilationUnit;

	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JUnitTestReader.class);

	/**
	 * <p>
	 * Constructor for JUnitTestReader.
	 * </p>
	 * 
	 * @param classpath
	 *            an array of {@link java.lang.String} objects.
	 * @param sources
	 *            an array of {@link java.lang.String} objects.
	 */
	public JUnitTestReader(String[] classpath, String[] sources) {
		super();
		this.sources = sources;
		this.classpath = expandClasspath(classpath);
	}

	public JUnitTestReader() {
		super();
		this.classpath = Properties.CP.split(File.pathSeparator);
		LoggingUtils.getEvoLogger().info("Using classpath: "
		                                         + Arrays.asList(this.classpath));
		this.sources = new String[0];
	}

	/** {@inheritDoc} */
	@Override
	public int getLineNumber(int sourcePos) {
		return compilationUnit.getLineNumber(sourcePos);
	}

	/**
	 * <p>
	 * readJUnitTestCase
	 * </p>
	 * 
	 * @param qualifiedTestMethod
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.testcase.TestCase} object.
	 */
	public TestCase readJUnitTestCase(String qualifiedTestMethod) {
		String clazz = qualifiedTestMethod.substring(0, qualifiedTestMethod.indexOf("#"));
		String method = qualifiedTestMethod.substring(qualifiedTestMethod.indexOf("#") + 1);
		CompoundTestCase testCase = new CompoundTestCase(clazz, method);
		TestExtractingVisitor testExtractingVisitor = new TestExtractingVisitor(testCase,
		        clazz, method, this);
		String javaFile = findTestFile(clazz);
		String fileContents = readJavaFile(javaFile);
		compilationUnit = parseJavaFile(javaFile, fileContents);
		compilationUnit.accept(testExtractingVisitor);
		TestCase result = testCase.finalizeTestCase();
		return result;
	}

	/**
	 * <p>
	 * readJUnitTestCase
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 * @param cu
	 *            a {@link org.eclipse.jdt.core.dom.CompilationUnit} object.
	 * @return a {@link org.evosuite.testcase.TestCase} object.
	 */
	public TestCase readJUnitTestCase(String className, final String methodName,
	        final CompilationUnit cu) {
		CompoundTestCase testCase = new CompoundTestCase(className, methodName);

		TestExtractingVisitor testExtractingVisitor = new TestExtractingVisitor(testCase,
		        className, null, this);

		compilationUnit = cu; // parseJavaFile(className, fileContent);
		compilationUnit.accept(testExtractingVisitor);

		TestCase result = testCase.finalizeTestCase();
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public CompoundTestCase readTestCase(String clazz, CompoundTestCase child) {
		CompoundTestCase testCase = new CompoundTestCase(clazz, child);
		TestExtractingVisitor testExtractingVisitor = new TestExtractingVisitor(testCase,
		        clazz, null, this);
		String javaFile = findTestFile(clazz);
		String fileContents = readJavaFile(javaFile);
		compilationUnit = parseJavaFile(javaFile, fileContents);
		compilationUnit.accept(testExtractingVisitor);
		return testCase;
	}

	public Map<String, TestCase> readTests(String javaFile) {
		String fileContents = readJavaFile(javaFile);
		compilationUnit = parseJavaFile(javaFile, fileContents);
		MethodsExtractor methodsExtractor = new MethodsExtractor();
		compilationUnit.accept(methodsExtractor);
		Map<String, TestCase> result = new HashMap<String, TestCase>();
		String clazz = methodsExtractor.getClassName();
		for (String method : methodsExtractor.getMethods()) {
			CompoundTestCase testCase = new CompoundTestCase(clazz, method);
			TestExtractingVisitor testExtractingVisitor = new TestExtractingVisitor(
			        testCase, clazz, method, this);
			compilationUnit.accept(testExtractingVisitor);
			result.put(method, testCase.finalizeTestCase());
		}
		return result;
	}

	/**
	 * <p>
	 * extractJavaFile
	 * </p>
	 * 
	 * @param srcDir
	 *            a {@link java.lang.String} object.
	 * @param clazz
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String extractJavaFile(String srcDir, String clazz) {
		clazz = clazz.replaceAll("\\.", File.separator);
		if (!srcDir.endsWith(File.separator)) {
			srcDir += File.separator;
		}
		return srcDir + clazz + ".java";
	}

	/**
	 * <p>
	 * extractTestMethodName
	 * </p>
	 * 
	 * @param testMethod
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String extractTestMethodName(String testMethod) {
		return testMethod.substring(testMethod.indexOf("#") + 1);
	}

	/**
	 * <p>
	 * findTestFile
	 * </p>
	 * 
	 * @param clazz
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String findTestFile(String clazz) {
		StringBuffer sourcesString = new StringBuffer();
		for (String dir : sources) {
			String path = extractJavaFile(dir, clazz);
			File file = new File(path);
			if (file.exists()) {
				return path;
			}
			sourcesString.append(dir).append(";");
		}
		throw new RuntimeException("Could not find class '" + clazz + "' in sources: "
		        + sourcesString.toString());
	}

	/**
	 * <p>
	 * parseJavaFile
	 * </p>
	 * 
	 * @param unitName
	 *            a {@link java.lang.String} object.
	 * @param fileContents
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.eclipse.jdt.core.dom.CompilationUnit} object.
	 */
	protected CompilationUnit parseJavaFile(String unitName, String fileContents) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setUnitName(unitName);
		@SuppressWarnings("unchecked")
		Hashtable<String, String> options = JavaCore.getDefaultOptions();
		options.put(JavaCore.COMPILER_SOURCE, SOURCE_JAVA_VERSION);
		parser.setCompilerOptions(options);
		String[] encodings = createEncodings(ENCODING, sources.length);
		parser.setEnvironment(classpath, sources, encodings, true);
		parser.setSource(fileContents.toCharArray());
		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
		Set<String> problems = new HashSet<String>();
		for (IProblem problem : compilationUnit.getProblems()) {
			problems.add(problem.toString());
		}
		if (!problems.isEmpty()) {
			logger.warn("Got {} problems compiling the source file: ", problems.size());
			for (String problem : problems) {
				logger.warn("{}", problem);
			}
		}
		return compilationUnit;
	}

	/**
	 * <p>
	 * readJavaFile
	 * </p>
	 * 
	 * @param path
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String readJavaFile(String path) {
		StringBuffer result = new StringBuffer();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line = null;
			while ((line = reader.readLine()) != null) {
				result.append(line).append("\n");
			}
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception exc) {
					// muted
				}
			}
		}
		return result.toString();
	}

	private String[] createEncodings(String encoding, int length) {
		String[] encodings = new String[length];
		for (int idx = 0; idx < length; idx++) {
			encodings[idx] = encoding;
		}
		return encodings;
	}

	private String[] expandClasspath(String[] classpath) {
		ArrayList<String> result = new ArrayList<String>();
		if (classpath != null) {
			for (String classpathEntry : classpath) {
				if (classpathEntry.endsWith("*")) {
					File dir = new File(
					        classpathEntry.substring(0, classpathEntry.length() - 1));
					for (File file : dir.listFiles()) {
						if (file.getName().endsWith(".jar")) {
							try {
								result.add(file.getCanonicalPath());
							} catch (IOException exc) {
								throw new RuntimeException(exc);
							}
						}
					}
				} else {
					result.add(classpathEntry);
				}
			}
		}
		return result.toArray(new String[result.size()]);
	}
}
