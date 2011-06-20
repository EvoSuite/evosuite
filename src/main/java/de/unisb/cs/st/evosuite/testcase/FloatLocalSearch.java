/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.LocalSearchObjective;

/**
 * @author fraser
 * 
 */
public class FloatLocalSearch<T> implements LocalSearch {

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
		doSearch(test, statement, objective, 1.0, p);
		doSearch(test, statement, objective, Properties.EPSILON, p);
		logger.debug("Finished local search with result " + p.getCode());
	}

	private void doSearch(TestChromosome test, int statement,
	        LocalSearchObjective objective, double initialDelta, PrimitiveStatement<T> p) {

		oldValue = p.getValue();
		ExecutionResult oldResult = test.last_result;

		boolean done = false;
		while (!done) {
			done = true;
			// Try +1
			logger.debug("Trying increment of " + p.getCode());
			p.increment(initialDelta);
			if (objective.hasImproved(test)) {
				done = false;

				iterate(2 * initialDelta, objective, test, p, statement);
				oldValue = p.getValue();
				oldResult = test.last_result;

			} else {
				// Restore original, try -1
				p.setValue(oldValue);
				test.last_result = oldResult;
				test.setChanged(false);

				logger.debug("Trying decrement of " + p.getCode());
				p.increment(-initialDelta);
				if (objective.hasImproved(test)) {
					done = false;
					iterate(-2 * initialDelta, objective, test, p, statement);
					oldValue = p.getValue();
					oldResult = test.last_result;

				} else {
					p.setValue(oldValue);
					test.last_result = oldResult;
					test.setChanged(false);
				}
			}
		}

		logger.debug("Finished local search with result " + p.getCode());
	}

	private boolean iterate(double delta, LocalSearchObjective objective,
	        TestChromosome test, PrimitiveStatement<T> p, int statement) {

		boolean improvement = false;
		T oldValue = p.getValue();
		ExecutionResult oldResult = test.last_result;
		logger.debug("Trying increment " + delta + " of " + p.getCode());

		p.increment(delta);
		while (objective.hasImproved(test)) {
			oldValue = p.getValue();
			oldResult = test.last_result;
			test.setChanged(false);
			improvement = true;
			delta = 2 * delta;
			if (delta > 1)
				return improvement;
			logger.debug("Trying increment " + delta + " of " + p.getCode());
			p.increment(delta);
		}

		p.setValue(oldValue);
		test.last_result = oldResult;
		test.setChanged(false);

		return improvement;

	}

}
