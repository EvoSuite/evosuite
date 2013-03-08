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
package org.evosuite.setup;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * CallContext class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class CallContext {

	private static class Call {

		private final String className;
		private final String methodName;

		public Call(String classname, String methodName) {
			this.className = classname;
			this.methodName = methodName;
		}

		public Call(Call call) {
			this.className = call.className;
			this.methodName = call.methodName;
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
			if (className == null) {
				if (other.className != null)
					return false;
			} else if (!className.equals(other.className))
				return false;
			if (methodName == null) {
				if (other.methodName != null)
					return false;
			} else if (!methodName.equals(other.methodName))
				return false;
			return true;
		}

		public boolean matches(Call other) {
			if (!other.getClassName().equals(className))
				return false;

			// The stacktraceelement does not contain the signature
			// so we just look if the name matches
			// TODO: Could consider line number?
			if (methodName.startsWith(other.getMethodName()))
				return true;

			return false;
		}

		@Override
		public String toString() {
			return className + ":" + methodName;
		}

	}

	private final List<Call> context = new ArrayList<Call>();

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
		while (stackTrace[startPos].getClassName().startsWith("java")
		        || stackTrace[startPos].getClassName().startsWith("sun")
		        || stackTrace[startPos].getClassName().startsWith("org.evosuite")) {
			startPos--;
		}
		int endPos = 0;
		while (stackTrace[endPos].getClassName().startsWith("java")
		        || stackTrace[endPos].getClassName().startsWith("sun")
		        || stackTrace[endPos].getClassName().startsWith("org.evosuite")) {
			endPos++;
		}

		//LoggingUtils.getEvoLogger().info("Filtered stacktrace:");
		for (int i = startPos; i >= endPos; i--) {
			StackTraceElement element = stackTrace[i];
			//LoggingUtils.getEvoLogger().info(element.toString());
			context.add(new Call(element.getClassName(), element.getMethodName()));
		}

		//		for (StackTraceElement element : stackTrace) {
		//			if (!element.getClassName().startsWith("org.evosuite"))
		//				context.add(new Call(element.getClassName(), element.getMethodName()));
		//		}
	}

	/**
	 * Constructor for public methods
	 * 
	 * @param className
	 * @param methodName
	 */
	public CallContext(String className, String methodName) {
		context.add(new Call(className, methodName));
	}

	public CallContext() {

	}

	public void addCallingMethod(String className, String methodName) {
		context.add(0, new Call(className, methodName));
	}

	public void addCalledMethod(String className, String methodName) {
		context.add(new Call(className, methodName));
	}

	public CallContext getSuperContext(String className, String methodName) {
		CallContext copy = new CallContext();
		copy.context.add(new Call(className, methodName));
		for (Call call : context) {
			copy.context.add(call);
		}
		return copy;
	}

	/**
	 * Determine if the concrete stack trace matches this call context
	 * 
	 * @param stackTrace
	 *            an array of {@link java.lang.StackTraceElement} objects.
	 * @return a boolean.
	 */
	public boolean matches(StackTraceElement[] stackTrace) {
		// TODO: Implement
		return true;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Call call : context) {
			builder.append(call.toString());
			builder.append("\n");
		}
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
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
		CallContext other = (CallContext) obj;
		if (context == null) {
			if (other.context != null)
				return false;
		} else if (!context.equals(other.context))
			return false;
		return true;
	}

	public boolean matches(CallContext other) {
		if (other.context.size() != context.size())
			return false;

		for (int i = 0; i < context.size(); i++) {
			Call call1 = context.get(i);
			Call call2 = other.context.get(i);
			if (!call1.matches(call2)) {
				return false;
			}
		}

		return true;
	}
}
