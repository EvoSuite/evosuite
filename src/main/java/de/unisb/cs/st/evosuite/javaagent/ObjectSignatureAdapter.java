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

package de.unisb.cs.st.evosuite.javaagent;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import de.unisb.cs.st.ds.util.io.Io;
import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.javalanche.mutation.javaagent.classFileTransfomer.mutationDecision.Excludes;

/**
 * Testing might require adapting function signatures, for example Object classes.
 * 
 * @author Gordon Fraser
 *
 */
public class ObjectSignatureAdapter extends ClassAdapter {

	protected static Logger logger = Logger.getLogger(ObjectSignatureAdapter.class);
	
	private Excludes e = Excludes.getTestExcludesInstance();
	
	private String className;

	private boolean exclude = false;
	
	private Map<String, String> descriptors = new HashMap<String, String>();

	/**
	 * 
	 */
	public ObjectSignatureAdapter(ClassVisitor visitor, String className) {
		super(visitor);
		
		this.className = className.replace('/', '.');
		
		if (e.shouldExclude(this.className) || !(this.className.startsWith(Properties.PROJECT_PREFIX))) {
			exclude = true;
		} else {
			exclude = false;
			
			// Initialize descriptors
			// mutation-report/className.obj contains method signatures
			File file = new File(Properties.OUTPUT_DIR+"/"+className+".obj");
			List<String> lines = Io.getLinesFromFile(file);
			descriptors = new HashMap<String, String>();
			for(String line : lines) {
				line = line.trim();
				// Skip comments
				if(line.startsWith("#"))
					continue;
				
				String[] parameters = line.split(",");
				if(parameters.length == 2) {
					descriptors.put(parameters[0], parameters[1]);
					logger.info("Adding descriptor: "+parameters[1]);
				}
				else if(parameters.length == 3) {
					// TODO: Handle signature
					descriptors.put(parameters[0], parameters[1]);
				}
			}
		}
	}

	public MethodVisitor visitMethod(int methodAccess, String name,
			String descriptor, String signature, String[] exceptions) {

		
		if (!exclude) {
			// If we have a signature for this method:
			if(descriptors.containsKey(name+descriptor)) {
				// descriptor = Type.getMethodDescriptor(arg0, arg1);
				// signature = Type.getMethodSignature(arg0, arg1);
				logger.info("Changing descriptor from "+descriptor+" to "+descriptors.get(name+descriptor));
				descriptor = descriptors.get(name+descriptor);
			} else {
				logger.info("Couldn't find descriptor from "+name+descriptor);
			}
		}
		MethodVisitor mv = super.visitMethod(methodAccess, name, descriptor, signature, exceptions);
		mv = new ObjectCallAdapter(mv, descriptors);
		
		return mv;
	}
}
