package org.evosuite.continuous.project;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Class used to contain all the static data/info of the target project (SUT),
 * like number of classes, branches per class, etc.
 * </p>
 * 
 * <p>
 * A class under test (CUT) is any <i>public</i> class in the SUT,
 * regardless of whether it is concrete, abstract or an interface.
 * Anonymous and private classes are not CUTs.
 * Protected and package level classes are bit tricky, and at least for the moment
 * they are not considered as CUTs (might change in the future though). 
 * </p>
 * 
 * <p>
 * Note: till Java 7, there would be no point in considering an interface as CUT.
 * But, from Java 8 onward, interfaces might have code.
 * Furthermore, when we build the CUT graph, we need to consider interfaces as well,
 * regardless of whether they have any branch (ie code) or not.
 * </p>
 * 
 * @author arcuri
 *
 */
public class ProjectStaticData {

	public enum ClassKind {CONCRETE,ABSTRACT,INTERFACE};
	
	/**
	 * Map from CUT full class name (key) to ClassInfo object (value)
	 */
	private Map<String,ClassInfo> classes;
	
	private ProjectGraph graph;
	
	/**
	 * Immutable class representing all the info data for a class 
	 * 
	 * @author arcuri
	 *
	 */
	public static class ClassInfo{
		public final Class<?> theClass;
		public final int numberOfBranches;
		public final ClassKind kind;
		public final int numberOfImplementedMethods;
		
		public ClassInfo(Class<?> theClass, int numberOfBranches,
				ClassKind kind, int numberOfImplementedMethods) {
			super();
			this.theClass = theClass;
			this.numberOfBranches = numberOfBranches;
			this.kind = kind;
			this.numberOfImplementedMethods = numberOfImplementedMethods;
		}
		
		public String getClassName(){
			return theClass.getName();
		}
		
		public boolean isTestable(){
			return numberOfImplementedMethods > 0;
		}
	}
	
	
	/**
	 * Return the number of classes in the project that can be used as CUT,
	 * including non-testable ones
	 * @return
	 */
	public int getTotalNumberOfCUTs(){
		return classes.size();
	}
	
	/**
	 * If an abstract class or interface has no coded/implemented
	 * method, then there would be no point in generating test 
	 * cases for it
	 * 
	 * @return
	 */
	public int getTotalNumberOfTestableCUTs(){
		int total = 0;
		for(ClassInfo info : classes.values()){
			if(info.numberOfImplementedMethods > 0 ){
				total++;
			}
		}
		return total;
	}
	
	/**
	 * Return an unmodifiable copy of the current CUTs' data info 
	 * 
	 * @return
	 */
	public Collection<ClassInfo> getClassInfos(){
		return Collections.unmodifiableCollection(classes.values());
	}
	
	/**
	 * Return a read-only view of the current project CUT graph
	 * 
	 * @return
	 */
	public ProjectGraph getProjectGraph() {
		return graph; //FIXME should be a read-only view
	}
}
