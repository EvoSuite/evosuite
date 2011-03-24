package de.unisb.cs.st.evosuite.coverage.branch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;

/**
 * This class is supposed to hold all the available information concerning
 * Branches.
 * 
 * The addBranch()-Method gets called by the CFGMethodAdapter whenever it
 * detects a CFGVertex that corresponds to a Branch in the class under test.
 * 
 * @author Andre Mis
 */
public class BranchPool {

	// TODO: root branches should not be special cases
	//			every root branch should be a branch just like every other branch with it's own branchId and all
	
	// maps className -> method inside that class -> list of branches inside that method 
	public static Map<String, Map<String, List<Branch>>> branchMap = new HashMap<String, Map<String, List<Branch>>>();

	// maps every Method to the Branches inside that method
	private static Map<String, Integer> methodBranchCount = new HashMap<String, Integer>();

	// set of all known methods without a Branch
	private static Set<String> branchlessMethods = new HashSet<String>();

	// maps the branch_counter field of this class to its bytecodeID in the CFG
	private static Map<Integer, Branch> bytecodeIdMap = new HashMap<Integer, Branch>();

	// number of known Branches
	private static int branchCounter = 0;

	private static Logger logger = Logger.getLogger(BranchPool.class);

	/**
	 * Get called by the CFGMethodAdapter whenever it detects a CFGVertex that
	 * corresponds to a Branch in the class under test.
	 * 
	 * @param v
	 *            CFGVertex of a Branch
	 */
	public static void addBranch(CFGVertex v) {
		if (!(v.isBranch() || v.isTableSwitch() || v.isLookupSwitch()))
			throw new IllegalArgumentException("CFGVertex of a branch expected");

		Branch b = new Branch(v);
		addBranchToMap(b);
		markBranchIDs(v);
		bytecodeIdMap.put(branchCounter, b);

		logger.debug("Branch " + branchCounter + " at line " + v.getID() + " - "
		        + v.line_no);

		branchCounter++;
	}

	/**
	 * Gets called by the CFGMethodAdapter when it detects a Method without any
	 * Branches.
	 * 
	 * @param methodName
	 *            Unique methodName of a method without Branches
	 */
	public static void addBranchlessMethod(String methodName) {
		branchlessMethods.add(methodName);
	}

	// TODO can't this just always be called private by addBranch?
	// TODO why is this called in CFGMethodAdapter.getInstrumentation() anyways?
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
		Branch branch = bytecodeIdMap.get(branchId);
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
		return bytecodeIdMap.get(branchId);
	}

	/**
	 * Returns a set with all unique methodNames of methods without Branches.
	 * 
	 * @return A set with all unique methodNames of methods without Branches.
	 */
	public static Set<String> getBranchlessMethods() {
		return branchlessMethods;
	}

	private static void addBranchToMap(Branch b) {
		CFGVertex v = b.getCFGVertex();
		String className = v.className;
		String methodName = v.methodName;

		if (!branchMap.containsKey(className))
			branchMap.put(className, new HashMap<String, List<Branch>>());
		if (!branchMap.get(className).containsKey(methodName))
			branchMap.get(className).put(methodName, new ArrayList<Branch>());
		branchMap.get(className).get(methodName).add(b);
	}

	private static void markBranchIDs(CFGVertex v) {
		ControlFlowGraph completeCFG = CFGMethodAdapter.getCompleteCFG(v.className,
		                                                               v.methodName);
		CFGVertex branchVertex = completeCFG.getVertex(v.getID());
		branchVertex.branchId = branchCounter;
		v.branchId = branchCounter;
		completeCFG.markBranchIds(branchVertex);
	}
}
