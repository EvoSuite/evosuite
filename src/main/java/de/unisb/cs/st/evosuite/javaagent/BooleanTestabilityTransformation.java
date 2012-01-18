/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.cfg.BasicBlock;
import de.unisb.cs.st.evosuite.cfg.BytecodeAnalyzer;
import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.cfg.BytecodeInstructionFactory;
import de.unisb.cs.st.evosuite.cfg.BytecodeInstructionPool;
import de.unisb.cs.st.evosuite.cfg.CFGPool;
import de.unisb.cs.st.evosuite.cfg.ControlDependenceGraph;
import de.unisb.cs.st.evosuite.cfg.ControlDependency;
import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;

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

		return cn;
	}

	/**
	 * Handle transformation of fields defined in this class
	 */
	private void processFields() {
		List<FieldNode> fields = cn.fields;
		for (FieldNode field : fields) {
			field.desc = transformFieldDescriptor(field.desc);
		}
	}

	/**
	 * Handle transformation of methods defined in this class
	 */
	private void processMethods() {
		List<MethodNode> methodNodes = cn.methods;
		for (MethodNode mn : methodNodes) {
			transformMethodSignature(mn);
			transformMethod(mn);
		}
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

	private boolean isBooleanOnStack(MethodNode mn, AbstractInsnNode node, int position) {
		int insnPosition = mn.instructions.indexOf(node);
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
			/*
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
			} else
			*/
			if (node.getOpcode() == Opcodes.ISTORE) {
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
				/*} else if (node.getOpcode() == Opcodes.IRETURN) {
					logger.info("Checking return value of method " + cn.name + "." + mn.name);
					if (mapping.isTransformedOrBooleanMethod(cn.name, mn.name, mn.desc)) {
						return true;
					} else {
						return false;
					}*/
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

		// compute Raw and ActualCFG and put both into CFGPool
		bytecodeAnalyzer.retrieveCFGGenerator().registerCFGs();
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
		if (desc.endsWith("Z")) {
			// TODO: Check if this is actually transformed or not
			// TODO: Higher dimensional arrays
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

	private void transformMethodSignature(MethodNode mn) {
		// If the method was declared in java.* then don't instrument
		// Otherwise change signature
		mn.desc = transformMethodDescriptor(mn.desc);
	}

	/**
	 * Apply testability transformation to an individual method
	 * 
	 * @param mn
	 */
	private void transformMethod(MethodNode mn) {
		logger.info("Transforming method " + mn.name + mn.desc);

		//currentCFG = CFGPool.getActualCFG(className, mn.name + mn.desc);

		// TODO: Skipping interfaces for now, but will need to handle Booleans in interfaces!
		if ((mn.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT)
			return;

		try {
			Analyzer a = new Analyzer(new BooleanValueInterpreter());
			a.analyze(className, mn);
			currentFrames = a.getFrames();
		} catch (Exception e) {
			logger.warn("Error during analysis: " + e);
			// TODO: Handle error
		}
		generateCDG(mn);
		currentMethodNode = mn;

		// First expand ifs without else
		new ImplicitElseTransformer().transform(mn);
		try {
			Analyzer a = new Analyzer(new BooleanValueInterpreter());
			a.analyze(className, mn);
			currentFrames = a.getFrames();
		} catch (Exception e) {
			logger.warn("Error during analysis: " + e);
			// TODO: Handle error
		}
		//		BytecodeInstructionPool.reRegisterMethodNode(mn, className, mn.name + mn.desc);

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

		// Replace all boolean arrays
		// new BooleanArrayTransformer().transform(mn);

		// Replace all bitwise operators
		logger.info("Transforming Boolean bitwise operators");
		new BitwiseOperatorTransformer().transform(mn);

		// Replace all boolean return values
		// new BooleanReturnTransformer().transform(mn);

		// Actually this should be done automatically by the ClassWriter...
		// +2 because we might do a DUP2
		CFGPool.clear(className, mn.name + mn.desc);
		BytecodeInstructionPool.clear(className, mn.name + mn.desc);
		BranchPool.clear(className, mn.name + mn.desc);

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
			logger.info("Checking transformation of InsnNode");
			if (insnNode.getOpcode() == Opcodes.ICONST_0
			        && isBooleanAssignment(insnNode, mn)) {
				insertGet(insnNode, mn.instructions);
			} else if (insnNode.getOpcode() == Opcodes.ICONST_1
			        && isBooleanAssignment(insnNode, mn)) {
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
				insertPush(jumpNode.getOpcode(), jumpNode, mn.instructions);
				break;
			case Opcodes.IF_ICMPEQ:
			case Opcodes.IF_ICMPNE:
			case Opcodes.IF_ICMPLT:
			case Opcodes.IF_ICMPGE:
			case Opcodes.IF_ICMPGT:
			case Opcodes.IF_ICMPLE:
				insertPush2(jumpNode.getOpcode(), jumpNode, mn.instructions);
				break;
			case Opcodes.IFNULL:
			case Opcodes.IFNONNULL:
				insertPushNull(jumpNode.getOpcode(), jumpNode, mn.instructions);
				break;
			case Opcodes.IF_ACMPEQ:
			case Opcodes.IF_ACMPNE:
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
					logger.info("Changing IFNE");
					jumpNode.setOpcode(Opcodes.IFGT);
				}
			} else if (jumpNode.getOpcode() == Opcodes.IFEQ) {
				if (isBooleanOnStack(mn, jumpNode, 0)) {
					logger.info("Changing IFEQ");
					jumpNode.setOpcode(Opcodes.IFLE);
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

		private void handleDependency(ControlDependency dependency,
		        ControlDependenceGraph cdg, MethodNode mn, VarInsnNode varNode,
		        BytecodeInstruction parentLevel) {
			Set<BasicBlock> blocks = cdg.getAlternativeBlocks(dependency);
			addedNodes.contains(dependency);

			Set<ControlDependency> dependencies = dependency.getBranch().getInstruction().getControlDependencies();
			if (dependencies.size() == 1) {
				ControlDependency dep = dependencies.iterator().next();
				if (!addedNodes.contains(dep) && dep != dependency)
					handleDependency(dep, cdg, mn, varNode,
					                 dependency.getBranch().getInstruction());
			}

			// TODO: Need to check that there is an assignment in every alternative path through CDG

			boolean hasAssignment = false;
			for (BasicBlock block : blocks) {
				// If this block also assigns a value to the same variable
				for (BytecodeInstruction instruction : block) {
					if (instruction.getASMNode().getOpcode() == Opcodes.ILOAD) {
						VarInsnNode otherVarNode = (VarInsnNode) instruction.getASMNode();
						if (otherVarNode.var == varNode.var) {
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
				JumpInsnNode jumpNode = (JumpInsnNode) dependency.getBranch().getInstruction().getASMNode();
				VarInsnNode newStore = new VarInsnNode(Opcodes.ISTORE, varNode.var);
				VarInsnNode newLoad = new VarInsnNode(Opcodes.ILOAD, varNode.var);
				if (dependency.getBranchExpressionValue()) {
					// Insert directly after if
					mn.instructions.insert(jumpNode, newStore);
					mn.instructions.insert(jumpNode, newLoad);
					BytecodeInstruction instruction = BytecodeInstructionFactory.createBytecodeInstruction(className,
					                                                                                       mn.name
					                                                                                               + mn.desc,
					                                                                                       BytecodeInstructionPool.getInstruction(className,
					                                                                                                                              mn.name
					                                                                                                                                      + mn.desc,
					                                                                                                                              jumpNode).getInstructionId(),
					                                                                                       0,
					                                                                                       newStore);
					instruction.setBasicBlock(BytecodeInstructionPool.getInstruction(className,
					                                                                 mn.name
					                                                                         + mn.desc,
					                                                                 varNode).getBasicBlock());
					BytecodeInstructionPool.registerInstruction(instruction);
					instruction = BytecodeInstructionFactory.createBytecodeInstruction(className,
					                                                                   mn.name
					                                                                           + mn.desc,
					                                                                   BytecodeInstructionPool.getInstruction(className,
					                                                                                                          mn.name
					                                                                                                                  + mn.desc,
					                                                                                                          jumpNode).getInstructionId(),
					                                                                   0,
					                                                                   newLoad);
					instruction.setBasicBlock(BytecodeInstructionPool.getInstruction(className,
					                                                                 mn.name
					                                                                         + mn.desc,
					                                                                 varNode).getBasicBlock());
					BytecodeInstructionPool.registerInstruction(instruction);

				} else {
					// Insert as jump target
					LabelNode target = jumpNode.label;
					LabelNode newTarget = new LabelNode(new Label());

					BytecodeInstruction instruction = BytecodeInstructionFactory.createBytecodeInstruction(className,
					                                                                                       mn.name
					                                                                                               + mn.desc,
					                                                                                       BytecodeInstructionPool.getInstruction(className,
					                                                                                                                              mn.name
					                                                                                                                                      + mn.desc,
					                                                                                                                              target).getInstructionId(),
					                                                                                       0,
					                                                                                       newStore);
					instruction.setBasicBlock(parentLevel.getBasicBlock());

					BytecodeInstructionPool.registerInstruction(instruction);
					instruction = BytecodeInstructionFactory.createBytecodeInstruction(className,
					                                                                   mn.name
					                                                                           + mn.desc,
					                                                                   BytecodeInstructionPool.getInstruction(className,
					                                                                                                          mn.name
					                                                                                                                  + mn.desc,
					                                                                                                          target).getInstructionId(),
					                                                                   0,
					                                                                   newLoad);
					instruction.setBasicBlock(parentLevel.getBasicBlock());
					BytecodeInstructionPool.registerInstruction(instruction);

					InsnList assignment = new InsnList();
					assignment.add(new JumpInsnNode(Opcodes.GOTO, target));
					assignment.add(newTarget);
					assignment.add(newLoad);
					assignment.add(newStore);
					jumpNode.label = newTarget;

					mn.instructions.insertBefore(target, assignment);
				}

				//				insertTautologicalElse(mn,
				//				                       (JumpInsnNode) dependency.getBranch().getInstruction().getASMNode());
			}

		}

		/* (non-Javadoc)
		 * @see de.unisb.cs.st.evosuite.javaagent.MethodNodeTransformer#transformVarInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.VarInsnNode)
		 */
		@Override
		protected AbstractInsnNode transformVarInsnNode(MethodNode mn, VarInsnNode varNode) {
			if (varNode.getOpcode() == Opcodes.ISTORE
			        && isBooleanVariable(varNode.var, mn)) {

				// Check if ICONST_0 or ICONST_1 are on the stack
				ControlDependenceGraph cdg = CFGPool.getCDG(className.replace("/", "."),
				                                            mn.name + mn.desc);
				int index = mn.instructions.indexOf(varNode);
				BytecodeInstruction insn = BytecodeInstructionPool.getInstruction(className.replace("/",
				                                                                                    "."),
				                                                                  mn.name
				                                                                          + mn.desc,
				                                                                  index);
				//varNode);
				logger.info("Found instruction: " + insn);
				if (insn.getASMNode().getOpcode() != varNode.getOpcode()) {
					logger.warn("Found wrong bytecode instruction at this index!");
					BytecodeInstructionPool.getInstruction(className, mn.name + mn.desc,
					                                       varNode);
				}
				Set<ControlDependency> dependencies = insn.getControlDependencies();
				// Only do completion if there's only one dependency
				// Not sure how other cases would look like
				if (dependencies.size() > 1)
					return varNode;
				else if (dependencies.isEmpty())
					return varNode;

				ControlDependency dep = dependencies.iterator().next();
				if (!addedNodes.contains(dep))
					handleDependency(dep, cdg, mn, varNode, insn);
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
				// Depending on the class version we need a String or a Class
				if (cn.version > 49) {
					LdcInsnNode lin = new LdcInsnNode(Type.getType("L" + typeNode.desc
					        + ";"));
					mn.instructions.insertBefore(typeNode, lin);
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
					} else if (insnNode.getOpcode() == Opcodes.IAND) {
						MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class), "IAND",
						        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {
						                Type.INT_TYPE, Type.INT_TYPE }));
						mn.instructions.insertBefore(insnNode, push);
						mn.instructions.remove(insnNode);

					} else if (insnNode.getOpcode() == Opcodes.IXOR) {
						MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class), "IXOR",
						        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {
						                Type.INT_TYPE, Type.INT_TYPE }));
						mn.instructions.insertBefore(insnNode, push);
						mn.instructions.remove(insnNode);
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
			if (insnNode.getOpcode() == Opcodes.IRETURN) {
				// If this function cannot be transformed, add a call to convert the value to a proper Boolean
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
			methodNode.desc = transformMethodDescriptor(methodNode.desc);
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
			fieldNode.desc = transformFieldDescriptor(fieldNode.desc);
			return fieldNode;
		}
	}

}
