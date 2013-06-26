package org.evosuite.coverage;

import org.evosuite.Properties;

public class MethodNameMatcher {

	/**
	 * Returns if a method name matches any of the target method criteria:
	 * <ol>
	 * <li>It ends with the property Properties.TARGET_METHOD (if setted)</li>
	 * <li>It is contained in the semi-colon separated list Propertires.TARGET_METHOD_LIST</li>
	 * <li>Its prefix matches Properties.TARGET_METHOD_PREFIX (if setted)</li>
	 * </ol>
	 * The format of the method name is identifier(ASM signature). For example: foo_bar1(Ljava/lang/String;)Z
	 * 
	 * Fully qualified method name example: com.examples.with.different.packagename.TargetMethodPrefix.<init>()V
	 * com.examples.with.different.packagename.Foo.foo_bar1(Ljava/lang/String;)Z
	 *  
	 * prefix "foo_bar1"
	 * com.examples.with.different.packagename.Foo.foo_bar1(Ljava/lang/String;)Z 
	 * @param fullyQualifiedMethodName 
	 * @return
	 */
	public boolean fullyQualifiedMethodMatches(String fullyQualifiedMethodName) {
		String methodName;
		int lastIndexOf = fullyQualifiedMethodName.lastIndexOf('.');
		if (lastIndexOf == -1)
			methodName = fullyQualifiedMethodName;
		else
			methodName = fullyQualifiedMethodName.substring(lastIndexOf + 1);

		return methodMatches(methodName);
	}

	/**
	 * Returns if a method name matches any of the target method criteria:
	 * <ol>
	 * <li>It is equal to Properties.TARGET_METHOD (if setted)</li>
	 * <li>It is contained in the semi-colon separated list Propertires.TARGET_METHOD_LIST (if setted)</li>
	 * <li>Its prefix matches Properties.TARGET_METHOD_PREFIX (if setted)</li>
	 * </ol>
	 * The format of the method name is identifier(ASM signature). For example: foo_bar1(Ljava/lang/String;)Z
	 * For fully qualified method names use <code>fullyQualifiedMethodNameMatches</code> instead.
	 * 
	 * @param methodName
	 * @return
	 */
	public boolean methodMatches(String methodName) {
		String targetMethod = Properties.TARGET_METHOD;
		if (!targetMethod.isEmpty() && methodName.equals(targetMethod))
			return true;

		final String targetMethodList = Properties.TARGET_METHOD_LIST;
		if (!targetMethodList.isEmpty()) {
			String[] targetMethods = targetMethodList.split(":");
			for (String targetMethodInList : targetMethods) {
				if (methodName.equals(targetMethodInList))
					return true;
			}
		}

		final String targetMethodPrefix = Properties.TARGET_METHOD_PREFIX;
		if (!targetMethodPrefix.isEmpty()
				&& methodName.startsWith(targetMethodPrefix))
			return true;

		final boolean noMethodTargetSpecified = targetMethod.isEmpty() && targetMethodList.isEmpty()
				&& targetMethodPrefix.isEmpty();
		return noMethodTargetSpecified;

	}
}
