package de.unisb.cs.st.evosuite.testcase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.asm.Type;

import de.unisb.cs.st.evosuite.Properties;

public class InspectorManager {

	private static InspectorManager instance = null;
	
	private static Logger logger = Logger.getLogger(InspectorManager.class);
	
	Map<Class<?>, List<Inspector> > inspectors = new HashMap<Class<?>, List<Inspector> >();
	
	private InspectorManager() {
		readInspectors();
	}
	
	private void addInspector(Class<?> clazz, Method m) {
		if(!inspectors.containsKey(clazz))
			inspectors.put(clazz, new ArrayList<Inspector>());
		List<Inspector> i = inspectors.get(clazz);
		i.add(new Inspector(m));
	}
	

	private void readInspectors() {
		FilenameFilter inspector_filter = new FilenameFilter() {
		  public boolean accept( File f, String s )
		  {
			  return s.toLowerCase().endsWith( ".inspectors" );
		  }
		};
		
		int num = 0;
		int num_old = 0;
		File basedir = new File(Properties.getProperty("OUTPUT_DIR"));
		for(File f : basedir.listFiles(inspector_filter)) {
			String name = f.getName().replaceAll("_\\d+.inspectors$", "").replace("_", "$");
			try {
				Class<?> clazz = Class.forName(name);
				Scanner scanner = new Scanner(f);
		    	Set<String> inspector_names = new HashSet<String>();
			    try {
			      //first use a Scanner to get each line
			    	while ( scanner.hasNextLine() ){
			    		inspector_names.add( scanner.nextLine().trim() );
			    	}
			    }
			    finally {
			      //ensure the underlying stream is always closed
			      scanner.close();
			    }
			    
			    for(Method m : clazz.getMethods()) {
			    	if(inspector_names.contains(m.getName()+Type.getMethodDescriptor(m))) {
			    		addInspector(clazz, m);
			    		num++;
			    	}
			    }
				logger.debug("Found inspector: "+name+" -> "+(num-num_old));
				num_old = num;
			} catch(FileNotFoundException e) {
				logger.error("Could not find file "+name);
			} catch(ClassNotFoundException e) {
				logger.error("Could not find inspector class "+name);
			}
	    }
		logger.info("Loaded "+num+" inspectors");
	}
	
	public static InspectorManager getInstance() {
		if(instance == null) {
			instance = new InspectorManager();
		}
		return instance;
	}
	
	public List<Inspector> getInspectors(Class<?> clazz) {
		if(inspectors.containsKey(clazz))
			return inspectors.get(clazz);
		else
			return new ArrayList<Inspector>();
	}
}
