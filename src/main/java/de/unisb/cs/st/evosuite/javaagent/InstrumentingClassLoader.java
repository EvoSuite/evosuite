package de.unisb.cs.st.evosuite.javaagent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;

public class InstrumentingClassLoader extends ClassLoader {
	private final Logger logger = LoggerFactory.getLogger(InstrumentingClassLoader.class);
	private final BytecodeInstrumentation instrumentation;
	private final ClassLoader classLoader;

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
				if (isTargetClass(name)) {
					return instrumentClass(name);
				} else {
					return loadClassByteCode(name);
				}
			}
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
			return result;
		} catch (Exception e) {
			throw new ClassNotFoundException(e.getMessage());
		}
	}

	private boolean isTargetClass(String className) {
		if (className.equals(Properties.TARGET_CLASS)
		        || className.startsWith(Properties.TARGET_CLASS + "$")) {
			return true;
		}
		return false;
	}

	private Class<?> loadClassByteCode(String name) throws ClassNotFoundException {
		if (name.startsWith("java") || name.startsWith("sun")) {
			throw new IllegalStateException("Cannot load java system classes!");
		}
		try {
			InputStream is = ClassLoader.getSystemResourceAsStream(name.replace('.', '/')
			        + ".class");
			if (is == null) {
				throw new ClassNotFoundException(
				        "Class should be in target project, but could not be found!");
			}
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[16384];
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			byte[] byteBuffer = buffer.toByteArray();
			Class<?> result = defineClass(name, byteBuffer, 0, byteBuffer.length);
			return result;
		} catch (IOException exc) {
			return null;
		}
	}
}
