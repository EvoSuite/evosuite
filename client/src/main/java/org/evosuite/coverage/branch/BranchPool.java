/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.branch;

import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.setup.DependencyAnalysis;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
	private Map<String, Map<String, List<Branch>>> branchMap = new HashMap<String, Map<String, List<Branch>>>();

	// set of all known methods without a Branch
	private Map<String, Map<String, Integer>> branchlessMethods = new HashMap<String, Map<String, Integer>>();

	// maps the branchIDs assigned by this pool to their respective Branches
	private Map<Integer, Branch> branchIdMap = new HashMap<Integer, Branch>();

	// maps all known branch instructions to their branchId
	private Map<BytecodeInstruction, Integer> registeredNormalBranches = new HashMap<BytecodeInstruction, Integer>();

	// maps all known switch instructions to a list containing all of their
	// associated Branch objects
	private Map<BytecodeInstruction, List<Branch>> registeredSwitches = new HashMap<BytecodeInstruction, List<Branch>>();

	private Map<BytecodeInstruction, Branch> registeredDefaultCases = new HashMap<BytecodeInstruction, Branch>();

	private Map<LabelNode, List<Branch>> switchLabels = new HashMap<LabelNode, List<Branch>>();

	// number of known Branches - used for actualBranchIds
	private int branchCounter = 0;

	private static Map<ClassLoader, BranchPool> instanceMap = new HashMap<ClassLoader, BranchPool>();

	public static BranchPool getInstance(ClassLoader classLoader) {
		if (!instanceMap.containsKey(classLoader)) {
			instanceMap.put(classLoader, new BranchPool());
		}

		return instanceMap.get(classLoader);
	}
	// fill the pool

	/**
	 * Gets called by the CFGMethodAdapter whenever it detects a method without
	 * any branches.
	 * 
	 * @param methodName
	 *            Unique methodName - consisting of <className>.<methodName> -
	 *            of a method without Branches
	 * @param className
	 *            a {@link java.lang.String} object.
	 */
	public void addBranchlessMethod(String className, String methodName,
	        int lineNumber) {
		if (!branchlessMethods.containsKey(className))
			branchlessMethods.put(className, new HashMap<String, Integer>());
		branchlessMethods.get(className).put(methodName, lineNumber);
	}

	/**
	 * Called by the BytecodeInstructionPool whenever it detects an instruction
	 * that corresponds to a Branch in the class under test as defined by
	 * BytecodeInstruction.isActualBranch().
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public void registerAsBranch(BytecodeInstruction instruction) {
		if (!(instruction.isActualBranch()))
			throw new IllegalArgumentException("CFGVertex of a branch expected");
		if (isKnownAsBranch(instruction))
			return;
		if (!DependencyAnalysis.shouldInstrument(instruction.getClassName(),
		                                         instruction.getMethodName())) {
			return;
		} 
		
		registerInstruction(instruction);

	}

	private void registerInstruction(BytecodeInstruction v) {
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

	private void registerNormalBranchInstruction(BytecodeInstruction v) {
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

	private void registerSwitchInstruction(BytecodeInstruction v) {
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

	private void registerDefaultCase(BytecodeInstruction v, LabelNode defaultLabel) {

		if (defaultLabel == null)
			throw new IllegalStateException("expect variable to bet set");

		Branch defaultBranch = createSwitchCaseBranch(v, null, defaultLabel);
		if (!defaultBranch.isSwitchCaseBranch() || !defaultBranch.isDefaultCase())
			throw new IllegalStateException(
			        "expect created branch to be a default case branch of a switch");
	}

	private void registerTableSwitchCases(BytecodeInstruction v,
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

	private void registerLookupSwitchCases(BytecodeInstruction v,
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

	private Branch createSwitchCaseBranch(BytecodeInstruction v,
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

	private void registerSwitchLabel(Branch b, LabelNode targetLabel) {

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

	private void registerSwitchBranch(BytecodeInstruction v, Branch switchBranch) {
		if (!v.isSwitch())
			throw new IllegalArgumentException("switch instruction expected");

		if (registeredSwitches.get(v) == null)
			registeredSwitches.put(v, new ArrayList<Branch>());

		List<Branch> oldList = registeredSwitches.get(v);

		if (oldList.contains(switchBranch))
			throw new IllegalArgumentException("switch branch already registered  "
			        + switchBranch.toString());

		oldList.add(switchBranch);

		registeredSwitches.put(v, oldList);
	}

	private void addBranchToMap(Branch b) {

		logger.info("Adding to map the branch {}", b);

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
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a boolean.
	 */
	public boolean isKnownAsBranch(BytecodeInstruction instruction) {
		return isKnownAsNormalBranchInstruction(instruction)
		        || isKnownAsSwitchBranchInstruction(instruction);
	}

	/**
	 * <p>
	 * isKnownAsNormalBranchInstruction
	 * </p>
	 * 
	 * @param ins
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a boolean.
	 */
	public boolean isKnownAsNormalBranchInstruction(BytecodeInstruction ins) {

		return registeredNormalBranches.containsKey(ins);
	}

	/**
	 * <p>
	 * isKnownAsSwitchBranchInstruction
	 * </p>
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a boolean.
	 */
	public boolean isKnownAsSwitchBranchInstruction(BytecodeInstruction instruction) {

		return registeredSwitches.containsKey(instruction);
	}

	/**
	 * <p>
	 * getActualBranchIdForNormalBranchInstruction
	 * </p>
	 * 
	 * @param ins
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a int.
	 */
	public int getActualBranchIdForNormalBranchInstruction(BytecodeInstruction ins) {
		if (!isKnownAsNormalBranchInstruction(ins))
			throw new IllegalArgumentException(
			        "instruction not registered as a normal branch");

		if (registeredNormalBranches.containsKey(ins))
			return registeredNormalBranches.get(ins);

		throw new IllegalStateException(
		        "expect registeredNormalBranches to contain a key for each known normal branch instruction");
	}

	/**
	 * <p>
	 * getCaseBranchesForSwitch
	 * </p>
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<Branch> getCaseBranchesForSwitch(BytecodeInstruction instruction) {
		if (instruction == null)
			throw new IllegalArgumentException("null given");
		if (!instruction.isSwitch())
			throw new IllegalArgumentException("switch instruction expected");
		if (!isKnownAsSwitchBranchInstruction(instruction))
			throw new IllegalArgumentException("not registered as a switch instruction");

		return registeredSwitches.get(instruction);
	}

	/**
	 * <p>
	 * getBranchForInstruction
	 * </p>
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a {@link org.evosuite.coverage.branch.Branch} object.
	 */
	public Branch getBranchForInstruction(BytecodeInstruction instruction) {
		if (instruction == null)
			throw new IllegalArgumentException("null given");
		if (!isKnownAsNormalBranchInstruction(instruction))
			throw new IllegalArgumentException(
			        "expect given instruction to be known as a normal branch");

		return getBranch(registeredNormalBranches.get(instruction));
	}

	/**
	 * <p>
	 * getBranchForLabel
	 * </p>
	 * 
	 * @param label
	 *            a {@link org.objectweb.asm.tree.LabelNode} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<Branch> getBranchForLabel(LabelNode label) {

		// TODO see registerSwitchLabel()!

		return switchLabels.get(label);
	}

	/**
	 * Returns the number of known Branches for a given methodName in a given
	 * class.
	 * 
	 * @return The number of currently known Branches inside the given method
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 */
	public int getBranchCountForMethod(String className, String methodName) {
		if (branchMap.get(className) == null)
			return 0;
		if (branchMap.get(className).get(methodName) == null)
			return 0;

		return branchMap.get(className).get(methodName).size();
	}

	public int getNonArtificialBranchCountForMethod(String className,
	        String methodName) {
		if (branchMap.get(className) == null)
			return 0;
		if (branchMap.get(className).get(methodName) == null)
			return 0;

		int num = 0;
		for (Branch b : branchMap.get(className).get(methodName)) {
			if (!b.isInstrumented())
				num++;
		}

		return num;
	}

	/**
	 * Returns the number of known Branches for a given class
	 * 
	 * @return The number of currently known Branches inside the given class
	 * @param className
	 *            a {@link java.lang.String} object.
	 */
	public int getBranchCountForClass(String className) {
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
	 * @param prefix
	 *            a {@link java.lang.String} object.
	 */
	public int getBranchCountForPrefix(String prefix) {
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
	 * @param prefix
	 *            a {@link java.lang.String} object.
	 */
	public Set<Integer> getBranchIdsForPrefix(String prefix) {
		Set<Integer> ids = new HashSet<>();
		Set<Branch> sutBranches = new HashSet<>();
		for (String className : branchMap.keySet()) {
			if (className.startsWith(prefix)) {
				logger.info("Found matching class for branch ids: " + className + "/"
				        + prefix);
				for (String method : branchMap.get(className).keySet()) {
					sutBranches.addAll(branchMap.get(className).get(method));
				}
			}
		}
		
		for (Integer id : branchIdMap.keySet()) {
			if(sutBranches.contains(branchIdMap.get(id))){
				ids.add(id);
			}
		}
		
		return ids;
	} 

	/**
	 * Returns the number of known Branches for a given class
	 * 
	 * @return The number of currently known Branches inside the given class
	 * @param prefix
	 *            a {@link java.lang.String} object.
	 */
	public int getBranchCountForMemberClasses(String prefix) {
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
	public int getBranchCounter() {
		return branchCounter;
	}

	public int getNumArtificialBranches() {
		int num = 0;
		for (Branch b : branchIdMap.values()) {
			if (b.isInstrumented())
				num++;
		}

		return num;
	}

	/**
	 * Returns the Branch object associated with the given branchID
	 * 
	 * @param branchId
	 *            The ID of a branch
	 * @return The branch, or null if it does not exist
	 */
	public Branch getBranch(int branchId) {

		return branchIdMap.get(branchId);
	}
	
	public Collection<Branch> getAllBranches() {
		return branchIdMap.values();
	}

	/**
	 * Returns a set with all unique methodNames of methods without Branches.
	 * 
	 * @return A set with all unique methodNames of methods without Branches.
	 * @param className
	 *            a {@link java.lang.String} object.
	 */
	public Set<String> getBranchlessMethods(String className) {
		if (!branchlessMethods.containsKey(className))
			return new HashSet<String>();

		return branchlessMethods.get(className).keySet();
	}

	/**
	 * Returns a set with all unique methodNames of methods without Branches.
	 * 
	 * @return A set with all unique methodNames of methods without Branches.
	 * @param className
	 *            a {@link java.lang.String} object.
	 */
	public Set<String> getBranchlessMethodsPrefix(String className) {
		Set<String> methods = new HashSet<String>();

		for (String name : branchlessMethods.keySet()) {
			if (name.equals(className) || name.startsWith(className + "$")) {
				methods.addAll(branchlessMethods.get(name).keySet());
			}
		}

		return methods;
	}

	/**
	 * Returns a set with all unique methodNames of methods without Branches.
	 * 
	 * @return A set with all unique methodNames of methods without Branches.
	 * @param className
	 *            a {@link java.lang.String} object.
	 */
	public Set<String> getBranchlessMethodsMemberClasses(String className) {
		Set<String> methods = new HashSet<String>();

		for (String name : branchlessMethods.keySet()) {
			if (name.equals(className) || name.startsWith(className + "$")) {
				methods.addAll(branchlessMethods.get(name).keySet());
			}
		}

		return methods;
	}

	public int getBranchlessMethodLineNumber(String className, String methodName) {
		// check if the given method is branchless
		if (branchlessMethods.get(className) != null
		        && branchlessMethods.get(className).get(className + "." + methodName) != null) {
			return branchlessMethods.get(className).get(className + "." + methodName);
		}
		// otherwise consult the branchMap and return the lineNumber of the earliest Branch

		return branchlessMethods.get(className).get(className + "." + methodName);
	}

	/**
	 * Returns a set with all unique methodNames of methods without Branches.
	 * 
	 * @return A set with all unique methodNames of methods without Branches.
	 */
	public Set<String> getBranchlessMethods() {
		Set<String> methods = new HashSet<String>();

		for (String name : branchlessMethods.keySet()) {
			methods.addAll(branchlessMethods.get(name).keySet());
		}

		return methods;
	}
	
	public boolean isBranchlessMethod(String className, String methodName) {
		Map<String, Integer> methodMap = branchlessMethods.get(className);
		if(methodMap != null) {
			return methodMap.containsKey(methodName);
		}
		return false;
	}

	/**
	 * Returns the number of methods without Branches for class className
	 * 
	 * @return The number of methods without Branches.
	 * @param className
	 *            a {@link java.lang.String} object.
	 */
	public int getNumBranchlessMethods(String className) {
		if (!branchlessMethods.containsKey(className))
			return 0;
		return branchlessMethods.get(className).size();
	}

	/**
	 * Returns the number of methods without Branches for class className
	 * 
	 * @return The number of methods without Branches.
	 * @param className
	 *            a {@link java.lang.String} object.
	 */
	public int getNumBranchlessMethodsPrefix(String className) {
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
	 * @param className
	 *            a {@link java.lang.String} object.
	 */
	public int getNumBranchlessMethodsMemberClasses(String className) {
		int num = 0;
		for (String name : branchlessMethods.keySet()) {
			if (name.equals(className) || name.startsWith(className + "$"))
				num += branchlessMethods.get(name).size();
		}
		return num;
	}

	/**
	 * Returns the total number of methods without branches in the instrumented
	 * classes
	 * 
	 * @return
	 */
	public int getNumBranchlessMethods() {
		int num = 0;
		for (String name : branchlessMethods.keySet()) {
			num += branchlessMethods.get(name).size();
		}
		return num;
	}

	/**
	 * Returns a Set containing all classes for which this pool knows Branches
	 * for as Strings
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public Set<String> knownClasses() {
		Set<String> r = new HashSet<String>();
		r.addAll(branchMap.keySet());
		r.addAll(branchlessMethods.keySet());

		if (logger.isDebugEnabled()) {
			logger.debug("Known classes: " + r);
		}

		return r;
	}

	/**
	 * Returns a Set containing all methods in the class represented by the
	 * given String for which this pool knows Branches for as Strings
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.util.Set} object.
	 */
	public Set<String> knownMethods(String className) {
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
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<Branch> retrieveBranchesInMethod(String className,
	        String methodName) {
		List<Branch> r = new ArrayList<Branch>();
		if (branchMap.get(className) == null)
			return r;
		List<Branch> branches = branchMap.get(className).get(methodName);
		if (branches != null)
			r.addAll(branches);
		return r;
	}

	/**
	 * <p>
	 * getDefaultBranchForSwitch
	 * </p>
	 * 
	 * @param v
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a {@link org.evosuite.coverage.branch.Branch} object.
	 */
	public Branch getDefaultBranchForSwitch(BytecodeInstruction v) {
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

	/**
	 * Reset all the data structures used to keep track of the branch
	 * information
	 */
	public void reset() {
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
	 * <p>
	 * clear
	 * </p>
	 * 
	 * TODO: One of these two methods should go
	 */
	public void clear() {
		branchCounter = 0;
		branchMap.clear();
		branchIdMap.clear();
		branchlessMethods.clear();
		switchLabels.clear();
		registeredDefaultCases.clear();
		registeredNormalBranches.clear();
		registeredSwitches.clear();
	}

	/**
	 * <p>
	 * clear
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 */
	public void clear(String className) {
		branchMap.remove(className);
		branchlessMethods.remove(className);
	}

	/**
	 * <p>
	 * clear
	 * </p>
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 */
	public void clear(String className, String methodName) {
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
