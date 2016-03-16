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

/**
 * This interface is used to specify that this class is a "override" mock, ie
 * a class that extends the mocked one, and does mocking by @Override the parent's methods.
 * This type of mocking might not be possible when mocked class is final, no accessible 
 * constructor, etc.
 * 
 * <p>
 * <b>IMPORTANT</b>: each OverrideMock implementation should handle rollback to non-mocked functionality.
 * Such check can be based on {@link MockFramework#isEnabled()}.
 * Automated rollback in the instrumentation itself cannot be done, as we cannot change the signature
 * of an instrumented class based on flag. Eg: "class Foo extends File" will be replaced by "class Foo extends MockFile",
 * and we cannot change it back afterwards.  
 * 
 * @author arcuri
 *
 */
public interface OverrideMock extends EvoSuiteMock{
}
