/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.coverage.branch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

// TODO: root branches should not be special cases
// every root branch should be a branch just
// like every other branch with it's own branchId and all

/**
 * This class is supposed to hold all the available information concerning
 * Branches.
 * 
 * The addBranch()-Method gets called during class analysis. Whenever the
 * BytecodeInstructionPool detects a BytecodeInstruction that corresponds to a
 * Branch in the class under test as defined in
 * BytecodeInstruction.isActualBranch() it calls the registerAsBranch() method
 * of this class which in turn properly registers the instruction within this
 * pool.
 * 
 * There are two kinds of Branch objects: normal branches and switch case
 * branches. For more details about the difference between these two look at the
 * Branch class.
 * 
 * @author Andre Mis
 */
public class BranchPool {

	private static Logger logger = LoggerFactory.getLogger(BranchPool.class);

	// maps className -> method inside that class -> list of branches inside
	// that method
	private static Map<String, Map<String, List<Branch>>> branchMap = new HashMap<String, Map<String, List<Branch>>>();

	// set of all known methods without a Branch
	private static Map<String, Set<String>> branchlessMethods = new HashMap<String, Set<String>>();

	// maps the branchIDs assigned by this pool to their respective Branches
	private static Map<Integer, Branch> branchIdMap = new HashMap<Integer, Branch>();

	// maps all known branch instructions to their branchId
	private static Map<BytecodeInstruction, Integer> registeredNormalBranches = new HashMap<BytecodeInstruction, Integer>();

	// maps all known switch instructions to a list containing all of their
	// associated Branch objects
	private static Map<BytecodeInstruction, List<Branch>> registeredSwitches = new HashMap<BytecodeInstruction, List<Branch>>();

	private static Map<BytecodeInstruction, Branch> registeredDefaultCases = new HashMap<BytecodeInstruction, Branch>();

	private static Map<LabelNode, List<Branch>> switchLabels = new HashMap<LabelNode, List<Branch>>();

	// number of known Branches - used for actualBranchIds
	private static int branchCounter = 0;

	// fill the pool

	/**
	 * Reset all the data structures used to keep track of the branch information
	 */
	public static void reset(){
		branchCounter = 0;
		branchMap.clear();
		branchlessMethods.clear();
		branchIdMap.clear();
		registeredNormalBranches.clear();
		registeredSwitches.clear();
		registeredDefaultCases.clear();
		switchLabels.clear();
	}
	
	/**
	 * Gets called by the CFGMethodAdapter whenever it detects a method without
	 * any branches.
	 * 
	 * @param methodName
	 *            Unique methodName - consisting of <className>.<methodName> -
	 *            of a method without Branches
	 */
	public static void addBranchlessMethod(String className, String methodName) {
		if (!branchlessMethods.containsKey(className))
			branchlessMethods.put(className, new HashSet<String>());
		branchlessMethods.get(className).add(methodName);
	}

	/**
	 * Called by the BytecodeInstructionPool whenever it detects an instruction
	 * that corresponds to a Branch in the class under test as defined by
	 * BytecodeInstruction.isActualBranch().
	 * 
	 */
	public static void registerAsBranch(BytecodeInstruction instruction) {
		if (!(instruction.isActualBranch()))
			throw new IllegalArgumentException("CFGVertex of a branch expected");
		if (isKnownAsBranch(instruction))
			return;
		// throw new
		// IllegalArgumentException("branches can only be added to the pool once");

		registerInstruction(instruction);

	}

	private static void registerInstruction(BytecodeInstruction v) {
		if (isKnownAsBranch(v))
			throw new IllegalStateException(
			        "expect registerInstruction() to be called at most once for each instruction");

		if (v.isBranch())
			registerNormalBranchInstruction(v);
		else if (v.isSwitch())
			registerSwitchInstruction(v);
		else
			throw new IllegalArgumentException(
			        "expect given instruction to be an actual branch");
	}

	private static void registerNormalBranchInstruction(BytecodeInstruction v) {
		if (!v.isBranch())
			throw new IllegalArgumentException("normal branch instruction expceted");

		if (registeredNormalBranches.containsKey(v))
			throw new IllegalArgumentException(
			        "instruction already registered as a normal branch");

		branchCounter++;
		registeredNormalBranches.put(v, branchCounter);

		Branch b = new Branch(v, branchCounter);
		addBranchToMap(b);
		branchIdMap.put(branchCounter, b);

		logger.info("Branch " + branchCounter + " at line " + v.getLineNumber());
	}

	private static void registerSwitchInstruction(BytecodeInstruction v) {
		if (!v.isSwitch())
			throw new IllegalArgumentException("expect a switch instruction");

		LabelNode defaultLabel = null;

		switch (v.getASMNode().getOpcode()) {
		case Opcodes.TABLESWITCH:
			TableSwitchInsnNode tableSwitchNode = (TableSwitchInsnNode) v.getASMNode();
			registerTableSwitchCases(v, tableSwitchNode);
			defaultLabel = tableSwitchNode.dflt;

			break;
		case Opcodes.LOOKUPSWITCH:
			LookupSwitchInsnNode lookupSwitchNode = (LookupSwitchInsnNode) v.getASMNode();
			registerLookupSwitchCases(v, lookupSwitchNode);
			defaultLabel = lookupSwitchNode.dflt;
			break;
		default:
			throw new IllegalStateException(
			        "expect ASMNode of a switch to either be a LOOKUP- or TABLESWITCH");
		}

		registerDefaultCase(v, defaultLabel);
	}

	private static void registerDefaultCase(BytecodeInstruction v, LabelNode defaultLabel) {

		if (defaultLabel == null)
			throw new IllegalStateException("expect variable to bet set");

		Branch defaultBranch = createSwitchCaseBranch(v, null, defaultLabel);
		if (!defaultBranch.isSwitchCaseBranch() || !defaultBranch.isDefaultCase())
			throw new IllegalStateException(
			        "expect created branch to be a default case branch of a switch");
	}

	private static void registerTableSwitchCases(BytecodeInstruction v,
	        TableSwitchInsnNode tableSwitchNode) {

		int num = 0;

		for (int i = tableSwitchNode.min; i <= tableSwitchNode.max; i++) {
			LabelNode targetLabel = (LabelNode) tableSwitchNode.labels.get(num);
			Branch switchBranch = createSwitchCaseBranch(v, i, targetLabel);
			if (!switchBranch.isSwitchCaseBranch() || !switchBranch.isActualCase())
				throw new IllegalStateException(
				        "expect created branch to be an actual case branch of a switch");
			num++;
		}
	}

	private static void registerLookupSwitchCases(BytecodeInstruction v,
	        LookupSwitchInsnNode lookupSwitchNode) {

		for (int i = 0; i < lookupSwitchNode.keys.size(); i++) {
			LabelNode targetLabel = (LabelNode) lookupSwitchNode.labels.get(i);
			Branch switchBranch = createSwitchCaseBranch(v,
			                                             (Integer) lookupSwitchNode.keys.get(i),
			                                             targetLabel);
			if (!switchBranch.isSwitchCaseBranch() || !switchBranch.isActualCase())
				throw new IllegalStateException(
				        "expect created branch to be an actual case branch of a switch");
		}
	}

	private static Branch createSwitchCaseBranch(BytecodeInstruction v,
	        Integer caseValue, LabelNode targetLabel) {

		branchCounter++;

		Branch switchBranch = new Branch(v, caseValue, targetLabel, branchCounter);
		registerSwitchBranch(v, switchBranch);
		addBranchToMap(switchBranch);
		branchIdMap.put(branchCounter, switchBranch);

		registerSwitchLabel(switchBranch, targetLabel);

		// default case
		if (caseValue == null) {
			if (registeredDefaultCases.containsKey(v))
				throw new IllegalStateException(
				        "instruction already registered as a branch");
			registeredDefaultCases.put(v, switchBranch);
		}

		if (!switchBranch.isSwitchCaseBranch())
			throw new IllegalStateException("expect created Branch to be a switch branch");

		return switchBranch;
	}

	private static void registerSwitchLabel(Branch b, LabelNode targetLabel) {

		if (switchLabels.get(targetLabel) == null)
			switchLabels.put(targetLabel, new ArrayList<Branch>());

		List<Branch> oldList = switchLabels.get(targetLabel);

		if (oldList.contains(b))
			throw new IllegalStateException(
			        "branch already registered for this switch label");

		oldList.add(b);

		// TODO several Branches can map to one Label, so switchLabels should
		// either map from branches to labels, not the other way around. or it
		// should map labels to a list of branches
		// this stems from the fact that empty case: blocks do not have their
		// own label

		// TODO STOPPED HERE

		switchLabels.put(targetLabel, oldList);
	}

	private static void registerSwitchBranch(BytecodeInstruction v, Branch switchBranch) {
		if (!v.isSwitch())
			throw new IllegalArgumentException("switch instruction expected");

		if (registeredSwitches.get(v) == null)
			registeredSwitches.put(v, new ArrayList<Branch>());

		List<Branch> oldList = registeredSwitches.get(v);

		if (oldList.contains(v))
			throw new IllegalArgumentException("switch branch already registered  "
			        + switchBranch.toString());

		oldList.add(switchBranch);

		registeredSwitches.put(v, oldList);
	}

	private static void addBranchToMap(Branch b) {
		
		logger.info("Adding to map the branch {}",b);
		
		String className = b.getClassName();
		String methodName = b.getMethodName();

		if (!branchMap.containsKey(className))
			branchMap.put(className, new HashMap<String, List<Branch>>());
		if (!branchMap.get(className).containsKey(methodName))
			branchMap.get(className).put(methodName, new ArrayList<Branch>());
		branchMap.get(className).get(methodName).add(b);
	}

	// retrieve information from the pool

	/**
	 * Checks whether the given instruction has Branch objects associated with
	 * it.
	 * 
	 * Returns true if the given BytecodeInstruction previously passed a call to
	 * registerAsBranch(instruction), false otherwise
	 */
	public static boolean isKnownAsBranch(BytecodeInstruction instruction) {
		return isKnownAsNormalBranchInstruction(instruction)
		        || isKnownAsSwitchBranchInstruction(instruction);
	}

	public static boolean isKnownAsNormalBranchInstruction(BytecodeInstruction ins) {

		return registeredNormalBranches.containsKey(ins);
	}

	public static boolean isKnownAsSwitchBranchInstruction(BytecodeInstruction instruction) {

		return registeredSwitches.containsKey(instruction);
	}

	public static int getActualBranchIdForNormalBranchInstruction(BytecodeInstruction ins) {
		if (!isKnownAsNormalBranchInstruction(ins))
			throw new IllegalArgumentException(
			        "instruction not registered as a normal branch");

		if (registeredNormalBranches.containsKey(ins))
			return registeredNormalBranches.get(ins);

		throw new IllegalStateException(
		        "expect registeredNormalBranches to contain a key for each known normal branch instruction");
	}

	public static List<Branch> getCaseBranchesForSwitch(BytecodeInstruction instruction) {
		if (instruction == null)
			throw new IllegalArgumentException("null given");
		if (!instruction.isSwitch())
			throw new IllegalArgumentException("switch instruction expected");
		if (!isKnownAsSwitchBranchInstruction(instruction))
			throw new IllegalArgumentException("not registered as a switch instruction");

		return registeredSwitches.get(instruction);
	}

	public static Branch getBranchForInstruction(BytecodeInstruction instruction) {
		if (instruction == null)
			throw new IllegalArgumentException("null given");
		if (!isKnownAsNormalBranchInstruction(instruction))
			throw new IllegalArgumentException(
			        "expect given instruction to be known as a normal branch");

		return getBranch(registeredNormalBranches.get(instruction));
	}

	public static List<Branch> getBranchForLabel(LabelNode label) {

		// TODO see registerSwitchLabel()!

		return switchLabels.get(label);
	}

	/**
	 * Returns the number of known Branches for a given methodName in a given
	 * class.
	 * 
	 * @return The number of currently known Branches inside the given method
	 */
	public static int getBranchCountForMethod(String className, String methodName) {
		if (branchMap.get(className) == null)
			return 0;
		if (branchMap.get(className).get(methodName) == null)
			return 0;

		return branchMap.get(className).get(methodName).size();
	}

	/**
	 * Returns the number of known Branches for a given class
	 * 
	 * @return The number of currently known Branches inside the given class
	 */
	public static int getBranchCountForClass(String className) {
		if (branchMap.get(className) == null)
			return 0;
		int total = 0;
		for (String method : branchMap.get(className).keySet()) {
			total += branchMap.get(className).get(method).size();
		}
		return total;
	}

	/**
	 * Returns the number of known Branches for a given class
	 * 
	 * @return The number of currently known Branches inside the given class
	 */
	public static int getBranchCountForPrefix(String prefix) {
		int num = 0;
		for (String className : branchMap.keySet()) {
			if (className.startsWith(prefix)) {
				logger.info("Found matching class for branch count: " + className + "/"
				        + prefix);
				for (String method : branchMap.get(className).keySet()) {
					num += branchMap.get(className).get(method).size();
				}
			}
		}
		return num;
	}

	/**
	 * Returns the number of known Branches for a given class
	 * 
	 * @return The number of currently known Branches inside the given class
	 */
	public static int getBranchCountForMemberClasses(String prefix) {
		int num = 0;
		for (String className : branchMap.keySet()) {
			if (className.equals(prefix) || className.startsWith(prefix + "$")) {
				logger.info("Found matching class for branch count: " + className + "/"
				        + prefix);
				for (String method : branchMap.get(className).keySet()) {
					num += branchMap.get(className).get(method).size();
				}
			}
		}
		return num;
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
	public static Set<String> getBranchlessMethods(String className) {
		if (!branchlessMethods.containsKey(className))
			return new HashSet<String>();

		return branchlessMethods.get(className);
	}

	/**
	 * Returns a set with all unique methodNames of methods without Branches.
	 * 
	 * @return A set with all unique methodNames of methods without Branches.
	 */
	public static Set<String> getBranchlessMethodsPrefix(String className) {
		Set<String> methods = new HashSet<String>();

		for (String name : branchlessMethods.keySet()) {
			if (name.equals(className) || name.startsWith(className + "$")) {
				methods.addAll(branchlessMethods.get(name));
			}
		}

		return methods;
	}

	/**
	 * Returns a set with all unique methodNames of methods without Branches.
	 * 
	 * @return A set with all unique methodNames of methods without Branches.
	 */
	public static Set<String> getBranchlessMethodsMemberClasses(String className) {
		Set<String> methods = new HashSet<String>();

		for (String name : branchlessMethods.keySet()) {
			if (name.equals(className) || name.startsWith(className + "$")) {
				methods.addAll(branchlessMethods.get(name));
			}
		}

		return methods;
	}

	/**
	 * Returns the number of methods without Branches for class className
	 * 
	 * @return The number of methods without Branches.
	 */
	public static int getNumBranchlessMethods(String className) {
		if (!branchlessMethods.containsKey(className))
			return 0;
		return branchlessMethods.get(className).size();
	}

	/**
	 * Returns the number of methods without Branches for class className
	 * 
	 * @return The number of methods without Branches.
	 */
	public static int getNumBranchlessMethodsPrefix(String className) {
		int num = 0;
		for (String name : branchlessMethods.keySet()) {
			if (name.startsWith(className))
				num += branchlessMethods.get(name).size();
		}
		return num;
	}

	/**
	 * Returns the number of methods without Branches for class className
	 * 
	 * @return The number of methods without Branches.
	 */
	public static int getNumBranchlessMethodsMemberClasses(String className) {
		int num = 0;
		for (String name : branchlessMethods.keySet()) {
			if (name.equals(className) || name.startsWith(className + "$"))
				num += branchlessMethods.get(name).size();
		}
		return num;
	}

	/**
	 * Returns a Set containing all classes for which this pool knows Branches
	 * for as Strings
	 */
	public static Set<String> knownClasses() {
		Set<String> r = new HashSet<String>();
		r.addAll(branchMap.keySet());
		r.addAll(branchlessMethods.keySet());
		
		if(logger.isDebugEnabled()){
			logger.debug("Known classes: "+r);
		}
		
		return r;
	}

	/**
	 * Returns a Set containing all methods in the class represented by the
	 * given String for which this pool knows Branches for as Strings
	 * 
	 */
	public static Set<String> knownMethods(String className) {
		Set<String> r = new HashSet<String>();
		Map<String, List<Branch>> methods = branchMap.get(className);
		if (methods != null)
			r.addAll(methods.keySet());

		return r;
	}

	/**
	 * Returns a List containing all Branches in the given class and method
	 * 
	 * Should no such Branch exist an empty List is returned
	 */
	public static List<Branch> retrieveBranchesInMethod(String className,
	        String methodName) {
		List<Branch> r = new ArrayList<Branch>();
		if (branchMap.get(className) == null)
			return r;
		List<Branch> branches = branchMap.get(className).get(methodName);
		if (branches != null)
			r.addAll(branches);
		return r;
	}

	public static Branch getDefaultBranchForSwitch(BytecodeInstruction v) {
		if (!v.isSwitch())
			throw new IllegalArgumentException("switch instruction expected");
		if (!isKnownAsSwitchBranchInstruction(v))
			throw new IllegalArgumentException(
			        "instruction not known to be a switch instruction");
		if (!registeredDefaultCases.containsKey(v))
			throw new IllegalArgumentException(
			        "there is no registered default case for this instruction");

		return registeredDefaultCases.get(v);
	}

	public static int getRealBranches(String className) {
		int real = 0;
		for (String methodName : branchMap.get(className).keySet())
			for (Branch b : (branchMap.get(className).get(methodName))) {
				if (!b.getInstruction().isForcedBranch())
					real++;
			}

		return real;
	}

	public static void clear() {
		branchCounter = 0;
		branchMap.clear();
		branchIdMap.clear();
		branchlessMethods.clear();
		switchLabels.clear();
		registeredDefaultCases.clear();
		registeredNormalBranches.clear();
		registeredSwitches.clear();
	}

	public static void clear(String className) {
		branchMap.remove(className);
		branchlessMethods.remove(className);
	}

	public static void clear(String className, String methodName) {
		int numBranches = 0;

		if (branchMap.containsKey(className)) {
			if (branchMap.get(className).containsKey(methodName))
				numBranches = branchMap.get(className).get(methodName).size();
			branchMap.get(className).remove(methodName);
		}
		if (branchlessMethods.containsKey(className))
			branchlessMethods.get(className).remove(methodName);
		logger.info("Resetting branchCounter from " + branchCounter + " to "
		        + (branchCounter - numBranches));
		branchCounter -= numBranches;
	}

}
