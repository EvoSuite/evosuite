/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author Gordon Fraser
 * 
 */
public class RemoveFinalMethodAdapter extends MethodVisitor {

	private final List<String> finalFields;

	private final String className;

	/**
	 * @param mv
	 */
	public RemoveFinalMethodAdapter(String className, MethodVisitor mv,
	        List<String> finalFields) {
		super(Opcodes.ASM4, mv);
		this.finalFields = finalFields;
		this.className = className;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodAdapter#visitFieldInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		if ((opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC)
		        && owner.equals(className)) {

			if (!finalFields.contains(name)) {
				//System.out.println("Keeping non-final field " + name + " in class "
				//        + owner);
				super.visitFieldInsn(opcode, owner, name, desc);
			} else {
				//System.out.println("Omitting final field " + name + " in class " + owner);
				Type type = Type.getType(desc);
				if (type.getSize() == 1)
					super.visitInsn(Opcodes.POP);
				else if (type.getSize() == 2)
					super.visitInsn(Opcodes.POP2);
			}
		} else {
			//if (!owner.equals(className))
			//	System.out.println("Mismatch: " + className + " / " + owner);
			super.visitFieldInsn(opcode, owner, name, desc);
		}
	}
}
