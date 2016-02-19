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
package org.evosuite.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;

/**
 * <p>
 * JUnitRunner class
 * </p>
 * 
 * @author José Campos
 */
public class JUnitRunner {

	/**
	 * 
	 */
	private List<JUnitResult> testResults;

	/**
	 * 
	 */
	private final Class<?> junitClass;

	/**
	 * 
	 */
	public JUnitRunner(Class<?> junitClass) {
		this.testResults = new ArrayList<JUnitResult>();
		this.junitClass = junitClass;
	}

	public void run() {
		Request request = Request.aClass(this.junitClass);

		JUnitCore junit = new JUnitCore();
		junit.addListener(new JUnitRunListener(this));
		junit.run(request);
	}

	/**
	 * 
	 * @param testResult
	 */
	public void addResult(JUnitResult testResult) {
		this.testResults.add(testResult);
	}

	/**
	 * 
	 * @return
	 */
	public List<JUnitResult> getTestResults() {
		return this.testResults;
	}

	/**
	 * 
	 * @return
	 */
	public Class<?> getJUnitClass() {
		return this.junitClass;
	}
}
