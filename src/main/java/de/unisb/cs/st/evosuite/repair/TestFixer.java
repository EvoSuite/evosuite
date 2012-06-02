/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
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
package de.unisb.cs.st.evosuite.repair;

import java.io.File;

public class TestFixer {
	
	protected final File projectDir;
	
	protected final File fixedTestSrcDir;
	
	protected final File fixedTestClassDir;

	public TestFixer(File projectDir, File fixedTestSrcDir,
			File fixedTestClassDir) {
		super();
		this.projectDir = projectDir;
		this.fixedTestSrcDir = fixedTestSrcDir;
		this.fixedTestClassDir = fixedTestClassDir;
	}
	
	public void fixNonCompilingTest(File testSrc) {
		// TODO implement glue for Stefan's code
	}
	
	public void fixFailedAssertion(File testSrc) {
		// TODO implement glue for Stefan's code
	}
	
}
