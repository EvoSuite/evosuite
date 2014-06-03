package org.evosuite.localsearch;

import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.DSEType;
import org.evosuite.testcase.ArrayStatement;
import org.evosuite.testcase.ConstructorStatement;
import org.evosuite.testcase.EnumPrimitiveStatement;
import org.evosuite.testcase.FieldStatement;
import org.evosuite.testcase.MethodStatement;
import org.evosuite.testcase.NullStatement;
import org.evosuite.testcase.PrimitiveStatement;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.TestChromosome;

public abstract class StatementLocalSearch {

	private TestChromosome backup = null;

	protected void backup(TestChromosome test) {
		backup = (TestChromosome) test.clone();
	}

	protected void restore(TestChromosome test) {
		if (backup == null)
			return;

		test.setTestCase(backup.getTestCase().clone());
		test.copyCachedResults(backup);
		test.setFitness(backup.getFitness());
		test.setChanged(backup.isChanged());
	}

	
	/**
	 * <p>
	 * doSearch
	 * </p>
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestChromosome} object.
	 * @param statement
	 *            a int.
	 * @param objective
	 *            a {@link org.evosuite.localsearch.LocalSearchObjective} object.
	 */
	public abstract boolean doSearch(TestChromosome test, int statement,
	        LocalSearchObjective<TestChromosome> objective);
	
	
	
	public boolean doSearch(TestChromosome test, Set<Integer> statements,
	        LocalSearchObjective<TestChromosome> objective) {
		boolean success = false;
		for(Integer statement : statements) {
			if(doSearch(test, statement, objective))
				success = true;
		}
		
		return success;
	}

	/**
	 * If the position of the statement on which the local search was performed
	 * has changed, then we need to tell this to the outside world
	 * 
	 * @return
	 */
	public int getPositionDelta() {
		return 0;
	}

	public static StatementLocalSearch getLocalSearchFor(StatementInterface statement) {
		StatementLocalSearch search = null;
		if (statement instanceof NullStatement) {
			if (Properties.LOCAL_SEARCH_REFERENCES == false)
				return null;

			search = new ReferenceLocalSearch();
			//search = new NullReferenceSearch();
		} else if (statement instanceof PrimitiveStatement<?>) {
			if(Properties.LOCAL_SEARCH_DSE == DSEType.STATEMENT)
				return new DSELocalSearch();

			Class<?> type = statement.getReturnValue().getVariableClass();
			if (type.equals(String.class)) {
				if(Properties.LOCAL_SEARCH_STRINGS)
					search = new StringLocalSearch();
			} else {
				if (Properties.LOCAL_SEARCH_PRIMITIVES == false)
					return null;


			if (type.equals(Integer.class) || type.equals(int.class)) {
				search = new IntegerLocalSearch<Integer>();
			} else if (type.equals(Byte.class) || type.equals(byte.class)) {
				search = new IntegerLocalSearch<Byte>();
			} else if (type.equals(Short.class) || type.equals(short.class)) {
				search = new IntegerLocalSearch<Short>();
			} else if (type.equals(Long.class) || type.equals(long.class)) {
				search = new IntegerLocalSearch<Long>();
			} else if (type.equals(Character.class) || type.equals(char.class)) {
				search = new IntegerLocalSearch<Character>();
			} else if (type.equals(Float.class) || type.equals(float.class)) {
				search = new FloatLocalSearch<Float>();
			} else if (type.equals(Double.class) || type.equals(double.class)) {
				search = new FloatLocalSearch<Double>();
			} else if (type.equals(Boolean.class)) {
				search = new BooleanLocalSearch();
			} else if (statement instanceof EnumPrimitiveStatement) {
				search = new EnumLocalSearch();
			}
			}
		} else if (statement instanceof ArrayStatement) {
			if (Properties.LOCAL_SEARCH_ARRAYS == false)
				return null;

			search = new ArrayLocalSearch();
		} else if (statement instanceof MethodStatement) {
			if (Properties.LOCAL_SEARCH_REFERENCES == false)
				return null;

			search = new ReferenceLocalSearch();
		} else if (statement instanceof ConstructorStatement) {
			if (Properties.LOCAL_SEARCH_REFERENCES == false)
				return null;

			search = new ReferenceLocalSearch();
		} else if (statement instanceof FieldStatement) {
			if (Properties.LOCAL_SEARCH_REFERENCES == false)
				return null;

			search = new ReferenceLocalSearch();
		}
		return search;
	}
}
