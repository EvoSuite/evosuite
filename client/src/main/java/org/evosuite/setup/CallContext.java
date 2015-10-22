/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.setup;

import org.evosuite.PackageInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * CallContext class.
 * </p>
 * 
 * @author Gordon Fraser
 */

/**
 * TODO THIS IS APPROXIMATED call context computed at runtime DO NOT consider
 * the method signature, but only the method name. Currently, callcontext with
 * and without signature are considered equal
 * 
 * @author mattia
 *
 */
public class CallContext implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8650619230188403356L;

    private final List<Call> context;

	private final int hcode;

	public boolean isEmpty() {
		return context.isEmpty();
	}

	/**
	 * <p>
	 * Constructor for CallContext.
	 * </p>
	 * 
	 * @param stackTrace
	 *            an array of {@link java.lang.StackTraceElement} objects.
	 */
	public CallContext(StackTraceElement[] stackTrace) {
		int startPos = stackTrace.length - 1;
		List<Call> context = new ArrayList<Call>();
		while (stackTrace[startPos].getClassName().startsWith("java")
				|| stackTrace[startPos].getClassName().startsWith("sun")
				|| stackTrace[startPos].getClassName().startsWith(PackageInfo.getEvoSuitePackage())) {
			startPos--;
		}
		int endPos = 0;
		while (stackTrace[endPos].getClassName().startsWith("java")
				|| stackTrace[endPos].getClassName().startsWith("sun")
				|| stackTrace[endPos].getClassName().startsWith(PackageInfo.getEvoSuitePackage())) {
			endPos++;
		}

		for (int i = startPos; i >= endPos; i--) {
			StackTraceElement element = stackTrace[i];
			
			context.add(new Call(element.getClassName(), element.getMethodName()));
		} 
		this.context=context;
		hcode = this.context.hashCode();
	}

	/**
	 * Constructor for public methods
	 * 
	 * @param className
	 * @param methodName
	 */
	public CallContext(String className, String methodName) {
		List<Call> context = new ArrayList<Call>();
		context.add(new Call(className, methodName));
		this.context=context;
		hcode = this.context.hashCode();
	}
	
	public CallContext() {
		List<Call> context = new ArrayList<Call>();
		this.context=context;
		hcode = this.context.hashCode();
	}

	public CallContext(Collection<Call> contextt) {
		List<Call> context = new ArrayList<Call>();
		context.addAll(contextt);
		this.context=context;
		hcode = this.context.hashCode();
	}

	public int size() {
		return context.size();

	}

	/**
	 * attach the className-methodname pair passed as parameter before the
	 * current context.
	 **/
	@Deprecated
	public CallContext getSuperContext(String className, String methodName) {
		throw new IllegalStateException("YET TO IMPLEMENT, DEPRECATED");
	}

	/**
	 * <p>
	 * getRootClassName
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getRootClassName() {
		return context.get(0).getClassName();
	}

	/**
	 * <p>
	 * getRootMethodName
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getRootMethodName() {
		return context.get(0).getMethodName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Call call : context) {
			builder.append(call.toString());
			builder.append(" ");
		}
		String tmp = builder.toString();
		return tmp.trim();
	}

	@Override
	public int hashCode() {
		return hcode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CallContext other = (CallContext) obj;
		if (hcode == other.hcode)
			return true;
		return false;
	}

	public boolean oldMatches(CallContext other) {
		if (context.size() != other.context.size())
			return false;
		if (other.hcode == hcode)
			return true;
		for (int i = 0; i < context.size(); i++) {
			Call call1 = context.get(i);
			Call call2 = other.context.get(i);
			if (!call1.matches(call2)) {
				return false;
			}
		}

		return false;
	}
	//A empty context matches with everything.
	public boolean matches(CallContext other) {
		if (context.isEmpty()||other.context.isEmpty()|| other.hcode == hcode)
			return true;
		return false;
	}


    public List<Call> getContext() {
        return context;
    }
	// ----------------
	// CALL class

}
