package de.unisb.cs.st.evosuite.junit;

import org.eclipse.jdt.core.dom.CompilationUnit;

import de.unisb.cs.st.evosuite.testcase.TestCase;

public class ComplexJUnitTestReader extends JUnitTestReader {

	public ComplexJUnitTestReader(String[] classpath, String[] sources) {
		super(classpath, sources);
	}

	@Override
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

}
