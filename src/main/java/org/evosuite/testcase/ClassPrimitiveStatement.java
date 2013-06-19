package org.evosuite.testcase;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.primitives.ConstantPoolManager;
import org.evosuite.utils.GenericClass;
import org.objectweb.asm.commons.GeneratorAdapter;

public class ClassPrimitiveStatement extends PrimitiveStatement<Class<?>> {

	private static final long serialVersionUID = -2728777640255424791L;

	public ClassPrimitiveStatement(TestCase tc, Class<?> value) {
		super(tc, new GenericClass(Class.class).getWithWildcardTypes(), value);
	}

	public ClassPrimitiveStatement(TestCase tc) {
		super(tc, new GenericClass(Class.class).getWithWildcardTypes(),
		        Properties.getTargetClass());
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
		if(type.getSort() == org.objectweb.asm.Type.ARRAY) {
			org.objectweb.asm.Type componentType = type.getElementType();
			Class<?> componentClass = getType(componentType);
			Class<?> arrayClass = Array.newInstance(componentClass, 0).getClass();
			return arrayClass;
		} else {
			return Class.forName(type.getClassName(), true,
					TestGenerationContext.getClassLoader());
		}
	}
	
	@Override
	public void randomize() {
		org.objectweb.asm.Type type = ConstantPoolManager.getInstance().getConstantPool().getRandomType();
		try {
			value = getType(type);
		} catch (ClassNotFoundException e) {
			logger.warn("Error loading class " + type.getClassName() + ": " + e);
		} catch (NoClassDefFoundError e) {
			logger.warn("Error loading class " + type.getClassName() + ": " + e);
		}
	}

	@Override
	public void changeClassLoader(ClassLoader loader) {
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
