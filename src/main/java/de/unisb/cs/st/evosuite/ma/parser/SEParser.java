/**
 * 
 */
package de.unisb.cs.st.evosuite.ma.parser;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.unisb.cs.st.evosuite.ma.Editor;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * @author Yury Pavlov
 * 
 */
public class SEParser {

	private final TestVisitor tv;
	
	private final Editor editor;
	
	public SEParser(Editor editor) {
		this.editor = editor;
		tv = new TestVisitor(true);
	}

	public TestCase parseTest(String testCode) throws IOException,
			ParseException {
		tv.reset();
		CompilationUnit cu = null;

		testCode = "class DummyCl{void DummyMt(){" + testCode + "}}";
		InputStream inputStream = new ByteArrayInputStream(testCode.getBytes());

		try {
			cu = JavaParser.parse(inputStream);
		} finally {
			inputStream.close();
		}

		TestCase res = null;
		tv.visit(cu, null);
		if (tv.isValid()) {
			res = tv.getNewTC();
			
			System.out.println("\n-------------------------------------------");
			System.out.println(res.toCode());
			System.out.println("===========================================");
		} else {
			for (String error : tv.getParsErrors()) {
				editor.showParseException(error);
			}
		}
		return res;
	}

}
