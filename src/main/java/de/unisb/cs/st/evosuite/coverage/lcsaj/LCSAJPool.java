package de.unisb.cs.st.evosuite.coverage.lcsaj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class LCSAJPool {

	public static Map<String, Map<String, List<LCSAJ>>> lcsaj_map = new HashMap<String, Map<String, List<LCSAJ>>>();

	public static void add_lcsaj(String className, String methodName, LCSAJ lcsaj) {

		if (!lcsaj_map.containsKey(className))
			lcsaj_map.put(className, new HashMap<String, List<LCSAJ>>());
		if (!lcsaj_map.get(className).containsKey(methodName))
			lcsaj_map.get(className).put(methodName, new ArrayList<LCSAJ>());
		lcsaj_map.get(className).get(methodName).add(lcsaj);

		lcsaj.setID(lcsaj_map.get(className).get(methodName).size());
		Logger.getLogger(LCSAJPool.class).info("Adding LCSAJ - "
		                                               + lcsaj_map.get(className).get(methodName).size());

	}

	public static int getLCSAJCount(String className, String methodName) {
		return lcsaj_map.get(className).get(methodName).size();
	}

	public static ArrayList<LCSAJ> getLCSAJs(String className, String methodName)
	        throws IllegalArgumentException {
		ArrayList<LCSAJ> lcsajs = (ArrayList<LCSAJ>) lcsaj_map.get(className).get(methodName);
		if (lcsajs == null) {
			throw new IllegalArgumentException(className + "/" + methodName
			        + " does not exist!");
			//TODO Notify logger.
		}
		return lcsajs;
	}

	public static int getNewLCSAJID(String className, String methodName) {
		return lcsaj_map.get(className).get(methodName).size() + 1;
	}

	public static Map<String, Map<String, List<LCSAJ>>> getLCSAJMap() {
		return lcsaj_map;
	}
}
