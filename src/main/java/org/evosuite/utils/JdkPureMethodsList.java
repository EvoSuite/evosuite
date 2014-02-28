package org.evosuite.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.objectweb.asm.Type;

public enum JdkPureMethodsList {

	instance;

	private Set<String> pureMethods;

	private JdkPureMethodsList() {
		pureMethods = loadInfo();
	}

	private Set<String> loadInfo() {
		Set<String> set = new HashSet<String>(2020);

		try {
			InputStream fstream = this.getClass().getResourceAsStream(
					"/jdkPureMethods.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				set.add(strLine);
			}
			in.close();
		} catch (IOException e) {
			System.err.println("Wrong filename/path/file is missing");
			e.printStackTrace();
		}
		if (set.isEmpty())
			throw new IllegalStateException(
					"Error in the initialization of the set containing the pure java.* methods");

		return set;
	}

	public boolean checkPurity(BytecodeInstruction fieldCall) {
		if(!fieldCall.isMethodCall())
			throw new IllegalArgumentException("method only accepts method calls");
		
		String paraz = fieldCall.getMethodCallDescriptor();
		Type[] parameters = org.objectweb.asm.Type.getArgumentTypes(paraz);
		String newParams = "";
		if (parameters.length != 0) {
			for (Type i : parameters) {
				newParams = newParams + "," + i.getClassName();
			}
			newParams = newParams.substring(1, newParams.length());
		}
		String qualifiedName = fieldCall.getCalledMethodsClass() + "."
				+ fieldCall.getCalledMethodName() + "(" + newParams + ")";
		return checkPurity(qualifiedName);
	}

	public boolean checkPurity(String qualifiedName) {
		return pureMethods.contains(qualifiedName);
	}
	
	public boolean isPureJDKMethod(Method method) {
		String className = method.getDeclaringClass().getCanonicalName();
		if(!className.startsWith("java."))
			return false;
		
		String toAnalyze = className + "." + method.getName();

		Type[] parameters = org.objectweb.asm.Type.getArgumentTypes(method);
		String newParams = "";
		if (parameters.length != 0) {
			for (Type i : parameters) {
				newParams = newParams + "," + i.getClassName();
			}
			newParams = newParams.substring(1, newParams.length());
		}
		toAnalyze += "(" + newParams + ")";
			//System.out.println(toAnalyze);

		return checkPurity(toAnalyze);
	}

}
