package org.evosuite.assertion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.evosuite.instrumentation.BytecodeInstrumentation;
import org.evosuite.runtime.mock.MockList;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.InheritanceTree;
import org.evosuite.setup.TestCluster;
import org.evosuite.utils.JdkPureMethodsList;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs a very cheap purity analysis by under-approximating the set of
 * pure methods. It does not use any kind of escape-analysis. The purity analysis
 * is solely based on already collected bytecode instructions during class loading.
 * A method is <i>cheap-pure</i> if and only if:
 * <ul>
 * 	<li>The method is listed in the JdkPureMethodList</li>
 * 	<li>There is no declared overriding method that is not <i>cheap-pure</i></li>
 *  <li>All invoked classes are loaded in the inheritance tree</li>
 * 	<li>Has no PUTSTATIC nor PUTFIELD instructions</li>
 * 	<li>All static invocations (INVOKESTATIC) are made to <i>cheap-pure</i> static methods</li>
 *  <li>All special invocations (INVOKESPECIAL) are also made to <i>cheap-pure</i> methods</li>
 *  <li>All interface invocations (INVOKEINTERFACE) are also made to <i>cheap-pure</i> methods</li>
 * </ul>
 * 
 * @author galeotti
 *
 */
public class CheapPurityAnalyzer {

	private static Logger logger = LoggerFactory
			.getLogger(CheapPurityAnalyzer.class);

	private final HashSet<MethodEntry> updateFieldMethodList = new HashSet<MethodEntry>();
	private final HashSet<MethodEntry> notUpdateFieldMethodList = new HashSet<MethodEntry>();

	private final HashMap<MethodEntry, Boolean> purityCache = new HashMap<MethodEntry, Boolean>();

	private static final CheapPurityAnalyzer instance = new CheapPurityAnalyzer();

	public static CheapPurityAnalyzer getInstance() {
		return instance;
	}

	/**
	 * Returns if the method is cheap-pure.
	 * 
	 * @param className The declaring class name
	 * @param methodName The method name
	 * @param descriptor The method descriptor
	 * @return true if the method is cheap-pure, false otherwise
	 */
	public boolean isPure(String className, String methodName, String descriptor) {
		MethodEntry entry = new MethodEntry(className, methodName, descriptor);
		return isPure(entry);
	}

	private boolean isPure(MethodEntry entry) {
		Stack<MethodEntry> emptyStack = new Stack<MethodEntry>();
		return isPure(entry, emptyStack);
	}

	private boolean isCached(MethodEntry entry) {
		return this.purityCache.containsKey(entry);
	}

	private boolean getCacheValue(MethodEntry entry) {
		return this.purityCache.get(entry);
	}

	private void addCacheValue(MethodEntry entry, boolean new_value) {
		if (isCached(entry)) {
			boolean old_value = this.purityCache.get(entry);
			if (old_value != new_value) {
				String fullyQuantifiedMethodName = entry.className + "."
						+ entry.methodName + entry.descriptor;

				logger.warn("The method "
						+ fullyQuantifiedMethodName
						+ " had a different value in the purity cache (old_value="
						+ old_value + ",new_value=" + new_value + ")");
			}
		}
		this.purityCache.put(entry, new_value);
	}

	private boolean isPure0(MethodEntry entry, Stack<MethodEntry> callStack) {
		if (isRandomCall(entry)) {
			return false;
		}
		
		if (isJdkPureMethod(entry)) {
			return true;
		}

		if (!BytecodeInstrumentation.checkIfCanInstrument(entry.className)) {
			return false;
		}

		if (this.updateFieldMethodList.contains(entry)) {
			return false;
		}

		if (staticCalls.containsKey(entry)) {
			Set<MethodEntry> calls = staticCalls.get(entry);
			if (checkAnyCallImpure(calls, entry, callStack)) {
				return false;
			}
		}

		if (specialCalls.containsKey(entry)) {
			Set<MethodEntry> calls = specialCalls.get(entry);
			if (checkAnyCallImpure(calls, entry, callStack)) {
				return false;
			}
		}

		if (virtualCalls.containsKey(entry)) {
			Set<MethodEntry> calls = virtualCalls.get(entry);
			if (checkAnyCallImpure(calls, entry, callStack)) {
				return false;
			}
		}

		if (interfaceCalls.containsKey(entry)) {
			Set<MethodEntry> calls = interfaceCalls.get(entry);
			if (checkAnyCallImpure(calls, entry, callStack)) {
				return false;
			}
		}

		// check overriding methods
		if (checkAnyOverridingMethodImpure(entry, callStack)) {
			return false;
		}

		if (this.notUpdateFieldMethodList.contains(entry)) {
			return true;
		}
		if (this.interfaceMethodEntries.contains(entry)) {
			return true;
		}
		return checkSuperclass(entry);
		// return DEFAULT_PURITY_VALUE;
	}
	
	private boolean checkSuperclass(MethodEntry entry) {
		InheritanceTree inheritanceTree = TestCluster.getInheritanceTree();
		for(String superClassName : inheritanceTree.getSuperclasses(entry.className)) {
			if(superClassName.equals(entry.className))
				continue;
			
			MethodEntry superEntry = new MethodEntry(superClassName, entry.methodName, entry.descriptor);
			if(isPure(superEntry))
				return true;
		}
		return DEFAULT_PURITY_VALUE;
	}

	private boolean isRandomCall(MethodEntry entry) {
		if (entry.className.equals("java.util.Random"))
			return true;
		else if (entry.className.equals("java.security.SecureRandom"))
			return true;
		else if (entry.className.equals("org.evosuite.Random")) 
			return true;
		else if (entry.className.equals("java.lang.Math") && entry.methodName.equals("random"))
			return true;
		 else 
			return false;
	}

	private boolean isPure(MethodEntry entry, Stack<MethodEntry> callStack) {
		if (isCached(entry)) {
			return getCacheValue(entry);
		} else {
			boolean isPure = isPure0(entry, callStack);
			addCacheValue(entry, isPure);
			return isPure;
		}
	}

	private boolean checkAnyOverridingMethodImpure(MethodEntry entry,
			Stack<MethodEntry> callStack) {
		InheritanceTree inheritanceTree = DependencyAnalysis
				.getInheritanceTree();

		String className = ""+entry.className;
		while(className.contains("[L")) {
			className = className.substring(2, className.length()-1);
		}
		
		if (!inheritanceTree.hasClass(className)) {
			logger.info(className
					+ " was not found in the inheritance tree. Using DEFAULT value for cheap-purity analysis");
			return DEFAULT_PURITY_VALUE;
		}		

		Set<String> subclasses = inheritanceTree.getSubclasses(className);
		for (String subclassName : subclasses) {
			if (!entry.className.equals(subclassName)) {

				MethodEntry subclassEntry = new MethodEntry(subclassName,
						entry.methodName, entry.descriptor);

				if (!callStack.contains(subclassEntry)
						&& methodEntries.contains(subclassEntry)) {

					Stack<MethodEntry> newStack = new Stack<MethodEntry>();
					newStack.addAll(callStack);
					newStack.add(subclassEntry);
					if (!isPure(subclassEntry, newStack)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	
	
	private boolean isJdkPureMethod(MethodEntry entry) {
		String paraz = entry.descriptor;
		Type[] parameters = org.objectweb.asm.Type.getArgumentTypes(paraz);
		String newParams = "";
		if (parameters.length != 0) {
			for (Type i : parameters) {
				newParams = newParams + "," + i.getClassName();
			}
			newParams = newParams.substring(1, newParams.length());
		}
		String qualifiedName = entry.className + "." + entry.methodName + "("
				+ newParams + ")";

		return (JdkPureMethodsList.instance.checkPurity(qualifiedName));
	}

	private boolean checkAnyCallImpure(Set<MethodEntry> calls,
			MethodEntry entry, Stack<MethodEntry> callStack) {
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
	 */
	private static final boolean DEFAULT_PURITY_VALUE = false;

	/**
	 * Returns if a Method is <code>cheap-pure</code>
	 * @param method
	 * @return true if the method is cheap-pure, otherwise false.
	 */
	public boolean isPure(java.lang.reflect.Method method) {
		// Using getName rather than getCanonicalName because that's what
		// the inheritancetree also uses
		String className = method.getDeclaringClass().getName();
		if(MockList.isAMockClass(className)) {
			className = method.getDeclaringClass().getSuperclass().getName();
		} 
		
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

	private final HashSet<MethodEntry> methodEntries = new HashSet<MethodEntry>();

	public void addMethod(String className, String methodName,
			String methodDescriptor) {
		MethodEntry entry = new MethodEntry(className, methodName,
				methodDescriptor);
		methodEntries.add(entry);
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

		addCall(staticCalls, sourceClassName, sourceMethodName,
				sourceDescriptor, targetClassName, targetMethodName,
				targetDescriptor);

	}

	public void addVirtualCall(String sourceClassName, String sourceMethodName,
			String sourceDescriptor, String targetClassName,
			String targetMethodName, String targetDescriptor) {

		addCall(virtualCalls, sourceClassName, sourceMethodName,
				sourceDescriptor, targetClassName, targetMethodName,
				targetDescriptor);

	}

	public void addInterfaceCall(String sourceClassName,
			String sourceMethodName, String sourceDescriptor,
			String targetClassName, String targetMethodName,
			String targetDescriptor) {

		addCall(interfaceCalls, sourceClassName, sourceMethodName,
				sourceDescriptor, targetClassName, targetMethodName,
				targetDescriptor);

	}

	private static void addCall(HashMap<MethodEntry, Set<MethodEntry>> calls,
			String sourceClassName, String sourceMethodName,
			String sourceDescriptor, String targetClassName,
			String targetMethodName, String targetDescriptor) {

		MethodEntry sourceEntry = new MethodEntry(sourceClassName,
				sourceMethodName, sourceDescriptor);
		MethodEntry targetEntry = new MethodEntry(targetClassName,
				targetMethodName, targetDescriptor);
		if (!calls.containsKey(sourceEntry)) {
			calls.put(sourceEntry, new HashSet<MethodEntry>());
		}
		calls.get(sourceEntry).add(targetEntry);
	}

	public void addSpecialCall(String sourceClassName, String sourceMethodName,
			String sourceDescriptor, String targetClassName,
			String targetMethodName, String targetDescriptor) {

		addCall(specialCalls, sourceClassName, sourceMethodName,
				sourceDescriptor, targetClassName, targetMethodName,
				targetDescriptor);
	}

	private final HashSet<MethodEntry> interfaceMethodEntries = new HashSet<MethodEntry>();

	public void addInterfaceMethod(String className, String methodName,
			String methodDescriptor) {
		MethodEntry entry = new MethodEntry(className, methodName,
				methodDescriptor);
		interfaceMethodEntries.add(entry);
	}

}
