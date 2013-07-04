package org.evosuite.testcase;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.LinkedHashSet;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.primitives.ConstantPoolManager;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.Randomness;
import org.objectweb.asm.commons.GeneratorAdapter;

public class ClassPrimitiveStatement extends PrimitiveStatement<Class<?>> {

	private static final long serialVersionUID = -2728777640255424791L;

	private Set<Class<?>> assignableClasses = new LinkedHashSet<Class<?>>();
	
	public ClassPrimitiveStatement(TestCase tc, GenericClass type, Set<Class<?>> assignableClasses) {
		super(tc, type, Randomness.choice(assignableClasses));
		this.assignableClasses.addAll(assignableClasses);
	}

	public ClassPrimitiveStatement(TestCase tc, Class<?> value) {
		super(tc, new GenericClass(Class.class).getWithWildcardTypes(), value);
	}

	public ClassPrimitiveStatement(TestCase tc) {
		super(tc, new GenericClass(Class.class).getWithWildcardTypes(),
		        Properties.getTargetClass());
	}

	@Override
	public boolean hasMoreThanOneValue() {
		return assignableClasses.size() != 1;
	}
	
	@Override
	public void delta() {
		randomize();
	}

	@Override
	public void zero() {
		this.value = Properties.getTargetClass();
	}

	@Override
	protected void pushBytecode(GeneratorAdapter mg) {
		// TODO Auto-generated method stub

	}

	private Class<?> getType(org.objectweb.asm.Type type) throws ClassNotFoundException {
		// Not quite sure why we have to treat primitives explicitly...
		switch(type.getSort()) {
		case org.objectweb.asm.Type.ARRAY:
			org.objectweb.asm.Type componentType = type.getElementType();
			Class<?> componentClass = getType(componentType);
			Class<?> arrayClass = Array.newInstance(componentClass, 0).getClass();
			return arrayClass;
		case org.objectweb.asm.Type.BOOLEAN:
			return boolean.class;
		case org.objectweb.asm.Type.BYTE:
			return byte.class;
		case org.objectweb.asm.Type.CHAR:
			return char.class;
		case org.objectweb.asm.Type.DOUBLE:
			return double.class;
		case org.objectweb.asm.Type.FLOAT:
			return float.class;
		case org.objectweb.asm.Type.INT:
			return int.class;
		case org.objectweb.asm.Type.LONG:
			return long.class;
		case org.objectweb.asm.Type.SHORT:
			return short.class;
		default:
			return Class.forName(type.getClassName(), true,
					TestGenerationContext.getClassLoader());
		}
	}
	
	@Override
	public void randomize() {
		if(!assignableClasses.isEmpty()) {
			value = Randomness.choice(assignableClasses);
		} else {
			org.objectweb.asm.Type type = ConstantPoolManager.getInstance().getConstantPool().getRandomType();
			try {
				value = getType(type);
			} catch (ClassNotFoundException e) {
				logger.warn("Error loading class " + type.getClassName() + ": " + e);
			} catch (NoClassDefFoundError e) {
				logger.warn("Error loading class " + type.getClassName() + ": " + e);
			} catch (ExceptionInInitializerError e) {
				logger.warn("Error loading class " + type.getClassName() + ": " + e);
			}
		}
	}

	@Override
	public void changeClassLoader(ClassLoader loader) {
		super.changeClassLoader(loader);
		Class<?> currentClass = value;
		try {
			value = loader.loadClass(currentClass.getCanonicalName());
		} catch (ClassNotFoundException e) {
			logger.warn("Could not load class in new classloader: " + currentClass);
		}
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		Class<?> currentClass = value;
		oos.writeObject(currentClass.getName());
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		String name = (String) ois.readObject();
		try {
			value = TestGenerationContext.getClassLoader().loadClass(name);
		} catch (ClassNotFoundException e) {
			logger.warn("Could not load class in new classloader: " + name);
		}

	}
}
