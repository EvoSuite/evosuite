package org.evosuite.instrumentation.error;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.evosuite.instrumentation.ErrorConditionMethodAdapter;
import org.objectweb.asm.Opcodes;

public class DequeInstrumentation extends ErrorBranchInstrumenter {

	private final List<String> listNames = Arrays.asList(new String[] {Deque.class.getCanonicalName().replace('.', '/'), LinkedBlockingDeque.class.getCanonicalName().replace('.', '/'), BlockingDeque.class.getCanonicalName().replace('.', '/'), ArrayDeque.class.getCanonicalName().replace('.', '/')});

	private final List<String> emptyListMethods = Arrays.asList(new String[] {"getFirst", "getLast", "removeFirst", "removeLast", "remove", "element", "pop"});

	public DequeInstrumentation(ErrorConditionMethodAdapter mv) {
		super(mv);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {
		if(listNames.contains(owner)) {
			if(emptyListMethods.contains(name)) {
				// empty
				Map<Integer, Integer> tempVariables = getMethodCallee(desc);

				tagBranchStart();
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner,
	                      "isEmpty", "()Z");
				insertBranchWithoutTag(Opcodes.IFLE, "java/util/NoSuchElementException");
				tagBranchEnd();
				restoreMethodParameters(tempVariables, desc);
				
			} 
		}
	}
}
