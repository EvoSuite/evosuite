package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.utils.ClassTransformer;

public class TestExecutionTracer {

	private static final String fullyQualifiedTargetClass = "de.unisb.cs.st.evosuite.testcase.IF_CMP_Test";
	private static final String signature = "(Ljava/lang/Integer;Ljava/lang/Integer;)V";
	private static final double DELTA = 0.0;
	private final ClassTransformer classTransformer = ClassTransformer.getInstance();

	@Test
	public void testGreaterEqual() {
		final Integer branchId = 1;
		final String methodName = "greaterEqual_IF_CMPLT";
		ExecutionTrace execTrace = execute(methodName, 5, 5);
		Assert.assertEquals(methodName + signature, BranchPool.getBranch(branchId).getMethodName());
		Assert.assertEquals(0.0, execTrace.true_distances.get(branchId), DELTA);
		Assert.assertEquals(1.0, execTrace.false_distances.get(branchId), DELTA);
		execTrace = execute(methodName, 5, 6);
		Assert.assertEquals(1.0, execTrace.true_distances.get(branchId), DELTA);
		Assert.assertEquals(0.0, execTrace.false_distances.get(branchId), DELTA);
	}

	@Test
	public void testGreaterThan() {
		final Integer branchId = 2;
		final String methodName = "greaterThan_IF_CMPLE";
		ExecutionTrace execTrace = execute(methodName, 5, 5);
		Assert.assertEquals(methodName + signature, BranchPool.getBranch(branchId).getMethodName());
		Assert.assertEquals(1.0, execTrace.true_distances.get(branchId), DELTA);
		Assert.assertEquals(0.0, execTrace.false_distances.get(branchId), DELTA);
		execTrace = execute(methodName, 6, 5);
		Assert.assertEquals(0.0, execTrace.true_distances.get(branchId), DELTA);
		Assert.assertEquals(1.0, execTrace.false_distances.get(branchId), DELTA);
	}

	@Test
	public void testLesserEqual() {
		final Integer branchId = 3;
		final String methodName = "lesserEqual_IF_CMPGT";
		ExecutionTrace execTrace = execute(methodName, 5, 5);
		Assert.assertEquals(methodName + signature, BranchPool.getBranch(branchId).getMethodName());
		Assert.assertEquals(0.0, execTrace.true_distances.get(branchId), DELTA);
		Assert.assertEquals(1.0, execTrace.false_distances.get(branchId), DELTA);
		execTrace = execute(methodName, 6, 5);
		Assert.assertEquals(1.0, execTrace.true_distances.get(branchId), DELTA);
		Assert.assertEquals(0.0, execTrace.false_distances.get(branchId), DELTA);
		execTrace = execute(methodName, 5, 6);
		Assert.assertEquals(0.0, execTrace.true_distances.get(branchId), DELTA);
		Assert.assertEquals(2.0, execTrace.false_distances.get(branchId), DELTA);
	}

	@Test
	public void testLesserThan() {
		final Integer branchId = 4;
		final String methodName = "lesserThan_IF_CMPGE";
		ExecutionTrace execTrace = execute(methodName, 5, 5);
		Assert.assertEquals(methodName + signature, BranchPool.getBranch(branchId).getMethodName());
		Assert.assertEquals(0.0, execTrace.true_distances.get(branchId), 1.0);
		Assert.assertEquals(0.0, execTrace.false_distances.get(branchId), 0.0);
		execTrace = execute(methodName, 5, 6);
		Assert.assertEquals(0.0, execTrace.true_distances.get(branchId), 0.0);
		Assert.assertEquals(0.0, execTrace.false_distances.get(branchId), 1.0);
	}

	private ExecutionTrace execute(String methodName, Integer val1, Integer val2) {
		try {
			ExecutionTracer.enable();
			Class<?> targetClass = classTransformer.instrumentClass(fullyQualifiedTargetClass);
			Constructor<?> constructor = targetClass.getConstructor();
			Object target = constructor.newInstance();
			Method method = targetClass.getMethod(methodName, Integer.class, Integer.class);
			method.invoke(target, val1, val2);
			ExecutionTrace execTrace = ExecutionTracer.getExecutionTracer().getTrace();
			ExecutionTracer.getExecutionTracer().clear();
			return execTrace;
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}
}
