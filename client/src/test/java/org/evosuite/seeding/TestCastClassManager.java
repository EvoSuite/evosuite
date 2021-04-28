/**
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

import ch.qos.logback.classic.Level;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassFactory;
import org.junit.Test;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;


public class TestCastClassManager {

    @Test
    public void testSelectClass(){
        GenericClass<?> intClass = GenericClassFactory.get(int.class);
        List<GenericClass<?>> ts = Collections.singletonList(intClass);
        GenericClass<?> genericClass = CastClassManager.selectClass(ts);
        assertThat(genericClass, equalTo(intClass));
    }

    @Test
    public void testAddCastClass(){
        CastClassManager instance = CastClassManager.getInstance();
        instance.addCastClass("java.lang.Integer", 5);
        Set<GenericClass<?>> castClasses = instance.getCastClasses();
        assertThat(castClasses, hasItem(GenericClassFactory.get(Integer.class)));

        instance.clear();
        Type t = Integer.class;
        instance.addCastClass(t, 5);
        castClasses = instance.getCastClasses();
        assertThat(castClasses, hasItem(GenericClassFactory.get(Integer.class)));

        instance.clear();
        GenericClass<?> gc = GenericClassFactory.get(t);
        instance.addCastClass(gc, 5);
        castClasses = instance.getCastClasses();
        assertThat(castClasses, hasItem(GenericClassFactory.get(Integer.class)));
    }
}