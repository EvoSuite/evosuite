package org.evosuite.result;

import java.io.Serializable;

import org.evosuite.coverage.mutation.Mutation;

public class MutationInfo implements Serializable {
	
	private static final long serialVersionUID = 4580001065523289191L;

	private String className;

	private String methodName;

	int lineNo;

	private String replacement;

	public MutationInfo(Mutation m) {
		this.className = m.getClassName();
		this.methodName = m.getMethodName();
		this.lineNo = m.getLineNumber();
		this.replacement = m.getMutationName();
	}
	
	public MutationInfo(String className, String methodName, int lineNo,
			String replacement) {
		this.className = className;
		this.methodName = methodName;
		this.lineNo = lineNo;
		this.replacement = replacement;
	}

	public String getClassName() {
		return className;
	}
	
	public int getLineNo() {
		return lineNo;
	}
	
	public String getMethodName() {
		return methodName;
	}
		
	public String getReplacement() {
		return replacement;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result + lineNo;
		result = prime * result
				+ ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result
				+ ((replacement == null) ? 0 : replacement.hashCode());
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
		MutationInfo other = (MutationInfo) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (lineNo != other.lineNo)
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		if (replacement == null) {
			if (other.replacement != null)
				return false;
		} else if (!replacement.equals(other.replacement))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MutationInfo [className=" + className + ", methodName="
				+ methodName + ", lineNo=" + lineNo + ", replacement="
				+ replacement + "]";
	}
	
	
}
