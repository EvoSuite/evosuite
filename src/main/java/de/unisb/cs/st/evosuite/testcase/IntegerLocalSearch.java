/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.ga.LocalSearchObjective;

/**
 * @author Gordon Fraser
 * 
 */
public class IntegerLocalSearch<T> implements LocalSearch {

	private static Logger logger = Logger.getLogger(LocalSearch.class);

	private T oldValue;

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.LocalSearch#doSearch(de.unisb.cs.st.evosuite.testcase.TestChromosome, int, de.unisb.cs.st.evosuite.ga.LocalSearchObjective)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void doSearch(TestChromosome test, int statement,
	        LocalSearchObjective objective) {

		PrimitiveStatement<T> p = (PrimitiveStatement<T>) test.test.getStatement(statement);
		ExecutionResult oldResult = test.last_result;
		oldValue = p.getValue();

		// Try +1
		//logger.info("Trying increment of " + p.getCode());
		p.increment();
		if (objective.hasImproved(test)) {
			logger.debug("Starting local search on " + p.getCode() + " (was: " + oldValue
			        + ")");
			while (iterate(1, objective, test, p, statement))
				;

		} else {
			// Restore original, try -1
			p.setValue(oldValue);
			test.last_result = oldResult;
			test.setChanged(false);
			//logger.info("Trying decrement of " + p.getCode());
			p.decrement();
			if (objective.hasImproved(test)) {
				logger.debug("Starting local search on " + p.getCode() + " (was: "
				        + oldValue + ")");
				while (iterate(-1, objective, test, p, statement))
					;

			} else {
				p.setValue(oldValue);
				test.last_result = oldResult;
				test.setChanged(false);
			}
		}
		logger.debug("Finished local search with result " + p.getCode());
	}

	private boolean iterate(long delta, LocalSearchObjective objective,
	        TestChromosome test, PrimitiveStatement<T> p, int statement) {

		boolean improvement = false;
		T oldValue = p.getValue();
		ExecutionResult oldResult = test.last_result;

		logger.debug("Trying increment " + delta + " of " + p.getCode());

		p.increment(delta);
		while (objective.hasImproved(test)) {
			oldValue = p.getValue();
			oldResult = test.last_result;
			improvement = true;
			delta = 2 * delta;
			logger.debug("Trying increment " + delta + " of " + p.getCode());
			p.increment(delta);
		}
		logger.debug("No improvement on " + p.getCode());

		p.setValue(oldValue);
		test.last_result = oldResult;
		test.setChanged(false);
		logger.debug("Final value of this iteration: " + p.getValue());

		return improvement;

	}

}
