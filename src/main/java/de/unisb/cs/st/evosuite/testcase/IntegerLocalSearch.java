/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.ga.LocalSearchObjective;

/**
 * @author Gordon Fraser
 * 
 */
public class IntegerLocalSearch<T> implements LocalSearch {

	private static Logger logger = LoggerFactory.getLogger(LocalSearch.class);

	private T oldValue;

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.LocalSearch#doSearch(de.unisb.cs.st.evosuite.testcase.TestChromosome, int, de.unisb.cs.st.evosuite.ga.LocalSearchObjective)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void doSearch(TestChromosome test, int statement,
	        LocalSearchObjective objective) {

		NumericalPrimitiveStatement<T> p = (NumericalPrimitiveStatement<T>) test.test.getStatement(statement);
		ExecutionResult oldResult = test.getLastExecutionResult();
		oldValue = p.getValue();

		boolean done = false;
		while (!done) {
			done = true;
			// Try +1
			logger.debug("Trying increment of " + p.getCode());
			p.increment(1);
			if (objective.hasImproved(test)) {
				done = false;

				iterate(2, objective, test, p, statement);
				oldValue = p.getValue();
				oldResult = test.getLastExecutionResult();

			} else {
				// Restore original, try -1
				p.setValue(oldValue);
				test.setLastExecutionResult(oldResult);
				test.setChanged(false);

				logger.debug("Trying decrement of " + p.getCode());
				p.increment(-1);
				if (objective.hasImproved(test)) {
					done = false;
					iterate(-2, objective, test, p, statement);
					oldValue = p.getValue();
					oldResult = test.getLastExecutionResult();

				} else {
					p.setValue(oldValue);
					test.setLastExecutionResult(oldResult);
					test.setChanged(false);
				}
			}
		}

		logger.debug("Finished local search with result " + p.getCode());
	}

	private boolean iterate(long delta, LocalSearchObjective objective,
	        ExecutableChromosome test, NumericalPrimitiveStatement<T> p, int statement) {

		boolean improvement = false;
		T oldValue = p.getValue();
		ExecutionResult oldResult = test.getLastExecutionResult();

		logger.debug("Trying increment " + delta + " of " + p.getCode());

		p.increment(delta);
		while (objective.hasImproved(test)) {
			oldValue = p.getValue();
			oldResult = test.getLastExecutionResult();
			improvement = true;
			delta = 2 * delta;
			logger.debug("Trying increment " + delta + " of " + p.getCode());
			p.increment(delta);
		}
		logger.debug("No improvement on " + p.getCode());

		p.setValue(oldValue);
		test.setLastExecutionResult(oldResult);
		test.setChanged(false);
		logger.debug("Final value of this iteration: " + p.getValue());

		return improvement;

	}

}
