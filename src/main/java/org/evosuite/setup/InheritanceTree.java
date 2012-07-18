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
 */
/**
 * 
 */
package org.evosuite.setup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Gordon Fraser
 * 
 */
public class InheritanceTree {

	private final Map<String, InheritanceTreeEntry> classInformation = new HashMap<String, InheritanceTreeEntry>();

	private final Map<String, Set<String>> subclassCache = new HashMap<String, Set<String>>();

	public boolean isMethodDefined(String className, String methodName) {
		return false;
	}

	public void addSuperclass(String className, String superName, int access) {
		String classNameWithDots = className.replaceAll("/", ".");
		classInformation.put(classNameWithDots, new InheritanceTreeEntry(
		        classNameWithDots, superName.replaceAll("/", "."), access));

	}

	public void addInterface(String className, String interfaceName) {
		String classNameWithDots = className.replaceAll("/", ".");
		assert (classInformation.containsKey(classNameWithDots));

		classInformation.get(classNameWithDots).addInterface(interfaceName.replaceAll("/",
		                                                                              "."));
	}

	public Set<String> getSubclasses(String className) {
		if (subclassCache.containsKey(className))
			return subclassCache.get(className);

		Set<String> subClasses = new HashSet<String>();
		Set<String> workingSet = getDirectSubclasses(className.replace("/", "."));
		while (!workingSet.isEmpty()) {
			Iterator<String> it = workingSet.iterator();
			String next = it.next();
			subClasses.add(next);
			it.remove();
			Set<String> directSubClasses = getDirectSubclasses(next);
			for (String subClass : directSubClasses) {
				if (!subClasses.contains(subClass)) {
					subClasses.add(subClass);
					workingSet.add(subClass);
				}
			}
		}
		subclassCache.put(className, subClasses);
		return subClasses;
	}

	private Set<String> getDirectSubclasses(String className) {
		Set<String> subClasses = new HashSet<String>();
		for (InheritanceTreeEntry entry : classInformation.values()) {
			if (entry.getSuperClass().equals(className)) {
				subClasses.add(entry.getClassName());
			} else if (entry.getInterfaces().contains(className)) {
				subClasses.add(entry.getClassName());
			}
		}

		return subClasses;
	}

	public List<String> getSuperclasses(String className) {
		List<String> superClasses = new ArrayList<String>();
		String currentClass = className;
		while (hasEntry(currentClass)) {
			InheritanceTreeEntry entry = getEntry(currentClass);
			superClasses.add(entry.getSuperClass());
			currentClass = entry.getSuperClass();
		}
		return superClasses;
	}

	private boolean hasEntry(String className) {
		return classInformation.containsKey(className);
	}

	private InheritanceTreeEntry getEntry(String className) {
		return classInformation.get(className);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "";
		for (InheritanceTreeEntry entry : classInformation.values()) {
			result += entry.toString() + "\n";
		}
		return result;
	}

}
