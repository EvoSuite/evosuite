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
