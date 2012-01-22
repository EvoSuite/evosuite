/**
 * 
 */
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
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerBinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstraint;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.Operator;

/**
 * @author fraser
 * 
 */
public class TestIntegerSearch {

	@Test
	public void testEQConstant() {
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 0, -1000000,
		        1000000), Comparator.EQ, new IntegerConstant(235082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertEquals(235082, ((Number) result.get("test1")).intValue());
	}

	@Test
	public void testNEConstant() {
		// TODO: Currently, the model returned by the search is null if the constraint is already satisfied, 
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 235082,
		        -1000000, 1000000), Comparator.NE, new IntegerConstant(235082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(235082 != ((Number) result.get("test1")).intValue());
	}

	@Test
	public void testLEConstant() {
		// TODO: Currently, the model returned by the search is null if the constraint is already satisfied, 
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 235086,
		        -1000000, 1000000), Comparator.LE, new IntegerConstant(235082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(235082 >= ((Number) result.get("test1")).intValue());
	}

	@Test
	public void testLTConstant() {
		// TODO: Currently, the model returned by the search is null if the constraint is already satisfied, 
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 235086,
		        -1000000, 1000000), Comparator.LT, new IntegerConstant(235082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(235082 > ((Number) result.get("test1")).intValue());
	}

	@Test
	public void testGEConstant() {
		// TODO: Currently, the model returned by the search is null if the constraint is already satisfied, 
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 0, -1000000,
		        1000000), Comparator.GE, new IntegerConstant(235082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(235082 <= ((Number) result.get("test1")).intValue());
	}

	@Test
	public void testGTConstant() {
		// TODO: Currently, the model returned by the search is null if the constraint is already satisfied, 
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 0, -1000000,
		        1000000), Comparator.GT, new IntegerConstant(235082)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertTrue(235082 < ((Number) result.get("test1")).intValue());
	}

	@Test
	public void testEQVariable() {
		int var1 = 0;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.EQ, new IntegerVariable("test2", var2,
		        -1000000, 1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		assertEquals(var1, var2);
	}

	@Test
	public void testNEVariable() {
		int var1 = 1;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.NE, new IntegerVariable("test2", var2,
		        -1000000, 1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		assertTrue(var1 != var2);
	}

	@Test
	public void testLEVariable() {
		int var1 = 2;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.LE, new IntegerVariable("test2", var2,
		        -1000000, 1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		assertTrue(var1 <= var2);
	}

	@Test
	public void testLTVariable() {
		int var1 = 2;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.LT, new IntegerVariable("test2", var2,
		        -1000000, 1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		assertTrue(var1 < var2);
	}

	@Test
	public void testGEVariable() {
		int var1 = 0;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.GE, new IntegerVariable("test2", var2,
		        -1000000, 1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		assertTrue(var1 >= var2);
	}

	@Test
	public void testGTVariable() {
		int var1 = 0;
		int var2 = 1;
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.GT, new IntegerVariable("test2", var2,
		        -1000000, 1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		assertTrue(var1 > var2);
	}

	@Test
	public void testEQArithmetic() {
		int var1 = 0;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 != var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.EQ, new IntegerBinaryExpression(
		        new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
		        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		if (result.containsKey("test3"))
			var3 = ((Number) result.get("test3")).intValue();
		assertTrue(var1 == var2 + var3);
	}

	@Test
	public void testNEArithmetic() {
		int var1 = 2;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 == var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.NE, new IntegerBinaryExpression(
		        new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
		        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		if (result.containsKey("test3"))
			var3 = ((Number) result.get("test3")).intValue();
		assertTrue(var1 != var2 + var3);
	}

	@Test
	public void testLEArithmetic() {
		int var1 = 3;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 > var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.LE, new IntegerBinaryExpression(
		        new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
		        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		if (result.containsKey("test3"))
			var3 = ((Number) result.get("test3")).intValue();
		assertTrue(var1 <= var2 + var3);
	}

	@Test
	public void testLTArithmetic() {
		int var1 = 2;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 >= var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.LT, new IntegerBinaryExpression(
		        new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
		        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		if (result.containsKey("test3"))
			var3 = ((Number) result.get("test3")).intValue();
		assertTrue(var1 < var2 + var3);
	}

	@Test
	public void testGEArithmetic() {
		int var1 = 0;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 < var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.GT, new IntegerBinaryExpression(
		        new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
		        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		if (result.containsKey("test3"))
			var3 = ((Number) result.get("test3")).intValue();
		assertTrue(var1 >= var2 + var3);
	}

	@Test
	public void testGTArithmetic() {
		int var1 = 0;
		int var2 = 1;
		int var3 = 1;
		assertTrue(var1 <= var2 + var3);

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.GE, new IntegerBinaryExpression(
		        new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
		        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		if (result.containsKey("test3"))
			var3 = ((Number) result.get("test3")).intValue();
		assertTrue(var1 >= var2 + var3);
	}

	@Test
	public void testEvosuiteExample1() {
		int var1 = 1;
		int var2 = 1;

		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.LE, new IntegerConstant(0)));
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.LT, new IntegerVariable("test2", var2,
		        -1000000, 1000000)));
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1,
		        -1000000, 1000000), Comparator.GE, new IntegerConstant(0)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		if (result.containsKey("test1"))
			var1 = ((Number) result.get("test1")).intValue();
		if (result.containsKey("test2"))
			var2 = ((Number) result.get("test2")).intValue();
		assertEquals(0, var1);
		assertTrue(var1 < var2);
	}

}
