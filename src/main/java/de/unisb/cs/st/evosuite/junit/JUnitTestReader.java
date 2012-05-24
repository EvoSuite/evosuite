package de.unisb.cs.st.evosuite.junit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.junit.TestExtractingVisitor.TestReader;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.TestCase;

public class JUnitTestReader implements TestReader {

	public static void main(String... args) {
		ExecutionTracer.enable();
		String[] classpath = Properties.CLASSPATH;
		String[] sourcepath = Properties.SOURCEPATH;
		JUnitTestReader testReader = new JUnitTestReader(classpath, sourcepath);
		File file = new File(args[0]);
		if (file.isDirectory()) {
			for (File javaFile : file.listFiles()) {
				if (!javaFile.getName().endsWith(".java")) {
					continue;
				}
				CompoundTestCase testCase = testReader.readJUnitTestCase(javaFile);
				// TODO Execute test
//				ExecutionResult tmpResult = JUnitUtils.runTest(testCase);
				// TODO Find classfile
//				String testName = 
				// TODO Execute classfile via JUnit
//				TestRun testRun = JUnitUtils.runTest(testName);
				// TODO Compare results for individual tests
			}
		}
	}

	private CompoundTestCase readJUnitTestCase(File javaFile) {
		// TODO-JRO Implement method readJUnitTestCase
		return null;
	}

	protected final String[] sources;
	protected final String[] classpath;

	public JUnitTestReader(String[] classpath, String[] sources) {
		super();
		this.sources = sources;
		this.classpath = expandClasspath(classpath);
	}

	public TestCase readJUnitTestCase(String qualifiedTestMethod) {
		String clazz = qualifiedTestMethod.substring(0, qualifiedTestMethod.indexOf("#"));
		String method = qualifiedTestMethod.substring(qualifiedTestMethod.indexOf("#") + 1);
		CompoundTestCase testCase = new CompoundTestCase(clazz, method);
		TestExtractingVisitor testExtractingVisitor = new TestExtractingVisitor(testCase, clazz, method, this);
		String javaFile = findTestFile(clazz);
		String fileContents = readJavaFile(javaFile);
		CompilationUnit compilationUnit = parseJavaFile(javaFile, fileContents);
		compilationUnit.accept(testExtractingVisitor);
		TestCase result = testCase.finalizeTestCase();
		return result;
	}

	@Override
	public CompoundTestCase readTestCase(String clazz, CompoundTestCase child) {
		CompoundTestCase testCase = new CompoundTestCase(clazz, child);
		TestExtractingVisitor testExtractingVisitor = new TestExtractingVisitor(testCase, clazz, null, this);
		String javaFile = findTestFile(clazz);
		String fileContents = readJavaFile(javaFile);
		CompilationUnit compilationUnit = parseJavaFile(javaFile, fileContents);
		compilationUnit.accept(testExtractingVisitor);
		return testCase;
	}

	protected String extractJavaFile(String srcDir, String clazz) {
		clazz = clazz.replaceAll("\\.", File.separator);
		if (!srcDir.endsWith(File.separator)) {
			srcDir += File.separator;
		}
		return srcDir + clazz + ".java";
	}

	protected String extractTestMethodName(String testMethod) {
		return testMethod.substring(testMethod.indexOf("#") + 1);
	}

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
		throw new RuntimeException("Could not find class '" + clazz + "' in sources: " + sourcesString.toString());
	}

	protected CompilationUnit parseJavaFile(String unitName, String fileContents) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setUnitName(unitName);
		parser.setEnvironment(classpath, sources, null, true);
		parser.setSource(fileContents.toCharArray());
		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
		return compilationUnit;
	}

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

	private String[] expandClasspath(String[] classpath) {
		ArrayList<String> result = new ArrayList<String>();
		if (classpath != null) {
			for (String classpathEntry : classpath) {
				if (classpathEntry.endsWith("*")) {
					File dir = new File(classpathEntry.substring(0, classpathEntry.length() - 1));
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
