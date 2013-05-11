package org.evosuite.continuous.project;

import java.util.Collection;
import java.util.regex.Pattern;

import org.evosuite.utils.ResourceList;

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
		Pattern pattern = Pattern.compile("[^\\$]*.class");
		Collection<String> resources = ResourceList.getResources(target, pattern);
		
		ProjectStaticData data = new ProjectStaticData();
		
		for (String resource : resources) {
			//TODO: processing/validation should be done here
			
		}
		
		return data;
	}
}
