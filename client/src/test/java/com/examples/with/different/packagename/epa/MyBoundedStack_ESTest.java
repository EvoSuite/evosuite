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
package com.examples.with.different.packagename.epa;

import static org.junit.Assert.fail;

import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class)
@EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true)
public class MyBoundedStack_ESTest extends MyBoundedStack_ESTest_scaffolding {

	@Test
	public void test0() throws Throwable {
		MyBoundedStack st = new MyBoundedStack();
		st.push(new Object());
		st.pop();
	}

	@Test
	public void test1() throws Throwable {
		MyBoundedStack st = new MyBoundedStack();
		st.push(new Object());
		st.push(new Object());
		st.push(new Object());
		st.push(new Object());
		st.push(new Object());
		st.push(new Object());
		st.push(new Object());
		st.push(new Object());
		st.push(new Object());
		st.push(new Object());
	}

	@Test
	public void test3() throws Throwable {
		MyBoundedStack st = new MyBoundedStack();
		try {
			st.pop();
			fail();
		} catch (IllegalStateException ex) {

		}
	}

	@Test
	public void test4() throws Throwable {
		MyBoundedStack st = new MyBoundedStack();
		st.push(new Object());
		st.push(new Object());
		st.push(new Object());
		st.push(new Object());
		st.push(new Object());
		st.push(new Object());
		st.push(new Object());
		st.push(new Object());
		st.push(new Object());
		st.push(new Object());
		try {
			st.push(new Object());
			fail();
		} catch (IllegalStateException ex) {

		}
	}
}
