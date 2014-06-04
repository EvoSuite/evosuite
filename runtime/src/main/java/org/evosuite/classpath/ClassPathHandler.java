package org.evosuite.classpath;

import java.io.File;

import org.evosuite.Properties;

/**
 * When running EvoSuite there are at least three different classpaths
 * to handle: (1) the one of the target project, (2) the one of EvoSuite
 * itself, and (3) the one of the starter of EvoSuite.
 * Note that (2) and (3) can indeed be different: eg if EvoSuite is
 * not run from command line, and rather run from Maven/Eclipse that
 * use their own classloaders.
 * 
 * @author arcuri
 *
 */
public class ClassPathHandler {

	private static final ClassPathHandler singleton = new ClassPathHandler();
	
	/**
	 * The classpath of the project to test, including all
	 * its dependencies
	 */
	private String targetClassPath;
	
	/**
	 * When we start the client, we need to know what is the classpath of EvoSuite.
	 * Problem is, if EvoSuite was called from Eclipse/Maven, when the classpath will
	 * not be part of the system classpath. And might not be feasible to query the
	 * current classloader to get such info, as we cannot make any assumption on its implementation.
	 * So, in those cases, we need to manually specify it
	 */
	private String evosuiteClassPath;
	
	
	private ClassPathHandler(){		
	}
	
	public static ClassPathHandler getInstance(){
		return singleton;
	}
	
	public static void resetSingleton(){
		getInstance().targetClassPath = null;
		getInstance().evosuiteClassPath = null;
	}
	
	
	public String getEvoSuiteClassPath(){
		if(evosuiteClassPath==null){
			evosuiteClassPath = System.getProperty("java.class.path");
		}
		
		return evosuiteClassPath;
	}
	
	/**
	 * Replace current CP of EvoSuite with the given <code>elements</code> 
	 * @param elements
	 * @throws IllegalArgumentException if values in <code>elements</code> are not valid classpath entries
	 */
	public void setEvoSuiteClassPath(String[] elements) throws IllegalArgumentException{
		String cp = getClassPath(elements);
		evosuiteClassPath = cp;
	}
	
	
	/**
	 * Replace current CP for target project with the given <code>elements</code> 
	 * @param elements
	 * @throws IllegalArgumentException if values in <code>elements</code> are not valid classpath entries
	 */
	public void changeTargetClassPath(String[] elements) throws IllegalArgumentException{
		String cp = getClassPath(elements);
		Properties.CP = cp;
		targetClassPath = cp;
	}

	private String getClassPath(String[] elements) {
		if(elements==null || elements.length==0){
			throw new IllegalArgumentException("No classpath elements");
		}

		String cp = "";
		boolean first = true;
		for (String entry : elements) {
			checkIfValidClasspathEntry(entry);
			if (first) {
				first = false;
			} else {
				cp += File.pathSeparator;
			}
			cp += entry;
		}
		return cp;
	}
	
	/**
	 * Return the classpath of the target project.
	 * This should include also all the third-party jars
	 * it depends on
	 * 
	 *  <p>
	 *  If no classpath has been set so far, the one from the property file
	 *  will be used, if it exists.
	 * 
	 * @return
	 */
	public String getTargetProjectClasspath(){
		if(targetClassPath == null){
			targetClassPath = Properties.CP;
		}
		return targetClassPath;
	}
	
	/**
	 * Add classpath entry to the classpath of the target project
	 * 
	 * @param element
	 * @throws IllegalArgumentException
	 */
	public void addElementToTargetProjectClassPath(String element) throws IllegalArgumentException{
		checkIfValidClasspathEntry(element);
		
		getTargetProjectClasspath(); //need to be sure it is initialized
		if(targetClassPath==null || targetClassPath.isEmpty()){
			targetClassPath = element;
		} else {
			
			if(targetClassPath.contains(element)){
				return; //already there, nothing to add
			}
			
			targetClassPath += File.pathSeparator + element;
			Properties.CP = targetClassPath;
		}
	}

	private void checkIfValidClasspathEntry(String element) throws IllegalArgumentException{
		if(element==null || element.isEmpty()){
			throw new IllegalArgumentException("Empty input element");
		}
		
		File file = new File(element);
		if(!file.exists()){
			throw new IllegalArgumentException("Classpath element does not exist on disk at: "+element);
		}
		if(!element.endsWith(".jar") && !file.isDirectory()){
			throw new IllegalArgumentException("A classpath element should either be a jar or a folder");
		}
	}
	
	/**
	 * Get the project classpath as an array of elements
	 * 
	 * @return a non-null array
	 */
	public String[] getClassPathElementsForTargetProject(){
		String cp = getTargetProjectClasspath();
		if(cp==null){
			return new String[0];
		}
		return cp.split(File.pathSeparator);
	}
	
	/**
	 * This is meant only for running the EvoSuite test cases, whose CUTs will be in the
	 * classpath of EvoSuite itself
	 */
	public void changeTargetCPtoTheSameAsEvoSuite(){
		
		File outDir = new File("target"+File.separator+"classes");
		if(outDir.exists()){
			changeTargetClassPath(new String[]{outDir.getAbsolutePath()});
			
			File testDir = new File("target"+File.separator+"test-classes");
			if(testDir.exists()){
				addElementToTargetProjectClassPath(testDir.getAbsolutePath());
			}
		} else {
			//TODO: just in case... not sure it would work properly
			changeTargetClassPath(getEvoSuiteClassPath().split(File.pathSeparator));
		}
	}
}