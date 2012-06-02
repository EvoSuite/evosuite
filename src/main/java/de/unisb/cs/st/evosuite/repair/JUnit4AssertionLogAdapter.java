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
/**
 * 
 */
package de.unisb.cs.st.evosuite.repair;

import java.util.Map;

import de.unisb.cs.st.evosuite.junit.JUnit4TestAdapter;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * @author Gordon Fraser
 * 
 */
public class JUnit4AssertionLogAdapter extends JUnit4TestAdapter {
	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.junit.UnitTestAdapter#getImports()
	 */
	@Override
	public String getImports() {
		return "import org.junit.Before;\n" + "import org.junit.Ignore;\n"
		        + "import org.junit.Test;\n"
		        + "import static de.unisb.cs.st.evosuite.repair.AssertionLogger.*;\n";
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.junit.UnitTestAdapter#getTestString(de.unisb.cs.st.evosuite.testcase.TestCase, java.util.Map)
	 */
	@Override
	public String getTestString(int id, TestCase test, Map<Integer, Throwable> exceptions) {
		AssertionLogTestVisitor visitor = new AssertionLogTestVisitor(id);
		visitor.setExceptions(exceptions);
		test.accept(visitor);
		return visitor.getCode();
	}
}
