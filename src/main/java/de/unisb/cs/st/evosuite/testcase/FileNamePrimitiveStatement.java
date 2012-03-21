/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import org.objectweb.asm.commons.GeneratorAdapter;

import de.unisb.cs.st.evosuite.runtime.EvoSuiteFile;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * This class is a wrapper for a string that represents a filename
 * 
 * @author fraser
 * 
 */
public class FileNamePrimitiveStatement extends PrimitiveStatement<String> {

	/**
	 * @param tc
	 * @param type
	 * @param value
	 */
	public FileNamePrimitiveStatement(TestCase tc, String value) {
		super(tc, EvoSuiteFile.class, value);
		//logger.info("Selecting filename: " + value);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#delta()
	 */
	@Override
	public void delta() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#zero()
	 */
	@Override
	public void zero() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#pushBytecode(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	protected void pushBytecode(GeneratorAdapter mg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#randomize()
	 */
	@Override
	public void randomize() {
		// TODO: Check if any files were accessed
		setValue(Randomness.choice(tc.getAccessedFiles()));
		//logger.info("Randomized filename: " + value);

	}

}
