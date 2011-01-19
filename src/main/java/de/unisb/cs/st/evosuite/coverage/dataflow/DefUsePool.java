package de.unisb.cs.st.evosuite.coverage.dataflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

public class DefUsePool {

	// maps: classname -> methodName  -> DUVarName -> branchID -> List of Defs as CFGVertex in that branch 
	public static Map<String, Map<String, Map<String, Map<Integer,List<CFGVertex>>>>> def_map = new HashMap<String, Map<String, Map<String, Map<Integer,List<CFGVertex>>>>>();

	// maps: classname -> methodName  -> DUVarName -> branchID -> List of Defs as CFGVertex in that branch
	public static Map<String, Map<String, Map<String, Map<Integer,List<CFGVertex>>>>> use_map = new HashMap<String, Map<String, Map<String, Map<Integer,List<CFGVertex>>>>>();	
	
	public static int def_counter = 0;
	public static int use_counter = 0;
	
	public static void addDefinition(CFGVertex v) {
		if(!v.isDefinition())
			throw new IllegalArgumentException("Vertex of a definition or use expected");
		
		v.duID = def_counter;
		List<CFGVertex> defs = initDefUseMap(def_map, v);	
		defs.add(v);
		def_counter++;		
	}
	
	public static boolean addUse(CFGVertex v) {
		if(!v.isUse()) 
			throw new IllegalArgumentException("Vertex of a use expected");
		
		if(v.isLocalVarUse() && !hasEntryForVariable(def_map, v))
			return false;
		
		v.duID = use_counter;
		List<CFGVertex> uses = initDefUseMap(use_map, v);
		uses.add(v);						
		use_counter++;

		return true;
	}

	private static boolean hasEntryForVariable(
			Map<String, Map<String, Map<String, Map<Integer, List<CFGVertex>>>>> map, CFGVertex v) {
		
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
	
	
	private static List<CFGVertex> initDefUseMap(
			Map<String, Map<String, Map<String, Map<Integer, List<CFGVertex>>>>> map, CFGVertex v) {

		String className = v.className;
		String methodName = v.methodName;
		String varName = v.getDUVariableName();
		int branchID = v.branchID; 
		
		if(!map.containsKey(className))
			map.put(className, new HashMap<String, Map<String, Map<Integer,List<CFGVertex>>>>());
		if(!map.get(className).containsKey(methodName)) 
			map.get(className).put(methodName, new HashMap<String, Map<Integer,List<CFGVertex>>>());

		
		if(!map.get(className).get(methodName).containsKey(varName))
			map.get(className).get(methodName).put(varName, new HashMap<Integer,List<CFGVertex>>());
		if(!map.get(className).get(methodName).get(varName).containsKey(branchID))
			map.get(className).get(methodName).get(varName).put(branchID, new ArrayList<CFGVertex>());
		
		return map.get(className).get(methodName).get(varName).get(branchID);
	}
}
