/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * @author Gordon Fraser
 * 
 */
// TODO: If we transform a method and there already is a method with the transformed descriptor, we need to change the method name as well!
public class TestabilityTransformation {

	static Logger logger = LoggerFactory.getLogger(TestabilityTransformation.class);

	private final ClassNode cn;

	public static String getTransformedDesc(String className, String methodName,
	        String desc) {
		return mapping.getMethodDesc(className, methodName, desc);
	}

	public static String getTransformedName(String className, String methodName,
	        String desc) {
		return mapping.getMethodName(className, methodName, desc);
	}

	public static String getOriginalNameDesc(String className, String methodName,
	        String desc) {
		String key = className.replace(".", "/") + "." + methodName + desc;
		if (mapping.originalDesc.containsKey(key)) {

			return mapping.getOriginalName(className, methodName, desc)
			        + mapping.originalDesc.get(key);
		} else {
			return methodName + desc;
		}
	}

	private static DescriptorMapping mapping = DescriptorMapping.getInstance();

	private static Set<AbstractInsnNode> flagDefs = new HashSet<AbstractInsnNode>();

	private static Set<JumpInsnNode> flagUses = new HashSet<JumpInsnNode>();

	public TestabilityTransformation(ClassNode cn) {
		this.cn = cn;
	}

	public ClassNode transform() {
		processFields();
		processMethods();
		//for (int i = 0; i < cn.interfaces.size(); i++) {
		//	String n = (String) cn.interfaces.get(i);
		//	cn.interfaces.set(i, n.replaceAll(Matcher.quoteReplacement("java/util"),
		//	                                  "java2/util2"));//
		//
		//		}
		//		cn.superName = cn.superName.replaceAll(Matcher.quoteReplacement("java/util"),
		//		                                       "java2/util2");

		return cn;
	}

	@SuppressWarnings("unchecked")
	private void processFields() {
		List<FieldNode> fieldNodes = cn.fields;

		for (FieldNode fn : fieldNodes) {
			logger.info("Transforming field " + fn.name + " - " + fn.desc);
			fn.desc = mapping.getFieldDesc(cn.name, fn.name, fn.desc);
			//fn.desc = fn.desc.replaceAll(Matcher.quoteReplacement("java/util"),
			//	                             "java2/util2");

			logger.info("Transformed field: " + fn.desc);
		}
	}

	@SuppressWarnings("unchecked")
	private void processMethods() {
		List<MethodNode> methodNodes = cn.methods;
		int count = 0;
		int defs = flagDefs.size();
		for (MethodNode mn : methodNodes) {
			// If this method was defined somewhere outside the test package, do not transform signature
			String desc = mn.desc;
			mn.desc = mapping.getMethodDesc(cn.name, mn.name, mn.desc);
			//mn.desc = mn.desc.replaceAll(Matcher.quoteReplacement("java/util"),
			//                            "java2/util2");
			//
			mn.name = mapping.getMethodName(cn.name, mn.name, desc);
			logger.info("Now going inside " + mn.name + mn.desc);
			// Actually this should be done automatically by the ClassWriter...
			// +2 because we might do a DUP2
			mn.maxStack += 3;

			count += transformMethod(mn);

		}
		System.out.println("Flag definitions found for class " + cn.name + ": "
		        + (flagDefs.size() - defs));
		System.out.println("Flag uses found for class " + cn.name + ": " + count);
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
		        Type.getInternalName(BooleanHelper.class), "isNull",
		        Type.getMethodDescriptor(Type.INT_TYPE,
		                                 new Type[] { Type.getType(Object.class),
		                                         Type.INT_TYPE }));
		list.insertBefore(position, new InsnNode(Opcodes.DUP));
		list.insertBefore(position, new LdcInsnNode(opcode));
		list.insertBefore(position, nullCheck);
		MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "pushPredicate",
		        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE }));
		list.insertBefore(position, push);

	}

	private void insertPushEquals(int opcode, AbstractInsnNode position, InsnList list) {
		MethodInsnNode equalCheck = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "isEqual",
		        Type.getMethodDescriptor(Type.INT_TYPE,
		                                 new Type[] { Type.getType(Object.class),
		                                         Type.getType(Object.class),
		                                         Type.INT_TYPE }));
		list.insertBefore(position, new InsnNode(Opcodes.DUP2));
		list.insertBefore(position, new LdcInsnNode(opcode));
		list.insertBefore(position, equalCheck);
		MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "pushPredicate",
		        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE }));
		list.insertBefore(position, push);

	}

	private void insertPush(int opcode, AbstractInsnNode position, InsnList list) {
		//list.insertBefore(position, new InsnNode(Opcodes.ICONST_0));
		list.insertBefore(position, new InsnNode(Opcodes.DUP));
		//list.insertBefore(position, new InsnNode(Opcodes.SWAP));
		//list.insertBefore(position, new InsnNode(Opcodes.ISUB));
		MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "pushPredicate",
		        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE }));
		list.insertBefore(position, push);

	}

	private void insertPush2(int opcode, AbstractInsnNode position, InsnList list) {
		list.insertBefore(position, new InsnNode(Opcodes.DUP2));
		list.insertBefore(position, new InsnNode(Opcodes.ISUB));
		MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "pushPredicate",
		        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE }));
		list.insertBefore(position, push);

	}

	private void insertGet(AbstractInsnNode position, InsnList list) {
		MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "getDistance",
		        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] { Type.INT_TYPE }));
		list.insert(position, get);
		//list.remove(position);
	}

	private void insertLongComparison(AbstractInsnNode position, InsnList list) {
		list.insertBefore(position, new InsnNode(Opcodes.LSUB));
		MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "fromLong",
		        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] { Type.LONG_TYPE }));
		list.insert(position, get);
		list.remove(position);
	}

	private void insertFloatComparison(AbstractInsnNode position, InsnList list) {
		list.insertBefore(position, new InsnNode(Opcodes.FSUB));
		MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "fromFloat",
		        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] { Type.FLOAT_TYPE }));
		list.insert(position, get);
		list.remove(position);
	}

	private void insertDoubleComparison(AbstractInsnNode position, InsnList list) {
		list.insertBefore(position, new InsnNode(Opcodes.DSUB));
		MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "fromDouble",
		        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] { Type.DOUBLE_TYPE }));
		list.insert(position, get);
		list.remove(position);
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

	@SuppressWarnings("unchecked")
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
				        Type.getInternalName(BooleanHelper.class), "intToBoolean",
				        Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
				                                 new Type[] { Type.INT_TYPE }));
				mn.instructions.insertBefore(node, n);
			} else {
				MethodInsnNode n = new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(BooleanHelper.class), "booleanToInt",
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
					        Type.getInternalName(BooleanHelper.class), "intToBoolean",
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
							        Type.getInternalName(BooleanHelper.class),
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
				        Type.getInternalName(BooleanHelper.class), "booleanToInt",
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
						        Type.getInternalName(BooleanHelper.class),
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
					// Type t = Type.getType("L" + tn.desc + ";");
					logger.info("Class version " + cn.version);
					if (cn.version > 49) {
						LdcInsnNode lin = new LdcInsnNode(Type.getType("L" + tn.desc
						        + ";"));
						mn.instructions.insertBefore(node, lin);
					} else {
						LdcInsnNode lin = new LdcInsnNode(tn.desc.replace("/", "."));
						mn.instructions.insertBefore(node, lin);
						MethodInsnNode n = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(Class.class),
						        "forName",
						        Type.getMethodDescriptor(Type.getType(Class.class),
						                                 new Type[] { Type.getType(String.class) }));
						mn.instructions.insertBefore(node, n);
					}
					MethodInsnNode n = new MethodInsnNode(Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "instanceOf",
					        Type.getMethodDescriptor(Type.INT_TYPE,
					                                 new Type[] {
					                                         Type.getType(Object.class),
					                                         Type.getType(Class.class) }));
					mn.instructions.insertBefore(node, n);
					//if (next instanceof JumpInsnNode) {
					//	flagNodes.add((JumpInsnNode) next);
					//}
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
					flagDefs.add(node);
				} else if (in.getOpcode() == Opcodes.ICONST_1
				        && isBooleanAssignment(node, mn)) {
					insertGet(node, mn.instructions);
					flagDefs.add(node);
				}
			} else if (node instanceof VarInsnNode) {
				// Special case for implicit else branch
				VarInsnNode vn1 = (VarInsnNode) node;
				if (isBooleanVariable(vn1.var, mn)
				        && node.getNext() instanceof VarInsnNode) {
					VarInsnNode vn2 = (VarInsnNode) node.getNext();
					if (vn1.var == vn2.var) {
						insertGet(node, mn.instructions);
						flagDefs.add(node);
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
						        && fn2.getOpcode() == Opcodes.PUTFIELD) {
							insertGet(node, mn.instructions);
							flagDefs.add(node);
						} else if (fn1.getOpcode() == Opcodes.GETSTATIC
						        && fn2.getOpcode() == Opcodes.PUTSTATIC) {
							insertGet(node, mn.instructions);
							flagDefs.add(node);
						}
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
	@SuppressWarnings("unchecked")
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
						        Type.getInternalName(BooleanHelper.class),
						        "StringEquals",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] {
						                                         Type.getType(String.class),
						                                         Type.getType(Object.class) }));
						mn.instructions.insertBefore(node, equalCheck);
						mn.instructions.remove(node);

					} else if (min.name.equals("equalsIgnoreCase")) {
						MethodInsnNode equalCheck = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "StringEqualsIgnoreCase",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] {
						                                         Type.getType(String.class),
						                                         Type.getType(String.class) }));
						mn.instructions.insertBefore(node, equalCheck);
						mn.instructions.remove(node);

					} else if (min.name.equals("startsWith")) {
						if (min.desc.equals("(Ljava/lang/String;)Z")) {
							mn.instructions.insertBefore(node, new InsnNode(
							        Opcodes.ICONST_0));
						}
						MethodInsnNode equalCheck = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
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
						        Type.getInternalName(BooleanHelper.class),
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
						        Type.getInternalName(BooleanHelper.class),
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
				if (!(fn.getPrevious().getPrevious() instanceof VarInsnNode))
					return; // TODO: How is this possible? java.math.MutableBigInteger
				VarInsnNode vn = (VarInsnNode) fn.getPrevious().getPrevious();
				@SuppressWarnings("unused")
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
			if (!(target.getPrevious() instanceof FieldInsnNode))
				return; // How can that be?
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
			flagUses.add(node);
		} else if (node.getOpcode() == Opcodes.IFEQ) {
			logger.info("Changing IFEQ");
			node.setOpcode(Opcodes.IFLE);
			flagUses.add(node);
		}
	}

	private void transformBitwiseOperators(MethodNode mn) {
		AbstractInsnNode node = mn.instructions.getFirst();

		while (node != mn.instructions.getLast()) {
			AbstractInsnNode next = node.getNext();
			if (node instanceof InsnNode) {
				if (isBooleanAssignment(node, mn)) {
					if (node.getOpcode() == Opcodes.IOR) {
						MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class), "IOR",
						        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {
						                Type.INT_TYPE, Type.INT_TYPE }));
						mn.instructions.insertBefore(node, push);
						mn.instructions.remove(node);
					} else if (node.getOpcode() == Opcodes.IAND) {
						MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class), "IAND",
						        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {
						                Type.INT_TYPE, Type.INT_TYPE }));
						mn.instructions.insertBefore(node, push);
						mn.instructions.remove(node);

					} else if (node.getOpcode() == Opcodes.IXOR) {
						MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class), "IXOR",
						        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {
						                Type.INT_TYPE, Type.INT_TYPE }));
						mn.instructions.insertBefore(node, push);
						mn.instructions.remove(node);

					}
				}
			}
			node = next;
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
					} else if (me.owner.equals("de/unisb/cs/st/evosuite/javaagent/BooleanHelper")
					        && me.name.startsWith("String")) {
						transformBooleanIf(jump, mn);
					} else {
						logger.info("This does not look like a transformable if: "
						        + me.owner + "." + me.name);
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

	@SuppressWarnings("unchecked")
	private void transformBooleanReturns(MethodNode mn) {
		Type type = Type.getReturnType(mapping.getMethodDesc(cn.name, mn.name, mn.desc));
		if (type.equals(Type.BOOLEAN_TYPE)) {
			ListIterator<AbstractInsnNode> iterator = mn.instructions.iterator();
			while (iterator.hasNext()) {
				AbstractInsnNode node = iterator.next();
				if (node.getOpcode() == Opcodes.IRETURN) {
					MethodInsnNode n = new MethodInsnNode(Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "intToBoolean",
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

	private int transformMethod(MethodNode mn) {
		logger.info("Transforming method " + mn.name + mn.desc);
		int before = flagUses.size();

		@SuppressWarnings("unused")
		MethodInsnNode reset = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "clearPredicates",
		        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] {}));

		// First, reset the stack of distance values
		// TODO: Need to find a better strategy for this
		// TODO: Only insert this if there is a call to push/get in the method
		//if (mn.instructions.getFirst() != null && !mn.name.startsWith("<")
		//        && !mn.name.equals("__STATIC_RESET"))
		//	mn.instructions.insertBefore(mn.instructions.getFirst(), reset);

		logger.info("Transforming ELSE");
		// Now unfold implicit else branches

		transformImplicitElse(mn);
		transformImplicitElseField(mn);
		transformImplicitElseStaticField(mn);
		// Change comparisons of non-int values to distance functions
		//		transformComparisons(mn); // Done in ComparisonTransformation
		transformStrings(mn);

		// Remove flag definitions
		transformBooleanAssignments(mn);

		transformInstanceOf(mn);

		// Insert distance function between Boolean variable and jump
		transformFlagUsage(mn);

		// Change IFNE/IFEQ for flags
		transformBooleanPredicates(mn);

		// Change signatures of fields and methods with Booleans
		transformCalls(mn);

		transformBitwiseOperators(mn);

		transformBooleanReturns(mn);

		// Convert information about local variables (only needed for debugging really)
		transformLocalVariables(mn);

		// Convert boolean arrays to integer arrays
		transformArrays(mn);

		return flagUses.size() - before;
	}

}
