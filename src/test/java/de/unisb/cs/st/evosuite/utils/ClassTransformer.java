package de.unisb.cs.st.evosuite.utils;

import java.util.HashMap;
import java.util.Map;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.javaagent.InstrumentingClassLoader;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;

public class ClassTransformer {

	private static ClassTransformer instance = new ClassTransformer();

	public static ClassTransformer getInstance() {
		return instance;
	}

	private final Map<String, Class<?>> instrumentedClasses = new HashMap<String, Class<?>>();

	private ClassTransformer() {
		// private constructor
	}

	public Class<?> instrumentClass(String fullyQualifiedTargetClass) {
		Class<?> result = instrumentedClasses.get(fullyQualifiedTargetClass);
		if (result != null) {
			assert Properties.TARGET_CLASS.equals(fullyQualifiedTargetClass);
			assert Properties.PROJECT_PREFIX.equals(fullyQualifiedTargetClass);
			return result;
		}
		try {
			Properties.TARGET_CLASS = fullyQualifiedTargetClass;
			Properties.PROJECT_PREFIX = fullyQualifiedTargetClass;
			ExecutionTracer.enable();
			ClassLoader classLoader = new InstrumentingClassLoader();
			result = classLoader.loadClass(fullyQualifiedTargetClass);
			instrumentedClasses.put(fullyQualifiedTargetClass, result);
			return result;
		} catch (ClassNotFoundException exc) {
			throw new RuntimeException(exc);
		}
	}
}
