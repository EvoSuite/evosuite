package org.evosuite.coverage.dataflow.analysis;

import org.evosuite.graphs.cfg.BytecodeInstruction;

/**
 * A VariableDefinition consisting of a defining BytecodeInstruction and a
 * MethodCall.
 * 
 * Used in Inter-Method pair search algorithm to differentiate between
 * Intra-Method pairs and Inter-Method Pairs.
 * 
 * More or less just a pair of a BytecodeInstruction and a Methodcall.
 * 
 * @author Andre Mis
 */
public class VariableDefinition {
	private final BytecodeInstruction definition;
	private final MethodCall call;

	public VariableDefinition(BytecodeInstruction definition,
			MethodCall call) {
		this.definition = definition;
		this.call = call;
	}

	public BytecodeInstruction getDefinition() {
		return definition;
	}

	public MethodCall getMethodCall() {
		return call;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((call == null) ? 0 : call.hashCode());
		result = prime * result
				+ ((definition == null) ? 0 : definition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariableDefinition other = (VariableDefinition) obj;
		if (call == null) {
			if (other.call != null)
				return false;
		} else if (!call.equals(other.call))
			return false;
		if (definition == null) {
			if (other.definition != null)
				return false;
		} else if (!definition.equals(other.definition))
			return false;
		return true;
	}

	public String toString() {
		return definition.toString() + " in " + call.toString();
	}
}