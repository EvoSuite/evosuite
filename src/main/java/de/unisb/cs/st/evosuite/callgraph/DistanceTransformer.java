/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.callgraph;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.utils.Utils;

public class DistanceTransformer implements ClassFileTransformer {

	public static class ClassEntry {

		String name;

		Set<String> supers;

		public ClassEntry(String name, Set<String> supers) {
			super();
			this.name = name.replace('/', '.');
			this.supers = new HashSet<String>();
			for (String s : supers) {
				this.supers.add(s.replace('/', '.'));
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ClassEntry other = (ClassEntry) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		public String getName() {
			return name;
		}

		public Set<String> getSupers() {
			return supers;
		}

	}

	public static final class SuperClassAdapter extends ClassVisitor {
		Set<String> supers = new HashSet<String>();

		public SuperClassAdapter(ClassVisitor cv) {
			super(Opcodes.ASM4, cv);
		}

		@Override
		public void visit(int version, int access, String name, String signature,
		        String superName, String[] interfaces) {

			supers.add(superName);
			for (String i : interfaces) {
				supers.add(i);
			}
			super.visit(version, access, name, signature, superName, interfaces);
		}

		public Set<String> getSupers() {
			return supers;
		}
	}

	private final ConnectionData data = new ConnectionData();

	private final AtomicBoolean traceLock = new AtomicBoolean(true);

	Set<ClassEntry> classes = new HashSet<ClassEntry>();

	public void saveData() {
		traceLock.set(false);
		data.save();
		Utils.writeXML(classes, Properties.OUTPUT_DIR + "/" + Properties.HIERARCHY_DATA);
	}

	@Override
	@Deprecated
	public byte[] transform(ClassLoader loader, String className,
	        Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
	        byte[] classfileBuffer) throws IllegalClassFormatException {
		String classNameWithDots = className.replace('/', '.');
		Set<String> supers = getSuper(classfileBuffer);
		if (traceLock.get()) {
			classes.add(new ClassEntry(className, supers));
			if (classNameWithDots.startsWith(Properties.PROJECT_PREFIX) || classNameWithDots.startsWith(Properties.TARGET_CLASS_PREFIX)) {
				ClassReader cr = new ClassReader(classfileBuffer);
				ClassWriter cw = new ClassWriter(0);
				ClassVisitor cv = new DistanceClassAdapter(cw, data,
				        new HashSet<String>());
				cr.accept(cv, ClassReader.SKIP_FRAMES);
			}
		}
		return classfileBuffer;
	}

	private Set<String> getSuper(byte[] classfileBuffer) {
		ClassReader cr = new ClassReader(classfileBuffer);
		ClassWriter cw = new ClassWriter(0);
		SuperClassAdapter cv = new SuperClassAdapter(cw);
		cr.accept(cv, ClassReader.SKIP_FRAMES);
		return cv.getSupers();
	}

	public ConnectionData getConnectionData() {
		return data;
	}

	public Set<ClassEntry> getClasses() {
		return classes;
	}

}
