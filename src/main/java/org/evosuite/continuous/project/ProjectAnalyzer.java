package org.evosuite.continuous.project;

/**
 * This class is used to analyze and gather all the
 * static information of the target project.
 * 
 * @author arcuri
 *
 */
public class ProjectAnalyzer {
 

	/**
	 * the folder/jar where to find the .class files used as CUTs
	 */
	private final String target;

	public ProjectAnalyzer(String target) {
		super();
		this.target = target;
	}
	
	public ProjectStaticData analyze(){
		//TODO
		return null;
	}
}
