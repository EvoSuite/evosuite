package org.evosuite.symbolic.solver.smt;

public abstract class SmtVariable extends SmtExpr {

	protected final String varName;

	public SmtVariable(String varName) {
		this.varName = varName;
	}

	public String getName() {
		return varName;
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((varName == null) ? 0 : varName.hashCode());
		return result;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SmtVariable other = (SmtVariable) obj;
		if (varName == null) {
			if (other.varName != null)
				return false;
		} else if (!varName.equals(other.varName))
			return false;
		return true;
	}
	
	@Override
	public final boolean isSymbolic() {
		return true;
	}

}
