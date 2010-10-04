/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */


package de.unisb.cs.st.evosuite.testcase;

import de.unisb.cs.st.evosuite.assertion.Assertion;

/**
 * Abstract base class of execution traces
 * 
 * @author Gordon Fraser
 *
 */
public abstract class OutputTrace {


	public abstract boolean differs(OutputTrace other);
	
	public abstract int numDiffer(OutputTrace other);
	
	public abstract int getAssertions(TestCase test, OutputTrace other);
	
	public abstract boolean isDetectedBy(Assertion assertion); 
	
}
