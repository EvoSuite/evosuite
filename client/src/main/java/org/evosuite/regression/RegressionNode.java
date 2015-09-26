package org.evosuite.regression;

public class RegressionNode {

	public int bytecodeOffset;
	public int basicBlock;

	public RegressionNode(int offset) {
		this.bytecodeOffset = offset;
	}
}
