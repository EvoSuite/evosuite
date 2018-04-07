/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import java.util.*;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ListInstrumentation extends ErrorBranchInstrumenter {

	private static final String LIST = List.class.getCanonicalName().replace('.', '/');
	private static final String ARRAYLIST = ArrayList.class.getCanonicalName().replace('.', '/');
	private static final String LINKEDLIST = LinkedList.class.getCanonicalName().replace('.', '/');
	private static final List<String> LISTS = Arrays.asList(LIST, ARRAYLIST, LINKEDLIST);
	
	private final List<String> indexListMethods = Arrays.asList(new String[] {"get", "set", "add", "remove", "listIterator", "addAll"});
	// overloaded version of add(Element, Index) and add(Index, Collection) is considered here.
	// TODO: listIterator has no method which throws IndexOutOfBoundsException. Hence, we may ignore it or add an appropriate instrumentation.

	private final List<String> emptyListMethods = Arrays.asList(new String[] {"getFirst", "getLast", "removeFirst", "removeLast", "element", "pop"});

	// Missing: subList, removeRange

	public ListInstrumentation(ErrorConditionMethodAdapter mv) {
		super(mv);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {

		if (LISTS.contains(owner)) {
			if (emptyListMethods.contains(name)) {
				// empty
				Map<Integer, Integer> tempVariables = getMethodCallee(desc);

				//tagBranchStart();
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner,
						"isEmpty", "()Z", false);
				insertBranch(Opcodes.IFLE, "java/util/NoSuchElementException");
				//tagBranchEnd();
				restoreMethodParameters(tempVariables, desc);

			} else if (indexListMethods.contains(name)) {
				Type[] args = Type.getArgumentTypes(desc);
				if (args.length == 0)
					return;
				if (!args[0].equals(Type.INT_TYPE))
					return;

				Map<Integer, Integer> tempVariables = getMethodCallee(desc);
				tagBranchStart();
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner,
						"size", "()I", false);

				// index >= size
				mv.loadLocal(tempVariables.get(0));
				insertBranch(Opcodes.IF_ICMPGT, "java/lang/IndexOutOfBoundsException");

				// index < 0
				mv.loadLocal(tempVariables.get(0));
				insertBranch(Opcodes.IFGE, "java/lang/IndexOutOfBoundsException");
				tagBranchEnd();

				restoreMethodParameters(tempVariables, desc);
			}
		}
	}
}
