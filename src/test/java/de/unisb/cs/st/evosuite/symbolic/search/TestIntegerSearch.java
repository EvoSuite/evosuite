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
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstraint;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerVariable;

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
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 0, -1000000,
		        1000000), Comparator.EQ, new IntegerVariable("test2", 1, -1000000,
		        1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertNotNull(result.get("test2"));
		assertEquals(((Number) result.get("test1")).intValue(),
		             ((Number) result.get("test2")).intValue());
	}

	@Test
	public void testNEVariable() {
		// TODO: Currently, the model returned by the search is null if the constraint is already satisfied, 
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 0, -1000000,
		        1000000), Comparator.NE, new IntegerVariable("test2", 0, -1000000,
		        1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertNotNull(result.get("test2"));
		assertTrue(((Number) result.get("test2")).intValue() != ((Number) result.get("test1")).intValue());
	}

	@Test
	public void testLEVariable() {
		// TODO: Currently, the model returned by the search is null if the constraint is already satisfied, 
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 235086,
		        -1000000, 1000000), Comparator.LE, new IntegerVariable("test2", 0,
		        -1000000, 1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertNotNull(result.get("test2"));
		assertTrue(((Number) result.get("test2")).intValue() >= ((Number) result.get("test1")).intValue());
	}

	@Test
	public void testLTVariable() {
		// TODO: Currently, the model returned by the search is null if the constraint is already satisfied, 
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 235086,
		        -1000000, 1000000), Comparator.LT, new IntegerVariable("test2", 0,
		        -1000000, 1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertNotNull(result.get("test2"));
		assertTrue(((Number) result.get("test2")).intValue() > ((Number) result.get("test1")).intValue());
	}

	@Test
	public void testGEVariable() {
		// TODO: Currently, the model returned by the search is null if the constraint is already satisfied, 
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 0, -1000000,
		        1000000), Comparator.GE, new IntegerVariable("test2", 1, -1000000,
		        1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertNotNull(result.get("test2"));
		assertTrue(((Number) result.get("test2")).intValue() <= ((Number) result.get("test1")).intValue());
	}

	@Test
	public void testGTVariable() {
		// TODO: Currently, the model returned by the search is null if the constraint is already satisfied, 
		// so in this example the concrete value has to be the target initially
		List<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		constraints.add(new IntegerConstraint(new IntegerVariable("test1", 0, -1000000,
		        1000000), Comparator.GT, new IntegerVariable("test2", 0, -1000000,
		        1000000)));

		Seeker skr = new Seeker();
		Map<String, Object> result = skr.getModel(constraints);
		assertNotNull(result);
		assertNotNull(result.get("test1"));
		assertNotNull(result.get("test2"));
		assertTrue(((Number) result.get("test2")).intValue() < ((Number) result.get("test1")).intValue());
	}

}
