/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.seeding;

import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericClassFactory;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;


public class TestCastClassManager {

    @Test
    public void testSelectClass() {
        GenericClass<?> intClass = GenericClassFactory.get(int.class);
        List<GenericClass<?>> ts = Collections.singletonList(intClass);
        GenericClass<?> genericClass = CastClassManager.selectClass(ts);
        assertThat(genericClass, equalTo(intClass));
    }

    @Test
    public void testAddCastClass() {
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