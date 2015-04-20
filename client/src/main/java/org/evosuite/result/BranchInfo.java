package org.evosuite.result;

import java.io.Serializable;

import org.evosuite.coverage.branch.Branch;

public class BranchInfo implements Serializable {

	private static final long serialVersionUID = -2145547942894978737L;

	private String className;
	
	private String methodName;
	
	private int lineNo;
	
	private boolean truthValue;

	public BranchInfo(Branch branch, boolean truthValue) {
		this.className = branch.getClassName();
		this.methodName = branch.getMethodName();
		this.lineNo = branch.getInstruction().getLineNumber();
		this.truthValue = truthValue;
	}
	
	public BranchInfo(String className, String methodName, int lineNo,
			boolean truthValue) {
		this.className = className;
		this.methodName = methodName;
		this.lineNo = lineNo;
		this.truthValue = truthValue;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public int getLineNo() {
		return lineNo;
	}

	public boolean getTruthValue() {
		return truthValue;
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
		result = prime * result + (truthValue ? 1231 : 1237);
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
		BranchInfo other = (BranchInfo) obj;
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
		if (truthValue != other.truthValue)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BranchInfo [className=" + className + ", methodName="
				+ methodName + ", lineNo=" + lineNo + ", truthValue="
				+ truthValue + "]";
	}
	
	
}
