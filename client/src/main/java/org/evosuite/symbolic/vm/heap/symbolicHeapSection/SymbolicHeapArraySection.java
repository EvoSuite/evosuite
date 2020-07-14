/**
 * Copyright (C) 2010-2020 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.vm.heap.symbolicHeapSection;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.expr.ref.ReferenceVariable;
import org.evosuite.symbolic.expr.str.StringValue;
import org.objectweb.asm.Type;

/**
 * General interface for the arrays memory model.
 * TODO: In the future we can do an implementation similar to the VMs, a general symbolicHeap with all the methods
 *       and an implementation of each section (e.g. AbstractSymbolicHeap -> ArraysHeap -> ReferencesHeap -> etc...
 *
 * @author Ignacio Lebrero
 */
public interface SymbolicHeapArraySection {

	/** Instances creation and initialization */
	ReferenceVariable createVariableArray(Object concreteArray, int instanceId, String name);
  ReferenceConstant createConstantArray(Type arrayType, int instanceId);
  void initializeArrayReference(ReferenceExpression symbolicArray);

  /** Load operations */
	RealValue arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, RealValue symbolicValue);
	StringValue arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, StringValue symbolicValue);
	IntegerValue arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, IntegerValue symbolicValue);
  ReferenceExpression arrayLoad(ReferenceExpression symbolicArray, IntegerValue symbolicIndex, ReferenceExpression symbolicValue);

  /** Store operations */
	void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex, RealValue symbolicValue);
	void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex, StringValue symbolicValue);
	void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex, IntegerValue symbolicValue);
	void arrayStore(Object concreteArray, ReferenceExpression symbolicArray, IntegerValue symbolicIndex, ReferenceExpression symbolicValue);


}
