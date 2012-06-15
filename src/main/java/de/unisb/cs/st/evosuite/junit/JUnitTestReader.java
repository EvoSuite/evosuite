/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.junit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import de.unisb.cs.st.evosuite.testcase.TestCase;

public class JUnitTestReader {

	protected final String[] sources;
	protected final String[] classpath;

	public JUnitTestReader(String[] classpath, String[] sources) {
		super();
		this.classpath = classpath;
		this.sources = sources;
	}

	public TestCase readJUnitTestCase(String qualifiedTestMethod) {
		String javaFile = findTestFile(qualifiedTestMethod);
		String fileContents = readJavaFile(javaFile);
		CompilationUnit compilationUnit = parseJavaFile(javaFile, fileContents);
		CompoundTestCase testCase = new CompoundTestCase();
		TestExtractingVisitor testExtractingVisitor = new TestExtractingVisitor(testCase, qualifiedTestMethod);
		compilationUnit.accept(testExtractingVisitor);
		// TODO-JRO Implement iteration over parents
		testCase.finalizeTestCase();
		return testCase;
	}

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
