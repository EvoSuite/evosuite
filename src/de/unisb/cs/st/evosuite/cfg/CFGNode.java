package de.unisb.cs.st.evosuite.cfg;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.tree.analysis.Frame;

public class CFGNode extends Frame { 
	Map<Integer, CFGNode> successors = new HashMap<Integer, CFGNode>();
	
	public CFGNode(int nLocals, int nStack) {
		super(nLocals, nStack);
	}
	
	public CFGNode(Frame src) {
		super(src);
	}
	
}