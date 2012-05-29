/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
public class FileNamePrimitiveStatement extends PrimitiveStatement<EvoSuiteFile> {

	private static final long serialVersionUID = 4402006999670328128L;

	/**
	 * @param tc
	 * @param type
	 * @param value
	 */
	public FileNamePrimitiveStatement(TestCase tc, EvoSuiteFile value) {
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
		setValue(new EvoSuiteFile(Randomness.choice(tc.getAccessedFiles())));
		//logger.info("Randomized filename: " + value);

	}

}
