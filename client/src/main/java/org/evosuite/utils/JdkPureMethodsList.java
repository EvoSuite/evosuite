/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.utils;

import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.objectweb.asm.Type;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * This class maintains a set of methods from the Java API known to be cheap-pure.
 * (For a definition of cheap-purity, refer to {@link org.evosuite.assertion.CheapPurityAnalyzer
 * CheapPurityAnalyzer}.) For the sake of efficiency, the purity analysis has to be carried out
 * offline, i.e., prior to starting EvoSuite, using some external tool. (Analyzing the entire JDK
 * anew every time EvoSuite is launched would incur considerable overhead.) Instead, this class
 * reads from a file {@link JdkPureMethodsList#JDK_PURE_METHODS_TXT} that contains the
 * fully-qualified signature of all cheap-pure JDK methods (e.g.,
 * {@code java.beans.Beans.getInstanceOf(java.lang.Object,java.lang.Class<?>)}) separated by
 * newlines. The list of cheap-pure methods can then be accessed using
 * {@code JdkPureMethodsList.instance}.
 */
public enum JdkPureMethodsList {

    instance;

    private final Set<String> pureMethods;

    JdkPureMethodsList() {
        pureMethods = loadInfo();
    }

    /**
     * Tries to read the file {@code JDK_PURE_METHODS_TXT} that contains the precomputed list
     * of cheap-pure JDK methods. Returns a set of Strings, where every String corresponds to the
     * fully qualified signature of a method.
     *
     * @return signatures of cheap-pure JDK methods
     */
    private Set<String> loadInfo() {
        Set<String> set = new HashSet<>(2020);

        try (
            InputStream fstream = this.getClass().getResourceAsStream(
                    "/jdkPureMethods.txt");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in)); ) {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                set.add(strLine);
            }
        } catch (IOException e) {
            System.err.println("Wrong filename/path/file is missing");
            e.printStackTrace();
        }
        if (set.isEmpty())
            throw new IllegalStateException(
                    "Error in the initialization of the set containing the pure java.* methods");

        return set;
    }

    /**
     * Tells whether the given byte code instruction invokes a JDK method that is cheap-pure.
     * If the given instruction is not a method call, an {@code IllegalArgumentException} is
     * thrown.
     *
     * @param fieldCall the byte code instruction to test
     * @return {@code true} if the invoked method is cheap-pure, {@code false} otherwise
     */
    public boolean checkPurity(BytecodeInstruction fieldCall) {
        if (!fieldCall.isMethodCall())
            throw new IllegalArgumentException("method only accepts method calls");

        String paraz = fieldCall.getMethodCallDescriptor();
        Type[] parameters = org.objectweb.asm.Type.getArgumentTypes(paraz);
        String newParams = "";
        if (parameters.length != 0) {
            for (Type i : parameters) {
                newParams = newParams + "," + i.getClassName();
            }
            newParams = newParams.substring(1);
        }
        String qualifiedName = fieldCall.getCalledMethodsClass() + "."
                + fieldCall.getCalledMethodName() + "(" + newParams + ")";
        return checkPurity(qualifiedName);
    }

    /**
     * Tells whether the method with the given fully-qualified signature (e.g.,
     * {@code java.beans.Beans.getInstanceOf(java.lang.Object,java.lang.Class<?>)}) is cheap-pure
     *
     * @param qualifiedName the fully-qualified method signature
     * @return {@code true} if the method is cheap-pure, {@code false} otherwise
     */
    public boolean checkPurity(String qualifiedName) {
        return pureMethods.contains(qualifiedName);
    }

    /**
     * Tells whether the given {@code method} is cheap-pure.
     *
     * @param method the method to analyze
     * @return {@code true} if the method is cheap-pure, {@code false} otherwise
     */
    public boolean isPureJDKMethod(Method method) {
        String className = method.getDeclaringClass().getCanonicalName();
        if (!className.startsWith("java."))
            return false;

        String toAnalyze = className + "." + method.getName();

        Type[] parameters = org.objectweb.asm.Type.getArgumentTypes(method);
        String newParams = "";
        if (parameters.length != 0) {
            for (Type i : parameters) {
                newParams = newParams + "," + i.getClassName();
            }
            newParams = newParams.substring(1);
        }
        toAnalyze += "(" + newParams + ")";
        //System.out.println(toAnalyze);

        return checkPurity(toAnalyze);
    }

}
