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
public class ArrayLocalSearch implements LocalSearch {

	int oldLength = 0;

	private static Logger logger = LoggerFactory.getLogger(LocalSearch.class);

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.LocalSearch#doSearch(de.unisb.cs.st.evosuite.testcase.TestChromosome, int, de.unisb.cs.st.evosuite.ga.LocalSearchObjective)
	 */
	@Override
	public void doSearch(TestChromosome test, int statement,
	        LocalSearchObjective objective) {
		ArrayStatement p = (ArrayStatement) test.test.getStatement(statement);
		ExecutionResult oldResult = test.getLastExecutionResult();
		oldLength = p.size();
		boolean done = false;
		while (!done) {
			done = true;
			// Try +1
			logger.debug("Trying increment of " + p.getCode());
			p.setSize(oldLength + 1);
			if (objective.hasImproved(test)) {
				done = false;

				boolean improved = true;
				while (improved) {
					oldLength = p.size();
					oldResult = test.getLastExecutionResult();
					p.setSize(oldLength + 1);
					improved = objective.hasImproved(test);
				}
				p.setSize(oldLength);
				test.setLastExecutionResult(oldResult);
				test.setChanged(false);

			} else if (oldLength > 0) {
				// Restore original, try -1
				p.setSize(oldLength);
				test.setLastExecutionResult(oldResult);
				test.setChanged(false);

				logger.debug("Trying decrement of " + p.getCode());
				p.setSize(oldLength - 1);
				if (objective.hasImproved(test)) {
					done = false;

					boolean improved = true;
					while (improved && p.size() > 0) {
						oldLength = p.size();
						oldResult = test.getLastExecutionResult();
						p.setSize(oldLength - 1);
						improved = objective.hasImproved(test);
					}
					p.setSize(oldLength);
					test.setLastExecutionResult(oldResult);
					test.setChanged(false);
				} else {
					p.setSize(oldLength);
					test.setLastExecutionResult(oldResult);
					test.setChanged(false);
				}
			}
		}

		logger.debug("Finished local search with result " + p.getCode());

	}

}
