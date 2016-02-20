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
package org.evosuite.runtime;

import org.evosuite.runtime.FalsePositiveException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


/**
 * If a new method is called on mocked object that is different from what was used
 * when the test was generated, then ignore the test, as likely it will be a false positive
 *
 */
public class ViolatedAssumptionAnswer implements Answer<Object> {

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {

        if(invocation.getMethod().getReturnType().equals(Void.TYPE)) {
            //no need of exception, as no return value will be used in the CUT anyway which could affect the test
            return null;
        } else {
            throw new FalsePositiveException("Mock call to "+invocation.getMethod().getName()+
                    " which was not presented when the test was generated");
        }
    }
}
