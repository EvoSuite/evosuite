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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */


/**
 * The main instrumentation class is {@link ConcolicMethodAdapter}
 *
 * @author csallner@uta.edu (Christoph Csallner)
 */
final class ConcolicClassAdapter extends ClassVisitor {

    private final String className;

    ConcolicClassAdapter(ClassVisitor cv, String className) {
        super(Opcodes.ASM9, cv);
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(
            int access,
            String methName,
            String methDesc,
            String methSignGeneric,
            String[] exceptions) {
        MethodVisitor mv;
        mv = cv.visitMethod(access, methName, methDesc, methSignGeneric, exceptions);
        // Added to handle Java 7
        mv = new JSRInlinerAdapter(mv, access, methName, methDesc, methSignGeneric, exceptions);
        if (mv != null) {
            mv = new ConcolicMethodAdapter(mv, access, className, methName, methDesc);
        }
        return mv;
    }

}
