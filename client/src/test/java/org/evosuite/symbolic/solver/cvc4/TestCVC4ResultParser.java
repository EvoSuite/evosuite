/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.solver.cvc4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.evosuite.symbolic.solver.SolverErrorException;
import org.evosuite.symbolic.solver.SolverParseException;
import org.evosuite.symbolic.solver.SolverResult;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.junit.Test;

public class TestCVC4ResultParser {

	@Test
	public void parseBlankStringSolution() throws SolverParseException, SolverErrorException, SolverTimeoutException {
		StringBuffer buff = new StringBuffer();
		buff.append("sat\n");
		buff.append("(model\n");
		buff.append("(define-fun var8 () String \" \")\n");
		buff.append(")\n");
		String result_str = buff.toString();
		CVC4ResultParser parser = new CVC4ResultParser();
		SolverResult solution = parser.parse(result_str);
		assertTrue(solution.isSAT());
		assertEquals(" ", solution.getValue("var8"));
	}
	
	@Test
	public void parseEmptyStringSolution() throws SolverParseException, SolverErrorException, SolverTimeoutException {
		StringBuffer buff = new StringBuffer();
		buff.append("sat\n");
		buff.append("(model\n");
		buff.append("(define-fun var8 () String \"\")\n");
		buff.append(")\n");
		String result_str = buff.toString();
		CVC4ResultParser parser = new CVC4ResultParser();
		SolverResult solution = parser.parse(result_str);
		assertTrue(solution.isSAT());
		assertEquals("", solution.getValue("var8"));
	}

	@Test
	public void parseSingleStringSolution() throws SolverParseException, SolverErrorException, SolverTimeoutException {
		StringBuffer buff = new StringBuffer();
		buff.append("sat\n");
		buff.append("(model\n");
		buff.append("(define-fun var8 () String \"Hello\")\n");
		buff.append(")\n");
		String result_str = buff.toString();
		CVC4ResultParser parser = new CVC4ResultParser();
		SolverResult solution = parser.parse(result_str);
		assertTrue(solution.isSAT());
		assertEquals("Hello", solution.getValue("var8"));
	}
	
	@Test
	public void parseSingleLineStringSolution() throws SolverParseException, SolverErrorException, SolverTimeoutException {
		StringBuffer buff = new StringBuffer();
		buff.append("sat\n");
		buff.append("(model\n");
		buff.append("(define-fun var8 () String \"Hello World\")\n");
		buff.append(")\n");
		String result_str = buff.toString();
		CVC4ResultParser parser = new CVC4ResultParser();
		SolverResult solution = parser.parse(result_str);
		assertTrue(solution.isSAT());
		assertEquals("Hello World", solution.getValue("var8"));
	}

	@Test
	public void parseMultiLineSolution() throws SolverParseException, SolverErrorException, SolverTimeoutException {
		StringBuffer buff = new StringBuffer();
		buff.append("sat\n");
		buff.append("(model\n");
		buff.append("(define-fun var8 () String \"Hello\nBeautiful\nWorld\")\n");
		buff.append(")\n");
		String result_str = buff.toString();
		CVC4ResultParser parser = new CVC4ResultParser();
		SolverResult solution = parser.parse(result_str);
		assertTrue(solution.isSAT());
		assertEquals("Hello\nBeautiful\nWorld", solution.getValue("var8"));
	}

}
