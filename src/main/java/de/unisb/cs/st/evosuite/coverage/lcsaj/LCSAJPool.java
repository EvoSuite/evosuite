package de.unisb.cs.st.evosuite.coverage.lcsaj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;

public class LCSAJPool {

	public static Map<String, Map<String, List<LCSAJ>>> lcsaj_map = new HashMap<String, Map<String, List<LCSAJ>>>();

	public static Set<Branch> lcsaj_branches = new HashSet<Branch>();

	public static void add_lcsaj(String className, String methodName, LCSAJ lcsaj) {

		if (!lcsaj_map.containsKey(className))
			lcsaj_map.put(className, new HashMap<String, List<LCSAJ>>());
		if (!lcsaj_map.get(className).containsKey(methodName))
			lcsaj_map.get(className).put(methodName, new ArrayList<LCSAJ>());
		lcsaj_map.get(className).get(methodName).add(lcsaj);

		lcsaj.setID(lcsaj_map.get(className).get(methodName).size());
		Logger logger = LoggerFactory.getLogger(LCSAJPool.class);
		logger.info("Adding LCSAJ: " + lcsaj);
		for (Branch branch : lcsaj.getBranchInstructions()) {
			logger.info(" -> " + branch.getInstruction().getASMNodeString());
		}

	}

	public static void addLCSAJBranch(Branch b) {
		lcsaj_branches.add(b);
	}

	public static boolean isLCSAJBranch(Branch b) {
		return lcsaj_branches.contains(b);
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
	
	public static int getLCSAJsPerClass(String className){
		int out = 0;
		for (String methodName : lcsaj_map.get(className).keySet())
			out += getLCSAJCount(className, methodName);
		
		return out;
	}
	
	public static int getMinDependentBranches(String className){
		int min = Integer.MAX_VALUE;
		for (String methodName : lcsaj_map.get(className).keySet())
			for (LCSAJ l : lcsaj_map.get(className).get(methodName)){
				int branches = l.getLastBranch().getInstruction().getAllControlDependentBranches().size();
				if (branches < min)
					min = branches;
		}
		return min;
	}
	
	public static int getMaxDependentBranches(String className){
		int max = Integer.MIN_VALUE;
		for (String methodName : lcsaj_map.get(className).keySet())
			for (LCSAJ l : lcsaj_map.get(className).get(methodName)){
				int branches = l.getLastBranch().getInstruction().getAllControlDependentBranches().size();
					if (branches > max)
						max = branches;
		}
		return max;
	}
	
	public static double getAvgDependentBranches(String className){
		double avg = 0;
		int n = 0;
		for (String methodName : lcsaj_map.get(className).keySet())
			for (LCSAJ l : lcsaj_map.get(className).get(methodName)){
					int branches = l.getLastBranch().getInstruction().getAllControlDependentBranches().size();
					avg += branches;
					n++;
			}
		if (n != 0){
			avg /= n;
			return avg;
		}
		else
			return 0;
	}
	
	public static int getMinLCSAJlength(String className){
		int min = Integer.MAX_VALUE;
		for (String methodName : lcsaj_map.get(className).keySet())
			for (LCSAJ l : lcsaj_map.get(className).get(methodName)){
				if (l.length() < min)
					min = l.length();
		}
		return min;
	}
	
	public static int getMaxLCSAJlength(String className){
		int max = Integer.MIN_VALUE;
		for (String methodName : lcsaj_map.get(className).keySet())
			for (LCSAJ l : lcsaj_map.get(className).get(methodName)){
				if (l.length() > max)
					max = l.length();
		}
		return max;
	}
	
	public static double getAvgLCSAJlength(String className){
		double avg = 0;
		int n = 0;
		for (String methodName : lcsaj_map.get(className).keySet())
			for (LCSAJ l : lcsaj_map.get(className).get(methodName)){
					avg += l.length();
					n++;
			}
		if (n != 0){
			avg /= n;
			return avg;
		}
		else
			return 0;
	}
	
	public static int getInfeasableLCSAJs(String className){
		int out = 0;
		for (String methodName : lcsaj_map.get(className).keySet())
			for (LCSAJ l : lcsaj_map.get(className).get(methodName)){
				if (l.getdPositionReached() == 0)
					out++;
		}
		return out;
	}
	
	public static int getUnfinishedLCSAJs(String className){
		int out = 0;
		for (String methodName : lcsaj_map.get(className).keySet())
			for (LCSAJ l : lcsaj_map.get(className).get(methodName)){
				if (l.getdPositionReached() > 0 && l.getdPositionReached() < l.length()-1)
					out++;
		}
		return out;
	}
	
	
}
