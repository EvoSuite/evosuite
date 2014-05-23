package org.evosuite.classpath;

import java.util.HashMap;
import java.util.Map;

public class ResourceCache {


	/**
	 * Cache of class names visited to avoid repeated checking of classpath for
	 * existing files
	 */
	private  Map<String, Boolean> classNameCache = new HashMap<String, Boolean>();
	
	public boolean hasClass(String className) {
		if (!classNameCache.containsKey(className)){
			classNameCache.put(className, ResourceList.getClassAsResource(className) != null);
		}

		return classNameCache.get(className);
	}
}
