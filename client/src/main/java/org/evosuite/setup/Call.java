package org.evosuite.setup;

import java.io.Serializable;

public class Call implements Serializable {

	/**
	 * Call of call context
	 * TODO this class is approximated and does not consider the method signature
	 * mattia
	 */
	private static final long serialVersionUID = -8148115191773499144L;
	private final String className;
	private final String methodName;
	private final int hcode;
	private final int approxHcode;

	public Call(String classname, String methodName) {
		this.className = classname;
		this.methodName = methodName;
		approxHcode = computeApproximatedHashCode();
		hcode = computeHashCode();
	}

	public Call(Call call) {
		this.className = call.className;
		this.methodName = call.methodName;
		approxHcode = computeApproximatedHashCode();
		hcode = computeHashCode();
	}

	private int computeApproximatedHashCode() {
		String mname = methodName;
		if (mname.contains("("))
			mname = mname.substring(0, mname.indexOf("("));
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((mname == null) ? 0 : mname.hashCode());
		return result;
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	@Override
	public int hashCode() {
		return approxHcode;
	}

	private int computeHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
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
		Call other = (Call) obj;
		if (hcode == other.hcode)
			return true;
		return false;
	}

	// TODO: Could consider line number?
	public boolean matches(Call other) {
		if (approxHcode == other.approxHcode)
			return true;
		return false;
	}

	@Override
	public String toString() {
		return className + ":" + methodName;
	}

}