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

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.classpath.ResourceList;
import org.evosuite.runtime.classhandling.ClassResetter;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestClusterUtils;
import org.evosuite.utils.ArrayUtil;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Take care of all instrumentation that is necessary to trace executions
 *
 * @author Gordon Fraser
 */
public class ExecutionPathClassAdapter extends ClassVisitor {

	private final String className;

	private static boolean isMutation() {
	    return ArrayUtil.contains(Properties.CRITERION, Criterion.MUTATION)
	            || ArrayUtil.contains(Properties.CRITERION, Criterion.STRONGMUTATION)
	            || ArrayUtil.contains(Properties.CRITERION, Criterion.WEAKMUTATION);
	}

	private static Logger logger = LoggerFactory.getLogger(ExecutionPathClassAdapter.class);

	/** Skip methods on enums - at least some */
	private boolean isEnum = false;

	/** Skip default constructors on anonymous classes */
	private boolean isAnonymous = false;

	/**
	 * <p>
	 * Constructor for ExecutionPathClassAdapter.
	 * </p>
	 *
	 * @param visitor
	 *            a {@link org.objectweb.asm.ClassVisitor} object.
	 * @param className
	 *            a {@link java.lang.String} object.
	 */
	public ExecutionPathClassAdapter(ClassVisitor visitor, String className) {
		super(Opcodes.ASM5, visitor);
		this.className = ResourceList.getClassNameFromResourcePath(className);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visit(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	/** {@inheritDoc} */
	@Override
	public void visit(int version, int access, String name, String signature,
	        String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		if (superName.equals("java/lang/Enum"))
			isEnum = true;
		if(TestClusterUtils.isAnonymousClass(name))
			isAnonymous = true;
	}

	/*
	 * Set default access rights to public access rights
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

		// Don't touch bridge and synthetic methods
		if ((methodAccess & Opcodes.ACC_SYNTHETIC) > 0
		        || (methodAccess & Opcodes.ACC_BRIDGE) > 0) {
			return mv;
		}
		if (name.equals("<clinit>"))
			return mv;

		if (name.equals(ClassResetter.STATIC_RESET))
			return mv;

		if (!DependencyAnalysis.shouldInstrument(className, name + descriptor))
			return mv;

		if (isEnum && (name.equals("valueOf") || name.equals("values"))) {
			return mv;
		}

		// Default constructors of anonymous classes are synthetic
		// but the Java Compiler is inconsistent in whether it has
		// line numbers, so we skip it.
		// https://bugs.openjdk.java.net/browse/JDK-8061778
		if (isAnonymous && name.equals("<init>")) {
			return mv;
		}
		
		if (isMutation()) {
			mv = new ReturnValueAdapter(mv, className, name, descriptor);
		}
		mv = new MethodEntryAdapter(mv, methodAccess, className, name, descriptor);
		mv = new LineNumberMethodAdapter(mv, className, name, descriptor);
		mv = new ArrayAllocationLimitMethodAdapter(mv, className, name, methodAccess,
		        descriptor);
		mv = new ExplicitExceptionHandler(mv, className, name, descriptor);
		return mv;
	}

}
