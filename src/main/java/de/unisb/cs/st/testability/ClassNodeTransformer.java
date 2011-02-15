package de.unisb.cs.st.testability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Created by Yanchuan Li Date: 1/17/11 Time: 11:13 AM
 */
public class ClassNodeTransformer {

	private final ClassNode cn;
	private final String classNameWithDots;
	private final ClassTransformer ct;
	private static Logger log = Logger.getLogger(ClassNodeTransformer.class);
	private final List<Transformation> records;

	public ClassNodeTransformer(ClassNode cn) {
		this.cn = cn;
		classNameWithDots = cn.name.replace('/', '.');
		ct = new ClassTransformer();
		records = new ArrayList<Transformation>();
	}

	public ClassNode transform() {
		processFields();
		processMethods();
		return cn;
	}

	private void processFields() {
		List fieldNodes = cn.fields;
		log.debug("======================= process field nodes =======================");
		int i = 0;
		for (Object o : fieldNodes) {
			FieldNode fn = (FieldNode) o;
			//            log.debug("access:" + fn.access + " desc:" + fn.desc + " name:" + fn.name + " signature:" + fn.signature + " value:" + fn.value);
			if (fn.desc.equals("Z")) {
				fn.desc = "I";
				i++;
				log.debug("boolean field " + fn.name + " is changed to integer");
			}
		}
		if (i != 0) {
			records.add(new Transformation(classNameWithDots, "",
			        StatisticUtil.FieldRetyped, i));
		}
	}

	private void processMethods() {
		List methodNodes = cn.methods;
		List<MethodNode> valkyrieMethods = new ArrayList<MethodNode>();

		for (Object o : methodNodes) {
			MethodNode mn = (MethodNode) o;

			String methodSignature = mn.name + "|" + mn.desc;
			if (TransformationHelper.methodsMap.containsKey(classNameWithDots)
			        && TransformationHelper.methodsMap.get(classNameWithDots).containsKey(methodSignature)) {
				String newSignature = TransformationHelper.methodsMap.get(classNameWithDots).get(methodSignature).getNewName();
				String[] arr = newSignature.split("\\|");
				String newName = arr[0];
				String newDesc = arr[1];

				MethodNode valkyrieMethod = cloneMethodNode(mn);
				valkyrieMethod.name = newName;
				valkyrieMethod.desc = newDesc;
				log.debug(methodSignature + " is cloned to " + newName + "|" + newDesc);
				StatisticUtil.clear();
				fixOriginalMethod(mn);
				StatisticUtil.registerTransformation(StatisticUtil.MethodDuplicated);
				for (String s : StatisticUtil.logbook.keySet()) {
					records.add(new Transformation(classNameWithDots, methodSignature, s,
					        StatisticUtil.logbook.get(s)));
				}

				StatisticUtil.clear();
				transformMethod(valkyrieMethod);
				StatisticUtil.registerTransformation(StatisticUtil.MethodSignatureUpdated);
				for (String s : StatisticUtil.logbook.keySet()) {
					records.add(new Transformation(classNameWithDots, newSignature, s,
					        StatisticUtil.logbook.get(s)));
				}

				valkyrieMethods.add(valkyrieMethod);
			} else {
				StatisticUtil.clear();
				transformMethod(mn);

				for (String s : StatisticUtil.logbook.keySet()) {
					records.add(new Transformation(classNameWithDots, methodSignature, s,
					        StatisticUtil.logbook.get(s)));
				}

			}

		}
		for (MethodNode mn : valkyrieMethods) {
			cn.methods.add(mn);
		}

		DBHelper.writeToDB(records);

	}

	// as all fields are converted to integer, we have to revert back.
	private void fixOriginalMethod(MethodNode mn) {
		transformMethod(mn);
		InsnList ins = mn.instructions;
		AbstractInsnNode ain = ins.getFirst();
		while (ain != ins.getLast()) {
			if (ain.getOpcode() == Opcodes.IRETURN) {
				MethodInsnNode booleanHelperInvoke = new MethodInsnNode(
				        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
				        "RevertIntToBoolean",
				        Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
				                                 new Type[] { Type.INT_TYPE }));
				ins.insertBefore(ain, booleanHelperInvoke);
			}
			ain = ain.getNext();
		}
	}

	private void transformMethod(MethodNode mn) {

		String methodSignature = mn.name + "|" + mn.desc;
		log.debug("======================= process method " + methodSignature
		        + " =======================");

		//clear the local boolean variable index
		TransformationHelper.localVariableIndex.clear();
		boolean isBooleanMethod = TransformationHelper.valkyrieMethodExist(classNameWithDots,
		                                                                   methodSignature);

		log.debug("processing method:" + mn.name + " boolean? "
		        + String.valueOf(isBooleanMethod) + " desc:" + mn.desc);

		List lvs = mn.localVariables;

		if (lvs != null) {
			for (Object oo : lvs) {
				LocalVariableNode lvn = (LocalVariableNode) oo;
				if (lvn.desc.equals("Z")) {
					lvn.desc = "I";
					TransformationHelper.localVariableIndex.put(lvn.index, lvn.name);
					StatisticUtil.registerTransformation(StatisticUtil.LocalVariableRetyped);
					//                    log.debug("local variable " + lvn.name + " is changed to integer");
				}
				//                log.debug("local variable:" + lvn.name + " desc:" + lvn.desc + " index:" + lvn.index);
			}
		}

		InsnList ins = mn.instructions;
		AbstractInsnNode ain = ins.getFirst();
		while (ain != ins.getLast()) {
			ain = transformInstNode(ain, ins, isBooleanMethod);
			ain = ain.getNext();
		}

		//hack for return false
		if (isBooleanMethod) {
			AbstractInsnNode lastNode = mn.instructions.getLast();
			while (lastNode != null && lastNode.getOpcode() == -1) {
				lastNode = lastNode.getPrevious();
			}
			if (lastNode != null && lastNode.getOpcode() == Opcodes.IRETURN) {
				AbstractInsnNode predecessor = lastNode.getPrevious();
				if (predecessor.getOpcode() == Opcodes.ICONST_0) {
					//                    InsnNode in = new InsnNode(Opcodes.ICONST_M1);
					//                    ins.insertBefore(predecessor, in);

					MethodInsnNode booleanHelperPopFalse = new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "popFalse",
					        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
					ins.insertBefore(predecessor, booleanHelperPopFalse);
					ins.remove(predecessor);

					log.debug("\"return false\" is changed to \"return -1\"");
				}
			}
		}
	}

	private AbstractInsnNode transformInstNode(AbstractInsnNode ain, InsnList ins,
	        boolean isBooleanMethod) {

		AbstractInsnNode resultNode = ain;
		if (ain instanceof VarInsnNode) {
			VarInsnNode vin = (VarInsnNode) ain;
			if (TransformationHelper.localVariableIndex.keySet().contains(vin.var)) {
				//handle ICONST_0;ISTORE 1
				AbstractInsnNode predecessor = vin.getPrevious();
				if (vin.getOpcode() == Opcodes.ISTORE) {

					if (predecessor.getOpcode() == Opcodes.ICONST_0
					        || predecessor.getOpcode() == Opcodes.ICONST_1) {
						StatisticUtil.registerTransformation(StatisticUtil.BooleansBySimpleTrueAndFalse);
					} else if (predecessor instanceof MethodInsnNode) {
						StatisticUtil.registerTransformation(StatisticUtil.BooleansBySimpleMethod);
					} else if (predecessor.getOpcode() == Opcodes.GETFIELD
					        || predecessor.getOpcode() == Opcodes.GETSTATIC
					        || predecessor.getOpcode() == Opcodes.ILOAD) {
						StatisticUtil.registerTransformation(StatisticUtil.BooleansByVariable);
					}

					if (predecessor.getOpcode() == Opcodes.ICONST_0) {
						InsnNode in = new InsnNode(Opcodes.ICONST_M1);
						ins.remove(predecessor);
						ins.insertBefore(vin, in);
					}

				} else if (vin.getOpcode() == Opcodes.ILOAD) {
					//handel negation of local variable/field
					// ! and !!
					//                    TransformationHelper.checkAndTransformNegation(vin, ins, TransformationHelper.localVariableIndex);
				} else if (vin.getOpcode() == Opcodes.ASTORE) {
					//save a local array
					if (predecessor.getOpcode() == Opcodes.IASTORE
					        || predecessor.getOpcode() == Opcodes.BASTORE
					        || predecessor.getOpcode() == Opcodes.AASTORE) {
						TransformationHelper.findAndUpdateFalseinArray(predecessor,
						                                               ins,
						                                               TransformationHelper.localVariableIndex);
					} else if (predecessor.getOpcode() == Opcodes.NEWARRAY) {
						IntInsnNode newarrayNode = (IntInsnNode) predecessor;
						newarrayNode.operand = Opcodes.T_INT;
					} else if (predecessor.getOpcode() == Opcodes.MULTIANEWARRAY) {
						MultiANewArrayInsnNode manai = (MultiANewArrayInsnNode) predecessor;
						manai.desc = manai.desc.replaceAll("Z", "I");
					}
				}
			}
		} else if (ain instanceof FieldInsnNode) {
			FieldInsnNode fin = (FieldInsnNode) ain;
			//    PUTSTATIC BooleanSamples.a : boolean
			if (fin.desc.equals("Z")
			        && (fin.getOpcode() == Opcodes.PUTSTATIC || fin.getOpcode() == Opcodes.PUTFIELD)) {
				AbstractInsnNode predecessor = ain.getPrevious();
				if (predecessor.getOpcode() == Opcodes.ICONST_0
				        || predecessor.getOpcode() == Opcodes.ICONST_1) {
					StatisticUtil.registerTransformation(StatisticUtil.BooleansBySimpleTrueAndFalse);
				} else if (predecessor instanceof MethodInsnNode) {
					StatisticUtil.registerTransformation(StatisticUtil.BooleansBySimpleMethod);
				} else if (predecessor.getOpcode() == Opcodes.GETFIELD
				        || predecessor.getOpcode() == Opcodes.GETSTATIC
				        || predecessor.getOpcode() == Opcodes.ILOAD) {
					StatisticUtil.registerTransformation(StatisticUtil.BooleansByVariable);
				}

				if (TransformationHelper.checkPackage(fin.owner)) {
					fin.desc = "I";

					if (predecessor.getOpcode() == Opcodes.ICONST_0) {
						InsnNode in = new InsnNode(Opcodes.ICONST_M1);
						ins.remove(predecessor);
						ins.insertBefore(fin, in);
					} else if (predecessor.getOpcode() == Opcodes.NEWARRAY) {
						IntInsnNode newarrayNode = (IntInsnNode) predecessor;
						newarrayNode.operand = Opcodes.T_INT;
					} else if (predecessor.getOpcode() == Opcodes.MULTIANEWARRAY) {
						MultiANewArrayInsnNode manai = (MultiANewArrayInsnNode) predecessor;
						manai.desc = manai.desc.replaceAll("Z", "I");
					} else if (predecessor.getOpcode() == Opcodes.IASTORE
					        || predecessor.getOpcode() == Opcodes.BASTORE
					        || predecessor.getOpcode() == Opcodes.AASTORE) {
						TransformationHelper.findAndUpdateFalseinArray(predecessor,
						                                               ins,
						                                               TransformationHelper.localVariableIndex);
					}

				} else {
					log.debug("insert RevertIntToBoolean for desc:" + fin.desc
					        + " owner:" + fin.owner + " name:" + fin.name);
					MethodInsnNode booleanHelperInvoke = new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class),
					        "RevertIntToBoolean",
					        Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
					                                 new Type[] { Type.INT_TYPE }));
					ins.insertBefore(fin, booleanHelperInvoke);
				}

			} else if (fin.desc.equals("Z")
			        && (fin.getOpcode() == Opcodes.GETFIELD || fin.getOpcode() == Opcodes.GETSTATIC)) {
				//internal field
				log.debug(fin.getOpcode() + " name:" + fin.name + " desc:" + fin.desc
				        + " owner:" + fin.owner);
				if (TransformationHelper.checkPackage(fin.owner)) {
					log.debug("enter");
					fin.desc = "I";
				} else {
					//convert fields from other packages to integers, it may caused unused code if it's not followed by a putfield or putstatic
					log.debug("here");
					MethodInsnNode booleanHelperInvoke = new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class),
					        "convertBooleanToInt",
					        Type.getMethodDescriptor(Type.INT_TYPE,
					                                 new Type[] { Type.BOOLEAN_TYPE }));
					ins.insert(fin, booleanHelperInvoke);
					resultNode = booleanHelperInvoke;
				}
				//                resultNode = TransformationHelper.checkAndTransformNegation(fin, ins, localVariableIndex);
			}
		} else if (ain instanceof MethodInsnNode) {
			MethodInsnNode min = (MethodInsnNode) ain;
			if (min.getOpcode() == Opcodes.INVOKEINTERFACE) {
				// contains boolean in parameter or return value
				if (isBooleanRelatedMethod(min.desc)) {

					int firstBooleanParameterIndex = -1;
					Type[] types = Type.getArgumentTypes(min.desc);
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
							        Type.getInternalName(BooleanHelper.class),
							        "RevertIntToBoolean",
							        Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
							                                 new Type[] { Type.INT_TYPE }));
							ins.insertBefore(min, booleanHelperInvoke);
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
									        "RevertIntToBoolean",
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
										        Type.getMethodDescriptor(types[i],
										                                 new Type[] {}));
									} else if (types[i] == Type.CHAR_TYPE) {
										booleanHelperPopParameter = new MethodInsnNode(
										        Opcodes.INVOKESTATIC,
										        Type.getInternalName(BooleanHelper.class),
										        "popParameterChar",
										        Type.getMethodDescriptor(types[i],
										                                 new Type[] {}));
									} else if (types[i] == Type.BYTE_TYPE) {
										booleanHelperPopParameter = new MethodInsnNode(
										        Opcodes.INVOKESTATIC,
										        Type.getInternalName(BooleanHelper.class),
										        "popParameterByte",
										        Type.getMethodDescriptor(types[i],
										                                 new Type[] {}));
									} else if (types[i] == Type.SHORT_TYPE) {
										booleanHelperPopParameter = new MethodInsnNode(
										        Opcodes.INVOKESTATIC,
										        Type.getInternalName(BooleanHelper.class),
										        "popParameterShort",
										        Type.getMethodDescriptor(types[i],
										                                 new Type[] {}));
									} else if (types[i] == Type.INT_TYPE) {
										booleanHelperPopParameter = new MethodInsnNode(
										        Opcodes.INVOKESTATIC,
										        Type.getInternalName(BooleanHelper.class),
										        "popParameterInt",
										        Type.getMethodDescriptor(types[i],
										                                 new Type[] {}));
									} else if (types[i] == Type.FLOAT_TYPE) {
										booleanHelperPopParameter = new MethodInsnNode(
										        Opcodes.INVOKESTATIC,
										        Type.getInternalName(BooleanHelper.class),
										        "popParameterFloat",
										        Type.getMethodDescriptor(types[i],
										                                 new Type[] {}));
									} else if (types[i] == Type.LONG_TYPE) {
										booleanHelperPopParameter = new MethodInsnNode(
										        Opcodes.INVOKESTATIC,
										        Type.getInternalName(BooleanHelper.class),
										        "popParameterLong",
										        Type.getMethodDescriptor(types[i],
										                                 new Type[] {}));
									} else if (types[i] == Type.DOUBLE_TYPE) {
										booleanHelperPopParameter = new MethodInsnNode(
										        Opcodes.INVOKESTATIC,
										        Type.getInternalName(BooleanHelper.class),
										        "popParameterDouble",
										        Type.getMethodDescriptor(types[i],
										                                 new Type[] {}));
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
										TypeInsnNode tin = new TypeInsnNode(
										        Opcodes.CHECKCAST,
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
							ins.insertBefore(min, insnlist);
						}
					}
					if (min.desc.endsWith("Z")) {
						MethodInsnNode booleanHelperInvoke = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "convertBooleanToInt",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] { Type.BOOLEAN_TYPE }));
						ins.insert(min, booleanHelperInvoke);
						resultNode = booleanHelperInvoke;
					}
				}
			} else {
				String methodclassNameWithDots = min.owner.replace('/', '.');
				String methodSignature = min.name + "|" + min.desc;
				log.debug(methodclassNameWithDots + ":" + min.getOpcode() + " signature:"
				        + methodSignature);
				if (isBooleanRelatedMethod(min.desc)) {
					if (TransformationHelper.methodsMap.containsKey(methodclassNameWithDots)) {
						if (TransformationHelper.methodsMap.get(methodclassNameWithDots).containsKey(methodSignature)) {
							String newSignature = TransformationHelper.methodsMap.get(methodclassNameWithDots).get(methodSignature).getNewName();
							String[] arr = newSignature.split("\\|");
							min.name = arr[0];
							min.desc = arr[1];
						} else {
							Map<String, Set<String>> superclasses = ct.findAllSuperClassesWithMethods(methodclassNameWithDots);
							String origin = ct.findMethodOrigin(methodSignature,
							                                    superclasses);
							if (origin != null
							        && TransformationHelper.checkPackageWithDotName(origin)) {
								String newSignature = TransformationHelper.methodsMap.get(origin).get(methodSignature).getNewName();
								String[] arr = newSignature.split("\\|");
								min.name = arr[0];
								min.desc = arr[1];
							} else {
								if (!min.name.equals("RevertIntToBoolean")
								        && !min.owner.endsWith(Type.getInternalName(BooleanHelper.class))) {
									int firstBooleanParameterIndex = -1;
									Type[] types = Type.getArgumentTypes(min.desc);
									for (int i = 0; i < types.length; i++) {
										if (types[i].getDescriptor().equals("Z")) {
											if (firstBooleanParameterIndex == -1) {
												firstBooleanParameterIndex = i;
											}
										}
									}
									if (firstBooleanParameterIndex != -1) {
										int numOfPushs = types.length - 1
										        - firstBooleanParameterIndex;
										//                        int numOfPushs = types.length - firstBooleanParameterIndex;

										if (numOfPushs == 0) {
											//the boolean parameter is the last parameter
											MethodInsnNode booleanHelperInvoke = new MethodInsnNode(
											        Opcodes.INVOKESTATIC,
											        Type.getInternalName(BooleanHelper.class),
											        "RevertIntToBoolean",
											        Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
											                                 new Type[] { Type.INT_TYPE }));
											ins.insertBefore(min, booleanHelperInvoke);
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
														                                         - 1
														                                         - i] }));
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
													        "RevertIntToBoolean",
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
														        Type.getMethodDescriptor(types[i],
														                                 new Type[] {}));
													} else if (types[i] == Type.CHAR_TYPE) {
														booleanHelperPopParameter = new MethodInsnNode(
														        Opcodes.INVOKESTATIC,
														        Type.getInternalName(BooleanHelper.class),
														        "popParameterChar",
														        Type.getMethodDescriptor(types[i],
														                                 new Type[] {}));
													} else if (types[i] == Type.BYTE_TYPE) {
														booleanHelperPopParameter = new MethodInsnNode(
														        Opcodes.INVOKESTATIC,
														        Type.getInternalName(BooleanHelper.class),
														        "popParameterByte",
														        Type.getMethodDescriptor(types[i],
														                                 new Type[] {}));
													} else if (types[i] == Type.SHORT_TYPE) {
														booleanHelperPopParameter = new MethodInsnNode(
														        Opcodes.INVOKESTATIC,
														        Type.getInternalName(BooleanHelper.class),
														        "popParameterShort",
														        Type.getMethodDescriptor(types[i],
														                                 new Type[] {}));
													} else if (types[i] == Type.INT_TYPE) {
														booleanHelperPopParameter = new MethodInsnNode(
														        Opcodes.INVOKESTATIC,
														        Type.getInternalName(BooleanHelper.class),
														        "popParameterInt",
														        Type.getMethodDescriptor(types[i],
														                                 new Type[] {}));
													} else if (types[i] == Type.FLOAT_TYPE) {
														booleanHelperPopParameter = new MethodInsnNode(
														        Opcodes.INVOKESTATIC,
														        Type.getInternalName(BooleanHelper.class),
														        "popParameterFloat",
														        Type.getMethodDescriptor(types[i],
														                                 new Type[] {}));
													} else if (types[i] == Type.LONG_TYPE) {
														booleanHelperPopParameter = new MethodInsnNode(
														        Opcodes.INVOKESTATIC,
														        Type.getInternalName(BooleanHelper.class),
														        "popParameterLong",
														        Type.getMethodDescriptor(types[i],
														                                 new Type[] {}));
													} else if (types[i] == Type.DOUBLE_TYPE) {
														booleanHelperPopParameter = new MethodInsnNode(
														        Opcodes.INVOKESTATIC,
														        Type.getInternalName(BooleanHelper.class),
														        "popParameterDouble",
														        Type.getMethodDescriptor(types[i],
														                                 new Type[] {}));
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
														TypeInsnNode tin = new TypeInsnNode(
														        Opcodes.CHECKCAST,
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
											ins.insertBefore(min, insnlist);
										}
									}

									if (!TransformationHelper.checkPackage(min.owner)
									        && min.desc.endsWith("Z")) {
										MethodInsnNode booleanHelperInvoke = new MethodInsnNode(
										        Opcodes.INVOKESTATIC,
										        Type.getInternalName(BooleanHelper.class),
										        "convertBooleanToInt",
										        Type.getMethodDescriptor(Type.INT_TYPE,
										                                 new Type[] { Type.BOOLEAN_TYPE }));
										ins.insert(min, booleanHelperInvoke);
										resultNode = booleanHelperInvoke;
									}
								}
							}
						}
					} else {
						if (!min.name.equals("RevertIntToBoolean")
						        && !min.owner.endsWith(Type.getInternalName(BooleanHelper.class))) {
							int firstBooleanParameterIndex = -1;
							Type[] types = Type.getArgumentTypes(min.desc);
							for (int i = 0; i < types.length; i++) {
								if (types[i].getDescriptor().equals("Z")) {
									if (firstBooleanParameterIndex == -1) {
										firstBooleanParameterIndex = i;
									}
								}
							}
							if (firstBooleanParameterIndex != -1) {
								int numOfPushs = types.length - 1
								        - firstBooleanParameterIndex;
								//                        int numOfPushs = types.length - firstBooleanParameterIndex;

								if (numOfPushs == 0) {
									//the boolean parameter is the last parameter
									MethodInsnNode booleanHelperInvoke = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "RevertIntToBoolean",
									        Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
									                                 new Type[] { Type.INT_TYPE }));
									ins.insertBefore(min, booleanHelperInvoke);
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
												                                         - 1
												                                         - i] }));
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
											        "RevertIntToBoolean",
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
												        Type.getMethodDescriptor(types[i],
												                                 new Type[] {}));
											} else if (types[i] == Type.CHAR_TYPE) {
												booleanHelperPopParameter = new MethodInsnNode(
												        Opcodes.INVOKESTATIC,
												        Type.getInternalName(BooleanHelper.class),
												        "popParameterChar",
												        Type.getMethodDescriptor(types[i],
												                                 new Type[] {}));
											} else if (types[i] == Type.BYTE_TYPE) {
												booleanHelperPopParameter = new MethodInsnNode(
												        Opcodes.INVOKESTATIC,
												        Type.getInternalName(BooleanHelper.class),
												        "popParameterByte",
												        Type.getMethodDescriptor(types[i],
												                                 new Type[] {}));
											} else if (types[i] == Type.SHORT_TYPE) {
												booleanHelperPopParameter = new MethodInsnNode(
												        Opcodes.INVOKESTATIC,
												        Type.getInternalName(BooleanHelper.class),
												        "popParameterShort",
												        Type.getMethodDescriptor(types[i],
												                                 new Type[] {}));
											} else if (types[i] == Type.INT_TYPE) {
												booleanHelperPopParameter = new MethodInsnNode(
												        Opcodes.INVOKESTATIC,
												        Type.getInternalName(BooleanHelper.class),
												        "popParameterInt",
												        Type.getMethodDescriptor(types[i],
												                                 new Type[] {}));
											} else if (types[i] == Type.FLOAT_TYPE) {
												booleanHelperPopParameter = new MethodInsnNode(
												        Opcodes.INVOKESTATIC,
												        Type.getInternalName(BooleanHelper.class),
												        "popParameterFloat",
												        Type.getMethodDescriptor(types[i],
												                                 new Type[] {}));
											} else if (types[i] == Type.LONG_TYPE) {
												booleanHelperPopParameter = new MethodInsnNode(
												        Opcodes.INVOKESTATIC,
												        Type.getInternalName(BooleanHelper.class),
												        "popParameterLong",
												        Type.getMethodDescriptor(types[i],
												                                 new Type[] {}));
											} else if (types[i] == Type.DOUBLE_TYPE) {
												booleanHelperPopParameter = new MethodInsnNode(
												        Opcodes.INVOKESTATIC,
												        Type.getInternalName(BooleanHelper.class),
												        "popParameterDouble",
												        Type.getMethodDescriptor(types[i],
												                                 new Type[] {}));
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
												TypeInsnNode tin = new TypeInsnNode(
												        Opcodes.CHECKCAST,
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
									ins.insertBefore(min, insnlist);
								}
							}

							if (!TransformationHelper.checkPackage(min.owner)
							        && min.desc.endsWith("Z")) {
								MethodInsnNode booleanHelperInvoke = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "convertBooleanToInt",
								        Type.getMethodDescriptor(Type.INT_TYPE,
								                                 new Type[] { Type.BOOLEAN_TYPE }));
								ins.insert(min, booleanHelperInvoke);
								resultNode = booleanHelperInvoke;
							}
						}
					}

				} else {

				}
			}

			//            resultNode = TransformationHelper.checkAndTransformNegation(min, ins, TransformationHelper.localVariableIndex);
		} else if (ain instanceof TypeInsnNode) {
			TypeInsnNode tin = (TypeInsnNode) ain;
			if (ain.getOpcode() == Opcodes.INSTANCEOF) {
				//                log.debug("instanceof:" + tin.desc);
				resultNode = TransformationHelper.checkAndTransformInstanceof(ain,
				                                                              ins,
				                                                              TransformationHelper.localVariableIndex);

			}
		} else if (ain instanceof JumpInsnNode) {
			if (ain.getOpcode() == Opcodes.IF_ICMPEQ
			        || ain.getOpcode() == Opcodes.IF_ICMPNE
			        || ain.getOpcode() == Opcodes.IF_ICMPLE
			        || ain.getOpcode() == Opcodes.IF_ICMPGE
			        || ain.getOpcode() == Opcodes.IF_ICMPLT
			        || ain.getOpcode() == Opcodes.IF_ICMPGT
			        || ain.getOpcode() == Opcodes.IFLE || ain.getOpcode() == Opcodes.IFLT
			        || ain.getOpcode() == Opcodes.IFGE || ain.getOpcode() == Opcodes.IFGT) {
				resultNode = TransformationHelper.checkAndTransformBooleansByArithmeticOperation(ain,
				                                                                                 ins,
				                                                                                 isBooleanMethod);
			} else if (ain.getOpcode() == Opcodes.IFNE || ain.getOpcode() == Opcodes.IFEQ) {
				resultNode = TransformationHelper.checkAndTransformFlagUsage(ain, ins,
				                                                             isBooleanMethod);
			} else if (ain.getOpcode() == Opcodes.IF_ACMPNE
			        || ain.getOpcode() == Opcodes.IF_ACMPEQ) {
				resultNode = TransformationHelper.checkAndTransformEqualityComparison(ain,
				                                                                      ins,
				                                                                      isBooleanMethod);
			} else if (ain.getOpcode() == Opcodes.IFNULL
			        || ain.getOpcode() == Opcodes.IFNONNULL) {
				resultNode = TransformationHelper.checkAndTransformNullityComparison(ain,
				                                                                     ins,
				                                                                     isBooleanMethod);
			}
		} else if (ain instanceof InsnNode) {
			if (ain.getOpcode() == Opcodes.IRETURN) {
				/*
				   e.g.
				   if (c == ' ') return true;
				*/

			} else if (ain.getOpcode() == Opcodes.BASTORE) {
				//handle assign array values:  z=booleanArray[1];
				AbstractInsnNode arrayNode = TransformationHelper.findAndUpdateArrayNode(ain,
				                                                                         ins,
				                                                                         TransformationHelper.localVariableIndex);
				boolean saveToLocalArray = false;
				if (arrayNode.getOpcode() == Opcodes.ALOAD) {
					//assign value to a local boolean array
					saveToLocalArray = true;
				} else if (arrayNode.getOpcode() == Opcodes.GETFIELD
				        || arrayNode.getOpcode() == Opcodes.GETSTATIC) {
					FieldInsnNode fin = (FieldInsnNode) arrayNode;
					if (TransformationHelper.checkPackage(fin.owner)) {
						saveToLocalArray = true;
					}
				}
				if (saveToLocalArray) {
					AbstractInsnNode predecessor = ain.getPrevious();
					if (predecessor.getOpcode() == Opcodes.ICONST_0) {
						InsnNode in = new InsnNode(Opcodes.ICONST_M1);
						ins.remove(predecessor);
						ins.insertBefore(ain, in);
					}

					InsnNode in = new InsnNode(Opcodes.IASTORE);
					ins.insertBefore(ain, in);
					ins.remove(ain);
					resultNode = in;
				}
			} else if (ain.getOpcode() == Opcodes.BALOAD) {
				AbstractInsnNode arrayNode = TransformationHelper.findAndUpdateArrayNode(ain,
				                                                                         ins,
				                                                                         TransformationHelper.localVariableIndex);
				boolean saveToLocalArray = false;
				if (arrayNode.getOpcode() == Opcodes.ALOAD) {
					//assign value to a local boolean array
					saveToLocalArray = true;
				} else if (arrayNode.getOpcode() == Opcodes.GETFIELD
				        || arrayNode.getOpcode() == Opcodes.GETSTATIC) {
					FieldInsnNode fin = (FieldInsnNode) arrayNode;
					if (TransformationHelper.checkPackage(fin.owner)) {
						saveToLocalArray = true;
					}
				}
				if (saveToLocalArray) {
					InsnNode in = new InsnNode(Opcodes.IALOAD);
					ins.insertBefore(ain, in);
					ins.remove(ain);
					resultNode = in;
				}
			}
		}

		return resultNode;
	}

	private MethodNode cloneMethodNode(MethodNode method) {

		String[] exceptions = (String[]) method.exceptions.toArray(new String[method.exceptions.size()]);
		MethodNode result = new MethodNode(method.access, method.name, method.desc,
		        method.signature, exceptions) {

			/**
			 * Label remapping. Old label -> new label.
			 */
			private final Map<Label, Label> labels = new HashMap<Label, Label>();

			@Override
			protected LabelNode getLabelNode(Label label) {
				Label newLabel = labels.get(label);
				if (newLabel == null) {
					newLabel = new Label();
					labels.put(label, newLabel);
				}

				return super.getLabelNode(newLabel);
			}
		};
		method.accept(result);

		return result;
	}

	private boolean isBooleanRelatedMethod(String methodDesc) {
		boolean result = false;
		if (methodDesc.endsWith("Z")) {
			result = true;
		} else {
			Type[] types = Type.getArgumentTypes(methodDesc);
			for (int i = 0; i < types.length; i++) {
				if (types[i].getDescriptor().equals("Z")) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

}
