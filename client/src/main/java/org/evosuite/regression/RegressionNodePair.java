package org.evosuite.regression;

public class RegressionNodePair {

	public RegressionNode oldNode;
	public RegressionNode newNode;
	int relationship;

	public RegressionNodePair(int oldOffset, int newOffset, int rel) {
		this.oldNode = new RegressionNode(oldOffset);
		this.newNode = new RegressionNode(newOffset);
		this.relationship = rel;
	}

}
