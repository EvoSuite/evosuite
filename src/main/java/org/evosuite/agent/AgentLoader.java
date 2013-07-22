package org.evosuite.agent;

import java.io.File;
import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.VirtualMachine;

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

		try {
			VirtualMachine vm = VirtualMachine.attach(pid);
			vm.loadAgent(jarFilePath, "");
			vm.detach();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		alreadyLoaded = true;
	}

	private static String getJarPath(){
		String jarFilePath = null;	
		String classPath = System.getProperty("java.class.path");
		String[] tokens = classPath.split(File.pathSeparator); 

		for(String entry : tokens){
			if(! entry.endsWith("minimal.jar")){
				continue;
			}
			if(entry.contains("/evosuite-0")){ //FIXME we need a better check 
				jarFilePath = entry;
				break;
			}
		}

		if(jarFilePath==null){
			/*
			 * this could happen in Eclipse or during test execution in Maven, and so search in compilation 'target' folder 
			 */    			
			jarFilePath = searchInTarget();    			
		}

		if(jarFilePath==null){
			/*
			 * nothing seems to work, so try .m2 folder
			 */    			
			jarFilePath = searchInM2();    			
		}

		return jarFilePath; 
	}

	private static String searchInM2() {
	
		File home = new File(System.getProperty("user.home"));
		File m2 = new File(home.getAbsolutePath()+"/.m2");
		if(!m2.exists()){
			logger.warn("Cannot find the .m2 folder in home directory in "+m2);
			return null;
		}
		
		//FIXME we would need a more robust approach, as this is just an hack for now
		String relativePath = "/repository/org/evosuite/evosuite/0.1-SNAPSHOT/evosuite-0.1-SNAPSHOT-jar-minimal.jar";
		File jar = new File(m2.getAbsolutePath()+relativePath);
		
		if(!jar.exists()){
			logger.warn("No jar file at: "+jar);
			return null;
		} else {
			return jar.getAbsolutePath();
		}
	}

	private static String searchInTarget() {
		File target = new File("target");
		if(!target.exists()){
			logger.warn("No target folder "+target.getAbsolutePath());
			return null;
		}

		if(!target.isDirectory()){
			logger.error("'target' exists, but it is not a folder");
			return null;
		}

		for(File file : target.listFiles()){
			String name = file.getName();
			if(name.startsWith("evosuite") && name.endsWith("minimal.jar")){
				return file.getAbsolutePath();
			}
		}

		return null;
	}
}