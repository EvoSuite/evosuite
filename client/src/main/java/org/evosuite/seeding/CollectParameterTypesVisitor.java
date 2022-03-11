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

package org.evosuite.seeding;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Gordon Fraser
 */
public class CollectParameterTypesVisitor extends SignatureVisitor {

    private final static Logger logger = LoggerFactory.getLogger(CollectParameterTypesVisitor.class);

    private final Set<Type> classes = new LinkedHashSet<>();

    private final String className;

    private boolean topLevel = true;

    public Set<Type> getClasses() {
        return classes;
    }

    /**
     * @param className
     */
    public CollectParameterTypesVisitor(String className) {
        super(Opcodes.ASM9);
        this.className = className;
        logger.debug("Target class name: " + className);
    }

    @Override
    public void visitFormalTypeParameter(String name) {
        logger.debug("  visitFormalTypeParameter(" + name + ")");
    }

    @Override
    public SignatureVisitor visitClassBound() {
        logger.debug("  visitClassBound()");
        return this;
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        logger.debug("  visitInterfaceBound()");
        topLevel = false;
        return this;
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        logger.debug("  visitSuperclass()");
        topLevel = true;
        return this;
    }

    @Override
    public SignatureVisitor visitInterface() {
        logger.debug("  visitInterface()");
        topLevel = true;
        return this;
    }

    @Override
    public SignatureVisitor visitParameterType() {
        logger.debug("  visitParameterType()");
        topLevel = true;
        return this;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.signature.SignatureVisitor#visitClassType(java.lang.String)
     */
    @Override
    public void visitClassType(String name) {
        logger.debug("  visitClassType(" + name + ")");

        if (topLevel)
            topLevel = false;
        else if (!name.equals(className))
            classes.add(Type.getObjectType(name));

        super.visitClassType(name);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.signature.SignatureVisitor#visitTypeVariable(java.lang.String)
     */
    @Override
    public void visitTypeVariable(String name) {
        logger.debug("  visitTypeVariable(" + name + ")");

        super.visitTypeVariable(name);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.signature.SignatureVisitor#visitTypeArgument()
     */
    @Override
    public void visitTypeArgument() {
        logger.debug("  visitTypeArgument");
        super.visitTypeArgument();
    }

    @Override
    public SignatureVisitor visitReturnType() {
        logger.debug("  visitReturnType");
        topLevel = true;
        return super.visitReturnType();
    }
}
