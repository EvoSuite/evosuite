package org.evosuite.agent;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.VirtualMachine;

/**
 * This class is responsible to load the jar with the agent
 * definition (in its manifest) and then hook it to the current
 * running JVM 
 * 
 * @author arcuri
 *
 */
public class AgentLoader {

	private static final Logger logger = LoggerFactory.getLogger(AgentLoader.class);

	private static volatile boolean alreadyLoaded = false; 
	
	public synchronized static void loadAgent() throws RuntimeException{
		
		if(alreadyLoaded){
			return;
		}
		
		logger.info("dynamically loading javaagent");
		String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
		int p = nameOfRunningVM.indexOf('@');
		String pid = nameOfRunningVM.substring(0, p);

		String jarFilePath = getJarPath();
		if(jarFilePath==null){
			throw new RuntimeException("Cannot find either the compilation target folder nor the EvoSuite jar in classpath: "+System.getProperty("java.class.path"));
		} else {
			logger.info("Using JavaAgent in "+jarFilePath);
		}

		/*
		 * We need to use reflection on a new instantiated ClassLoader because
		 * we can make no assumption whatsoever on the class loader of AgentLoader 
		 */
		ClassLoader toolLoader = ToolsJarLocator.getLoaderForToolsJar();		
		
		
		logger.info("System classloader class: "+ClassLoader.getSystemClassLoader().getClass()); //TODO remove
		logger.info("Classpath: "+System.getProperty("java.class.path"));
		
		try {
			//ClassPathHacker.addFile(jarFilePath); //FIXME should check if already there and why it failed to get the default one
			
			Class<?> string = toolLoader.loadClass("java.lang.String");
			
			Class<?> clazz = toolLoader.loadClass("com.sun.tools.attach.VirtualMachine");
			Method attach = clazz.getMethod("attach", string);
			
			logger.info("Going to attach agent to process "+pid);
			 
			VirtualMachine vm = VirtualMachine.attach(pid);
			//Object instance = attach.invoke(null, pid);
			
			 vm.loadAgent(jarFilePath, "");
			//Method loadAgent = clazz.getMethod("loadAgent", string, string);
			//loadAgent.invoke(instance, jarFilePath, "");
			
			 vm.detach(); 
			//Method detach = clazz.getMethod("detach");
			//detach.invoke(instance);

		} catch (Exception e) {
			Throwable cause = e.getCause();
			String causeDescription = cause==null ? "" : " , cause "+cause.getClass()+" "+cause.getMessage();
			logger.error("Exception "+e.getClass()+": "+e.getMessage()+causeDescription,e);
			throw new RuntimeException(e);
		}
		
		alreadyLoaded = true;
	}

	private static boolean isEvoSuiteMainJar(String path){
		/*
		if(! jar.endsWith("minimal.jar")){
			return false;
		}
		if(jar.contains("/evosuite-0")){ //FIXME we need a better check 
			return true;
		}
		*/
		
		File file = new File(path);
		String jar = file.getName();
		
		if(jar.startsWith("evosuite-0") && jar.endsWith(".jar")){
			return true; //FIXME better handling
		}
		
		return false;
	}
	
	
	private static String getJarPath(){
		String jarFilePath = null;	
		String classPath = System.getProperty("java.class.path");
		String[] tokens = classPath.split(File.pathSeparator); 

		for(String entry : tokens){
			if(isEvoSuiteMainJar(entry)){
				jarFilePath = entry;
				break;
			}
		}

		if(jarFilePath==null){
			jarFilePath = searchInCurrentClassLoaderIfUrlOne();    
		}
		
		if(jarFilePath==null){
			/*
			 * this could happen in Eclipse or during test execution in Maven, and so search in compilation 'target' folder 
			 */    			
			jarFilePath = searchInFolder("target");    			
		}

		if(jarFilePath==null){
			/*
			 * this could happen in Eclipse or during test execution in Maven, and so search in compilation 'target' folder 
			 */    			
			jarFilePath = searchInFolder("lib");    			
		}

		if(jarFilePath==null){
			/*
			 * nothing seems to work, so try .m2 folder
			 */    			
			jarFilePath = searchInM2();    			
		}

		if(jarFilePath==null){
			//this could happen if the name of the jar has been changed, so just pick one that contains evosuite in its name 
			for(String entry : tokens){
				if(isEvoSuiteMainJar(entry)){  
					jarFilePath = entry;
					break;
				}
			}
		}
		
		return jarFilePath; 
	}

	private static String searchInCurrentClassLoaderIfUrlOne() {
		
		Set<URL> urls = new HashSet<URL>();
		
		ClassLoader loader = AgentLoader.class.getClassLoader();
		while(loader != null){
			if(loader instanceof URLClassLoader){
				URLClassLoader urlLoader = (URLClassLoader) loader;
				for(URL url : urlLoader.getURLs()){
					urls.add(url);
					try {
						File file = new File(url.toURI());
						if(isEvoSuiteMainJar(file.getName())){
							return file.getAbsolutePath();
						}
					} catch (Exception e) {
						logger.error("Error while parsing URL "+url);
						continue;
					}
				}
			}
			
			loader = loader.getParent();
		}
		
		String msg = "Failed to find EvoSuite jar in current classloader. URLs of classloader:";
		for(URL url : urls){
			msg += "\n"+url.toString();
		}
		logger.warn(msg);
		
		return null;
	}

	private static String searchInM2() {
	
		File home = new File(System.getProperty("user.home"));
		File m2 = new File(home.getAbsolutePath()+"/.m2");
		if(!m2.exists()){
			logger.debug("Cannot find the .m2 folder in home directory in "+m2);
			return null;
		}
		
		//FIXME we would need a more robust approach, as this is just an hack for now
		String relativePath = "/repository/org/evosuite/evosuite/0.1-SNAPSHOT/evosuite-0.1-SNAPSHOT-jar-minimal.jar";
		File jar = new File(m2.getAbsolutePath()+relativePath);
		
		if(!jar.exists()){
			logger.debug("No jar file at: "+jar);
			return null;
		} else {
			return jar.getAbsolutePath();
		}
	}

	private static String searchInFolder(String folder) {
		File target = new File(folder);
		if(!target.exists()){
			logger.debug("No target folder "+target.getAbsolutePath());
			return null;
		}

		if(!target.isDirectory()){
			logger.debug("'target' exists, but it is not a folder");
			return null;
		}

		for(File file : target.listFiles()){
			String name = file.getName();
			if(isEvoSuiteMainJar(name)){
				return file.getAbsolutePath();
			}
		}

		return null;
	}
}