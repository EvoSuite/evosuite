package org.evosuite.continuous.project;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.event.ListSelectionEvent;

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

	/**
	 * package prefix to select a subset of classes on classpath/target to define
	 * what to run CTG on
	 */
	private final String prefix;

	private transient String[] cutsToAnalyze;

	public ProjectAnalyzer(String target, String prefix) {
		super();
		this.target = target;
		this.prefix = prefix;
		this.cutsToAnalyze = null;
	}

	/**
	 * Instead of scanning for classes in the given target, directly specify
	 * the class names the project is composed by
	 * 
	 * <p>
	 * Note: this constructor is mainly meant for unit tests
	 * @param cuts
	 */
	public ProjectAnalyzer(String[] cuts) throws NullPointerException {
		super();
		if(cuts==null){
			throw new NullPointerException("Input array cannot be null");
		}
		this.target = null;
		this.prefix = null;
		this.cutsToAnalyze = cuts;
	}

	private Collection<String> getCutsToAnalyze(){

		if(cutsToAnalyze!=null){
			return Arrays.asList(cutsToAnalyze);
		}

		Pattern pattern = Pattern.compile("[^\\$]*.class");
		Collection<String> classes = null;

		if(target!=null){
			if(!target.contains(File.pathSeparator)){
				classes = ResourceList.getResources(target, pattern);
			} else {
				classes = new HashSet<>();
				for(String element : target.split(File.pathSeparator)){
					classes.addAll(ResourceList.getResources(element, pattern));
				}
			}
		} else {
			/*
			 * if no target specified, just grab everything on classpath
			 */
			classes = ResourceList.getResources(pattern);
		}

		List<String> cuts = new LinkedList<String>();

		for (String fileName : classes) {
			/*
			 * Using File.separator seems to give problems in Windows
			 */
			String className = fileName.replace(".class", "").replaceAll("/", ".");

			if(prefix!=null && !prefix.isEmpty() && !className.startsWith(prefix)){
				/*
				 * A prefix is defined, but this class does not belong to that package hierarchy
				 */
				continue;
			}

			cuts.add(className);

		}
		return cuts;

	}

	/**
	 * Analyze the classes in the given target
	 * @return
	 */
	public ProjectStaticData analyze(){		

		ProjectStaticData data = new ProjectStaticData();

		for (String className : getCutsToAnalyze()) {
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
				logger.warn("Cannot handle "+className+" due to: "+e.getClass()+" "+e.getMessage());
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

