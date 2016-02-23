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

import java.util.Arrays;

import org.evosuite.Properties;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class StringAVMLocalSearch extends StatementLocalSearch {

	private static final Logger logger = LoggerFactory.getLogger(StringLocalSearch.class);

	private String oldValue;

	private ExecutionResult oldResult;

	private boolean oldChanged;

	private void backup(TestChromosome test, StringPrimitiveStatement p) {
		oldValue = new String(p.getValue());
		oldResult = test.getLastExecutionResult();
		oldChanged = test.isChanged();
	}

	private void restore(TestChromosome test, StringPrimitiveStatement p) {
		p.setValue(new String(oldValue));
		test.setLastExecutionResult(oldResult);
		test.setChanged(oldChanged);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.LocalSearch#doSearch(org.evosuite.testcase.TestChromosome, int, org.evosuite.ga.LocalSearchObjective)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean doSearch(TestChromosome test, int statement,
	        LocalSearchObjective<TestChromosome> objective) {
		StringPrimitiveStatement p = (StringPrimitiveStatement) test.getTestCase().getStatement(statement);
		backup(test, p);
		
		// TODO: First apply 10 random mutations to determine if string influences _uncovered_ branch

		boolean affected = false;
		String oldValue = p.getValue();
		
		
		for (int i = 0; i < Properties.LOCAL_SEARCH_PROBES; i++) {
			if (Randomness.nextDouble() > 0.5)
				p.increment();
			else
				p.randomize();

			logger.info("Probing string " + oldValue + " ->" + p.getCode());
			int result = objective.hasChanged(test);
			if (result < 0) {
				backup(test, p);
			} else {
				restore(test, p);
			}
			if (result != 0) {
				affected = true;
				logger.info("String affects fitness");
				break;
			}
		}

		if (affected) {

			boolean hasImproved = false;

			logger.info("Applying local search to string " + p.getCode());
			// First try to remove each of the characters
			logger.info("Removing characters");
			if (removeCharacters(objective, test, p, statement))
				hasImproved = true;
			logger.info("Statement: " + p.getCode());

			// Second, try to replace each of the characters with each of the 64 possible characters
			logger.info("Replacing characters");
			if (replaceCharacters(objective, test, p, statement))
				hasImproved = true;
			logger.info("Statement: " + p.getCode());

			// Third, try to add characters
			logger.info("Adding characters");
			if (addCharacters(objective, test, p, statement))
				hasImproved = true;
			logger.info("Statement: " + p.getCode());

			logger.info("Resulting string: " + p.getValue());
			return hasImproved;
			//} else {
			//	logger.info("Not applying local search to string as it does not improve fitness");
		}

		return false;
	}

	private boolean removeCharacters(LocalSearchObjective<TestChromosome> objective,
	        TestChromosome test, StringPrimitiveStatement p, int statement) {

		boolean improvement = false;
		backup(test, p);

		for (int i = oldValue.length() - 1; i >= 0; i--) {
			String newString = oldValue.substring(0, i) + oldValue.substring(i + 1);
			p.setValue(newString);
			logger.info(" " + i + " " + oldValue + "/" + oldValue.length() + " -> "
			        + newString + "/" + newString.length());
			if (objective.hasImproved(test)) {
				logger.info("Has improved");
				backup(test, p);
				improvement = true;
			} else {
				logger.info("Has not improved");
				restore(test, p);
			}
		}

		return improvement;
	}

	private boolean replaceCharacters(LocalSearchObjective<TestChromosome> objective,
	        TestChromosome test, StringPrimitiveStatement p, int statement) {

		logger.info(" -> In replacement");
		boolean improvement = false;
		backup(test, p);
		for (int i = 0; i < oldValue.length(); i++) {

			boolean done = false;
			while (!done) {
				done = true;
				// Try +1
				logger.debug("Trying increment of " + p.getCode());

				char oldChar = oldValue.charAt(i);
				logger.info(" -> Character " + i + ": " + oldChar);
				char[] characters = oldValue.toCharArray();
				char replacement = oldChar;

				replacement += 1;
				characters[i] = replacement;
				String newString = new String(characters);
				p.setValue(newString);
				logger.info(" " + i + " " + oldValue + "/" + oldValue.length() + " -> "
				        + newString + "/" + newString.length());

				if (objective.hasImproved(test)) {
					done = false;

					iterate(2, objective, test, p, i, statement);
					oldValue = p.getValue();
					oldResult = test.getLastExecutionResult();

				} else {
					// Restore original, try -1
					p.setValue(oldValue);
					test.setLastExecutionResult(oldResult);
					test.setChanged(false);

					logger.debug("Trying decrement of " + p.getCode());
					replacement -= 2;
					characters[i] = replacement;
					newString = new String(characters);
					p.setValue(newString);
					logger.info(" " + i + " " + oldValue + "/" + oldValue.length()
					        + " -> " + newString + "/" + newString.length());

					if (objective.hasImproved(test)) {
						done = false;
						iterate(-2, objective, test, p, i, statement);
						oldValue = p.getValue();
						oldResult = test.getLastExecutionResult();

					} else {
						p.setValue(oldValue);
						test.setLastExecutionResult(oldResult);
						test.setChanged(false);
					}
				}
			}
		}

		return improvement;
	}

	private boolean iterate(long delta, LocalSearchObjective<TestChromosome> objective,
	        TestChromosome test, StringPrimitiveStatement p, int character,
	        int statement) {

		boolean improvement = false;
		oldValue = p.getValue();
		ExecutionResult oldResult = test.getLastExecutionResult();

		logger.debug("Trying increment " + delta + " of " + p.getCode());
		char oldChar = oldValue.charAt(character);
		logger.info(" -> Character " + character + ": " + oldChar);
		char[] characters = oldValue.toCharArray();
		char replacement = oldChar;

		replacement += delta;
		characters[character] = replacement;
		String newString = new String(characters);
		p.setValue(newString);

		while (objective.hasImproved(test)) {
			oldValue = p.getValue();
			oldResult = test.getLastExecutionResult();
			improvement = true;
			delta = 2 * delta;
			logger.info(" " + character + " " + oldValue + "/" + oldValue.length()
			        + " -> " + newString + "/" + newString.length());
			replacement += delta;
			characters[character] = replacement;
			newString = new String(characters);
			p.setValue(newString);
		}
		logger.debug("No improvement on " + p.getCode());

		p.setValue(oldValue);
		test.setLastExecutionResult(oldResult);
		test.setChanged(false);
		logger.debug("Final value of this iteration: " + p.getValue());

		return improvement;

	}

	private boolean addCharacters(LocalSearchObjective<TestChromosome> objective,
	        TestChromosome test, StringPrimitiveStatement p, int statement) {

		boolean improvement = false;
		backup(test, p);

		boolean add = true;

		while (add) {
			add = false;
			int position = oldValue.length();
			char[] characters = Arrays.copyOf(oldValue.toCharArray(), position + 1);
			for (char replacement = 9; replacement < 128; replacement++) {
				characters[position] = replacement;
				String newString = new String(characters);
				p.setValue(newString);
				//logger.debug(" " + oldValue + "/" + oldValue.length() + " -> " + newString
				//        + "/" + newString.length());

				if (objective.hasImproved(test)) {
					backup(test, p);
					improvement = true;
					add = true;
					break;
				} else {
					restore(test, p);
				}
			}
		}

		add = true;
		while (add) {
			add = false;
			int position = 0;
			char[] characters = (" " + oldValue).toCharArray();
			for (char replacement = 9; replacement < 128; replacement++) {
				characters[position] = replacement;
				String newString = new String(characters);
				p.setValue(newString);
				//logger.debug(" " + oldValue + "/" + oldValue.length() + " -> " + newString
				//        + "/" + newString.length());

				if (objective.hasImproved(test)) {
					backup(test, p);
					improvement = true;
					add = true;
					break;
				} else {
					restore(test, p);
				}
			}
		}

		return improvement;
	}

}
