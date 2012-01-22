package de.unisb.cs.st.evosuite.symbolic.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.unisb.cs.st.evosuite.symbolic.expr.Comparator;
import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
import de.unisb.cs.st.evosuite.symbolic.expr.Operator;
import de.unisb.cs.st.evosuite.symbolic.expr.RealBinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.RealConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.RealConstraint;
import de.unisb.cs.st.evosuite.symbolic.expr.RealVariable;

public class TestRealSearch {
	@Test
	public void testEQConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 0, -1000000.0,
		        1000000.0), Comparator.EQ, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 == ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testNEConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 2.35082, -1000000.0,
		        1000000.0), Comparator.NE, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 != ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testLEConstant() {

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 5.35086, -1000000.0,
		        1000000.0), Comparator.LE, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 >= ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testLTConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 5.35086, -1000000.0,
		        1000000.0), Comparator.LT, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 > ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testGEConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 0, -1000000.0,
		        1000000.0), Comparator.GE, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 <= ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testGTConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 0, -1000000.0,
		        1000000.0), Comparator.GT, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 < ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testEQConstantAfterComma() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 0, -1000000.0,
		        1000000.0), Comparator.EQ, new RealConstant(0.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(0.35082 == ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testLEConstantAfterComma() {

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 2.35086, -1000000.0,
		        1000000.0), Comparator.LE, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 >= ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testLTConstantAfterComma() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 2.35086, -1000000.0,
		        1000000.0), Comparator.LT, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 > ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testGEConstantAfterComma() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 2.0, -1000000.0,
		        1000000.0), Comparator.GE, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 <= ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testGTConstantAfterComma() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", 2.0, -1000000.0,
		        1000000.0), Comparator.GT, new RealConstant(2.35082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(2.35082 < ((Number) result.get("test1")).doubleValue());
	}

	@Test
	public void testEQVariable() {
		double var1 = 0.23123;
		double var2 = 1.12321;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0,
		        1000000.0), Comparator.EQ, new RealVariable("test2", var2, -1000000.0,
		        1000000.0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).doubleValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).doubleValue();
		assertTrue(var1 == var2);
	}

	@Test
	public void testNEVariable() {
		double var1 = 1.5546;
		double var2 = 1.5546;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0,
		        1000000.0), Comparator.NE, new RealVariable("test2", var2, -1000000.0,
		        1000000.0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).doubleValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).doubleValue();
		assertTrue(var1 != var2);
	}

	@Test
	public void testLEVariable() {
		double var1 = 2.6576;
		double var2 = 1.434;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0,
		        1000000.0), Comparator.LE, new RealVariable("test2", var2, -1000000.0,
		        1000000.0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).doubleValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).doubleValue();
		assertTrue(var1 <= var2);
	}

	@Test
	public void testLTVariable() {
		double var1 = 2.6576;
		double var2 = 1.434;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0,
		        1000000.0), Comparator.LT, new RealVariable("test2", var2, -1000000.0,
		        1000000.0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).doubleValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).doubleValue();
		assertTrue(var1 < var2);
	}

	@Test
	public void testGEVariable() {
		double var1 = 0.7868;
		double var2 = 1.9765;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0,
		        1000000.0), Comparator.GE, new RealVariable("test2", var2, -1000000.0,
		        1000000.0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).doubleValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).doubleValue();
		assertTrue(var1 >= var2);
	}

	@Test
	public void testGTVariable() {
		double var1 = 0.7868;
		double var2 = 1.9765;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000.0,
		        1000000.0), Comparator.GT, new RealVariable("test2", var2, -1000000.0,
		        1000000.0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).doubleValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).doubleValue();
		assertTrue(var1 > var2);
	}

	@Test
	public void testEvosuiteExample1() {
		double var1 = 1;
		double var2 = 1;

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000,
		        1000000), Comparator.LE, new RealConstant(0)));
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000,
		        1000000), Comparator.LT, new RealVariable("test2", var2, -1000000,
		        1000000)));
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000,
		        1000000), Comparator.GE, new RealConstant(0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		assertEquals(0, var1, 0.0001);
		assertTrue(var1 < var2);
	}

	@Test
	public void testEvosuiteExample2() {
		double var1 = 0.0;
		double var2 = 2.9999999995311555E-7;

		// x >= 0
		// x < y + 1
		// y > 0

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000,
		        1000000), Comparator.GE, new RealConstant(0.0)));
		constraints.add(new RealConstraint(new RealVariable("test1", var1, -1000000,
		        1000000), Comparator.LT, new RealBinaryExpression(new RealVariable(
		        "test2", var2, -1000000, 1000000), Operator.PLUS, new RealConstant(1.0),
		        var2 + 1.0)));
		constraints.add(new RealConstraint(new RealVariable("test2", var2, -1000000,
		        1000000), Comparator.GT, new RealConstant(0.0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		//assertEquals(0, var1, 0.0001);
		//assertTrue(var1 < var2);
	}
}
