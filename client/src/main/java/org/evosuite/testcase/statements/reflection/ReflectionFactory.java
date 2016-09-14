/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.testcase.statements.reflection;

import org.evosuite.Properties;
import org.evosuite.runtime.Reflection;
import org.evosuite.runtime.javaee.injection.Injector;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.utils.Randomness;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Class used to get private fields/methods to construct statements in the generated tests
 *
 * Created by Andrea Arcuri on 22/02/15.
 */
public class ReflectionFactory {

    private final Class<?> target;
    private final List<Field> fields;
    private final List<Method> methods;


    public ReflectionFactory(Class<?> target) throws IllegalArgumentException{
        this.target = target;
        if(target==null){
            throw new IllegalArgumentException("Target class cannot be null");
        }

        fields = new ArrayList<>();
        methods = new ArrayList<>();

        for(Method m : target.getDeclaredMethods()){
            if(Modifier.isPrivate(m.getModifiers()) && !m.isBridge() && !m.isSynthetic()){
                //only interested in private methods, as the others can be called directly
                methods.add(m);
            }
        }

        //do not use reflection on JEE injection
        List<Field> toSkip = null;
        if(Properties.JEE){
            toSkip = Injector.getAllFieldsToInject(target);
        }

        for(Field f : target.getDeclaredFields()){
            if(Modifier.isPrivate(f.getModifiers())
                    && !f.isSynthetic()
                    && (toSkip==null || ! toSkip.contains(f))
                    && !f.getName().equals("serialVersionUID")
                    // read/writeObject must not be invoked directly, otherwise it raises a java.io.NotActiveException
                    && !f.getName().equals("writeObject")
                    && !f.getName().equals("readObject")
                    // final primitives cannot be changed
                    && !(Modifier.isFinal(f.getModifiers()) && f.getType().isPrimitive())
                    // changing final strings also doesn't make much sense
                    && !(Modifier.isFinal(f.getModifiers()) && f.getType().equals(String.class))
                    //static fields lead to just too many problems... although this could be set as a parameter
                    && !Modifier.isStatic(f.getModifiers())
                    ) {
                fields.add(f);
            }
        }
    }

    public int getNumberOfUsableFields(){
        return fields.size();
    }

    public boolean hasPrivateFieldsOrMethods(){
        return  !(fields.isEmpty() && methods.isEmpty());
    }

    public boolean nextUseField(){
        if(fields.isEmpty()){
            return false;
        }
        if(methods.isEmpty()){
            assert !fields.isEmpty();
            return true;
        }

        assert !fields.isEmpty() && !methods.isEmpty();

        int tot = fields.size() + methods.size();
        double ratio = (double)fields.size() / (double) tot;

        return Randomness.nextDouble() <= ratio;
    }

    public Field nextField() throws IllegalStateException{
        if(fields.isEmpty()){
            throw new IllegalStateException("No private field");
        }
        return Randomness.choice(fields);
    }

    public Method nextMethod()  throws IllegalStateException{
        if(methods.isEmpty()){
            throw new IllegalStateException("No private method");
        }
        return Randomness.choice(methods);
    }

    public Class<?> getReflectedClass(){
        return target;
    }
}

