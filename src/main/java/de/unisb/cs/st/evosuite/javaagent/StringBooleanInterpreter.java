/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

/**
 * @author Gordon Fraser
 * 
 */
public class StringBooleanInterpreter extends BasicInterpreter {

	public final static BasicValue STRING_BOOLEAN = new BasicValue(null);

	/* (non-Javadoc)
	 * @see org.objectweb.asm.tree.analysis.BasicInterpreter#naryOperation(org.objectweb.asm.tree.AbstractInsnNode, java.util.List)
	 */
	@Override
	public BasicValue naryOperation(AbstractInsnNode insn,
	        @SuppressWarnings("rawtypes") List values) throws AnalyzerException {
		if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
			MethodInsnNode mn = (MethodInsnNode) insn;
			if (mn.owner.equals(Type.getInternalName(BooleanHelper.class))
			        && mn.name.startsWith("String")) {
				return STRING_BOOLEAN;
			}
		}
		return super.naryOperation(insn, values);
	}

}
