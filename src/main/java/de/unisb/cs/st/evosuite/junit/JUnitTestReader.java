package de.unisb.cs.st.evosuite.junit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import de.unisb.cs.st.evosuite.testcase.TestCase;

public abstract class JUnitTestReader {

	protected final String[] sources;
	protected final String[] classpath;

	public JUnitTestReader(String[] classpath, String[] sources) {
		super();
		this.classpath = classpath;
		this.sources = sources;
	}

	public abstract TestCase readJUnitTestCase(String qualifiedTestMethod);

	protected String extractJavaFile(String srcDir, String testMethod) {
		String clazz = testMethod.substring(0, testMethod.indexOf("#"));
		clazz = clazz.replaceAll("\\.", File.separator);
		if (!srcDir.endsWith(File.separator)) {
			srcDir += File.separator;
		}
		return srcDir + clazz + ".java";
	}

	protected String extractTestMethodName(String testMethod) {
		return testMethod.substring(testMethod.indexOf("#") + 1);
	}

	protected String findTestFile(String qualifiedTestMethod) {
		StringBuffer sourcesString = new StringBuffer();
		for (String dir : sources) {
			String path = extractJavaFile(dir, qualifiedTestMethod);
			File file = new File(path);
			if (file.exists()) {
				return path;
			}
			sourcesString.append(dir).append(";");
		}
		throw new RuntimeException("Could not find test '" + qualifiedTestMethod + "' in sources: "
				+ sourcesString.toString());
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
}
