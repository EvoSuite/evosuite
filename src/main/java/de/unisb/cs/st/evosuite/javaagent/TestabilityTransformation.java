/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.testability.BooleanHelper;

/**
 * @author Gordon Fraser
 * 
 */
// TODO: If we transform a method and there already is a method with the transformed descriptor, we need to change the method name as well!
public class TestabilityTransformation {

	static Logger logger = Logger.getLogger(TestabilityTransformation.class);

	private final ClassNode cn;

	public static final int K = Properties.getPropertyOrDefault("K",
	                                                            (Integer.MAX_VALUE - 2));

	private static Stack<Integer> distanceStack = null; //new Stack<Integer>();

	private static Stack<Stack<Integer>> stackStack = new Stack<Stack<Integer>>();

	public static String getTransformedDesc(String className, String methodName,
	        String desc) {
		return mapping.getMethodDesc(className, methodName, desc);
	}

	public static String getOriginalNameDesc(String className, String methodName,
	        String desc) {
		String key = className.replace(".", "/") + "." + methodName + desc;
		if (mapping.original.containsKey(key)) {
			return methodName + mapping.original.get(key);
		} else {
			return methodName + desc;
		}
	}

	private static DescriptorMapping mapping = new DescriptorMapping();

	private static Set<JumpInsnNode> flagNodes = new HashSet<JumpInsnNode>();

	public TestabilityTransformation(ClassNode cn) {
		this.cn = cn;
	}

	public ClassNode transform() {
		processFields();
		processMethods();
		return cn;
	}

	private void processFields() {
		if (!Properties.TRANSFORM_BOOLEAN)
			return;

		List<FieldNode> fieldNodes = cn.fields;
		int i = 0;
		for (FieldNode fn : fieldNodes) {
			logger.info("Transforming field " + fn.name + " - " + fn.desc);
			fn.desc = mapping.getFieldDesc(cn.name, fn.name, fn.desc);
			logger.info("Transformed field: " + fn.desc);
		}
	}

	private void processMethods() {
		List<MethodNode> methodNodes = cn.methods;
		for (MethodNode mn : methodNodes) {
			if (Properties.TRANSFORM_BOOLEAN) {
				// If this method was defined somewhere outside the test package, do not transform signature
				String desc = mn.desc;
				mn.desc = mapping.getMethodDesc(cn.name, mn.name, mn.desc);
				mn.name = mapping.getMethodName(cn.name, mn.name, desc);
				logger.info("Now going inside " + mn.name + mn.desc);
				// Actually this should be done automatically by the ClassWriter...
				// +2 because we might do a DUP2
				mn.maxStack += 3;
			}
			transformMethod(mn);

		}
	}

	private boolean isBooleanField(String desc) {
		Type type = Type.getType(desc);
		return type.equals(Type.BOOLEAN_TYPE);
	}

	private boolean isBooleanMethod(String desc) {
		Type[] types = Type.getArgumentTypes(desc);
		for (Type type : types) {
			if (type.equals(Type.BOOLEAN_TYPE)) {
				return true;
			} else if (type.equals(Type.ARRAY)) {
				if (type.getElementType().equals(Type.BOOLEAN_TYPE)) {
					return true;
				}
			}
		}

		Type type = Type.getReturnType(desc);
		if (type.equals(Type.BOOLEAN_TYPE)) {
			return true;
		} else if (type.equals(Type.ARRAY)) {
			if (type.getElementType().equals(Type.BOOLEAN_TYPE)) {
				return true;
			}
		}

		return false;
	}

	private boolean isBooleanMethod(MethodNode mn) {
		if (mn.desc.endsWith("Z") || mn.desc.endsWith("[Z")) {
			logger.info("Method " + mn.name + mn.desc + " is a boolean method");
			return true;
		} else {
			Type[] types = Type.getArgumentTypes(mn.desc);
			for (Type type : types) {
				if (type.equals(Type.BOOLEAN_TYPE)) {
					logger.info("Method " + mn.name + mn.desc + " is a boolean method");
					return true;
				} else if (type.equals(Type.ARRAY)) { // TODO: Does this work?
					if (type.getElementType().equals(Type.BOOLEAN_TYPE)) {
						logger.info("Method " + mn.name + mn.desc
						        + " is a boolean method");
						return true;
					}
				}
			}
			logger.info("Method " + mn.name + mn.desc + " is not a boolean method");
		}

		return false;
	}

	private boolean isOutsideMethod(MethodNode mn) {
		Set<String> visited = new HashSet<String>();
		Queue<String> parents = new LinkedList<String>();
		parents.addAll(cn.interfaces);
		parents.add(cn.superName);

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

				if (!parent.name.startsWith(Properties.PROJECT_PREFIX)) {
					for (Object o : parent.methods) {
						MethodNode mn2 = (MethodNode) o;
						if (mn2.name.equals(mn.name) && mn2.desc.equals(mn.desc)) {
							logger.info("Method " + mn.name
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
				logger.info("Error reading class " + name);
			}
		}

		return false;
	}

	private boolean isBooleanAssignment(AbstractInsnNode position, MethodNode mn) {
		AbstractInsnNode node = position.getNext();

		boolean done = false;
		while (!done) {
			if (node.getOpcode() == Opcodes.PUTFIELD
			        || node.getOpcode() == Opcodes.PUTSTATIC) {
				// TODO: Check whether field is static
				logger.info("Checking field assignment");
				FieldInsnNode fn = (FieldInsnNode) node;
				if (mapping.isTransformedOrBooleanField(fn.owner, fn.name, fn.desc)) {
					return true;
				} else {
					return false;
				}
			} else if (node.getOpcode() == Opcodes.ISTORE) {
				VarInsnNode vn = (VarInsnNode) node;
				// TODO: Check whether variable at this position is a boolean
				if (isBooleanVariable(vn.var, mn)) {
					logger.info("Assigning boolean to variable ");
					return true;
				} else {
					logger.info("Variable is not a bool");
					return false;
				}
			} else if (node.getOpcode() == Opcodes.IRETURN) {
				logger.info("Checking return value of method " + cn.name + "." + mn.name);
				if (mapping.isTransformedOrBooleanMethod(cn.name, mn.name, mn.desc)) {
					return true;
				} else {
					return false;
				}
			} else if (node.getOpcode() == Opcodes.BASTORE) {
				// We remove all bytes, so BASTORE is only used for booleans
				AbstractInsnNode start = position.getNext();
				boolean reassignment = false;
				while (start != node) {
					if (node instanceof InsnNode) {
						reassignment = true;
					}
					start = start.getNext();
				}
				logger.info("Possible assignment to array?");
				if (reassignment)
					return false;
				else
					return true;

			} else if (node instanceof MethodInsnNode) {
				// if it is a boolean parameter of a converted method, then it needs to be converted
				// Problem: How do we know which parameter it represents?

				logger.warn("Cannot handle method insn?"); // TODO: Just got to check last parameter?
				return false;

			} else if (node.getOpcode() == Opcodes.GOTO
			        || node.getOpcode() == Opcodes.ICONST_0
			        || node.getOpcode() == Opcodes.ICONST_1 || node.getOpcode() == -1) {
				logger.info("Continuing search");

				// continue search
			} else if (!(node instanceof LineNumberNode || node instanceof FrameNode)) {
				logger.info("Search ended with opcode " + node.getOpcode());

				return false;
			}
			if (node != mn.instructions.getLast())
				node = node.getNext();
			else
				done = true;
		}

		return false;
	}

	private void insertPushNull(int opcode, AbstractInsnNode position, InsnList list) {
		MethodInsnNode nullCheck = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(TestabilityTransformation.class), "isNull",
		        Type.getMethodDescriptor(Type.INT_TYPE,
		                                 new Type[] { Type.getType(Object.class),
		                                         Type.INT_TYPE }));
		list.insertBefore(position, new InsnNode(Opcodes.DUP));
		list.insertBefore(position, new LdcInsnNode(opcode));
		list.insertBefore(position, nullCheck);
		MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(TestabilityTransformation.class), "pushPredicate",
		        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE }));
		list.insertBefore(position, push);

	}

	private void insertPushEquals(int opcode, AbstractInsnNode position, InsnList list) {
		MethodInsnNode equalCheck = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(TestabilityTransformation.class), "isEqual",
		        Type.getMethodDescriptor(Type.INT_TYPE,
		                                 new Type[] { Type.getType(Object.class),
		                                         Type.getType(Object.class),
		                                         Type.INT_TYPE }));
		list.insertBefore(position, new InsnNode(Opcodes.DUP2));
		list.insertBefore(position, new LdcInsnNode(opcode));
		list.insertBefore(position, equalCheck);
		MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(TestabilityTransformation.class), "pushPredicate",
		        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE }));
		list.insertBefore(position, push);

	}

	private void insertPush(int opcode, AbstractInsnNode position, InsnList list) {
		//list.insertBefore(position, new InsnNode(Opcodes.ICONST_0));
		list.insertBefore(position, new InsnNode(Opcodes.DUP));
		//list.insertBefore(position, new InsnNode(Opcodes.SWAP));
		//list.insertBefore(position, new InsnNode(Opcodes.ISUB));
		MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(TestabilityTransformation.class), "pushPredicate",
		        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE }));
		list.insertBefore(position, push);

	}

	private void insertPush2(int opcode, AbstractInsnNode position, InsnList list) {
		list.insertBefore(position, new InsnNode(Opcodes.DUP2));
		list.insertBefore(position, new InsnNode(Opcodes.ISUB));
		MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(TestabilityTransformation.class), "pushPredicate",
		        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE }));
		list.insertBefore(position, push);

	}

	private void insertGet(AbstractInsnNode position, InsnList list) {
		MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(TestabilityTransformation.class), "getDistance",
		        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] { Type.INT_TYPE }));
		list.insert(position, get);
		//list.remove(position);
	}

	private void insertLongComparison(AbstractInsnNode position, InsnList list) {
		list.insertBefore(position, new InsnNode(Opcodes.LSUB));
		MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(TestabilityTransformation.class), "fromLong",
		        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] { Type.LONG_TYPE }));
		list.insert(position, get);
		list.remove(position);
	}

	private void insertFloatComparison(AbstractInsnNode position, InsnList list) {
		list.insertBefore(position, new InsnNode(Opcodes.FSUB));
		MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(TestabilityTransformation.class), "fromFloat",
		        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] { Type.FLOAT_TYPE }));
		list.insert(position, get);
		list.remove(position);
	}

	private void insertDoubleComparison(AbstractInsnNode position, InsnList list) {
		list.insertBefore(position, new InsnNode(Opcodes.DSUB));
		MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(TestabilityTransformation.class), "fromDouble",
		        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] { Type.DOUBLE_TYPE }));
		list.insert(position, get);
		list.remove(position);
	}

	private String transformMethodDescriptor(String desc) {
		String new_desc = "(";

		Type[] types = Type.getArgumentTypes(desc);
		for (Type type : types) {
			if (type.equals(Type.BOOLEAN_TYPE)) {
				new_desc += "I";
			} else if (type.equals(Type.ARRAY)) {
				if (type.getElementType().equals(Type.BOOLEAN_TYPE)) {
					new_desc += "[I";
				} else {
					new_desc += type.getDescriptor();
				}
			} else {
				new_desc += type.getDescriptor();
			}
		}
		new_desc += ")";

		Type type = Type.getReturnType(desc);
		if (type.equals(Type.BOOLEAN_TYPE)) {
			new_desc += "I";
		} else if (type.equals(Type.ARRAY)) {
			if (type.getElementType().equals(Type.BOOLEAN_TYPE)) {
				new_desc += "[I";
			} else {
				new_desc += type.getDescriptor();
			}
		} else {
			new_desc += type.getDescriptor();
		}

		return new_desc;
	}

	private LocalVariableNode getVariable(int var, MethodNode mn) {
		for (Object o : mn.localVariables) {
			LocalVariableNode vn = (LocalVariableNode) o;
			if (vn.index == var)
				return vn;
		}
		return null;
	}

	private boolean isBooleanVariable(int var, MethodNode mn) {
		for (Object o : mn.localVariables) {
			LocalVariableNode vn = (LocalVariableNode) o;
			if (vn.index == var)
				return Type.getType(vn.desc).equals(Type.BOOLEAN_TYPE);
		}
		return false;
	}

	private void transformLocalVariables(MethodNode mn) {
		logger.info("Transforming local variables");
		List<LocalVariableNode> variables = mn.localVariables;
		if (variables == null)
			return;
		int num = 0;
		for (LocalVariableNode var : variables) {
			if (Type.getType(var.desc).equals(Type.BOOLEAN_TYPE)) {
				var.desc = Type.INT_TYPE.getDescriptor();
			}
			num++;
		}
	}

	private void transformFieldInsn(FieldInsnNode node, MethodNode mn) {
		logger.info("Transforming field instruction");
		node.desc = mapping.getFieldDesc(node.owner, node.name, node.desc);
		if (Type.getType(node.desc).equals(Type.BOOLEAN_TYPE)) {
			if (node.getOpcode() == Opcodes.PUTFIELD
			        || node.getOpcode() == Opcodes.PUTSTATIC) {
				MethodInsnNode n = new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(TestabilityTransformation.class),
				        "intToBoolean",
				        Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
				                                 new Type[] { Type.INT_TYPE }));
				mn.instructions.insertBefore(node, n);
			} else {
				MethodInsnNode n = new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(TestabilityTransformation.class),
				        "booleanToInt",
				        Type.getMethodDescriptor(Type.INT_TYPE,
				                                 new Type[] { Type.BOOLEAN_TYPE }));
				mn.instructions.insert(node, n);

			}
		}
	}

	private void transformMethodInsn(MethodInsnNode node, MethodNode mn) {
		logger.info("Transforming method instruction " + node.name);
		node.desc = mapping.getMethodDesc(node.owner, node.name, node.desc);

		if (mapping.hasBooleanParameters(node.desc)) {
			// Convert ints on stack back to boolean

			int firstBooleanParameterIndex = -1;
			Type[] types = Type.getArgumentTypes(node.desc);
			for (int i = 0; i < types.length; i++) {
				if (types[i].getDescriptor().equals("Z")) {
					if (firstBooleanParameterIndex == -1) {
						firstBooleanParameterIndex = i;
					}
				}
			}
			if (firstBooleanParameterIndex != -1) {
				int numOfPushs = types.length - 1 - firstBooleanParameterIndex;
				//                        int numOfPushs = types.length - firstBooleanParameterIndex;

				if (numOfPushs == 0) {
					//the boolean parameter is the last parameter
					MethodInsnNode booleanHelperInvoke = new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        Type.getInternalName(TestabilityTransformation.class),
					        "intToBoolean",
					        Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
					                                 new Type[] { Type.INT_TYPE }));
					mn.instructions.insertBefore(node, booleanHelperInvoke);
				} else {
					InsnList insnlist = new InsnList();

					for (int i = 0; i < numOfPushs; i++) {
						MethodInsnNode booleanHelperPushParameter;
						if (types[types.length - 1 - i] == Type.BOOLEAN_TYPE
						        || types[types.length - 1 - i] == Type.CHAR_TYPE
						        || types[types.length - 1 - i] == Type.BYTE_TYPE
						        || types[types.length - 1 - i] == Type.SHORT_TYPE
						        || types[types.length - 1 - i] == Type.INT_TYPE
						        || types[types.length - 1 - i] == Type.FLOAT_TYPE
						        || types[types.length - 1 - i] == Type.LONG_TYPE
						        || types[types.length - 1 - i] == Type.DOUBLE_TYPE) {
							if (types[types.length - 1 - i] == Type.BOOLEAN_TYPE) {
								booleanHelperPushParameter = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "pushParameter",
								        Type.getMethodDescriptor(Type.VOID_TYPE,
								                                 new Type[] { Type.INT_TYPE }));
							} else {
								booleanHelperPushParameter = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "pushParameter",
								        Type.getMethodDescriptor(Type.VOID_TYPE,
								                                 new Type[] { types[types.length
								                                         - 1 - i] }));
							}
						} else {
							booleanHelperPushParameter = new MethodInsnNode(
							        Opcodes.INVOKESTATIC,
							        Type.getInternalName(BooleanHelper.class),
							        "pushParameter",
							        Type.getMethodDescriptor(Type.VOID_TYPE,
							                                 new Type[] { Type.getType(Object.class) }));
						}

						insnlist.add(booleanHelperPushParameter);
					}
					for (int i = firstBooleanParameterIndex; i < types.length; i++) {
						if (i == firstBooleanParameterIndex) {
							MethodInsnNode booleanHelperInvoke = new MethodInsnNode(
							        Opcodes.INVOKESTATIC,
							        Type.getInternalName(TestabilityTransformation.class),
							        "intToBoolean",
							        Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
							                                 new Type[] { Type.INT_TYPE }));
							insnlist.add(booleanHelperInvoke);
						} else {
							MethodInsnNode booleanHelperPopParameter;
							boolean objectNeedCast = false;
							if (types[i] == Type.BOOLEAN_TYPE) {
								booleanHelperPopParameter = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "popParameterBooleanFromInt",
								        Type.getMethodDescriptor(types[i], new Type[] {}));
							} else if (types[i] == Type.CHAR_TYPE) {
								booleanHelperPopParameter = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "popParameterChar",
								        Type.getMethodDescriptor(types[i], new Type[] {}));
							} else if (types[i] == Type.BYTE_TYPE) {
								booleanHelperPopParameter = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "popParameterByte",
								        Type.getMethodDescriptor(types[i], new Type[] {}));
							} else if (types[i] == Type.SHORT_TYPE) {
								booleanHelperPopParameter = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "popParameterShort",
								        Type.getMethodDescriptor(types[i], new Type[] {}));
							} else if (types[i] == Type.INT_TYPE) {
								booleanHelperPopParameter = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "popParameterInt",
								        Type.getMethodDescriptor(types[i], new Type[] {}));
							} else if (types[i] == Type.FLOAT_TYPE) {
								booleanHelperPopParameter = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "popParameterFloat",
								        Type.getMethodDescriptor(types[i], new Type[] {}));
							} else if (types[i] == Type.LONG_TYPE) {
								booleanHelperPopParameter = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "popParameterLong",
								        Type.getMethodDescriptor(types[i], new Type[] {}));
							} else if (types[i] == Type.DOUBLE_TYPE) {
								booleanHelperPopParameter = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "popParameterDouble",
								        Type.getMethodDescriptor(types[i], new Type[] {}));
							} else {
								objectNeedCast = true;
								booleanHelperPopParameter = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "popParameterObject",
								        Type.getMethodDescriptor(Type.getType(Object.class),
								                                 new Type[] {}));
							}

							insnlist.add(booleanHelperPopParameter);
							if (objectNeedCast) {
								TypeInsnNode tin = new TypeInsnNode(Opcodes.CHECKCAST,
								        types[i].getInternalName());
								insnlist.add(tin);
							}
							if (types[i].getDescriptor().equals("Z")
							        || types[i].getDescriptor().equals(Type.getDescriptor(Boolean.class))) {
								MethodInsnNode booleanHelperCast = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "RevertIntToBoolean",
								        Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
								                                 new Type[] { Type.INT_TYPE }));
								insnlist.add(booleanHelperCast);
							}
						}

					}
					mn.instructions.insertBefore(node, insnlist);
				}

			}
			if (Type.getReturnType(node.desc).equals(Type.BOOLEAN_TYPE)) {
				MethodInsnNode n = new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(TestabilityTransformation.class),
				        "booleanToInt",
				        Type.getMethodDescriptor(Type.INT_TYPE,
				                                 new Type[] { Type.BOOLEAN_TYPE }));
				mn.instructions.insert(node, n);
			}
		} else if (mapping.hasTransformedArguments(node.owner, node.name, node.desc)) {
			// If it's a non-boolean method, make sure there are no boolean parameters
			logger.info("Verifying parameters for transformed method");
			node.name = mapping.getMethodName(node.owner, node.name, node.desc);
			// TODO: Check if we need to change the method name as well

			/*
			Type[] types = Type.getArgumentTypes(node.desc);
			try {
				Analyzer a = new Analyzer(new IsBooleanInterpreter());
				a.analyze(cn.name, mn);
				Frame[] frames = a.getFrames();
				Frame current = frames[mn.instructions.indexOf(node)];
				int firstBooleanParameterIndex = -1;
				int stackSize = current.getStackSize();
				for (int i = 0; i < types.length; i++) {
					if (current.getStack(stackSize - types.length + i) == IsBooleanInterpreter.BOOLEAN) {
						firstBooleanParameterIndex = i;
						break;
					}
				}
				if (firstBooleanParameterIndex > 0) {
					logger.info("Need to check starting with argument "
					        + firstBooleanParameterIndex + " out of " + types.length);
					int numOfPushs = types.length - 1 - firstBooleanParameterIndex;
					if (numOfPushs == 0) {
						//the boolean parameter is the last parameter
						MethodInsnNode booleanHelperInvoke = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(TestabilityTransformation.class),
						        "booleanToInt",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] { Type.BOOLEAN_TYPE }));
						mn.instructions.insertBefore(node, booleanHelperInvoke);
					}else {
						InsnList insnlist = new InsnList();

						for (int i = 0; i < numOfPushs; i++) {
							MethodInsnNode booleanHelperPushParameter;
							if (types[types.length - 1 - i] == Type.BOOLEAN_TYPE
							        || types[types.length - 1 - i] == Type.CHAR_TYPE
							        || types[types.length - 1 - i] == Type.BYTE_TYPE
							        || types[types.length - 1 - i] == Type.SHORT_TYPE
							        || types[types.length - 1 - i] == Type.INT_TYPE
							        || types[types.length - 1 - i] == Type.FLOAT_TYPE
							        || types[types.length - 1 - i] == Type.LONG_TYPE
							        || types[types.length - 1 - i] == Type.DOUBLE_TYPE) {
								if (types[types.length - 1 - i] == Type.BOOLEAN_TYPE) {
									booleanHelperPushParameter = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "pushParameter",
									        Type.getMethodDescriptor(Type.VOID_TYPE,
									                                 new Type[] { Type.BOOLEAN_TYPE }));
								} else {
									booleanHelperPushParameter = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "pushParameter",
									        Type.getMethodDescriptor(Type.VOID_TYPE,
									                                 new Type[] { types[types.length
									                                         - 1 - i] }));
								}
							} else {
								booleanHelperPushParameter = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "pushParameter",
								        Type.getMethodDescriptor(Type.VOID_TYPE,
								                                 new Type[] { Type.getType(Object.class) }));
							}

							insnlist.add(booleanHelperPushParameter);
						}
						for (int i = firstBooleanParameterIndex; i < types.length; i++) {
							if (i == firstBooleanParameterIndex) {
								MethodInsnNode booleanHelperInvoke = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "booleanToInt",
								        Type.getMethodDescriptor(Type.INT_TYPE,
								                                 new Type[] { Type.BOOLEAN_TYPE }));
								insnlist.add(booleanHelperInvoke);
							} else {
								MethodInsnNode booleanHelperPopParameter;
								boolean objectNeedCast = false;
								if (types[i] == Type.BOOLEAN_TYPE) {
									booleanHelperPopParameter = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "popParameterBooleanFromInt",
									        Type.getMethodDescriptor(types[i], new Type[] {}));
								} else if (types[i] == Type.CHAR_TYPE) {
									booleanHelperPopParameter = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "popParameterChar",
									        Type.getMethodDescriptor(types[i], new Type[] {}));
								} else if (types[i] == Type.BYTE_TYPE) {
									booleanHelperPopParameter = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "popParameterByte",
									        Type.getMethodDescriptor(types[i], new Type[] {}));
								} else if (types[i] == Type.SHORT_TYPE) {
									booleanHelperPopParameter = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "popParameterShort",
									        Type.getMethodDescriptor(types[i], new Type[] {}));
								} else if (types[i] == Type.INT_TYPE) {
									booleanHelperPopParameter = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "popParameterInt",
									        Type.getMethodDescriptor(types[i], new Type[] {}));
								} else if (types[i] == Type.FLOAT_TYPE) {
									booleanHelperPopParameter = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "popParameterFloat",
									        Type.getMethodDescriptor(types[i], new Type[] {}));
								} else if (types[i] == Type.LONG_TYPE) {
									booleanHelperPopParameter = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "popParameterLong",
									        Type.getMethodDescriptor(types[i], new Type[] {}));
								} else if (types[i] == Type.DOUBLE_TYPE) {
									booleanHelperPopParameter = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "popParameterDouble",
									        Type.getMethodDescriptor(types[i], new Type[] {}));
								} else {
									objectNeedCast = true;
									booleanHelperPopParameter = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "popParameterObject",
									        Type.getMethodDescriptor(Type.getType(Object.class),
									                                 new Type[] {}));
								}

								insnlist.add(booleanHelperPopParameter);
								if (objectNeedCast) {
									TypeInsnNode tin = new TypeInsnNode(Opcodes.CHECKCAST,
									        types[i].getInternalName());
									insnlist.add(tin);
								}
								if (types[i].getDescriptor().equals("Z")
								        || types[i].getDescriptor().equals(Type.getDescriptor(Boolean.class))) {
									MethodInsnNode booleanHelperCast = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "RevertIntToBoolean",
									        Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
									                                 new Type[] { Type.INT_TYPE }));
									insnlist.add(booleanHelperCast);
								}
							}

						}
						mn.instructions.insertBefore(node, insnlist);
					}
				}

			} catch (AnalyzerException e) {
				logger.error("Analysis failed: " + e);
			}
			*/
		}
	}

	private void transformInstanceOf(MethodNode mn) {
		logger.info("Transforming calls");

		AbstractInsnNode node = mn.instructions.getFirst();
		while (node != mn.instructions.getLast()) {
			AbstractInsnNode next = node.getNext();

			if (node instanceof TypeInsnNode) {
				// TODO: This doesn't belong in here
				TypeInsnNode tn = (TypeInsnNode) node;
				if (tn.getOpcode() == Opcodes.INSTANCEOF) {
					LdcInsnNode lin = new LdcInsnNode(Type.getType("L" + tn.desc + ";"));
					mn.instructions.insertBefore(node, lin);
					MethodInsnNode n = new MethodInsnNode(Opcodes.INVOKESTATIC,
					        Type.getInternalName(TestabilityTransformation.class),
					        "instanceOf",
					        Type.getMethodDescriptor(Type.INT_TYPE,
					                                 new Type[] {
					                                         Type.getType(Object.class),
					                                         Type.getType(Class.class) }));
					mn.instructions.insertBefore(node, n);
					if (next instanceof JumpInsnNode) {
						flagNodes.add((JumpInsnNode) next);
					}
					mn.instructions.remove(node);
				}
			}
			node = next;
		}
	}

	private void transformCalls(MethodNode mn) {
		logger.info("Transforming calls");

		AbstractInsnNode node = mn.instructions.getFirst();
		while (node != mn.instructions.getLast()) {
			AbstractInsnNode next = node.getNext();
			if (node instanceof FieldInsnNode) {
				transformFieldInsn((FieldInsnNode) node, mn);
			} else if (node instanceof MethodInsnNode) {
				transformMethodInsn((MethodInsnNode) node, mn);
			}
			node = next;
		}
	}

	private void transformBooleanPredicates(MethodNode mn) {
		logger.info("Transforming predicates");

		AbstractInsnNode node = mn.instructions.getFirst();
		while (node != mn.instructions.getLast()) {
			if (node instanceof JumpInsnNode) {
				JumpInsnNode jn = (JumpInsnNode) node;
				switch (jn.getOpcode()) {
				// TODO: Only insert this push if we are comparing ints? -> Not for flag usage! 
				case Opcodes.IFEQ:
				case Opcodes.IFNE:
				case Opcodes.IFLT:
				case Opcodes.IFGE:
				case Opcodes.IFGT:
				case Opcodes.IFLE:
					//if (!flagNodes.contains(jn))
					insertPush(jn.getOpcode(), node, mn.instructions);
					break;
				case Opcodes.IF_ICMPEQ:
				case Opcodes.IF_ICMPNE:
				case Opcodes.IF_ICMPLT:
				case Opcodes.IF_ICMPGE:
				case Opcodes.IF_ICMPGT:
				case Opcodes.IF_ICMPLE:
					insertPush2(jn.getOpcode(), node, mn.instructions);
					break;
				case Opcodes.IFNULL:
				case Opcodes.IFNONNULL:
					insertPushNull(jn.getOpcode(), node, mn.instructions);
					break;
				case Opcodes.IF_ACMPEQ:
				case Opcodes.IF_ACMPNE:
					insertPushEquals(jn.getOpcode(), node, mn.instructions);
					break;
				default:
					// IF_ACMPEQ, IF_ACMPNE, GOTO, JSR, IFNULL or IFNONNULL.
					// Do nothing
				}
			}
			node = node.getNext();
		}
	}

	private void transformBooleanAssignments(MethodNode mn) {
		logger.info("Transforming boolean assignments");

		AbstractInsnNode node = mn.instructions.getFirst();
		while (node != mn.instructions.getLast()) {

			if (node instanceof InsnNode) {
				// TODO: Only transform if this is a proper flag assignment
				// -> Which is either an ISTORE, a field assignment, or a return
				InsnNode in = (InsnNode) node;
				if (in.getOpcode() == Opcodes.ICONST_0 && isBooleanAssignment(node, mn)) {
					insertGet(node, mn.instructions);
				} else if (in.getOpcode() == Opcodes.ICONST_1
				        && isBooleanAssignment(node, mn)) {
					insertGet(node, mn.instructions);
				}
			} else if (node instanceof VarInsnNode) {
				// Special case for implicit else branch
				VarInsnNode vn1 = (VarInsnNode) node;
				if (isBooleanVariable(vn1.var, mn)
				        && node.getNext() instanceof VarInsnNode) {
					VarInsnNode vn2 = (VarInsnNode) node.getNext();
					if (vn1.var == vn2.var) {
						insertGet(node, mn.instructions);
					}
				}
			} else if (node instanceof FieldInsnNode) {
				// This handles the else branch for field assignments
				FieldInsnNode fn1 = (FieldInsnNode) node;
				if (mapping.isTransformedOrBooleanField(fn1.owner, fn1.name, fn1.desc)
				        && node.getNext() instanceof FieldInsnNode) {
					FieldInsnNode fn2 = (FieldInsnNode) node.getNext();
					if (fn1.owner.equals(fn2.owner) && fn1.name.equals(fn2.name)
					        && fn1.desc.equals(fn2.desc)) {
						if (fn1.getOpcode() == Opcodes.GETFIELD
						        && fn2.getOpcode() == Opcodes.PUTFIELD)
							insertGet(node, mn.instructions);
						else if (fn1.getOpcode() == Opcodes.GETSTATIC
						        && fn2.getOpcode() == Opcodes.PUTSTATIC)
							insertGet(node, mn.instructions);
					}
				}
			}

			node = node.getNext();
		}
	}

	// TODO: Do we need to transform the IF expression after this (probably not)
	private void transformComparisons(MethodNode mn) {
		logger.info("Transforming comparisons");

		// Transform IFNE / IFEQ following transformed booleans

		AbstractInsnNode node = mn.instructions.getFirst();
		while (node != mn.instructions.getLast()) {
			AbstractInsnNode next = node.getNext();
			if (node instanceof InsnNode) {
				InsnNode in = (InsnNode) node;
				if (in.getOpcode() == Opcodes.LCMP) {
					insertLongComparison(in, mn.instructions);
				} else if (in.getOpcode() == Opcodes.DCMPG) {
					insertDoubleComparison(in, mn.instructions);
				} else if (in.getOpcode() == Opcodes.DCMPL) {
					insertDoubleComparison(in, mn.instructions);
				} else if (in.getOpcode() == Opcodes.FCMPG) {
					insertFloatComparison(in, mn.instructions);
				} else if (in.getOpcode() == Opcodes.FCMPL) {
					insertFloatComparison(in, mn.instructions);
				}
			}
			node = next;
		}
	}

	/**
	 * Replace boolean-returning method calls on String classes
	 * 
	 * @param mn
	 */
	private void transformStrings(MethodNode mn) {
		ListIterator<AbstractInsnNode> iterator = mn.instructions.iterator();
		while (iterator.hasNext()) {
			AbstractInsnNode node = iterator.next();
			if (node instanceof MethodInsnNode) {
				MethodInsnNode min = (MethodInsnNode) node;
				if (min.owner.equals("java/lang/String")) {
					if (min.name.equals("equals")) {
						MethodInsnNode equalCheck = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(TestabilityTransformation.class),
						        "StringEquals",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] {
						                                         Type.getType(String.class),
						                                         Type.getType(String.class) }));
						mn.instructions.insertBefore(node, equalCheck);
						mn.instructions.remove(node);

					} else if (min.name.equals("equalsIgnoreCase")) {
						MethodInsnNode equalCheck = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(TestabilityTransformation.class),
						        "StringEqualsIgnoreCase",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] {
						                                         Type.getType(String.class),
						                                         Type.getType(String.class) }));
						mn.instructions.insertBefore(node, equalCheck);
						mn.instructions.remove(node);

					} else if (min.name.equals("startsWith")) {
						if (min.desc.equals("(Ljava/lang/String;I)Z")) {
							mn.instructions.insertBefore(node, new InsnNode(
							        Opcodes.ICONST_0));
						}
						MethodInsnNode equalCheck = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(TestabilityTransformation.class),
						        "StringStartsWith",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] {
						                                         Type.getType(String.class),
						                                         Type.getType(String.class),
						                                         Type.INT_TYPE }));
						mn.instructions.insertBefore(node, equalCheck);
						mn.instructions.remove(node);

					} else if (min.name.equals("endsWith")) {
						MethodInsnNode equalCheck = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(TestabilityTransformation.class),
						        "StringEndsWith",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] {
						                                         Type.getType(String.class),
						                                         Type.getType(String.class) }));
						mn.instructions.insertBefore(node, equalCheck);
						mn.instructions.remove(node);

					} else if (min.name.equals("isEmpty")) {
						MethodInsnNode equalCheck = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(TestabilityTransformation.class),
						        "StringIsEmpty",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] { Type.getType(String.class) }));
						mn.instructions.insertBefore(node, equalCheck);
						mn.instructions.remove(node);

					} else if (min.name.equals("regionMatches")) {
						// TODO
					}

				}
			}
		}
	}

	private void transformImplicitElse(MethodNode mn) {
		AbstractInsnNode node = mn.instructions.getFirst();

		// TODO: we might also store this in a field (PUTSTATIC / ALOAD+PUTFIELD)
		node = NodeRegularExpression.STOREFLAG.getNextMatch(node, mn.instructions);

		while (node != null && node != mn.instructions.getLast()) {
			// Normally: ICONST_? -> ISTORE -> Label ICONST_? -> ISTORE
			// Implicit else: ICONST_? -> ISTORE -> Label -> Not ICONST_?
			logger.info("Found potential un-expanded else: ");
			logger.info("Match starting with opcode " + node.getOpcode()
			        + " at position " + mn.instructions.indexOf(node));
			logger.info("Regex starting with opcode "
			        + NodeRegularExpression.STOREFLAG.pattern[0][0]);

			JumpInsnNode n = (JumpInsnNode) node;
			LabelNode target = n.label;
			AbstractInsnNode m = node.getNext();
			int num = -1;
			while (m != target && m != mn.instructions.getLast()) {
				if (m instanceof VarInsnNode) {
					VarInsnNode vn = (VarInsnNode) m;
					LocalVariableNode lvn = getVariable(vn.var, mn);
					if (lvn != null
					        && mn.instructions.indexOf(lvn.end) > mn.instructions.indexOf(target)
					        && lvn.desc.equals("Z")) {
						num = vn.var;
					}
				}
				m = m.getNext();
			}
			if (num >= 0) {
				logger.info("Found variable to store: " + num);
				LabelNode l = new LabelNode();
				mn.instructions.insertBefore(target, new JumpInsnNode(Opcodes.GOTO, l));
				mn.instructions.insert(target, l);
				mn.instructions.insert(target, new VarInsnNode(Opcodes.ISTORE, num));
				mn.instructions.insert(target, new VarInsnNode(Opcodes.ILOAD, num));
			}

			// Find label node
			// After label, add new ILOAD_2 ISTORE_2
			if (m != mn.instructions.getLast())
				node = NodeRegularExpression.STOREFLAG.getNextMatch(m.getNext(),
				                                                    mn.instructions);
			else
				node = mn.instructions.getLast();
		}
		logger.info("Done with else");
	}

	private void transformImplicitElseField(MethodNode mn) {
		AbstractInsnNode node = mn.instructions.getFirst();

		// TODO: we might also store this in a field (PUTSTATIC / ALOAD+PUTFIELD)
		node = NodeRegularExpression.STOREFLAG3.getNextMatch(node, mn.instructions);
		logger.info("Looking for unexpanded else with field");

		while (node != null && node != mn.instructions.getLast()) {
			// Normally: ICONST_? -> ISTORE -> Label ICONST_? -> ISTORE
			// Implicit else: ICONST_? -> ISTORE -> Label -> Not ICONST_?
			logger.info("Found potential un-expanded else with field: ");
			logger.info("Match starting with opcode " + node.getOpcode());
			logger.info("Regex starting with opcode "
			        + NodeRegularExpression.STOREFLAG3.pattern[0][0]);

			// IFEQ L1
			// ICONST_1
			// ISTORE 1
			// L1 
			// ...

			JumpInsnNode n = (JumpInsnNode) node;
			LabelNode target = n.label;
			if (target.getPrevious() instanceof FieldInsnNode) {
				FieldInsnNode fn = (FieldInsnNode) target.getPrevious();
				VarInsnNode vn = (VarInsnNode) fn.getPrevious().getPrevious();
				AbstractInsnNode m = node.getNext();
				logger.info("Found variable to store: ");
				LabelNode l = new LabelNode();
				mn.instructions.insertBefore(target, new JumpInsnNode(Opcodes.GOTO, l));
				mn.instructions.insert(target, l);
				mn.instructions.insert(target, new FieldInsnNode(Opcodes.PUTFIELD,
				        fn.owner, fn.name, fn.desc));
				mn.instructions.insert(target, new FieldInsnNode(Opcodes.GETFIELD,
				        fn.owner, fn.name, fn.desc));
				mn.instructions.insert(target, new VarInsnNode(vn.getOpcode(), vn.var));
				mn.instructions.insert(target, new VarInsnNode(vn.getOpcode(), vn.var));
			}
			// Find label node
			// After label, add new ILOAD_2 ISTORE_2
			node = NodeRegularExpression.STOREFLAG3.getNextMatch(node.getNext(),
			                                                     mn.instructions);
		}
	}

	private void transformImplicitElseStaticField(MethodNode mn) {
		AbstractInsnNode node = mn.instructions.getFirst();

		// TODO: we might also store this in a field (PUTSTATIC / ALOAD+PUTFIELD)
		node = NodeRegularExpression.STOREFLAG2.getNextMatch(node, mn.instructions);

		while (node != null && node != mn.instructions.getLast()) {
			// Normally: ICONST_? -> ISTORE -> Label ICONST_? -> ISTORE
			// Implicit else: ICONST_? -> ISTORE -> Label -> Not ICONST_?
			logger.info("Found potential un-expanded else: ");
			logger.info("Match starting with opcode " + node.getOpcode());
			logger.info("Regex starting with opcode "
			        + NodeRegularExpression.STOREFLAG2.pattern[0][0]);

			// IFEQ L1
			// ICONST_1
			// ISTORE 1
			// L1 
			// ...

			JumpInsnNode n = (JumpInsnNode) node;
			LabelNode target = n.label;
			FieldInsnNode fn = (FieldInsnNode) target.getPrevious();
			AbstractInsnNode m = node.getNext();
			int num = -1;
			while (m != target) {
				if (m instanceof VarInsnNode) {
					VarInsnNode vn = (VarInsnNode) m;
					num = vn.var;
				}
				m = m.getNext();
			}
			if (num >= 0) {
				logger.info("Found variable to store: " + num);
				LabelNode l = new LabelNode();
				mn.instructions.insertBefore(target, new JumpInsnNode(Opcodes.GOTO, l));
				mn.instructions.insert(target, l);
				mn.instructions.insert(target, new FieldInsnNode(Opcodes.PUTSTATIC,
				        fn.owner, fn.name, fn.desc));
				mn.instructions.insert(target, new FieldInsnNode(Opcodes.GETSTATIC,
				        fn.owner, fn.name, fn.desc));
			}

			// Find label node
			// After label, add new ILOAD_2 ISTORE_2
			node = NodeRegularExpression.STOREFLAG2.getNextMatch(m.getNext(),
			                                                     mn.instructions);
		}
	}

	private void transformBooleanIf(JumpInsnNode node, MethodNode mn) {
		if (node.getOpcode() == Opcodes.IFNE) {
			logger.info("Changing IFNE");
			node.setOpcode(Opcodes.IFGT);
			flagNodes.add(node);
		} else if (node.getOpcode() == Opcodes.IFEQ) {
			logger.info("Changing IFEQ");
			node.setOpcode(Opcodes.IFLE);
			flagNodes.add(node);
		}
	}

	private JumpInsnNode isFollowedByJump(AbstractInsnNode node, MethodNode mn) {
		if (node == mn.instructions.getLast())
			return null;

		AbstractInsnNode next = node.getNext();
		while (next != mn.instructions.getLast()
		        && (next instanceof FrameNode || next instanceof LabelNode || next.getOpcode() < 0)) {
			next = next.getNext();
		}
		if (next instanceof JumpInsnNode)
			return (JumpInsnNode) next;
		else
			return null;
	}

	private void transformFlagUsage(MethodNode mn) {
		AbstractInsnNode node = mn.instructions.getFirst();

		while (node != mn.instructions.getLast()) {
			JumpInsnNode jump = isFollowedByJump(node, mn);
			if (jump != null) {
				if (node instanceof MethodInsnNode) {
					logger.info("Found IFELSE");
					MethodInsnNode me = (MethodInsnNode) node;
					if (me.name.equals("instanceOf")
					        || mapping.isTransformedOrBooleanReturnMethod(me.owner,
					                                                      me.name,
					                                                      me.desc)) {
						transformBooleanIf(jump, mn);
					}
				} else if (node instanceof FieldInsnNode) {
					logger.info("Found IFELSE");
					FieldInsnNode fn = (FieldInsnNode) node;
					if ((fn.getOpcode() == Opcodes.GETFIELD || fn.getOpcode() == Opcodes.GETSTATIC)
					        && mapping.isTransformedOrBooleanField(fn.owner, fn.name,
					                                               fn.desc)) {
						transformBooleanIf(jump, mn);
					}
				} else if (node instanceof VarInsnNode) {
					logger.info("Found IFELSE");
					// TODO: Check this is an iload
					VarInsnNode vn = (VarInsnNode) node;
					if (isBooleanVariable(vn.var, mn)) {
						transformBooleanIf(jump, mn);
					}

				}
			}
			node = node.getNext();
		}
	}

	private Frame[] getArrayFrames(Frame[] frames, MethodNode mn) {
		if (frames != null)
			return frames;

		try {
			Analyzer a = new Analyzer(new BooleanArrayInterpreter());
			a.analyze(cn.name, mn);
			return a.getFrames();
		} catch (Exception e) {
			logger.warn("Error during analysis: " + e);
			return null;
		}
	}

	private void transformArrays(MethodNode mn) {
		AbstractInsnNode node = mn.instructions.getFirst();

		while (node != mn.instructions.getLast()) {
			AbstractInsnNode next = node.getNext();

			// Array definition
			if (node.getOpcode() == Opcodes.NEWARRAY) {
				IntInsnNode in = (IntInsnNode) node;
				if (in.operand == Opcodes.T_BOOLEAN) {
					in.operand = Opcodes.T_INT;
				}
			} else if (node.getOpcode() == Opcodes.MULTIANEWARRAY) {
				MultiANewArrayInsnNode manai = (MultiANewArrayInsnNode) node;
				logger.info("Replacing MULTIANEWARRAY bools from " + manai.desc);
				String new_desc = "";
				Type t = Type.getType(manai.desc);
				while (t.equals(Type.ARRAY)) {
					new_desc += "[";
					t = t.getElementType();
				}
				if (t.equals(Type.BOOLEAN_TYPE))
					new_desc += "I";
				else
					new_desc += t.getDescriptor();
				manai.desc = new_desc;
			} else if (node.getOpcode() == Opcodes.ANEWARRAY) {
				TypeInsnNode tn = (TypeInsnNode) node;
				logger.info("Replacing ANEWARRAY bools from " + tn.desc);
				String new_desc = "";
				int pos = 0;
				while (pos < tn.desc.length() && tn.desc.charAt(pos) == '[') {
					new_desc += "[";
					pos++;
				}
				String d = tn.desc.substring(pos);
				logger.info("Unfolded arrays to: " + d);
				if (d.equals("Z"))
					//if (t.equals(Type.BOOLEAN_TYPE))
					new_desc += "I";
				else
					new_desc += d; //t.getInternalName();
				tn.desc = new_desc;
				logger.info("Replacing ANEWARRAY bools to " + tn.desc);
			}
			node = next;
		}

		boolean changed = true;
		while (changed) {
			changed = false;

			node = mn.instructions.getFirst();
			Frame[] frames = getArrayFrames(null, mn);
			while (node != mn.instructions.getLast()) {
				AbstractInsnNode next = node.getNext();

				// Array access
				if (node.getOpcode() == Opcodes.BALOAD) {
					//frames = getArrayFrames(frames, mn);
					if (frames == null) {
						logger.info("Setting BALOAD to IALOAD because frame is null");
						mn.instructions.insertBefore(node, new InsnNode(Opcodes.IALOAD));
						mn.instructions.remove(node);
						changed = true;

					} else {
						Frame current = frames[mn.instructions.indexOf(node)];
						int size = current.getStackSize();
						if (current.getStack(size - 2) == BooleanArrayInterpreter.INT_ARRAY) {
							logger.info("Array is of boolean type, changing BALOAD to IALOAD");
							mn.instructions.insertBefore(node, new InsnNode(
							        Opcodes.IALOAD));
							mn.instructions.remove(node);
							changed = true;

						} else {
							logger.info("XXX Stack has size " + size
							        + ", expecting array at " + (size - 2));
							if (current.getStack(size - 2) == BooleanArrayInterpreter.BYTE_ARRAY)
								logger.info("Array is of byte type ");
							else if (current.getStack(size - 2) == BooleanArrayInterpreter.INT_ARRAY)
								logger.info("Array is of int type ");
							else if (current.getStack(size - 2) == BooleanArrayInterpreter.BOOLEAN_ARRAY)
								logger.info("Array is of boolean type ");
							else
								logger.info("Array is of other type ");
						}
					}
				} else if (node.getOpcode() == Opcodes.BASTORE) {
					//frames = getArrayFrames(frames, mn);
					if (frames == null) {
						logger.info("Setting BASTORE to IASTORE because frame is null");
						mn.instructions.insertBefore(node, new InsnNode(Opcodes.IASTORE));
						mn.instructions.remove(node);
						changed = true;

					} else {
						Frame current = frames[mn.instructions.indexOf(node)];
						int size = current.getStackSize();
						if (current.getStack(size - 3) == BooleanArrayInterpreter.INT_ARRAY) {
							logger.info("Array is of boolean type, changing BASTORE to IASTORE");
							mn.instructions.insertBefore(node, new InsnNode(
							        Opcodes.IASTORE));
							mn.instructions.remove(node);
							changed = true;

						} else {
							logger.info("Stack has size " + size
							        + ", expecting array at " + (size - 3));
							if (current.getStack(size - 3) == BooleanArrayInterpreter.BYTE_ARRAY)
								logger.info("Array is of byte type ");
							else if (current.getStack(size - 3) == BooleanArrayInterpreter.INT_ARRAY)
								logger.info("Array is of int type ");
							else if (current.getStack(size - 3) == BooleanArrayInterpreter.BOOLEAN_ARRAY)
								logger.info("Array is of boolean type ");
							else
								logger.info("Array is of other type ");
						}
					}
				} else if (node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
					MethodInsnNode min = (MethodInsnNode) node;
					if (min.owner.equals("[Z")) {
						if (frames == null) {
							mn.instructions.insertBefore(node, new MethodInsnNode(
							        Opcodes.INVOKEVIRTUAL, "[I", min.name, min.desc));
							mn.instructions.remove(node);
							changed = true;

						} else {
							Frame current = frames[mn.instructions.indexOf(node)];
							int size = current.getStackSize();
							if (current.getStack(size - 1) == BooleanArrayInterpreter.INT_ARRAY) {
								logger.info("Array is of boolean type, changing INVOKEVIRTUAL to [I");
								mn.instructions.insertBefore(node, new MethodInsnNode(
								        Opcodes.INVOKEVIRTUAL, "[I", min.name, min.desc));
								mn.instructions.remove(node);
								changed = true;

							}
						}

					}
				} else if (node.getOpcode() == Opcodes.CHECKCAST) {
					TypeInsnNode tin = (TypeInsnNode) node;
					if (tin.desc.equals("[Z")) {
						logger.info("YYY found cast to [Z");
						if (frames == null) {
							mn.instructions.insertBefore(node, new TypeInsnNode(
							        Opcodes.CHECKCAST, "[I"));
							mn.instructions.remove(node);
							changed = true;

						} else {
							Frame current = frames[mn.instructions.indexOf(node)];
							int size = current.getStackSize();
							if (current.getStack(size - 1) == BooleanArrayInterpreter.INT_ARRAY) {
								logger.info("Array is of boolean type, changing CHECKCAST to [I");
								mn.instructions.insertBefore(node, new TypeInsnNode(
								        Opcodes.CHECKCAST, "[I"));
								mn.instructions.remove(node);
								changed = true;
							} else {
								if (current.getStack(size - 1) == BooleanArrayInterpreter.BYTE_ARRAY)
									logger.info("Array is of byte type ");
								else if (current.getStack(size - 1) == BooleanArrayInterpreter.INT_ARRAY)
									logger.info("Array is of int type ");
								else if (current.getStack(size - 1) == BooleanArrayInterpreter.BOOLEAN_ARRAY)
									logger.info("Array is of boolean type ");
								else
									logger.info("Array is of other type ");
							}
						}
					}
				}
				node = next;
			}
		}

	}

	private void transformBooleanReturns(MethodNode mn) {
		Type type = Type.getReturnType(mapping.getMethodDesc(cn.name, mn.name, mn.desc));
		if (type.equals(Type.BOOLEAN_TYPE)) {
			ListIterator<AbstractInsnNode> iterator = mn.instructions.iterator();
			while (iterator.hasNext()) {
				AbstractInsnNode node = iterator.next();
				if (node.getOpcode() == Opcodes.IRETURN) {
					MethodInsnNode n = new MethodInsnNode(Opcodes.INVOKESTATIC,
					        Type.getInternalName(TestabilityTransformation.class),
					        "intToBoolean",
					        Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
					                                 new Type[] { Type.INT_TYPE }));
					mn.instructions.insertBefore(node, n);

				}
			}
		} else if (mapping.isTransformedOrBooleanReturnMethod(cn.name, mn.name, mn.desc)) {
			// If it is a transformed method
			// Before each return of ICONST_0/ICONST_1, add
			try {
				Analyzer a = new Analyzer(new BooleanInterpreter());
				a.analyze(cn.name, mn);
				Frame[] frames = a.getFrames();

				ListIterator<AbstractInsnNode> iterator = mn.instructions.iterator();
				while (iterator.hasNext()) {
					AbstractInsnNode node = iterator.next();
					if (node.getOpcode() == Opcodes.IRETURN) {
						logger.info("CHECKING IRETURN");
						Frame current = frames[mn.instructions.indexOf(node)];
						int size = current.getStackSize();
						if (current.getStack(size - 1) == BooleanInterpreter.BOOLEAN) {
							logger.info("IS BOOLEAN!");

						} else {
							if (current.getStack(size - 1) == BasicValue.INT_VALUE)
								logger.info("Stack value is an int");
							else if (current.getStack(size - 1) == BasicValue.RETURNADDRESS_VALUE)
								logger.info("Stack value is return address");
							else
								logger.info("Stack value is of other type ");
						}
					}
				}

			} catch (Exception e) {
				logger.warn("Error during analysis: " + e);
			}
		}
	}

	private void transformMethod(MethodNode mn) {
		logger.info("Transforming method " + mn.name + mn.desc);
		MethodInsnNode reset = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(TestabilityTransformation.class), "clearPredicates",
		        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] {}));

		// First, reset the stack of distance values
		// TODO: Need to find a better strategy for this
		// TODO: Only insert this if there is a call to push/get in the method
		//if (mn.instructions.getFirst() != null && !mn.name.startsWith("<")
		//       && !mn.name.equals("__STATIC_RESET"))
		//	mn.instructions.insertBefore(mn.instructions.getFirst(), reset);

		if (Properties.TRANSFORM_ELSE) {
			logger.info("Transforming ELSE");
			// Now unfold implicit else branches
			transformImplicitElse(mn);
			transformImplicitElseField(mn);
			transformImplicitElseStaticField(mn);
		}

		if (Properties.TRANSFORM_COMPARISON) {
			// Change comparisons of non-int values to distance functions
			transformComparisons(mn);
		}

		if (Properties.TRANSFORM_BOOLEAN) {

			if (Properties.TRANSFORM_STRING) {
				transformStrings(mn);
			}

			// Remove flag definitions
			transformBooleanAssignments(mn);

			transformInstanceOf(mn);

			// Insert distance function between Boolean variable and jump
			transformFlagUsage(mn);

			// Change IFNE/IFEQ for flags
			transformBooleanPredicates(mn);

			// Change signatures of fields and methods with Booleans
			transformCalls(mn);

			transformBooleanReturns(mn);

			// Convert information about local variables (only needed for debugging really)
			transformLocalVariables(mn);

			// Convert boolean arrays to integer arrays
			transformArrays(mn);
		}

	}

	public static int instanceOf(Object o, Class<?> c) {
		if (o == null)
			return -K;
		//logger.info("Checking whether " + o.getClass().getName() + " can be assigned to "
		//        + c.getName());
		if (c.isAssignableFrom(o.getClass())) {
			//logger.info("Yes");
			return K;
		} else {
			//logger.info("No");
			return -K;
		}
	}

	public static int isNull(Object o, int opcode) {
		if (opcode == Opcodes.IFNULL)
			return o == null ? K : -K;
		else
			return o != null ? K : -K;
	}

	public static int isEqual(Object o1, Object o2, int opcode) {
		if (opcode == Opcodes.IF_ACMPEQ)
			return o1 == o2 ? K : -K;
		else
			return o1 != o2 ? K : -K;
	}

	public static void clearPredicates() {
		distanceStack.clear();
	}

	public static void methodEntered() {
		if (distanceStack != null)
			stackStack.push(distanceStack);
		distanceStack = new Stack<Integer>();
	}

	public static void methodLeft() {
		if (!stackStack.isEmpty())
			distanceStack = stackStack.pop();
	}

	public static void pushPredicate(int distance) {
		//logger.debug("Push: " + distance);
		distanceStack.push(Math.abs(distance));
	}

	private static double normalize(int distance) {
		//		double k = K;
		double k = Properties.getPropertyOrDefault("max_int", K);
		double d = distance;
		return d / (d + 0.5 * k);
		//return distance / (distance + 1.0);
	}

	public static int getDistance(int original) {
		int l = distanceStack.size();
		int distance = K;
		if (!distanceStack.isEmpty())
			distance = distanceStack.peek();
		distanceStack.clear();
		/*
				if (l <= 1) {
					//distance += K;
					if (original <= 0)
						distance = -distance;
					logger.debug("Distance (2)" + distance);
					return distance;
				}
		*/
		double val = (1.0 + normalize(distance)) / Math.pow(2.0, l);

		int d = (int) Math.ceil(K * val);
		//if (d == 0 && val != 0.0)
		//	d = 1; // TODO: This is a problem if the number of pushes is too big
		if (original <= 0)
			d = -d;

		logger.debug("Distance: " + d);

		return d;
	}

	public static int fromDouble(double d) {
		//logger.info("Converting double " + d);
		/*
		if (d > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		else if (d < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		else 
		*/
		if (d == 0.0)
			return 0;
		else {
			double d2 = Math.signum(d) * Math.abs(d) / (1.0 + Math.abs(d));
			//logger.info(" -> " + d2);
			int d3 = (int) Math.round(Integer.MAX_VALUE * d2);
			//logger.info(" -> " + d3);
			return d3;
		}
	}

	public static int fromFloat(float d) {
		//logger.info("Converting float " + d);
		/*
		if (d > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		else if (d < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		else */
		if (d == 0.0f)
			return 0;
		else {
			float d2 = Math.signum(d) * Math.abs(d) / (1f + Math.abs(d));
			//logger.info(" ->" + d2);
			int d3 = Math.round(Integer.MAX_VALUE * d2);
			//logger.info(" -> " + d3);
			return d3;
		}
	}

	public static int fromLong(long d) {
		/*
		if (d > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		else if (d < Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
			*/
		//else
		//	return (int) d;
		double d2 = Math.signum(d) * Math.abs(d) / (1L + Math.abs(d));
		int d3 = (int) Math.round(Integer.MAX_VALUE * d2);
		return d3;
	}

	public static int booleanToInt(boolean b) {
		if (b)
			return K;
		else
			return -K;
	}

	public static boolean intToBoolean(int x) {
		return x > 0;
	}

	public static int min(int a, int b, int c) {
		if (a < b)
			return Math.min(a, c);
		else
			return Math.min(b, c);
	}

	public static int editDistance(String s, String t) {
		int d[][]; // matrix
		int n; // length of s
		int m; // length of t
		int i; // iterates through s
		int j; // iterates through t
		char s_i; // ith character of s
		char t_j; // jth character of t
		int cost; // cost

		int k = 127;

		// Step 1

		n = s.length();
		m = t.length();
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		d = new int[n + 1][m + 1];

		// Step 2

		for (i = 0; i <= n; i++) {
			d[i][0] = i;
		}

		for (j = 0; j <= m; j++) {
			d[0][j] = j;
		}

		// Step 3

		for (i = 1; i <= n; i++) {

			s_i = s.charAt(i - 1);

			// Step 4

			for (j = 1; j <= m; j++) {

				t_j = t.charAt(j - 1);

				// Step 5

				if (s_i == t_j) {
					cost = 0;
				} else {
					//					cost = 127/4 + 3 * Math.abs(s_i - t_j)/4;
					cost = 127;
				}

				// Step 6

				d[i][j] = min(d[i - 1][j] + k, d[i][j - 1] + k, d[i - 1][j - 1] + cost);

			}

		}

		// Step 7

		return d[n][m];
	}

	public static int StringEquals(String first, String second) {
		if (first.equals(second))
			return K; // Identical
		else {
			return -editDistance(first, second);
		}
	}

	public static int StringEqualsIgnoreCase(String first, String second) {
		return StringEquals(first.toLowerCase(), second.toLowerCase());
	}

	public static int StringStartsWith(String value, String prefix, int start) {
		int len = Math.min(prefix.length(), value.length());
		return StringEquals(value.substring(start, len), prefix);
	}

	public static int StringEndsWith(String value, String suffix) {
		int len = Math.min(suffix.length(), value.length());
		String other = value.substring(value.length() - len);
		if (other.length() != suffix.length())
			logger.error("Error in string comparison - should subtract - 1");
		return StringEquals(other, suffix); // TODO: -1?
	}

	public static int StringIsEmpty(String value) {
		int len = value.length();
		if (len == 0)
			return K;
		else
			return -len;
	}

	public static int StringRegionMatches(String value, int thisStart, String string,
	        int start, int length, boolean ignoreCase) {
		if (value == null || string == null)
			throw new NullPointerException();

		if (start < 0 || string.length() - start < length) {
			return -K;
		}

		if (thisStart < 0 || value.length() - thisStart < length) {
			return -K;
		}
		if (length <= 0) {
			return K;
		}

		String s1 = value;
		String s2 = string;
		if (ignoreCase) {
			s1 = s1.toLowerCase();
			s2 = s2.toLowerCase();
		}

		return StringEquals(s1.substring(thisStart, length), s2.substring(start, length));
	}
}
