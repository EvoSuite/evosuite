/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.testcase.TestCluster;

/**
 * Duplicate static initializers in methods, such that we can explicitly restore
 * the initial state of classes.
 * 
 * @author Gordon Fraser
 * 
 */
public class StaticInitializationClassAdapter extends ClassVisitor {

	private final String className;

	public static List<String> static_classes = new ArrayList<String>();

	private static Logger logger = LoggerFactory.getLogger(StaticInitializationClassAdapter.class);

	private boolean isInterface = false;

	private final List<String> finalFields = new ArrayList<String>();

	public StaticInitializationClassAdapter(ClassVisitor visitor, String className) {
		super(Opcodes.ASM4, visitor);
		this.className = className;
	}

	@Override
	public void visit(int version, int access, String name, String signature,
	        String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		isInterface = ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitField(int, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public FieldVisitor visitField(int access, String name, String desc,
	        String signature, Object value) {
		if ((access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL)
			finalFields.add(name);

		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int methodAccess, String name, String descriptor,
	        String signature, String[] exceptions) {

		MethodVisitor mv = super.visitMethod(methodAccess, name, descriptor, signature,
		                                     exceptions);
		if (name.equals("<clinit>") && !isInterface) {
			logger.info("Found static initializer in class " + className);
			MethodVisitor mv2 = new RemoveFinalMethodAdapter(className,
			        super.visitMethod(methodAccess | Opcodes.ACC_PUBLIC
			                                  | Opcodes.ACC_STATIC, "__STATIC_RESET",
			                          descriptor,
			                          signature, exceptions), finalFields);
			static_classes.add(className.replace('/', '.'));
			TestCluster.registerStaticInitializer(className.replace("/", "."));
			return new MultiMethodVisitor(mv2, mv);
		}
		return mv;
	}
}
