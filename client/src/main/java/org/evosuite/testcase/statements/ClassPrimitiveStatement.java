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
package org.evosuite.testcase.statements;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.testcase.TestCase;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ClassPrimitiveStatement extends PrimitiveStatement<Class<?>> {

    private static final long serialVersionUID = -2728777640255424791L;

    private transient Set<Class<?>> assignableClasses = new LinkedHashSet<>();

    public ClassPrimitiveStatement(TestCase tc, GenericClass<?> type,
                                   Set<Class<?>> assignableClasses) {
        super(tc, type, Randomness.choice(assignableClasses));
        this.assignableClasses.addAll(assignableClasses);
    }

    public ClassPrimitiveStatement(TestCase tc, Class<?> value) {
        //		super(tc, new GenericClass(Class.class).getWithWildcardTypes(), value);
        super(
                tc,
                GenericClassFactory.get(Class.class).getWithParameterTypes(new Type[]{value}),
                value);
        //		super(tc, new GenericClass(value.getClass()), value);
        this.assignableClasses.add(value);
    }

    public ClassPrimitiveStatement(TestCase tc) {
        //		super(tc, new GenericClass(Class.class).getWithWildcardTypes(),
        super(
                tc,
                GenericClassFactory.get(Class.class).getWithParameterTypes(new Type[]{Properties.getTargetClassAndDontInitialise()}),
                Properties.getTargetClassAndDontInitialise());
        //		super(tc, new GenericClass(Properties.getTargetClass()),
        //		        Properties.getTargetClass());
    }

    @Override
    public boolean hasMoreThanOneValue() {
        return assignableClasses.size() != 1;
    }

    @Override
    public void delta() {
        randomize();
    }

    @Override
    public void zero() {
        this.value = Properties.getTargetClassAndDontInitialise();
    }

    private Class<?> getType(org.objectweb.asm.Type type) throws ClassNotFoundException {
        // Not quite sure why we have to treat primitives explicitly...
        switch (type.getSort()) {
            case org.objectweb.asm.Type.ARRAY:
                org.objectweb.asm.Type componentType = type.getElementType();
                Class<?> componentClass = getType(componentType);
                Class<?> arrayClass = Array.newInstance(componentClass, 0).getClass();
                return arrayClass;
            case org.objectweb.asm.Type.BOOLEAN:
                return boolean.class;
            case org.objectweb.asm.Type.BYTE:
                return byte.class;
            case org.objectweb.asm.Type.CHAR:
                return char.class;
            case org.objectweb.asm.Type.DOUBLE:
                return double.class;
            case org.objectweb.asm.Type.FLOAT:
                return float.class;
            case org.objectweb.asm.Type.INT:
                return int.class;
            case org.objectweb.asm.Type.LONG:
                return long.class;
            case org.objectweb.asm.Type.SHORT:
                return short.class;
            default:
                return Class.forName(type.getClassName(), true,
                        TestGenerationContext.getInstance().getClassLoaderForSUT());
        }
    }

    @Override
    public void randomize() {
        if (!assignableClasses.isEmpty()) {
            value = Randomness.choice(assignableClasses);
        } else {
            org.objectweb.asm.Type type = ConstantPoolManager.getInstance().getConstantPool().getRandomType();
            try {
                value = getType(type);
            } catch (ClassNotFoundException | NoClassDefFoundError | ExceptionInInitializerError e) {
                logger.warn("Error loading class " + type.getClassName() + ": " + e);
            }
        }
    }

    @Override
    public void changeClassLoader(ClassLoader loader) {
        super.changeClassLoader(loader);
        GenericClass<?> genericClass = GenericClassFactory.get(value);
        genericClass.changeClassLoader(loader);
        value = genericClass.getRawClass();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(GenericClassFactory.get(value));
        List<GenericClass<?>> currentAssignableClasses = new ArrayList<>();
        for (Class<?> assignableClass : assignableClasses)
            currentAssignableClasses.add(GenericClassFactory.get(assignableClass));
        oos.writeObject(currentAssignableClasses);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
            IOException {
        ois.defaultReadObject();
        this.value = ((GenericClass<?>) ois.readObject()).getRawClass();
        List<GenericClass<?>> newAssignableClasses = (List<GenericClass<?>>) ois.readObject();
        assignableClasses = new LinkedHashSet<>();
        for (GenericClass<?> assignableClass : newAssignableClasses) {
            assignableClasses.add(assignableClass.getRawClass());
        }
    }
}
