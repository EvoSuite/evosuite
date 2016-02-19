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
package org.evosuite.runtime.mock;

import java.lang.annotation.*;

/**
 * This @tag is used for methods in the OverrideMocks that
 * cannot be handled with inheritance.
 * A typical example is "final" methods.
 *
 * <p>
 *     These methods will be handled like they were static
 *     replacement ones: ie, same method name, but static, and
 *     with class instance as first input parameter
 *
 * <p>
 * Note: such tag cannot be used in unspecialized EvoSuiteMocks,
 * eg when one wants to mock one single method instead of using
 * a full-blown StaticReplacementMock.
 * Why?
 * First, we want to be sure that we mocked all methods (eg  to check
 * if by error we missed one, or new Java version introduces new
 * ones).
 * Second, a mock class could be used directly in the test as input data,
 * and so its full signature should be available (as the mocked class would
 * not be accessible)
 *
 * <p>
 * WARNING: should only be used on mocking _static_ methods
 *
 * Created by arcuri on 9/25/14.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)  //TODO is there a way to specify only static ones?
public @interface StaticReplacementMethod {
}
