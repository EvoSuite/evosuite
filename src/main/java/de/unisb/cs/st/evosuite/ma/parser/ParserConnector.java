/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.ma.parser;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.ma.Editor;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * Connector between the {@link Editor} and {@link VisitorParser}
 * 
 * @author Yury Pavlov
 */
public class ParserConnector {

	private final VisitorParser tv;

	private final Editor editor;

	public ParserConnector(Editor editor, boolean guiActive) {
		this.editor = editor;
		tv = new VisitorParser(guiActive);
	}

	public TestCase parseTest(String testCode) throws IOException, ParseException {
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
				if (editor != null) {
					editor.showParseException(error);
				}
			}
		}
		return res;
	}

	/**
	 * To load test cases from file. {@code namePat} the pattern for function
	 * names which are loaded like test cases.
	 * 
	 * @param file
	 *            {@link File}
	 * @param namePat
	 *            {@code String}
	 * @return <code>List<{@link TestCase}></code>
	 */
	public List<TestCase> parseFile(File file, String namePat) {
		CompilationUnit cu = null;

		try {
			cu = JavaParser.parse(file);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// visit all methods
		MethodVisitor mv = new MethodVisitor();
		mv.visit(cu, namePat);
		List<TestCase> res = mv.getTCs();

		return res;
	}

	/**
	 * For files parsing.
	 */
	private class MethodVisitor extends VoidVisitorAdapter<String> {
		private List<TestCase> res = new ArrayList<TestCase>();

		public void visit(MethodDeclaration n, String arg) {
			// if function's name matches name's pattern then pars this function
			// like a TestCase
			if (n.getName().matches(arg)) {
				tv.reset();
				tv.visit(n, arg);
				if (tv.isValid()) {
					res.add(tv.getNewTC());
				}
			}
		}

		/**
		 * @return <code>List<{@link TestCase}></code>
		 */
		public List<TestCase> getTCs() {
			return res;
		}
	}

}
