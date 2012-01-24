/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.ga.LocalSearchObjective;

/**
 * @author fraser
 * 
 */
public class FloatLocalSearch<T extends Number> implements LocalSearch {

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
		doSearch(test, statement, objective, 1.0, 2, p);

		int maxPrecision = p.getValue().getClass().equals(Float.class) ? 7 : 15;
		for (int precision = 1; precision <= maxPrecision; precision++) {
			logger.debug("Current precision: " + precision);
			doSearch(test, statement, objective, Math.pow(10.0, -precision), 2, p);
		}

		logger.info("Finished local search with result " + p.getCode());
	}

	private boolean doSearch(ExecutableChromosome test, int statement,
	        LocalSearchObjective objective, double initialDelta, double factor,
	        NumericalPrimitiveStatement<T> p) {

		boolean changed = false;

		oldValue = p.getValue();
		ExecutionResult oldResult = test.getLastExecutionResult();

		boolean done = false;
		while (!done) {
			done = true;
			// Try +1
			logger.debug("Trying increment of " + p.getCode());
			p.increment(initialDelta);
			//logger.info(" -> " + p.getCode());
			if (objective.hasImproved(test)) {
				done = false;
				changed = true;

				iterate(factor * initialDelta, factor, objective, test, p, statement);
				oldValue = p.getValue();
				oldResult = test.getLastExecutionResult();

			} else {
				// Restore original, try -1
				p.setValue(oldValue);
				test.setLastExecutionResult(oldResult);
				test.setChanged(false);

				logger.debug("Trying decrement of " + p.getCode());
				p.increment(-initialDelta);
				//logger.info(" -> " + p.getCode());
				if (objective.hasImproved(test)) {
					changed = true;
					done = false;
					iterate(-factor * initialDelta, factor, objective, test, p, statement);
					oldValue = p.getValue();
					oldResult = test.getLastExecutionResult();

				} else {
					p.setValue(oldValue);
					test.setLastExecutionResult(oldResult);
					test.setChanged(false);
				}
			}
		}

		logger.info("Finished local search with result " + p.getCode());
		return changed;
	}

	private boolean iterate(double delta, double factor, LocalSearchObjective objective,
	        ExecutableChromosome test, NumericalPrimitiveStatement<T> p, int statement) {

		boolean improvement = false;
		T oldValue = p.getValue();
		ExecutionResult oldResult = test.getLastExecutionResult();
		logger.debug("Trying increment " + delta + " of " + p.getCode());

		p.increment(delta);
		while (objective.hasImproved(test)) {
			oldValue = p.getValue();
			oldResult = test.getLastExecutionResult();
			test.setChanged(false);
			improvement = true;
			delta = factor * delta;
			//if (delta > 1)
			//	return improvement;
			logger.debug("Trying increment " + delta + " of " + p.getCode());
			p.increment(delta);
		}

		p.setValue(oldValue);
		test.setLastExecutionResult(oldResult);
		test.setChanged(false);

		return improvement;

	}
}
