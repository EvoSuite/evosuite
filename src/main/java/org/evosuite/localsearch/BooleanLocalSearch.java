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
package org.evosuite.localsearch;

import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.PrimitiveStatement;
import org.evosuite.testcase.TestChromosome;

/**
 * <p>
 * BooleanLocalSearch class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class BooleanLocalSearch extends StatementLocalSearch {

	private boolean oldValue;

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.LocalSearch#doSearch(org.evosuite.testcase.TestChromosome, int, org.evosuite.ga.LocalSearchObjective)
	 */
	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public boolean doSearch(TestChromosome test, int statement,
	        LocalSearchObjective<TestChromosome> objective) {

		PrimitiveStatement<Boolean> p = (PrimitiveStatement<Boolean>) test.getTestCase().getStatement(statement);
		ExecutionResult oldResult = test.getLastExecutionResult();
		oldValue = p.getValue();

		p.setValue(!oldValue);

		if (!objective.hasImproved(test)) {
			// Restore original
			p.setValue(oldValue);
			test.setLastExecutionResult(oldResult);
			test.setChanged(false);
			return false;
		} else {
			return true;
		}
	}

}
