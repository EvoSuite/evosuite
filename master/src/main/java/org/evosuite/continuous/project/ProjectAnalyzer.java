package org.evosuite.continuous.project;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.AvailableSchedule;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.classpath.ResourceList;
import org.evosuite.continuous.job.schedule.HistorySchedule;
import org.evosuite.continuous.project.ProjectStaticData.ClassInfo;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.junit.CoverageAnalysis;
import org.evosuite.runtime.sandbox.Sandbox;
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

	private transient Set<String> cutsToAnalyze;
	
	/**
	 * When specifying a set of CUTs, still check if they do exist (eg scan folder to search for
	 * them), instead of justing using them directly (and get errors later)
	 * 
	 */
	private boolean validateCutsToAnalyze;

	/**
	 * Main constructor
	 * 
	 * @param target
	 * @param prefix
	 * @param cuts
	 */
	public ProjectAnalyzer(String target, String prefix, String[] cuts) {
		super();
		this.target = target;
		this.prefix = prefix==null ? "" : prefix;
		this.validateCutsToAnalyze = true;

		if(cuts == null){
			this.cutsToAnalyze = null;			
		} else {
			this.cutsToAnalyze = new LinkedHashSet<>();
			for(String s : cuts){
				if(s!=null && !s.isEmpty()){
					cutsToAnalyze.add(s.trim());
				}
			}
		}
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
		this.validateCutsToAnalyze = false;
		this.cutsToAnalyze = new LinkedHashSet<>();		
		cutsToAnalyze.addAll(Arrays.asList(cuts));
	}

	private Collection<String> getCutsToAnalyze(){

		if(cutsToAnalyze!=null && !validateCutsToAnalyze){
			// this is meanly in test cases
			return cutsToAnalyze;
		}

		Set<String> suts = null;

		if(target!=null){
			if(!target.contains(File.pathSeparator)){
				suts = ResourceList.getAllClasses(target, prefix, false);
			} else {
				suts = new LinkedHashSet<String>();
				for(String element : target.split(File.pathSeparator)){
					suts.addAll(ResourceList.getAllClasses(element, prefix, false));
				}
			}
		} else {
			/*
			 * if no target specified, just grab everything on SUT classpath
			 */
			suts = ResourceList.getAllClasses(ClassPathHandler.getInstance().getTargetProjectClasspath(), prefix, false);
		}

		List<String> cuts = new LinkedList<String>();

		for (String className : suts) {
			
			if(cutsToAnalyze!=null && !cutsToAnalyze.contains(className)){
				/*
				 * Note: if this is happens, it is not necessarily an error.
				 * For example, this will happen when CTG is run on a multi-module
				 * maven project 
				 */
				continue;
			}
			
			try {
				Class<?> clazz = Class.forName(className);
				if (!CoverageAnalysis.isTest(clazz)){
					cuts.add(className);
				}
			} catch (ClassNotFoundException e) {
				logger.error(""+e,e);
			} catch(ExceptionInInitializerError | NoClassDefFoundError e){
                /**
                 * TODO: for now we skip it, but at a certain point
                 * we should able to handle it, especially if it
                 * is due to static state initialization
                 */
                logger.warn("Cannot initialize class: "+className);
            }

		}
		return cuts;

	}

	/**
	 * Analyze the classes in the given target
	 * @return
	 */
	public ProjectStaticData analyze(){		

		ProjectStaticData data = new ProjectStaticData();
		if(Properties.CTG_SCHEDULE.equals(AvailableSchedule.HISTORY)){
			data.initializeLocalHistory();
		}
		
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

			ClassInfo ci = new ClassInfo(theClass, numberOfBranches, hasCode);
			data.addNewClass(ci);

			if (Properties.CTG_SCHEDULE == AvailableSchedule.HISTORY) {
				ci.setChanged(data.hasChanged(theClass.getCanonicalName() + ".java"));
				ci.isToTest(data.isToTest(theClass.getCanonicalName(), HistorySchedule.COMMIT_IMPROVEMENT));
			}
		}

		return data;
	}
}

