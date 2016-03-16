/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.instrumentation.testability.transformer;

import org.evosuite.instrumentation.testability.BooleanHelper;
import org.evosuite.instrumentation.testability.BooleanTestabilityTransformation;
import org.evosuite.instrumentation.testability.DescriptorMapping;
import org.evosuite.instrumentation.TransformationStatistics;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Replace signatures of all calls/field accesses on Booleans
 */
public class BooleanCallsTransformer extends MethodNodeTransformer {
	/**
	 * 
	 */
	private final BooleanTestabilityTransformation booleanTestabilityTransformation;

	/**
	 * @param booleanTestabilityTransformation
	 */
	public BooleanCallsTransformer(
			BooleanTestabilityTransformation booleanTestabilityTransformation) {
		this.booleanTestabilityTransformation = booleanTestabilityTransformation;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.instrumentation.MethodNodeTransformer#transformMethodInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.MethodInsnNode)
	 */
	@Override
	protected AbstractInsnNode transformMethodInsnNode(MethodNode mn,
	        MethodInsnNode methodNode) {
		if (methodNode.owner.equals(Type.getInternalName(BooleanHelper.class)))
			return methodNode;

		methodNode.desc = this.booleanTestabilityTransformation.transformMethodDescriptor(methodNode.owner,
		                                            methodNode.name, methodNode.desc);
		methodNode.name = DescriptorMapping.getInstance().getMethodName(methodNode.owner,
		                                                                methodNode.name,
		                                                                methodNode.desc);
		if (DescriptorMapping.getInstance().isBooleanMethod(methodNode.desc)) {
			BooleanTestabilityTransformation.logger.info("Method needs value transformation: " + methodNode.name);
			if (DescriptorMapping.getInstance().hasBooleanParameters(methodNode.desc)) {
				BooleanTestabilityTransformation.logger.info("Method needs parameter transformation: "
				        + methodNode.name);
				TransformationStatistics.transformBackToBooleanParameter();
				int firstBooleanParameterIndex = -1;
				Type[] types = Type.getArgumentTypes(methodNode.desc);
				for (int i = 0; i < types.length; i++) {
					if (types[i].getDescriptor().equals("Z")) {
						if (firstBooleanParameterIndex == -1) {
							firstBooleanParameterIndex = i;
							break;
						}
					}
				}
				if (firstBooleanParameterIndex != -1) {
					int numOfPushs = types.length - 1 - firstBooleanParameterIndex;
					//                        int numOfPushs = types.length - firstBooleanParameterIndex;

					if (numOfPushs == 0) {
						if (!(methodNode.getPrevious().getOpcode() == Opcodes.ICONST_1 || methodNode.getPrevious().getOpcode() == Opcodes.ICONST_0)) {

							//the boolean parameter is the last parameter
							MethodInsnNode booleanHelperInvoke = new MethodInsnNode(
							        Opcodes.INVOKESTATIC,
							        Type.getInternalName(BooleanHelper.class),
							        "intToBoolean",
							        Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
							                                 new Type[] { Type.INT_TYPE }));
							mn.instructions.insertBefore(methodNode,
							                             booleanHelperInvoke);
						}
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
							}

						}
						mn.instructions.insertBefore(methodNode, insnlist);
					}
				}
			}
			if (Type.getReturnType(methodNode.desc).equals(Type.BOOLEAN_TYPE)) {
				BooleanTestabilityTransformation.logger.info("Method needs return transformation: " + methodNode.name);
				TransformationStatistics.transformBackToBooleanParameter();
				MethodInsnNode n = new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(BooleanHelper.class), "booleanToInt",
				        Type.getMethodDescriptor(Type.INT_TYPE,
				                                 new Type[] { Type.BOOLEAN_TYPE }));
				mn.instructions.insert(methodNode, n);
				return n;
			}
		} else {
			BooleanTestabilityTransformation.logger.info("Method needs no transformation: " + methodNode.name);
		}

		// TODO: If this is a method that is not transformed, and it requires a Boolean parameter
		// then we need to convert this boolean back to an int
		// For example, we could use flow analysis to determine the point where the value is added to the stack
		// and insert a conversion function there
		return methodNode;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.instrumentation.MethodNodeTransformer#transformFieldInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.FieldInsnNode)
	 */
	@Override
	protected AbstractInsnNode transformFieldInsnNode(MethodNode mn,
	        FieldInsnNode fieldNode) {

		// TODO: If the field owner is not transformed, then convert this to a proper Boolean
		fieldNode.desc = this.booleanTestabilityTransformation.transformFieldDescriptor(fieldNode.owner, fieldNode.name,
		                                          fieldNode.desc);

		// If after transformation the field is still Boolean, we need to convert 
		if (Type.getType(fieldNode.desc).equals(Type.BOOLEAN_TYPE)) {
			if (fieldNode.getOpcode() == Opcodes.PUTFIELD
			        || fieldNode.getOpcode() == Opcodes.PUTSTATIC) {
				MethodInsnNode n = new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(BooleanHelper.class), "intToBoolean",
				        Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
				                                 new Type[] { Type.INT_TYPE }));
				TransformationStatistics.transformBackToBooleanField();
				mn.instructions.insertBefore(fieldNode, n);
			} else {
				MethodInsnNode n = new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(BooleanHelper.class), "booleanToInt",
				        Type.getMethodDescriptor(Type.INT_TYPE,
				                                 new Type[] { Type.BOOLEAN_TYPE }));
				mn.instructions.insert(fieldNode, n);
				TransformationStatistics.transformBackToBooleanField();
				return n;
			}
		}
		return fieldNode;
	}
}