/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
     * <p>
     * Fully qualified method name example: com.examples.with.different.packagename.TargetMethodPrefix.<init>()V
     * com.examples.with.different.packagename.Foo.foo_bar1(Ljava/lang/String;)Z
     * <p>
     * prefix "foo_bar1"
     * com.examples.with.different.packagename.Foo.foo_bar1(Ljava/lang/String;)Z
     *
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
