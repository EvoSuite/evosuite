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

	// maps: classname -> methodName  -> DUVarName -> branchID -> List of Definitions in that branch 
	public static Map<String, Map<String, Map<String, Map<Integer,List<Definition>>>>> def_map = new HashMap<String, Map<String, Map<String, Map<Integer,List<Definition>>>>>();

	// maps: classname -> methodName  -> DUVarName -> branchID -> List of Uses in that branch
	public static Map<String, Map<String, Map<String, Map<Integer,List<Use>>>>> use_map = new HashMap<String, Map<String, Map<String, Map<Integer,List<Use>>>>>();	
	
	// maps all known duIDs to their DefUse
	private static Map<Integer,DefUse> duIDsToDefUses = new HashMap<Integer,DefUse>();
	private static Map<Integer,DefUse> duIDsToDefs = new HashMap<Integer,DefUse>();
	private static Map<Integer,DefUse> duIDsToUses = new HashMap<Integer,DefUse>();
	
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
		if(!v.isDefinition())
			throw new IllegalArgumentException("Vertex of a definition or use expected");
		
		defCounter++;
		v.defID = defCounter; // IINCs already have duID set do useCounter value
		if(!v.isUse()) {
			duCounter++;			
			v.duID = duCounter;
		}
		
		Definition d = new Definition(v);
		List<Definition> defs = initDefMap(def_map, d);	
		defs.add(d);
		duIDsToDefUses.put(d.getDUID(),d);
		duIDsToDefs.put(d.getDUID(),d);
		
		logger.info("Found "+d.toString()+" in "+v.methodName+":"+v.branchID+(v.branchExpressionValue?"t":"f")+"("+v.line_no+")");

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

		if(v.isLocalVarUse() && !hasEntryForVariable(def_map, v)) // TODO was an argument
			return false;

		useCounter++;		
		v.useID = useCounter;
		duCounter++;
		v.duID = duCounter;
		
		Use u = new Use(v);
		List<Use> uses = initUseMap(use_map, u);
		uses.add(u);
		duIDsToDefUses.put(u.getDUID(),u);
		duIDsToUses.put(u.getDUID(),u);
		
		logger.info("Found "+u.toString()+" in "+v.methodName+":"+v.branchID+(v.branchExpressionValue?"t":"f")+"("+v.line_no+")");
		

		return true;
	}
	
//	/**
//	 * Returns the DefUse with the given duID
//	 * 
//	 * @param duID ID of a DefUse
//	 * @return The DefUse with the given duID if such an ID is known, null otherwise
//	 */
//	public static DefUse getDefUse(int duID) {
//		return duIDsToDefUses.get(duID);
//	}

	/**
	 * Returns the Use with the given duID
	 * 
	 * @param duID ID of a Use
	 * @return The Use with the given duID if such an ID is known for a Use, null otherwise
	 */
	public static Use getUse(int duID) {
		DefUse du = duIDsToUses.get(duID);
		if(du==null)
			return null;
//		if(!du.isUse()) {
//			logger.warn("getUse() called with the duID of a definition");
//			return null;
//		}
		
		return (Use)du;
	}
	
	/**
	 * Returns the Definition with the given duID
	 * 
	 * @param duID ID of a Definition
	 * @return The Definition with the given duID if such an ID is known for a Definition, null otherwise
	 */	
	public static Definition getDefinition(int duID) {
		DefUse du = duIDsToDefs.get(duID);
		if(du == null)
			return null;
//		if(!du.isDefinition()) {
//			logger.warn("getDefinition() called with the duID of a use");
//			return null;
//		}
		
		return (Definition)du;
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
