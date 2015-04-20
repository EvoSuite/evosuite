package org.evosuite.junit.rules;

import java.util.Arrays;

import org.evosuite.TestGenerationContext;

/**
 * Should be used as MethodRule
 */
public class StaticStateResetter extends BaseRule {

	private String[] classNames;
	
	public StaticStateResetter(String... classesToReset) {
		classNames = Arrays.copyOf(classesToReset, classesToReset.length);
		org.evosuite.Properties.RESET_STATIC_FIELDS = true;
		
		/*
		 * FIXME: tmp hack done during refactoring
		 */
		org.evosuite.runtime.reset.ClassResetter.getInstance().setClassLoader(
				TestGenerationContext.getInstance().getClassLoaderForSUT());
	}
	
	@Override
	protected void before() {
	}

	@Override
	protected void after() {
		for (int i=0; i< classNames.length;i++) {
			String classNameToReset = classNames[i];
			try {
				org.evosuite.runtime.reset.ClassResetter.getInstance().reset(classNameToReset); 
			} catch (Throwable t) {
			}
		}
	}
}
