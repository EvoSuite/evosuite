/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.ga.LocalSearchObjective;

/**
 * Local search on enum values means we simply iterate over all possible values
 * the enum can take
 * 
 * @author Gordon Fraser
 * 
 */
public class EnumLocalSearch implements LocalSearch {

	private static Logger logger = LoggerFactory.getLogger(LocalSearch.class);

	private Object oldValue;

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.LocalSearch#doSearch(de.unisb.cs.st.evosuite.testcase.TestChromosome, int, de.unisb.cs.st.evosuite.ga.LocalSearchObjective)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void doSearch(TestChromosome test, int statement,
	        LocalSearchObjective objective) {
		EnumPrimitiveStatement p = (EnumPrimitiveStatement) test.test.getStatement(statement);
		ExecutionResult oldResult = test.getLastExecutionResult();
		oldValue = p.getValue();

		for (Object value : p.getEnumValues()) {
			p.setValue(value);

			if (!objective.hasImproved(test)) {
				// Restore original
				p.setValue(oldValue);
				test.setLastExecutionResult(oldResult);
				test.setChanged(false);
			} else {
				break;
			}

		}

		logger.debug("Finished local search with result " + p.getCode());

	}

}
