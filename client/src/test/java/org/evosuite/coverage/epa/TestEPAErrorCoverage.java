package org.evosuite.coverage.epa;

import org.evosuite.Properties.Criterion;

public class TestEPAErrorCoverage extends TestEPACoverage {

	@Override
	protected Criterion[] getCriteria() {
		return new Criterion[] { Criterion.EPAERROR };
	}

}
