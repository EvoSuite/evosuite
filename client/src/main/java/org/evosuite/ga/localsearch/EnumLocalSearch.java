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
package org.evosuite.ga.localsearch;

import org.evosuite.testcase.EnumPrimitiveStatement;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local search on enum values means we simply iterate over all possible values
 * the enum can take
 * 
 * @author Gordon Fraser
 */
public class EnumLocalSearch extends StatementLocalSearch {

	private static final Logger logger = LoggerFactory.getLogger(TestCaseLocalSearch.class);

	private Object oldValue;

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.LocalSearch#doSearch(org.evosuite.testcase.TestChromosome, int, org.evosuite.ga.LocalSearchObjective)
	 */
	/** {@inheritDoc} */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public boolean doSearch(TestChromosome test, int statement,
	        LocalSearchObjective<TestChromosome> objective) {
		EnumPrimitiveStatement p = (EnumPrimitiveStatement) test.getTestCase().getStatement(statement);
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
				logger.debug("Finished local search with result " + p.getCode());
				return true;
			}

		}

		return false;

	}

}
