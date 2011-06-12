/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.util.Arrays;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.ga.LocalSearchObjective;

/**
 * @author fraser
 * 
 */
public class StringLocalSearch implements LocalSearch {

	private static Logger logger = Logger.getLogger(StringLocalSearch.class);

	private String oldValue;

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.LocalSearch#doSearch(de.unisb.cs.st.evosuite.testcase.TestChromosome, int, de.unisb.cs.st.evosuite.ga.LocalSearchObjective)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void doSearch(TestChromosome test, int statement,
	        LocalSearchObjective objective) {
		PrimitiveStatement<String> p = (PrimitiveStatement<String>) test.test.getStatement(statement);
		oldValue = p.getValue();

		// TODO: First apply 10 random mutations to determine if string influences uncovered branch

		//logger.info("Statement: " + p.getCode());
		// First try to remove each of the characters
		//logger.info("Removing characters");
		removeCharacters(objective, test, p, statement);
		//logger.info("Statement: " + p.getCode());

		// Second, try to replace each of the characters with each of the 64 possible characters
		//logger.info("Replacing characters");
		replaceCharacters(objective, test, p, statement);
		//logger.info("Statement: " + p.getCode());

		// Third, try to add characters
		//logger.info("Adding characters");
		addCharacters(objective, test, p, statement);
		//logger.info("Statement: " + p.getCode());
	}

	private boolean removeCharacters(LocalSearchObjective objective, TestChromosome test,
	        PrimitiveStatement<String> p, int statement) {

		boolean improvement = false;
		String oldValue = p.getValue();

		for (int i = oldValue.length() - 1; i >= 0; i--) {
			String newString = oldValue.substring(0, i) + oldValue.substring(i + 1);
			p.setValue(newString);
			//logger.info(" " + i + " " + oldValue + "/" + oldValue.length() + " -> "
			//        + newString + "/" + newString.length());
			if (objective.hasImproved(test)) {
				oldValue = newString;
				improvement = true;
			} else {
				p.setValue(oldValue);
			}
		}

		return improvement;
	}

	private boolean replaceCharacters(LocalSearchObjective objective,
	        TestChromosome test, PrimitiveStatement<String> p, int statement) {

		boolean improvement = false;
		String oldValue = p.getValue();

		char[] characters = oldValue.toCharArray();

		for (int i = 0; i < oldValue.length(); i++) {
			char oldChar = oldValue.charAt(i);
			for (char replacement = 0; replacement < 128; replacement++) {
				if (replacement != oldChar) {
					characters[i] = replacement;
					String newString = new String(characters);
					p.setValue(newString);
					//logger.info(" " + i + " " + oldValue + "/" + oldValue.length()
					//        + " -> " + newString + "/" + newString.length());

					if (objective.hasImproved(test)) {
						oldValue = newString;
						oldChar = replacement;
						improvement = true;
					} else {
						characters[i] = oldChar;
						p.setValue(oldValue);
					}
				}
			}
		}

		return improvement;
	}

	private boolean addCharacters(LocalSearchObjective objective, TestChromosome test,
	        PrimitiveStatement<String> p, int statement) {

		boolean improvement = false;
		String oldValue = p.getValue();

		boolean add = true;

		while (add) {
			add = false;
			int position = oldValue.length();
			char[] characters = Arrays.copyOf(oldValue.toCharArray(), position + 1);
			for (char replacement = 0; replacement < 128; replacement++) {
				characters[position] = replacement;
				String newString = new String(characters);
				p.setValue(newString);
				//logger.info(" " + oldValue + "/" + oldValue.length() + " -> " + newString
				//        + "/" + newString.length());

				if (objective.hasImproved(test)) {
					oldValue = newString;
					improvement = true;
					add = true;
					break;
				} else {
					p.setValue(oldValue);
				}
			}
		}

		return improvement;
	}

}
