package de.unisb.cs.st.evosuite.coverage.branch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.cfg.CFGPool;
import de.unisb.cs.st.evosuite.cfg.RawControlFlowGraph;

// TODO: root branches should not be special cases
//			every root branch should be a branch just 
//			like every other branch with it's own branchId and all

/**
 * This class is supposed to hold all the available information concerning
 * Branches.
 * 
 * The addBranch()-Method gets called by the BranchInstrumentation whenever it
 * detects a BytecodeInstruction that corresponds to a Branch in the class under test.
 * 
 * @author Andre Mis
 */
public class BranchPool {

	private static Logger logger = Logger.getLogger(BranchPool.class);
	
	
	// maps className -> method inside that class -> list of branches inside that method 
	private static Map<String, Map<String, List<Branch>>> branchMap = new HashMap<String, Map<String, List<Branch>>>();

	// maps every Method to the number of Branches inside that method
	private static Map<String, Integer> methodBranchCount = new HashMap<String, Integer>();

	// set of all known methods without a Branch
	private static Set<String> branchlessMethods = new HashSet<String>();

	// maps the branchIDs assigned by this pool to their respective Branches
	private static Map<Integer, Branch> branchIdMap = new HashMap<Integer, Branch>();
	
	// maps all known branch instructions to their branchId
	private static Map<BytecodeInstruction, Integer> registeredBranches = new HashMap<BytecodeInstruction, Integer>();

	// number of known Branches
	private static int branchCounter = 0;

	
	// fill the pool
	
	
	/**
	 * Get called by the BranchInstrumentation whenever it detects a CFGVertex that
	 * corresponds to a Branch in the class under test.
	 * 
	 * @param v
	 *            CFGVertex of a Branch
	 */
	public static void addBranch(BytecodeInstruction v) {
		if (!(v.isActualBranch()))
			throw new IllegalArgumentException("CFGVertex of a branch expected");
		if(isKnownAsBranch(v))
			throw new IllegalArgumentException("branches can only be added to the pool once");

		registerInstruction(v);
		
	}

	/**
	 * Gets called by the CFGMethodAdapter whenever it detects a method 
	 * without any branches.
	 * 
	 * @param methodName
	 *            Unique methodName of a method without Branches
	 */
	public static void addBranchlessMethod(String methodName) {
		branchlessMethods.add(methodName);
	}	
	
	private static void registerInstruction(BytecodeInstruction v) {
		if(isKnownAsBranch(v))
			throw new IllegalStateException("expect registerInstruction() to be called at most once for each instruction");
		
		branchCounter++;
		v.setBranchId(branchCounter);
		markBranchIDs(v);
		registeredBranches.put(v, branchCounter);
		
		Branch b = new Branch(v);
		addBranchToMap(b);
		branchIdMap.put(branchCounter, b);

		logger.debug("Branch " + branchCounter + " at line " + b.getLineNumber());
	}

	private static void addBranchToMap(Branch b) {
		String className = b.getClassName();
		String methodName = b.getMethodName();

		if (!branchMap.containsKey(className))
			branchMap.put(className, new HashMap<String, List<Branch>>());
		if (!branchMap.get(className).containsKey(methodName))
			branchMap.get(className).put(methodName, new ArrayList<Branch>());
		branchMap.get(className).get(methodName).add(b);
	}

	private static void markBranchIDs(BytecodeInstruction b) {
		RawControlFlowGraph completeCFG = CFGPool.getCompleteCFG(b
				.getClassName(), b.getMethodName());
		
		completeCFG.markBranchIds(b);
	}	
	
	
	// retrieve information from the pool
	
	
	/**
	 * Checks whether the given instruction is already known to be a Branch
	 * 
	 * Returns true if the given BytecodeInstruction previously passed a
	 * call to addBranch(instruction), false otherwise  
	 */
	public static boolean isKnownAsBranch(BytecodeInstruction v) {
		return registeredBranches.containsKey(v);
	}	


	// TODO can't this just always be called private by addBranch?
	// 		 why is this called in CFGMethodAdapter.getInstrumentation() anyways?
	//			.. well it only get's called if Properties.CRITERION is set to LCSAJ 
	//			... might want to change that
	public static void countBranch(String id) {
		if (!methodBranchCount.containsKey(id)) {
			methodBranchCount.put(id, 1);
		} else
			methodBranchCount.put(id, methodBranchCount.get(id) + 1);
	}

	/**
	 * Returns the number of known Branches for a given methodName.
	 * 
	 * @param methodName
	 *            Unique methodName (consisting of "className.methodName")
	 * @return The number of currently known Branches inside the given method
	 */
	public static int getBranchCountForMethod(String methodName) {
		Integer count = methodBranchCount.get(methodName);
		if (count == null)
			return 0;

		return count;
	}

	/**
	 * Returns the number of currently known Branches
	 * 
	 * @return The number of currently known Branches
	 */
	public static int getBranchCounter() {
		return branchCounter;
	}

	/**
	 * Returns the bytecodeID for the branch associated with the given branchID
	 * 
	 * @param branchId
	 *            The ID of a Branch
	 * @return If there is a Branch with the given branchID this method returns
	 *         its bytecodeID, -1 otherwise
	 */
	public static int getBytecodeIdFor(int branchId) {
		Branch branch = branchIdMap.get(branchId);
		if (branch == null)
			return -1;

		return branch.getBytecodeId();
	}

	/**
	 * Returns the Branch object associated with the given branchID
	 * 
	 * @param branchId
	 *            The ID of a branch
	 * @return The branch, or null if it does not exist
	 */
	public static Branch getBranch(int branchId) {
		return branchIdMap.get(branchId);
	}

	/**
	 * Returns a set with all unique methodNames of methods without Branches.
	 * 
	 * @return A set with all unique methodNames of methods without Branches.
	 */
	public static Set<String> getBranchlessMethods() {
		return branchlessMethods;
	}

	/**
	 *  Returns a Set containing all classes for which this pool
	 * knows Branches for as Strings
	 */
	public static Set<String> knownClasses() {
		Set<String> r = new HashSet<String>();
		r.addAll(branchMap.keySet());
		return r;
	}
	
	/**
	 *  Returns a Set containing all methods in the class 
	 * represented by the given String for which this pool 
	 * knows Branches for as Strings
	 * 
	 */	
	public static Set<String> knownMethods(String className) {
		Set<String> r = new HashSet<String>();
		Map<String, List<Branch>> methods = branchMap.get(className);
		if(methods != null)
			r.addAll(methods.keySet());
		
		return r;
	}
	
	/**
	 * Returns the branch contained in the given method of the given class
	 * 
	 *  Should no such Branch exist null is returned
	 */
	public static Branch getBranchByBytecodeId(String className, String methodName, int bytecodeId) {
		List<Branch> branches = retrieveBranchesInMethod(className, methodName);
		for(Branch b : branches)
			if(b.getBytecodeId()==bytecodeId)
				return b;
		
		return null;
	}
	
	/**
	 * Returns a List containing all Branches in the given class and method
	 * 
	 *  Should no such Branch exist an empty List is returned
	 */
	public static List<Branch> retrieveBranchesInMethod(String className, String methodName) {
		List<Branch> r = new ArrayList<Branch>();
		if(branchMap.get(className) == null)
			return r;
		List<Branch> branches = branchMap.get(className).get(methodName);
		if(branches != null)
			r.addAll(branches);
		return r;
	}
}
