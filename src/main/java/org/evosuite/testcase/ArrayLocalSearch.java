/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.testcase;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.LocalSearchObjective;
import org.evosuite.testsuite.TestCaseExpander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ArrayLocalSearch class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class ArrayLocalSearch extends LocalSearch {

	private int oldLength = 0;

	private static final Logger logger = LoggerFactory.getLogger(LocalSearch.class);

	private TestChromosome backup = null;

	private void backup(TestChromosome test) {
		backup = (TestChromosome) test.clone();
	}

	private void restore(TestChromosome test) {
		if (backup == null)
			return;

		test.lastExecutionResult = backup.lastExecutionResult.clone();
		test.test = backup.test.clone();
		test.setFitness(backup.getFitness());
		test.setChanged(backup.isChanged());

		// TODO: Deep copy
		test.lastMutationResult = backup.lastMutationResult;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.LocalSearch#doSearch(org.evosuite.testcase.TestChromosome, int, org.evosuite.ga.LocalSearchObjective)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean doSearch(TestChromosome test, int statement,
	        LocalSearchObjective objective) {

		boolean hasImproved = false;
		ArrayStatement p = (ArrayStatement) test.test.getStatement(statement);

		stripAssignments(p, test, objective);
		hasImproved = searchLength(test, statement, objective);
		TestCaseExpander expander = new TestCaseExpander();
		int lengthWithoutAssignments = test.size();
		p = (ArrayStatement) test.test.getStatement(statement);
		expander.visitArrayStatement(test.getTestCase(), p);
		int assignmentLength = test.size() - lengthWithoutAssignments;
		for (int position = statement + 1; position < statement + assignmentLength; position++) {
			logger.debug("Doing local search on statement " + position);
			LocalSearch search = LocalSearch.getLocalSearchFor(test.getTestCase().getStatement(position));
			if (search != null) {
				if (search.doSearch(test, position, objective))
					hasImproved = true;
			}
		}

		logger.debug("Finished local search with result {}", p.getCode());
		return hasImproved;
	}

	private void stripAssignments(ArrayStatement statement, TestChromosome test,
	        LocalSearchObjective objective) {
		ArrayReference arrRef = (ArrayReference) statement.getReturnValue();
		TestFactory factory = TestFactory.getInstance();
		for (int position = test.size() - 1; position >= 0; position--) {
			if (test.getTestCase().getStatement(position) instanceof AssignmentStatement) {
				AssignmentStatement assignment = (AssignmentStatement) test.getTestCase().getStatement(position);
				if (assignment.getReturnValue().getAdditionalVariableReference() == arrRef) {
					backup(test);
					try {
						factory.deleteStatement(test.getTestCase(), position);
						if (!objective.hasNotWorsened(test))
							restore(test);
					} catch (ConstructionFailedException e) {
						restore(test);
					}
				}
			}
		}
	}

	private boolean searchLength(TestChromosome test, int statement,
	        LocalSearchObjective objective) {

		boolean hasImproved = false;

		ArrayStatement p = (ArrayStatement) test.test.getStatement(statement);
		logger.debug("Performing local search on array length, starting with length {}",
		             p.size());
		ExecutionResult oldResult = test.getLastExecutionResult();
		oldLength = p.size();
		boolean done = false;
		while (!done) {
			done = true;
			// Try +1
			p.setSize(oldLength + 1);
			logger.debug("Trying increment of {}", p.getCode());
			if (objective.hasImproved(test)) {
				done = false;
				hasImproved = true;

				boolean improved = true;
				while (improved) {
					oldLength = p.size();
					oldResult = test.getLastExecutionResult();
					p.setSize(oldLength + 1);
					logger.debug("Trying increment of {}", p.getCode());
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

				p.setSize(oldLength - 1);
				logger.debug("Trying decrement of {}", p.getCode());
				if (objective.hasImproved(test)) {
					done = false;
					hasImproved = true;

					boolean improved = true;
					while (improved && p.size() > 0) {
						oldLength = p.size();
						oldResult = test.getLastExecutionResult();
						p.setSize(oldLength - 1);
						logger.debug("Trying decrement of {}", p.getCode());
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

		logger.debug("Finished local array length search with result {}", p.getCode());
		return hasImproved;
	}

}
