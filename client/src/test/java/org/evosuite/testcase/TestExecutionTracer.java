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
package org.evosuite.testcase;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.utils.ClassTransformer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


public class TestExecutionTracer {

    private static final String fullyQualifiedTargetClass = "com.examples.with.different.packagename.IF_CMP_Test";
    private static final String signature = "(Ljava/lang/Integer;Ljava/lang/Integer;)V";
    private static final double DELTA = 0.0;
    private final ClassTransformer classTransformer = ClassTransformer.getInstance();

    @Ignore
    @Test
    public void testGreaterEqual() {
        final Integer branchId = 1;
        final String methodName = "greaterEqual_IF_CMPLT";
        ExecutionTrace execTrace = execute(methodName, 5, 5);
        Assert.assertEquals(methodName + signature,
                BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranch(branchId).getMethodName());
        Assert.assertEquals(0.0, execTrace.getTrueDistance(branchId), DELTA);
        Assert.assertEquals(1.0, execTrace.getFalseDistance(branchId), DELTA);
        execTrace = execute(methodName, 5, 6);
        Assert.assertEquals(1.0, execTrace.getTrueDistance(branchId), DELTA);
        Assert.assertEquals(0.0, execTrace.getFalseDistance(branchId), DELTA);
    }

    @Ignore
    @Test
    public void testGreaterThan() {
        final Integer branchId = 2;
        final String methodName = "greaterThan_IF_CMPLE";
        ExecutionTrace execTrace = execute(methodName, 5, 5);
        Assert.assertEquals(methodName + signature,
                BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranch(branchId).getMethodName());
        Assert.assertEquals(1.0, execTrace.getTrueDistance(branchId), DELTA);
        Assert.assertEquals(0.0, execTrace.getFalseDistance(branchId), DELTA);
        execTrace = execute(methodName, 6, 5);
        Assert.assertEquals(0.0, execTrace.getTrueDistance(branchId), DELTA);
        Assert.assertEquals(1.0, execTrace.getFalseDistance(branchId), DELTA);
    }

    @Ignore
    @Test
    public void testLesserEqual() {
        final Integer branchId = 3;
        final String methodName = "lesserEqual_IF_CMPGT";
        ExecutionTrace execTrace = execute(methodName, 5, 5);
        Assert.assertEquals(methodName + signature,
                BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranch(branchId).getMethodName());
        Assert.assertEquals(0.0, execTrace.getTrueDistance(branchId), DELTA);
        Assert.assertEquals(1.0, execTrace.getFalseDistance(branchId), DELTA);
        execTrace = execute(methodName, 6, 5);
        Assert.assertEquals(1.0, execTrace.getTrueDistance(branchId), DELTA);
        Assert.assertEquals(0.0, execTrace.getFalseDistance(branchId), DELTA);
        execTrace = execute(methodName, 5, 6);
        Assert.assertEquals(0.0, execTrace.getTrueDistance(branchId), DELTA);
        Assert.assertEquals(2.0, execTrace.getFalseDistance(branchId), DELTA);
    }

    @Ignore
    @Test
    public void testLesserThan() {
        final Integer branchId = 4;
        final String methodName = "lesserThan_IF_CMPGE";
        ExecutionTrace execTrace = execute(methodName, 5, 5);
        Assert.assertEquals(methodName + signature,
                BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranch(branchId).getMethodName());
        Assert.assertEquals(0.0, execTrace.getTrueDistance(branchId), 1.0);
        Assert.assertEquals(0.0, execTrace.getFalseDistance(branchId), 0.0);
        execTrace = execute(methodName, 5, 6);
        Assert.assertEquals(0.0, execTrace.getTrueDistance(branchId), 0.0);
        Assert.assertEquals(0.0, execTrace.getFalseDistance(branchId), 1.0);
    }

    private ExecutionTrace execute(String methodName, Integer val1, Integer val2) {
        try {
            ExecutionTracer.enable();
            Class<?> targetClass = classTransformer.instrumentClass(fullyQualifiedTargetClass);
            Constructor<?> constructor = targetClass.getConstructor();
            Object target = constructor.newInstance();
            Method method = targetClass.getMethod(methodName, Integer.class,
                    Integer.class);
            method.invoke(target, val1, val2);
            ExecutionTrace execTrace = ExecutionTracer.getExecutionTracer().getTrace();
            ExecutionTracer.getExecutionTracer().clear();
            return execTrace;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }
}
