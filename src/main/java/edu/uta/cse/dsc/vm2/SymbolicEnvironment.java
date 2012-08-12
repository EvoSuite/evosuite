package edu.uta.cse.dsc.vm2;

import edu.uta.cse.dsc.MainConfig;
import edu.uta.cse.dsc.instrument.DscInstrumentingClassLoader;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.evosuite.symbolic.expr.StringExpression;
import org.objectweb.asm.Type;

public final class SymbolicEnvironment {

	/**
	 * Stack of function/method/constructor invocation frames
	 */
	private final Deque<Frame> stackFrame = new LinkedList<Frame>();

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
		return stackFrame.peek();
	}

	public void pushFrame(Frame frame) {
		stackFrame.push(frame);
	}

	public Frame callerFrame() {
		Frame top = stackFrame.pop();
		Frame res = stackFrame.peek();
		stackFrame.push(top);
		return res;
	}

	public Frame popFrame() {
		return stackFrame.pop();
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
		if (className == null) {
			// no canonical name
		}
		/*
		 * Field[] fields = claz.getDeclaredFields();
		 * 
		 * final boolean isIgnored = MainConfig.get().isIgnored(className);
		 * 
		 * for (Field field : fields) {
		 * 
		 * final int fieldModifiers = field.getModifiers(); if (isIgnored &&
		 * Modifier.isPrivate(fieldModifiers)) continue; // skip private field
		 * of ignored class.
		 * 
		 * }
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
		stackFrame.clear();
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
			Reference emptyStringRef = this.buildReference(emptyStringArray);
			fakeMainCallerFrame.operandStack.pushRef(emptyStringRef);
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
		return stackFrame.isEmpty();
	}

	public Reference buildReference(Object o) {
		if (o == null) {
			return NullReference.getInstance();
		} else {
			if (o instanceof String) {
				String string = (String) o;
				StringExpression strExpr = ExpressionFactory
						.buildNewStringConstant(string);
				StringReference strRef = new StringReference(strExpr);
				return strRef;
			} else {
				if (!object_to_ref.containsKey(o)) {
					NonNullReference nonNullRef = buildNonNullReference(o
							.getClass().getName());
					object_to_ref.put(o, nonNullRef);
					ref_to_object.put(nonNullRef, o);
					return nonNullRef;
				} else {
					return object_to_ref.get(o);
				}

			}
		}
	}

	private int instanceId = 0;

	/**
	 * This constructor is for references created in instrumented code (NEW,
	 * ANEW, NEWARRAY, etc)
	 * 
	 * @param className
	 * @return
	 */
	public NonNullReference buildNonNullReference(String className) {
		return new NonNullReference(className, instanceId++);
	}

	// @TODO Replace Object with System.identityHashCode and WeakReference to
	// save space
	// Try to avoid this from growing too much
	private final Map<Object, NonNullReference> object_to_ref = new THashMap<Object, NonNullReference>();
	private final Map<NonNullReference, Object> ref_to_object = new THashMap<NonNullReference, Object>();

	public Object getObject(NonNullReference nonNullRef) {
		Object object = ref_to_object.get(nonNullRef);
		return object;
	}

	private HashMap<String, HashMap<NonNullReference, StringExpression>> str_fields = new HashMap<String, HashMap<NonNullReference, StringExpression>>();

	public void updateHeap(String fieldName, NonNullReference ref,
			StringExpression valueExpr) {

		if (!str_fields.containsKey(fieldName)) {
			str_fields.put(fieldName,
					new HashMap<NonNullReference, StringExpression>());
		}
		HashMap<NonNullReference, StringExpression> str_fied_values = str_fields
				.get(fieldName);

		str_fied_values.put(ref, valueExpr);
	}

	public StringExpression getHeap(String fieldDesc,
			NonNullReference nonNullRef) {
		if (!str_fields.containsKey(fieldDesc)) {
			return null;
		}
		HashMap<NonNullReference, StringExpression> str_fied_values = str_fields
				.get(fieldDesc);

		return str_fied_values.get(nonNullRef);
	}
}
