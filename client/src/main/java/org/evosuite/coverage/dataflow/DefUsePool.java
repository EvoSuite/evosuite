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
package org.evosuite.coverage.dataflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is supposed to hold all the available information concerning
 * Definitions and Uses.
 * 
 * The addDefinition()- and addUse()-Method get called by the
 * DefUseInstrumentation whenever it detects a BytecodeInstruction that
 * corresponds to a Definition or Use in the class under test.
 * 
 * BytecodeInstructions that are not known to this pool can not be instantiated
 * as Definition or Use by the DefUseFactory
 * 
 * @author Andre Mis
 */
public class DefUsePool {

	private static Logger logger = LoggerFactory.getLogger(DefUsePool.class);

	// trees of all known definitions and uses

	// className -> methodName -> DUVarName -> List of Definitions in that
	// method for the variable
	private static Map<String, Map<String, Map<String, List<Definition>>>> defMap = new HashMap<String, Map<String, Map<String, List<Definition>>>>();

	// className -> methodName -> DUVarName -> List of Uses in that method for
	// the variable
	private static Map<String, Map<String, Map<String, List<Use>>>> useMap = new HashMap<String, Map<String, Map<String, List<Use>>>>();
	// maps IDs to objects
	private static Map<Integer, DefUse> defuseIdsToDefUses = new HashMap<Integer, DefUse>();
	private static Map<Integer, Definition> defuseIdsToDefs = new HashMap<Integer, Definition>();
	private static Map<Integer, Use> defuseIdsToUses = new HashMap<Integer, Use>();

	// maps objects to IDs
	// register of all known DefUse-, Definition- and Use-IDs
	private static Map<BytecodeInstruction, Integer> registeredDUs = new HashMap<BytecodeInstruction, Integer>();
	private static Map<BytecodeInstruction, Integer> registeredDefs = new HashMap<BytecodeInstruction, Integer>();
	private static Map<BytecodeInstruction, Integer> registeredUses = new HashMap<BytecodeInstruction, Integer>();
	// an extra one to keep track of parameterUses
	private static List<BytecodeInstruction> knownParameterUses = new ArrayList<BytecodeInstruction>();
	// and an extra one to keep track of field method calls
	private static List<BytecodeInstruction> knownFieldMethodCalls = new ArrayList<BytecodeInstruction>();

	// keep track of known DUs and assign IDs accordingly
	private static int defCounter = 0;
	private static int useCounter = 0;
	private static int duCounter = 0;

	/**
	 * Gets called by DefUseInstrumentation whenever it detects a definition as
	 * defined by ASMWrapper.isDefinition()
	 * 
	 * Registers the given instruction as a definition, assigns a fresh defId
	 * for it and if the given instruction does not represent an IINC also
	 * assigns a fresh defUseId to the given instruction.
	 * 
	 * Warning: - Should the instruction be an IINC it is expected to have
	 * passed addAsUse() first! - if registering of the given instruction fails
	 * (like it does for IINCs due to the fact above) for any reason this method
	 * throws an IllegalStateException!
	 * 
	 * Return false if the given instruction does not represent an instruction
	 * which is a definition as defined in ASMWrapper.isDefinition().
	 * 
	 * Should isKnownAsDefinition() return true for the instruction before
	 * calling this method, it also returns false. After the call
	 * isKnownAsDefinition() is expected to return true for the instruction at
	 * hand however.
	 * 
	 * @param d
	 *            CFGVertex corresponding to a Definition in the CUT
	 * @return a boolean.
	 */
	public static boolean addAsDefinition(BytecodeInstruction d) {
		if (!(d.isDefinition() || d.isMethodCallOfField())) {
			logger.error("expect instruction of a definition");
			return false;
		}
		if (isKnownAsDefinition(d)) {
			logger.error("each definition can be added at most once");
			return false;
		}
		if (!d.canBeInstrumented())
			return false;

//		if (d.isLocalArrayDefinition())
//			LoggingUtils.getEvoLogger().info("registering LOCAL ARRAY VAR DEF "
//			                                         + d.toString());

		// register instruction

		// IINCs and field method calls already have duID set so this can fail
		boolean registeredAsDU = registerAsDefUse(d);

		// sanity check for IINCs
		if (!registeredAsDU && !(d.isIINC() || d.isMethodCallOfField()))
			throw new IllegalStateException(
			        "expect registering to fail only on IINCs and field method calls");

		registerAsDefinition(d);

//		if (d.isMethodCallOfField())
//			LoggingUtils.getEvoLogger().info("Registered field method call as Definition "
//			                                         + d.toString());

		return true;
	}

	/**
	 * Gets called by DefUseInstrumentation whenever it detects a use as defined
	 * by ASMWrapper.isUse()
	 * 
	 * Registers the given instruction as a use, assigns a fresh useId a fresh
	 * defUseId to the given instruction.
	 * 
	 * Return false if the given instruction does not represent an instruction
	 * which is a use as defined in ASMWrapper.isUse().
	 * 
	 * Should isKnown() return true for the instruction before calling this
	 * method, it also returns false.
	 * 
	 * After the call isKnown() and isKnownAsUse() are expected to return true
	 * for the instruction at hand however.
	 * 
	 * @param u
	 *            CFGVertex corresponding to a Use in the CUT
	 * @return a boolean.
	 */
	public static boolean addAsUse(BytecodeInstruction u) {
		if (!(u.isUse() || u.isMethodCallOfField()))
			return false;
		if (isKnownAsUse(u))
			return false;
		if (!u.canBeInstrumented())
			return false;

		// register instruction

		// field method calls already have duID set so this can fail
		boolean registeredAsDU = registerAsDefUse(u);

		// sanity check for IINCs
		if (!registeredAsDU && !u.isMethodCallOfField())
			throw new IllegalStateException(
			        "expect registering to fail only on field method calls");

		registerAsUse(u);

//		if (u.isMethodCallOfField())
//			LoggingUtils.getEvoLogger().info("Registered field method call as Use "
//			                                         + u.toString());

		return true;
	}

	/**
	 * Gets called by DefUseInstrumentation whenever it detects a field method
	 * call as defined by ASMWrapper.isMethodCallField()
	 * 
	 * It is not clear whether a field method call represents a Definition or
	 * Use when it is first detected. Later on the DefUseCoverageFactory (TODO
	 * or some other part of evosuite) will decide this using the complete CCFGs
	 * and their purity analysis, which are not available when
	 * DefUseInstrumentation has its turn.
	 * 
	 * The instrumentation will call a special method of the ExecutionTracer
	 * which will redirect the instrumentation call to either passedDefinition()
	 * or passedUse() depending on how the given instruction will be categorized
	 * later on.
	 * 
	 * Registers the given instruction as a field method call and assigns a
	 * fresh defUseId to the given instruction.
	 * 
	 * Return false if the given instruction does not represent an instruction
	 * which is a use as defined in ASMWrapper.isUse().
	 * 
	 * Should isKnown() return true for the instruction before calling this
	 * method, it also returns false.
	 * 
	 * After the call isKnown() and isKnownAsUse() are expected to return true
	 * for the instruction at hand however.
	 * 
	 * @param u
	 *            CFGVertex corresponding to a Use in the CUT
	 * @return a boolean.
	 */
	public static boolean addAsFieldMethodCall(BytecodeInstruction f) {
		if (!f.isMethodCallOfField())
			return false;
		if (!f.canBeInstrumented())
			return false;

		registerAsDefUse(f);

		registerAsFieldMethodCall(f);

		return true;
	}

	// registering

	private static boolean registerAsDefUse(BytecodeInstruction d) {
		if (registeredDUs.containsKey(d))
			return false;

		// assign fresh defUseId
		duCounter++;
		registeredDUs.put(d, duCounter);

		return true;
	}

	private static boolean registerAsDefinition(BytecodeInstruction d) {
		if (!registeredDUs.containsKey(d))
			throw new IllegalStateException(
			        "expect registerAsDefUse() to be called before registerAsDefinition()/Use()");
		if (registeredDefs.containsKey(d))
			return false;

		// assign fresh defId
		defCounter++;
		registeredDefs.put(d, defCounter);

		// now the first Definition instance for this instruction can be created
		Definition def = DefUseFactory.makeDefinition(d);

//		if (d.isLocalArrayDefinition())
//			LoggingUtils.getEvoLogger().info("succesfully registered LOCAL ARRAY VAR DEF "
//			                                         + def.toString());

		// finally add the Definition to all corresponding maps
		fillDefinitionMaps(def);
		return true;
	}

	private static boolean registerAsUse(BytecodeInstruction d) {
		if (!registeredDUs.containsKey(d))
			throw new IllegalStateException(
			        "expect registerAsDefUse() to be called before registerAsDefinition()/Use()");
		if (registeredUses.containsKey(d))
			return false;

		// assign fresh useId
		useCounter++;
		registeredUses.put(d, useCounter);

		// check if this particular use is a parameterUse
		if (d.isLocalVariableUse() && !knowsDefinitionForVariableOf(d))
			registerParameterUse(d);

		// now the first Use instance for this instruction can be created
		Use use = DefUseFactory.makeUse(d);

		// finally add the use to all corresponding maps
		fillUseMaps(use);
		return true;
	}

	private static void registerParameterUse(BytecodeInstruction d) {
		if (!knownParameterUses.contains(d))
			knownParameterUses.add(d);
	}

	private static void registerAsFieldMethodCall(BytecodeInstruction f) {
		if (!knownFieldMethodCalls.contains(f))
			knownFieldMethodCalls.add(f);
	}

	private static void fillDefinitionMaps(Definition def) {
		addToDefMap(def);
		defuseIdsToDefUses.put(def.getDefUseId(), def);
		defuseIdsToDefs.put(def.getDefUseId(), def);

		logger.debug("Added to DefUsePool as def: " + def.toString());
	}

	private static void fillUseMaps(Use use) {
		addToUseMap(use);
		defuseIdsToDefUses.put(use.getDefUseId(), use);
		defuseIdsToUses.put(use.getDefUseId(), use);

		logger.debug("Added to DefUsePool as use: " + use.toString());
	}

	// filling the maps

	private static boolean addToDefMap(Definition d) {
		String className = d.getClassName();
		String methodName = d.getMethodName();
		String varName = d.getVariableName();

		initMap(defMap, className, methodName, varName);

		return defMap.get(className).get(methodName).get(varName).add(d);
	}

	private static boolean addToUseMap(Use u) {
		String className = u.getClassName();
		String methodName = u.getMethodName();
		String varName = u.getVariableName();

		initMap(useMap, className, methodName, varName);

		return useMap.get(className).get(methodName).get(varName).add(u);
	}

	private static <T> void initMap(Map<String, Map<String, Map<String, List<T>>>> map,
	        String className, String methodName, String varName) {

		if (!map.containsKey(className))
			map.put(className, new HashMap<String, Map<String, List<T>>>());
		if (!map.get(className).containsKey(methodName))
			map.get(className).put(methodName, new HashMap<String, List<T>>());
		if (!map.get(className).get(methodName).containsKey(varName))
			map.get(className).get(methodName).put(varName, new ArrayList<T>());
	}

	// functionality to retrieve information from the pool

	/**
	 * <p>
	 * knowsDefinitionForVariableOf
	 * </p>
	 * 
	 * @param du
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a boolean.
	 */
	public static boolean knowsDefinitionForVariableOf(BytecodeInstruction du) {
		if (!du.isDefUse())
			throw new IllegalArgumentException("defuse expected");

		String className = du.getClassName();
		String methodName = du.getMethodName();
		String varName = du.getVariableName();

		try {
			return defMap.get(className).get(methodName).get(varName).size() > 0;
		} catch (NullPointerException nex) {
			// expected
			return false;
		}
	}

	/**
	 * <p>
	 * isKnown
	 * </p>
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a boolean.
	 */
	public static boolean isKnown(BytecodeInstruction instruction) {

		return isKnownAsDefinition(instruction) || isKnownAsUse(instruction);
	}

	/**
	 * <p>
	 * isKnownAsDefinition
	 * </p>
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a boolean.
	 */
	public static boolean isKnownAsDefinition(BytecodeInstruction instruction) {
		return registeredDefs.containsKey(instruction);
	}

	public static boolean isKnownAsDefinition(int defuseId) {
		return defuseIdsToDefs.containsKey(defuseId);
	}

	public static boolean isKnownAsUse(int defuseId) {
		return defuseIdsToUses.containsKey(defuseId);
	}

	/**
	 * <p>
	 * isKnownAsUse
	 * </p>
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a boolean.
	 */
	public static boolean isKnownAsUse(BytecodeInstruction instruction) {
		return registeredUses.containsKey(instruction);
	}

	/**
	 * <p>
	 * isKnownAsFieldMethodCall
	 * </p>
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a boolean.
	 */
	public static boolean isKnownAsFieldMethodCall(BytecodeInstruction instruction) {
		return knownFieldMethodCalls.contains(instruction);
	}

	/**
	 * <p>
	 * isKnownAsParameterUse
	 * </p>
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a boolean.
	 */
	public static boolean isKnownAsParameterUse(BytecodeInstruction instruction) {
		return knownParameterUses.contains(instruction);
	}

	/**
	 * <p>
	 * retrieveRegisteredDefinitions
	 * </p>
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<Definition> retrieveRegisteredDefinitions() {
		Set<Definition> r = new HashSet<Definition>();
		for (Integer defId : registeredDefs.values()) {
			r.add(getDefinitionByDefId(defId));
		}
		return r;
	}

	/**
	 * <p>
	 * retrieveRegisteredUses
	 * </p>
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<Use> retrieveRegisteredUses() {
		Set<Use> r = new HashSet<Use>();
		for (Integer useId : registeredUses.values()) {
			r.add(getUseByUseId(useId));
		}
		return r;
	}

	public static Set<BytecodeInstruction> retrieveFieldMethodCalls() {
		return new HashSet<BytecodeInstruction>(knownFieldMethodCalls);

	}

	/**
	 * <p>
	 * retrieveRegisteredParameterUses
	 * </p>
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<Use> retrieveRegisteredParameterUses() {
		Set<Use> r = new HashSet<Use>();
		for (BytecodeInstruction instruction : knownParameterUses) {
			r.add(getUseByUseId(registeredUses.get(instruction)));
		}
		return r;
	}

	/**
	 * <p>
	 * getDefinitionByInstruction
	 * </p>
	 * 
	 * @param def
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a {@link org.evosuite.coverage.dataflow.Definition} object.
	 */
	public static Definition getDefinitionByInstruction(BytecodeInstruction def) {
		if (!isKnownAsDefinition(def))
			return null;

		return getDefinitionByDefId(getRegisteredDefId(def));
	}

	/**
	 * <p>
	 * getUseByInstruction
	 * </p>
	 * 
	 * @param use
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a {@link org.evosuite.coverage.dataflow.Use} object.
	 */
	public static Use getUseByInstruction(BytecodeInstruction use) {
		if (!isKnownAsUse(use))
			return null;

		return getUseByUseId(getRegisteredUseId(use));
	}

	/**
	 * Returns the Use with the given duID
	 * 
	 * @param duId
	 *            ID of a Use
	 * @return The Use with the given duID if such an ID is known for a Use,
	 *         null otherwise
	 */
	public static Use getUseByDefUseId(int duId) {
		DefUse du = defuseIdsToUses.get(duId);
		if (du == null)
			return null;

		return (Use) du;
	}

	/**
	 * Returns the Definition with the given duID
	 * 
	 * @param duId
	 *            ID of a Definition
	 * @return The Definition with the given duID if such an ID is known for a
	 *         Definition, null otherwise
	 */
	public static Definition getDefinitionByDefUseId(int duId) {
		DefUse du = defuseIdsToDefs.get(duId);
		if (du == null)
			return null;

		return (Definition) du;
	}

	/**
	 * <p>
	 * getUseByUseId
	 * </p>
	 * 
	 * @param useId
	 *            a int.
	 * @return a {@link org.evosuite.coverage.dataflow.Use} object.
	 */
	public static Use getUseByUseId(int useId) {

		for (Use use : defuseIdsToUses.values()) {
			if (use.getUseId() == useId)
				return use;
		}
		return null;
	}

	/**
	 * <p>
	 * getDefinitionByDefId
	 * </p>
	 * 
	 * @param defId
	 *            a int.
	 * @return a {@link org.evosuite.coverage.dataflow.Definition} object.
	 */
	public static Definition getDefinitionByDefId(int defId) {

		for (Definition def : defuseIdsToDefs.values()) {
			if (def.getDefId() == defId)
				return def;
		}
		return null;
	}

	/**
	 * <p>
	 * getRegisteredDefUseId
	 * </p>
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a int.
	 */
	public static int getRegisteredDefUseId(BytecodeInstruction instruction) {
		if (registeredDUs.containsKey(instruction))
			return registeredDUs.get(instruction);

		return -1;
	}

	/**
	 * <p>
	 * getRegisteredDefId
	 * </p>
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a int.
	 */
	public static int getRegisteredDefId(BytecodeInstruction instruction) {
		if (registeredDefs.containsKey(instruction))
			return registeredDefs.get(instruction);

		return -1;
	}

	/**
	 * <p>
	 * getRegisteredUseId
	 * </p>
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a int.
	 */
	public static int getRegisteredUseId(BytecodeInstruction instruction) {
		if (registeredUses.containsKey(instruction))
			return registeredUses.get(instruction);

		return -1;
	}

	/**
	 * Returns the number of currently known Definitions
	 * 
	 * @return the number of currently known Definitions
	 */
	public static int getDefCounter() {
		return defCounter;
	}

	/**
	 * Returns the number of currently known Uses
	 * 
	 * @return the number of currently known Uses
	 */
	public static int getUseCounter() {
		return useCounter;
	}

	/**
	 * Returns the number of currently known DUs
	 * 
	 * @return the number of currently known DUs
	 */
	public static int getDefUseCounter() {
		return duCounter;
	}

	/**
	 * Determine the number of DefUse pairs for the given Def
	 * 
	 * @param def
	 * @return
	 */
	public static int getDefUseCounterForDef(Definition def) {
		int count = 0;
		if (def == null)
			return 1; // FIXXME - what is this?

		for (Definition d : defuseIdsToDefs.values()) {
			if (d.getDefId() == def.getDefId())
				count++;
		}
		return count;
	}
	
	public static void clear() {
		defMap.clear();
		useMap.clear();
		defuseIdsToDefUses.clear();
		defuseIdsToDefs.clear();
		defuseIdsToUses.clear();
		registeredDUs.clear();
		registeredDefs.clear();
		registeredUses.clear();
		knownParameterUses.clear();
		knownFieldMethodCalls.clear();
		defCounter = 0;
		useCounter = 0;
		duCounter = 0;
		DefUseCoverageFactory.clear();		
	}
}
