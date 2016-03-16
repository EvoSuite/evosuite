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
		org.evosuite.runtime.classhandling.ClassResetter.getInstance().setClassLoader(
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
				org.evosuite.runtime.classhandling.ClassResetter.getInstance().reset(classNameToReset);
			} catch (Throwable t) {
			}
		}
	}
}
