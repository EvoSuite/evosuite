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
package org.evosuite.javaagent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.utils.Utils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Visits given class in order to collect information about classes used.
 * 
 * @author Andrey Tarasevich
 * 
 */
public class CIClassAdapter extends ClassVisitor {

	private String className;

	public CIClassAdapter(ClassVisitor cv) {
		super(Opcodes.ASM4, cv);
	}

	CIMethodAdapter mv = new CIMethodAdapter();
	private final Set<String> classesReferenced = new HashSet<String>();

	@Override
	public void visit(int version, int access, String name, String signature,
	        String superName, String[] interfaces) {
		this.className = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
	        String signature, Object value) {
		if (value != null)
			classesReferenced.addAll(Utils.classesDescFromString(value.toString()));
		classesReferenced.addAll(Utils.classesDescFromString(desc));
		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
	        String signature, String[] exceptions) {
		if (exceptions != null)
			for (String e : exceptions)
				classesReferenced.addAll(Utils.classesDescFromString(e));
		classesReferenced.addAll(Utils.classesDescFromString(desc));
		return mv;
	}

	@Override
	public void visitEnd() {
		saveCItoFile(mv.getClassesReferenced());
		super.visitEnd();
	}

	private void saveCItoFile(Set<String> cr) {
		try {
			cr.remove(className);
			FileWriter fw = new FileWriter(Properties.OUTPUT_DIR + File.separator
			        + className.replace("/", ".") + ".CIs");
			BufferedWriter bw = new BufferedWriter(fw);
			String lineSeparator = System.getProperty("line.separator");
			for (String s : cr)
				bw.write(s + lineSeparator);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}