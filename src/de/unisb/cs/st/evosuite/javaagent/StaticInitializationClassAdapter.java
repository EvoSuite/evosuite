/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.javaagent;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Duplicate static initializers in methods, such that we can explicitly
 * restore the initial state of classes.
 * 
 * @author Gordon Fraser
 *
 */
public class StaticInitializationClassAdapter extends ClassAdapter {

	
	private String className;
	
	public static List<String> static_classes = new ArrayList<String>();
	
	private static Logger logger = Logger.getLogger(StaticInitializationClassAdapter.class);
	
	public StaticInitializationClassAdapter (ClassVisitor visitor, String className) {
		super(visitor);
		this.className = className;
	}

	public MethodVisitor visitMethod(int methodAccess, String name,
			String descriptor, String signature, String[] exceptions) {

		MethodVisitor mv = super.visitMethod(methodAccess, name, descriptor,
				signature, exceptions);
		if(name.equals("<clinit>")) {
			logger.info("Found static initializer in class "+className);
			MethodVisitor mv2 = super.visitMethod(methodAccess | Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "__STATIC_RESET", descriptor, signature, exceptions);
			static_classes.add(className.replace('/', '.'));
			return new MultiMethodVisitor(mv2, mv);
		}
		return mv;
	}
}
