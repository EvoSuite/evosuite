package org.evosuite.symbolic.search;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringConstraint;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.junit.Test;

public class TestPatternSearch {

	@Test
	public void testMatcherMatches() {

		String input = "random_value";
		String format = "(\\d+)-(\\d\\d)-(\\d)";
		// String format = "^(\\d+)-(\\d\\d)-(\\d)$";

		StringVariable var0 = new StringVariable("var0", input);

		StringConstant symb_regex = ExpressionFactory
				.buildNewStringConstant(format);
		StringBinaryComparison strComp = new StringBinaryComparison(symb_regex,
				Operator.PATTERNMATCHES, var0, 0L);

		StringConstraint constraint = new StringConstraint(strComp,
				Comparator.NE, new IntegerConstant(0));

		List<Constraint<?>> constraints = Collections
				.<Constraint<?>> singletonList(constraint);

		ConstraintSolver skr = new ConstraintSolver();
		Map<String, Object> solution;
		try {
			solution = skr.solve(constraints);
			assertNotNull(solution);
			String var0_value = (String) solution.get("var0");

			Pattern pattern = Pattern.compile(format);
			Matcher matcher = pattern.matcher(var0_value);
			assertTrue(matcher.matches());
		} catch (ConstraintSolverTimeoutException e) {
			fail();
		}

	}

}
