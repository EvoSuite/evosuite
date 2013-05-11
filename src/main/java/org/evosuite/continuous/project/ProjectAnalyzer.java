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
	 * The full classpath of the project and dependecies
	 */
	private String classPath;
	
	/**
	 * the folder where to find the .class files used as CUTs
	 */
	private String targetFolder;

	public ProjectAnalyzer(String classPath, String targetFolder) {
		super();
		this.classPath = classPath;
		this.targetFolder = targetFolder;
	}
	
	public ProjectStaticData analyze(){
		//TODO
		return null;
	}
}
