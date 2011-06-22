/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.util.Arrays;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.LocalSearchObjective;

/**
 * @author fraser
 * 
 */
public class StringLocalSearch implements LocalSearch {

	private static Logger logger = Logger.getLogger(StringLocalSearch.class);

	private String oldValue;

	private ExecutionResult oldResult;

	private boolean oldChanged;

	private void backup(ExecutableChromosome test, StringPrimitiveStatement p) {
		oldValue = new String(p.getValue());
		oldResult = test.getLastExecutionResult();
		oldChanged = test.isChanged();
	}

	private void restore(ExecutableChromosome test, StringPrimitiveStatement p) {
		p.setValue(new String(oldValue));
		test.setLastExecutionResult(oldResult);
		test.setChanged(oldChanged);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.LocalSearch#doSearch(de.unisb.cs.st.evosuite.testcase.TestChromosome, int, de.unisb.cs.st.evosuite.ga.LocalSearchObjective)
	 */
	@Override
	public void doSearch(TestChromosome test, int statement,
	        LocalSearchObjective objective) {
		StringPrimitiveStatement p = (StringPrimitiveStatement) test.test.getStatement(statement);
		backup(test, p);
		// TODO: First apply 10 random mutations to determine if string influences _uncovered_ branch

		boolean affected = false;
		logger.info("Probing string " + p.getCode());
		for (int i = 0; i < Properties.LOCAL_SEARCH_PROBES; i++) {
			p.increment();
			if (objective.hasImproved(test)) {
				affected = true;
				backup(test, p);
			} else {
				restore(test, p);
			}
		}

		if (affected) {
			logger.info("Applying local search to string " + p.getCode());
			// First try to remove each of the characters
			logger.info("Removing characters");
			removeCharacters(objective, test, p, statement);
			logger.info("Statement: " + p.getCode());

			// Second, try to replace each of the characters with each of the 64 possible characters
			logger.info("Replacing characters");
			replaceCharacters(objective, test, p, statement);
			logger.info("Statement: " + p.getCode());

			// Third, try to add characters
			logger.info("Adding characters");
			addCharacters(objective, test, p, statement);
			logger.info("Statement: " + p.getCode());

			logger.info("Resulting string: " + p.getValue());
			//} else {
			//	logger.info("Not applying local search to string as it does not improve fitness");
		}
	}

	private boolean removeCharacters(LocalSearchObjective objective, ExecutableChromosome test,
			StringPrimitiveStatement p, int statement) {

		boolean improvement = false;
		backup(test, p);

		for (int i = oldValue.length() - 1; i >= 0; i--) {
			String newString = oldValue.substring(0, i) + oldValue.substring(i + 1);
			p.setValue(newString);
			//logger.info(" " + i + " " + oldValue + "/" + oldValue.length() + " -> "
			//        + newString + "/" + newString.length());
			if (objective.hasImproved(test)) {
				backup(test, p);
				improvement = true;
			} else {
				restore(test, p);
			}
		}

		return improvement;
	}

	private boolean replaceCharacters(LocalSearchObjective objective,
	        ExecutableChromosome test, StringPrimitiveStatement p, int statement) {

		boolean improvement = false;
		backup(test, p);

		for (int i = 0; i < oldValue.length(); i++) {
			char oldChar = oldValue.charAt(i);
			char[] characters = oldValue.toCharArray();
			for (char replacement = 0; replacement < 128; replacement++) {
				if (replacement != oldChar) {
					characters[i] = replacement;
					String newString = new String(characters);
					p.setValue(newString);
					//logger.debug(" " + i + " " + oldValue + "/" + oldValue.length()
					//        + " -> " + newString + "/" + newString.length());

					if (objective.hasImproved(test)) {
						backup(test, p);
						//oldChar = replacement;
						improvement = true;
					} else {
						characters[i] = oldChar;
						restore(test, p);
					}
				}
			}
		}

		return improvement;
	}

	private boolean addCharacters(LocalSearchObjective objective, ExecutableChromosome test,
			StringPrimitiveStatement p, int statement) {

		boolean improvement = false;
		backup(test, p);

		boolean add = true;

		while (add) {
			add = false;
			int position = oldValue.length();
			char[] characters = Arrays.copyOf(oldValue.toCharArray(), position + 1);
			for (char replacement = 0; replacement < 128; replacement++) {
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
			for (char replacement = 0; replacement < 128; replacement++) {
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
