package de.unisb.cs.st.evosuite.javaagent;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentingClassLoader extends ClassLoader {
	private final static Logger logger = LoggerFactory.getLogger(InstrumentingClassLoader.class);
	private final BytecodeInstrumentation instrumentation;
	private final ClassLoader classLoader;
	private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();

	public InstrumentingClassLoader() {
		this(new BytecodeInstrumentation());
	}

	public InstrumentingClassLoader(BytecodeInstrumentation instrumentation) {
		super(InstrumentingClassLoader.class.getClassLoader());
		classLoader = InstrumentingClassLoader.class.getClassLoader();
		this.instrumentation = instrumentation;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (instrumentation.isTargetProject(name)) {
			// if (TestCluster.isTargetClassName(name)) {
			Class<?> result = findLoadedClass(name);
			if (result != null) {
				return result;
			} else {
				result = classes.get(name);
				if (result != null) {
					return result;
				} else {
					logger.info("Seeing class for first time: " + name);
					return instrumentClass(name);
				}
			}
		} else {
			logger.trace("Not instrumenting: " + name);
		}
		Class<?> result = findLoadedClass(name);
		if (result != null) {
			return result;
		}
		result = classLoader.loadClass(name);
		return result;
	}

	private Class<?> instrumentClass(String fullyQualifiedTargetClass)
	        throws ClassNotFoundException {
		logger.info("Instrumenting class '" + fullyQualifiedTargetClass + "'.");
		try {
			String className = fullyQualifiedTargetClass.replace('.', '/');
			InputStream is = ClassLoader.getSystemResourceAsStream(className + ".class");
			if (is == null) {
				throw new ClassNotFoundException(
				        "Class should be in target project, but could not be found!");
			}
			byte[] byteBuffer = instrumentation.transformBytes(className,
			                                                   new ClassReader(is));
			Class<?> result = defineClass(fullyQualifiedTargetClass, byteBuffer, 0,
			                              byteBuffer.length);
			classes.put(fullyQualifiedTargetClass, result);
			logger.info("Keeping class: " + fullyQualifiedTargetClass);
			return result;
		} catch (Exception e) {
			throw new ClassNotFoundException(e.getMessage(), e);
		}
	}

}
