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
