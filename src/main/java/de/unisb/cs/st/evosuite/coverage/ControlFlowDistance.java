/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage;

import de.unisb.cs.st.evosuite.ga.FitnessFunction;

/**
 * @author Gordon Fraser, Andre Mis
 * 
 */
public class ControlFlowDistance implements Comparable<ControlFlowDistance> {

	// TODO make private and redirect all accesses to setter and getter - was
	// too lazy to do concurrency and mutation package right now
	public int approachLevel;
	public double branchDistance;

	/**
	 * Creates the 0-distance, meaning a distance having approachLevel and
	 * branchDistance set to 0
	 */
	public ControlFlowDistance() {
		approachLevel = 0;
		branchDistance = 0.0;
	}

	/**
	 * Can be used to create an arbitrary ControlFlowDistance
	 * 
	 * However approachLevel and branchDistance are expected to be positive
	 */
	public ControlFlowDistance(int approachLevel, double branchDistance) {
		if (approachLevel < 0 || branchDistance < 0.0)
			throw new IllegalStateException(
			        "expect approachLevel and branchDistance to always be positive");

		this.approachLevel = approachLevel;
		this.branchDistance = branchDistance;
	}

	@Override
	public int compareTo(ControlFlowDistance o) {
		ControlFlowDistance d = o;
		if (approachLevel < d.approachLevel)
			return -1;
		else if (approachLevel > d.approachLevel)
			return 1;
		else {
			if (branchDistance < d.branchDistance)
				return -1;
			else if (branchDistance > d.branchDistance)
				return 1;
			else
				return 0;
		}
	}

	public void increaseApproachLevel() {
		approachLevel++;
		if (approachLevel < 0)
			throw new IllegalStateException(
			        "expect approach Level to always be positive - overflow?");
	}

	public int getApproachLevel() {
		return approachLevel;
	}

	public void setApproachLevel(int approachLevel) {
		if (approachLevel < 0)
			throw new IllegalArgumentException(
			        "expect approachLevel to always be positive");

		this.approachLevel = approachLevel;
	}

	public double getBranchDistance() {
		return branchDistance;
	}

	public void setBranchDistance(double branchDistance) {
		if (branchDistance < 0.0)
			throw new IllegalArgumentException("expect branchDistance to be positive");

		this.branchDistance = branchDistance;
	}

	public double getResultingBranchFitness() {

		return approachLevel + FitnessFunction.normalize(branchDistance);
	}

	@Override
	public String toString() {
		return "Approach = " + approachLevel + ", branch distance = " + branchDistance;
	}
}
