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

package org.evosuite.instrumentation;

import org.objectweb.asm.*;

/**
 * <p>EmptyVisitor class.</p>
 *
 * @author fraser
 */
public class EmptyVisitor extends ClassVisitor {

    AnnotationVisitor av = new AnnotationVisitor(Opcodes.ASM9) {

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            return this;
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return this;
        }
    };

    /**
     * <p>Constructor for EmptyVisitor.</p>
     */
    public EmptyVisitor() {
        super(Opcodes.ASM9);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return av;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldVisitor visitField(int access, String name, String desc,
                                   String signature, Object value) {
        return new FieldVisitor(Opcodes.ASM9) {

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                return av;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        return new MethodVisitor(Opcodes.ASM9) {

            @Override
            public AnnotationVisitor visitAnnotationDefault() {
                return av;
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                return av;
            }

            @Override
            public AnnotationVisitor visitParameterAnnotation(int parameter, String desc,
                                                              boolean visible) {
                return av;
            }
        };
    }
}
