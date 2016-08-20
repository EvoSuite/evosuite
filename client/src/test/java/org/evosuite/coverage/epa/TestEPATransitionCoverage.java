package org.evosuite.coverage.epa;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;

public class TestEPATransitionCoverage extends TestEPACoverage {

	@Override
	protected Criterion[] getCriteria() {
		return new Criterion[] { Properties.Criterion.EPATRANSITION };
	}

}
