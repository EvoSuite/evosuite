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
package org.evosuite.graphs.cfg;

import org.evosuite.classpath.ResourceList;
import org.evosuite.runtime.instrumentation.RemoveFinalClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The CFGClassAdapter calls a CFG generator for relevant methods
 * 
 * @author Gordon Fraser
 */
public class CFGClassAdapter extends ClassVisitor {

	private static Logger logger = LoggerFactory.getLogger(CFGClassAdapter.class);

	/** Current class */
	private final String className;

	private final ClassLoader classLoader;

	/** Skip methods on enums - at least some */
	private boolean isEnum = false;

	/**
	 * Constructor
	 * 
	 * @param visitor
	 *            a {@link org.objectweb.asm.ClassVisitor} object.
	 * @param className
	 *            a {@link java.lang.String} object.
	 */
	public CFGClassAdapter(ClassLoader classLoader, ClassVisitor visitor, String className) {
		super(Opcodes.ASM5, visitor);
		this.className = className;
		this.classLoader = classLoader;
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visit(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	/** {@inheritDoc} */
	@Override
	public void visit(int version, int access, String name, String signature,
	        String superName, String[] interfaces) {
		if((access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL) {
			RemoveFinalClassAdapter.finalClasses.add(name.replace('/', '.'));
		}

		// We are removing final access to allow mocking
		// TODO: Is this redundant wrt RemoveFinalClassAdapter?
		super.visit(version, access & ~Opcodes.ACC_FINAL, name, signature, superName, interfaces);
		if (superName.equals("java/lang/Enum"))
			isEnum = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String[])
	 */
	/** {@inheritDoc} */
	@Override
	public MethodVisitor visitMethod(int methodAccess, String name, String descriptor,
	        String signature, String[] exceptions) {

		MethodVisitor mv = super.visitMethod(methodAccess, name, descriptor, signature,
		                                     exceptions);
		mv = new JSRInlinerAdapter(mv, methodAccess, name, descriptor, signature, exceptions);


		if ((methodAccess & Opcodes.ACC_SYNTHETIC) != 0
		        || (methodAccess & Opcodes.ACC_BRIDGE) != 0) {
			return mv;
		}

		// We ignore deprecated only for dependencies, not for the SUT
//		if (!Properties.USE_DEPRECATED
//		        && (methodAccess & Opcodes.ACC_DEPRECATED) == Opcodes.ACC_DEPRECATED) {
//			logger.info("Skipping deprecated method " + name);
//			return mv;
//		}

		if (isEnum) {
			if(name.equals("valueOf") || name.equals("values")) {
				logger.info("Skipping enum valueOf");
				return mv;
			}
		    if (name.equals("<init>") && descriptor.equals("(Ljava/lang/String;I)V")) {
				logger.info("Skipping enum default constructor");
				return mv;
			}
		}

		logger.info("Analyzing CFG of "+className);

		String classNameWithDots = ResourceList.getClassNameFromResourcePath(className);

		mv = new CFGMethodAdapter(classLoader, classNameWithDots, methodAccess, name,
		        descriptor, signature, exceptions, mv);
		return mv;
	}
}
