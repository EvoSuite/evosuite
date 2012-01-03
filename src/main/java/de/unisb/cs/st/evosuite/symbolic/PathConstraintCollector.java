/*
 * Copyright (C) 2011 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.symbolic;


import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.bytecode.IfInstruction;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.jvm.bytecode.SwitchInstruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import gov.nasa.jpf.JPF;
import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.bytecode.PathConstraint;
import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstraint;

/**
 * @author Jan Malburg
 * 
 */
public class PathConstraintCollector extends ListenerAdapter {

	//@SuppressWarnings("unused")
	//private static Logger logger = LoggerFactory.getLogger(PathConstraintCollector.class);
	
	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic");
	
	private HashTableSet<Constraint<?>> last;

	public List<BranchCondition> conditions = new ArrayList<BranchCondition>();

	protected static class MyComparator implements java.util.Comparator<Constraint<?>> {

		@Override
		public int compare(Constraint<?> o1, Constraint<?> o2) {
			IntegerConstraint a1 = (IntegerConstraint) o1;
			IntegerConstraint a2 = (IntegerConstraint) o2;
			return Long.signum(((IntegerConstant) a1.getRightOperand()).getConcreteValue()
			        - ((IntegerConstant) a2.getRightOperand()).getConcreteValue());
		}
	}

	@Override
	public void executeInstruction(JVM vm) {
		Instruction ins = vm.getNextInstruction();
		if (ins instanceof IfInstruction || ins instanceof SwitchInstruction) {
			last = PathConstraint.getInstance().getCurrentConstraints();
		}
	}

	@Override
	public void instructionExecuted(JVM vm) {
		if (vm.getLastInstruction() instanceof IfInstruction) {
			HashTableSet<Constraint<?>> current = PathConstraint.getInstance().getCurrentConstraints();
			if (current.size() == 0) {
				//no constraints yet -> not in our function
				return;
			}
			if (current.size() == last.size()) {
				//logger.info("Double call");
				return;//double call; // FIXXME: What??
			}

			Set<Constraint<?>> mylast = last;
			Set<Constraint<?>> local = getSetOfNewConstraints(last, current);
			BranchCondition bc = new BranchCondition(vm.getLastInstruction(), mylast,
			        local);
			this.conditions.add(bc);

		} else if (vm.getLastInstruction() instanceof SwitchInstruction) {
			HashTableSet<Constraint<?>> current = PathConstraint.getInstance().getCurrentConstraints();
			if (current.size() == 0) {
				return;
			}
			if (last.containsAll(current)) {
				return;//double call;
			}
			Set<Constraint<?>> mylast = last;
			Set<Constraint<?>> local = getSetOfNewConstraints(last, current);
			BranchCondition bc = new BranchCondition(vm.getLastInstruction(), mylast,
			        local);
			this.conditions.add(bc);
		}
	}

	@SuppressWarnings("unchecked")
	protected Set<Constraint<?>> getSetOfNewConstraints(HashTableSet<Constraint<?>> oldC,
	        HashTableSet<Constraint<?>> newC) {
		HashTableSet<Constraint<?>> ret = (HashTableSet<Constraint<?>>) newC.clone();
		ret.removeAll(oldC);
		return ret;
	}

}
