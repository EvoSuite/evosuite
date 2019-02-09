package org.evosuite.coverage.aes.method;

public class AESPublicMethodCoverageSuiteFitness extends AESMethodCoverageSuiteFitness {

	private static final long serialVersionUID = -3281621600808603551L;

	public AESPublicMethodCoverageSuiteFitness(Metric metric) {
		super(metric);
	}

	public AESPublicMethodCoverageSuiteFitness() {
		this(Metric.AES);
	}
	
	protected AESMethodCoverageFactory getFactory() {
		return new AESMethodCoverageFactory().setPublicFilter(true);
	}
}
