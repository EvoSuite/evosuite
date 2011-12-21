/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.utils.Utils;

/**
 * @author Gordon Fraser
 * 
 */
public class MethodDescriptorReplacement implements Serializable {

	private static final long serialVersionUID = -2324044891555746456L;

	/** Singleton instance */
	private static MethodDescriptorReplacement instance = null;

	private static Logger logger = LoggerFactory.getLogger(MethodDescriptorReplacement.class);

	/** Map from class to method pairs */
	private final Map<String, String> descriptors = new HashMap<String, String>();

	private final Map<Method, Type> return_types = new HashMap<Method, Type>();

	private final Map<Method, List<Type>> method_parameters = new HashMap<Method, List<Type>>();

	private final Map<Constructor<?>, List<Type>> constructor_parameters = new HashMap<Constructor<?>, List<Type>>();

	/**
	 * Private constructor
	 */
	private MethodDescriptorReplacement() {
		getDescriptorMapping();
	}

	/**
	 * Singleton accessor
	 * 
	 * @return
	 */
	public static MethodDescriptorReplacement getInstance() {
		if (instance == null)
			instance = new MethodDescriptorReplacement();

		return instance;
	}

	/**
	 * Check if we need to change anything here
	 * 
	 * @param className
	 * @param methodName
	 * @param descriptor
	 * @return
	 */
	public boolean hasKey(String className, String methodName, String descriptor) {
		//if(!descriptors.containsKey(className))
		//	return false;

		return descriptors.containsKey(className + "." + methodName + descriptor);
	}

	/**
	 * Get the actual replacement, if there is one
	 * 
	 * @param className
	 * @param methodName
	 * @param descriptor
	 * @return
	 */
	public String get(String className, String methodName, String descriptor) {
		if (hasKey(className, methodName, descriptor))
			return descriptors.get(className + "." + methodName + descriptor);
		else
			return descriptor;
	}

	public List<Type> getParameterTypes(Method method) {
		return getParameterTypes(method.getDeclaringClass(), method);
	}

	public List<Type> getParameterTypes(Type callee, Method method) {
		if (method_parameters.containsKey(method))
			return method_parameters.get(method);

		String className = null;
		if (callee == null) {
			className = method.getDeclaringClass().getName();
		} else {
			GenericClass c = new GenericClass(callee);
			className = c.getRawClass().getName();
		}

		/*
		if (Properties.TRANSFORM_BOOLEAN) {
			List<Type> parameters = new ArrayList<Type>();
			for (org.objectweb.asm.Type asm_param : DescriptorMapping.getInstance().getOriginalTypes(method.getDeclaringClass().getName(),
			                                                                                         method.getName(),
			                                                                                         org.objectweb.asm.Type.getMethodDescriptor(method))) {
				logger.info("Next type: " + asm_param);
				parameters.add(getType(asm_param));
			}
			return parameters;
		}
		*/

		String descriptor = org.objectweb.asm.Type.getMethodDescriptor(method);
		/*
		if (Properties.TRANSFORM_BOOLEAN) {
			descriptor = DescriptorMapping.getInstance().getOriginalDescriptor(className,
			                                                                   method.getName(),
			                                                                   descriptor);
		}
		*/

		if (hasKey(className, method.getName(), descriptor)) {
			String replacement = descriptors.get(className + "." + method.getName()
			        + descriptor);
			logger.debug("Found replacement: " + className + "." + method.getName()
			        + " -> " + replacement);
			List<Type> parameters = new ArrayList<Type>();
			for (org.objectweb.asm.Type asm_param : org.objectweb.asm.Type.getArgumentTypes(replacement)) {
				parameters.add(getType(asm_param));
			}
			method_parameters.put(method, parameters);
			return parameters;

		} else {
			List<Type> parameters = Arrays.asList(method.getGenericParameterTypes());
			/*
			if (Properties.TRANSFORM_BOOLEAN) {
				logger.info("Checking " + className + "." + method.getName() + descriptor);

				int num = 0;
				for (org.objectweb.asm.Type asm_param : DescriptorMapping.getInstance().getOriginalTypes(method.getDeclaringClass().getName(),
				                                                                                         method.getName(),
				                                                                                         org.objectweb.asm.Type.getMethodDescriptor(method))) {
					logger.info("Next type: " + asm_param);
					parameters.set(num, getType(asm_param));
					num++;
				}
			}
			*/
			method_parameters.put(method, parameters);
			return parameters;
		}
	}

	public List<Type> getParameterTypes(Constructor<?> constructor) {
		if (constructor_parameters.containsKey(constructor))
			return constructor_parameters.get(constructor);

		String className = constructor.getDeclaringClass().getName();

		String descriptor = org.objectweb.asm.Type.getConstructorDescriptor(constructor);
		/*
		if (Properties.TRANSFORM_BOOLEAN) {
			descriptor = DescriptorMapping.getInstance().getOriginalDescriptor(className,
			                                                                   "<init>",
			                                                                   descriptor);
		}
		*/
		if (hasKey(className, "<init>", descriptor)) {
			String replacement = descriptors.get(className + ".<init>" + descriptor);
			logger.debug("Found replacement: " + className + ".<init>" + " -> "
			        + replacement);
			List<Type> parameters = new ArrayList<Type>();
			for (org.objectweb.asm.Type asm_param : org.objectweb.asm.Type.getArgumentTypes(replacement)) {
				parameters.add(getType(asm_param));
			}
			constructor_parameters.put(constructor, parameters);
			return parameters;

		} else {
			List<Type> parameters = Arrays.asList(constructor.getGenericParameterTypes());
			/*
			if (Properties.TRANSFORM_BOOLEAN) {
				logger.info("Checking " + className + ".<init>" + descriptor);
				int num = 0;
				for (org.objectweb.asm.Type asm_param : DescriptorMapping.getInstance().getOriginalTypes(constructor.getDeclaringClass().getName(),
				                                                                                         "<init>",
				                                                                                         org.objectweb.asm.Type.getConstructorDescriptor(constructor))) {
					logger.info("Next type: " + asm_param);
					parameters.set(num, getType(asm_param));
					num++;
				}
			}
			*/
			constructor_parameters.put(constructor, parameters);
			return parameters;
		}
	}

	private Type getType(org.objectweb.asm.Type asm_type) {
		switch (asm_type.getSort()) {
		case org.objectweb.asm.Type.BOOLEAN:
			return boolean.class;
		case org.objectweb.asm.Type.BYTE:
			return byte.class;
		case org.objectweb.asm.Type.CHAR:
			return char.class;
		case org.objectweb.asm.Type.DOUBLE:
			return double.class;
		case org.objectweb.asm.Type.FLOAT:
			return float.class;
		case org.objectweb.asm.Type.INT:
			return int.class;
		case org.objectweb.asm.Type.LONG:
			return long.class;
		case org.objectweb.asm.Type.SHORT:
			return short.class;
		case org.objectweb.asm.Type.VOID:
			return void.class;
		case org.objectweb.asm.Type.ARRAY:
			//logger.trace("Converting to array of type "+asm_type.getElementType());
			return Array.newInstance((Class<?>) getType(asm_type.getElementType()), 0).getClass();
		default:
			try {
				Class<?> clazz = Class.forName(asm_type.getClassName());

				return clazz;
			} catch (ClassNotFoundException e) {
				logger.error("Could not find replacement type for " + asm_type);
			}
		}
		return null;

	}

	/**
	 * Type of return value of a method
	 * 
	 * @param className
	 * @param method
	 * @return
	 */
	public Type getReturnType(String className, Method method) {
		if (return_types.containsKey(method))
			return return_types.get(method);

		if (hasKey(className, method.getName(),
		           org.objectweb.asm.Type.getMethodDescriptor(method))) {
			String replacement = descriptors.get(className + "." + method.getName()
			        + org.objectweb.asm.Type.getMethodDescriptor(method));
			logger.debug("Found replacement: " + replacement);

			Type type = getType(org.objectweb.asm.Type.getReturnType(replacement));
			return_types.put(method, type);
			return type;

		} else {
			return_types.put(method, method.getGenericReturnType());
			return method.getGenericReturnType();
		}
	}

	private void getDescriptorMapping() {
		//String className = System.getProperty("target.class");;
		//File file = new File(MutationProperties.OUTPUT_DIR+"/"+className+".obj");
		File dir = new File(Properties.OUTPUT_DIR);
		assert dir.exists() : "OutputDir '" + dir + "' does not exist!";
		
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".obj"); // && !dir.isDirectory();
			}
		};
		File[] files = dir.listFiles(filter);
		assert files != null : "OutputDir '" + dir + "' does not exist!";
		for (File file : files) {
			if (file.isDirectory())
				continue;
			List<String> lines = Utils.readFile(file);
			//descriptors.put(className, new HashMap<String, String>());
			for (String line : lines) {
				//logger.debug("Read line: "+line);
				line = line.trim();
				// Skip comments
				if (line.startsWith("#"))
					continue;

				String[] parameters = line.split(",");
				if (parameters.length == 2) {
					if (!parameters[0].endsWith(parameters[1])) {
						descriptors.put(parameters[0], parameters[1]);
						logger.debug("Adding descriptor for class " + parameters[0]);
					}
				}
			}
		}
	}
}
