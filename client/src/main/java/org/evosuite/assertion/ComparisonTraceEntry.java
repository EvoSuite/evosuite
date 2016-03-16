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
package org.evosuite.assertion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.Assert;

/**
 * <p>ComparisonTraceEntry class.</p>
 *
 * @author Gordon Fraser
 */
public class ComparisonTraceEntry implements OutputTraceEntry {

	private final VariableReference var;

	private final Map<VariableReference, Boolean> equalityMap = new HashMap<VariableReference, Boolean>();
	
	private final Map<Integer,VariableReference> equalityMapIntVar = new HashMap<Integer, VariableReference>();

	/**
	 * <p>Constructor for ComparisonTraceEntry.</p>
	 *
	 * @param var a {@link org.evosuite.testcase.variable.VariableReference} object.
	 */
	public ComparisonTraceEntry(VariableReference var) {
		this.var = var;
	}

	/**
	 * <p>addEntry</p>
	 *
	 * @param other a {@link org.evosuite.testcase.variable.VariableReference} object.
	 * @param value a boolean.
	 */
	public void addEntry(VariableReference other, boolean value) {
		equalityMap.put(other, value);
		equalityMapIntVar.put(other.getStPosition(),other);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.OutputTraceEntry#differs(org.evosuite.assertion.OutputTraceEntry)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean differs(OutputTraceEntry other) {
		if (other instanceof ComparisonTraceEntry) {
			if (!((ComparisonTraceEntry) other).var.equals(var))
				return false;

			ComparisonTraceEntry otherEntry = (ComparisonTraceEntry) other;
			for (VariableReference otherVar : equalityMap.keySet()) {
				if (!otherEntry.equalityMap.containsKey(otherVar))
					continue;

				if (!equals(otherEntry.equalityMap.get(otherVar), equalityMap.get(otherVar)))
					return true;
			}

		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.OutputTraceEntry#getAssertions(org.evosuite.assertion.OutputTraceEntry)
	 */
	/** {@inheritDoc} */
	@Override
	public Set<Assertion> getAssertions(OutputTraceEntry other) {
		Set<Assertion> assertions = new HashSet<Assertion>();
		if (other instanceof ComparisonTraceEntry) {
			ComparisonTraceEntry otherEntry = (ComparisonTraceEntry) other;
			for (Integer otherVar : equalityMapIntVar.keySet()) {
				if (!otherEntry.equalityMapIntVar.containsKey(otherVar))
					continue;

				if (otherVar == null)
					continue;
				if (!equals(otherEntry.equalityMap.get(otherEntry.equalityMapIntVar.get(otherVar)),
						equalityMap.get(equalityMapIntVar.get(otherVar)))) {

					EqualsAssertion assertion = new EqualsAssertion();
					assertion.source = var;
					assertion.dest = equalityMapIntVar.get(otherVar);
					assertion.value = equalityMap.get(equalityMapIntVar.get(otherVar));
					if(Properties.isRegression())
						assertion.setComment("// (Comp) Original Value: " + equalityMap.get(equalityMapIntVar.get(otherVar)) +" | Regression Value: " + otherEntry.equalityMap.get(otherEntry.equalityMapIntVar.get(otherVar)));
					assertions.add(assertion);
					assert (assertion.isValid());
				}
			}
		}
		return assertions;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.OutputTraceEntry#getAssertions()
	 */
	/** {@inheritDoc} */
	@Override
	public Set<Assertion> getAssertions() {
		Set<Assertion> assertions = new HashSet<Assertion>();

		for (VariableReference otherVar : equalityMap.keySet()) {
			if (otherVar == null)
				continue;

			EqualsAssertion assertion = new EqualsAssertion();
			assertion.source = var;
			assertion.dest = otherVar;
			assertion.value = equalityMap.get(otherVar);
			assertions.add(assertion);
			assert (assertion.isValid());
		}
		return assertions;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.OutputTraceEntry#isDetectedBy(org.evosuite.assertion.Assertion)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isDetectedBy(Assertion assertion) {
		if (assertion instanceof EqualsAssertion) {
			EqualsAssertion ass = (EqualsAssertion) assertion;
			if (ass.source.equals(var) && equalityMap.containsKey(ass.dest)) {
				return !equals(equalityMap.get(ass.dest), ass.value);
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.assertion.OutputTraceEntry#cloneEntry()
	 */
	/** {@inheritDoc} */
	@Override
	public OutputTraceEntry cloneEntry() {
		ComparisonTraceEntry copy = new ComparisonTraceEntry(var);
		copy.equalityMap.putAll(equalityMap);
		copy.equalityMapIntVar.putAll(equalityMapIntVar);
		return copy;
	}

	public static boolean equals(Object a, Object b) {
		if (a == null) {
			return b == null;
		}
		else if (b == null) {
			return false;
		}

		if(a.getClass().equals(Double.class) || a.getClass().equals(double.class)) {
			if (Double.compare((Double)a, (Double)b) == 0) {
				return true;
			}
			if ((Math.abs((Double)a- (Double)b) <= Properties.DOUBLE_PRECISION)) {
				return true;
			}
			return false;
		} else if(a.getClass().equals(Float.class) || a.getClass().equals(float.class)) {
			if (Float.compare((Float)a, (Float)b) == 0) {
				return true;
			}
			if ((Math.abs((Float)a- (Float)b) <= Properties.FLOAT_PRECISION)) {
				return true;
			}
			return false;
		} else {
			return a.equals(b);
		}
	}

}
