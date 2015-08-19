/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.javaee.injection;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provide a list of JavaEE tags which we handle for dependency injection
 *
 * Created by Andrea Arcuri on 30/05/15.
 */
public class InjectionList {

    private static final List<Class<? extends Annotation>> list =
            Collections.unmodifiableList(Arrays.<Class<? extends Annotation>>asList(
        javax.inject.Inject.class,
        javax.persistence.PersistenceContext.class,
        javax.persistence.PersistenceUnit.class
    ));

    public static List<Class<? extends Annotation>> getList(){
        return list;
    }

}
