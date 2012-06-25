/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.coverage.ibranch;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gordon Fraser
 * 
 */
public class CallContext {

	private static class Call {

		private final String className;
		private final String methodName;

		public Call(String classname, String methodName) {
			this.className = classname;
			this.methodName = methodName;
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
	}

	private final List<Call> context = new ArrayList<Call>();

	public CallContext(StackTraceElement[] stackTrace) {
		for (StackTraceElement element : stackTrace) {
			if (!element.getClassName().startsWith("de.unisb.cs.st.evosuite"))
				context.add(new Call(element.getClassName(), element.getMethodName()));
		}
	}

	/**
	 * Determine if the concrete stack trace matches this call context
	 * 
	 * @param stackTrace
	 * @return
	 */
	public boolean matches(StackTraceElement[] stackTrace) {
		// TODO: Implement
		return false;
	}

	public String getRootClassName() {
		return context.get(0).getClassName();
	}

	public String getRootMethodName() {
		return context.get(0).getMethodName();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "";

		return result;
	}
}
