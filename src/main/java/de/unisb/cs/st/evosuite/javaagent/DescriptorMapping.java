/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;

public class DescriptorMapping {

	private static int id = 0;

	private final Map<String, String> descriptorMapping = new HashMap<String, String>();

	private static Logger logger = LoggerFactory.getLogger(DescriptorMapping.class);

	private static DescriptorMapping instance = null;

	private DescriptorMapping() {

	}

	public static DescriptorMapping getInstance() {
		if (instance == null)
			instance = new DescriptorMapping();

		return instance;
	}

	final Map<String, String> originalDesc = new HashMap<String, String>();

	final Map<String, String> originalName = new HashMap<String, String>();

	private final Map<String, String> nameMapping = new HashMap<String, String>();

	public static boolean shouldTransform(String classNameUnknown) {
		String className = classNameUnknown.replace("/", ".");
		switch (Properties.TT_SCOPE) {
		case ALL:
			return true;
		case TARGET:
			if (className.equals(Properties.TARGET_CLASS)
			        || className.startsWith(Properties.TARGET_CLASS + "$"))
				return true;
			break;
		case PREFIX:
			if (className.startsWith(Properties.PROJECT_PREFIX))
				return true;

		}
		return false;
	}

	public boolean isTransformedMethod(String className, String methodName, String desc) {
		logger.info("Initiating transformation of " + methodName);
		getMethodDesc(className, methodName, desc);
		//		return originalDesc.containsKey(className.replace(".", "/") + "/" + methodName
		//		        + desc);
		return descriptorMapping.containsKey(className.replace(".", "/") + "/"
		        + methodName + desc);
	}

	public boolean hasTransformedArguments(String className, String methodName,
	        String desc) {
		getMethodDesc(className, methodName, desc);
		if (!originalDesc.containsKey(className.replace(".", "/") + "/" + methodName
		        + desc)) {
			return false;
		} else {
			String newDesc = originalDesc.get(className.replace(".", "/") + "/"
			        + methodName + desc);
			for (Type type : Type.getArgumentTypes(newDesc)) {
				if (type.equals(Type.BOOLEAN_TYPE))
					return true;
			}
			return false;
		}
	}

	public boolean isTransformedField(String className, String fieldName, String desc) {
		getFieldDesc(className, fieldName, desc);
		return descriptorMapping.containsKey(className.replace(".", "/") + "/"
		        + fieldName + desc);
	}

	public boolean isTransformedOrBooleanMethod(String className, String methodName,
	        String desc) {
		logger.info("Checking method: " + className + "." + methodName + desc);
		String new_desc = getMethodDesc(className, methodName, desc);
		TestabilityTransformation.logger.info("Transformed desc is " + new_desc);
		String name = className.replace(".", "/") + "/" + methodName + desc;
		if (originalDesc.containsKey(name)) {
			TestabilityTransformation.logger.info("Desc is already transformed");
		}
		return originalDesc.containsKey(name) || isBooleanMethod(desc);
	}

	private boolean isStringReplacement(String className, String methodName) {
		if (className.equals("de/unisb/cs/st/evosuite/javaagent/TestabilityTransformation")) {
			if (methodName.equals("StringEquals")
			        || methodName.equals("StringEqualsIgnoreCase")
			        || methodName.equals("StringIsEmpty")
			        || methodName.equals("StringStartsWith")
			        || methodName.equals("StringEndsWith"))
				return true;
		}
		return false;
	}

	public boolean isTransformedOrBooleanReturnMethod(String className,
	        String methodName, String desc) {
		if (isStringReplacement(className, methodName))
			return true;

		TestabilityTransformation.logger.info("Checking method: " + className + "."
		        + methodName + desc);
		String new_desc = getMethodDesc(className, methodName, desc);
		TestabilityTransformation.logger.info("Transformed desc is " + new_desc);
		String name = className.replace(".", "/") + "/" + methodName + desc;
		if (originalDesc.containsKey(name)) {
			return Type.getReturnType(originalDesc.get(name)).equals(Type.BOOLEAN_TYPE);
		} else {
			return Type.getReturnType(desc).equals(Type.BOOLEAN_TYPE);
		}
	}

	public boolean isTransformedOrBooleanField(String className, String fieldName,
	        String desc) {
		TestabilityTransformation.logger.info("Checking field: " + className + "."
		        + fieldName + desc);
		String new_desc = getFieldDesc(className, fieldName, desc);
		TestabilityTransformation.logger.info("Transformed desc is " + new_desc);
		String name = className.replace(".", "/") + "/" + fieldName + desc;
		if (originalDesc.containsKey(name)) {
			TestabilityTransformation.logger.info("Desc is already transformed");
		}
		return originalDesc.containsKey(name) || isBooleanField(desc);
	}

	public boolean isBooleanMethod(String desc) {
		Type[] types = Type.getArgumentTypes(desc);
		for (Type type : types) {
			if (type.equals(Type.BOOLEAN_TYPE)) {
				return true;
			} else if (type.getDescriptor().equals("[Z")) {
				return true;
			}
		}

		Type type = Type.getReturnType(desc);
		if (type.equals(Type.BOOLEAN_TYPE)) {
			return true;
		} else if (type.getDescriptor().equals("[Z")) {
			return true;
		}

		return false;
	}

	public boolean hasBooleanParameters(String desc) {
		for (Type t : Type.getArgumentTypes(desc)) {
			if (t.equals(Type.BOOLEAN_TYPE))
				return true;
		}

		return false;
	}

	private boolean isInside(String className) {
		String classNameWithDots = className.replace("/", ".");
		switch (Properties.TT_SCOPE) {
		case ALL:
			if (!classNameWithDots.startsWith("java")
			        && !classNameWithDots.startsWith("sun"))
				return true;
		case TARGET:
			if (classNameWithDots.equals(Properties.TARGET_CLASS)
			        || classNameWithDots.startsWith(Properties.TARGET_CLASS + "$"))
				return true;
			break;
		case PREFIX:
			if (classNameWithDots.startsWith(Properties.PROJECT_PREFIX))
				return true;

		}
		return false;
	}

	private boolean isBooleanField(String desc) {
		TestabilityTransformation.logger.info("Checkign type of field " + desc);
		return desc.endsWith("Z");
		//Type type = Type.getType(desc);
		//return type.equals(Type.BOOLEAN_TYPE)
		//       || (type.equals(Type.ARRAY) && type.getElementType().equals(Type.BOOLEAN_TYPE));
	}

	private boolean isOutsideMethod(String className, String methodName, String desc) {
		Set<String> visited = new HashSet<String>();
		Queue<String> parents = new LinkedList<String>();
		parents.add(className);

		while (!parents.isEmpty()) {
			String name = parents.poll();
			if (name == null)
				continue;

			visited.add(name);
			TestabilityTransformation.logger.info("Visiting class " + name
			        + " while looking for source of " + className + "." + methodName);
			ClassReader reader;
			try {
				reader = new ClassReader(name);
				ClassNode parent = new ClassNode();
				reader.accept(parent, ClassReader.EXPAND_FRAMES);

				boolean isInside = isInside(parent.name);

				//boolean isInside = parent.name.startsWith(Properties.PROJECT_PREFIX.replace(".",
				//                                                                            "/"))
				//        || (!Properties.TARGET_CLASS_PREFIX.isEmpty() && parent.name.startsWith(Properties.TARGET_CLASS_PREFIX.replace(".",
				//				                                                                                                                       "/")));

				logger.info("Checking " + parent.name);
				for (Object o : parent.methods) {
					MethodNode mn2 = (MethodNode) o;
					if (mn2.name.equals(methodName) && mn2.desc.equals(desc)) {
						if (!isInside) {
							logger.info("Method " + name
							        + " was defined outside the test package");
							return true;
						} else {
							logger.info("Method " + name
							        + " was defined outside the test package");
							return false;
						}
					}
				}

				for (Object o : parent.interfaces) {
					String par = (String) o;
					if (!visited.contains(par) && !parents.contains(par)) {
						parents.add(par);
					}
				}
				if (!visited.contains(parent.superName)
				        && !parents.contains(parent.superName)) {
					parents.add(parent.superName);
				}
			} catch (IOException e) {
				TestabilityTransformation.logger.info("Error reading class " + name);
			}
		}

		return false;
	}

	private String transformMethodName(String className, String methodName, String desc,
	        String transformedDesc) {
		Set<String> visited = new HashSet<String>();
		Queue<String> parents = new LinkedList<String>();
		parents.add(className);

		while (!parents.isEmpty()) {
			String name = parents.poll();
			if (name == null)
				continue;

			visited.add(name);
			TestabilityTransformation.logger.info("Visiting class " + name
			        + " while looking for name clashes of " + className + "."
			        + methodName + transformedDesc);
			ClassReader reader;
			try {
				reader = new ClassReader(name);
				ClassNode parent = new ClassNode();
				reader.accept(parent, ClassReader.EXPAND_FRAMES);

				if (originalDesc.containsKey(className + "." + methodName
				        + transformedDesc)) {
					TestabilityTransformation.logger.info("Method " + methodName
					        + " has conflicting transformed method");
					return methodName + "_transformed" + (id++);
				}

				for (Object o : parent.methods) {
					MethodNode mn2 = (MethodNode) o;
					//logger.info("Checking " + parent.name + "." + mn2.name + mn2.desc);
					if (mn2.name.equals(methodName) && mn2.desc.equals(transformedDesc)) {
						TestabilityTransformation.logger.info("Method " + methodName
						        + " has conflicting method");
						if (methodName.equals("<init>"))
							return null; // TODO: This should be a bit nicer
						return methodName + "_transformed" + (id++);
					}
				}

				for (Object o : parent.interfaces) {
					String par = (String) o;
					if (!visited.contains(par) && !parents.contains(par)) {
						parents.add(par);
					}
				}
				if (!visited.contains(parent.superName)
				        && !parents.contains(parent.superName)) {
					parents.add(parent.superName);
				}
			} catch (IOException e) {
				TestabilityTransformation.logger.info("Error reading class " + name);
			}
		}

		return methodName;
	}

	private boolean isOutsideField(String className, String fieldName, String desc) {
		Set<String> visited = new HashSet<String>();
		Queue<String> parents = new LinkedList<String>();
		parents.add(className);

		while (!parents.isEmpty()) {
			String name = parents.poll();
			if (name == null)
				continue;

			visited.add(name);
			logger.info("Checking class " + name
			        + " while looking for definition of field " + fieldName);

			ClassReader reader;
			try {
				reader = new ClassReader(name);
				ClassNode parent = new ClassNode();
				reader.accept(parent, ClassReader.EXPAND_FRAMES);

				boolean isInside = isInside(parent.name);

				//				boolean isInside = parent.name.startsWith(Properties.PROJECT_PREFIX.replace(".",
				//				                                                                            "/"))
				//				        | parent.name.startsWith(Properties.TARGET_CLASS_PREFIX.replace(".",
				//				                                                                        "/"));

				for (Object o : parent.fields) {
					FieldNode mn2 = (FieldNode) o;
					if (mn2.name.equals(fieldName) && mn2.desc.equals(desc)) {
						if (!isInside) {
							logger.info("Field " + name
							        + " was defined outside the test package - "
							        + parent.name);
							return true;
						} else {
							logger.info("Field " + name
							        + " was defined inside the test package "
							        + parent.name);
							return false;

						}
					}
				}
				for (Object o : parent.interfaces) {
					String par = (String) o;
					if (!visited.contains(par) && !parents.contains(par)) {
						parents.add(par);
					}
				}
				if (!visited.contains(parent.superName)
				        && !parents.contains(parent.superName)) {
					parents.add(parent.superName);
				}
			} catch (IOException e) {
				logger.info("Error reading class " + name);
			}
		}

		return false;
	}

	private String transformMethodDescriptor(String desc) {
		String new_desc = "(";

		Type[] types = Type.getArgumentTypes(desc);
		for (Type type : types) {
			if (type.equals(Type.BOOLEAN_TYPE)) {
				new_desc += "I";
			} else if (type.getDescriptor().equals("[Z")) {
				new_desc += "[I";
			} else {
				new_desc += type.getDescriptor();
			}
		}
		new_desc += ")";

		Type type = Type.getReturnType(desc);
		if (type.equals(Type.BOOLEAN_TYPE)) {
			new_desc += "I";
		} else if (type.getDescriptor().equals("[Z")) {
			new_desc += "[I";
		} else {
			new_desc += type.getDescriptor();
		}

		return new_desc;
	}

	private String transformFieldDescriptor(String className, String desc) {
		if (!shouldTransform(className)) {
			return desc;
		}
		TestabilityTransformation.logger.info("Transforming field instruction " + desc);
		if (isBooleanField(desc)) {
			// TODO: Check if this is actually transformed or not
			if (desc.equals("Z"))
				return "I";
			else if (desc.equals("[Z"))
				return "[I";
			else
				return desc;
		} else {
			return desc;
		}
	}

	public String getMethodName(String className, String methodName, String desc) {
		if (!shouldTransform(className)) {
			return methodName;
		}

		String old = className + "." + methodName + desc;
		old = old.replace(".", "/");
		if (isBooleanMethod(desc)) {
			getMethodDesc(className, methodName, desc);
			return nameMapping.get(old);
		} else {
			if (nameMapping.containsKey(old))
				return nameMapping.get(old);
			else
				return methodName;
		}
	}

	public String getMethodDesc(String className, String methodName, String desc) {
		if (!shouldTransform(className)) {
			return desc;
		}

		if (isBooleanMethod(desc)) {
			String old = className.replace(".", "/") + "/" + methodName + desc;
			//old = old.replace(".", "/");

			if (!descriptorMapping.containsKey(old)) {
				if (isOutsideMethod(className, methodName, desc)) {
					logger.info("Is outside method: " + className + "." + methodName);
					descriptorMapping.put(old, desc);
					nameMapping.put(old, methodName);
				} else {
					logger.info("Is inside method: " + className + "." + methodName);
					String newDesc = transformMethodDescriptor(desc);
					String newName = transformMethodName(className, methodName, desc,
					                                     newDesc);
					if (newName != null) {
						descriptorMapping.put(old, newDesc);
						nameMapping.put(old, newName);
						//nameMapping.put(className + "." + methodName + newDesc,
						//                newName);
						TestabilityTransformation.logger.info("Keeping transformation from "
						        + old
						        + " to "
						        + descriptorMapping.get(old)
						        + " with new name " + newName);
						originalDesc.put(className.replace(".", "/") + "/" + newName
						        + newDesc, desc);
						originalName.put(className.replace(".", "/") + "/" + newName
						        + newDesc, methodName);
					} else {
						descriptorMapping.put(old, desc);
						nameMapping.put(old, methodName);
						// toDO: Original?
						originalDesc.put(className.replace(".", "/") + "/" + methodName
						        + newDesc, desc);
						originalName.put(className.replace(".", "/") + "/" + methodName
						        + newDesc, methodName);
					}
				}
			}
			return descriptorMapping.get(old);
		} else {
			return desc;
		}
	}

	public String getFieldDesc(String className, String fieldName, String desc) {
		if (!shouldTransform(className)) {
			return desc;
		}
		if (isBooleanField(desc)) {
			String old = className.replace(".", "/") + "/" + fieldName + desc;
			//old = old.replace(".", "/");

			if (!descriptorMapping.containsKey(old)) {
				if (isOutsideField(className, fieldName, desc)) {
					descriptorMapping.put(old, desc);
				} else {
					descriptorMapping.put(old, transformFieldDescriptor(className, desc));
					originalDesc.put(className.replace(".", "/") + "/" + fieldName
					        + descriptorMapping.get(old), desc);
				}
			}
			return descriptorMapping.get(old);
		} else {
			return desc;
		}
	}

	public String getOriginalName(String className, String methodName, String desc) {
		String key = className.replace(".", "/") + "/" + methodName + desc;
		if (originalName.containsKey(key)) {
			logger.info("Found transformed version of " + className + "." + methodName
			        + desc);
			return originalName.get(key);
		} else {
			logger.info("Don't have transformed version of " + className + "."
			        + methodName + desc);
			return methodName;
		}
	}

	public String getOriginalDescriptor(String className, String methodName, String desc) {
		String key = className.replace(".", "/") + "/" + methodName + desc;
		if (originalDesc.containsKey(key)) {
			logger.info("Found transformed version of " + className + "." + methodName
			        + desc);
			return originalDesc.get(key);
		} else {
			logger.info("Don't have transformed version of " + className + "."
			        + methodName + desc);
			return desc;
		}
	}

	public Type[] getOriginalTypes(String className, String methodName, String desc) {
		String key = className.replace(".", "/") + "/" + methodName + desc;
		if (originalDesc.containsKey(key))
			return org.objectweb.asm.Type.getArgumentTypes(originalDesc.get(key));
		else
			return org.objectweb.asm.Type.getArgumentTypes(desc);
	}
}
