/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

/**
 * @author Gordon Fraser
 * 
 */
public class ThisInterpreter extends BasicInterpreter {

	public final static BasicValue THIS_VALUE = new BasicValue(Type.INT_TYPE);

	/* (non-Javadoc)
	 * @see org.objectweb.asm.tree.analysis.BasicInterpreter#copyOperation(org.objectweb.asm.tree.AbstractInsnNode, org.objectweb.asm.tree.analysis.BasicValue)
	 */
	@Override
	public BasicValue copyOperation(AbstractInsnNode insn, BasicValue value)
	        throws AnalyzerException {
		if (insn.getOpcode() == Opcodes.ALOAD) {
			VarInsnNode varNode = (VarInsnNode) insn;
			if (varNode.var == 0) {
				return THIS_VALUE;
			}
		}
		return super.copyOperation(insn, value);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.tree.analysis.BasicInterpreter#merge(org.objectweb.asm.tree.analysis.BasicValue, org.objectweb.asm.tree.analysis.BasicValue)
	 */
	@Override
	public BasicValue merge(BasicValue v, BasicValue w) {
		if (v == THIS_VALUE && w == BasicValue.REFERENCE_VALUE)
			return BasicValue.REFERENCE_VALUE;
		else if (w == THIS_VALUE && v == BasicValue.REFERENCE_VALUE)
			return BasicValue.REFERENCE_VALUE;
		else
			return super.merge(v, w);
	}

}
