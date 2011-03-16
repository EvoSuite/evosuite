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

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import de.unisb.cs.st.evosuite.Properties;

public class DescriptorMapping {

	private static int id = 0;

	private final Map<String, String> descriptorMapping = new HashMap<String, String>();

	private static Logger logger = Logger.getLogger(DescriptorMapping.class);

	private static DescriptorMapping instance = null;

	private DescriptorMapping() {

	}

	public static DescriptorMapping getInstance() {
		if (instance == null)
			instance = new DescriptorMapping();

		return instance;
	}

	final Map<String, String> original = new HashMap<String, String>();

	private final Map<String, String> nameMapping = new HashMap<String, String>();

	public boolean isTransformedMethod(String className, String methodName, String desc) {
		getMethodDesc(className, methodName, desc);
		return original.containsKey(className + "." + methodName + desc);
	}

	public boolean hasTransformedArguments(String className, String methodName,
	        String desc) {
		getMethodDesc(className, methodName, desc);
		if (!original.containsKey(className + "." + methodName + desc)) {
			return false;
		} else {
			String newDesc = original.get(className + "." + methodName + desc);
			for (Type type : Type.getArgumentTypes(newDesc)) {
				if (type.equals(Type.BOOLEAN_TYPE))
					return true;
			}
			return false;
		}
	}

	public boolean isTransformedField(String className, String fieldName, String desc) {
		getFieldDesc(className, fieldName, desc);
		return original.containsKey(className + "." + fieldName + desc);
	}

	public boolean isTransformedOrBooleanMethod(String className, String methodName,
	        String desc) {
		// logger.info("Checking method: " + className + "." + methodName + desc);
		String new_desc = getMethodDesc(className, methodName, desc);
		TestabilityTransformation.logger.info("Transformed desc is " + new_desc);
		String name = className + "." + methodName + desc;
		if (original.containsKey(name)) {
			TestabilityTransformation.logger.info("Desc is already transformed");
		}
		return original.containsKey(name) || isBooleanMethod(desc);
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
		if (Properties.TRANSFORM_STRING && isStringReplacement(className, methodName))
			return true;

		TestabilityTransformation.logger.info("Checking method: " + className + "."
		        + methodName + desc);
		String new_desc = getMethodDesc(className, methodName, desc);
		TestabilityTransformation.logger.info("Transformed desc is " + new_desc);
		String name = className + "." + methodName + desc;
		if (original.containsKey(name)) {
			return Type.getReturnType(original.get(name)).equals(Type.BOOLEAN_TYPE);
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
		String name = className + "." + fieldName + desc;
		if (original.containsKey(name)) {
			TestabilityTransformation.logger.info("Desc is already transformed");
		}
		return original.containsKey(name) || isBooleanField(desc);
	}

	private boolean isBooleanMethod(String desc) {
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

				if (!parent.name.startsWith(Properties.PROJECT_PREFIX.replace(".", "/"))) {
					TestabilityTransformation.logger.info("Checking " + parent.name);
					for (Object o : parent.methods) {
						MethodNode mn2 = (MethodNode) o;
						if (mn2.name.equals(methodName) && mn2.desc.equals(desc)) {
							TestabilityTransformation.logger.info("Method " + name
							        + " was defined outside the test package");
							return true;
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

				if (original.containsKey(className + "." + methodName + transformedDesc)) {
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

			ClassReader reader;
			try {
				reader = new ClassReader(name);
				ClassNode parent = new ClassNode();
				reader.accept(parent, ClassReader.EXPAND_FRAMES);

				if (!parent.name.startsWith(Properties.PROJECT_PREFIX.replace(".", "/"))) {
					for (Object o : parent.fields) {
						FieldNode mn2 = (FieldNode) o;
						if (mn2.name.equals(fieldName) && mn2.desc.equals(desc)) {
							TestabilityTransformation.logger.info("Field " + name
							        + " was defined outside the test package");
							return true;
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

	private String transformFieldDescriptor(String desc) {
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
		String old = className + "." + methodName + desc;
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
		if (isBooleanMethod(desc)) {
			String old = className + "." + methodName + desc;
			if (!descriptorMapping.containsKey(old)) {
				if (isOutsideMethod(className, methodName, desc)) {
					descriptorMapping.put(old, desc);
					nameMapping.put(old, methodName);
				} else {
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
						original.put(className + "." + newName + newDesc, desc);
					} else {
						descriptorMapping.put(old, desc);
						nameMapping.put(old, methodName);
						// toDO: Original?
						original.put(className + "." + methodName + newDesc, desc);
					}
				}
			}
			return descriptorMapping.get(old);
		} else {
			return desc;
		}
	}

	public String getFieldDesc(String className, String fieldName, String desc) {
		if (isBooleanField(desc)) {
			String old = className + "." + fieldName + desc;
			if (!descriptorMapping.containsKey(old)) {
				if (isOutsideField(className, fieldName, desc)) {
					descriptorMapping.put(old, desc);
				} else {
					descriptorMapping.put(old, transformFieldDescriptor(desc));
					original.put(className + "." + fieldName + descriptorMapping.get(old),
					             desc);
				}
			}
			return descriptorMapping.get(old);
		} else {
			return desc;
		}
	}

	public String getOriginalDescriptor(String className, String methodName, String desc) {
		String key = className.replace(".", "/") + "." + methodName + desc;
		if (original.containsKey(key)) {
			logger.info("Found transformed version of " + className + "." + methodName
			        + desc);
			return original.get(key);
		} else {
			logger.info("Don't have transformed version of " + className + "."
			        + methodName + desc);
			return desc;
		}
	}

	public Type[] getOriginalTypes(String className, String methodName, String desc) {
		String key = className.replace(".", "/") + "." + methodName + desc;
		if (original.containsKey(key))
			return org.objectweb.asm.Type.getArgumentTypes(original.get(key));
		else
			return org.objectweb.asm.Type.getArgumentTypes(desc);
	}
}