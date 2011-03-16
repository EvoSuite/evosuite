package de.unisb.cs.st.evosuite.coverage.dataflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

/**
 * This class is supposed to hold all the available information concerning Definitions and Uses.
 * 
 * The addDefinition()- and addUse()-Method get called by the CFGMethodAdapter whenever it detects 
 * a CFGVertex that corresponds to a Definition or Use in the class under test. 
 * 
 * @author Andre Mis
 */
public class DefUsePool {

	// maps: className -> methodName  -> DUVarName -> branchID -> List of Definitions in that branch 
	public static Map<String, Map<String, Map<String, Map<Integer,List<Definition>>>>> def_map = new HashMap<String, Map<String, Map<String, Map<Integer,List<Definition>>>>>();

	// maps: className -> methodName  -> DUVarName -> branchID -> List of Uses in that branch
	public static Map<String, Map<String, Map<String, Map<Integer,List<Use>>>>> use_map = new HashMap<String, Map<String, Map<String, Map<Integer,List<Use>>>>>();	
	
	// maps all known duIDs to their DefUse
	private static Map<Integer,DefUse> defuseIdsToDefUses = new HashMap<Integer,DefUse>();
	private static Map<Integer,Definition> defuseIdsToDefs = new HashMap<Integer,Definition>();
	private static Map<Integer,Use> defuseIdsToUses = new HashMap<Integer,Use>();
	
	private static int defCounter = 0;
	private static int useCounter = 0;
	private static int duCounter = 0;
	
	private static Logger logger = Logger.getLogger(DefUsePool.class);
	
	
	/**
	 * Gets called by the CFGMethodAdapter whenever it detects a Definition
	 * 
	 * @param v CFGVertex corresponding to a Definition
	 */
	public static boolean addDefinition(CFGVertex v) {
		if(!v.isDefinition() && !v.isParameterUse) // TODO
			throw new IllegalArgumentException("Vertex of a definition expected");
		
		defCounter++;
		v.defId = defCounter; 
		if(!v.isUse()) {
			// IINCs already have duID set do useCounter value
			duCounter++;			
			v.defuseId = duCounter;
		}
		
		Definition d = new Definition(v);
		addToDefMap(d);	
		defuseIdsToDefUses.put(d.getDefUseId(),d);
		defuseIdsToDefs.put(d.getDefUseId(),d);
		
		logger.info("Added to Definitions: "+d.toString()+" in "+v.methodName+":"+v.branchId+(v.branchExpressionValue?"t":"f")+"("+v.line_no+")");
		return true;
	}

	/**
	 * Gets called by the CFGMethodAdapter whenever it detects a Use
	 * 
	 * @param v CFGVertex corresponding to a Use
	 */
	public static boolean addUse(CFGVertex v) {
		if(!v.isUse()) 
			throw new IllegalArgumentException("Vertex of a use expected");

		if(v.isLocalVarUse()) {
			// was ALOAD_0 ("this")
			if(v.getLocalVar()==0)
				return false;
			// was an argument
			if(!hasEntryForVariable(def_map, v))
				v.isParameterUse = true;
		}

		useCounter++;		
		v.useId = useCounter;
		duCounter++;
		v.defuseId = duCounter;
		
		Use u = new Use(v);
		addToUseMap(u);
		defuseIdsToDefUses.put(u.getDefUseId(),u);
		defuseIdsToUses.put(u.getDefUseId(),u);
		
		logger.info("Added to Uses: "+u.toString()+" in "+v.methodName+":"+v.branchId+(v.branchExpressionValue?"t":"f")+"("+v.line_no+")");
		return true;
	}
	
	/**
	 * Returns the Use with the given duID
	 * 
	 * @param duId ID of a Use
	 * @return The Use with the given duID if such an ID is known for a Use, null otherwise
	 */
	public static Use getUseByDefUseId(int duId) {
		DefUse du = defuseIdsToUses.get(duId);
		if(du==null)
			return null;
		
		return (Use)du;
	}
	
	/**
	 * Returns the Definition with the given duID
	 * 
	 * @param duId ID of a Definition
	 * @return The Definition with the given duID if such an ID is known for a Definition, null otherwise
	 */	
	public static Definition getDefinitionByDefUseId(int duId) {
		DefUse du = defuseIdsToDefs.get(duId);
		if(du == null)
			return null;
		
		return (Definition)du;
	}
	
	public static Use getUseByUseId(int useId) {
		
		for(Use use : defuseIdsToUses.values()) {
			if(use.getUseId() == useId)
				return use;
		}
		return null;
	}
	
	public static Definition getDefinitionByDefId(int defId) {
		
		for(Definition def : defuseIdsToDefs.values()) {
			if(def.getDefId() == defId)
				return def;
		}
		return null;
	}	

	/**
	 * Returns the number of currently known Definitions
	 * 
	 * @return the number of currently known Definitions
	 */
	public static Object getDefCounter() {
		return defCounter;
	}

	/**
	 * Returns the number of currently known Uses
	 * 
	 * @return the number of currently known Uses
	 */	
	public static Object getUseCounter() {
		return useCounter;
	}	
	
	
	private static boolean hasEntryForVariable(
			Map<String, Map<String, Map<String, Map<Integer, List<Definition>>>>> map, CFGVertex v) {
		
		String className = v.className;
		String methodName = v.methodName;
		String varName = v.getDUVariableName();
		
		if(map.get(className) == null)
			return false;
		if(map.get(className).get(methodName) == null)
			return false;
		if(map.get(className).get(methodName).get(varName) == null)
			return false;
		if(map.get(className).get(methodName).get(varName).size() > 0)
			return true;
	
		return false;
	}
	
	
	private static boolean addToDefMap(Definition d) {
		CFGVertex v = d.getCFGVertex();
		String className = v.className;
		String methodName = v.methodName;
		String varName = v.getDUVariableName();
		int branchID = v.branchId; 
		
		if(!def_map.containsKey(className))
			def_map.put(className, new HashMap<String, Map<String, Map<Integer,List<Definition>>>>());
		if(!def_map.get(className).containsKey(methodName)) 
			def_map.get(className).put(methodName, new HashMap<String, Map<Integer,List<Definition>>>());
		if(!def_map.get(className).get(methodName).containsKey(varName))
			def_map.get(className).get(methodName).put(varName, new HashMap<Integer,List<Definition>>());
		if(!def_map.get(className).get(methodName).get(varName).containsKey(branchID))
			def_map.get(className).get(methodName).get(varName).put(branchID, new ArrayList<Definition>());
		
		return def_map.get(className).get(methodName).get(varName).get(branchID).add(d);
	}
	
	private static boolean addToUseMap(Use u) {
		CFGVertex v = u.getCFGVertex();
		String className = v.className;
		String methodName = v.methodName;
		String varName = v.getDUVariableName();
		int branchId = v.branchId; 
		
		if(!use_map.containsKey(className))
			use_map.put(className, new HashMap<String, Map<String, Map<Integer,List<Use>>>>());
		if(!use_map.get(className).containsKey(methodName)) 
			use_map.get(className).put(methodName, new HashMap<String, Map<Integer,List<Use>>>());
		if(!use_map.get(className).get(methodName).containsKey(varName))
			use_map.get(className).get(methodName).put(varName, new HashMap<Integer,List<Use>>());
		if(!use_map.get(className).get(methodName).get(varName).containsKey(branchId))
			use_map.get(className).get(methodName).get(varName).put(branchId, new ArrayList<Use>());
		
		return use_map.get(className).get(methodName).get(varName).get(branchId).add(u);
	}	
}
