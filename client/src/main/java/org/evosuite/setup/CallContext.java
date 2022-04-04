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

package org.evosuite.setup;

import org.evosuite.PackageInfo;
import org.evosuite.Properties;
import org.evosuite.testcase.execution.MethodCall;

import java.io.Serializable;
import java.util.*;

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
 */
public class CallContext implements Serializable {


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
     * @param stackTrace an array of {@link java.lang.StackTraceElement} objects.
     */
    public CallContext(StackTraceElement[] stackTrace) {
        addJUnitExcludes();

        int startPos = stackTrace.length - 1;
        int endPos = 0;
        List<Call> context = new ArrayList<>();

        // Stack traces may be empty, e.g. if an exception is thrown in a constructor call in a test
        while (startPos >= 0 && shouldSkipEntry(stackTrace[startPos].getClassName())) {
            startPos--;
        }
        while (endPos < stackTrace.length && shouldSkipEntry(stackTrace[endPos].getClassName())) {
            endPos++;
        }

        for (int i = startPos; i >= endPos; i--) {
            StackTraceElement element = stackTrace[i];
            Call newCall = new Call(element.getClassName(), element.getMethodName());
            boolean skip = false;
            if (context.size() >= 2) { // Need at least a sequence of three for this check to make sense
                Call previousCall1 = context.get(context.size() - 1);
                Call previousCall2 = context.get(context.size() - 2);
                if (previousCall1.equals(newCall) && previousCall2.equals(newCall)) {
                    skip = true;
                }
            }
            if (!skip)
                context.add(newCall);
        }
        this.context = context;
        hcode = this.context.hashCode();
    }

    /**
     * Constructor for CallContext.
     *
     * @param stackTrace
     */
    public CallContext(LinkedList<MethodCall> stackTrace) {
        addJUnitExcludes();

        int startPos = stackTrace.size() - 1;
        int endPos = 0;
        List<Call> context = new ArrayList<>();

        while (startPos >= 0 && shouldSkipEntry(stackTrace.get(startPos).className)) {
            startPos--;
        }

        while (endPos < stackTrace.size() && shouldSkipEntry(stackTrace.get(endPos).className)) {
            endPos++;
        }

        for (int i = startPos; i >= endPos; i--) {
            MethodCall element = stackTrace.get(i);

            Call newCall = new Call(element.className, element.methodName);

            boolean skip = false;
            if (context.size() >= 2) { // Need at least a sequence of three for this check to make sense
                Call previousCall1 = context.get(context.size() - 1);
                Call previousCall2 = context.get(context.size() - 2);
                if (previousCall1.equals(newCall) && previousCall2.equals(newCall)) {
                    skip = true;
                }
            }
            if (!skip)
                context.add(newCall);
        }
        this.context = context;
        hcode = this.context.hashCode();
    }

    /**
     * Constructor for public methods
     *
     * @param className
     * @param methodName
     */
    public CallContext(String className, String methodName) {
        addJUnitExcludes();

        List<Call> context = new ArrayList<>();
        context.add(new Call(className, methodName));
        this.context = context;
        hcode = this.context.hashCode();
    }

    public CallContext() {
        addJUnitExcludes();

        this.context = new ArrayList<>();
        hcode = this.context.hashCode();
    }

    public CallContext(Collection<Call> contextt) {
        addJUnitExcludes();

        this.context = new ArrayList<>(contextt);
        hcode = this.context.hashCode();
    }

    public int size() {
        return context.size();

    }

    private String[] excludedPackages = new String[]{"java", "sun", PackageInfo.getEvoSuitePackage()};

    /**
     * If we are using -measureCoverage then we need to also exclude the junit tests
     */
    private void addJUnitExcludes() {
        if (Properties.JUNIT.isEmpty())
            return;
        List<String> values = new ArrayList<>(Arrays.asList(excludedPackages));
        values.add("org.junit");
        for (String junitClass : Properties.JUNIT.split(":")) {
            values.add(junitClass);
        }
        excludedPackages = new String[values.size()];
        excludedPackages = values.toArray(excludedPackages);
    }

    private boolean shouldSkipEntry(String entry) {
        if (entry.isEmpty())
            return true;
        for (String excludedPackage : excludedPackages) {
            if (entry.startsWith(excludedPackage))
                return true;
        }
        return false;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (context == null)
            return "";

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
        return hcode == other.hcode;
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
        return context.isEmpty() || other.context.isEmpty() || other.hcode == hcode;
    }


    public List<Call> getContext() {
        return context;
    }
    // ----------------
    // CALL class

}
