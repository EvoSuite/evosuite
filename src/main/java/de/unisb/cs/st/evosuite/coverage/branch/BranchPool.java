package de.unisb.cs.st.evosuite.coverage.branch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

public class BranchPool {

	private static Logger logger = Logger.getLogger(BranchPool.class);
	
	public static Map<String, Integer> branch_count = new HashMap<String, Integer>();

	public static Map<String, Map<String, List<Branch>>> branch_map = new HashMap<String, Map<String, List<Branch>>>();

	public static Set<String> branchless_methods = new HashSet<String>();

	// maps the branch_counter field of this class to its bytecodeID in the CFG
	private static Map<Integer, Branch> branchIDMap = new HashMap<Integer, Branch>();	
	
	private static int branchCounter = 0;
	

	public static void addBranch(CFGVertex v)  {
		if(!v.isBranch())
			throw new IllegalArgumentException("CFGVertex of a branch expected");
		
		Branch b = new Branch(v);
		
		addBranchToMap(b);
		markBranchIDs(v);
		branchIDMap.put(branchCounter, b);
		
		logger.debug("Branch "+branchCounter+" at line "+v.getID()+" - "+v.line_no);
		
		branchCounter++;
	}

	public static int getBranchCounter() {
		return branchCounter;
	}
	
	public static int getBytecodeIDFor(int branchID) {
		return branchIDMap.get(branchID).getBytecodeID();
	}
	
	private static void addBranchToMap(Branch b) {
		CFGVertex v = b.getCFGVertex();
		String className = v.className;
		String methodName = v.methodName;
		
		if(!branch_map.containsKey(className))
			branch_map.put(className, new HashMap<String, List<Branch>>());
		if(!branch_map.get(className).containsKey(methodName))
			branch_map.get(className).put(methodName, new ArrayList<Branch>());
		branch_map.get(className).get(methodName).add(b);
	}

	private static void markBranchIDs(CFGVertex v) {
		ControlFlowGraph completeCFG = CFGMethodAdapter.getCompleteCFG(v.className, v.methodName);
		CFGVertex branchVertex = completeCFG.getVertex(v.getID());
		branchVertex.branchID = branchCounter;
		completeCFG.markBranchIDs(branchVertex);
	}
}
