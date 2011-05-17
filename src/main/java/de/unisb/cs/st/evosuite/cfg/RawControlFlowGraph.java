package de.unisb.cs.st.evosuite.cfg;

import org.jgrapht.graph.DefaultEdge;

public class RawControlFlowGraph extends
		EvoSuiteGraph<BytecodeInstruction, DefaultEdge> {

	public RawControlFlowGraph() {
		super(DefaultEdge.class);
	}

}
