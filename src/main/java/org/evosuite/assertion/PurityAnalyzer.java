package org.evosuite.assertion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.evosuite.setup.CallTree;
import org.evosuite.setup.CallTreeEntry;
import org.evosuite.setup.DependencyAnalysis;

import com.sun.xml.internal.ws.org.objectweb.asm.Type;

public class PurityAnalyzer {

	private boolean underApproximateAnalysis = true;

	private final HashSet<MethodEntry> updateFieldMethodList = new HashSet<MethodEntry>();
	private final HashSet<MethodEntry> notUpdateFieldMethodList = new HashSet<MethodEntry>();

	private final HashSet<MethodEntry> pureMethodCache = new HashSet<MethodEntry>();
	private final HashSet<MethodEntry> notPureMethodCache = new HashSet<MethodEntry>();

	private static final PurityAnalyzer instance = new PurityAnalyzer();

	public static PurityAnalyzer getInstance() {
		return instance;
	}

	public boolean isPure(String className, String methodName, String descriptor) {
		MethodEntry entry = new MethodEntry(className, methodName, descriptor);
		return isPure(entry);
	}

	private boolean isPure(MethodEntry entry) {
		if (this.pureMethodCache.contains(entry))
			return true;

		if (this.notPureMethodCache.contains(entry))
			return false;

		if (this.updateFieldMethodList.contains(entry)) {
			this.notPureMethodCache.add(entry);
			return false;
		}

		if (staticCalls.containsKey(entry)) {
			Set<MethodEntry> calls = staticCalls.get(entry);
			for (MethodEntry callMethodEntry : calls) {
				if (!isPure(callMethodEntry)) {
					this.notPureMethodCache.add(entry);
					return false;
				}
			}
		}

		if (this.notUpdateFieldMethodList.contains(entry)) {
			this.pureMethodCache.add(entry);
			return true;
		}

		return defaultPurityValue();
	}

	/**
	 * We return this value when we can't conclude if a given method is pure or not.
	 * @return
	 */
	private boolean defaultPurityValue() {
		if (this.underApproximateAnalysis) {
			return false;
		} else {
			return true;
		}
	}

	public boolean isPure(java.lang.reflect.Method method) {
		String className = method.getDeclaringClass().getCanonicalName();
		String methodName = method.getName();
		String descriptor = Type.getMethodDescriptor(method);

		MethodEntry entry = new MethodEntry(className, methodName, descriptor);
		return isPure(entry);
	}

	private static class MethodEntry {
		private final String className;
		private final String methodName;
		private final String descriptor;

		public MethodEntry(String className, String methodName,
				String descriptor) {
			this.className = className;
			this.methodName = methodName;
			this.descriptor = descriptor;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + className.hashCode();
			result = prime * result + descriptor.hashCode();
			result = prime * result + methodName.hashCode();
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
			MethodEntry other = (MethodEntry) obj;
			return (className.equals(other.className) && methodName
					.equals(other.methodName))
					&& descriptor.equals(other.descriptor);
		}

		@Override
		public String toString() {
			return "MethodEntry [className=" + className + ", methodName="
					+ methodName + ", descriptor=" + String.valueOf(descriptor)
					+ "]";
		}
	}

	public void addUpdatesFieldMethod(String className, String methodName,
			String descriptor) {
		String classNameWithDots = className.replace("/", ".");
		MethodEntry entry = new MethodEntry(classNameWithDots, methodName,
				descriptor);
		updateFieldMethodList.add(entry);
	}

	public void addNotUpdatesFieldMethod(String className, String methodName,
			String descriptor) {
		String classNameWithDots = className.replace("/", ".");
		MethodEntry entry = new MethodEntry(classNameWithDots, methodName,
				descriptor);
		notUpdateFieldMethodList.add(entry);
	}

	private final HashMap<MethodEntry, Set<MethodEntry>> staticCalls = new HashMap<MethodEntry, Set<MethodEntry>>();

	public void addStaticCall(String sourceClassName, String sourceMethodName,
			String sourceDescriptor, String targetClassName,
			String targetMethodName, String targetDescriptor) {
		MethodEntry sourceEntry = new MethodEntry(sourceClassName.replace("/","."),
				sourceMethodName, sourceDescriptor);
		MethodEntry targetEntry = new MethodEntry(targetClassName.replace("/","."),
				targetMethodName, targetDescriptor);

		if (!staticCalls.containsKey(sourceEntry)) {
			staticCalls.put(sourceEntry, new HashSet<MethodEntry>());
		}
		staticCalls.get(sourceEntry).add(targetEntry);
	}

	public void addVirtualCall(String sourceClassName,
			String sourceMethodAndDescriptor, String targetClassName,
			String targetMehtodAndDescriptor) {
		// TODO Auto-generated method stub

	}

	public void addInterfaceCall(String sourceClassName,
			String sourceMethodAndDescriptor, String targetClassName,
			String targetMehtodAndDescriptor) {
		// TODO Auto-generated method stub
	}

	public void addSpecialCall(String sourceClassName,
			String sourceMethodAndDescriptor, String targetClassName,
			String targetMehtodAndDescriptor) {
		// TODO Auto-generated method stub

	}

}
