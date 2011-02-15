package de.unisb.cs.st.testability;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Created by Yanchuan Li Date: 21.12.10 Time: 22:17
 */
public class TransformationHelper {

	private static Logger log = Logger.getLogger(TransformationHelper.class);
	public static String[] testPackage = "com.yanchuan".split("\\.");

	public static Map<String, Map<String, MethodUnit>> methodsMap = new HashMap<String, Map<String, MethodUnit>>();

	public static Map<String, MethodUnit> reverseMethodsMap = new HashMap<String, MethodUnit>();

	public static Map<String, List<String>> fieldsMap = new HashMap<String, List<String>>();

	public static Map<Integer, String> localVariableIndex = new HashMap<Integer, String>();

	public static void setTestPackage(String p) {
		testPackage = p.split("\\.");
	}

	public static AbstractInsnNode checkAndTransformBooleansByArithmeticOperation(
	        AbstractInsnNode startNode, InsnList ins, boolean isBooleanMethod) {
		log.debug("checkAndTransformBooleansByArithmeticOperation ...");
		AbstractInsnNode resultNode = startNode;
		boolean completeSimplePredicateStructureDetected = false;
		boolean simpleIReturnStructureDetected = false;

		AbstractInsnNode storeLabel;
		//mark all the nodes to be deleted
		if (startNode.getOpcode() == Opcodes.IF_ICMPEQ
		        || startNode.getOpcode() == Opcodes.IF_ICMPNE
		        || startNode.getOpcode() == Opcodes.IF_ICMPLE
		        || startNode.getOpcode() == Opcodes.IF_ICMPGE
		        || startNode.getOpcode() == Opcodes.IF_ICMPGT
		        || startNode.getOpcode() == Opcodes.IF_ICMPLT
		        || startNode.getOpcode() == Opcodes.IFLE
		        || startNode.getOpcode() == Opcodes.IFLT
		        || startNode.getOpcode() == Opcodes.IFGE
		        || startNode.getOpcode() == Opcodes.IFGT
		        || startNode.getOpcode() == Opcodes.IFNE
		        || startNode.getOpcode() == Opcodes.IFEQ) {
			AbstractInsnNode iconst0Oriconst1 = startNode.getNext();
			while (iconst0Oriconst1.getOpcode() == -1) {
				iconst0Oriconst1 = iconst0Oriconst1.getNext();
			}
			if (iconst0Oriconst1 != null
			        && iconst0Oriconst1 instanceof InsnNode
			        && (iconst0Oriconst1.getOpcode() == Opcodes.ICONST_0 || iconst0Oriconst1.getOpcode() == Opcodes.ICONST_1)) {
				AbstractInsnNode gotoLabel = iconst0Oriconst1.getNext();
				while (gotoLabel.getOpcode() == -1) {
					gotoLabel = gotoLabel.getNext();
				}
				//                log.debug("12");
				if (gotoLabel != null && gotoLabel instanceof JumpInsnNode
				        && gotoLabel.getOpcode() == Opcodes.GOTO) {
					AbstractInsnNode falseLabel = gotoLabel.getNext();
					if (falseLabel != null && falseLabel instanceof LabelNode) {

						AbstractInsnNode iconst0Oriconst1_2 = falseLabel.getNext();
						while (iconst0Oriconst1_2.getOpcode() == -1) {
							iconst0Oriconst1_2 = iconst0Oriconst1_2.getNext();
						}

						if (iconst0Oriconst1_2 != null
						        && iconst0Oriconst1_2 instanceof InsnNode
						        && (iconst0Oriconst1_2.getOpcode() == Opcodes.ICONST_0 || iconst0Oriconst1_2.getOpcode() == Opcodes.ICONST_1)) {
							storeLabel = iconst0Oriconst1_2.getNext();
							if (storeLabel != null && storeLabel instanceof LabelNode) {

								AbstractInsnNode storeNode = storeLabel.getNext();
								while (storeNode.getOpcode() == -1) {
									storeNode = storeNode.getNext();
								}

								completeSimplePredicateStructureDetected = isLocalBooleanRelated(storeNode,
								                                                                 ins);
								log.debug("completeSimplePredicateStructureDetected");
							}
						} else {
							//iconst_0 is already changed to addK/subK operations
							if (iconst0Oriconst1_2.getOpcode() == Opcodes.INVOKESTATIC) {
								MethodInsnNode min = (MethodInsnNode) iconst0Oriconst1_2;
								if ((min.name.equals("pop") || min.name.equals("popTrue") || min.name.equals("popFalse"))
								        && min.owner.equals("com/yanchuan/valkyrie/util/BooleanHelper")) {
									completeSimplePredicateStructureDetected = true;
								}
							}
						}
					}
				} else {
					//                    log.debug("23:" + gotoLabel.getOpcode());
					if (gotoLabel != null && (gotoLabel.getOpcode() == Opcodes.IRETURN)
					        && isBooleanMethod) {
						simpleIReturnStructureDetected = true;
						log.debug("simpleIReturnStructureDetected detected");
					}
				}
			} else {
				log.debug("detect conjunctions and disjunctions");
				//check if it's already modified
				boolean alreadyTransformed = false;
				AbstractInsnNode ain = startNode.getPrevious();
				if (ain instanceof MethodInsnNode) {
					MethodInsnNode pushNode = (MethodInsnNode) ain;
					if (pushNode.name.equals("push")
					        && pushNode.owner.equals(Type.getInternalName(BooleanHelper.class))) {
						alreadyTransformed = true;
						//                        log.debug("already modified");
					} else {
						if (checkPackage(pushNode.owner) && pushNode.desc.endsWith("I")) {
							MethodInsnNode booleanHelperPush = new MethodInsnNode(
							        Opcodes.INVOKESTATIC,
							        Type.getInternalName(BooleanHelper.class),
							        "push",
							        Type.getMethodDescriptor(Type.VOID_TYPE,
							                                 new Type[] { Type.INT_TYPE }));
							InsnNode dupNode = new InsnNode(Opcodes.DUP);
							ins.insert(ain, booleanHelperPush);
							ins.insert(ain, dupNode);
						}
					}
				} else {
					//                    log.debug("previous:" + ain.getOpcode() + " current:" + startNode.getOpcode());
				}

				if (!alreadyTransformed) {
					log.debug("halo23");
					//check if it's storing the value to local variable or variable within the test package
					AbstractInsnNode nextGotoNode = startNode.getNext();
					while (nextGotoNode != null
					        && nextGotoNode.getOpcode() != Opcodes.GOTO) {
						nextGotoNode = nextGotoNode.getNext();
					}

					JumpInsnNode gotoNode = (JumpInsnNode) nextGotoNode;
					if (gotoNode != null) {
						//                        log.debug("hallo45");
						AbstractInsnNode nextStoreLabel = gotoNode.label;
						AbstractInsnNode nodeAfterStoreLabel = nextStoreLabel.getNext();
						while (nodeAfterStoreLabel.getOpcode() == -1) {
							nodeAfterStoreLabel = nodeAfterStoreLabel.getNext();
						}
						if (isLocalBooleanRelated(nodeAfterStoreLabel, ins)) {
							InsnNode subNode;
							InsnNode castNode = null;
							AbstractInsnNode otherType = startNode.getPrevious();
							if (otherType.getOpcode() == Opcodes.LCMP) {
								subNode = new InsnNode(Opcodes.LSUB);
								castNode = new InsnNode(Opcodes.L2I);
							} else if (otherType.getOpcode() == Opcodes.FCMPL
							        || otherType.getOpcode() == Opcodes.FCMPG) {
								subNode = new InsnNode(Opcodes.FSUB);
								castNode = new InsnNode(Opcodes.F2I);
							} else if (otherType.getOpcode() == Opcodes.DCMPL
							        || otherType.getOpcode() == Opcodes.DCMPG) {
								subNode = new InsnNode(Opcodes.DSUB);
								castNode = new InsnNode(Opcodes.D2I);
							} else {
								subNode = new InsnNode(Opcodes.ISUB);
								castNode = new InsnNode(Opcodes.ICONST_0);
							}

							MethodInsnNode booleanHelperPush = new MethodInsnNode(
							        Opcodes.INVOKESTATIC,
							        Type.getInternalName(BooleanHelper.class),
							        "push",
							        Type.getMethodDescriptor(Type.VOID_TYPE,
							                                 new Type[] { Type.INT_TYPE }));
							InsnNode dupNode = new InsnNode(Opcodes.DUP);
							if (otherType.getOpcode() == Opcodes.INVOKESTATIC
							        || otherType.getOpcode() == Opcodes.INVOKEVIRTUAL) {
								MethodInsnNode min = (MethodInsnNode) otherType;
								if (min.name.equals("convertBooleanToInt")
								        && min.owner.equals(Type.getInternalName(BooleanHelper.class))) {
									ins.insertBefore(startNode, dupNode);
									ins.insertBefore(startNode, booleanHelperPush);
								}
							} else if (otherType.getOpcode() == Opcodes.LCMP
							        || otherType.getOpcode() == Opcodes.FCMPL
							        || otherType.getOpcode() == Opcodes.DCMPL
							        || otherType.getOpcode() == Opcodes.DCMPG
							        || otherType.getOpcode() == Opcodes.FCMPG) {
								if (otherType.getOpcode() == Opcodes.FCMPG
								        || otherType.getOpcode() == Opcodes.FCMPL) {
									InsnNode dup2Node = new InsnNode(Opcodes.DUP2);
									ins.insertBefore(otherType, dup2Node);
									ins.insertBefore(otherType, subNode);
									ins.insertBefore(otherType, castNode);
									ins.insertBefore(otherType, booleanHelperPush);
								} else if (otherType.getOpcode() == Opcodes.DCMPL
								        || otherType.getOpcode() == Opcodes.DCMPG) {
									MethodInsnNode computeDistance = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "computeDoubleDifference",
									        Type.getMethodDescriptor(Type.INT_TYPE,
									                                 new Type[] {
									                                         Type.DOUBLE_TYPE,
									                                         Type.DOUBLE_TYPE }));
									ins.insertBefore(otherType, computeDistance);
									ins.insertBefore(otherType, booleanHelperPush);
									MethodInsnNode pop1 = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "popDoubleOperand",
									        Type.getMethodDescriptor(Type.DOUBLE_TYPE,
									                                 new Type[] {}));
									MethodInsnNode pop2 = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "popDoubleOperand",
									        Type.getMethodDescriptor(Type.DOUBLE_TYPE,
									                                 new Type[] {}));
									ins.insertBefore(otherType, pop1);
									ins.insertBefore(otherType, pop2);
								} else {
									//LCMP
									MethodInsnNode computeDistance = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "computeLongDifference",
									        Type.getMethodDescriptor(Type.INT_TYPE,
									                                 new Type[] {
									                                         Type.LONG_TYPE,
									                                         Type.LONG_TYPE }));
									ins.insertBefore(otherType, computeDistance);
									ins.insertBefore(otherType, booleanHelperPush);
									MethodInsnNode pop1 = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "popLongOperand",
									        Type.getMethodDescriptor(Type.LONG_TYPE,
									                                 new Type[] {}));
									MethodInsnNode pop2 = new MethodInsnNode(
									        Opcodes.INVOKESTATIC,
									        Type.getInternalName(BooleanHelper.class),
									        "popLongOperand",
									        Type.getMethodDescriptor(Type.LONG_TYPE,
									                                 new Type[] {}));
									ins.insertBefore(otherType, pop1);
									ins.insertBefore(otherType, pop2);
								}

								//                                ins.remove(otherType);
							} else {

								if (startNode.getOpcode() == Opcodes.IFLE
								        || startNode.getOpcode() == Opcodes.IFLT
								        || startNode.getOpcode() == Opcodes.IFGT
								        || startNode.getOpcode() == Opcodes.IFGE) {
									if (castNode.getOpcode() != Opcodes.ICONST_0) {
										ins.insertBefore(startNode, castNode);
									}
									ins.insertBefore(startNode, dupNode);
									ins.insertBefore(startNode, booleanHelperPush);
								} else if (startNode.getOpcode() == Opcodes.IF_ICMPEQ
								        || startNode.getOpcode() == Opcodes.IF_ICMPNE
								        || startNode.getOpcode() == Opcodes.IF_ICMPLE
								        || startNode.getOpcode() == Opcodes.IF_ICMPGE
								        || startNode.getOpcode() == Opcodes.IF_ICMPGT
								        || startNode.getOpcode() == Opcodes.IF_ICMPLT) {
									InsnNode dup2Node = new InsnNode(Opcodes.DUP2);
									ins.insertBefore(startNode, dup2Node);
									ins.insertBefore(startNode, subNode);
									ins.insertBefore(startNode, booleanHelperPush);
								}
								//if it's not integer, we have to add an cast node

							}

							log.debug("1");
							JumpInsnNode jin = transformJumpNode((JumpInsnNode) startNode);

							ins.insertBefore(startNode, jin);
							ins.remove(startNode);
							resultNode = jin;

							StatisticUtil.registerTransformation(StatisticUtil.BooleansByArithmeticOperation);
						}
					} else {
						// a ireturn structure in conjunctions/disjunctions
						log.debug("cannot found goto node");
						LabelNode branchLabel = ((JumpInsnNode) startNode).label;
						AbstractInsnNode iconst1or0 = branchLabel.getNext();
						while (iconst1or0 != null && iconst1or0.getOpcode() == -1) {
							iconst1or0 = iconst1or0.getNext();
						}
						//                        log.debug("hall2112:" + iconst1or0.getOpcode());
						if ((iconst1or0.getOpcode() == Opcodes.ICONST_0 || iconst1or0.getOpcode() == Opcodes.ICONST_1)
						        && isBooleanMethod) {
							AbstractInsnNode ireturn = iconst1or0.getNext();
							//                            log.debug("iconst0 or iconst1 found ...");
							if (ireturn != null && ireturn.getOpcode() == Opcodes.IRETURN) {
								//conjunction/disjunction with ireturn detected
								//                                log.debug("ireturn found ...");
								InsnNode subNode;
								AbstractInsnNode castNode = null;
								AbstractInsnNode otherType = startNode.getPrevious();
								if (otherType.getOpcode() == Opcodes.LCMP) {
									subNode = new InsnNode(Opcodes.LSUB);
									castNode = new InsnNode(Opcodes.L2I);
								} else if (otherType.getOpcode() == Opcodes.FCMPL
								        || otherType.getOpcode() == Opcodes.FCMPG) {
									subNode = new InsnNode(Opcodes.FSUB);
									castNode = new InsnNode(Opcodes.F2I);
								} else if (otherType.getOpcode() == Opcodes.DCMPL
								        || otherType.getOpcode() == Opcodes.DCMPG) {
									subNode = new InsnNode(Opcodes.DSUB);
									castNode = new InsnNode(Opcodes.D2I);
								} else {
									subNode = new InsnNode(Opcodes.ISUB);
									castNode = new InsnNode(Opcodes.ICONST_0);
								}

								MethodInsnNode booleanHelperPush = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "push",
								        Type.getMethodDescriptor(Type.VOID_TYPE,
								                                 new Type[] { Type.INT_TYPE }));
								InsnNode dupNode = new InsnNode(Opcodes.DUP);
								if (otherType.getOpcode() == Opcodes.INVOKESTATIC
								        || otherType.getOpcode() == Opcodes.INVOKEVIRTUAL) {
									MethodInsnNode min = (MethodInsnNode) otherType;
									if (min.name.equals("convertBooleanToInt")
									        && min.owner.equals(Type.getInternalName(BooleanHelper.class))) {
										ins.insertBefore(startNode, dupNode);
										ins.insertBefore(startNode, booleanHelperPush);
									} else {
										ins.insertBefore(startNode, dupNode);
										ins.insertBefore(startNode, booleanHelperPush);
									}
								} else if (otherType.getOpcode() == Opcodes.LCMP
								        || otherType.getOpcode() == Opcodes.FCMPL
								        || otherType.getOpcode() == Opcodes.DCMPL
								        || otherType.getOpcode() == Opcodes.DCMPG
								        || otherType.getOpcode() == Opcodes.FCMPG) {
									if (otherType.getOpcode() == Opcodes.FCMPG
									        || otherType.getOpcode() == Opcodes.FCMPL) {
										InsnNode dup2Node = new InsnNode(Opcodes.DUP2);
										ins.insertBefore(otherType, dup2Node);
										ins.insertBefore(otherType, subNode);
										ins.insertBefore(otherType, castNode);
										ins.insertBefore(otherType, booleanHelperPush);
									} else if (otherType.getOpcode() == Opcodes.DCMPL
									        || otherType.getOpcode() == Opcodes.DCMPG) {
										MethodInsnNode computeDistance = new MethodInsnNode(
										        Opcodes.INVOKESTATIC,
										        Type.getInternalName(BooleanHelper.class),
										        "computeDoubleDifference",
										        Type.getMethodDescriptor(Type.INT_TYPE,
										                                 new Type[] {
										                                         Type.DOUBLE_TYPE,
										                                         Type.DOUBLE_TYPE }));
										ins.insertBefore(otherType, computeDistance);
										ins.insertBefore(otherType, booleanHelperPush);
										MethodInsnNode pop1 = new MethodInsnNode(
										        Opcodes.INVOKESTATIC,
										        Type.getInternalName(BooleanHelper.class),
										        "popDoubleOperand",
										        Type.getMethodDescriptor(Type.DOUBLE_TYPE,
										                                 new Type[] {}));
										MethodInsnNode pop2 = new MethodInsnNode(
										        Opcodes.INVOKESTATIC,
										        Type.getInternalName(BooleanHelper.class),
										        "popDoubleOperand",
										        Type.getMethodDescriptor(Type.DOUBLE_TYPE,
										                                 new Type[] {}));
										ins.insertBefore(otherType, pop1);
										ins.insertBefore(otherType, pop2);
									} else {
										//LCMP
										MethodInsnNode computeDistance = new MethodInsnNode(
										        Opcodes.INVOKESTATIC,
										        Type.getInternalName(BooleanHelper.class),
										        "computeLongDifference",
										        Type.getMethodDescriptor(Type.INT_TYPE,
										                                 new Type[] {
										                                         Type.LONG_TYPE,
										                                         Type.LONG_TYPE }));
										ins.insertBefore(otherType, computeDistance);
										ins.insertBefore(otherType, booleanHelperPush);
										MethodInsnNode pop1 = new MethodInsnNode(
										        Opcodes.INVOKESTATIC,
										        Type.getInternalName(BooleanHelper.class),
										        "popLongOperand",
										        Type.getMethodDescriptor(Type.LONG_TYPE,
										                                 new Type[] {}));
										MethodInsnNode pop2 = new MethodInsnNode(
										        Opcodes.INVOKESTATIC,
										        Type.getInternalName(BooleanHelper.class),
										        "popLongOperand",
										        Type.getMethodDescriptor(Type.LONG_TYPE,
										                                 new Type[] {}));
										ins.insertBefore(otherType, pop1);
										ins.insertBefore(otherType, pop2);
									}
								} else {
									if (startNode.getOpcode() == Opcodes.IFLE
									        || startNode.getOpcode() == Opcodes.IFLT
									        || startNode.getOpcode() == Opcodes.IFGT
									        || startNode.getOpcode() == Opcodes.IFGE) {
										if (castNode.getOpcode() != Opcodes.ICONST_0) {
											ins.insertBefore(startNode, castNode);
										}
										ins.insertBefore(startNode, dupNode);
										ins.insertBefore(startNode, booleanHelperPush);
									} else if (startNode.getOpcode() == Opcodes.IF_ICMPEQ
									        || startNode.getOpcode() == Opcodes.IF_ICMPNE
									        || startNode.getOpcode() == Opcodes.IF_ICMPLE
									        || startNode.getOpcode() == Opcodes.IF_ICMPGE
									        || startNode.getOpcode() == Opcodes.IF_ICMPGT
									        || startNode.getOpcode() == Opcodes.IF_ICMPLT) {

										InsnNode dup2Node = new InsnNode(Opcodes.DUP2);
										ins.insertBefore(startNode, dup2Node);
										ins.insertBefore(startNode, subNode);
										ins.insertBefore(startNode, booleanHelperPush);
									}
									//if it's not integer, we have to add an cast node

								}

								//change IF node to IFLT
								log.debug("1");
								JumpInsnNode jin = transformJumpNode((JumpInsnNode) startNode);
								//                                log.debug(jin.getOpcode() + " inserted");
								ins.insertBefore(startNode, jin);
								ins.remove(startNode);
								resultNode = jin;

								StatisticUtil.registerTransformation(StatisticUtil.BooleansByArithmeticOperation);
							}
						} else {
							//quick work round to change ifeq when it's a outsider field
							//it could be:
							//if(DummyUtil.a){sysout}
							log.debug("iconst1or0 not found :" + iconst1or0.getOpcode());
							boolean isFlag = isFlag(startNode, ins, localVariableIndex);
							if (isFlag) {
								MethodInsnNode booleanHelperPush = new MethodInsnNode(
								        Opcodes.INVOKESTATIC,
								        Type.getInternalName(BooleanHelper.class),
								        "push",
								        Type.getMethodDescriptor(Type.VOID_TYPE,
								                                 new Type[] { Type.INT_TYPE }));
								InsnNode dupNode = new InsnNode(Opcodes.DUP);
								ins.insertBefore(startNode, dupNode);
								ins.insertBefore(startNode, booleanHelperPush);
								JumpInsnNode jin = transformJumpNode((JumpInsnNode) startNode);
								ins.insertBefore(startNode, jin);
								resultNode = jin;
								ins.remove(startNode);
							}

						}

					}

				} else {
					//                    log.debug("haldsa2231lo");
				}

			}
			//one complete structure detected
			if (completeSimplePredicateStructureDetected) {
				log.debug("start arithmatic transformation");
				InsnNode subNode;
				AbstractInsnNode castNode = null;
				AbstractInsnNode otherType = startNode.getPrevious();
				if (otherType.getOpcode() == Opcodes.LCMP) {
					subNode = new InsnNode(Opcodes.LSUB);
					castNode = new InsnNode(Opcodes.L2I);
				} else if (otherType.getOpcode() == Opcodes.FCMPL
				        || otherType.getOpcode() == Opcodes.FCMPG) {
					subNode = new InsnNode(Opcodes.FSUB);
					castNode = new InsnNode(Opcodes.F2I);
				} else if (otherType.getOpcode() == Opcodes.DCMPL
				        || otherType.getOpcode() == Opcodes.DCMPG) {
					subNode = new InsnNode(Opcodes.DSUB);
					castNode = new InsnNode(Opcodes.D2I);
				} else {
					subNode = new InsnNode(Opcodes.ISUB);
					castNode = new InsnNode(Opcodes.ICONST_0);
				}

				MethodInsnNode booleanHelperPush = new MethodInsnNode(
				        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
				        "push", Type.getMethodDescriptor(Type.VOID_TYPE,
				                                         new Type[] { Type.INT_TYPE }));
				InsnNode dupNode = new InsnNode(Opcodes.DUP);
				if (otherType.getOpcode() == Opcodes.INVOKESTATIC
				        || otherType.getOpcode() == Opcodes.INVOKEVIRTUAL) {
					MethodInsnNode min = (MethodInsnNode) otherType;
					if (min.name.equals("convertBooleanToInt")
					        && min.owner.equals(Type.getInternalName(BooleanHelper.class))) {
						ins.insertBefore(startNode, dupNode);
						ins.insertBefore(startNode, booleanHelperPush);
					} else {
						ins.insertBefore(startNode, dupNode);
						ins.insertBefore(startNode, booleanHelperPush);
					}
				} else if (otherType.getOpcode() == Opcodes.LCMP
				        || otherType.getOpcode() == Opcodes.FCMPL
				        || otherType.getOpcode() == Opcodes.DCMPL
				        || otherType.getOpcode() == Opcodes.DCMPG
				        || otherType.getOpcode() == Opcodes.FCMPG) {
					if (otherType.getOpcode() == Opcodes.FCMPG
					        || otherType.getOpcode() == Opcodes.FCMPL) {
						InsnNode dup2Node = new InsnNode(Opcodes.DUP2);
						ins.insertBefore(otherType, dup2Node);
						ins.insertBefore(otherType, subNode);
						ins.insertBefore(otherType, castNode);
						ins.insertBefore(otherType, booleanHelperPush);
					} else if (otherType.getOpcode() == Opcodes.DCMPL
					        || otherType.getOpcode() == Opcodes.DCMPG) {
						MethodInsnNode computeDistance = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "computeDoubleDifference",
						        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {
						                Type.DOUBLE_TYPE, Type.DOUBLE_TYPE }));
						ins.insertBefore(otherType, computeDistance);
						ins.insertBefore(otherType, booleanHelperPush);
						MethodInsnNode pop1 = new MethodInsnNode(Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "popDoubleOperand",
						        Type.getMethodDescriptor(Type.DOUBLE_TYPE, new Type[] {}));
						MethodInsnNode pop2 = new MethodInsnNode(Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "popDoubleOperand",
						        Type.getMethodDescriptor(Type.DOUBLE_TYPE, new Type[] {}));
						ins.insertBefore(otherType, pop1);
						ins.insertBefore(otherType, pop2);
					} else {
						//LCMP
						MethodInsnNode computeDistance = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "computeLongDifference",
						        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {
						                Type.LONG_TYPE, Type.LONG_TYPE }));
						ins.insertBefore(otherType, computeDistance);
						ins.insertBefore(otherType, booleanHelperPush);
						MethodInsnNode pop1 = new MethodInsnNode(Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "popLongOperand",
						        Type.getMethodDescriptor(Type.LONG_TYPE, new Type[] {}));
						MethodInsnNode pop2 = new MethodInsnNode(Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "popLongOperand",
						        Type.getMethodDescriptor(Type.LONG_TYPE, new Type[] {}));
						ins.insertBefore(otherType, pop1);
						ins.insertBefore(otherType, pop2);
					}
				} else {
					if (startNode.getOpcode() == Opcodes.IFLE
					        || startNode.getOpcode() == Opcodes.IFLT
					        || startNode.getOpcode() == Opcodes.IFGT
					        || startNode.getOpcode() == Opcodes.IFGE) {
						if (castNode.getOpcode() != Opcodes.ICONST_0) {
							ins.insertBefore(startNode, castNode);
						}
						ins.insertBefore(startNode, dupNode);
						ins.insertBefore(startNode, booleanHelperPush);
					} else if (startNode.getOpcode() == Opcodes.IF_ICMPEQ
					        || startNode.getOpcode() == Opcodes.IF_ICMPNE
					        || startNode.getOpcode() == Opcodes.IF_ICMPLE
					        || startNode.getOpcode() == Opcodes.IF_ICMPGE
					        || startNode.getOpcode() == Opcodes.IF_ICMPGT
					        || startNode.getOpcode() == Opcodes.IF_ICMPLT) {
						//                        ins.insertBefore(startNode, subNode);
						//                        InsnNode iconst0 = new InsnNode(Opcodes.ICONST_0);
						//                        ins.insertBefore(startNode, iconst0);
						//                        ins.insertBefore(startNode, booleanHelperPush);
						InsnNode dup2Node = new InsnNode(Opcodes.DUP2);
						ins.insertBefore(startNode, dup2Node);
						ins.insertBefore(startNode, subNode);
						ins.insertBefore(startNode, booleanHelperPush);
					}
				}

				//change IF node to IFLT
				log.debug("1");
				JumpInsnNode jin = transformJumpNode((JumpInsnNode) startNode);
				ins.insertBefore(startNode, jin);
				ins.remove(startNode);
				resultNode = jin;
				AbstractInsnNode gotoNode = jin;
				while (gotoNode.getOpcode() != Opcodes.GOTO) {
					gotoNode = gotoNode.getNext();
				}
				LabelNode gotoLabel = ((JumpInsnNode) gotoNode).label;
				//change ICONST_0 to -k and ICONST_1 to +k

				AbstractInsnNode nextNode = jin.getNext();
				//modify the ICONST1/ICONST0 directly after IF
				while (nextNode instanceof LabelNode) {
					nextNode = nextNode.getNext();
					//                    log.debug("skip label node");
				}
				MethodInsnNode booleanHelperPopTrue = new MethodInsnNode(
				        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
				        "popTrue", Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
				MethodInsnNode booleanHelperPopFalse = new MethodInsnNode(
				        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
				        "popFalse",
				        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));

				while (!(nextNode instanceof LabelNode)) {
					//                    log.debug("ifle next:" + nextNode.getNext().getOpcode());
					if (nextNode.getOpcode() == Opcodes.ICONST_1) {
						ins.insertBefore(nextNode, booleanHelperPopTrue);
						ins.remove(nextNode);
						nextNode = booleanHelperPopTrue;
						//                        log.debug("hehe iconst1 transformed! 1");
					} else if (nextNode.getOpcode() == Opcodes.ICONST_0) {
						ins.insertBefore(nextNode, booleanHelperPopFalse);
						ins.remove(nextNode);
						nextNode = booleanHelperPopFalse;
						//                        log.debug("hehe iconst0 transformed! 1");
					}
					nextNode = nextNode.getNext();
				}
				while (nextNode instanceof LabelNode) {
					nextNode = nextNode.getNext();
					//                    log.debug("skip label node 2");
				}
				//                nextNode = nextNode.getNext();
				MethodInsnNode booleanHelperPopTrue2 = new MethodInsnNode(
				        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
				        "popTrue", Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
				MethodInsnNode booleanHelperPopFalse2 = new MethodInsnNode(
				        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
				        "popFalse",
				        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));

				while (!(nextNode instanceof LabelNode)) {
					if (nextNode.getOpcode() == Opcodes.ICONST_1) {
						ins.insertBefore(nextNode, booleanHelperPopTrue2);
						ins.remove(nextNode);
						nextNode = booleanHelperPopTrue2;
						//                        log.debug("hehe iconst1 transformed! 2");
					} else if (nextNode.getOpcode() == Opcodes.ICONST_0) {
						ins.insertBefore(nextNode, booleanHelperPopFalse2);
						ins.remove(nextNode);
						nextNode = booleanHelperPopFalse2;
						//                        log.debug("hehe iconst0 transformed! 2");
					}
					nextNode = nextNode.getNext();
				}

				StatisticUtil.registerTransformation(StatisticUtil.BooleansByArithmeticOperation);

			} else if (simpleIReturnStructureDetected) {
				log.debug("start simpleIReturnStructureDetected");
				InsnNode subNode;
				AbstractInsnNode castNode = null;
				AbstractInsnNode otherType = startNode.getPrevious();
				if (otherType.getOpcode() == Opcodes.LCMP) {
					subNode = new InsnNode(Opcodes.LSUB);
					castNode = new InsnNode(Opcodes.L2I);
				} else if (otherType.getOpcode() == Opcodes.FCMPL
				        || otherType.getOpcode() == Opcodes.FCMPG) {
					subNode = new InsnNode(Opcodes.FSUB);
					castNode = new InsnNode(Opcodes.F2I);
				} else if (otherType.getOpcode() == Opcodes.DCMPL
				        || otherType.getOpcode() == Opcodes.DCMPG) {
					subNode = new InsnNode(Opcodes.DSUB);
					castNode = new InsnNode(Opcodes.D2I);
				} else {
					subNode = new InsnNode(Opcodes.ISUB);
					castNode = new InsnNode(Opcodes.ICONST_0);
				}

				MethodInsnNode booleanHelperPush = new MethodInsnNode(
				        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
				        "push", Type.getMethodDescriptor(Type.VOID_TYPE,
				                                         new Type[] { Type.INT_TYPE }));
				InsnNode dupNode = new InsnNode(Opcodes.DUP);
				if (otherType.getOpcode() == Opcodes.INVOKESTATIC
				        || otherType.getOpcode() == Opcodes.INVOKEVIRTUAL) {
					MethodInsnNode min = (MethodInsnNode) otherType;
					if (min.name.equals("convertBooleanToInt")
					        && min.owner.equals(Type.getInternalName(BooleanHelper.class))) {
						ins.insertBefore(startNode, dupNode);
						ins.insertBefore(startNode, booleanHelperPush);
					} else {
						ins.insertBefore(startNode, dupNode);
						ins.insertBefore(startNode, booleanHelperPush);
					}
				} else if (otherType.getOpcode() == Opcodes.LCMP
				        || otherType.getOpcode() == Opcodes.FCMPL
				        || otherType.getOpcode() == Opcodes.DCMPL
				        || otherType.getOpcode() == Opcodes.DCMPG
				        || otherType.getOpcode() == Opcodes.FCMPG) {
					if (otherType.getOpcode() == Opcodes.FCMPG
					        || otherType.getOpcode() == Opcodes.FCMPL) {
						InsnNode dup2Node = new InsnNode(Opcodes.DUP2);
						ins.insertBefore(otherType, dup2Node);
						ins.insertBefore(otherType, subNode);
						ins.insertBefore(otherType, castNode);
						ins.insertBefore(otherType, booleanHelperPush);
					} else if (otherType.getOpcode() == Opcodes.DCMPL
					        || otherType.getOpcode() == Opcodes.DCMPG) {
						MethodInsnNode computeDistance = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "computeDoubleDifference",
						        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {
						                Type.DOUBLE_TYPE, Type.DOUBLE_TYPE }));
						ins.insertBefore(otherType, computeDistance);
						ins.insertBefore(otherType, booleanHelperPush);
						MethodInsnNode pop1 = new MethodInsnNode(Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "popDoubleOperand",
						        Type.getMethodDescriptor(Type.DOUBLE_TYPE, new Type[] {}));
						MethodInsnNode pop2 = new MethodInsnNode(Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "popDoubleOperand",
						        Type.getMethodDescriptor(Type.DOUBLE_TYPE, new Type[] {}));
						ins.insertBefore(otherType, pop1);
						ins.insertBefore(otherType, pop2);
					} else {
						//LCMP
						MethodInsnNode computeDistance = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "computeLongDifference",
						        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {
						                Type.LONG_TYPE, Type.LONG_TYPE }));
						ins.insertBefore(otherType, computeDistance);
						ins.insertBefore(otherType, booleanHelperPush);
						MethodInsnNode pop1 = new MethodInsnNode(Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "popLongOperand",
						        Type.getMethodDescriptor(Type.LONG_TYPE, new Type[] {}));
						MethodInsnNode pop2 = new MethodInsnNode(Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "popLongOperand",
						        Type.getMethodDescriptor(Type.LONG_TYPE, new Type[] {}));
						ins.insertBefore(otherType, pop1);
						ins.insertBefore(otherType, pop2);
					}
				} else {
					log.debug("hallo");
					if (startNode.getOpcode() == Opcodes.IFLE
					        || startNode.getOpcode() == Opcodes.IFLT
					        || startNode.getOpcode() == Opcodes.IFGT
					        || startNode.getOpcode() == Opcodes.IFGE) {
						if (castNode.getOpcode() != Opcodes.ICONST_0) {
							ins.insertBefore(startNode, castNode);
						}
						log.debug("hallo1");
						ins.insertBefore(startNode, dupNode);
						ins.insertBefore(startNode, booleanHelperPush);
					} else if (startNode.getOpcode() == Opcodes.IF_ICMPEQ
					        || startNode.getOpcode() == Opcodes.IF_ICMPNE
					        || startNode.getOpcode() == Opcodes.IF_ICMPLE
					        || startNode.getOpcode() == Opcodes.IF_ICMPGE
					        || startNode.getOpcode() == Opcodes.IF_ICMPGT
					        || startNode.getOpcode() == Opcodes.IF_ICMPLT) {
						InsnNode dup2Node = new InsnNode(Opcodes.DUP2);
						ins.insertBefore(startNode, dup2Node);
						ins.insertBefore(startNode, subNode);
						ins.insertBefore(startNode, booleanHelperPush);
					}
					//if it's not integer, we have to add an cast node

				}

				//change IF node to IFLT
				log.debug("1");
				JumpInsnNode jin = transformJumpNode((JumpInsnNode) startNode);
				ins.insertBefore(startNode, jin);
				ins.remove(startNode);
				resultNode = jin;

				LabelNode jumpLabel = jin.label;
				AbstractInsnNode nextNode = jin.getNext();
				MethodInsnNode booleanHelperPopTrue = new MethodInsnNode(
				        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
				        "popTrue", Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
				MethodInsnNode booleanHelperPopFalse = new MethodInsnNode(
				        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
				        "popFalse",
				        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
				while (nextNode != null && nextNode != jumpLabel) {
					//                    log.debug("ifle next:" + nextNode.getNext().getOpcode());
					if (nextNode.getOpcode() == Opcodes.ICONST_1) {
						ins.insertBefore(nextNode, booleanHelperPopTrue);
						ins.remove(nextNode);
						nextNode = booleanHelperPopTrue;
					} else if (nextNode.getOpcode() == Opcodes.ICONST_0) {
						ins.insertBefore(nextNode, booleanHelperPopFalse);
						ins.remove(nextNode);
						nextNode = booleanHelperPopFalse;

					}
					nextNode = nextNode.getNext();
				}

				StatisticUtil.registerTransformation(StatisticUtil.BooleansByArithmeticOperation);

			}

		}
		return resultNode;
	}

	public static AbstractInsnNode checkAndTransformFlagUsage(AbstractInsnNode startNode,
	        InsnList ins, boolean isBooleanMethod) {
		log.debug("checkAndTransformFlagUsage ...");
		AbstractInsnNode resultNode = startNode;
		boolean completeSimplePredicateStructureDetected = false;
		boolean simpleIReturnStructureDetected = false;
		boolean isFlag = isFlag(resultNode, ins, localVariableIndex);
		if (isFlag) {
			StatisticUtil.registerTransformation(StatisticUtil.BooleanAsFlag);
			log.debug("flag found");
			if (startNode.getOpcode() == Opcodes.IF_ICMPEQ
			        || startNode.getOpcode() == Opcodes.IF_ICMPNE
			        || startNode.getOpcode() == Opcodes.IF_ICMPLE
			        || startNode.getOpcode() == Opcodes.IF_ICMPGE
			        || startNode.getOpcode() == Opcodes.IF_ICMPGT
			        || startNode.getOpcode() == Opcodes.IF_ICMPLT
			        || startNode.getOpcode() == Opcodes.IFLE
			        || startNode.getOpcode() == Opcodes.IFLT
			        || startNode.getOpcode() == Opcodes.IFGE
			        || startNode.getOpcode() == Opcodes.IFGT
			        || startNode.getOpcode() == Opcodes.IFNE
			        || startNode.getOpcode() == Opcodes.IFEQ) {
				AbstractInsnNode iconst0Oriconst1 = startNode.getNext();
				while (iconst0Oriconst1.getOpcode() == -1) {
					iconst0Oriconst1 = iconst0Oriconst1.getNext();
				}
				if (iconst0Oriconst1 != null
				        && iconst0Oriconst1 instanceof InsnNode
				        && (iconst0Oriconst1.getOpcode() == Opcodes.ICONST_0 || iconst0Oriconst1.getOpcode() == Opcodes.ICONST_1)) {
					AbstractInsnNode gotoLabel = iconst0Oriconst1.getNext();
					while (gotoLabel.getOpcode() == -1) {
						gotoLabel = gotoLabel.getNext();
					}
					if (gotoLabel != null && gotoLabel instanceof JumpInsnNode
					        && gotoLabel.getOpcode() == Opcodes.GOTO) {
						AbstractInsnNode falseLabel = gotoLabel.getNext();
						if (falseLabel != null && falseLabel instanceof LabelNode) {
							//                            AbstractInsnNode tempAin = falseLabel.getNext();
							AbstractInsnNode iconst0Oriconst1_2 = falseLabel.getNext();
							while (iconst0Oriconst1_2.getOpcode() == -1) {
								iconst0Oriconst1_2 = iconst0Oriconst1_2.getNext();
							}
							if (iconst0Oriconst1_2 != null
							        && iconst0Oriconst1_2 instanceof InsnNode
							        && (iconst0Oriconst1_2.getOpcode() == Opcodes.ICONST_0 || iconst0Oriconst1_2.getOpcode() == Opcodes.ICONST_1)) {
								AbstractInsnNode storeLabel = iconst0Oriconst1_2.getNext();
								log.debug("hallo");
								if (storeLabel != null && storeLabel instanceof LabelNode) {
									//                                    AbstractInsnNode tempAin_2 = storeLabel.getNext();
									AbstractInsnNode storeNode = storeLabel.getNext();
									while (storeNode.getOpcode() == -1) {
										storeNode = storeNode.getNext();
									}
									//                                    log.debug("hallo2");
									completeSimplePredicateStructureDetected = isLocalBooleanRelated(storeNode,
									                                                                 ins);
									//                                    log.debug("completeSimplePredicateStructureDetected:" + completeSimplePredicateStructureDetected);
								}
							} else {
								//iconst_0 is already changed to addK/subK operations
								if (iconst0Oriconst1_2.getOpcode() == Opcodes.INVOKESTATIC) {
									MethodInsnNode min = (MethodInsnNode) iconst0Oriconst1_2;
									if ((min.name.equals("pop")
									        || min.name.equals("popTrue") || min.name.equals("popFalse"))
									        && min.owner.equals("com/yanchuan/valkyrie/util/BooleanHelper")) {
										completeSimplePredicateStructureDetected = true;
									}
								}
							}
						}
					} else {
						//                    log.debug("23:" + gotoLabel.getOpcode());
						if (gotoLabel != null
						        && (gotoLabel.getOpcode() == Opcodes.IRETURN)
						        && isBooleanMethod) {
							simpleIReturnStructureDetected = true;
							log.debug("simpleIReturnStructureDetected detected");
						}
					}
				}

				JumpInsnNode jin = (JumpInsnNode) startNode;
				if (jin.getOpcode() == Opcodes.IFEQ) {
					jin.setOpcode(Opcodes.IFLE);
					//                    log.debug("flag detected and reformed with IFLT ...");
				} else if (jin.getOpcode() == Opcodes.IFNE) {
					jin.setOpcode(Opcodes.IFGT);
					//                    log.debug("flag detected and reformed with IFGE ...");
				}

				if (simpleIReturnStructureDetected) {

					MethodInsnNode booleanHelperPush = new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "push",
					        Type.getMethodDescriptor(Type.VOID_TYPE,
					                                 new Type[] { Type.INT_TYPE }));
					InsnNode dupNode = new InsnNode(Opcodes.DUP);

					ins.insertBefore(startNode, dupNode);
					ins.insertBefore(startNode, booleanHelperPush);
					LabelNode jumpLabel = jin.label;
					AbstractInsnNode nextNode = jin.getNext();
					MethodInsnNode booleanHelperPopTrue = new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "popTrue",
					        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
					MethodInsnNode booleanHelperPopFalse = new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "popFalse",
					        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));

					while (nextNode != null && nextNode != jumpLabel) {
						//                    log.debug("ifle next:" + nextNode.getNext().getOpcode());
						if (nextNode.getOpcode() == Opcodes.ICONST_1) {
							ins.insertBefore(nextNode, booleanHelperPopTrue);
							ins.remove(nextNode);
							nextNode = booleanHelperPopTrue;
						} else if (nextNode.getOpcode() == Opcodes.ICONST_0) {
							ins.insertBefore(nextNode, booleanHelperPopFalse);
							ins.remove(nextNode);
							nextNode = booleanHelperPopFalse;

						}
						nextNode = nextNode.getNext();
					}

					StatisticUtil.registerTransformation(StatisticUtil.BooleansByBooleanOrMethodPredicate);
				} else if (completeSimplePredicateStructureDetected) {

					MethodInsnNode booleanHelperPush = new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "push",
					        Type.getMethodDescriptor(Type.VOID_TYPE,
					                                 new Type[] { Type.INT_TYPE }));
					InsnNode dupNode = new InsnNode(Opcodes.DUP);

					ins.insertBefore(startNode, dupNode);
					ins.insertBefore(startNode, booleanHelperPush);

					AbstractInsnNode nextNode = jin.getNext();
					//modify the ICONST1/ICONST0 directly after IF
					while (nextNode instanceof LabelNode) {
						nextNode = nextNode.getNext();
						//                    log.debug("skip label node");
					}
					MethodInsnNode booleanHelperPopTrue = new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "popTrue",
					        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
					MethodInsnNode booleanHelperPopFalse = new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "popFalse",
					        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
					while (!(nextNode instanceof LabelNode)) {
						//                    log.debug("ifle next:" + nextNode.getNext().getOpcode());
						if (nextNode.getOpcode() == Opcodes.ICONST_1) {
							ins.insertBefore(nextNode, booleanHelperPopTrue);
							ins.remove(nextNode);
							nextNode = booleanHelperPopTrue;
							//                        log.debug("hehe iconst1 transformed! 1");
						} else if (nextNode.getOpcode() == Opcodes.ICONST_0) {
							ins.insertBefore(nextNode, booleanHelperPopFalse);
							ins.remove(nextNode);
							nextNode = booleanHelperPopFalse;
							//                        log.debug("hehe iconst0 transformed! 1");
						}
						nextNode = nextNode.getNext();
					}
					while (nextNode instanceof LabelNode) {
						nextNode = nextNode.getNext();
						//                    log.debug("skip label node 2");
					}
					//                    nextNode = nextNode.getNext();
					MethodInsnNode booleanHelperPopTrue2 = new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "popTrue",
					        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
					MethodInsnNode booleanHelperPopFalse2 = new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "popFalse",
					        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));

					while (!(nextNode instanceof LabelNode)) {
						if (nextNode.getOpcode() == Opcodes.ICONST_1) {
							ins.insertBefore(nextNode, booleanHelperPopTrue2);
							ins.remove(nextNode);
							nextNode = booleanHelperPopTrue2;
							//                        log.debug("hehe iconst1 transformed! 2");
						} else if (nextNode.getOpcode() == Opcodes.ICONST_0) {
							ins.insertBefore(nextNode, booleanHelperPopFalse2);
							ins.remove(nextNode);
							nextNode = booleanHelperPopFalse2;
							//                        log.debug("hehe iconst0 transformed! 2");
						}
						nextNode = nextNode.getNext();
					}
					StatisticUtil.registerTransformation(StatisticUtil.BooleansByBooleanOrMethodPredicate);
				}

			}
		} else {
			log.debug("hallo2");
			//it could be boolean definition as well
			//            log.debug("flag non detected");
			resultNode = checkAndTransformBooleansByArithmeticOperation(startNode, ins,
			                                                            isBooleanMethod);
		}
		return resultNode;
	}

	public static AbstractInsnNode checkAndTransformEqualityComparison(
	        AbstractInsnNode startNode, InsnList ins, boolean isBooleanMethod) {
		//        log.debug("checkAndTransformEqualityComparison ...");

		AbstractInsnNode resultNode = startNode;
		boolean completeSimplePredicateStructureDetected = false;
		boolean simpleIReturnStructureDetected = false;

		if (startNode.getOpcode() == Opcodes.IF_ACMPNE
		        || startNode.getOpcode() == Opcodes.IF_ACMPEQ) {
			AbstractInsnNode iconst0Oriconst1 = startNode.getNext();
			if (iconst0Oriconst1 != null
			        && iconst0Oriconst1 instanceof InsnNode
			        && (iconst0Oriconst1.getOpcode() == Opcodes.ICONST_0 || iconst0Oriconst1.getOpcode() == Opcodes.ICONST_1)) {
				AbstractInsnNode gotoLabel = iconst0Oriconst1.getNext();
				if (gotoLabel != null && gotoLabel instanceof JumpInsnNode
				        && gotoLabel.getOpcode() == Opcodes.GOTO) {
					AbstractInsnNode falseLabel = gotoLabel.getNext();
					if (falseLabel != null && falseLabel instanceof LabelNode) {

						AbstractInsnNode iconst0Oriconst1_2 = falseLabel.getNext();
						while (iconst0Oriconst1_2.getOpcode() == -1) {
							iconst0Oriconst1_2 = iconst0Oriconst1_2.getNext();
						}
						if (iconst0Oriconst1_2 != null
						        && iconst0Oriconst1_2 instanceof InsnNode
						        && (iconst0Oriconst1_2.getOpcode() == Opcodes.ICONST_0 || iconst0Oriconst1_2.getOpcode() == Opcodes.ICONST_1)) {
							AbstractInsnNode storeLabel = iconst0Oriconst1_2.getNext();
							if (storeLabel != null && storeLabel instanceof LabelNode) {

								AbstractInsnNode storeNode = storeLabel.getNext();
								while (storeNode.getOpcode() == -1) {
									storeNode = storeNode.getNext();
								}
								if (storeNode != null && storeNode instanceof VarInsnNode
								        && storeNode.getOpcode() == Opcodes.ISTORE) {
									VarInsnNode storenode = (VarInsnNode) storeNode;
									if (localVariableIndex.keySet().contains(storenode.var)) {
										completeSimplePredicateStructureDetected = true;
									}
								} else if (storeNode != null
								        && storeNode instanceof FieldInsnNode
								        && (storeNode.getOpcode() == Opcodes.PUTFIELD || storeNode.getOpcode() == Opcodes.PUTSTATIC)) {
									completeSimplePredicateStructureDetected = true;
								} else if (storeNode != null
								        && storeNode instanceof InsnNode
								        && storeNode.getOpcode() == Opcodes.BASTORE) {
									AbstractInsnNode arrayNode = findAndUpdateArrayNode(storeNode,
									                                                    ins,
									                                                    localVariableIndex);
									if (arrayNode.getOpcode() == Opcodes.ALOAD) {
										completeSimplePredicateStructureDetected = true; //assign value to a local boolean array
									} else {
										FieldInsnNode fin = (FieldInsnNode) arrayNode;
										if (checkPackage(fin.owner)) {
											completeSimplePredicateStructureDetected = true;
										}
									}
								}
							}
						}
					}
				} else {
					if (gotoLabel != null && (gotoLabel.getOpcode() == Opcodes.IRETURN)
					        && isBooleanMethod) {
						simpleIReturnStructureDetected = true;
					}
				}
			}
		}
		if (completeSimplePredicateStructureDetected) {
			//            log.debug("completeSimplePredicateStructureDetected found ...");
			MethodInsnNode booleanHelperInvoke = new MethodInsnNode(Opcodes.INVOKESTATIC,
			        Type.getInternalName(BooleanHelper.class), "getObjectEquality",
			        Type.getMethodDescriptor(Type.INT_TYPE,
			                                 new Type[] { Type.getType(Object.class),
			                                         Type.getType(Object.class) }));
			ins.insertBefore(startNode, booleanHelperInvoke);
			InsnNode dupNode = new InsnNode(Opcodes.DUP);
			ins.insertBefore(startNode, dupNode);
			MethodInsnNode booleanHelperPush = new MethodInsnNode(
			        Opcodes.INVOKESTATIC,
			        Type.getInternalName(BooleanHelper.class),
			        "push",
			        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE }));
			ins.insertBefore(startNode, booleanHelperPush);
			JumpInsnNode iflt;
			if (startNode.getOpcode() == Opcodes.IF_ACMPNE) {
				iflt = new JumpInsnNode(Opcodes.IFLE, ((JumpInsnNode) startNode).label);
			} else {
				iflt = new JumpInsnNode(Opcodes.IFGT, ((JumpInsnNode) startNode).label);
			}

			ins.insertBefore(startNode, iflt);
			ins.remove(startNode);

			AbstractInsnNode gotoNode = iflt;
			while (gotoNode.getOpcode() != Opcodes.GOTO) {
				gotoNode = gotoNode.getNext();
			}
			LabelNode gotoLabel = ((JumpInsnNode) gotoNode).label;
			AbstractInsnNode nextNode = iflt.getNext();

			//modify the ICONST1/ICONST0 directly after IF
			while (nextNode instanceof LabelNode) {
				nextNode = nextNode.getNext();
				//                log.debug("skip label node");
			}

			MethodInsnNode booleanHelperPopTrue = new MethodInsnNode(
			        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
			        "popTrue", Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
			MethodInsnNode booleanHelperPopFalse = new MethodInsnNode(
			        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
			        "popFalse", Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
			while (!(nextNode instanceof LabelNode)) {
				//                log.debug("ifle next:" + nextNode.getNext().getOpcode());
				if (nextNode.getOpcode() == Opcodes.ICONST_1) {
					ins.insertBefore(nextNode, booleanHelperPopTrue);
					ins.remove(nextNode);
					nextNode = booleanHelperPopTrue;
					//                    log.debug("hehe iconst1 transformed! 1");
				} else if (nextNode.getOpcode() == Opcodes.ICONST_0) {
					ins.insertBefore(nextNode, booleanHelperPopFalse);
					ins.remove(nextNode);
					nextNode = booleanHelperPopFalse;
					//                    log.debug("hehe iconst0 transformed! 1");
				}
				nextNode = nextNode.getNext();
			}

			while (nextNode instanceof LabelNode) {
				nextNode = nextNode.getNext();
				//                log.debug("skip label node 2");
			}

			MethodInsnNode booleanHelperPopTrue2 = new MethodInsnNode(
			        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
			        "popTrue", Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
			MethodInsnNode booleanHelperPopFalse2 = new MethodInsnNode(
			        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
			        "popFalse", Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));

			//            nextNode = nextNode.getNext();
			while (!(nextNode instanceof LabelNode)) {

				if (nextNode.getOpcode() == Opcodes.ICONST_1) {
					ins.insertBefore(nextNode, booleanHelperPopTrue2);
					ins.remove(nextNode);
					nextNode = booleanHelperPopTrue2;
					//                    log.debug("hehe iconst1 transformed! 2");
				} else if (nextNode.getOpcode() == Opcodes.ICONST_0) {
					ins.insertBefore(nextNode, booleanHelperPopFalse2);
					ins.remove(nextNode);
					nextNode = booleanHelperPopFalse2;
					//                    log.debug("hehe iconst0 transformed! 2");
				}
				nextNode = nextNode.getNext();
			}

			StatisticUtil.registerTransformation(StatisticUtil.BooleansByEqualityComparison);

			resultNode = iflt;
		} else if (simpleIReturnStructureDetected) {

			MethodInsnNode booleanHelperInvoke = new MethodInsnNode(Opcodes.INVOKESTATIC,
			        Type.getInternalName(BooleanHelper.class), "getObjectEquality",
			        Type.getMethodDescriptor(Type.INT_TYPE,
			                                 new Type[] { Type.getType(Object.class),
			                                         Type.getType(Object.class) }));
			ins.insertBefore(startNode, booleanHelperInvoke);
			InsnNode dupNode = new InsnNode(Opcodes.DUP);
			ins.insertBefore(startNode, dupNode);
			MethodInsnNode booleanHelperPush = new MethodInsnNode(
			        Opcodes.INVOKESTATIC,
			        Type.getInternalName(BooleanHelper.class),
			        "push",
			        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE }));
			ins.insertBefore(startNode, booleanHelperPush);
			JumpInsnNode iflt;
			if (startNode.getOpcode() == Opcodes.IF_ACMPNE) {
				iflt = new JumpInsnNode(Opcodes.IFLE, ((JumpInsnNode) startNode).label);
			} else {
				iflt = new JumpInsnNode(Opcodes.IFGT, ((JumpInsnNode) startNode).label);
			}

			ins.insertBefore(startNode, iflt);
			ins.remove(startNode);

			resultNode = iflt;

			LabelNode jumpLabel = iflt.label;
			AbstractInsnNode nextNode = iflt.getNext();

			MethodInsnNode booleanHelperPopTrue = new MethodInsnNode(
			        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
			        "popTrue", Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
			MethodInsnNode booleanHelperPopFalse = new MethodInsnNode(
			        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
			        "popFalse", Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
			while (nextNode != null && nextNode != jumpLabel) {
				//                    log.debug("ifle next:" + nextNode.getNext().getOpcode());
				if (nextNode.getOpcode() == Opcodes.ICONST_1) {
					ins.insertBefore(nextNode, booleanHelperPopTrue);
					ins.remove(nextNode);
					nextNode = booleanHelperPopTrue;
				} else if (nextNode.getOpcode() == Opcodes.ICONST_0) {
					ins.insertBefore(nextNode, booleanHelperPopFalse);
					ins.remove(nextNode);
					nextNode = booleanHelperPopFalse;

				}
				nextNode = nextNode.getNext();
			}

			StatisticUtil.registerTransformation(StatisticUtil.BooleansByEqualityComparison);

		} else {
			if (startNode.getOpcode() == Opcodes.IF_ACMPNE
			        || startNode.getOpcode() == Opcodes.IF_ACMPEQ) {
				//                log.debug("start completeSimplePredicateStructureDetected in conjunctions");
				AbstractInsnNode nextGotoNode = startNode.getNext();
				while (nextGotoNode != null && nextGotoNode.getOpcode() != Opcodes.GOTO) {
					nextGotoNode = nextGotoNode.getNext();
				}

				JumpInsnNode gotoNode = (JumpInsnNode) nextGotoNode;
				if (gotoNode != null) {
					AbstractInsnNode nextStoreLabel = gotoNode.label;
					AbstractInsnNode nodeAfterStoreLabel = nextStoreLabel.getNext();
					while (nodeAfterStoreLabel.getOpcode() == -1) {
						nodeAfterStoreLabel = nodeAfterStoreLabel.getNext();
					}

					if (isLocalBooleanRelated(nodeAfterStoreLabel, ins)) {
						MethodInsnNode booleanHelperInvoke = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "getObjectEquality",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] {
						                                         Type.getType(Object.class),
						                                         Type.getType(Object.class) }));
						ins.insertBefore(startNode, booleanHelperInvoke);
						InsnNode dupNode = new InsnNode(Opcodes.DUP);
						ins.insertBefore(startNode, dupNode);
						MethodInsnNode booleanHelperPush = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class), "push",
						        Type.getMethodDescriptor(Type.VOID_TYPE,
						                                 new Type[] { Type.INT_TYPE }));
						ins.insertBefore(startNode, booleanHelperPush);
						JumpInsnNode iflt;
						if (startNode.getOpcode() == Opcodes.IF_ACMPNE) {
							iflt = new JumpInsnNode(Opcodes.IFLE,
							        ((JumpInsnNode) startNode).label);
						} else {
							iflt = new JumpInsnNode(Opcodes.IFGT,
							        ((JumpInsnNode) startNode).label);
						}

						ins.insertBefore(startNode, iflt);
						ins.remove(startNode);

						resultNode = iflt;
					}

				} else {
					MethodInsnNode booleanHelperInvoke = new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class),
					        "getObjectEquality",
					        Type.getMethodDescriptor(Type.INT_TYPE,
					                                 new Type[] {
					                                         Type.getType(Object.class),
					                                         Type.getType(Object.class) }));
					ins.insertBefore(startNode, booleanHelperInvoke);
					InsnNode dupNode = new InsnNode(Opcodes.DUP);
					ins.insertBefore(startNode, dupNode);
					MethodInsnNode booleanHelperPush = new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "push",
					        Type.getMethodDescriptor(Type.VOID_TYPE,
					                                 new Type[] { Type.INT_TYPE }));
					ins.insertBefore(startNode, booleanHelperPush);
					JumpInsnNode iflt;
					if (startNode.getOpcode() == Opcodes.IF_ACMPNE) {
						iflt = new JumpInsnNode(Opcodes.IFLE,
						        ((JumpInsnNode) startNode).label);
					} else {
						iflt = new JumpInsnNode(Opcodes.IFGT,
						        ((JumpInsnNode) startNode).label);
					}

					ins.insertBefore(startNode, iflt);
					ins.remove(startNode);

					resultNode = iflt;

				}

				StatisticUtil.registerTransformation(StatisticUtil.BooleansByEqualityComparison);
			}

		}
		return resultNode;
	}

	public static AbstractInsnNode checkAndTransformNullityComparison(
	        AbstractInsnNode startNode, InsnList ins, boolean isBooleanMethod) {
		//        log.debug("checkAndTransformNullityComparison ...");
		AbstractInsnNode resultNode = startNode;
		boolean completeSimplePredicateStructureDetected = false;
		boolean simpleIReturnStructureDetected = false;

		if (startNode.getOpcode() == Opcodes.IFNULL
		        || startNode.getOpcode() == Opcodes.IFNONNULL) {
			AbstractInsnNode iconst0Oriconst1 = startNode.getNext();
			while (iconst0Oriconst1.getOpcode() == -1) {
				iconst0Oriconst1 = iconst0Oriconst1.getNext();
			}
			if (iconst0Oriconst1 != null
			        && iconst0Oriconst1 instanceof InsnNode
			        && (iconst0Oriconst1.getOpcode() == Opcodes.ICONST_0 || iconst0Oriconst1.getOpcode() == Opcodes.ICONST_1)) {
				AbstractInsnNode gotoLabel = iconst0Oriconst1.getNext();
				while (gotoLabel.getOpcode() == -1) {
					gotoLabel = gotoLabel.getNext();
				}
				if (gotoLabel != null && gotoLabel instanceof JumpInsnNode
				        && gotoLabel.getOpcode() == Opcodes.GOTO) {
					AbstractInsnNode falseLabel = gotoLabel.getNext();
					if (falseLabel != null && falseLabel instanceof LabelNode) {

						AbstractInsnNode iconst0Oriconst1_2 = falseLabel.getNext();
						while (iconst0Oriconst1_2.getOpcode() == -1) {
							iconst0Oriconst1_2 = iconst0Oriconst1_2.getNext();
						}
						if (iconst0Oriconst1_2 != null
						        && iconst0Oriconst1_2 instanceof InsnNode
						        && (iconst0Oriconst1_2.getOpcode() == Opcodes.ICONST_0 || iconst0Oriconst1_2.getOpcode() == Opcodes.ICONST_1)) {
							AbstractInsnNode storeLabel = iconst0Oriconst1_2.getNext();
							if (storeLabel != null && storeLabel instanceof LabelNode) {

								AbstractInsnNode storeNode = storeLabel.getNext();
								while (storeNode.getOpcode() == -1) {
									storeNode = storeNode.getNext();
								}

								completeSimplePredicateStructureDetected = isLocalBooleanRelated(storeNode,
								                                                                 ins);
							}
						} else {
							//iconst_0 is already changed to addK/subK operations
							if (iconst0Oriconst1_2.getOpcode() == Opcodes.INVOKESTATIC) {
								MethodInsnNode min = (MethodInsnNode) iconst0Oriconst1_2;
								if ((min.name.equals("pop") || min.name.equals("popTrue") || min.name.equals("popFalse"))
								        && min.owner.equals("com/yanchuan/valkyrie/util/BooleanHelper")) {
									completeSimplePredicateStructureDetected = true;
								}
							}
						}
					}
				} else {
					if (gotoLabel != null && (gotoLabel.getOpcode() == Opcodes.IRETURN)
					        && isBooleanMethod) {
						simpleIReturnStructureDetected = true;
					}
				}
			}
		}
		if (completeSimplePredicateStructureDetected) {
			//            log.debug("completeSimplePredicateStructureDetected found ...");

			AbstractInsnNode gotoNode = startNode;
			while (gotoNode.getOpcode() != Opcodes.GOTO) {
				gotoNode = gotoNode.getNext();
			}
			LabelNode gotoLabel = ((JumpInsnNode) gotoNode).label;

			AbstractInsnNode nextNode = startNode.getNext();

			//modify the ICONST1/ICONST0 directly after IF
			while (nextNode instanceof LabelNode) {
				nextNode = nextNode.getNext();
				//                log.debug("skip label node");
			}

			MethodInsnNode booleanHelperPopTrue = new MethodInsnNode(
			        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
			        "popTrue", Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
			MethodInsnNode booleanHelperPopFalse = new MethodInsnNode(
			        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
			        "popFalse", Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
			while (!(nextNode instanceof LabelNode)) {
				//                log.debug("ifle next:" + nextNode.getNext().getOpcode());
				if (nextNode.getOpcode() == Opcodes.ICONST_1) {
					ins.insertBefore(nextNode, booleanHelperPopTrue);
					ins.remove(nextNode);
					nextNode = booleanHelperPopTrue;
					//                    log.debug("hehe iconst1 transformed! 1");
				} else if (nextNode.getOpcode() == Opcodes.ICONST_0) {
					ins.insertBefore(nextNode, booleanHelperPopFalse);
					ins.remove(nextNode);
					nextNode = booleanHelperPopFalse;
					//                    log.debug("hehe iconst0 transformed! 1");
				}
				nextNode = nextNode.getNext();
			}

			while (nextNode instanceof LabelNode) {
				nextNode = nextNode.getNext();
				//                log.debug("skip label node 2");
			}
			MethodInsnNode booleanHelperPopTrue2 = new MethodInsnNode(
			        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
			        "popTrue", Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
			MethodInsnNode booleanHelperPopFalse2 = new MethodInsnNode(
			        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
			        "popFalse", Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));

			//            nextNode = nextNode.getNext();
			while (!(nextNode instanceof LabelNode)) {

				if (nextNode.getOpcode() == Opcodes.ICONST_1) {
					ins.insertBefore(nextNode, booleanHelperPopTrue2);
					ins.remove(nextNode);
					nextNode = booleanHelperPopTrue2;
					//                    log.debug("hehe iconst1 transformed! 2");
				} else if (nextNode.getOpcode() == Opcodes.ICONST_0) {
					ins.insertBefore(nextNode, booleanHelperPopFalse2);
					ins.remove(nextNode);
					nextNode = booleanHelperPopFalse2;
					//                    log.debug("hehe iconst0 transformed! 2");
				}
				nextNode = nextNode.getNext();
			}

			/*
			AbstractInsnNode nextLabel = gotoLabel.getNext();
			while (!(nextLabel instanceof LabelNode)) {
			    nextLabel = nextLabel.getNext();
			}
			//found a label for debug purpose
			LabelNode ln = new LabelNode();
			JumpInsnNode gotoNextLabel = new JumpInsnNode(Opcodes.GOTO, ln);
			ins.insertBefore(nextLabel, gotoNextLabel);
			ins.insert(nextLabel, ln);
			 */

			StatisticUtil.registerTransformation(StatisticUtil.BooleansByNullityComparison);

		} else if (simpleIReturnStructureDetected) {

			JumpInsnNode ifgt = (JumpInsnNode) startNode;

			resultNode = ifgt;

			LabelNode jumpLabel = ifgt.label;
			AbstractInsnNode nextNode = ifgt.getNext();

			MethodInsnNode booleanHelperPopTrue = new MethodInsnNode(
			        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
			        "popTrue", Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
			MethodInsnNode booleanHelperPopFalse = new MethodInsnNode(
			        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
			        "popFalse", Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {}));
			while (nextNode != null && nextNode != jumpLabel) {
				//                    log.debug("ifle next:" + nextNode.getNext().getOpcode());
				if (nextNode.getOpcode() == Opcodes.ICONST_1) {
					ins.insertBefore(nextNode, booleanHelperPopTrue);
					ins.remove(nextNode);
					nextNode = booleanHelperPopTrue;
				} else if (nextNode.getOpcode() == Opcodes.ICONST_0) {
					ins.insertBefore(nextNode, booleanHelperPopFalse);
					ins.remove(nextNode);
					nextNode = booleanHelperPopFalse;
				}
				nextNode = nextNode.getNext();
			}
			StatisticUtil.registerTransformation(StatisticUtil.BooleansByNullityComparison);

		} else {
			if (startNode.getOpcode() == Opcodes.IFNONNULL
			        || startNode.getOpcode() == Opcodes.IFNULL) {
				//                log.debug("start isEqualityComparison in conjunctions");
				AbstractInsnNode nextGotoNode = startNode.getNext();
				while (nextGotoNode != null && nextGotoNode.getOpcode() != Opcodes.GOTO) {
					nextGotoNode = nextGotoNode.getNext();
				}

				JumpInsnNode gotoNode = (JumpInsnNode) nextGotoNode;
				if (gotoNode != null) {
					AbstractInsnNode nextStoreLabel = gotoNode.label;
					AbstractInsnNode nodeAfterStoreLabel = nextStoreLabel.getNext();
					while (nodeAfterStoreLabel.getOpcode() == -1) {
						nodeAfterStoreLabel = nodeAfterStoreLabel.getNext();
					}
					if (isLocalBooleanRelated(nodeAfterStoreLabel, ins)) {

					}

				} else {

				}

			}

		}
		return resultNode;
	}

	public static AbstractInsnNode checkAndTransformInstanceof(
	        AbstractInsnNode startNode, InsnList ins,
	        Map<Integer, String> localVariableIndex) {
		//        log.debug("checkAndTransformInstanceof ...");
		AbstractInsnNode resultNode = startNode;
		boolean completeSimplePredicateStructureDetected = false;
		boolean simpleIReturnStructureDetected = false;

		if (startNode.getOpcode() == Opcodes.INSTANCEOF) {
			AbstractInsnNode storeNode = startNode.getNext();
			completeSimplePredicateStructureDetected = isLocalBooleanRelated(storeNode,
			                                                                 ins);
		}

		if (completeSimplePredicateStructureDetected) {
			log.debug("instanceof detected");
			TypeInsnNode instanceofNode = (TypeInsnNode) startNode;
			LdcInsnNode lin = new LdcInsnNode(Type.getType("L" + instanceofNode.desc
			        + ";"));
			ins.insertBefore(startNode, lin);
			MethodInsnNode booleanHelperIsInstanceOf = new MethodInsnNode(
			        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
			        "isInstanceOf", Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {
			                Type.getType(Object.class), Type.getType(Class.class) }));
			ins.insertBefore(startNode, booleanHelperIsInstanceOf);
			ins.remove(startNode);
			resultNode = booleanHelperIsInstanceOf;

			StatisticUtil.registerTransformation(StatisticUtil.BooleansByInstanceof);
		} else {
			log.debug("non complete simple instanceof sequence detected");
			AbstractInsnNode nextGotoNode = startNode.getNext();
			//            log.debug(nextGotoNode.getOpcode());
			while (nextGotoNode != null && !(nextGotoNode instanceof JumpInsnNode)) {
				nextGotoNode = nextGotoNode.getNext();
			}

			JumpInsnNode gotoNode = (JumpInsnNode) nextGotoNode;
			if (gotoNode != null) {
				log.debug("1231231:" + gotoNode.getOpcode());
				AbstractInsnNode nextStoreLabel = gotoNode.label;
				AbstractInsnNode nodeAfterStoreLabel = nextStoreLabel.getNext();
				while (nodeAfterStoreLabel.getOpcode() == -1) {
					nodeAfterStoreLabel = nodeAfterStoreLabel.getNext();
				}
				//                log.debug("hehe:" + nodeAfterStoreLabel.getOpcode());
				//                if (isLocalBooleanRelated(nodeAfterStoreLabel, ins)) {
				TypeInsnNode instanceofNode = (TypeInsnNode) startNode;
				LdcInsnNode lin = new LdcInsnNode(Type.getType("L" + instanceofNode.desc
				        + ";"));
				ins.insertBefore(startNode, lin);
				MethodInsnNode booleanHelperIsInstanceOf = new MethodInsnNode(
				        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
				        "isInstanceOf",
				        Type.getMethodDescriptor(Type.INT_TYPE,
				                                 new Type[] { Type.getType(Object.class),
				                                         Type.getType(Class.class) }));
				ins.insertBefore(startNode, booleanHelperIsInstanceOf);
				InsnNode dupNode = new InsnNode(Opcodes.DUP);
				ins.insertBefore(startNode, dupNode);
				MethodInsnNode booleanHelperPush = new MethodInsnNode(
				        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
				        "push", Type.getMethodDescriptor(Type.VOID_TYPE,
				                                         new Type[] { Type.INT_TYPE }));
				ins.insertBefore(startNode, booleanHelperPush);

				//change next IFNE/IFEQ as well
				AbstractInsnNode ifneNode = startNode.getNext();
				ins.remove(startNode);

				if (ifneNode.getOpcode() == Opcodes.IFNE) {

					JumpInsnNode ifle = new JumpInsnNode(Opcodes.IFGT,
					        ((JumpInsnNode) ifneNode).label);
					ins.insertBefore(ifneNode, ifle);
					ins.remove(ifneNode);
					resultNode = ifle;
				} else if (ifneNode.getOpcode() == Opcodes.IFEQ) {

					JumpInsnNode ifgt = new JumpInsnNode(Opcodes.IFLE,
					        ((JumpInsnNode) ifneNode).label);
					ins.insertBefore(ifneNode, ifgt);
					ins.remove(ifneNode);
					resultNode = ifgt;
				} else {
					//                    log.info(ifneNode.getOpcode());
					resultNode = booleanHelperPush;
				}
				//                }
			} else {

				TypeInsnNode instanceofNode = (TypeInsnNode) startNode;
				LdcInsnNode lin = new LdcInsnNode(Type.getType("L" + instanceofNode.desc
				        + ";"));
				ins.insertBefore(startNode, lin);
				MethodInsnNode booleanHelperIsInstanceOf = new MethodInsnNode(
				        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
				        "isInstanceOf",
				        Type.getMethodDescriptor(Type.INT_TYPE,
				                                 new Type[] { Type.getType(Object.class),
				                                         Type.getType(Class.class) }));
				ins.insertBefore(startNode, booleanHelperIsInstanceOf);
				InsnNode dupNode = new InsnNode(Opcodes.DUP);
				ins.insertBefore(startNode, dupNode);
				MethodInsnNode booleanHelperPush = new MethodInsnNode(
				        Opcodes.INVOKESTATIC, Type.getInternalName(BooleanHelper.class),
				        "push", Type.getMethodDescriptor(Type.VOID_TYPE,
				                                         new Type[] { Type.INT_TYPE }));
				ins.insertBefore(startNode, booleanHelperPush);
				AbstractInsnNode ifneNode = startNode.getNext();
				ins.remove(startNode);
				resultNode = ifneNode;

			}
			StatisticUtil.registerTransformation(StatisticUtil.BooleansByInstanceof);

		}
		return resultNode;
	}

	public static boolean isFlag(AbstractInsnNode node, InsnList ins,
	        Map<Integer, String> localVariableIndex) {
		boolean result = false;
		if (node instanceof JumpInsnNode) {
			JumpInsnNode jin = (JumpInsnNode) node;
			if (jin.getOpcode() == Opcodes.IFEQ || jin.getOpcode() == Opcodes.IFNE) {
				//check previous node

				AbstractInsnNode an = jin.getPrevious();
				if (an instanceof VarInsnNode) {
					/*
					   flag:
					   boolean a=false;
					   if(a)
					*/
					VarInsnNode vin = (VarInsnNode) an;
					if (localVariableIndex.containsKey(vin.var)) {
						result = true;
					}
				} else if (an instanceof FieldInsnNode) {
					/*
					   private boolean flag=false;
					   if(flag)
					*/
					FieldInsnNode fin = (FieldInsnNode) an;
					String classNameWithDots = fin.owner.replace('/', '.');
					boolean isBooleanField = false;
					//                    log.debug("my field:" + fin.name + " desc:" + fin.desc);
					if (checkPackage(fin.owner)) {
						if (fieldsMap.get(classNameWithDots).contains(fin.name)) {
							isBooleanField = true;
						}
					} else {
						if (fin.desc.equals("Z")) {
							isBooleanField = true;
						}
					}

					if ((fin.getOpcode() == Opcodes.GETFIELD || fin.getOpcode() == Opcodes.GETSTATIC)
					        && isBooleanField) {
						result = true;
					}
				} else if (an instanceof MethodInsnNode) {
					/*
					   if(booleanMethod())

					   or Boolean v=false; if(v)
					*/
					MethodInsnNode min = (MethodInsnNode) an;
					String classNameWithDots = min.owner.replace('/', '.');

					String methodSignature = min.name + "|" + min.desc;
					boolean isBooleanMethod = false;

					if (min.name.endsWith("convertBooleanToInt")
					        && classNameWithDots.endsWith("com.yanchuanli.valkyrie.util.BooleanHelper")) {
						isBooleanMethod = true;
					} else if (checkPackage(min.owner)) {
						if (min.name.contains("valkyrie")) {
							log.debug("check signature:" + methodSignature + "of class:"
							        + classNameWithDots);
							MethodUnit mu = reverseMethodsMap.get(methodSignature);
							isBooleanMethod = mu.isBooleanReturnType();
						}
					} else {
						if (min.desc.endsWith("Z")) {
							isBooleanMethod = true;
						}
					}

					if ((min.getOpcode() == Opcodes.INVOKESPECIAL
					        || min.getOpcode() == Opcodes.INVOKESTATIC || min.getOpcode() == Opcodes.INVOKEVIRTUAL)
					        && isBooleanMethod) {
						result = true;
					}
				} else if (an instanceof InsnNode) {
					if (an.getOpcode() == Opcodes.IALOAD) {
						AbstractInsnNode arrayNode = findAndUpdateArrayNode(an, ins,
						                                                    localVariableIndex);
						if (arrayNode.getOpcode() == Opcodes.ALOAD) {
							//assign value to a local boolean array
							result = true;
						} else if (arrayNode.getOpcode() == Opcodes.GETFIELD
						        || arrayNode.getOpcode() == Opcodes.GETSTATIC) {
							FieldInsnNode fin = (FieldInsnNode) arrayNode;
							if (TransformationHelper.checkPackage(fin.owner)) {
								result = true;
							}
						}
					}
				}

			}
		}
		return result;
	}

	public static AbstractInsnNode findAndUpdateArrayNode(AbstractInsnNode bastoreNode,
	        InsnList ins, Map<Integer, String> localVariableIndex) {
		AbstractInsnNode resultNode = bastoreNode;
		boolean baload = false; // baload load the array element as the second operand
		boolean arrayNodeFound = false;
		AbstractInsnNode predecessor = bastoreNode;
		AbstractInsnNode baloadNode = null;
		while (!arrayNodeFound) {
			predecessor = predecessor.getPrevious();
			if (predecessor != null) {
				if (predecessor.getOpcode() == Opcodes.BALOAD) {
					baloadNode = predecessor;
					baload = true;
				} else if (baload
				        && (predecessor.getOpcode() == Opcodes.ALOAD
				                || predecessor.getOpcode() == Opcodes.GETFIELD || predecessor.getOpcode() == Opcodes.GETSTATIC)) {
					if (predecessor.getOpcode() == Opcodes.ALOAD) {
						VarInsnNode vin = (VarInsnNode) predecessor;
						if (localVariableIndex.containsKey(vin.var)) {
							baload = false;
							InsnNode in = new InsnNode(Opcodes.IALOAD);
							ins.insertBefore(baloadNode, in);
							ins.remove(baloadNode);
						}
					} else if (predecessor.getOpcode() == Opcodes.GETFIELD
					        || predecessor.getOpcode() == Opcodes.GETSTATIC) {
						FieldInsnNode fin = (FieldInsnNode) predecessor;
						if (fin.desc.endsWith("[Z") || fin.desc.endsWith("[I")) {
							baload = false;
							if (checkPackage(fin.owner)) {
								InsnNode in = new InsnNode(Opcodes.IALOAD);
								ins.insertBefore(baloadNode, in);
								ins.remove(baloadNode);
							}
						}

					}
				} else if (!baload
				        && (predecessor.getOpcode() == Opcodes.ALOAD
				                || predecessor.getOpcode() == Opcodes.GETFIELD
				                || predecessor.getOpcode() == Opcodes.GETSTATIC || predecessor.getOpcode() == Opcodes.NEWARRAY)) {
					arrayNodeFound = true;
					if (predecessor.getOpcode() == Opcodes.ALOAD) {
						VarInsnNode vin = (VarInsnNode) predecessor;
						if (localVariableIndex.containsKey(vin.var)) {
							resultNode = predecessor;
						}
					} else if (predecessor.getOpcode() == Opcodes.GETFIELD
					        || predecessor.getOpcode() == Opcodes.GETSTATIC) {
						resultNode = predecessor;
					} else if (predecessor.getOpcode() == Opcodes.NEWARRAY) {
						resultNode = predecessor;
					}
				}

			} else {
				arrayNodeFound = true;
			}
		}
		return resultNode;
	}

	public static AbstractInsnNode findAndUpdateFalseinArray(AbstractInsnNode storeNode,
	        InsnList ins, Map<Integer, String> localVariableIndex) {
		AbstractInsnNode resultNode = storeNode;
		boolean isTwoDimensionalArray = false;
		boolean startNodeFound = false;
		AbstractInsnNode predecessor = storeNode;

		if (predecessor.getOpcode() == Opcodes.IASTORE
		        || storeNode.getOpcode() == Opcodes.AASTORE
		        || storeNode.getOpcode() == Opcodes.BASTORE) {
			if (storeNode.getOpcode() == Opcodes.AASTORE) {
				isTwoDimensionalArray = true;
			}
			if (predecessor.getOpcode() == Opcodes.BASTORE) {
				InsnNode iastoreNode = new InsnNode(Opcodes.IASTORE);
				ins.insertBefore(predecessor, iastoreNode);
				ins.remove(predecessor);
				predecessor = iastoreNode;
			}
			while (!startNodeFound) {
				predecessor = predecessor.getPrevious();
				if (isTwoDimensionalArray && predecessor.getOpcode() == Opcodes.ANEWARRAY) {
					startNodeFound = true;
					TypeInsnNode manai = (TypeInsnNode) predecessor;
					manai.desc = manai.desc.replaceAll("Z", "I");
				} else if (!isTwoDimensionalArray
				        && predecessor.getOpcode() == Opcodes.NEWARRAY) {
					startNodeFound = true;
					IntInsnNode iin = new IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_INT);
					ins.insertBefore(predecessor, iin);
					ins.remove(predecessor);
					predecessor = iin;
				} else if (isTwoDimensionalArray
				        && predecessor.getOpcode() == Opcodes.NEWARRAY) {
					IntInsnNode iin = new IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_INT);
					ins.insertBefore(predecessor, iin);
					ins.remove(predecessor);
					predecessor = iin;
				} else if (predecessor.getOpcode() == Opcodes.ICONST_0) {
					AbstractInsnNode bastoreNode = predecessor.getNext();
					AbstractInsnNode indexNode = predecessor.getPrevious();
					//                    log.debug("hallo1");
					if (bastoreNode.getOpcode() == Opcodes.IASTORE
					        && isIntRelatedNode(indexNode)) {
						AbstractInsnNode minusone = getIntNode(-1);
						ins.insert(indexNode, minusone);
						ins.remove(predecessor);
						predecessor = indexNode;
					} else {
						//                        log.debug("hallo2");
						//                        log.debug(bastoreNode.getOpcode());
					}
				} else if (predecessor.getOpcode() == Opcodes.BASTORE) {
					InsnNode iastoreNode = new InsnNode(Opcodes.IASTORE);
					ins.insertBefore(predecessor, iastoreNode);
					ins.remove(predecessor);
					predecessor = iastoreNode;
				}

			}

		}
		return resultNode;
	}

	//check if one boolean usage is originated within the test package

	public static boolean checkPackage(String owner) {
		String[] ownerPackage = owner.split("/");
		boolean result = true;
		if (ownerPackage.length > testPackage.length) {
			for (int i = 0; i < testPackage.length; i++) {
				if (!testPackage[i].equals(ownerPackage[i])) {
					result = false;
				}
			}
		} else {
			result = false;
		}

		if (owner.contains("yanchuan")) {
			result = true;
		}
		return result;
	}

	public static boolean checkPackageWithDotName(String owner) {
		String[] ownerPackage = owner.split("\\.");
		boolean result = true;
		if (ownerPackage.length > testPackage.length) {
			for (int i = 0; i < testPackage.length; i++) {
				if (!testPackage[i].equals(ownerPackage[i])) {
					result = false;
				}
			}
		} else {
			result = false;
		}

		if (owner.contains("yanchuan")) {
			result = true;
		}
		return result;
	}

	public static boolean isIntRelatedNode(AbstractInsnNode node) {
		return node.getOpcode() == Opcodes.ICONST_0
		        || node.getOpcode() == Opcodes.ICONST_1
		        || node.getOpcode() == Opcodes.ICONST_2
		        || node.getOpcode() == Opcodes.ICONST_3
		        || node.getOpcode() == Opcodes.ICONST_4
		        || node.getOpcode() == Opcodes.ICONST_5
		        || node.getOpcode() == Opcodes.ICONST_M1
		        || node.getOpcode() == Opcodes.BIPUSH;
	}

	public static int getInt(AbstractInsnNode intNode) {
		int result = 0;
		if (intNode.getOpcode() == Opcodes.ICONST_0) {
			result = 0;
		} else if (intNode.getOpcode() == Opcodes.ICONST_1) {
			result = 1;
		} else if (intNode.getOpcode() == Opcodes.ICONST_2) {
			result = 2;
		} else if (intNode.getOpcode() == Opcodes.ICONST_3) {
			result = 3;
		} else if (intNode.getOpcode() == Opcodes.ICONST_4) {
			result = 4;
		} else if (intNode.getOpcode() == Opcodes.ICONST_5) {
			result = 5;
		} else if (intNode.getOpcode() == Opcodes.ICONST_M1) {
			result = -1;
		} else if (intNode.getOpcode() == Opcodes.BIPUSH) {
			IntInsnNode iin = (IntInsnNode) intNode;
			result = iin.operand;
		}
		return result;
	}

	public static AbstractInsnNode getIntNode(int i) {
		AbstractInsnNode resultNode;
		if (i == 0) {
			resultNode = new InsnNode(Opcodes.ICONST_0);
		} else if (i == 1) {
			resultNode = new InsnNode(Opcodes.ICONST_1);
		} else if (i == 2) {
			resultNode = new InsnNode(Opcodes.ICONST_2);
		} else if (i == 3) {
			resultNode = new InsnNode(Opcodes.ICONST_3);
		} else if (i == 4) {
			resultNode = new InsnNode(Opcodes.ICONST_4);
		} else if (i == 5) {
			resultNode = new InsnNode(Opcodes.ICONST_5);
		} else if (i == -1) {
			resultNode = new InsnNode(Opcodes.ICONST_M1);
		} else {
			resultNode = new IntInsnNode(Opcodes.BIPUSH, i);
		}
		return resultNode;
	}

	public static boolean isLocalBooleanRelated(AbstractInsnNode storeNode, InsnList ins) {
		boolean result = false;
		if (storeNode != null && storeNode.getOpcode() == Opcodes.ISTORE) {
			VarInsnNode storenode = (VarInsnNode) storeNode;
			log.debug(storenode.getOpcode() + " " + storenode.var);
			if (localVariableIndex.keySet().contains(storenode.var)) {
				result = true;
			}
		} else if (storeNode != null
		        && (storeNode.getOpcode() == Opcodes.PUTFIELD || storeNode.getOpcode() == Opcodes.PUTSTATIC)) {
			FieldInsnNode fin = (FieldInsnNode) storeNode;
			if (checkPackage(fin.owner)) {
				result = true;
			}
		} else if (storeNode != null
		        && (storeNode.getOpcode() == Opcodes.IASTORE || storeNode.getOpcode() == Opcodes.BASTORE)) {
			AbstractInsnNode arrayNode = findAndUpdateArrayNode(storeNode, ins,
			                                                    localVariableIndex);
			/*
			     boolean[] a = { !(x < 5), true };
			     a[1] = true;

			      ALOAD 2: a
			      ICONST_1
			      ICONST_1
			      BASTORE
			 */
			if (arrayNode.getOpcode() == Opcodes.ALOAD) {
				result = true; //assign value to a local boolean array
			} else if (arrayNode instanceof FieldInsnNode) {
				FieldInsnNode fin = (FieldInsnNode) arrayNode;
				if (checkPackage(fin.owner)) {
					result = true;
				}

			} else {
				//can't find the array declarion or it's simply a NEWARRAY
				//                log.debug("finding ASTORE...");
				AbstractInsnNode astoreNode = storeNode;
				while (astoreNode.getOpcode() != Opcodes.ASTORE) {
					astoreNode = astoreNode.getNext();
				}
				VarInsnNode vin = (VarInsnNode) astoreNode;
				if (localVariableIndex.containsKey(vin.var)) {
					result = true;
				}

			}
		} else if (storeNode != null && storeNode.getOpcode() == Opcodes.IRETURN) {
			result = true;
		}
		return result;
	}

	public static JumpInsnNode transformJumpNode(JumpInsnNode startNode) {
		AbstractInsnNode predecessor = startNode.getPrevious();
		JumpInsnNode jin = new JumpInsnNode(startNode.getOpcode(), startNode.label);
		//        log.debug("transformJumpNode:predecessor:" + predecessor.getOpcode());
		//        log.debug("transformJumpNode:startnode:" + startNode.getOpcode());

		if (startNode.getOpcode() == Opcodes.IFNE) {
			if (predecessor.getOpcode() == Opcodes.INVOKESTATIC
			        || predecessor.getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode min = (MethodInsnNode) predecessor;
				if (min.owner.equals(Type.getInternalName(BooleanHelper.class))) {
					jin = new JumpInsnNode(Opcodes.IFGT, startNode.label);
				}
			}
		} else if (startNode.getOpcode() == Opcodes.IFEQ) {
			if (predecessor.getOpcode() == Opcodes.INVOKESTATIC
			        || predecessor.getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode min = (MethodInsnNode) predecessor;
				//                log.debug("invokestatic found before ifeq");
				if (min.owner.equals(Type.getInternalName(BooleanHelper.class))) {
					jin = new JumpInsnNode(Opcodes.IFLE, startNode.label);
					//                    log.debug("iflt added");
				}
			}
		} else if (startNode.getOpcode() == Opcodes.IFLE) {
			if (predecessor.getOpcode() == Opcodes.INVOKESTATIC
			        || predecessor.getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode min = (MethodInsnNode) predecessor;
				if (min.owner.equals(Type.getInternalName(BooleanHelper.class))) {
					jin = new JumpInsnNode(Opcodes.IFLE, startNode.label);
				}
			}
		} else if (startNode.getOpcode() == Opcodes.IFLT) {
			if (predecessor.getOpcode() == Opcodes.INVOKESTATIC
			        || predecessor.getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode min = (MethodInsnNode) predecessor;
				if (min.owner.equals(Type.getInternalName(BooleanHelper.class))) {
					jin = new JumpInsnNode(Opcodes.IFLT, startNode.label);
				}
			}
		} else if (startNode.getOpcode() == Opcodes.IFGT) {
			if (predecessor.getOpcode() == Opcodes.INVOKESTATIC
			        || predecessor.getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode min = (MethodInsnNode) predecessor;
				if (min.owner.equals(Type.getInternalName(BooleanHelper.class))) {
					jin = new JumpInsnNode(Opcodes.IFGT, startNode.label);
				}
			}
		} else if (startNode.getOpcode() == Opcodes.IFGE) {
			if (predecessor.getOpcode() == Opcodes.INVOKESTATIC
			        || predecessor.getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode min = (MethodInsnNode) predecessor;
				if (min.owner.equals(Type.getInternalName(BooleanHelper.class))) {
					jin = new JumpInsnNode(Opcodes.IFGE, startNode.label);
				}
			}
		} else if (startNode.getOpcode() == Opcodes.IF_ICMPLE) {
			if (predecessor.getOpcode() == Opcodes.INVOKESTATIC
			        || predecessor.getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode min = (MethodInsnNode) predecessor;
				if (min.owner.equals(Type.getInternalName(BooleanHelper.class))) {
					//                    jin = new JumpInsnNode(Opcodes.IFLE, startNode.label);
				}
			}
		} else if (startNode.getOpcode() == Opcodes.IF_ICMPLT) {
			if (predecessor.getOpcode() == Opcodes.INVOKESTATIC
			        || predecessor.getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode min = (MethodInsnNode) predecessor;
				if (min.owner.equals(Type.getInternalName(BooleanHelper.class))) {
					//                    jin = new JumpInsnNode(Opcodes.IFLT, startNode.label);
				}
			}
		} else if (startNode.getOpcode() == Opcodes.IF_ICMPGE) {
			//            jin = new JumpInsnNode(Opcodes.IFGE, startNode.label);
		} else if (startNode.getOpcode() == Opcodes.IF_ICMPGT) {
			if (predecessor.getOpcode() == Opcodes.INVOKESTATIC
			        || predecessor.getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode min = (MethodInsnNode) predecessor;
				if (min.owner.equals(Type.getInternalName(BooleanHelper.class))) {
					//                    jin = new JumpInsnNode(Opcodes.IFGT, startNode.label);
				}
			}
		} else if (startNode.getOpcode() == Opcodes.IF_ICMPNE) {
			if (predecessor.getOpcode() == Opcodes.INVOKESTATIC
			        || predecessor.getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode min = (MethodInsnNode) predecessor;
				if (min.owner.equals(Type.getInternalName(BooleanHelper.class))) {
					//                    jin = new JumpInsnNode(Opcodes.IFNE, startNode.label);
				}
			}
		} else if (startNode.getOpcode() == Opcodes.IF_ICMPEQ) {
			if (predecessor.getOpcode() == Opcodes.INVOKESTATIC
			        || predecessor.getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode min = (MethodInsnNode) predecessor;
				if (min.owner.equals(Type.getInternalName(BooleanHelper.class))) {
					//                    jin = new JumpInsnNode(Opcodes.IFEQ, startNode.label);
				}
			}
		}
		return jin;
	}

	public static boolean valkyrieMethodExist(String classname, String methodSignature) {
		boolean result = false;
		if (methodsMap.containsKey(classname)) {
			Map<String, MethodUnit> methods = methodsMap.get(classname);
			for (String s : methods.keySet()) {
				MethodUnit mu = methods.get(s);
				if (mu.getNewName().equals(methodSignature)) {
					result = true;
				}
			}
		}
		return result;
	}

	public static boolean hasValkyrieMethod(String classname, String methodSignature) {
		boolean result = false;
		if (methodsMap.containsKey(classname)) {
			Map<String, MethodUnit> methods = methodsMap.get(classname);
			for (String s : methods.keySet()) {
				MethodUnit mu = methods.get(s);
				if (mu.getOriginalName().equals(methodSignature)) {
					result = true;
				}
			}
		}
		return result;
	}

	public static MethodUnit getMethodUnit(String classname, String methodSignature) {
		MethodUnit result = null;
		if (methodsMap.containsKey(classname)) {
			Map<String, MethodUnit> methods = methodsMap.get(classname);
			for (String s : methods.keySet()) {
				MethodUnit mu = methods.get(s);
				if (mu.getNewName().equals(methodSignature)) {
					result = mu;
				}
			}
		}

		if (result == null) {
			log.debug("start find superclasses");
			Map<String, Set<String>> superclasses = findAllSuperClassesWithMethods(classname);
			//todo : build a reverse hash
			String origin = findMethodOrigin(methodSignature, superclasses);
			if (origin != null && TransformationHelper.checkPackageWithDotName(origin)) {
				Map<String, MethodUnit> methods = methodsMap.get(origin);
				for (String s : methods.keySet()) {
					MethodUnit mu = methods.get(s);
					if (mu.getNewName().equals(methodSignature)) {
						result = mu;
					}
				}
			}
		}
		return result;
	}

	public static String findMethodOrigin(String methodSignature,
	        Map<String, Set<String>> superClasses) {
		String result = null;
		for (String classname : superClasses.keySet()) {
			Set<String> methods = superClasses.get(classname);
			if (methods.contains(methodSignature)) {
				result = classname;
				break;
			}
		}
		return result;
	}

	public static Map<String, Set<String>> findAllSuperClassesWithMethods(String className) {

		Map<String, Set<String>> interfaces = new HashMap<String, Set<String>>();
		List<String> unprocessedInterfaces = new ArrayList<String>();
		ClassNode cn = parseToClassNode(className);

		String superName = cn.superName.replace('/', '.');
		interfaces.put(superName, new HashSet<String>());
		unprocessedInterfaces.add(superName);
		for (Object objInterface : cn.interfaces) {
			String strInterface = ((String) objInterface).replace('/', '.');
			interfaces.put(strInterface, new HashSet<String>());
			unprocessedInterfaces.add(strInterface);
		}

		List<String> tmpList = new ArrayList<String>();
		while (unprocessedInterfaces.size() != 0) {
			tmpList.clear();
			for (Iterator<String> it = unprocessedInterfaces.iterator(); it.hasNext();) {
				String targetInterface = it.next();
				ClassNode targetInterfaceNode = parseToClassNode(targetInterface);
				if (targetInterfaceNode != null) {
					if (targetInterfaceNode.superName == null) {
						// we found java.lang.Object
						//                        log.debug(targetInterface + " has null supername");
					} else {
						interfaces.put(targetInterface,
						               extractMethodSignatures(targetInterfaceNode));
						String targetInterfaceSuperName = targetInterfaceNode.superName.replace('/',
						                                                                        '.');
						tmpList.add(targetInterfaceSuperName);
						for (Object objInterface : targetInterfaceNode.interfaces) {
							String strInterface = ((String) objInterface).replace('/',
							                                                      '.');
							tmpList.add(strInterface);
						}
					}
				}
				it.remove();
			}
			unprocessedInterfaces.addAll(tmpList);
		}

		//add the java.lang.Object methods
		ClassNode javaLangObject = parseToClassNode("java.lang.Object");
		interfaces.put("java.lang.Object", extractMethodSignatures(javaLangObject));
		return interfaces;
	}

	public static Set<String> extractMethodSignatures(ClassNode cn) {
		Set<String> methods = new HashSet<String>();
		for (Object o : cn.methods) {
			MethodNode mn = (MethodNode) o;
			if (!(mn.name.equals("<init>") || mn.name.equals("<clinit>"))) {
				String tempSignature = mn.name + "|" + mn.desc;
				methods.add(tempSignature);
			}
		}
		return methods;
	}

	public static ClassNode parseToClassNode(ClassUnit cu) {

		InputStream fin = null;
		try {
			fin = new FileInputStream(cu.getFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int actual;
			do {
				actual = fin.read(buf);
				if (actual > 0) {
					out.write(buf, 0, actual);
				}
			} while (actual > 0);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fin.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		ClassNode cn = new ClassNode();
		ClassReader cr = new ClassReader(out.toByteArray());
		cr.accept(cn, ClassReader.SKIP_FRAMES);
		return cn;
	}

	public static ClassNode parseToClassNode(String className) {
		ClassNode cn = new ClassNode();
		ClassReader cr = null;
		try {
			cr = new ClassReader(className);
			cr.accept(cn, ClassReader.EXPAND_FRAMES);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cn;
	}
}
