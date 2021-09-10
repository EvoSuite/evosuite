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

import org.objectweb.asm.Type;

/**
 * @author Gordon Fraser
 */
public interface ConstantPool {

    /**
     * <p>
     * getRandomString
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getRandomString();

    /**
     * <p>
     * getRandomType
     * </p>
     *
     * @return a {@link org.objectweb.asm.Type} object.
     */
    Type getRandomType();

    /**
     * <p>
     * getRandomInt
     * </p>
     *
     * @return a int.
     */
    int getRandomInt();

    /**
     * <p>
     * getRandomFloat
     * </p>
     *
     * @return a float.
     */
    float getRandomFloat();

    /**
     * <p>
     * getRandomDouble
     * </p>
     *
     * @return a double.
     */
    double getRandomDouble();

    /**
     * <p>
     * getRandomLong
     * </p>
     *
     * @return a long.
     */
    long getRandomLong();

    /**
     * <p>
     * add
     * </p>
     *
     * @param object a {@link java.lang.Object} object.
     */
    void add(Object object);

    String toString();
}
