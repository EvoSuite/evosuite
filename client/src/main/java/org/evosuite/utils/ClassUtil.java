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

import org.evosuite.runtime.classhandling.ClassResetter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

/**
 * Class utils related class
 *
 * @author ilebrero
 */
public class ClassUtil {

    /**
     * Returns a set with the static methods of a class
     *
     * @param targetClass a class instance
     * @return
     */
    public static List<Method> getTargetClassStaticMethods(Class<?> targetClass) {
        Method[] declaredMethods = targetClass.getDeclaredMethods();
        List<Method> targetStaticMethods = new LinkedList<>();
        for (Method m : declaredMethods) {

            if (!Modifier.isStatic(m.getModifiers())) {
                continue;
            }

            if (Modifier.isPrivate(m.getModifiers())) {
                continue;
            }

            if (m.getName().equals(ClassResetter.STATIC_RESET)) {
                continue;
            }

            targetStaticMethods.add(m);
        }
        return targetStaticMethods;
    }

}
