/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.testcase.localsearch;

import org.evosuite.Properties;
import org.evosuite.ga.localsearch.LocalSearch;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.NullStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * LocalSearch interface.
 * </p>
 * 
 * @author Gordon Fraser
 */
public abstract class TestCaseLocalSearch implements LocalSearch<TestChromosome> {

	protected static final Logger logger = LoggerFactory.getLogger(TestCaseLocalSearch.class);
	

	public static TestCaseLocalSearch getLocalSearch() {
		if(Properties.LOCAL_SEARCH_SELECTIVE) {
			return new SelectiveTestCaseLocalSearch();
		} else {
			return new StandardTestCaseLocalSearch();
		}
	}
	
	public static void randomizePrimitives(TestCase test) {
		for(Statement s : test) {
			if(s instanceof PrimitiveStatement<?>) {
				if (s instanceof NullStatement) {
					continue; // ignore NullStatement
				}
				((PrimitiveStatement<?>)s).randomize();
			}
		}
	}

}
