/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcarver.extraction;

import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ResourceList;
import org.evosuite.runtime.instrumentation.JSRInlinerClassVisitor;
import org.evosuite.runtime.instrumentation.RuntimeInstrumentation;
import org.evosuite.testcarver.instrument.Instrumenter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gordon Fraser
 */
public class CarvingClassLoader extends ClassLoader {
    private final static Logger logger = LoggerFactory.getLogger(CarvingClassLoader.class);
    private final Instrumenter instrumenter = new Instrumenter();
    private final ClassLoader classLoader;
    private final Map<String, Class<?>> classes = new HashMap<>();

    /**
     * <p>
     * Constructor for InstrumentingClassLoader.
     * </p>
     */
    public CarvingClassLoader() {
        classLoader = CarvingClassLoader.class.getClassLoader();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (!RuntimeInstrumentation.checkIfCanInstrument(name)) {
            Class<?> result = findLoadedClass(name);
            if (result != null) {
                return result;
            }
            result = classLoader.loadClass(name);
            return result;

        }

        Class<?> result = classes.get(name);
        if (result != null) {
            return result;
        } else {
            logger.info("Seeing class for first time: " + name);
            return instrumentClass(name);
        }
    }


    private Class<?> instrumentClass(String fullyQualifiedTargetClass)
            throws ClassNotFoundException {
        logger.warn("Instrumenting class '" + fullyQualifiedTargetClass + "'.");

        try {
            String className = fullyQualifiedTargetClass.replace('.', '/');

            InputStream is = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getClassAsStream(className);
            if (is == null) {
                throw new ClassNotFoundException("Class '" + className + ".class"
                        + "' should be in target project, but could not be found!");
            }

            ClassReader reader = new ClassReader(is);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.SKIP_FRAMES);
            instrumenter.transformClassNode(classNode, className);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            classNode.accept(new JSRInlinerClassVisitor(writer));
            //classNode.accept(writer);
            byte[] byteBuffer = writer.toByteArray();
            Class<?> result = defineClass(fullyQualifiedTargetClass, byteBuffer, 0,
                    byteBuffer.length);
            if (Modifier.isPrivate(result.getModifiers())) {
                logger.info("REPLACING PRIVATE CLASS " + fullyQualifiedTargetClass);
                result = super.loadClass(fullyQualifiedTargetClass);
            }
            classes.put(fullyQualifiedTargetClass, result);
            logger.info("Keeping class: " + fullyQualifiedTargetClass);
            return result;
        } catch (Throwable t) {
            logger.info("Error: " + t);
            for (StackTraceElement e : t.getStackTrace()) {
                logger.info(e.toString());
            }
            throw new ClassNotFoundException(t.getMessage(), t);
        }
    }
}
