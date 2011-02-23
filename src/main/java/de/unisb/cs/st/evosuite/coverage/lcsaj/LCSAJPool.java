package de.unisb.cs.st.evosuite.coverage.lcsaj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LCSAJPool {

	public static Map<String, Map<String, ArrayList<LCSAJ>>> lcsaj_map = new HashMap<String, Map<String, ArrayList<LCSAJ>>>();

	private static int num = 0;

	public static void add_lcsaj(String className, String methodName, LCSAJ lcsaj) {

		if (!lcsaj_map.containsKey(className))
			lcsaj_map.put(className, new HashMap<String, ArrayList<LCSAJ>>());
		if (!lcsaj_map.get(className).containsKey(methodName))
			lcsaj_map.get(className).put(methodName, new ArrayList<LCSAJ>());
		lcsaj_map.get(className).get(methodName).add(lcsaj);
		num++;

	}

	public static int getSize() {
		return num;
	}

	public static ArrayList<LCSAJ> getLCSAJs(String className, String methodName)
	        throws IllegalArgumentException {
		ArrayList<LCSAJ> lcsajs = lcsaj_map.get(className).get(methodName);
		if (lcsajs == null) {
			throw new IllegalArgumentException(className + "/" + methodName
			        + " does not exist!");
			//TODO Notify logger.
		}
		return lcsajs;
	}

}
