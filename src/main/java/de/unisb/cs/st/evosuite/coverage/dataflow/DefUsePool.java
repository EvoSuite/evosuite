package de.unisb.cs.st.evosuite.coverage.dataflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

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
	private static Map<String, Map<String, Map<String, List<Definition>>>> def_map = new HashMap<String, Map<String, Map<String, List<Definition>>>>();

	// className -> methodName -> DUVarName -> List of Uses in that method for
	// the variable
	private static Map<String, Map<String, Map<String, List<Use>>>> use_map = new HashMap<String, Map<String, Map<String, List<Use>>>>();
	// maps IDs to objects
	private static Map<Integer, DefUse> defuseIdsToDefUses = new HashMap<Integer, DefUse>();
	private static Map<Integer, Definition> defuseIdsToDefs = new HashMap<Integer, Definition>();
	private static Map<Integer, Use> defuseIdsToUses = new HashMap<Integer, Use>();

	// maps objects to IDs
	// register of all known DefUse-, Definition- and Use-IDs
	private static Map<BytecodeInstruction, Integer> registeredDUs = new HashMap<BytecodeInstruction, Integer>();
	private static Map<BytecodeInstruction, Integer> registeredDefs = new HashMap<BytecodeInstruction, Integer>();
	private static Map<BytecodeInstruction, Integer> registeredUses = new HashMap<BytecodeInstruction, Integer>();
	// and an extra one to keep track of parameterUses
	private static List<BytecodeInstruction> knownParameterUses = new ArrayList<BytecodeInstruction>();

	// keep track of known DUs and assign IDs accordingly
	private static int defCounter = 0;
	private static int useCounter = 0;
	private static int duCounter = 0;

	/**
	 * Gets called by DefUseInstrumentation whenever it detects a definition
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
	 * 
	 * @param d
	 *            CFGVertex corresponding to a Definition in the CUT
	 */
	public static boolean addAsDefinition(BytecodeInstruction d) {
		if (!d.isDefinition()) {
			logger.error("expect instruction of a definition");
			return false;
		}
		if (isKnownAsDefinition(d)) {
			logger.error("each definition can be added at most once");
			return false;
		}
		if(d.isWithinConstructor() && d.proceedsConstructorInvocation())
			return false;

		// register new instruction

		// IINCs already have duID set so this can fail
		boolean registeredAsDU = registerAsDefUse(d);

		// sanity check for IINCs
		if (!registeredAsDU && !d.isUse())
			throw new IllegalStateException(
					"expect registering to fail only on IINCs");

		registerAsDefinition(d);

		return true;
	}

	/**
	 * Gets called by DefUseInstrumentation whenever it detects a use
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
	 */
	public static boolean addAsUse(BytecodeInstruction u) {
		if (!u.isUse())
			return false;
		if (isKnownAsUse(u))
			return false;
		if(u.isWithinConstructor() && u.proceedsConstructorInvocation())
			return false;
		
		registerAsDefUse(u);

		registerAsUse(u);

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
		if (d.isLocalVarUse() && !knowsDefinitionForVariableOf(d))
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
		String varName = d.getDUVariableName();

		initMap(def_map, className, methodName, varName);

		return def_map.get(className).get(methodName).get(varName).add(d);
	}

	private static boolean addToUseMap(Use u) {
		String className = u.getClassName();
		String methodName = u.getMethodName();
		String varName = u.getDUVariableName();

		initMap(use_map, className, methodName, varName);

		return use_map.get(className).get(methodName).get(varName).add(u);
	}

	private static <T> void initMap(
			Map<String, Map<String, Map<String, List<T>>>> map,
			String className, String methodName, String varName) {

		if (!map.containsKey(className))
			map.put(className, new HashMap<String, Map<String, List<T>>>());
		if (!map.get(className).containsKey(methodName))
			map.get(className).put(methodName, new HashMap<String, List<T>>());
		if (!map.get(className).get(methodName).containsKey(varName))
			map.get(className).get(methodName).put(varName, new ArrayList<T>());
	}

	// functionality to retrieve information from the pool

	public static boolean knowsDefinitionForVariableOf(BytecodeInstruction du) {
		if (!du.isDefUse())
			throw new IllegalArgumentException("defuse expected");

		String className = du.getClassName();
		String methodName = du.getMethodName();
		String varName = du.getDUVariableName();

		try {
			return def_map.get(className).get(methodName).get(varName).size() > 0;
		} catch (NullPointerException nex) {
			// expected
			return false;
		}
	}

	public static boolean isKnown(BytecodeInstruction instruction) {

		return isKnownAsDefinition(instruction) || isKnownAsUse(instruction);
	}

	public static boolean isKnownAsDefinition(BytecodeInstruction instruction) {

		if (!instruction.isDefinition())
			return false;

		return registeredDefs.containsKey(instruction);
	}

	public static boolean isKnownAsUse(BytecodeInstruction instruction) {

		if (!instruction.isUse())
			return false;

		return registeredUses.containsKey(instruction);
	}

	public static Set<Definition> retrieveRegisteredDefinitions() {
		Set<Definition> r = new HashSet<Definition>();
		for (Integer defId : registeredDefs.values()) {
			r.add(getDefinitionByDefId(defId));
		}
		return r;
	}

	public static Set<Use> retrieveRegisteredUses() {
		Set<Use> r = new HashSet<Use>();
		for (Integer useId : registeredUses.values()) {
			r.add(getUseByUseId(useId));
		}
		return r;
	}

	public static Set<Use> retrieveRegisteredParameterUses() {
		Set<Use> r = new HashSet<Use>();
		for (BytecodeInstruction instruction : knownParameterUses) {
			r.add(getUseByUseId(registeredUses.get(instruction)));
		}
		return r;
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

	public static Use getUseByUseId(int useId) {

		for (Use use : defuseIdsToUses.values()) {
			if (use.getUseId() == useId)
				return use;
		}
		return null;
	}

	public static Definition getDefinitionByDefId(int defId) {

		for (Definition def : defuseIdsToDefs.values()) {
			if (def.getDefId() == defId)
				return def;
		}
		return null;
	}

	public static int getRegisteredDefUseId(BytecodeInstruction instruction) {
		if (registeredDUs.containsKey(instruction))
			return registeredDUs.get(instruction);

		return -1;
	}

	public static int getRegisteredDefId(BytecodeInstruction instruction) {
		if (registeredDefs.containsKey(instruction))
			return registeredDefs.get(instruction);

		return -1;
	}

	public static int getRegisteredUseId(BytecodeInstruction instruction) {
		if (registeredUses.containsKey(instruction))
			return registeredUses.get(instruction);

		return -1;
	}

	public static boolean isRegisteredParameterUse(
			BytecodeInstruction instruction) {
		return knownParameterUses.contains(instruction);
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
}
