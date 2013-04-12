package org.evosuite.testcase;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.primitives.ConstantPoolManager;
import org.evosuite.setup.TestCluster;
import org.evosuite.utils.Randomness;
import org.objectweb.asm.commons.GeneratorAdapter;


public class ClassPrimitiveStatement extends PrimitiveStatement<Class<?>> {

	private static final long serialVersionUID = -2728777640255424791L;

	public ClassPrimitiveStatement(TestCase tc, Class<?> value) {
		super(tc, Class.class, value);
	}
	
	public ClassPrimitiveStatement(TestCase tc) {
		super(tc, Class.class, Properties.getTargetClass());
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

	@Override
	public void randomize() {
		org.objectweb.asm.Type type = ConstantPoolManager.getInstance().getConstantPool().getRandomType();
		try {
			value = Class.forName(type.getClassName(), true, TestGenerationContext.getClassLoader());
		} catch (ClassNotFoundException e) {
			logger.warn("Error loading class: "+e);
		}
	}
	
	@Override
	public void changeClassLoader(ClassLoader loader) {
		Class<?> currentClass = (Class<?>)value;
		try {
			value = loader.loadClass(currentClass.getCanonicalName());
		} catch(ClassNotFoundException e) {
			logger.warn("Could not load class in new classloader: "+currentClass);
		}
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
		Class<?> currentClass = (Class<?>)value;
		oos.writeObject(currentClass.getName());
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		String name = (String) ois.readObject();
		try {
			value = TestGenerationContext.getClassLoader().loadClass(name);
		} catch(ClassNotFoundException e) {
			logger.warn("Could not load class in new classloader: "+name);
		}

	}
}
