package org.evosuite.instrumentation.error;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.evosuite.instrumentation.ErrorConditionMethodAdapter;
import org.objectweb.asm.Opcodes;

public class QueueInstrumentation extends ErrorBranchInstrumenter {
	
	private static final String LISTNAME = Queue.class.getCanonicalName().replace('.', '/');
	
	private final List<String> emptyListMethods = Arrays.asList(new String[] {"remove", "element" });

	public QueueInstrumentation(ErrorConditionMethodAdapter mv) {
		super(mv);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {
		if(owner.equals(LISTNAME)) {
			if(emptyListMethods.contains(name)) {
				// empty
				Map<Integer, Integer> tempVariables = getMethodCallee(desc);

				tagBranchStart();
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, LISTNAME,
	                      "isEmpty", "()Z");
				insertBranchWithoutTag(Opcodes.IFLE, "java/util/NoSuchElementException");
				tagBranchEnd();
				restoreMethodParameters(tempVariables, desc);
			} 
		}
	}
}
