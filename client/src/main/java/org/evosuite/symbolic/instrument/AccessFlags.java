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
package org.evosuite.symbolic.instrument;

import org.objectweb.asm.Opcodes;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */

/**
 * TODO: Does Asm has this already somewhere?
 *
 * @author csallner@uta.edu (Christoph Csallner)
 */
final class AccessFlags {

    private static boolean is(int access, int mask) {
        return (access & mask) != 0;
    }

    static boolean isAbstract(int access) {
        return is(access, Opcodes.ACC_ABSTRACT);
    }

    static boolean isAnnotation(int access) {
        return is(access, Opcodes.ACC_ANNOTATION);
    }

    static boolean isBridge(int access) {
        return is(access, Opcodes.ACC_BRIDGE);
    }

    static boolean isDeprecated(int access) {
        return is(access, Opcodes.ACC_DEPRECATED);
    }

    static boolean isEnum(int access) {
        return is(access, Opcodes.ACC_ENUM);
    }

    static boolean isFinal(int access) {
        return is(access, Opcodes.ACC_FINAL);
    }

    static boolean isInterface(int access) {
        return is(access, Opcodes.ACC_INTERFACE);
    }

    static boolean isNative(int access) {
        return is(access, Opcodes.ACC_NATIVE);
    }

    static boolean isPrivate(int access) {
        return is(access, Opcodes.ACC_PRIVATE);
    }

    static boolean isProtected(int access) {
        return is(access, Opcodes.ACC_PROTECTED);
    }

    static boolean isPublic(int access) {
        return is(access, Opcodes.ACC_PUBLIC);
    }

    static boolean isStatic(int access) {
        return is(access, Opcodes.ACC_STATIC);
    }

    static boolean isStrict(int access) {
        return is(access, Opcodes.ACC_STRICT);
    }

    static boolean isSuper(int access) {
        return is(access, Opcodes.ACC_SUPER);
    }

    static boolean isSynchronized(int access) {
        return is(access, Opcodes.ACC_SYNCHRONIZED);
    }

    static boolean isSynthetic(int access) {
        return is(access, Opcodes.ACC_SYNTHETIC);
    }

    static boolean isTransient(int access) {
        return is(access, Opcodes.ACC_TRANSIENT);
    }

    static boolean isVarArgs(int access) {
        return is(access, Opcodes.ACC_VARARGS);
    }

    static boolean isVolatile(int access) {
        return is(access, Opcodes.ACC_VOLATILE);
    }
}
