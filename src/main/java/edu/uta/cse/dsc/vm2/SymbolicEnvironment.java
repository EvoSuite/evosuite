package edu.uta.cse.dsc.vm2;

import edu.uta.cse.dsc.MainConfig;
import edu.uta.cse.dsc.instrument.DscInstrumentingClassLoader;
import gnu.trove.set.hash.THashSet;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;

import org.objectweb.asm.Type;

public final class SymbolicEnvironment {

	/**
	 * Stack of function/method/constructor invocation frames
	 */
	private final Deque<Frame> frames = new LinkedList<Frame>();

	/**
	 * Classes whose static fields have been set to the default zero value or a
	 * dummy value.
	 */
	private final Set<Class<?>> preparedClasses = new THashSet<Class<?>>();

	private final DscInstrumentingClassLoader classLoader;

	public SymbolicEnvironment(DscInstrumentingClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public Frame topFrame() {
		return frames.peek();
	}

	public void pushFrame(Frame frame) {
		frames.push(frame);
	}

	public Frame callerFrame() {
		Frame top = frames.pop();
		Frame res = frames.peek();
		frames.push(top);
		return res;
	}

	public Frame popFrame() {
		return frames.pop();
	}

	public Class<?> ensurePrepared(String className) {
		Type ownerType = Type.getType(className);
		if (ownerType.getSort() == Type.ARRAY) {
			Type elemType = ownerType.getElementType();
			className = elemType.getClassName();
		}
		Class<?> claz = null;
		claz = classLoader.getClassForName(className);
		ensurePrepared(claz);
		return claz;
	}

	public void ensurePrepared(Class<?> claz) {
		if (preparedClasses.contains(claz))
			return; // done, we have prepared this class earlier

		Class<?> superClass = claz.getSuperclass();
		if (superClass != null)
			ensurePrepared(superClass); // prepare super class first

		String className = claz.getCanonicalName();
		if (className==null ) {
			// no canonical name
		}
		/*
		Field[] fields = claz.getDeclaredFields();

		final boolean isIgnored = MainConfig.get().isIgnored(className);

		for (Field field : fields) {

			final int fieldModifiers = field.getModifiers();
			if (isIgnored && Modifier.isPrivate(fieldModifiers))
				continue; // skip private field of ignored class.

		}
		*/
		preparedClasses.add(claz);

	}

	/**
	 * Prepare stack of function invocation frames.
	 * 
	 * Clear function invocation stack, push a frame that pretends to call the
	 * method under test. We push variables for our method onto the
	 * pseudo-callers stack, so our method can pop them from there.
	 */
	public void prepareStack(Method mainMethod) {
		frames.clear();
		// bottom of the stack trace
		this.pushFrame(new FakeBottomFrame());

		// frame for argument purposes
		final FakeMainCallerFrame fakeMainCallerFrame = new FakeMainCallerFrame(
				mainMethod, MainConfig.get().MAX_LOCALS_DEFAULT); // fake caller
																	// of method
																	// under
																	// test

		if (mainMethod != null) {
			boolean isInstrumented = isInstrumented(mainMethod);
			fakeMainCallerFrame.invokeInstrumentedCode(isInstrumented);
			String[] emptyStringArray = new String[] {};
			fakeMainCallerFrame.operandStack.pushRef(emptyStringArray);
		}
		this.pushFrame(fakeMainCallerFrame);
	}

	/**
	 * @return method is instrumented. It is neither native nor declared by an
	 *         ignored JDK class, etc.
	 */
	private static boolean isInstrumented(Method method) {
		if (Modifier.isNative(method.getModifiers()))
			return false;

		String declClass = method.getDeclaringClass().getCanonicalName();
		return !MainConfig.get().isIgnored(declClass);
	}

	public boolean isEmpty() {
		return frames.isEmpty();
	}

}
