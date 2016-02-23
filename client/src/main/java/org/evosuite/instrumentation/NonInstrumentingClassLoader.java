/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.instrumentation;

import java.io.IOException;
import java.io.InputStream;

import org.evosuite.TestGenerationContext;
import org.evosuite.runtime.util.ComputeClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import javax.persistence.Entity;

public class NonInstrumentingClassLoader extends InstrumentingClassLoader {

	public NonInstrumentingClassLoader(){
		super();
	}

	/*
	public NonInstrumentingClassLoader(ClassLoader parent) {
		super(parent);
		setClassAssertionStatus(Properties.TARGET_CLASS, true);
		classLoader = parent; //NonInstrumentingClassLoader.class.getClassLoader();

	}
	*/

	@Override
	protected byte[] getTransformedBytes( String className, InputStream is) throws IOException{

		ClassReader reader = new ClassReader(is);
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
