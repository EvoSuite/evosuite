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

import java.util.HashSet;
import java.util.Set;

import org.evosuite.ga.LocalSearchObjective;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ArrayLocalSearch class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class ArrayLocalSearch implements LocalSearch {

	private int oldLength = 0;

	private static final Logger logger = LoggerFactory.getLogger(LocalSearch.class);

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.LocalSearch#doSearch(org.evosuite.testcase.TestChromosome, int, org.evosuite.ga.LocalSearchObjective)
	 */
	/** {@inheritDoc} */
	@Override
	public void doSearch(TestChromosome test, int statement,
	        LocalSearchObjective objective) {
		ArrayStatement p = (ArrayStatement) test.test.getStatement(statement);
		searchLength(test, statement, objective);

		Set<Integer> assignments = getAssignments(p, test.getTestCase());
		for (int position = statement; position < test.size(); position++) {
			if (assignments.contains(position))
				continue;

			// Insert a 0 assignment to this index
			// Perform AVM on element
		}

		logger.debug("Finished local search with result " + p.getCode());
	}

	private Set<Integer> getAssignments(ArrayStatement statement, TestCase test) {
		ArrayReference arrRef = (ArrayReference) statement.getReturnValue();
		Set<Integer> assignments = new HashSet<Integer>();
		int position = statement.getPosition() + 1;

		while (position < test.size()) {
			StatementInterface st = test.getStatement(position);
			if (st instanceof AssignmentStatement) {
				if (st.getReturnValue() instanceof ArrayIndex) {
					ArrayIndex arrayIndex = (ArrayIndex) st.getReturnValue();
					if (arrayIndex.getArray().equals(arrRef)) {
						assignments.add(arrayIndex.getArrayIndex());
					}
				}
			} else if (st instanceof PrimitiveStatement) {
				// OK, ignore
			} else {
				break;
			}
			position++;
		}

		return assignments;
	}

	private void searchLength(TestChromosome test, int statement,
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
