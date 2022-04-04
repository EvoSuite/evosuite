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
package org.evosuite.runtime.agent;

import org.evosuite.PackageInfo;
import org.evosuite.runtime.instrumentation.RuntimeInstrumentation;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Once the agent is hooked to the current JVM, each time a class is
 * loaded, its byte[] representation will be first given as input
 * to this class, which can modify/instrument it
 *
 * @author arcuri
 */
public class TransformerForTests implements ClassFileTransformer {

    protected static final Logger logger = LoggerFactory.getLogger(TransformerForTests.class);

    private volatile boolean active;
    private final RuntimeInstrumentation instrumenter;

    private final Set<String> instrumentedClasses;

    public TransformerForTests() {
        active = false;
        instrumenter = new RuntimeInstrumentation();
        instrumentedClasses = new LinkedHashSet<>();
    }

    public void setRetransformingMode(boolean on) {
        instrumenter.setRetransformingMode(on);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {

        String classWithDots = className.replace('/', '.');
        if (!active || !RuntimeInstrumentation.checkIfCanInstrument(classWithDots) || classWithDots.startsWith(PackageInfo.getEvoSuitePackage())) {
            return classfileBuffer;
        } else {
            //ClassResetter.getInstance().setClassLoader(loader);

            ClassReader reader = new ClassReader(classfileBuffer);
            synchronized (instrumentedClasses) {
                instrumentedClasses.add(classWithDots);
            }

            logger.debug("Going to instrument: " + classWithDots);

            if (instrumenter.isAlreadyInstrumented(new ClassReader(classfileBuffer))) {
                logger.debug("Skipping transformation of {} as it is already instrumented", classWithDots);
                return classfileBuffer;
            }

            return instrumenter.transformBytes(loader, className, reader, false); // TODO: Need to set skip instrumentation for test class
        }
    }

    public boolean isClassAlreadyTransformed(String className) {
        synchronized (instrumentedClasses) {
            return instrumentedClasses.contains(className);
        }
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }
}
