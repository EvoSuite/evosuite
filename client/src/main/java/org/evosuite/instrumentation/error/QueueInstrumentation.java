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
package org.evosuite.instrumentation.error;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.objectweb.asm.Opcodes;

public class QueueInstrumentation extends ErrorBranchInstrumenter {
	
	private static final String LISTNAME = Queue.class.getCanonicalName().replace('.', '/');
	
	private final List<String> emptyListMethods = Arrays.asList(new String[] {"remove", "element" });

	public QueueInstrumentation(ErrorConditionMethodAdapter mv) {
		super(mv);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		if(owner.equals(LISTNAME)) {
			if(emptyListMethods.contains(name)) {
				// empty
				Map<Integer, Integer> tempVariables = getMethodCallee(desc);

				tagBranchStart();
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, LISTNAME,
	                      "isEmpty", "()Z", false);
				insertBranchWithoutTag(Opcodes.IFLE, "java/util/NoSuchElementException");
				tagBranchEnd();
				restoreMethodParameters(tempVariables, desc);
			} 
		}
	}
}
