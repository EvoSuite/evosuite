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


package de.unisb.cs.st.evosuite.cfg;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import de.unisb.cs.st.ds.util.io.Io;
import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.javalanche.coverage.CoverageTransformer;
import de.unisb.cs.st.javalanche.mutation.javaagent.classFileTransfomer.mutationDecision.Excludes;

public class CFGTransformer implements ClassFileTransformer {

	private static Logger logger = Logger.getLogger(CoverageTransformer.class);

	private static final Excludes e = Excludes.getInstance(); 
	
	private Map<String, List<String>> test_classes;

	public CFGTransformer() {
		super();
		test_classes = getTestObjectsFromFile();
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				shutdown();
			}
		});
	}

	
	/**
	 * Load test methods from test task file
	 * @return
	 *   Map from classname to list of methodnames
	 */
	private static Map<String, List<String> > getTestObjectsFromFile() {
		//String property = System.getProperty("test.classes");
		String property= Properties.OUTPUT_DIR+"/"+Properties.TARGET_CLASS+".task";
		logger.info("Reading test methods to be instrumented from "+property);
		File file = new File(property);
		List<String> lines = Io.getLinesFromFile(file);
		Map<String, List<String> > objs = new HashMap<String, List<String> >();
		for(String line : lines) {
			line = line.trim();
			// Skip comments
			if(line.startsWith("#"))
				continue;
			
			String[] parameters = line.split(",");
			if(parameters.length != 2)
				continue;
			if(!objs.containsKey(parameters[0]))
				objs.put(parameters[0], new ArrayList<String>());
			
			objs.get(parameters[0]).add(parameters[1]);
		}
		return objs;
	}
	
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
	try{
		if (className == null) {
				return classfileBuffer;
			}
		if (loader != ClassLoader.getSystemClassLoader()) {
			return classfileBuffer;
		}

		// whitelist - only trace packages of that domain

		if (!className.startsWith(Properties.getProperty("PROJECT_PREFIX").replace('.', '/'))) {
			//System.err.println("Not on whitelist: " + className);
			return classfileBuffer;
		}

		// blacklist: can't trace yourself and don't instrument tests (better performance)

		if (e.shouldExclude(className.replace('/', '.'))) {
			//System.err.println("Blacklisted: " + className);
			return classfileBuffer;
		}
		//System.out.println("Going to read: " + className);

		if(!test_classes.containsKey(className)) {
			return classfileBuffer;
		}
		
		byte[] result = classfileBuffer;
		ClassReader reader = new ClassReader(classfileBuffer);
		ClassWriter writer = new ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS);
//		ClassWriter writer = new ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_FRAMES);
		ClassVisitor cv = writer;
		
		// Only use this if class has mutations 
//		cv = new CFGClassAdapter(cv, className, test_classes.get(className));
		cv = new CFGClassAdapter(cv, className); // TODO: Why is this here and in HOM transformer??
		reader.accept(cv, ClassReader.SKIP_FRAMES);
		result = writer.toByteArray();

		return result;
		}catch(Throwable t){
			t.printStackTrace();
			String message="Exception thrown during instrumentation";
			logger.error(message , t);
			System.err.println(message);
			System.exit(1);
		}
		throw new RuntimeException("Should not be reached");
	}

	private void shutdown() {
	}

}
