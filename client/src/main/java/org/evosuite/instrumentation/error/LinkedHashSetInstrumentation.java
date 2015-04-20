package org.evosuite.instrumentation.error;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;

public class LinkedHashSetInstrumentation extends ErrorBranchInstrumenter {
	
	private final String SETNAME = LinkedHashSet.class.getCanonicalName().replace('.', '/');
	
	private final List<String> emptyListMethods = Arrays.asList(new String[] {"first", "last"});

	public LinkedHashSetInstrumentation(ErrorConditionMethodAdapter mv) {
		super(mv);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		if(owner.equals(SETNAME)) {
			if(emptyListMethods.contains(name)) {
				// empty
				Map<Integer, Integer> tempVariables = getMethodCallee(desc);

				tagBranchStart();
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, SETNAME,
	                      "isEmpty", "()Z", false);
				insertBranchWithoutTag(Opcodes.IFLE, "java/util/NoSuchElementException");
				tagBranchEnd();
				restoreMethodParameters(tempVariables, desc);
				
			} 
		}
	}
}
