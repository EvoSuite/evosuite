package org.evosuite.instrumentation.error;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.evosuite.instrumentation.ErrorConditionMethodAdapter;
import org.objectweb.asm.Opcodes;

public class StackInstrumentation extends ErrorBranchInstrumenter {

private static final String LISTNAME = Stack.class.getCanonicalName().replace('.', '/');
	
	private final List<String> emptyStackMethods = Arrays.asList(new String[] {"pop", "peek"});

	public StackInstrumentation(ErrorConditionMethodAdapter mv) {
		super(mv);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {
		if(owner.equals(LISTNAME)) {
			if(emptyStackMethods.contains(name)) {
				// empty
				Map<Integer, Integer> tempVariables = getMethodCallee(desc);

				tagBranchStart();
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, LISTNAME,
	                      "empty", "()Z");
				insertBranchWithoutTag(Opcodes.IFLE, "java/util/EmptyStackException");
				tagBranchEnd();
				restoreMethodParameters(tempVariables, desc);
				
			} 
		}
	}
}
