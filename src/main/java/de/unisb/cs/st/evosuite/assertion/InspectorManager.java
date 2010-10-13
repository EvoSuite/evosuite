/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */


package de.unisb.cs.st.evosuite.assertion;

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
		i.add(new Inspector(clazz, m));
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
		File basedir = new File(Properties.OUTPUT_DIR);
		for(File f : basedir.listFiles(inspector_filter)) {
//			String name = f.getName().replaceAll("_\\d+.inspectors$", "").replace("_", "$");
			String name = f.getName().replaceAll(".inspectors", "").replace("_", "$");
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
				logger.debug("Found inspector: "+name+" -> "+(num-num_old)+" for class "+clazz.getName()+" in file "+name);
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
