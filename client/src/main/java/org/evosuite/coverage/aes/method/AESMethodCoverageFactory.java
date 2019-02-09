package org.evosuite.coverage.aes.method;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.setup.TestUsageChecker;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.objectweb.asm.Type;

public class AESMethodCoverageFactory extends 
	AbstractFitnessFactory<MethodCoverageTestFitness> {

	private boolean filterPublicModifier = false;
	
	public AESMethodCoverageFactory() {
		this(false);
	}
	
	public AESMethodCoverageFactory(boolean filterPublicModifier) {
		this.setPublicFilter(filterPublicModifier);
	}

	public AESMethodCoverageFactory setPublicFilter(boolean filterPublicModifier) {
		this.filterPublicModifier = filterPublicModifier;
		return this;
	}
	
	@Override
	public List<MethodCoverageTestFitness> getCoverageGoals() {
		String className = Properties.TARGET_CLASS;
		Class<?> targetClass = Properties.getInitializedTargetClass();         //modified harsh
		List<MethodCoverageTestFitness> goals = new ArrayList<MethodCoverageTestFitness>();
		
		if (targetClass != null) {
			Constructor<?>[] allConstructors = targetClass.getDeclaredConstructors();
			
			for (Constructor<?> c : allConstructors) {
				if (TestUsageChecker.canUse(c) && checkModifiers(c)) {
					String methodName = "<init>" + Type.getConstructorDescriptor(c);
					goals.add(new MethodCoverageTestFitness(className, methodName));
				}
			}
			
			Method[] allMethods = targetClass.getDeclaredMethods();
			for (Method m : allMethods) {
				if (TestUsageChecker.canUse(m) && checkModifiers(m)) {
					String methodName = m.getName() + Type.getMethodDescriptor(m);
					goals.add(new MethodCoverageTestFitness(className, methodName));
				}
			}
		}
		
		goals.add(new UnreachableMethodCoverageTestFitness());
		return goals;
	}

	private boolean checkModifiers(Constructor<?> c) {
		return checkModifiers(c.getModifiers());
	}
	
	private boolean checkModifiers(Method m) {
		return checkModifiers(m.getModifiers());
	}

	private boolean checkModifiers(int modifiers) {
		if (filterPublicModifier) {
			return Modifier.isPublic(modifiers);
		}
		return true;
	}

}
