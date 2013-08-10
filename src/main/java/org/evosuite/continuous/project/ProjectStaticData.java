package org.evosuite.continuous.project;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Class used to contain all the static data/info of the target project (SUT),
 * like number of classes, branches per class, etc.
 * </p>
 * 
 * <p>
 * A class under test (CUT) is any <i>public</i> class in the SUT, regardless of
 * whether it is concrete, abstract or an interface, but as long as they have
 * any code to test (eg, an abstract class with only abstract methods will not
 * be a CUT). Anonymous and private classes are not CUTs. Protected and package
 * level classes are bit tricky, and at least for the moment they are not
 * considered as CUTs (might change in the future though).
 * </p>
 * 
 * <p>
 * Note: till Java 7, there would be no point in considering an interface as
 * CUT. But, from Java 8 onward, interfaces might have code. Furthermore, when
 * we build the CUT graph, we need to consider interfaces as well, regardless of
 * whether they have any branch (ie code) or not.
 * </p>
 * 
 * @author arcuri
 * 
 */
public class ProjectStaticData {

	/**
	 * Map from CUT full class name (key) to ClassInfo object (value)
	 */
	private final Map<String, ClassInfo> classes;

	private ProjectGraph graph = null;

	public ProjectStaticData() {
		classes = new ConcurrentHashMap<String, ClassInfo>();
	}

	/**
	 * Immutable class representing all the info data for a class
	 * 
	 * @author arcuri
	 * 
	 */
	public static class ClassInfo {
		public final Class<?> theClass;
		public final int numberOfBranches;
		/**
		 * we cannot only consider the number of branches, as there might be
		 * CUTs with code but no branches
		 */
		public final boolean hasCode;

		public ClassInfo(Class<?> theClass, int numberOfBranches, boolean hasCode) {
			super();
			this.theClass = theClass;
			this.numberOfBranches = numberOfBranches;
			this.hasCode = hasCode;
		}

		public String getClassName() {
			return theClass.getName();
		}

		public boolean isTestable() {
			return hasCode;
		}
	}

	/**
	 * Add a new ClassInfo. Note: this is protected, as only classes in this
	 * package should be allowed to modify the state of this class
	 * 
	 * @param info
	 */
	protected void addNewClass(ClassInfo info) {
		classes.put(info.getClassName(), info);
	}

	public boolean containsClass(String c) {
		return classes.containsKey(c);
	}

	/**
	 * Return the class info of the given class (with full qualifying name),
	 * or <code>null</code> if missing
	 * 
	 * @param name
	 * @return
	 */
	public ClassInfo getClassInfo(String name) {
		return classes.get(name);
	}

	/**
	 * Return the number of classes in the project, including non-testable ones
	 * 
	 * @return
	 */
	public int getTotalNumberOfClasses() {
		return classes.size();
	}

	/**
	 * If an abstract class or interface has no coded/implemented method, then
	 * there would be no point in generating test cases for it
	 * 
	 * @return
	 */
	public int getTotalNumberOfTestableCUTs() {
		int total = 0;
		for (ClassInfo info : classes.values()) {
			if (info.isTestable()) {
				total++;
			}
		}
		return total;
	}

	public int getTotalNumberOfBranches() {
		int total = 0;
		for (ClassInfo info : classes.values()) {
			total += info.numberOfBranches;
		}
		return total;

	}

	/**
	 * Return an unmodifiable copy of the current data info of the classes in the SUT 
	 * 
	 * @return
	 */
	public Collection<ClassInfo> getClassInfos() {
		return Collections.unmodifiableCollection(classes.values());
	}

	/**
	 * Return an unmodifiable copy of the names of the classes in the SUT
	 * 
	 * @return
	 */
	public Collection<String> getClassNames(){
		return Collections.unmodifiableCollection(classes.keySet());
	}
	
	/**
	 * Return a read-only view of the current project CUT graph
	 * 
	 * @return
	 */
	public ProjectGraph getProjectGraph() {
		if (graph == null){
			graph = new ProjectGraph(this);
		}
		return graph; //FIXME should be a read-only view
	}
}
