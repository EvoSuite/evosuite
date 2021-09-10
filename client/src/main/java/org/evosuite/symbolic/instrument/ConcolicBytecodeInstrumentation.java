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
package org.evosuite.symbolic.instrument;

import org.evosuite.Properties;
import org.evosuite.instrumentation.EndOfClassInitializerVisitor;
import org.evosuite.instrumentation.StaticAccessClassAdapter;
import org.evosuite.junit.writer.TestSuiteWriterUtils;
import org.evosuite.runtime.instrumentation.CreateClassResetClassAdapter;
import org.evosuite.runtime.instrumentation.MethodCallReplacementClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * This class performns the bytecode transformation. It adds new bytecode for
 * registering the constraints during execution.
 *
 * @author galeotti
 */
public class ConcolicBytecodeInstrumentation {

    //private static Logger logger = LoggerFactory.getLogger(DscBytecodeInstrumentation.class);

    /**
     * Applies DscClassAdapter to the className in the argument
     */
    public byte[] transformBytes(String className, ClassReader reader) {
        int readFlags = ClassReader.SKIP_FRAMES;

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        ClassVisitor cv = writer;

        if (Properties.RESET_STATIC_FIELDS) {
            cv = new StaticAccessClassAdapter(cv, className);
        }

        // Apply transformations to class under test and its owned
        // classes
        // cv = new TraceClassVisitor(cv, new PrintWriter(System.err));
        cv = new ConcolicClassAdapter(cv, className);

        // If we need to reset static constructors, make them explicit methods
        if (Properties.RESET_STATIC_FIELDS) {
            // Create a __STATIC_RESET() cloning the original <clinit> method or create one by default
            final CreateClassResetClassAdapter resetClassAdapter;
            resetClassAdapter = new CreateClassResetClassAdapter(cv, className, Properties.RESET_STATIC_FINAL_FIELDS);
            cv = resetClassAdapter;
            // Add a callback before leaving the <clinit> method

            cv = new EndOfClassInitializerVisitor(cv, className);
        }

        // Mock instrumentation (eg File and TCP).
        if (TestSuiteWriterUtils.needToUseAgent()) {
            cv = new MethodCallReplacementClassAdapter(cv, className);
        }

        reader.accept(cv, readFlags);

        return writer.toByteArray();
    }
}
