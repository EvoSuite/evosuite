package org.evosuite.instrumentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ResourceList;
import org.evosuite.runtime.util.ComputeClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonInstrumentingClassLoader extends ClassLoader {
	private final static Logger logger = LoggerFactory.getLogger(NonInstrumentingClassLoader.class);
	private final ClassLoader classLoader;
	private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();

	/**
	 * <p>
	 * Constructor for InstrumentingClassLoader.
	 * </p>
	 */
	public NonInstrumentingClassLoader(ClassLoader parent) {
		super(parent);
		setClassAssertionStatus(Properties.TARGET_CLASS, true);
		classLoader = parent; //NonInstrumentingClassLoader.class.getClassLoader();

	}

	public List<String> getViewOfInstrumentedClasses(){
		List<String> list = new ArrayList<>();
		list.addAll(classes.keySet());
		return list;
	}
	
	
	public Class<?> loadClassFromFile(String fullyQualifiedTargetClass, String fileName) throws ClassNotFoundException {

		String className = fullyQualifiedTargetClass.replace('.', '/');
		Class<?> result = findLoadedClass(fullyQualifiedTargetClass);
		if (result != null) {
			return result;
		} else {

			result = classes.get(fullyQualifiedTargetClass);
			if (result != null) {
				return result;
			}
		}
		InputStream is = null;
		try {
			is = new FileInputStream(new File(fileName));
			byte[] byteBuffer = readBytes(this, className,
			                                                   new ClassReader(is));
			createPackageDefinition(fullyQualifiedTargetClass);
			result = defineClass(fullyQualifiedTargetClass, byteBuffer, 0,
			                              byteBuffer.length);
			classes.put(fullyQualifiedTargetClass, result);
			logger.info("Keeping class: " + fullyQualifiedTargetClass);
			return result;
		} catch (Throwable t) {
			logger.info("Error while loading class: "+t);
			throw new ClassNotFoundException(t.getMessage(), t);
		} finally {
			if(is != null)
				try {
					is.close();
				} catch (IOException e) {
					throw new Error(e);
				}
		}	
	}
	
	/** {@inheritDoc} */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		//if (instrumentation.isTargetProject(name)) {
		// if (TestCluster.isTargetClassName(name)) {
		if (!BytecodeInstrumentation.checkIfCanInstrument(name)
		        //|| (Properties.VIRTUAL_FS && (name.startsWith("org.apache.commons.vfs") || name.startsWith("org.apache.commons.logging")))
		        ) {
			Class<?> result = findLoadedClass(name);
			if (result != null) {
				return result;
			}
			result = classLoader.loadClass(name);
			return result;

		} else {
			Class<?> result = findLoadedClass(name);
			if (result != null) {
				return result;
			} else {

				result = classes.get(name);
				if (result != null) {
					return result;
				} else {
					logger.info("Seeing class for first time: " + name);
					Class<?> instrumentedClass = null;
					//LoggingUtils.muteCurrentOutAndErrStream();
					try {
						instrumentedClass = instrumentClass(name);
						classes.put(name, instrumentedClass);
					} finally {
						//LoggingUtils.restorePreviousOutAndErrStream();
					}
					return instrumentedClass;
				}
			}

		}
	}

	private Class<?> instrumentClass(String fullyQualifiedTargetClass)
	        throws ClassNotFoundException {
		logger.info("Instrumenting class '" + fullyQualifiedTargetClass + "'.");
		
		InputStream is = null;
		try {
			String className = fullyQualifiedTargetClass.replace('.', '/');

			is = ResourceList.getInstance(classLoader).getClassAsStream(fullyQualifiedTargetClass);
			
			if (is == null) {
				throw new ClassNotFoundException("Class '" + className + ".class"
						+ "' should be in target project, but could not be found!");
			}
			
			byte[] byteBuffer = readBytes(this, className,
			                                                   new ClassReader(is));
			createPackageDefinition(fullyQualifiedTargetClass);
			Class<?> result = defineClass(fullyQualifiedTargetClass, byteBuffer, 0,
			                              byteBuffer.length);
			classes.put(fullyQualifiedTargetClass, result);
			logger.info("Keeping class: " + fullyQualifiedTargetClass);
			return result;
		} catch (Throwable t) {
			logger.info("Error while loading class: "+t);
			throw new ClassNotFoundException(t.getMessage(), t);
		} finally {
			if(is != null)
				try {
					is.close();
				} catch (IOException e) {
					throw new Error(e);
				}
		}
	}

	public boolean hasInstrumentedClass(String className) {
		return classes.containsKey(className);
	}

	/**
	 * Before a new class is defined, we need to create a package definition for it
	 * 
	 * @param className
	 */
	private void createPackageDefinition(String className){
		int i = className.lastIndexOf('.');
		if (i != -1) {
		    String pkgname = className.substring(0, i);
		    // Check if package already loaded.
		    Package pkg = getPackage(pkgname);
		    if(pkg==null){
		    		definePackage(pkgname, null, null, null, null, null, null, null);
		    		logger.info("Defined package (3): "+getPackage(pkgname)+", "+getPackage(pkgname).hashCode());
		    }
	    }
	}

	public byte[] readBytes(ClassLoader classLoader, String className,
	        ClassReader reader) {
		int readFlags = ClassReader.SKIP_FRAMES;

		/*
		 *  To use COMPUTE_FRAMES we need to remove JSR commands.
		 *  Therefore, we have a JSRInlinerAdapter in NonTargetClassAdapter
		 *  as well as CFGAdapter.
		 */
		int asmFlags = ClassWriter.COMPUTE_FRAMES;
		ClassWriter writer = new ComputeClassWriter(asmFlags);

		ClassVisitor cv = writer;
		cv = new NonTargetClassAdapter(cv, className);
		reader.accept(cv, readFlags);
		return writer.toByteArray();
	}
}
