/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.ibranch;

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
