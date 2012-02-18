/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
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
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.graphs.GraphPool;
import de.unisb.cs.st.evosuite.graphs.cdg.ControlDependenceGraph;
import de.unisb.cs.st.evosuite.graphs.cfg.BasicBlock;
import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeAnalyzer;
import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstructionFactory;
import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstructionPool;
import de.unisb.cs.st.evosuite.graphs.cfg.ControlDependency;

/**
 * Transform everything Boolean to ints.
 * 
 * This transformation replaces: - TRUE/FALSE with +K/-K - IFEQ/IFNE with
 * IFLE/IFGT - Signatures in fields and calls - Inserts calls to remember
 * distance of last boolean calculation - Inserts calls to recall distance of
 * last boolean calculation when Boolean is used
 * 
 * @author Gordon Fraser
 * 
 */
public class BooleanTestabilityTransformation {

	private static Logger logger = LoggerFactory.getLogger(BooleanTestabilityTransformation.class);

	private final ClassNode cn;

	private final String className;

	private Frame[] currentFrames = null;

	private MethodNode currentMethodNode = null;

	private static final DescriptorMapping descriptorMapping = DescriptorMapping.getInstance();

	public BooleanTestabilityTransformation(ClassNode cn) {
		this.cn = cn;
		this.className = cn.name.replace("/", ".");
	}

	/**
	 * Transform all methods and fields
	 * 
	 * @return
	 */
	public ClassNode transform() {

		processFields();
		processMethods();
		if (className.equals(Properties.TARGET_CLASS)
		        || className.startsWith(Properties.TARGET_CLASS + "$"))
			TransformationStatistics.writeStatistics(className);

		return cn;
	}

	/**
	 * Handle transformation of fields defined in this class
	 */
	private void processFields() {
		List<FieldNode> fields = cn.fields;
		for (FieldNode field : fields) {
			if (descriptorMapping.isTransformedField(className, field.name, field.desc)) {
				String newDesc = transformFieldDescriptor(className, field.name,
				                                          field.desc);
				logger.info("Transforming field " + field.name + " from " + field.desc
				        + " to " + newDesc);
				if (!newDesc.equals(field.desc))
					TransformationStatistics.transformBooleanField();
				field.desc = newDesc;
			}
		}
	}

	/**
	 * Handle transformation of methods defined in this class
	 */
	private void processMethods() {
		List<MethodNode> methodNodes = cn.methods;
		for (MethodNode mn : methodNodes) {
			if ((mn.access & Opcodes.ACC_NATIVE) == Opcodes.ACC_NATIVE)
				continue;
			if (descriptorMapping.isTransformedMethod(className, mn.name, mn.desc)) {
				logger.info("Transforming signature of method " + mn.name + mn.desc);
				transformMethodSignature(mn);
				logger.info("Transformed signature to " + mn.name + mn.desc);
			}
			transformMethod(mn);
		}
	}

	public static String getOriginalNameDesc(String className, String methodName,
	        String desc) {
		String key = className.replace(".", "/") + "/" + methodName + desc;
		if (descriptorMapping.originalDesc.containsKey(key)) {
			logger.debug("Descriptor mapping contains original for " + key);
			return descriptorMapping.getOriginalName(className, methodName, desc)
			        + descriptorMapping.originalDesc.get(key);
		} else {
			logger.debug("Descriptor mapping does not contain original for " + key);
			return methodName + desc;
		}
	}

	public static String getOriginalDesc(String className, String methodName, String desc) {
		String key = className.replace(".", "/") + "/" + methodName + desc;
		if (descriptorMapping.originalDesc.containsKey(key)) {
			logger.debug("Descriptor mapping contains original for " + key);
			return descriptorMapping.originalDesc.get(key);
		} else {
			logger.debug("Descriptor mapping does not contain original for " + key);
			return desc;
		}
	}

	public static boolean hasTransformedParameters(String className, String methodName,
	        String desc) {
		String key = className.replace(".", "/") + "/" + methodName + desc;
		if (descriptorMapping.originalDesc.containsKey(key)) {
			for (Type type : Type.getArgumentTypes(descriptorMapping.originalDesc.get(key))) {
				if (type.equals(Type.BOOLEAN_TYPE))
					return true;
			}
		}

		return false;
	}

	public static boolean isTransformedField(String className, String fieldName,
	        String desc) {
		return descriptorMapping.isTransformedField(className, fieldName, desc);
	}

	/**
	 * Insert a call to the isNull helper function
	 * 
	 * @param opcode
	 * @param position
	 * @param list
	 */
	private void insertPushNull(int opcode, JumpInsnNode position, InsnList list) {
		MethodInsnNode nullCheck = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "isNull",
		        Type.getMethodDescriptor(Type.INT_TYPE,
		                                 new Type[] { Type.getType(Object.class),
		                                         Type.INT_TYPE }));
		list.insertBefore(position, new InsnNode(Opcodes.DUP));
		list.insertBefore(position, new LdcInsnNode(opcode));
		list.insertBefore(position, nullCheck);
		//list.insertBefore(position,
		//                  new LdcInsnNode(getBranchID(currentMethodNode, position)));
		insertBranchIdPlaceholder(currentMethodNode, position);
		MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "pushPredicate",
		        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE,
		                Type.INT_TYPE }));
		list.insertBefore(position, push);

	}

	/**
	 * Insert a call to the reference equality check helper function
	 * 
	 * @param opcode
	 * @param position
	 * @param list
	 */
	private void insertPushEquals(int opcode, JumpInsnNode position, InsnList list) {
		MethodInsnNode equalCheck = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "isEqual",
		        Type.getMethodDescriptor(Type.INT_TYPE,
		                                 new Type[] { Type.getType(Object.class),
		                                         Type.getType(Object.class),
		                                         Type.INT_TYPE }));
		list.insertBefore(position, new InsnNode(Opcodes.DUP2));
		list.insertBefore(position, new LdcInsnNode(opcode));
		list.insertBefore(position, equalCheck);
		//list.insertBefore(position,
		//                  new LdcInsnNode(getBranchID(currentMethodNode, position)));
		insertBranchIdPlaceholder(currentMethodNode, position);
		MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "pushPredicate",
		        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE,
		                Type.INT_TYPE }));
		list.insertBefore(position, push);

	}

	private BytecodeInstruction getBytecodeInstruction(MethodNode mn,
	        AbstractInsnNode node) {
		return BytecodeInstructionPool.getInstruction(className, mn.name + mn.desc, node);
	}

	private int getBranchID(MethodNode mn, JumpInsnNode jumpNode) {
		assert (mn.instructions.contains(jumpNode));
		BytecodeInstruction insn = getBytecodeInstruction(mn, jumpNode);
		logger.info("Found instruction: " + insn);
		Branch branch = BranchPool.getBranchForInstruction(insn);
		return branch.getActualBranchId();
	}

	private int getControlDependentBranchID(MethodNode mn, AbstractInsnNode insnNode) {
		BytecodeInstruction insn = getBytecodeInstruction(mn, insnNode);
		// FIXXME: Handle multiple control dependencies
		return insn.getControlDependentBranchId();
	}

	private int getApproximationLevel(MethodNode mn, AbstractInsnNode insnNode) {
		BytecodeInstruction insn = getBytecodeInstruction(mn, insnNode);
		// FIXXME: Handle multiple control dependencies
		return insn.getCDGDepth();
	}

	private void insertBranchIdPlaceholder(MethodNode mn, JumpInsnNode jumpNode) {
		Label label = new Label();
		LabelNode labelNode = new LabelNode(label);
		//BooleanTestabilityPlaceholderTransformer.addBranchPlaceholder(label, jumpNode);
		mn.instructions.insertBefore(jumpNode, labelNode);
		//mn.instructions.insertBefore(jumpNode, new LdcInsnNode(0));
		mn.instructions.insertBefore(jumpNode, new LdcInsnNode(getBranchID(mn, jumpNode)));
	}

	private void insertControlDependencyPlaceholder(MethodNode mn,
	        AbstractInsnNode insnNode) {
		Label label = new Label();
		LabelNode labelNode = new LabelNode(label);
		//BooleanTestabilityPlaceholderTransformer.addControlDependencyPlaceholder(label,
		//                                                                         insnNode);
		mn.instructions.insertBefore(insnNode, labelNode);
		//instructions.insertBefore(insnNode, new LdcInsnNode(0));
		//mn.instructions.insertBefore(insnNode, new LdcInsnNode(0));
		mn.instructions.insertBefore(insnNode, new LdcInsnNode(
		        getControlDependentBranchID(mn, insnNode)));
		mn.instructions.insertBefore(insnNode,
		                             new LdcInsnNode(getApproximationLevel(mn, insnNode)));
		logger.info("Control dependent branch id: "
		        + getControlDependentBranchID(mn, insnNode));
		logger.info("Approximation level: " + getApproximationLevel(mn, insnNode));
	}

	/**
	 * Insert a call to the distance function for unary comparison
	 * 
	 * @param opcode
	 * @param position
	 * @param list
	 */
	private void insertPush(int opcode, JumpInsnNode position, InsnList list) {
		list.insertBefore(position, new InsnNode(Opcodes.DUP));
		// TODO: We have to put a placeholder here instead of the actual branch ID
		// TODO: And then later add another transformation where we replace this with
		//       actual branch IDs
		//list.insertBefore(position,
		//                  new LdcInsnNode(getBranchID(currentMethodNode, position)));
		insertBranchIdPlaceholder(currentMethodNode, position);
		MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "pushPredicate",
		        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE,
		                Type.INT_TYPE }));
		list.insertBefore(position, push);

	}

	/**
	 * Insert a call to the distance function for binary comparison
	 * 
	 * @param opcode
	 * @param position
	 * @param list
	 */
	private void insertPush2(int opcode, JumpInsnNode position, InsnList list) {
		list.insertBefore(position, new InsnNode(Opcodes.DUP2));
		list.insertBefore(position, new InsnNode(Opcodes.ISUB));
		insertBranchIdPlaceholder(currentMethodNode, position);

		//		list.insertBefore(position,
		//		                  new LdcInsnNode(getBranchID(currentMethodNode, position)));
		MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "pushPredicate",
		        Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.INT_TYPE,
		                Type.INT_TYPE }));
		list.insertBefore(position, push);

	}

	/**
	 * Insert a call that takes a boolean from the stack, and returns the
	 * appropriate distance
	 * 
	 * @param position
	 * @param list
	 */
	private void insertGet(AbstractInsnNode position, InsnList list) {
		logger.info("Inserting get call");
		// Here, branchId is the first control dependency
		//list.insertBefore(position,
		//                  new LdcInsnNode(getControlDependentBranchID(currentMethodNode,
		//                                                              position)));
		insertControlDependencyPlaceholder(currentMethodNode, position);

		MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "getDistance",
		        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] { Type.INT_TYPE,
		                Type.INT_TYPE, Type.INT_TYPE }));
		list.insert(position, get);
	}

	/**
	 * Insert a call that takes a boolean from the stack, and returns the
	 * appropriate distance
	 * 
	 * @param position
	 * @param list
	 */
	private void insertGetBefore(AbstractInsnNode position, InsnList list) {
		logger.info("Inserting get call before");
		// Here, branchId is the first control dependency
		//list.insertBefore(position,
		//                  new LdcInsnNode(getControlDependentBranchID(currentMethodNode,
		//                                                              position)));
		// insertControlDependencyPlaceholder(currentMethodNode, position);

		// branch
		// approx
		// value

		Label label = new Label();
		LabelNode labelNode = new LabelNode(label);
		//BooleanTestabilityPlaceholderTransformer.addControlDependencyPlaceholder(label,
		//                                                                         insnNode);
		currentMethodNode.instructions.insertBefore(position, labelNode);
		//instructions.insertBefore(insnNode, new LdcInsnNode(0));
		//mn.instructions.insertBefore(insnNode, new LdcInsnNode(0));
		currentMethodNode.instructions.insertBefore(position, new LdcInsnNode(
		        getControlDependentBranchID(currentMethodNode, position)));
		currentMethodNode.instructions.insertBefore(position, new InsnNode(Opcodes.SWAP));
		currentMethodNode.instructions.insertBefore(position, new LdcInsnNode(
		        getApproximationLevel(currentMethodNode, position)));
		currentMethodNode.instructions.insertBefore(position, new InsnNode(Opcodes.SWAP));

		MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
		        Type.getInternalName(BooleanHelper.class), "getDistance",
		        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] { Type.INT_TYPE,
		                Type.INT_TYPE, Type.INT_TYPE }));
		list.insertBefore(position, get);
	}

	private boolean isBooleanOnStack(MethodNode mn, AbstractInsnNode node, int position) {
		int insnPosition = mn.instructions.indexOf(node);
		if (insnPosition >= currentFrames.length) {
			logger.warn("Trying to access frame out of scope: " + insnPosition + "/"
			        + currentFrames.length);
			return false;
		}
		Frame frame = currentFrames[insnPosition];
		return frame.getStack(position) == BooleanValueInterpreter.BOOLEAN_VALUE;
	}

	private boolean isBooleanVariable(int var, MethodNode mn) {
		for (Object o : mn.localVariables) {
			LocalVariableNode vn = (LocalVariableNode) o;
			if (vn.index == var)
				return Type.getType(vn.desc).equals(Type.BOOLEAN_TYPE);
		}
		return false;
	}

	/**
	 * This helper function determines whether the boolean on the stack at the
	 * current position will be stored in a Boolean variable
	 * 
	 * @param position
	 * @param mn
	 * @return
	 */
	private boolean isBooleanAssignment(AbstractInsnNode position, MethodNode mn) {
		AbstractInsnNode node = position.getNext();
		logger.info("Checking for ISTORE after boolean");
		boolean done = false;
		while (!done) {

			if (node.getOpcode() == Opcodes.PUTFIELD
			        || node.getOpcode() == Opcodes.PUTSTATIC) {
				// TODO: Check whether field is static
				logger.info("Checking field assignment");
				FieldInsnNode fn = (FieldInsnNode) node;
				if (descriptorMapping.isTransformedOrBooleanField(fn.owner, fn.name,
				                                                  fn.desc)) {
					return true;
				} else {
					return false;
				}
			} else if (node.getOpcode() == Opcodes.ISTORE) {
				logger.info("Found ISTORE after boolean");

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
				if (descriptorMapping.isTransformedOrBooleanMethod(cn.name, mn.name,
				                                                   mn.desc)) {
					logger.info("Method returns a bool");
					return true;
				} else {
					logger.info("Method does not return a bool");
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

	private void generateCDG(MethodNode mn) {
		BytecodeInstructionPool.registerMethodNode(mn, className, mn.name + mn.desc);

		BytecodeAnalyzer bytecodeAnalyzer = new BytecodeAnalyzer();
		logger.info("Generating initial CFG for method " + mn.name);

		try {

			bytecodeAnalyzer.analyze(className, mn.name + mn.desc, mn);
		} catch (AnalyzerException e) {
			logger.error("Analyzer exception while analyzing " + className + "."
			        + mn.name + ": " + e);
			e.printStackTrace();
		}

		// compute Raw and ActualCFG and put both into GraphPool
		bytecodeAnalyzer.retrieveCFGGenerator().registerCFGs();
	}

	/**
	 * Determine if the signature of the given method needs to be transformed,
	 * and transform if necessary
	 * 
	 * @param owner
	 * @param name
	 * @param desc
	 * @return
	 */
	private String transformMethodDescriptor(String owner, String name, String desc) {
		return descriptorMapping.getMethodDesc(owner, name, desc);
	}

	/**
	 * Determine if the signature of the given field needs to be transformed,
	 * and transform if necessary
	 * 
	 * @param owner
	 * @param name
	 * @param desc
	 * @return
	 */
	private String transformFieldDescriptor(String owner, String name, String desc) {
		return descriptorMapping.getFieldDesc(className, name, desc);
	}

	private void transformMethodSignature(MethodNode mn) {
		// If the method was declared in java.* then don't instrument
		// Otherwise change signature
		String newDesc = descriptorMapping.getMethodDesc(className, mn.name, mn.desc);
		if (Type.getReturnType(mn.desc) == Type.BOOLEAN_TYPE
		        && Type.getReturnType(newDesc) == Type.INT_TYPE)
			TransformationStatistics.transformBooleanReturnValue();
		if (Arrays.asList(Type.getArgumentTypes(mn.desc)).contains(Type.BOOLEAN_TYPE)
		        && !Arrays.asList(Type.getArgumentTypes(newDesc)).contains(Type.BOOLEAN_TYPE))
			TransformationStatistics.transformBooleanParameter();
		String newName = descriptorMapping.getMethodName(className, mn.name, mn.desc);
		logger.info("Changing method descriptor from " + mn.name + mn.desc + " to "
		        + descriptorMapping.getMethodName(className, mn.name, mn.desc) + newDesc);
		mn.desc = descriptorMapping.getMethodDesc(className, mn.name, mn.desc);
		mn.name = newName;
	}

	private Frame[] getArrayFrames(MethodNode mn) {
		try {
			Analyzer a = new Analyzer(new BooleanArrayInterpreter());
			a.analyze(cn.name, mn);
			return a.getFrames();
		} catch (Exception e) {
			logger.warn("[Array] Error during analysis: " + e);
			return null;
		}
	}

	/**
	 * Apply testability transformation to an individual method
	 * 
	 * @param mn
	 */
	private void transformMethod(MethodNode mn) {
		logger.info("Transforming method " + mn.name + mn.desc);

		//currentCFG = GraphPool.getActualCFG(className, mn.name + mn.desc);

		// TODO: Skipping interfaces for now, but will need to handle Booleans in interfaces!
		if ((mn.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT)
			return;

		String origDesc = getOriginalDesc(className, mn.name, mn.desc);

		try {
			Analyzer a = new Analyzer(new BooleanValueInterpreter(origDesc,
			        (mn.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC));
			a.analyze(className, mn);
			currentFrames = a.getFrames();
		} catch (Exception e) {
			logger.warn("1. Error during analysis: " + e);
			e.printStackTrace();
			// TODO: Handle error
		}
		generateCDG(mn);
		currentMethodNode = mn;
		// First expand ifs without else/*
		new ImplicitElseTransformer().transform(mn);
		try {
			Analyzer a = new Analyzer(new BooleanValueInterpreter(origDesc,
			        (mn.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC));
			a.analyze(className, mn);
			currentFrames = a.getFrames();
		} catch (Exception e) {
			logger.warn("2. Error during analysis: " + e);
			e.printStackTrace();
			// TODO: Handle error
		}

		//		BytecodeInstructionPool.reRegisterMethodNode(mn, className, mn.name + mn.desc);
		// Replace all bitwise operators
		logger.info("Transforming Boolean bitwise operators");
		new BitwiseOperatorTransformer().transform(mn);

		// Transform IFEQ/IFNE to IFLE/IFGT
		logger.info("Transforming Boolean IFs");
		new BooleanIfTransformer().transform(mn);

		// Insert call to BooleanHelper.get after ICONST_0/1 or Boolean fields
		logger.info("Transforming Boolean definitions");
		new BooleanDefinitionTransformer().transform(mn);

		// Replace all instanceof comparisons
		logger.info("Transforming instanceof");
		new InstanceOfTransformer().transform(mn);

		// Replace all calls to methods/fields returning booleans
		new BooleanCallsTransformer().transform(mn);

		// Transform all flag based comparisons
		logger.info("Transforming Boolean distances");
		new BooleanDistanceTransformer().transform(mn);
		mn.maxStack += 2;

		// Replace all boolean arrays
		new BooleanArrayTransformer().transform(mn);

		new BooleanArrayIndexTransformer(getArrayFrames(mn)).transform(mn);

		// Replace all boolean return values
		new BooleanReturnTransformer().transform(mn);

		GraphPool.clear(className, mn.name + mn.desc);
		BytecodeInstructionPool.clear(className, mn.name + mn.desc);
		BranchPool.clear(className, mn.name + mn.desc);

		// Actually this should be done automatically by the ClassWriter...
		// +2 because we might do a DUP2
		mn.maxStack += 3;
	}

	/**
	 * This transformer inserts calls to the get function when a Boolean is put
	 * on the stack
	 */
	private class BooleanDefinitionTransformer extends MethodNodeTransformer {

		// Get branch id
		// Get last distance for this branch id, else +/-K

		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.InsnNode)
		 */
		@Override
		protected AbstractInsnNode transformInsnNode(MethodNode mn, InsnNode insnNode) {
			logger.info("Checking transformation of InsnNode ");
			if (insnNode.getOpcode() == Opcodes.ICONST_0
			        && isBooleanAssignment(insnNode, mn)) {
				TransformationStatistics.insertedGet();
				insertGet(insnNode, mn.instructions);
			} else if (insnNode.getOpcode() == Opcodes.ICONST_1
			        && isBooleanAssignment(insnNode, mn)) {
				TransformationStatistics.insertedGet();
				insertGet(insnNode, mn.instructions);
			} else if (insnNode.getOpcode() == Opcodes.IRETURN
			        && isBooleanAssignment(insnNode, mn)) {
				TransformationStatistics.insertedGet();
				insertGet(insnNode, mn.instructions);
			}
			return insnNode;
		}

		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformVarInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.VarInsnNode)
		 */
		@Override
		protected AbstractInsnNode transformVarInsnNode(MethodNode mn, VarInsnNode varNode) {
			// Special case for implicit else branch
			if (isBooleanVariable(varNode.var, mn)
			        && varNode.getNext() instanceof VarInsnNode) {
				VarInsnNode vn2 = (VarInsnNode) varNode.getNext();
				if (varNode.var == vn2.var) {
					insertGet(varNode, mn.instructions);
				}
			}
			return varNode;
		}

		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformFieldInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.FieldInsnNode)
		 */
		@Override
		protected AbstractInsnNode transformFieldInsnNode(MethodNode mn,
		        FieldInsnNode fieldNode) {
			// This handles the else branch for field assignments
			if (descriptorMapping.isTransformedOrBooleanField(className, fieldNode.name,
			                                                  fieldNode.desc)) {
				if (fieldNode.getNext() instanceof FieldInsnNode) {
					FieldInsnNode other = (FieldInsnNode) fieldNode.getNext();
					if (fieldNode.owner.equals(other.owner)
					        && fieldNode.name.equals(other.name)
					        && fieldNode.desc.equals(other.desc)) {
						if (fieldNode.getOpcode() == Opcodes.GETFIELD
						        && other.getOpcode() == Opcodes.PUTFIELD) {
							insertGetBefore(other, mn.instructions);
						} else if (fieldNode.getOpcode() == Opcodes.GETSTATIC
						        && other.getOpcode() == Opcodes.PUTSTATIC) {
							insertGetBefore(other, mn.instructions);
						}
					}
				}
			}
			return fieldNode;
		}

	}

	/**
	 * This transformer inserts calls to the put function before a Boolean
	 * predicate
	 */
	private class BooleanDistanceTransformer extends MethodNodeTransformer {
		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformJumpInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.JumpInsnNode)
		 */
		@Override
		protected AbstractInsnNode transformJumpInsnNode(MethodNode mn,
		        JumpInsnNode jumpNode) {

			switch (jumpNode.getOpcode()) {
			case Opcodes.IFEQ:
			case Opcodes.IFNE:
			case Opcodes.IFLT:
			case Opcodes.IFGE:
			case Opcodes.IFGT:
			case Opcodes.IFLE:
				TransformationStatistics.insertPush(jumpNode.getOpcode());
				insertPush(jumpNode.getOpcode(), jumpNode, mn.instructions);
				break;
			case Opcodes.IF_ICMPEQ:
			case Opcodes.IF_ICMPNE:
			case Opcodes.IF_ICMPLT:
			case Opcodes.IF_ICMPGE:
			case Opcodes.IF_ICMPGT:
			case Opcodes.IF_ICMPLE:
				TransformationStatistics.insertPush(jumpNode.getOpcode());
				insertPush2(jumpNode.getOpcode(), jumpNode, mn.instructions);
				break;
			case Opcodes.IFNULL:
			case Opcodes.IFNONNULL:
				TransformationStatistics.insertPush(jumpNode.getOpcode());
				insertPushNull(jumpNode.getOpcode(), jumpNode, mn.instructions);
				break;
			case Opcodes.IF_ACMPEQ:
			case Opcodes.IF_ACMPNE:
				TransformationStatistics.insertPush(jumpNode.getOpcode());
				insertPushEquals(jumpNode.getOpcode(), jumpNode, mn.instructions);
				break;
			default:
				// GOTO, JSR: Do nothing
			}
			return jumpNode;
		}
	}

	/**
	 * Transform IFEQ/IFNE to IFLE/IFGT for transformed Boolean variables
	 */
	private class BooleanIfTransformer extends MethodNodeTransformer {

		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformJumpInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.JumpInsnNode)
		 */
		@Override
		protected AbstractInsnNode transformJumpInsnNode(MethodNode mn,
		        JumpInsnNode jumpNode) {
			if (jumpNode.getOpcode() == Opcodes.IFNE) {
				if (isBooleanOnStack(mn, jumpNode, 0)) {
					TransformationStatistics.transformedBooleanComparison();
					logger.info("Changing IFNE");
					jumpNode.setOpcode(Opcodes.IFGT);
				}
			} else if (jumpNode.getOpcode() == Opcodes.IFEQ) {
				if (isBooleanOnStack(mn, jumpNode, 0)) {
					TransformationStatistics.transformedBooleanComparison();
					logger.info("Changing IFEQ");
					jumpNode.setOpcode(Opcodes.IFLE);
				} else {
					int insnPosition = mn.instructions.indexOf(jumpNode);
					Frame frame = currentFrames[insnPosition];
					logger.info("Not changing IFEQ, no Boolean on stack: "
					        + frame.getStack(0));
				}
			} else if (jumpNode.getOpcode() == Opcodes.IF_ICMPEQ) {
				if (isBooleanOnStack(mn, jumpNode, 0)) {
					InsnList convert = new InsnList();
					convert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "pushParameter",
					        Type.getMethodDescriptor(Type.VOID_TYPE,
					                                 new Type[] { Type.INT_TYPE })));
					convert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "pushParameter",
					        Type.getMethodDescriptor(Type.VOID_TYPE,
					                                 new Type[] { Type.INT_TYPE })));
					convert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class),
					        "popParameterBooleanFromInt",
					        Type.getMethodDescriptor(Type.BOOLEAN_TYPE, new Type[] {})));
					convert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class),
					        "popParameterBooleanFromInt",
					        Type.getMethodDescriptor(Type.BOOLEAN_TYPE, new Type[] {})));
					mn.instructions.insertBefore(jumpNode, convert);
					TransformationStatistics.transformedBooleanComparison();
				}
			} else if (jumpNode.getOpcode() == Opcodes.IF_ICMPNE) {
				if (isBooleanOnStack(mn, jumpNode, 0)) {
					InsnList convert = new InsnList();
					convert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "pushParameter",
					        Type.getMethodDescriptor(Type.VOID_TYPE,
					                                 new Type[] { Type.INT_TYPE })));
					convert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "pushParameter",
					        Type.getMethodDescriptor(Type.VOID_TYPE,
					                                 new Type[] { Type.INT_TYPE })));
					convert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class),
					        "popParameterBooleanFromInt",
					        Type.getMethodDescriptor(Type.BOOLEAN_TYPE, new Type[] {})));
					convert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class),
					        "popParameterBooleanFromInt",
					        Type.getMethodDescriptor(Type.BOOLEAN_TYPE, new Type[] {})));
					mn.instructions.insertBefore(jumpNode, convert);
					TransformationStatistics.transformedBooleanComparison();
				}
			}
			return jumpNode;
		}
	}

	/**
	 * Expand ifs without else
	 */
	private class ImplicitElseTransformer extends MethodNodeTransformer {

		private final Set<ControlDependency> addedNodes = new HashSet<ControlDependency>();

		/*** Keep track of inserted PUTFIELDs */
		private final Set<AbstractInsnNode> addedInsns = new HashSet<AbstractInsnNode>();

		private boolean isDefinedBefore(MethodNode mn, VarInsnNode var,
		        AbstractInsnNode position) {
			// TODO: Iterate over local variables and check if local is defined here
			List<LocalVariableNode> localVar = mn.localVariables;
			if (localVar.isEmpty()) {
				// If we have no debug information, try to guess
				AbstractInsnNode pos = position.getPrevious();
				while (pos != mn.instructions.getFirst()) {
					if (pos instanceof VarInsnNode) {
						VarInsnNode vn = (VarInsnNode) pos;
						if (var.var == vn.var) {
							return true;
						}
					}
					pos = pos.getPrevious();
				}
			} else {

				int current = mn.instructions.indexOf(position);
				for (LocalVariableNode local : localVar) {
					if (local.index == var.var) {
						int start = mn.instructions.indexOf(local.start);
						int end = mn.instructions.indexOf(local.end);
						if (current >= start && current <= end)
							return true;
					}
				}
			}

			return false;
		}

		private void handleDependency(ControlDependency dependency,
		        ControlDependenceGraph cdg, MethodNode mn, FieldInsnNode varNode,
		        BytecodeInstruction parentLevel) {

			if (addedNodes.contains(dependency))
				return;

			// Get the basic blocks reachable if the dependency would evaluate different
			Set<BasicBlock> blocks = cdg.getAlternativeBlocks(dependency);
			addedNodes.add(dependency);

			Set<ControlDependency> dependencies = dependency.getBranch().getInstruction().getControlDependencies();
			//if (dependencies.size() == 1) {
			//	ControlDependency dep = dependencies.iterator().next();
			for (ControlDependency dep : dependencies) {
				if (!addedNodes.contains(dep) && dep != dependency)
					handleDependency(dep, cdg, mn, varNode,
					                 dependency.getBranch().getInstruction());
			}

			// TODO: Need to check that there is an assignment in every alternative path through CDG

			boolean hasAssignment = false;
			for (BasicBlock block : blocks) {
				// If this block also assigns a value to the same variable
				for (BytecodeInstruction instruction : block) {
					if (instruction.getASMNode().getOpcode() == Opcodes.PUTFIELD
					        || instruction.getASMNode().getOpcode() == Opcodes.PUTSTATIC) {
						FieldInsnNode otherFieldNode = (FieldInsnNode) instruction.getASMNode();
						FieldInsnNode thisFieldNode = varNode;
						if (otherFieldNode.owner.equals(thisFieldNode.owner)
						        && otherFieldNode.name.equals(thisFieldNode.name)) {
							hasAssignment = true;
							break;
						}
					}
				}
				if (hasAssignment) {
					break;
				}
			}

			// The Flag assignment is is the dependency evaluates to the given value
			// We thus need to insert the tautoligical assignment either directly after the IF (if the value is true)
			// or before the jump target (if the value is false)

			if (!hasAssignment) {
				if (dependency.getBranch().getInstruction().isSwitch()) {
					logger.warn("Don't know how to handle Switches yet");
					return;
				}

				TransformationStatistics.transformedImplicitElse();

				JumpInsnNode jumpNode = (JumpInsnNode) dependency.getBranch().getInstruction().getASMNode();
				FieldInsnNode newLoad = new FieldInsnNode(
				        varNode.getOpcode() == Opcodes.PUTSTATIC ? Opcodes.GETSTATIC
				                : Opcodes.GETFIELD, varNode.owner, varNode.name,
				        varNode.desc);
				FieldInsnNode newStore = new FieldInsnNode(varNode.getOpcode(),
				        varNode.owner, varNode.name, varNode.desc);
				AbstractInsnNode newOwnerLoad1 = null;
				AbstractInsnNode newOwnerLoad2 = null;
				if (varNode.getOpcode() == Opcodes.PUTFIELD) {
					// Need to copy the bloody owner
					// Check for VarInsn
					//if (varNode.getPrevious().getOpcode() == Opcodes.ALOAD) {
					newOwnerLoad1 = new VarInsnNode(Opcodes.ALOAD, 0);
					newOwnerLoad2 = new VarInsnNode(Opcodes.ALOAD, 0);
					/*
					} else {
					// Else use helper function
					// Insert DUP and
					logger.info("Wargh");
					System.exit(0);
					fieldOwnerId++;
					InsnNode dupNode = new InsnNode(Opcodes.DUP);
					mn.instructions.insertBefore(varNode, new LdcInsnNode(
					        fieldOwnerId));
					mn.instructions.insertBefore(varNode, dupNode);
					registerInstruction(mn, varNode, dupNode);
					MethodInsnNode storeOwner = new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        "de/unisb/cs/st/evosuite/javaagent/BooleanHelper",
					        "setFieldOwner", "(ILjava/lang/Object;)V");
					mn.instructions.insertBefore(varNode, storeOwner);
					registerInstruction(mn, varNode, storeOwner);
					newOwnerLoad1 = new MethodInsnNode(Opcodes.INVOKESTATIC,
					        "de/unisb/cs/st/evosuite/javaagent/BooleanHelper",
					        "getFieldOwner", "(I)Ljava/lang/Object;");
					newOwnerLoad2 = new MethodInsnNode(Opcodes.INVOKESTATIC,
					        "de/unisb/cs/st/evosuite/javaagent/BooleanHelper",
					        "getFieldOwner", "(I)Ljava/lang/Object;");
					}
					*/
				}

				if (dependency.getBranchExpressionValue()) {
					logger.info("Inserting after if");
					// Insert directly after if
					mn.instructions.insert(jumpNode, newStore);
					mn.instructions.insert(jumpNode, newLoad);
					if (newOwnerLoad1 != null) {
						mn.instructions.insert(jumpNode, newOwnerLoad1);
						registerInstruction(mn, varNode, newOwnerLoad1);
					}
					if (newOwnerLoad2 != null) {
						mn.instructions.insert(jumpNode, newOwnerLoad2);
						registerInstruction(mn, varNode, newOwnerLoad2);
					}
					registerInstruction(mn, varNode, newStore);
					registerInstruction(mn, varNode, newLoad);

				} else {
					logger.info("Inserting as jump target");

					// Insert as jump target
					LabelNode target = jumpNode.label;
					LabelNode newTarget = new LabelNode(new Label());

					registerInstruction(mn, target, newStore);
					registerInstruction(mn, target, newLoad);

					InsnList assignment = new InsnList();
					assignment.add(new JumpInsnNode(Opcodes.GOTO, target));
					assignment.add(newTarget);
					if (newOwnerLoad1 != null) {
						assignment.add(newOwnerLoad1);
						registerInstruction(mn, target, newOwnerLoad1);
					}
					if (newOwnerLoad2 != null) {
						assignment.add(newOwnerLoad2);
						registerInstruction(mn, target, newOwnerLoad2);
					}
					assignment.add(newLoad);
					assignment.add(newStore);
					jumpNode.label = newTarget;

					mn.instructions.insertBefore(target, assignment);
				}
				addedInsns.add(newStore);
				addedInsns.add(newLoad);
			}

		}

		private void registerInstruction(MethodNode mn, AbstractInsnNode oldValue,
		        AbstractInsnNode newValue) {
			BytecodeInstruction oldInstruction = BytecodeInstructionPool.getInstruction(className,
			                                                                            mn.name
			                                                                                    + mn.desc,
			                                                                            oldValue);
			BytecodeInstruction instruction = BytecodeInstructionFactory.createBytecodeInstruction(className,
			                                                                                       mn.name
			                                                                                               + mn.desc,
			                                                                                       oldInstruction.getInstructionId(),
			                                                                                       0,
			                                                                                       newValue);
			instruction.setBasicBlock(oldInstruction.getBasicBlock());
			BytecodeInstructionPool.registerInstruction(instruction);
		}

		private void handleDependency(ControlDependency dependency,
		        ControlDependenceGraph cdg, MethodNode mn, VarInsnNode varNode,
		        BytecodeInstruction parentLevel) {

			if (addedNodes.contains(dependency))
				return;

			// Get the basic blocks reachable if the dependency would evaluate different
			Set<BasicBlock> blocks = cdg.getAlternativeBlocks(dependency);
			addedNodes.add(dependency);

			Set<ControlDependency> dependencies = dependency.getBranch().getInstruction().getControlDependencies();
			//if (dependencies.size() == 1) {
			//	ControlDependency dep = dependencies.iterator().next();
			for (ControlDependency dep : dependencies) {
				if (!addedNodes.contains(dep) && dep != dependency)
					handleDependency(dep, cdg, mn, varNode,
					                 dependency.getBranch().getInstruction());
			}

			// TODO: Need to check that there is an assignment in every alternative path through CDG

			boolean hasAssignment = false;
			for (BasicBlock block : blocks) {
				// If this block also assigns a value to the same variable
				for (BytecodeInstruction instruction : block) {
					if (instruction.getASMNode().getOpcode() == Opcodes.ISTORE) {
						VarInsnNode otherVarNode = (VarInsnNode) instruction.getASMNode();
						VarInsnNode thisVarNode = varNode;
						if (otherVarNode.var == thisVarNode.var) {
							hasAssignment = true;
							break;
						}
					}
				}
				if (hasAssignment) {
					break;
				}
			}

			// The Flag assignment is is the dependency evaluates to the given value
			// We thus need to insert the tautoligical assignment either directly after the IF (if the value is true)
			// or before the jump target (if the value is false)

			if (!hasAssignment) {
				TransformationStatistics.transformedImplicitElse();
				if (dependency.getBranch().getInstruction().isSwitch()) {
					logger.warn("Don't know how to handle Switches yet");
					return;
				}
				JumpInsnNode jumpNode = (JumpInsnNode) dependency.getBranch().getInstruction().getASMNode();
				VarInsnNode newStore = new VarInsnNode(Opcodes.ISTORE, varNode.var);
				VarInsnNode newLoad = new VarInsnNode(Opcodes.ILOAD, varNode.var);
				if (dependency.getBranchExpressionValue()) {
					logger.info("Inserting else branch directly after if");
					// Insert directly after if
					if (isDefinedBefore(mn, varNode, jumpNode)) {
						mn.instructions.insert(jumpNode, newStore);
						mn.instructions.insert(jumpNode, newLoad);
						registerInstruction(mn, varNode, newStore);
						registerInstruction(mn, varNode, newLoad);
					}

				} else {
					logger.info("Inserting else branch as jump target");
					// Insert as jump target
					if (isDefinedBefore(mn, varNode, jumpNode)) {

						LabelNode target = jumpNode.label;
						LabelNode newTarget = new LabelNode(new Label());

						// jumpNode or target?
						registerInstruction(mn, jumpNode.getNext(), newStore);
						registerInstruction(mn, jumpNode.getNext(), newLoad);

						InsnList assignment = new InsnList();
						assignment.add(new JumpInsnNode(Opcodes.GOTO, target));
						assignment.add(newTarget);
						assignment.add(newLoad);
						assignment.add(newStore);
						jumpNode.label = newTarget;

						mn.instructions.insertBefore(target, assignment);
					}
				}
			}

		}

		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformFieldInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.FieldInsnNode)
		 */
		@Override
		protected AbstractInsnNode transformFieldInsnNode(MethodNode mn,
		        FieldInsnNode fieldNode) {

			if ((fieldNode.getOpcode() == Opcodes.PUTFIELD || fieldNode.getOpcode() == Opcodes.PUTSTATIC)
			        && descriptorMapping.isTransformedOrBooleanField(fieldNode.owner,
			                                                         fieldNode.name,
			                                                         fieldNode.desc)) {

				if (addedInsns.contains(fieldNode))
					return fieldNode;

				// Can only handle cases where the field owner is loaded directly before the field
				if (fieldNode.getOpcode() == Opcodes.PUTFIELD) {
					AbstractInsnNode previous = fieldNode.getPrevious();
					while (previous instanceof LineNumberNode
					        || previous instanceof FrameNode
					        || previous.getOpcode() == Opcodes.ICONST_0
					        || previous.getOpcode() == Opcodes.ICONST_1)
						previous = previous.getPrevious();
					if (previous.getOpcode() != Opcodes.ALOAD) {
						logger.info("Can't handle case of " + previous);
						return fieldNode;
					}
					VarInsnNode varNode = (VarInsnNode) previous;
					if (varNode.var != 0) {
						logger.info("Can't handle case of " + previous);
						return fieldNode;
					}
				}
				logger.info("Handling PUTFIELD case!");

				// Check if ICONST_0 or ICONST_1 are on the stack
				ControlDependenceGraph cdg = GraphPool.getCDG(className.replace("/", "."),
				                                              mn.name + mn.desc);
				int index = mn.instructions.indexOf(fieldNode);
				logger.info("Getting bytecode instruction for " + fieldNode.name + "/"
				        + ((FieldInsnNode) mn.instructions.get(index)).name);
				InsnList nodes = mn.instructions;
				ListIterator it = nodes.iterator();
				while (it.hasNext()) {
					BytecodeInstruction in = new BytecodeInstruction(className, mn.name,
					        0, 0, (AbstractInsnNode) it.next());
					logger.info(in.toString());
				}
				BytecodeInstruction insn = BytecodeInstructionPool.getInstruction(className.replace("/",
				                                                                                    "."),
				                                                                  mn.name
				                                                                          + mn.desc,
				                                                                  index);
				if (insn == null)
					insn = BytecodeInstructionPool.getInstruction(className.replace("/",
					                                                                "."),
					                                              mn.name + mn.desc,
					                                              fieldNode);
				//varNode);
				if (insn == null) {
					// TODO: Find out why
					logger.warn("ERROR: Could not find node");
					return fieldNode;
				}
				if (insn.getASMNode().getOpcode() != fieldNode.getOpcode()) {
					logger.warn("Found wrong bytecode instruction at this index!");
					BytecodeInstructionPool.getInstruction(className, mn.name + mn.desc,
					                                       fieldNode);
				}
				if (insn.getBasicBlock() == null) {
					logger.warn("ERROR: Problematic node found");
					return fieldNode;
				}
				Set<ControlDependency> dependencies = insn.getControlDependencies();
				logger.info("Found flag assignment: " + insn + ", checking "
				        + dependencies.size() + " control dependencies");

				for (ControlDependency dep : dependencies) {
					if (!addedNodes.contains(dep))
						handleDependency(dep, cdg, mn, fieldNode, insn);
				}
			}
			return fieldNode;
		}

		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformVarInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.VarInsnNode)
		 */
		@Override
		protected AbstractInsnNode transformVarInsnNode(MethodNode mn, VarInsnNode varNode) {
			if (varNode.getOpcode() == Opcodes.ISTORE
			        && isBooleanVariable(varNode.var, mn)) {

				// Check if ICONST_0 or ICONST_1 are on the stack
				ControlDependenceGraph cdg = GraphPool.getCDG(className.replace("/", "."),
				                                              mn.name + mn.desc);
				int index = mn.instructions.indexOf(varNode);
				BytecodeInstruction insn = BytecodeInstructionPool.getInstruction(className.replace("/",
				                                                                                    "."),
				                                                                  mn.name
				                                                                          + mn.desc,
				                                                                  index);
				//varNode);
				if (insn == null) {
					// TODO: Debug this on org.exolab.jms.net.uri.URI
					logger.warn("WARNING: Instruction not found!");
					return varNode;
				}
				if (insn.getASMNode().getOpcode() != varNode.getOpcode()) {
					logger.warn("Found wrong bytecode instruction at this index!");
					BytecodeInstructionPool.getInstruction(className, mn.name + mn.desc,
					                                       varNode);
				}
				Set<ControlDependency> dependencies = insn.getControlDependencies();
				logger.info("Found flag assignment: " + insn + ", checking "
				        + dependencies.size() + " control dependencies");

				for (ControlDependency dep : dependencies) {
					if (!addedNodes.contains(dep))
						handleDependency(dep, cdg, mn, varNode, insn);
				}

				// Only do completion if there's only one dependency
				// Not sure how other cases would look like
				/*
								//if (dependencies.size() > 1)
								//	return varNode;
								//else
								if (dependencies.isEmpty())
									return varNode;

								ControlDependency dep = dependencies.iterator().next();
								if (!addedNodes.contains(dep))
									handleDependency(dep, cdg, mn, varNode, insn);
									*/

			}
			return varNode;
		}

	}

	/**
	 * Replace instanceof operation with helper that puts int on the stack
	 */
	private class InstanceOfTransformer extends MethodNodeTransformer {
		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformTypeInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.TypeInsnNode)
		 */
		@Override
		protected AbstractInsnNode transformTypeInsnNode(MethodNode mn,
		        TypeInsnNode typeNode) {
			if (typeNode.getOpcode() == Opcodes.INSTANCEOF) {
				TransformationStatistics.transformInstanceOf();

				// Depending on the class version we need a String or a Class
				// TODO: This needs to be class version of the class that's loaded, not cn!
				ClassReader reader;
				int version = 48;
				/*
				String name = typeNode.desc.replace("/", ".");
				try {
					reader = new ClassReader(name);
					ClassNode parent = new ClassNode();
					reader.accept(parent, ClassReader.SKIP_CODE);
					version = parent.version;
				} catch (IOException e) {
					TestabilityTransformation.logger.info("Error reading class " + name);
				}
				*/
				if (version >= 49) {
					if (!typeNode.desc.startsWith("[")) {
						LdcInsnNode lin = new LdcInsnNode(Type.getType("L"
						        + typeNode.desc + ";"));
						mn.instructions.insertBefore(typeNode, lin);
					} else {
						LdcInsnNode lin = new LdcInsnNode(Type.getType(typeNode.desc
						        + ";"));
						mn.instructions.insertBefore(typeNode, lin);
					}
				} else {
					LdcInsnNode lin = new LdcInsnNode(typeNode.desc.replace("/", "."));
					mn.instructions.insertBefore(typeNode, lin);
					MethodInsnNode n = new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        Type.getInternalName(Class.class),
					        "forName",
					        Type.getMethodDescriptor(Type.getType(Class.class),
					                                 new Type[] { Type.getType(String.class) }));
					mn.instructions.insertBefore(typeNode, n);
				}
				MethodInsnNode n = new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(BooleanHelper.class), "instanceOf",
				        Type.getMethodDescriptor(Type.INT_TYPE,
				                                 new Type[] { Type.getType(Object.class),
				                                         Type.getType(Class.class) }));
				mn.instructions.insertBefore(typeNode, n);
				mn.instructions.remove(typeNode);
				return n;
			}
			return typeNode;
		}
	}

	/**
	 * Make sure bitwise operations on transformed Booleans are still valid
	 */
	private class BitwiseOperatorTransformer extends MethodNodeTransformer {
		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.InsnNode)
		 */
		@Override
		protected AbstractInsnNode transformInsnNode(MethodNode mn, InsnNode insnNode) {
			if (insnNode.getOpcode() == Opcodes.IOR
			        || insnNode.getOpcode() == Opcodes.IAND
			        || insnNode.getOpcode() == Opcodes.IXOR) {

				if (isBooleanOnStack(mn, insnNode, 0)
				        && isBooleanOnStack(mn, insnNode, 1)) {
					if (insnNode.getOpcode() == Opcodes.IOR) {
						MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class), "IOR",
						        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {
						                Type.INT_TYPE, Type.INT_TYPE }));
						mn.instructions.insertBefore(insnNode, push);
						mn.instructions.remove(insnNode);
						TransformationStatistics.transformedBitwise();
						return push;
					} else if (insnNode.getOpcode() == Opcodes.IAND) {
						MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class), "IAND",
						        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {
						                Type.INT_TYPE, Type.INT_TYPE }));
						mn.instructions.insertBefore(insnNode, push);
						mn.instructions.remove(insnNode);
						TransformationStatistics.transformedBitwise();
						return push;

					} else if (insnNode.getOpcode() == Opcodes.IXOR) {
						MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class), "IXOR",
						        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {
						                Type.INT_TYPE, Type.INT_TYPE }));
						mn.instructions.insertBefore(insnNode, push);
						mn.instructions.remove(insnNode);
						TransformationStatistics.transformedBitwise();
						return push;
					}
				}
			}
			return insnNode;
		}
	}

	/**
	 * Make sure arrays of booleans are also transformed
	 */
	private class BooleanArrayTransformer extends MethodNodeTransformer {
		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformIntInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.IntInsnNode)
		 */
		@Override
		protected AbstractInsnNode transformIntInsnNode(MethodNode mn,
		        IntInsnNode intInsnNode) {
			if (intInsnNode.operand == Opcodes.T_BOOLEAN) {
				intInsnNode.operand = Opcodes.T_INT;
			}
			return intInsnNode;
		}

		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformMultiANewArrayInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.MultiANewArrayInsnNode)
		 */
		@Override
		protected AbstractInsnNode transformMultiANewArrayInsnNode(MethodNode mn,
		        MultiANewArrayInsnNode arrayInsnNode) {
			String new_desc = "";
			Type t = Type.getType(arrayInsnNode.desc);
			while (t.equals(Type.ARRAY)) {
				new_desc += "[";
				t = t.getElementType();
			}
			if (t.equals(Type.BOOLEAN_TYPE))
				new_desc += "I";
			else
				new_desc += t.getDescriptor();
			arrayInsnNode.desc = new_desc;
			return arrayInsnNode;
		}

		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformTypeInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.TypeInsnNode)
		 */
		@Override
		protected AbstractInsnNode transformTypeInsnNode(MethodNode mn,
		        TypeInsnNode typeNode) {
			String new_desc = "";
			int pos = 0;
			while (pos < typeNode.desc.length() && typeNode.desc.charAt(pos) == '[') {
				new_desc += "[";
				pos++;
			}
			String d = typeNode.desc.substring(pos);
			logger.info("Unfolded arrays to: " + d);
			if (d.equals("Z"))
				//if (t.equals(Type.BOOLEAN_TYPE))
				new_desc += "I";
			else
				new_desc += d; //t.getInternalName();
			typeNode.desc = new_desc;
			return typeNode;
		}
	}

	/**
	 * Make sure array accesses of boolean arrays are also transformed
	 */
	private class BooleanArrayIndexTransformer extends MethodNodeTransformer {
		private final Frame[] frames;

		// TODO: Use currentFrames
		public BooleanArrayIndexTransformer(Frame[] frames) {
			this.frames = frames;
		}

		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.InsnNode)
		 */
		@Override
		protected AbstractInsnNode transformInsnNode(MethodNode mn, InsnNode insnNode) {
			if (frames == null) {
				return insnNode;
			}

			if (insnNode.getOpcode() == Opcodes.BALOAD) {
				Frame current = frames[mn.instructions.indexOf(insnNode)];
				int size = current.getStackSize();
				if (current.getStack(size - 2) == BooleanArrayInterpreter.INT_ARRAY) {
					logger.info("Array is of boolean type, changing BALOAD to IALOAD");
					InsnNode replacement = new InsnNode(Opcodes.IALOAD);
					mn.instructions.insertBefore(insnNode, replacement);
					mn.instructions.remove(insnNode);
					return replacement;
				}
			} else if (insnNode.getOpcode() == Opcodes.BASTORE) {
				Frame current = frames[mn.instructions.indexOf(insnNode)];
				int size = current.getStackSize();
				if (current.getStack(size - 3) == BooleanArrayInterpreter.INT_ARRAY) {
					logger.info("Array is of boolean type, changing BASTORE to IASTORE");
					InsnNode replacement = new InsnNode(Opcodes.IASTORE);
					mn.instructions.insertBefore(insnNode, replacement);
					mn.instructions.remove(insnNode);
					return replacement;
				}
			}
			return insnNode;
		}

		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformTypeInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.TypeInsnNode)
		 */
		@Override
		protected AbstractInsnNode transformTypeInsnNode(MethodNode mn,
		        TypeInsnNode typeNode) {
			if (frames == null)
				return typeNode;

			if (typeNode.getOpcode() == Opcodes.CHECKCAST) {
				Frame current = frames[mn.instructions.indexOf(typeNode)];
				int size = current.getStackSize();
				if (current.getStack(size - 1) == BooleanArrayInterpreter.INT_ARRAY) {
					logger.info("Array is of boolean type, changing CHECKCAST to [I");
					TypeInsnNode replacement = new TypeInsnNode(Opcodes.CHECKCAST, "[I");
					mn.instructions.insertBefore(typeNode, replacement);
					mn.instructions.remove(typeNode);
					return replacement;
				}
			}
			return typeNode;
		}
	}

	/**
	 * If a method needs to return a Boolean and not an int, then we need to
	 * transform the int back to a Boolean
	 */
	private class BooleanReturnTransformer extends MethodNodeTransformer {
		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.InsnNode)
		 */
		@Override
		protected AbstractInsnNode transformInsnNode(MethodNode mn, InsnNode insnNode) {
			String desc = descriptorMapping.getMethodDesc(className, mn.name, mn.desc);
			Type returnType = Type.getReturnType(desc);
			if (!returnType.equals(Type.BOOLEAN_TYPE))
				return insnNode;

			if (insnNode.getOpcode() == Opcodes.IRETURN) {
				// If this function cannot be transformed, add a call to convert the value to a proper Boolean
				MethodInsnNode n = new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(BooleanHelper.class), "intToBoolean",
				        Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
				                                 new Type[] { Type.INT_TYPE }));
				mn.instructions.insertBefore(insnNode, n);
			}

			return insnNode;
		}
	}

	/**
	 * Replace signatures of all calls/field accesses on Booleans
	 */
	private class BooleanCallsTransformer extends MethodNodeTransformer {
		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformMethodInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.MethodInsnNode)
		 */
		@Override
		protected AbstractInsnNode transformMethodInsnNode(MethodNode mn,
		        MethodInsnNode methodNode) {
			if (methodNode.owner.equals(Type.getInternalName(BooleanHelper.class)))
				return methodNode;

			methodNode.desc = transformMethodDescriptor(methodNode.owner,
			                                            methodNode.name, methodNode.desc);
			methodNode.name = descriptorMapping.getMethodName(className, methodNode.name,
			                                                  methodNode.desc);

			if (descriptorMapping.isBooleanMethod(methodNode.desc)) {
				if (descriptorMapping.hasBooleanParameters(methodNode.desc)) {
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
							//the boolean parameter is the last parameter
							MethodInsnNode booleanHelperInvoke = new MethodInsnNode(
							        Opcodes.INVOKESTATIC,
							        Type.getInternalName(BooleanHelper.class),
							        "intToBoolean",
							        Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
							                                 new Type[] { Type.INT_TYPE }));
							mn.instructions.insertBefore(methodNode, booleanHelperInvoke);
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
									/*
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
									*/
								}

							}
							mn.instructions.insertBefore(methodNode, insnlist);
						}
					}
				}
				if (Type.getReturnType(methodNode.desc).equals(Type.BOOLEAN_TYPE)) {
					TransformationStatistics.transformBackToBooleanParameter();
					MethodInsnNode n = new MethodInsnNode(Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "booleanToInt",
					        Type.getMethodDescriptor(Type.INT_TYPE,
					                                 new Type[] { Type.BOOLEAN_TYPE }));
					mn.instructions.insert(methodNode, n);
				}
			}

			// TODO: If this is a method that is not transformed, and it requires a Boolean parameter
			// then we need to convert this boolean back to an int
			// For example, we could use flow analysis to determine the point where the value is added to the stack
			// and insert a conversion function there
			return methodNode;
		}

		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformFieldInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.FieldInsnNode)
		 */
		@Override
		protected AbstractInsnNode transformFieldInsnNode(MethodNode mn,
		        FieldInsnNode fieldNode) {

			// TODO: If the field owner is not transformed, then convert this to a proper Boolean
			fieldNode.desc = transformFieldDescriptor(fieldNode.owner, fieldNode.name,
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
				}
			}
			return fieldNode;
		}
	}

}
