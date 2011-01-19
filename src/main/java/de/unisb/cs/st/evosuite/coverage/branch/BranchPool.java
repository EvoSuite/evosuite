package de.unisb.cs.st.evosuite.coverage.branch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BranchPool {

	public static Map<String, Integer> branch_count = new HashMap<String, Integer>();

	public static Map<String, Map<String, Map<Integer,Integer>>> branch_map = new HashMap<String, Map<String, Map<Integer,Integer>>>();

	public static Set<String> branchless_methods = new HashSet<String>();

	// maps the branch_counter field of this class to its bytecodeID in the CFG
	public static Map<Integer, Integer> branchCounterToBytecodeID = new HashMap<Integer, Integer>();	
	
	public static int branch_counter = 0;
	
	
}
