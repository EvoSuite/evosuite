package org.evosuite.continuous.project;

import java.io.File;
import java.util.Collection;
import java.util.regex.Pattern;

import org.evosuite.Properties;
import org.evosuite.continuous.project.ProjectStaticData.ClassInfo;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.sandbox.Sandbox;
import org.evosuite.utils.ResourceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class is used to analyze and gather all the
 * static information of the target project.
 * </p>
 * 
 * <p>
 * To be useful, this analysis does not need to be 100% precise,
 * as we use the generated statistics <i>only</i> for heuristics
 * </p>
 * 
 * <p>
 * Note: this class assumes the classpath is properly set
 * 
 * @author arcuri
 *
 */
public class ProjectAnalyzer {
 
	private static Logger logger = LoggerFactory.getLogger(ProjectAnalyzer.class);
	
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
		Collection<String> classes = ResourceList.getResources(target, pattern);
		
		ProjectStaticData data = new ProjectStaticData();
		
		for (String fileName : classes) {
			String className = fileName.replace(".class", "").replaceAll(File.separator, ".");

			Class<?> theClass = null; 
			int numberOfBranches = -1;			
			boolean hasCode = false;
			
			Properties.TARGET_CLASS = className;
			InstrumentingClassLoader instrumenting = new InstrumentingClassLoader();
			
			BranchPool.reset();
			
			try{
				/*
				 * to access number of branches, we need to use
				 * instrumenting class loader. But loading a class would
				 * execute its static code, and so we need to 
				 * use a security manager. 
				 */
				Sandbox.goingToExecuteUnsafeCodeOnSameThread();
				instrumenting.loadClass(className);
				
				numberOfBranches = BranchPool.getBranchCounter();
				hasCode = (numberOfBranches > 0) || (BranchPool.getBranchlessMethods().size() > 0);

				/*
				 * just to avoid possible issues with instrumenting classloader
				 */
				theClass = ClassLoader.getSystemClassLoader().loadClass(className);

				//TODO kind
				//if(theClass.isInterface()){
				//	kind = ClassKind.INTERFACE;
				//} else if(theClass.is  Modifier.isAbstract( someClass.getModifiers() );
				
			} catch  (Exception e) {
				logger.warn("Cannot handle "+className);
				continue;
			}
			finally {
				Sandbox.doneWithExecutingUnsafeCodeOnSameThread();
				BranchPool.reset();
				Properties.TARGET_CLASS = "";
			}
			
			data.addNewClass(new ClassInfo(theClass, numberOfBranches, hasCode));
		}
		
		return data;
	}
}

