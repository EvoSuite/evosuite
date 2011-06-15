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
package de.unisb.cs.st.evosuite.classcreation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.commons.io.output.NullOutputStream;
import org.eclipse.jdt.core.dom.CompilationUnit;

import de.unisb.cs.st.evosuite.utils.Utils;

/**
 * Class factory creates stubs for the abstract classes.
 * 
 * @author Andrey Tarasevich
 * 
 */
public class ClassFactory {

	public static String getStubDir() {
		return stubsDir;
	}

	/** Abstract class, for which stub should be generated */
	private Class<?> clazz;

	/** Directory which contains stubs */
	private final static String stubsDir = "evosuite-files/Stubs";

	/** Directory with stub source files */
	private String sourceDir;

	/** Directory with compiled stubs */
	private String classDir;

	/**
	 * Creates class stub for the given abstract class.
	 * 
	 * @param clazz
	 *            abstract class.
	 * @return class object iff class stub was successfully created and loaded
	 *         into current context, otherwise returns null.
	 */
	public Class<?> createClass(Class<?> clazz) {
		this.clazz = clazz;
		String packageDir = clazz.getPackage().getName().replace(".", "/") + "/";
		sourceDir = stubsDir + "/src/" + packageDir;
		classDir = stubsDir + "/classes/" + packageDir;

		// Create directories for source code and compiled stubs.
		Utils.createDir(sourceDir);
		Utils.createDir(classDir);

		// Generate source code for the stub.
		SourceCodeGenerator generator = new SourceCodeGenerator(clazz);
		CompilationUnit unit = generator.generateSourceCode();

		// If class wasn't compiled return null.
		if (!compileClass(createSourceFile(unit))) {
			return null;
		}

		// Return class object.
		return loadClass();
	}

	/**
	 * Compile stub source file and write compiled file to {@code classDir}.
	 * 
	 * @param sourceFileName
	 *            name of the source file.
	 * @return true if compilation was successful, false otherwise.
	 */
	private boolean compileClass(String sourceFileName) {

		// Get platform-specific java compiler.
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		NullOutputStream nullOutputStream = new NullOutputStream();

		// Run compilation of the source file. Warnings and errors are
		// hidden with NullOutputStream.
		int compilationResult = compiler.run(null, null, nullOutputStream, sourceDir + sourceFileName);
		String compiledFileName = sourceFileName.replace(".java", ".class");

		// Move file from sourceDir to classDir. This is done, since
		// java compiler outputs compiled files to the directory,
		// where source file located. I didn't found out, how to
		// change the directory.
		Utils.moveFile(new File(sourceDir + compiledFileName), new File(classDir + compiledFileName));

		if (compilationResult != 0) {
			return false;
		}
		return true;
	}

	/**
	 * Save source code to the file.
	 * 
	 * @param unit
	 *            CompilationUnit with source code.
	 * @return null if any problem occurred, otherwise returns name of the file
	 *         with source code.
	 */
	private String createSourceFile(CompilationUnit unit) {
		String sourceFileName = null;

		try {
			sourceFileName = clazz.getSimpleName() + "Stub" + ".java";
			FileWriter writer = new FileWriter(sourceDir + sourceFileName);
			BufferedWriter out = new BufferedWriter(writer);
			out.write(generateComment());
			out.write(unit.toString());
			out.close();
		} catch (IOException e) {
		}

		return sourceFileName;
	}

	/**
	 * Creates license comment for the abstract class stub. This is done in a
	 * such ugly way, since comment generation in AST is very difficult.
	 * 
	 * @return string with license comment.
	 */
	private String generateComment() {
		// Get line separator for the current platform.
		String lineSeparator = System.getProperty("line.separator");

		String comment = "// Copyright (C) 2010 Saarland University" + lineSeparator + "//" + lineSeparator
				+ "// This file is part of EvoSuite." + lineSeparator + "//" + lineSeparator
				+ "// EvoSuite is free software: you can redistribute it and/or modify" + lineSeparator
				+ "// it under the terms of the GNU Lesser Public License as published by" + lineSeparator
				+ "// the Free Software Foundation, either version 3 of the License, or" + lineSeparator
				+ "// (at your option) any later version." + lineSeparator + "//" + lineSeparator
				+ "// EvoSuite is distributed in the hope that it will be useful," + lineSeparator
				+ "// but WITHOUT ANY WARRANTY; without even the implied warranty of" + lineSeparator
				+ "// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the" + lineSeparator
				+ "// GNU Lesser Public License for more details." + lineSeparator + "//" + lineSeparator
				+ "// You should have received a copy of the GNU Lesser Public License" + lineSeparator
				+ "// along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>." + lineSeparator + "//"
				+ lineSeparator + "// Auto-generated class" + lineSeparator + lineSeparator;
		return comment;
	}

	/**
	 * Load class into current context.
	 * 
	 * @return class object iff class was successfully loaded, null otherwise.
	 */
	private Class<?> loadClass() {
		Utils.addURL(stubsDir + "/classes/");
		Class<?> loadedClass = null;
		try {
			loadedClass = Class.forName(clazz.getPackage().getName() + "." + clazz.getSimpleName() + "Stub");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return loadedClass;
	}
}
