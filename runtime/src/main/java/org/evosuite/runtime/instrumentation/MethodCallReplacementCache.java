package org.evosuite.runtime.instrumentation;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.mock.EvoSuiteMock;
import org.evosuite.runtime.mock.MockList;
import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.lang.MockThrowable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author gordon
 *
 */
public class MethodCallReplacementCache {

	private static MethodCallReplacementCache instance = null;
	
	private MethodCallReplacementCache() {
		initReplacements();
	}
	
	public static MethodCallReplacementCache getInstance() {
		if(instance == null)
			instance = new MethodCallReplacementCache();
		
		return instance;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(MethodCallReplacementCache.class);

	/**
	 * method replacements, which are called with Opcodes.INVOKESTATIC
	 */
	private final Map<String, Map<String, MethodCallReplacement>> replacementCalls = new HashMap<String, Map<String, MethodCallReplacement>>();

	/**
	 * method replacements, which are called with Opcodes.INVOKEVIRTUAL
	 */
	//private final Set<MethodCallReplacement> virtualReplacementCalls = new HashSet<MethodCallReplacement>();

	/**
	 * method replacements, which are called with Opcodes.INVOKESPECIAL
	 */
	private final Map<String, Map<String, MethodCallReplacement>> specialReplacementCalls = new HashMap<String, Map<String, MethodCallReplacement>>();
	
	private void addReplacementCall(MethodCallReplacement replacement) {
		if(!replacementCalls.containsKey(replacement.getClassName())) {
			replacementCalls.put(replacement.getClassName(), new HashMap<String, MethodCallReplacement>());
		}
		replacementCalls.get(replacement.getClassName()).put(replacement.getMethodNameWithDesc(), replacement);
	}

	private void addSpecialReplacementCall(MethodCallReplacement replacement) {
		if(!specialReplacementCalls.containsKey(replacement.getClassName())) {
			specialReplacementCalls.put(replacement.getClassName(), new HashMap<String, MethodCallReplacement>());
		}
		specialReplacementCalls.get(replacement.getClassName()).put(replacement.getMethodNameWithDesc(), replacement);
	}

	//private void addVirtualReplacementCall(MethodCallReplacement replacement) {
	//	virtualReplacementCalls.add(replacement);
	//}

	public boolean hasReplacementCall(String className, String methodNameWithDesc) {
		if(!replacementCalls.containsKey(className))
			return false;
		
		return replacementCalls.get(className).containsKey(methodNameWithDesc);
	}

	public MethodCallReplacement getReplacementCall(String className, String methodNameWithDesc) {
		return replacementCalls.get(className).get(methodNameWithDesc);
	}

	public boolean hasSpecialReplacementCall(String className, String methodNameWithDesc) {
		if(!specialReplacementCalls.containsKey(className))
			return false;
		
		return specialReplacementCalls.get(className).containsKey(methodNameWithDesc);
	}

	public MethodCallReplacement getSpecialReplacementCall(String className, String methodNameWithDesc) {
		return specialReplacementCalls.get(className).get(methodNameWithDesc);
	}

	// public Iterator<MethodCallReplacement> getVirtualReplacementCalls() {
	//	return virtualReplacementCalls.iterator();
	// }
	
	
	private void initReplacements() {
		if (RuntimeSettings.mockJVMNonDeterminism) {

			//java.lang.*
			addJavaLangCalls();

			//java.util.Calendar
			addCalendarCalls();

			// java.security.SecureRandom
			addSecureRandomCalls();

			addGUICalls();

			addExtraceExceptionReplacements();
			
			//java.util.UUID.randomUUID()
			addReplacementCall(new MethodCallReplacement("java/util/UUID", "randomUUID",
					"()Ljava/util/UUID;", Opcodes.INVOKESTATIC, "org/evosuite/runtime/Random", "randomUUID", "()Ljava/util/UUID;", false, false));


		}

		handleMockList();

	}
	
	/**
	 * Ideally, all mocking should be handled by either OverrideMock or StaticReplacementMock.
	 * However, even in such cases, we would not be able to get mocks from classes that are
	 * not instrumented (eg, other Java API classes we do not mock).
	 * In theory, we should mock all of them.
	 * The problem raises for "Exceptions", as they are used everywhere, and would require
	 * to mock the full Java API (which is not going to happen...).
	 * Solution is, beside using OverrideMock for them, to also a further static replacement (implemented
	 * in this method).
	 * 
	 * <p>
	 * Note: why not just using static replacement instead of OverrideMock? Because static replacement
	 * will not work if an exception instance is used in a non-instrumented class, whereas OverrideMock would.
	 * Still, it could be tedious to prepare OverrideMock for every single type of exception, so the static
	 * replacement here could be a temporary workaround
	 * 
	 */
	private void addExtraceExceptionReplacements(){

		List<Class<? extends Throwable>> classes = Arrays.asList(
				IOException.class,
				//following java.lang
				Throwable.class,
				ArithmeticException.class,
				ArrayIndexOutOfBoundsException.class,
				ArrayStoreException.class,
				ClassCastException.class,
				ClassNotFoundException.class,
				CloneNotSupportedException.class,
				EnumConstantNotPresentException.class,
				Exception.class,
				IllegalAccessException.class,
				IllegalArgumentException.class,
				IllegalMonitorStateException.class,
				IllegalStateException.class,
				IllegalThreadStateException.class,
				IndexOutOfBoundsException.class,
				InstantiationException.class,
				InterruptedException.class,
				NegativeArraySizeException.class,
				NoSuchFieldException.class,
				NoSuchMethodException.class,
				NullPointerException.class,
				NumberFormatException.class,
				ReflectiveOperationException.class,
				RuntimeException.class,
				SecurityException.class,
				StringIndexOutOfBoundsException.class,
				TypeNotPresentException.class,
				UnsupportedOperationException.class,
				AbstractMethodError.class,
				AssertionError.class,
				BootstrapMethodError.class,
				ClassCircularityError.class,
				ClassFormatError.class,
				Error.class,
				ExceptionInInitializerError.class,
				IllegalAccessError.class,
				IncompatibleClassChangeError.class,
				InstantiationError.class,
				InternalError.class,
				LinkageError.class,
				NoClassDefFoundError.class,
				NoSuchFieldError.class,
				NoSuchMethodError.class,
				OutOfMemoryError.class,
				StackOverflowError.class,
				ThreadDeath.class,
				UnknownError.class,
				UnsatisfiedLinkError.class,
				UnsupportedClassVersionError.class,
				VerifyError.class,
				VirtualMachineError.class
				);

		for(Class<?> k : classes){
			
			String jvmOriginal = k.getName().replace('.', '/');
			String jvmMock = MockThrowable.class.getName().replace('.', '/'); 
			
			addReplacementCall(new MethodCallReplacement(
					jvmOriginal,	 "getStackTrace", "()[Ljava/lang/StackTraceElement;", Opcodes.INVOKEVIRTUAL,
					jvmMock, "replacement_getStackTrace","(Ljava/lang/Throwable;)[Ljava/lang/StackTraceElement;",
					false, false));
			
			addReplacementCall(new MethodCallReplacement(
					jvmOriginal,"printStackTrace", "(Ljava/io/PrintStream;)V", Opcodes.INVOKEVIRTUAL,
					jvmMock, "printStackTrace", "(Ljava/lang/Throwable;Ljava/io/PrintStream;)V", 
					false, false));
			
			addReplacementCall(new MethodCallReplacement(
					jvmOriginal,"printStackTrace", "(Ljava/io/PrintWriter;)V", Opcodes.INVOKEVIRTUAL,
					jvmMock, "replacement_printStackTrace","(Ljava/lang/Throwable;Ljava/io/PrintWriter;)V",
					false, false));
		}
		
		
	}

	private void handleMockList() {
		for (Class<? extends EvoSuiteMock> mock : MockList.getList()) {

			if(OverrideMock.class.isAssignableFrom(mock)){
				replaceAllConstructors(mock, mock.getSuperclass());
				replaceAllStaticMethods(mock, mock.getSuperclass());
				replaceAllInvokeSpecial(mock, mock.getSuperclass());

			} else if(StaticReplacementMock.class.isAssignableFrom(mock)){

				String mockedName;
				try {
					mockedName = ((StaticReplacementMock)mock.newInstance()).getMockedClassName();
				} catch (InstantiationException | IllegalAccessException e1) {
					logger.error("Cannot instantiate mock "+mock.getCanonicalName());
					continue;
				}
				Class<?> mocked;
				try {
					mocked = StaticReplacementMock.class.getClassLoader().loadClass(mockedName);
				} catch (ClassNotFoundException e) {
					//should never happen
					logger.error("Mock class "+mock.getCanonicalName()+" has non-existent mocked target "+mockedName);
					continue;
				}

				replaceAllStaticMethods(mock, mocked);
				replaceAllInstanceMethodsWithStatic(mock,mocked);
			}
		}
	}

	private void addJavaLangCalls() {

		//java/lang/System
		addReplacementCall(new MethodCallReplacement("java/lang/System", "exit",
				"(I)V",  Opcodes.INVOKESTATIC, "org/evosuite/runtime/System", "exit", "(I)V", false, false));
		addReplacementCall(new MethodCallReplacement("java/lang/System",
				"currentTimeMillis", "()J",  Opcodes.INVOKESTATIC, "org/evosuite/runtime/System",
				"currentTimeMillis", "()J", false, false));
		addReplacementCall(new MethodCallReplacement("java/lang/System",
				"nanoTime", "()J",  Opcodes.INVOKESTATIC, "org/evosuite/runtime/System", "nanoTime", "()J",
				false, false));
		addReplacementCall(new MethodCallReplacement("java/lang/System",
				"identityHashCode", "(Ljava/lang/Object;)I",  Opcodes.INVOKESTATIC,
				"org/evosuite/runtime/System", "identityHashCode",
				"(Ljava/lang/Object;)I", false, false));

		//java/lang/Object
		addReplacementCall(new MethodCallReplacement("java/lang/Object",
				"hashCode", "()I", Opcodes.INVOKEVIRTUAL, "org/evosuite/runtime/System", "identityHashCode",
				"(Ljava/lang/Object;)I", false, false));

		addReplacementCall(new MethodCallReplacement("java/lang/Math", "random",
				"()D", Opcodes.INVOKESTATIC, "org/evosuite/runtime/Random", "nextDouble", "()D", false,
				false));


		//java/lang/Class
		addReplacementCall(new MethodCallReplacement("java/lang/Class",
				"getClasses", "()[Ljava/lang/Class;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getClasses",
				"(Ljava/lang/Class;)[Ljava/lang/Class;", false, false));
		addReplacementCall(new MethodCallReplacement("java/lang/Class",
				"getAnnotations", "()[Ljava/lang/annotation/Annotation;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getAnnotations",
				"(Ljava/lang/Class;)[Ljava/lang/annotation/Annotation;", false, false));
		addReplacementCall(new MethodCallReplacement("java/lang/Class",
				"getFields", "()[Ljava/lang/reflect/Field;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getFields",
				"(Ljava/lang/Class;)[Ljava/lang/reflect/Field;", false, false));
		addReplacementCall(new MethodCallReplacement("java/lang/Class",
				"getConstructors", "()[Ljava/lang/reflect/Constructor;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getConstructors",
				"(Ljava/lang/Class;)[Ljava/lang/reflect/Constructor;", false, false));
		addReplacementCall(new MethodCallReplacement("java/lang/Class",
				"getMethods", "()[Ljava/lang/reflect/Method;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getMethods",
				"(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", false, false));
		addReplacementCall(new MethodCallReplacement("java/lang/Class",
				"getDeclaredClasses", "()[Ljava/lang/Class;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getDeclaredClasses",
				"(Ljava/lang/Class;)[Ljava/lang/Class;", false, false));
		addReplacementCall(new MethodCallReplacement("java/lang/Class",
				"getDeclaredAnnotations", "()[Ljava/lang/annotation/Annotation;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getDeclaredAnnotations",
				"(Ljava/lang/Class;)[Ljava/lang/annotation/Annotation;", false, false));
		addReplacementCall(new MethodCallReplacement("java/lang/Class",
				"getDeclaredFields", "()[Ljava/lang/reflect/Field;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getDeclaredFields",
				"(Ljava/lang/Class;)[Ljava/lang/reflect/Field;", false, false));
		addReplacementCall(new MethodCallReplacement("java/lang/Class",
				"getDeclaredConstructors", "()[Ljava/lang/reflect/Constructor;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getDeclaredConstructors",
				"(Ljava/lang/Class;)[Ljava/lang/reflect/Constructor;", false, false));
		addReplacementCall(new MethodCallReplacement("java/lang/Class",
				"getDeclaredMethods", "()[Ljava/lang/reflect/Method;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getDeclaredMethods",
				"(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", false, false));

		//java/lang/ClassLoader
		addReplacementCall(new MethodCallReplacement("java/lang/ClassLoader",
				"getResource", "(Ljava/lang/String;)Ljava/net/URL;",  Opcodes.INVOKEVIRTUAL, "org/evosuite/runtime/ResourceLoader",
				"getResource", "(Ljava/lang/String;)Ljava/net/URL;", true, false));

	}

	private void addGUICalls() {
		addReplacementCall(new MethodCallReplacement("javax/swing/JComponent",
				"getPreferredSize", "()Ljava/awt/Dimension;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/gui/JComponent", "getPreferredSize",
				"()Ljava/awt/Dimension;", true, false));		
	}

	private void addCalendarCalls() {
		addReplacementCall(new MethodCallReplacement("java/util/Calendar",
				"getInstance", "()Ljava/util/Calendar;", Opcodes.INVOKESTATIC,
				"org/evosuite/runtime/Calendar", "getCalendar",
				"()Ljava/util/Calendar;", false, false));

		addReplacementCall(new MethodCallReplacement("java/util/Calendar",
				"getInstance", "(Ljava/util/Locale;)Ljava/util/Calendar;", Opcodes.INVOKESTATIC,
				"org/evosuite/runtime/Calendar", "getCalendar",
				"(Ljava/util/Locale;)Ljava/util/Calendar;", false, false));

		addReplacementCall(new MethodCallReplacement("java/util/Calendar",
				"getInstance", "(Ljava/util/TimeZone;)Ljava/util/Calendar;", Opcodes.INVOKESTATIC,
				"org/evosuite/runtime/Calendar", "getCalendar",
				"(Ljava/util/TimeZone;)Ljava/util/Calendar;", false, false));

		addReplacementCall(new MethodCallReplacement("java/util/Calendar",
				"getInstance",
				"(Ljava/util/TimeZone;Ljava/util/Locale;)Ljava/util/Calendar;", Opcodes.INVOKESTATIC,
				"org/evosuite/runtime/Calendar", "getCalendar",
				"(Ljava/util/TimeZone;Ljava/util/Locale;)Ljava/util/Calendar;",
				false, false));
	}

	@SuppressWarnings("unused")
	private void addRandomCalls() {
		addReplacementCall(new MethodCallReplacement("java/util/Random", "nextInt",
				"()I", Opcodes.INVOKEVIRTUAL, "org/evosuite/runtime/Random", "nextInt", "()I", true, false));
		addReplacementCall(new MethodCallReplacement("java/util/Random", "nextInt",
				"(I)I", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextInt", "(I)I", true, false));
		addReplacementCall(new MethodCallReplacement("java/util/Random",
				"nextDouble", "()D", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextDouble",
				"()D", true, false));
		addReplacementCall(new MethodCallReplacement("java/util/Random",
				"nextFloat", "()F", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextFloat",
				"()F", true, false));
		addReplacementCall(new MethodCallReplacement("java/util/Random",
				"nextLong", "()J", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextLong", "()J",
				true, false));
		addReplacementCall(new MethodCallReplacement("java/util/Random",
				"nextGaussian", "()D", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextGaussian",
				"()D", true, false));
		addReplacementCall(new MethodCallReplacement("java/util/Random",
				"nextBoolean", "()Z", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextBoolean",
				"()Z", true, false));
		addReplacementCall(new MethodCallReplacement("java/util/Random", "nextBytes",
				"([B)V", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextBytes", "([B)V", true, false));
	}

	private void addSecureRandomCalls() {
		addReplacementCall(new MethodCallReplacement("java/security/SecureRandom",
				"nextInt", "()I", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextInt", "()I",
				true, false));

		addReplacementCall(new MethodCallReplacement("java/security/SecureRandom", "nextInt",
				"(I)I", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextInt", "(I)I", true, false));
		addReplacementCall(new MethodCallReplacement("java/security/SecureRandom",
				"nextDouble", "()D", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextDouble",
				"()D", true, false));
		addReplacementCall(new MethodCallReplacement("java/security/SecureRandom",
				"nextFloat", "()F", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextFloat",
				"()F", true, false));
		addReplacementCall(new MethodCallReplacement("java/security/SecureRandom",
				"nextLong", "()J", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextLong", "()J",
				true, false));
		addReplacementCall(new MethodCallReplacement("java/security/SecureRandom",
				"nextGaussian", "()D", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextGaussian",
				"()D", true, false));
		addReplacementCall(new MethodCallReplacement("java/security/SecureRandom",
				"nextBoolean", "()Z", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextBoolean",
				"()Z", true, false));
		addReplacementCall(new MethodCallReplacement("java/security/SecureRandom", "nextBytes",
				"([B)V", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextBytes", "([B)V", true, false));
	}


	private void replaceAllInstanceMethodsWithStatic(Class<?> mockClass, Class<?> target){

		/*
		 *  replace "fooInstance.bar(x)"  with "MockFooClass.bar(fooInstance,x)"
		 */

		for (Method m : target.getMethods()) {
			if (Modifier.isStatic(m.getModifiers())) {
				continue;
			}

			String desc = Type.getMethodDescriptor(m);
			Type[] argumentTypes = Type.getArgumentTypes(m);
			Type[] mockedArgumentTypes = new Type[argumentTypes.length + 1];
			mockedArgumentTypes[0] = Type.getType(target);
			for(int i = 0; i < argumentTypes.length; i++)
				mockedArgumentTypes[i+1] = argumentTypes[i];
			String mockedDesc = Type.getMethodDescriptor(Type.getReturnType(m), mockedArgumentTypes);
			addReplacementCall(new MethodCallReplacement(
					target.getCanonicalName().replace('.', '/'), m.getName(), desc, Opcodes.INVOKEVIRTUAL, 
					mockClass.getCanonicalName().replace('.', '/'), m.getName(), mockedDesc,
					false, false));
		}
	}	

	private void replaceAllStaticMethods(Class<?> mockClass, Class<?> target)
			throws IllegalArgumentException {

		for (Method m : target.getMethods()) {
			if (!Modifier.isStatic(m.getModifiers())) {
				continue;
			}

			String desc = Type.getMethodDescriptor(m);
			addReplacementCall(new MethodCallReplacement(
					target.getCanonicalName().replace('.', '/'), m.getName(), desc, Opcodes.INVOKESTATIC, 
					mockClass.getCanonicalName().replace('.', '/'), m.getName(), desc,
					false, false));
		}
	}

	/**
	 * Replace all the constructors of {@code target} with a constructor (with
	 * same input parameters) of mock subclass {@code mockClass}.
	 * 
	 * @param mockClass
	 * @param target
	 * @throws IllegalArgumentException
	 */
	private void replaceAllConstructors(Class<?> mockClass, Class<?> target)
			throws IllegalArgumentException {

		if (!target.isAssignableFrom(mockClass)) {
			throw new IllegalArgumentException(
					"Constructor replacement can be done only for subclasses. Class "
							+ mockClass + " is not an instance of " + target);
		}

		for (Constructor<?> constructor : mockClass.getConstructors()) {
			String desc = Type.getConstructorDescriptor(constructor);
			addSpecialReplacementCall(new MethodCallReplacement(
					target.getCanonicalName().replace('.', '/'), "<init>", desc, Opcodes.INVOKESPECIAL, 
					mockClass.getCanonicalName().replace('.', '/'), "<init>", desc,
					false, false));
		}
	}

	/**
	 * Replace all the methods of {@code target} with a method (with same input
	 * parameters) of mock subclass {@code mockClass}.
	 * 
	 * @param mockClass
	 * @param target
	 * @throws IllegalArgumentException
	 */
	private void replaceAllInvokeSpecial(Class<?> mockClass, Class<?> target)
			throws IllegalArgumentException {

		if (!target.isAssignableFrom(mockClass)) {
			throw new IllegalArgumentException(
					"Method replacement can be done only for subclasses. Class "
							+ mockClass + " is not an instance of " + target);
		}

		logger.debug("Static Mock: "+mockClass.getCanonicalName()+" for "+target.getCanonicalName());
		for (Method method : mockClass.getMethods()) {
			String desc = Type.getMethodDescriptor(method);
			addSpecialReplacementCall(new MethodCallReplacement(
					target.getCanonicalName().replace('.', '/'), method.getName(), desc, Opcodes.INVOKESPECIAL, 
					mockClass.getCanonicalName().replace('.', '/'), method.getName(),
					desc, false, false));
		}
	}


}
