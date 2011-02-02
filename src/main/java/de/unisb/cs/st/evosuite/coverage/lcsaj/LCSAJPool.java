package de.unisb.cs.st.evosuite.coverage.lcsaj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

public class LCSAJPool {

	private static Map<String, Map<String, ArrayList<LCSAJ>>> lcsaj_map = new HashMap<String, Map<String, ArrayList<LCSAJ>>>();
	
	public static void add_lcsaj(String methodName, String className, LCSAJ lcsaj ){
		
		if (!lcsaj_map.containsKey(className))
			lcsaj_map.put(className,
					new HashMap<String, ArrayList<LCSAJ>>());
		if (!lcsaj_map.get(className).containsKey(methodName))
			lcsaj_map.get(className).put(methodName,
					new ArrayList<LCSAJ>());
		lcsaj_map.get(className).get(methodName)
				.add(lcsaj);
		
	}
	
	public static ArrayList<LCSAJ> getLCSAJs(String className, String methodName) throws IllegalArgumentException{
		ArrayList<LCSAJ> lcsajs = (ArrayList<LCSAJ>)lcsaj_map.get(className).get(methodName);
		if (lcsajs == null){
			throw new IllegalArgumentException(className+"/"+methodName+" does not exist!");
			//TODO Notify logger.
		}
		return lcsajs;
		}

}
