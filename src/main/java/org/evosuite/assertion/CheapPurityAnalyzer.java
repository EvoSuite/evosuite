package org.evosuite.assertion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.objectweb.asm.Type;

public class CheapPurityAnalyzer {

	private boolean underApproximateAnalysis = true;

	private final HashSet<MethodEntry> updateFieldMethodList = new HashSet<MethodEntry>();
	private final HashSet<MethodEntry> notUpdateFieldMethodList = new HashSet<MethodEntry>();

	private final HashSet<MethodEntry> pureMethodCache = new HashSet<MethodEntry>();
	private final HashSet<MethodEntry> notPureMethodCache = new HashSet<MethodEntry>();

	private static final CheapPurityAnalyzer instance = new CheapPurityAnalyzer();

	public static CheapPurityAnalyzer getInstance() {
		return instance;
	}

	public boolean isPure(String className, String methodName, String descriptor) {
		MethodEntry entry = new MethodEntry(className, methodName, descriptor);
		return isPure(entry);
	}

	private boolean isPure(MethodEntry entry) {
		return isPure(entry, new Stack<MethodEntry>());
	}

	private boolean isPure(MethodEntry entry, Stack<MethodEntry> callStack) {
		if (this.pureMethodCache.contains(entry))
			return true;

		if (this.notPureMethodCache.contains(entry))
			return false;

		if (this.updateFieldMethodList.contains(entry)) {
			this.notPureMethodCache.add(entry);
			return false;
		}

		if (staticCalls.containsKey(entry)) {
			if (checkAnyStaticCallImpure(entry, callStack)) {
				this.notPureMethodCache.add(entry);
				return false;
			}
		}

		if (specialCalls.containsKey(entry)) {
			if (checkAnySpecialCallImpure(entry, callStack)) {
				this.notPureMethodCache.add(entry);
				return false;
			}
		}

		if (virtualCalls.containsKey(entry)) {
			return false;
		}

		if (interfaceCalls.containsKey(entry)) {
			return false;
		}

		if (this.notUpdateFieldMethodList.contains(entry)) {
			this.pureMethodCache.add(entry);
			return true;
		}

		return defaultPurityValue();
	}

	private boolean checkAnyStaticCallImpure(MethodEntry entry,
			Stack<MethodEntry> callStack) {
		Set<MethodEntry> calls = staticCalls.get(entry);
		for (MethodEntry callMethodEntry : calls) {
			if (!callStack.contains(callMethodEntry)) {
				Stack<MethodEntry> copyOfStack = new Stack<MethodEntry>();
				copyOfStack.addAll(callStack);
				copyOfStack.add(entry);
				if (!isPure(callMethodEntry, copyOfStack)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkAnySpecialCallImpure(MethodEntry entry,
			Stack<MethodEntry> callStack) {
		Set<MethodEntry> calls = specialCalls.get(entry);
		for (MethodEntry callMethodEntry : calls) {
			if (!callStack.contains(callMethodEntry)) {
				Stack<MethodEntry> copyOfStack = new Stack<MethodEntry>();
				copyOfStack.addAll(callStack);
				copyOfStack.add(entry);
				if (!isPure(callMethodEntry, copyOfStack)) {
					return true;
				}
			}
		}
		return false;
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
	private final HashMap<MethodEntry, Set<MethodEntry>> virtualCalls = new HashMap<MethodEntry, Set<MethodEntry>>();
	private final HashMap<MethodEntry, Set<MethodEntry>> specialCalls = new HashMap<MethodEntry, Set<MethodEntry>>();
	private final HashMap<MethodEntry, Set<MethodEntry>> interfaceCalls = new HashMap<MethodEntry, Set<MethodEntry>>();

	public void addStaticCall(String sourceClassName, String sourceMethodName,
			String sourceDescriptor, String targetClassName,
			String targetMethodName, String targetDescriptor) {

		MethodEntry sourceEntry = new MethodEntry(sourceClassName,
				sourceMethodName, sourceDescriptor);
		MethodEntry targetEntry = new MethodEntry(targetClassName,
				targetMethodName, targetDescriptor);
		if (!staticCalls.containsKey(sourceEntry)) {
			staticCalls.put(sourceEntry, new HashSet<MethodEntry>());
		}
		staticCalls.get(sourceEntry).add(targetEntry);
	}

	public void addVirtualCall(String sourceClassName, String sourceMethodName,
			String sourceDescriptor, String targetClassName,
			String targetMethodName, String targetDescriptor) {

		MethodEntry sourceEntry = new MethodEntry(sourceClassName,
				sourceMethodName, sourceDescriptor);
		MethodEntry targetEntry = new MethodEntry(targetClassName,
				targetMethodName, targetDescriptor);
		if (!virtualCalls.containsKey(sourceEntry)) {
			virtualCalls.put(sourceEntry, new HashSet<MethodEntry>());
		}
		virtualCalls.get(sourceEntry).add(targetEntry);

	}

	public void addInterfaceCall(String sourceClassName,
			String sourceMethodName, String sourceDescriptor,
			String targetClassName, String targetMethodName,
			String targetDescriptor) {

		MethodEntry sourceEntry = new MethodEntry(sourceClassName,
				sourceMethodName, sourceDescriptor);
		MethodEntry targetEntry = new MethodEntry(targetClassName,
				targetMethodName, targetDescriptor);
		if (!interfaceCalls.containsKey(sourceEntry)) {
			interfaceCalls.put(sourceEntry, new HashSet<MethodEntry>());
		}
		interfaceCalls.get(sourceEntry).add(targetEntry);

	}

	public void addSpecialCall(String sourceClassName, String sourceMethodName,
			String sourceDescriptor, String targetClassName,
			String targetMethodName, String targetDescriptor) {
		MethodEntry sourceEntry = new MethodEntry(sourceClassName,
				sourceMethodName, sourceDescriptor);
		MethodEntry targetEntry = new MethodEntry(targetClassName,
				targetMethodName, targetDescriptor);
		if (!specialCalls.containsKey(sourceEntry)) {
			specialCalls.put(sourceEntry, new HashSet<MethodEntry>());
		}
		specialCalls.get(sourceEntry).add(targetEntry);
	}

}
