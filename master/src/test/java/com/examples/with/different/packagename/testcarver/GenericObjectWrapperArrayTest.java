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

package com.examples.with.different.packagename.testcarver;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gordon Fraser
 */
public class GenericObjectWrapperArrayTest {
    @Test
    public void test() {
        GenericObjectWrapper<Long[]> wrapper = new GenericObjectWrapper<>();
        Assert.assertNull(wrapper.get());

        Long[] someArray = new Long[]{1L, 47L};

        wrapper.set(someArray);
        Assert.assertNotNull(wrapper.get());

        GenericObjectWrapper<Long> fortySeven = new GenericObjectWrapper<>();
        fortySeven.set(47L);

        Long[] foo = wrapper.get();

        Assert.assertEquals(foo[1], fortySeven.get());
    }
}
