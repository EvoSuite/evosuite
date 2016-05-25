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
package org.evosuite.runtime.instrumentation;

import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.evosuite.runtime.classhandling.ClassResetter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Duplicate static initializers in methods, such that we can explicitly restore
 * the initial state of classes.
 * 
 * @author Gordon Fraser
 */
public class CreateClassResetClassAdapter extends ClassVisitor {

	private boolean removeUpdatesOnFinalFields = true;

	public void setRemoveUpdatesOnFinalFields(boolean removeUpdatesOnFinalFields) {
		this.removeUpdatesOnFinalFields = removeUpdatesOnFinalFields;
	}

	private final String className;

	/** Constant <code>static_classes</code> */
	public static List<String> staticClasses = new ArrayList<String>();

	private static Logger logger = LoggerFactory.getLogger(CreateClassResetClassAdapter.class);

	private boolean isInterface = false;

	private boolean isAnonymous = false;

	private boolean clinitFound = false;

	private boolean definesUid = false;

	private boolean resetMethodAdded = false;

	private long serialUID = -1L;

	private final List<String> finalFields = new ArrayList<String>();

	private static final Pattern ANONYMOUS_MATCHER1 = Pattern.compile(".*\\$\\d+.*$");

	/**
	 * <p>
	 * Constructor for StaticInitializationClassAdapter.
	 * </p>
	 * 
	 * @param visitor
	 *            a {@link org.objectweb.asm.ClassVisitor} object.
	 * @param className
	 *            a {@link java.lang.String} object.
	 */
	public CreateClassResetClassAdapter(ClassVisitor visitor, String className,
			boolean removeFinalModifierOnStaticFields) {
		super(Opcodes.ASM5, visitor);
		this.className = className;
		this.removeFinalModifierOnStaticFields = removeFinalModifierOnStaticFields;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		isInterface = ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE);
		if (ANONYMOUS_MATCHER1.matcher(name).matches()) {
			isAnonymous = true;
		}
	}

	static class StaticField {
		String name;
		String desc;
		Object value;
	}

	private final List<StaticField> static_fields = new LinkedList<StaticField>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.ClassAdapter#visitField(int, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {

		if (name.equals("serialVersionUID")) {
			definesUid = true;
			// We must not remove final from serialVersionUID or else the
			// class cannot be serialised and de-serialised any more
			return super.visitField(access, name, desc, signature, value);
		}

		if (hasStaticModifier(access)) {
			StaticField staticField = new StaticField();
			staticField.name = name;
			staticField.desc = desc;
			staticField.value = value;
			static_fields.add(staticField);
		}

		if (!isInterface && removeFinalModifierOnStaticFields) {
			int newAccess = access & (~Opcodes.ACC_FINAL);
			return super.visitField(newAccess, name, desc, signature, value);
		} else {
			if (hasFinalModifier(access))
				finalFields.add(name);

			return super.visitField(access, name, desc, signature, value);
		}
	}

	private boolean hasFinalModifier(int access) {
		return (access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL;
	}

	private boolean hasStaticModifier(int access) {
		return (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
	}

	private final boolean removeFinalModifierOnStaticFields;

	/** {@inheritDoc} */
	@Override
	public MethodVisitor visitMethod(int methodAccess, String methodName, String descriptor, String signature,
			String[] exceptions) {

		MethodVisitor mv = super.visitMethod(methodAccess, methodName, descriptor, signature, exceptions);

		if (methodName.equals("<clinit>") && !isInterface && !isAnonymous && !resetMethodAdded) {
			clinitFound = true;
			logger.info("Found static initializer in class " + className);
			// determineSerialisableUID();

			// duplicates existing <clinit>
			// TODO: Removed | Opcodes.ACC_PUBLIC
			// Does __STATIC_RESET need to be public?
			// <clinit> apparently can be private, resulting
			// in illegal modifiers
			MethodVisitor visitMethod = super.visitMethod(methodAccess | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
					ClassResetter.STATIC_RESET, descriptor, signature, exceptions);

			CreateClassResetMethodAdapter staticResetMethodAdapter = new CreateClassResetMethodAdapter(visitMethod,
					className, this.static_fields, finalFields);

			resetMethodAdded = true;

			if (this.removeUpdatesOnFinalFields) {
				MethodVisitor mv2 = new RemoveFinalMethodAdapter(className, staticResetMethodAdapter, finalFields);

				return new MultiMethodVisitor(mv2, mv);
			} else {
				return new MultiMethodVisitor(staticResetMethodAdapter, mv);
			}
		} else if (methodName.equals(ClassResetter.STATIC_RESET)) {
			if (resetMethodAdded) {
				// Do not add reset method a second time
			} else {
				resetMethodAdded = true;
			}
		}
		return mv;
	}

	@Override
	public void visitEnd() {
		if (!clinitFound && !isInterface && !isAnonymous && !resetMethodAdded) {
			// create brand new __STATIC_RESET
			if (!definesUid) {
				// determineSerialisableUID();
				// createSerialisableUID();
			}
			createEmptyStaticReset();
		} else if (clinitFound) {
			if (!definesUid) {
				// createSerialisableUID();
			}
		}
		super.visitEnd();
	}

	private void determineSerialisableUID() {
		try {
			Class<?> clazz = Class.forName(className.replace('/', '.'), false,
					MethodCallReplacementClassAdapter.class.getClassLoader());
			if (Serializable.class.isAssignableFrom(clazz)) {
				ObjectStreamClass c = ObjectStreamClass.lookup(clazz);
				serialUID = c.getSerialVersionUID();
			}
		} catch (ClassNotFoundException e) {
			logger.info("Failed to add serialId to class " + className + ": " + e.getMessage());
		}

	}

	// This method is a code clone from MethodCallReplacementClassAdapter
	private void createSerialisableUID() {
		// Only add this for serialisable classes
		if (serialUID < 0)
			return;
		/*
		 * If the class is serializable, then adding a hashCode will change the
		 * serialVersionUID if it is not defined in the class. Hence, if it is
		 * not defined, we have to define it to avoid problems in serialising
		 * the class.
		 */
		logger.info("Adding serialId to class " + className);
		visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "serialVersionUID", "J", null,
				serialUID);
	}

	private void createEmptyStaticReset() {
		logger.info("Creating brand-new static initializer in class " + className);
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
				ClassResetter.STATIC_RESET, "()V", null, null);
		mv.visitCode();
		for (StaticField staticField : static_fields) {

			if (!finalFields.contains(staticField.name) && !staticField.name.startsWith("__cobertura")
					&& !staticField.name.startsWith("$jacoco")) {

				logger.info("Adding bytecode for initializing field " + staticField.name);

				if (staticField.value != null) {
					mv.visitLdcInsn(staticField.value);
				} else {
					Type type = Type.getType(staticField.desc);
					switch (type.getSort()) {
					case Type.BOOLEAN:
					case Type.BYTE:
					case Type.CHAR:
					case Type.SHORT:
					case Type.INT:
						mv.visitInsn(Opcodes.ICONST_0);
						break;
					case Type.FLOAT:
						mv.visitInsn(Opcodes.FCONST_0);
						break;
					case Type.LONG:
						mv.visitInsn(Opcodes.LCONST_0);
						break;
					case Type.DOUBLE:
						mv.visitInsn(Opcodes.DCONST_0);
						break;
					case Type.ARRAY:
					case Type.OBJECT:
						mv.visitInsn(Opcodes.ACONST_NULL);
						break;
					}
				}
				mv.visitFieldInsn(Opcodes.PUTSTATIC, className, staticField.name, staticField.desc);

			}
		}
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

	}
}
