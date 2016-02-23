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
/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Gordon Fraser
 */
package org.evosuite.coverage.lcsaj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.coverage.branch.Branch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class LCSAJPool {

	/** Constant <code>lcsaj_map</code> */
	public static Map<String, Map<String, List<LCSAJ>>> lcsaj_map = new HashMap<String, Map<String, List<LCSAJ>>>();

	/** Constant <code>lcsaj_branches</code> */
	public static Set<Branch> lcsaj_branches = new HashSet<Branch>();

	/**
	 * <p>add_lcsaj</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 * @param lcsaj a {@link org.evosuite.coverage.lcsaj.LCSAJ} object.
	 */
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

	/**
	 * <p>addLCSAJBranch</p>
	 *
	 * @param b a {@link org.evosuite.coverage.branch.Branch} object.
	 */
	public static void addLCSAJBranch(Branch b) {
		lcsaj_branches.add(b);
	}

	/**
	 * <p>isLCSAJBranch</p>
	 *
	 * @param b a {@link org.evosuite.coverage.branch.Branch} object.
	 * @return a boolean.
	 */
	public static boolean isLCSAJBranch(Branch b) {
		return lcsaj_branches.contains(b);
	}

	/**
	 * <p>getLCSAJCount</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static int getLCSAJCount(String className, String methodName) {
		return lcsaj_map.get(className).get(methodName).size();
	}

	/**
	 * <p>getLCSAJs</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 * @return a {@link java.util.ArrayList} object.
	 * @throws java.lang.IllegalArgumentException if any.
	 */
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

	/**
	 * <p>getNewLCSAJID</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static int getNewLCSAJID(String className, String methodName) {
		return lcsaj_map.get(className).get(methodName).size() + 1;
	}

	/**
	 * <p>getLCSAJMap</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public static Map<String, Map<String, List<LCSAJ>>> getLCSAJMap() {
		return lcsaj_map;
	}

	/**
	 * <p>getLCSAJsPerClass</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static int getLCSAJsPerClass(String className) {
		int out = 0;
		for (String methodName : lcsaj_map.get(className).keySet())
			out += getLCSAJCount(className, methodName);

		return out;
	}

	/**
	 * <p>getMinDependentBranches</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static int getMinDependentBranches(String className) {
		int min = Integer.MAX_VALUE;
		for (String methodName : lcsaj_map.get(className).keySet())
			for (LCSAJ l : lcsaj_map.get(className).get(methodName)) {
				int branches = l.getLastBranch().getInstruction().getControlDependencies().size();
				if (branches < min)
					min = branches;
			}
		return min;
	}

	/**
	 * <p>getMaxDependentBranches</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static int getMaxDependentBranches(String className) {
		int max = Integer.MIN_VALUE;
		for (String methodName : lcsaj_map.get(className).keySet())
			for (LCSAJ l : lcsaj_map.get(className).get(methodName)) {
				int branches = l.getLastBranch().getInstruction().getControlDependencies().size();
				if (branches > max)
					max = branches;
			}
		return max;
	}

	/**
	 * <p>getAvgDependentBranches</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @return a double.
	 */
	public static double getAvgDependentBranches(String className) {
		double avg = 0;
		int n = 0;
		for (String methodName : lcsaj_map.get(className).keySet())
			for (LCSAJ l : lcsaj_map.get(className).get(methodName)) {
				int branches = l.getLastBranch().getInstruction().getControlDependencies().size();
				avg += branches;
				n++;
			}
		if (n != 0) {
			avg /= n;
			return avg;
		} else
			return 0;
	}

	/**
	 * <p>getMinLCSAJlength</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static int getMinLCSAJlength(String className) {
		int min = Integer.MAX_VALUE;
		for (String methodName : lcsaj_map.get(className).keySet())
			for (LCSAJ l : lcsaj_map.get(className).get(methodName)) {
				if (l.length() < min)
					min = l.length();
			}
		return min;
	}

	/**
	 * <p>getMaxLCSAJlength</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static int getMaxLCSAJlength(String className) {
		int max = Integer.MIN_VALUE;
		for (String methodName : lcsaj_map.get(className).keySet())
			for (LCSAJ l : lcsaj_map.get(className).get(methodName)) {
				if (l.length() > max)
					max = l.length();
			}
		return max;
	}

	/**
	 * <p>getAvgLCSAJlength</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @return a double.
	 */
	public static double getAvgLCSAJlength(String className) {
		double avg = 0;
		int n = 0;
		for (String methodName : lcsaj_map.get(className).keySet())
			for (LCSAJ l : lcsaj_map.get(className).get(methodName)) {
				avg += l.length();
				n++;
			}
		if (n != 0) {
			avg /= n;
			return avg;
		} else
			return 0;
	}

	/**
	 * <p>getInfeasableLCSAJs</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static int getInfeasableLCSAJs(String className) {
		int out = 0;
		for (String methodName : lcsaj_map.get(className).keySet())
			for (LCSAJ l : lcsaj_map.get(className).get(methodName)) {
				if (l.getdPositionReached() == 0)
					out++;
			}
		return out;
	}

	/**
	 * <p>getUnfinishedLCSAJs</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @return a int.
	 */
	public static int getUnfinishedLCSAJs(String className) {
		int out = 0;
		for (String methodName : lcsaj_map.get(className).keySet())
			for (LCSAJ l : lcsaj_map.get(className).get(methodName)) {
				if (l.getdPositionReached() > 0
				        && l.getdPositionReached() < l.length() - 1)
					out++;
			}
		return out;
	}

}
