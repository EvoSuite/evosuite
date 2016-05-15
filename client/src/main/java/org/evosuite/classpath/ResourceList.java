/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.classpath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.runtime.InitializingListener;
import org.evosuite.runtime.InitializingListenerUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * Utilities to list class resources (ie .class files) available from the classpath 
 * </p>
 * 
 * 
 * @author Gordon Fraser
 */
public class ResourceList {

	private static Logger logger = LoggerFactory.getLogger(ResourceList.class);

	private static class Cache{
		/**
		 * Key -> a classpath entry (eg folder or jar file)
		 * <p>
		 * Value -> set of all classes in that CP entry
		 */
		public  Map<String,Set<String>> mapCPtoClasses = new LinkedHashMap<>(); 

		/**
		 * Key -> full qualifying name of a class, eg org.some.Foo
		 * <p>
		 * Value -> the classpath entry in which it can be found
		 */
		public  Map<String,String> mapClassToCP = new LinkedHashMap<>();

		/**
		 * Key -> package prefix
		 * <p>
		 * Value -> set of classpath entries having such prefix
		 */
		public Map<String,Set<String>> mapPrefixToCPs = new LinkedHashMap<>();

		/**
		 * Keep track of the classes that should be on the classpath but they are not
		 */
		public Set<String> missingClasses = new LinkedHashSet<>();
		
		
		public void addPrefix(String prefix, String cpEntry){
			Set<String> classPathEntries = mapPrefixToCPs.get(prefix);
			if(classPathEntries==null){
				classPathEntries = new LinkedHashSet<String>();
				mapPrefixToCPs.put(prefix, classPathEntries);
			}
			classPathEntries.add(cpEntry);

			if(!prefix.isEmpty()){
				String parent = getParentPackageName(prefix);
				addPrefix(parent,cpEntry);
			}
		}

		/**
		 * Keep track of all jars we opened.
		 * Key -> the path of the jar file
		 */
		public Map<String,JarFile> openedJars = new LinkedHashMap<>();

		public JarFile getJar(String entry){
			if(openedJars.containsKey(entry)){
				return openedJars.get(entry);
			}
			try {
				JarFile jar = new JarFile(entry);
				openedJars.put(entry,jar);
				return jar;
			} catch (IOException e) {
				logger.error("Error while reading jar file "+entry+": "+e.getMessage(),e);
				return null;
			}
		}

		public void close(){
			for(JarFile jar : openedJars.values()){
				try {
					jar.close();
				} catch (IOException e) {
					logger.error("Cannot close jar file " + jar.getName() + ". " + e.toString());
				}
			}
		}
	}
	

	/**
	 * Current cache. Do not access directly, but rather use getCache(), as it can be null
	 */
	private Cache cache = null;
	
	
	/*
	 * ResourceList for each ClassLoader
	 */
	private static Map<ClassLoader, ResourceList> instanceMap = new HashMap<ClassLoader, ResourceList>();

	private final ClassLoader classLoader;

	/** Private constructor */
	private ResourceList(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public static ResourceList getInstance(ClassLoader classLoader) {
		if (!instanceMap.containsKey(classLoader)) {
			instanceMap.put(classLoader, new ResourceList(classLoader));
		}
		return instanceMap.get(classLoader);
	}


	// -------------------------------------------
	// --------- public methods  ----------------- 
	// -------------------------------------------

	public void resetCache(){
		if(cache!=null){
			cache.close();
		}
		cache = null;
	}
	
	public static void resetAllCaches() {
		instanceMap.clear();
	}


	/**
	 * is the target class among the ones in the SUT classpath?
	 * 
	 * @param className
	 *            a fully qualified class name
	 * @return
	 */
	public boolean hasClass(String className) {
		return getCache().mapClassToCP.containsKey(className);
	}

	/**
	 * 
	 * @param name  a fully qualifying name, e.g. org.some.Foo
	 * @return
	 */
	public InputStream getClassAsStream(String name) {

		String path = name.replace('.', '/') + ".class";
		String windowsPath = name.replace(".", "\\") + ".class";

		String cpEntry = getCache().mapClassToCP.get(name);
		if(cpEntry==null){
			
			/*
				the cache is initialized based on what is on the project classpath.
				but that does not include the Java API, although it is accessed by
				the SUT.
			 */

			InputStream ins = getClassAsStreamFromClassLoader(name);
			if(ins != null){
				return ins;
			}

			if(!getCache().missingClasses.contains(name)){
				getCache().missingClasses.add(name);
				/*
				 * Note: can't really have "warn" here, as the SUT can use the classloader,
				 * and try to load garbage (eg random string generated as test data) that
				 * would fill the logs 
				 */
				logger.debug("The class "+name+" is not on the classpath"); //only log once			
			}
			return null;
		}

		if(cpEntry.endsWith(".jar")){
			JarFile jar = getCache().getJar(cpEntry);
			if(jar==null){
				return null;
			}
			JarEntry entry = jar.getJarEntry(path);
			if(entry == null){
				logger.error("Error: could not find "+path+" inside of jar file "+cpEntry);
				return null;
			}
			InputStream is = null;
			try {
				is = jar.getInputStream(entry);
			} catch (IOException e) {
				logger.error("Error while reading jar file "+cpEntry+": "+e.getMessage(),e);
				return null;
			}
			return is;
		} else {
			//if not a jar, it is a folder
			File classFile = null;
			if (File.separatorChar != '/') {	
				classFile = new File(cpEntry+File.separator+windowsPath);
			} else {
				classFile = new File(cpEntry+File.separator+path);
			}
			if(!classFile.exists()){
				logger.error("Could not find "+classFile);
			}

			try {
				return new FileInputStream(classFile);
			} catch (FileNotFoundException e) {
				logger.error("Error while trying to open stream on: "+classFile.getAbsolutePath());
				return null;
			}
		}

	}


	/**
	 * Given the target classpath entry (eg folder or jar file), return the names (eg foo.Foo) of all the classes (.class files)
	 * inside
	 * 
	 * @param classPathEntry
	 * @param includeInternalClasses   should internal classes (ie static and anonymous having $ in their name) be included?
	 * @return
	 */
	public Set<String> getAllClasses(String classPathEntry, boolean includeInternalClasses){
		return getAllClasses(classPathEntry,"",includeInternalClasses);
	}


    /**
     * Given the target classpath entry (eg folder or jar file), return the names (eg foo.Foo) of all the classes (.class files)
     * inside
     *
     * @param classPathEntry
     * @param prefix
     * @param includeInternalClasses should internal classes (ie static and anonymous having $ in their name) be included?
     * @return
     */
    public Set<String> getAllClasses(String classPathEntry, String prefix, boolean includeInternalClasses){
        return getAllClasses(classPathEntry,prefix,includeInternalClasses,true);
    }


	/**
	 * Given the target classpath entry (eg folder or jar file), return the names (eg foo.Foo) of all the classes (.class files)
	 * inside
	 * 
	 * @param classPathEntry
	 * @param prefix
	 * @param includeInternalClasses should internal classes (ie static and anonymous having $ in their name) be included?
     * @param excludeAnonymous  if including internal classes, should though still exclude the anonymous? (ie keep only the static ones)
	 * @return
	 */
	public Set<String> getAllClasses(String classPathEntry, String prefix, boolean includeInternalClasses, boolean excludeAnonymous){

		if(classPathEntry.contains(File.pathSeparator)){
			Set<String> retval = new LinkedHashSet<String>();
			for(String element : classPathEntry.split(File.pathSeparator)){
				retval.addAll(getAllClasses(element,prefix,includeInternalClasses,excludeAnonymous));
			}
			return retval;
		} else {

			classPathEntry = (new File(classPathEntry)).getAbsolutePath();

			addEntry(classPathEntry);

			//no need to scan the classpath entry cache if it does not have the given prefix
			Set<String> cps = getCache().mapPrefixToCPs.get(prefix);
			if(cps==null || !cps.contains(classPathEntry)){
				return Collections.emptySet();
			}

			Set<String> classes = new LinkedHashSet<>();

			for(String className : getCache().mapCPtoClasses.get(classPathEntry)){
				if(!className.startsWith(prefix)){
					continue;
				}
                if(!includeInternalClasses && className.contains("$")){
                    continue;
                }
				if(includeInternalClasses && excludeAnonymous && className.matches(".*\\$\\d+$")) {
					continue;
				}

				classes.add(className);
			}

			return classes;
		}
	}

	public static boolean isInterface(String resource) throws IOException {
		InputStream input = ResourceList.class.getClassLoader().getResourceAsStream(resource);
		return isClassAnInterface(input);
	}

	public boolean isClassAnInterface(String className) throws IOException {
		InputStream input = getClassAsStream(className);		
		return isClassAnInterface(input);
	}

	public boolean isClassDeprecated(String className) throws IOException {
		InputStream input = getClassAsStream(className);		
		return isClassDeprecated(input);
	}
	
	public boolean isClassTestable(String className) throws IOException {
		InputStream input = getClassAsStream(className);		
		return isClassTestable(input);
	}

	/**
	 * <p>
	 * Given a resource path, eg foo/Foo.class, return the class name, eg foo.Foo
	 * 
	 * <p>
	 * This method is able to handle different operating systems (Unix/Windows) and whether
	 * the resource is in a folder or inside a jar file ('/' separator independent of operating system).  
	 *
	 */
	public static String getClassNameFromResourcePath(String resource){
		//method had to be moved due to constraints on "runtime" module dependencies
		return InitializingListenerUtils.getClassNameFromResourcePath(resource);
	}



	// -------------------------------------------
	// --------- private/protected methods  ------ 
	// -------------------------------------------

	private static InputStream getClassAsStreamFromClassLoader(String name) {

		String path = name.replace('.', '/') + ".class";
		String windowsPath = name.replace(".", "\\") + ".class";

		//first try with system classloader
		InputStream is = ClassLoader.getSystemResourceAsStream(path);
		if (is != null) {
			return is;
		}
		if (File.separatorChar != '/') {
			is = ClassLoader.getSystemResourceAsStream(windowsPath);
			if (is != null) {
				return is;
			}
		}

		return null;
	}

	private static boolean isClassAnInterface(InputStream input) throws IOException{
		try{
			ClassReader reader = new ClassReader(input);
			ClassNode cn = new ClassNode();
			reader.accept(cn, ClassReader.SKIP_FRAMES);		
			return (cn.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE;
		} finally{
			input.close(); //VERY IMPORTANT, as ASM does not close the stream
		}
	}

	/**
	 * Returns {@code true} if the class is deprecated; returns {@code false} otherwise.
	 * 
	 * @param input the input stream
	 * @return {@code true} if the class is deprecated, {@code false} otherwise
	 * @throws IOException if an error occurs while reading the input stream
	 */

	private static boolean isClassDeprecated(InputStream input) throws IOException{
		try{
			ClassReader reader = new ClassReader(input);
			ClassNode cn = new ClassNode();
			reader.accept(cn, ClassReader.SKIP_FRAMES);		
			return (cn.access & Opcodes.ACC_DEPRECATED) == Opcodes.ACC_DEPRECATED;
		} finally{
			input.close(); //VERY IMPORTANT, as ASM does not close the stream
		}
	}
	
	/**
	 * Returns {@code true} if there is at least one public method in the class; returns {@code false} otherwise.
	 * 
	 * @param input the input stream
	 * @return {@code true} if there is at least one public method in the class, {@code false} otherwise
	 * @throws IOException if an error occurs while reading the input stream
	 */
	private static boolean isClassTestable(InputStream input) throws IOException{
		try{
			ClassReader reader = new ClassReader(input);
			ClassNode cn = new ClassNode();
			reader.accept(cn, ClassReader.SKIP_FRAMES);
			@SuppressWarnings("unchecked")
			List<MethodNode> l = cn.methods; 
			for (MethodNode m : l) {
				if ((m.access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC ||
					(m.access & Opcodes.ACC_PROTECTED) == Opcodes.ACC_PROTECTED ||
					(m.access & Opcodes.ACC_PRIVATE) == 0 /* default */ ) {
					return true;
		        }
			}
			return false;
		} finally{
			input.close(); //VERY IMPORTANT, as ASM does not close the stream
		}
	}
	
	/**
	 * Remove last '.' token
	 * 
	 * @param className
	 * @return
	 */
	protected static String getParentPackageName(String className){
		if(className==null || className.isEmpty()){
			return className;
		}

		int index = className.lastIndexOf('.');
		if(index<0){
			return "";
		}

		return className.substring(0,index);
	}

	/**
	 * Init the cache if null
	 * @return
	 */
	private Cache getCache(){
		if(cache == null){
			initCache();
		}

		return cache;
	}

	
	private void initCache() {
		cache = new Cache();

		String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();

		// If running in regression mode and current ClassLoader is the regression ClassLoader
		if(Properties.isRegression() && 
				classLoader==TestGenerationContext.getInstance().getRegressionClassLoaderForSUT())
			 cp = org.evosuite.Properties.REGRESSIONCP;

		for(String entry : cp.split(File.pathSeparator)){
			addEntry(entry);
		}		
	}

	private void addEntry(String classPathElement) throws IllegalArgumentException{
		final File file = new File(classPathElement);

		classPathElement = file.getAbsolutePath();

		if(getCache().mapCPtoClasses.containsKey(classPathElement)){
			return; //this classpath entry has already been analyzed
		}

		getCache().mapCPtoClasses.put(classPathElement, new LinkedHashSet<String>());


		if (!file.exists()) {
			throw new IllegalArgumentException("The class path resource "
					+ file.getAbsolutePath() + " does not exist");
		}

		if (file.isDirectory()) {
			scanDirectory(file,classPathElement);
		} else if (file.getName().endsWith(".jar")) {
			scanJar(classPathElement);
		} else {
			throw new IllegalArgumentException("The class path resource "
					+ file.getAbsolutePath() + " is not valid");
		}		
	}

	private void scanDirectory(final File directory,
			final String classPathFolder) {

		if (!directory.exists()) {
			return;
		}
		if (!directory.isDirectory()) {
			return;
		}
		if (!directory.canRead()) {
			logger.warn("No permission to read: "+directory.getAbsolutePath());
			return;
		}

		String prefix =  directory.getAbsolutePath().replace(classPathFolder + File.separator,"");
		prefix = prefix.replace(File.separatorChar, '.');

		File[] fileList = directory.listFiles();
		for (final File file : fileList) {
			if (file.isDirectory()) {
				/*
				 * recursion till we get to a file that is not a folder.
				 */
				scanDirectory(file, classPathFolder);
			} else {
				if(! file.getName().endsWith(".class")){
					continue; // we are only interested in class files
				}
				String relativeFilePath = file.getAbsolutePath().replace(classPathFolder + File.separator,"");
				String className = getClassNameFromResourcePath(relativeFilePath);

				// The same class may exist in different classpath entries
				// and only the first one is kept
				if(getCache().mapClassToCP.containsKey(className))
					continue;
				
				// If there is an outer class, then we also have a classpath
				// problem and should ignore this
				if(className.contains("$")) {
					String outerClass = className.substring(0, className.indexOf('$'));
					if(getCache().mapClassToCP.containsKey(outerClass)) {
						if(!getCache().mapClassToCP.get(outerClass).equals(classPathFolder)) {
							continue;
						}
					}
				}


				getCache().mapClassToCP.put(className, classPathFolder);
				getCache().mapCPtoClasses.get(classPathFolder).add(className);
				getCache().addPrefix(prefix, classPathFolder);
			}
		}
	}

	private void scanJar(String jarEntry) {
		JarFile zf = getCache().getJar(jarEntry);

		Enumeration<?> e = zf.entries();
		while (e.hasMoreElements()) {
			JarEntry ze = (JarEntry) e.nextElement();
			String entryName = ze.getName();

			if(! entryName.endsWith(".class")){
				continue;
			}

			String className = getClassNameFromResourcePath(entryName);
			
			// The same class may exist in different classpath entries
			// and only the first one is kept
			if(getCache().mapClassToCP.containsKey(className))
				continue;
			
			if(className.contains("$")) {
				String outerClass = className.substring(0, className.indexOf('$'));
				if(getCache().mapClassToCP.containsKey(outerClass)) {
					if(!getCache().mapClassToCP.get(outerClass).equals(jarEntry)) {
						continue;
					}
				}
			}
			
			getCache().mapClassToCP.put(className, jarEntry);//getPackageName
			getCache().mapCPtoClasses.get(jarEntry).add(className);
			getCache().addPrefix(getParentPackageName(className), jarEntry);
		}
	}

}