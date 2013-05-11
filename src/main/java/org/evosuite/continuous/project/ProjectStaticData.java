package org.evosuite.continuous.project;

import java.util.Map;
import java.util.Set;

/**
 * Class used to contain all the static data/info of the SUT,
 * like number of classes, branches per class, etc
 * 
 * @author arcuri
 *
 */
public class ProjectStaticData {

	private Set<String> cuts;
	private Set<String> nonTestable;
	private Map<String,ClassInfo> classes;
		
	private static class ClassInfo{
		public final Class<?> theClass;
		public final int numberOfBranches;
		
		public ClassInfo(Class<?> theClass, int numberOfBranches) {
			super();
			this.theClass = theClass;
			this.numberOfBranches = numberOfBranches;
		}		
	}
	
	//FIXME: likely different input
	public void addClass(String fullName){
		//TODO
	}
}
