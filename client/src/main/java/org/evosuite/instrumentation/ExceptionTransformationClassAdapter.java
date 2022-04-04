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

import org.evosuite.classpath.ResourceList;
import org.evosuite.runtime.classhandling.ClassResetter;
import org.evosuite.setup.DependencyAnalysis;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by gordon on 17/03/2016.
 */
public class ExceptionTransformationClassAdapter extends ClassVisitor {

    private final static Logger logger = LoggerFactory.getLogger(ExceptionTransformationClassAdapter.class);

    private final String className;

    public static Map<String, Map<String, Set<Type>>> methodExceptionMap = new LinkedHashMap<>();

    public ExceptionTransformationClassAdapter(ClassVisitor cv, String className) {
        super(Opcodes.ASM9, cv);
        this.className = className;
        if (!methodExceptionMap.containsKey(className))
            methodExceptionMap.put(className, new LinkedHashMap<>());
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals("<clinit>"))
            return mv;

        if (name.equals(ClassResetter.STATIC_RESET))
            return mv;

        if (!DependencyAnalysis.shouldInstrument(ResourceList.getClassNameFromResourcePath(className), name + desc))
            return mv;

        logger.info("Applying exception transformation to " + className + ", method " + name
                + desc);
        return new ExceptionTransformationMethodAdapter(mv, className, name, access, desc);
    }
}
