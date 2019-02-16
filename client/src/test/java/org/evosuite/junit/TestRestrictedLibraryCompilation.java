package org.evosuite.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.After;
import org.junit.Test;

public class TestRestrictedLibraryCompilation {

	private static final String FOO_CLASS = "Foo.class";
	private static final String FOO_JAVA = "Foo.java";

	private static String buildTestCase() {
		StringBuffer b = new StringBuffer();
		b.append("import org.jcp.xml.dsig.internal.dom.XMLDSigRI;\n");
		b.append("class Foo {\n");
		b.append("  public static void main(String[] args) {\n");
		b.append(" XMLDSigRI s = new XMLDSigRI();\n ");
		b.append("  }\n");
		b.append("}\n");
		return b.toString();
	}

	@Test
	public void testCompilationRestrictedLibraries() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		final String testCaseString = buildTestCase();
		File testCaseFile = writeToFile(testCaseString);

		int ret_code = compiler.run(null, null, null, "-XDignore.symbol.file=true", testCaseFile.getAbsolutePath());

		assertEquals(0, ret_code);

		File expectedCompiledFile = new File(FOO_CLASS);
		assertTrue(expectedCompiledFile.exists());
	}

	@After
	public void deleteFiles() {
		File classFile = new File(FOO_CLASS);
		if (classFile.exists()) {
			classFile.delete();
		}
		File javaFile = new File(FOO_JAVA);
		if (javaFile.exists()) {
			javaFile.delete();
		}

	}

	private static File writeToFile(String testCaseString) throws FileNotFoundException {
		File f = new File(FOO_JAVA);
		PrintWriter out = new PrintWriter(f);
		out.print(testCaseString);
		out.close();
		return f;
	}

}
