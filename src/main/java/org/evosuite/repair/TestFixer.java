
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
 *
 * @author Gordon Fraser
 */
package org.evosuite.repair;

import java.io.File;
public class TestFixer {
	
	protected final File projectDir;
	
	protected final File fixedTestSrcDir;
	
	protected final File fixedTestClassDir;

	/**
	 * <p>Constructor for TestFixer.</p>
	 *
	 * @param projectDir a {@link java.io.File} object.
	 * @param fixedTestSrcDir a {@link java.io.File} object.
	 * @param fixedTestClassDir a {@link java.io.File} object.
	 */
	public TestFixer(File projectDir, File fixedTestSrcDir,
			File fixedTestClassDir) {
		super();
		this.projectDir = projectDir;
		this.fixedTestSrcDir = fixedTestSrcDir;
		this.fixedTestClassDir = fixedTestClassDir;
	}
	
	/**
	 * <p>fixNonCompilingTest</p>
	 *
	 * @param testSrc a {@link java.io.File} object.
	 */
	public void fixNonCompilingTest(File testSrc) {
		// TODO implement glue for Stefan's code
	}
	
	/**
	 * <p>fixFailedAssertion</p>
	 *
	 * @param testSrc a {@link java.io.File} object.
	 */
	public void fixFailedAssertion(File testSrc) {
		// TODO implement glue for Stefan's code
	}
	
}
