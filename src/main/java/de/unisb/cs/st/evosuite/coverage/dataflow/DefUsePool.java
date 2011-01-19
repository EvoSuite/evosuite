package de.unisb.cs.st.evosuite.coverage.dataflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

public class DefUsePool {

	private static Logger logger = Logger.getLogger(DefUsePool.class);
	
	// maps: classname -> methodName  -> DUVarName -> branchID -> List of Defs as CFGVertex in that branch 
	public static Map<String, Map<String, Map<String, Map<Integer,List<Definition>>>>> def_map = new HashMap<String, Map<String, Map<String, Map<Integer,List<Definition>>>>>();

	// maps: classname -> methodName  -> DUVarName -> branchID -> List of Defs as CFGVertex in that branch
	public static Map<String, Map<String, Map<String, Map<Integer,List<Use>>>>> use_map = new HashMap<String, Map<String, Map<String, Map<Integer,List<Use>>>>>();	
	
	private static Map<Integer,DefUse> duIDsToDefUses = new HashMap<Integer,DefUse>();
	
	private static int defCounter = 0;
	private static int useCounter = 0;
	
	public static void addDefinition(CFGVertex v) {
		if(!v.isDefinition())
			throw new IllegalArgumentException("Vertex of a definition or use expected");
		
		v.duID = defCounter;
		Definition d = new Definition(v);
		List<Definition> defs = initDefMap(def_map, d);	
		defs.add(d);
		duIDsToDefUses.put(d.getDUID(),d);
		
		logger.info("Found Def "+defCounter+" in "+v.methodName+":"+v.branchID+(v.branchExpressionValue?"t":"f")+"("+v.line_no+")"+" for var "+v.getDUVariableName());
		
		defCounter++;
	}
	
	public static boolean addUse(CFGVertex v) {
		if(!v.isUse()) 
			throw new IllegalArgumentException("Vertex of a use expected");

		if(v.isLocalVarUse() && !hasEntryForVariable(def_map, v))
			return false;

		v.duID = useCounter;
		Use u = new Use(v);
		List<Use> uses = initUseMap(use_map, u);
		uses.add(u);
		duIDsToDefUses.put(u.getDUID(),u);
		
		logger.info("Found Use "+DefUsePool.useCounter+" in "+v.methodName+":"+v.branchID+(v.branchExpressionValue?"t":"f")+"("+v.line_no+")"+" for var "+v.getDUVariableName());
		
		useCounter++;

		return true;
	}
	
	public static DefUse getDefUse(int duID) {
		return duIDsToDefUses.get(duID);
	}
	
	public static Use getUse(int duID) {
		DefUse du = duIDsToDefUses.get(duID);
		if(du==null)
			return null;
		if(!du.isUse()) {
			logger.warn("getUse() called with the duID of a definition");
			return null;
		}
		
		return (Use)du;
	}
	
	public static Definition getDefinition(int duID) {
		DefUse du = duIDsToDefUses.get(duID);
		if(du == null)
			return null;
		if(!du.isDefinition()) {
			logger.warn("getDefinition() called with the duID of a use");
			return null;
		}
		
		return (Definition)du;
	}	

	public static Object getDefCounter() {
		
		return defCounter;
	}
	
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
	
	
	private static List<Definition> initDefMap(
			Map<String, Map<String, Map<String, Map<Integer, List<Definition>>>>> map, Definition d) {

		CFGVertex v = d.getCFGVertex();
		String className = v.className;
		String methodName = v.methodName;
		String varName = v.getDUVariableName();
		int branchID = v.branchID; 
		
		if(!map.containsKey(className))
			map.put(className, new HashMap<String, Map<String, Map<Integer,List<Definition>>>>());
		if(!map.get(className).containsKey(methodName)) 
			map.get(className).put(methodName, new HashMap<String, Map<Integer,List<Definition>>>());
		if(!map.get(className).get(methodName).containsKey(varName))
			map.get(className).get(methodName).put(varName, new HashMap<Integer,List<Definition>>());
		if(!map.get(className).get(methodName).get(varName).containsKey(branchID))
			map.get(className).get(methodName).get(varName).put(branchID, new ArrayList<Definition>());
		
		return map.get(className).get(methodName).get(varName).get(branchID);
	}
	
	private static List<Use> initUseMap(
			Map<String, Map<String, Map<String, Map<Integer, List<Use>>>>> map, Use u) {

		CFGVertex v = u.getCFGVertex();
		String className = v.className;
		String methodName = v.methodName;
		String varName = v.getDUVariableName();
		int branchID = v.branchID; 
		
		if(!map.containsKey(className))
			map.put(className, new HashMap<String, Map<String, Map<Integer,List<Use>>>>());
		if(!map.get(className).containsKey(methodName)) 
			map.get(className).put(methodName, new HashMap<String, Map<Integer,List<Use>>>());
		if(!map.get(className).get(methodName).containsKey(varName))
			map.get(className).get(methodName).put(varName, new HashMap<Integer,List<Use>>());
		if(!map.get(className).get(methodName).get(varName).containsKey(branchID))
			map.get(className).get(methodName).get(varName).put(branchID, new ArrayList<Use>());
		
		return map.get(className).get(methodName).get(varName).get(branchID);
	}	
}
