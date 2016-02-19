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
package org.evosuite.testcase.statements.environment;

import org.evosuite.runtime.testdata.*;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.utils.Randomness;

/**
 * @see org.evosuite.runtime.testdata.EnvironmentDataList
 *
 * Created by arcuri on 12/11/14.
 */
public class EnvironmentStatements {

    public static boolean isEnvironmentData(Class<?> clazz){
        for(Class<?> env : EnvironmentDataList.getListOfClasses()){
            if(clazz.equals(env)){
                return true;
            }
        }
        return false;
    }

    public static PrimitiveStatement<?> getStatement(Class<?> clazz, TestCase tc) throws IllegalArgumentException{
        if(!isEnvironmentData(clazz)){
            throw new IllegalArgumentException("Class "+clazz.getName()+" is not an environment data type");
        }

        if(clazz.equals(EvoSuiteFile.class)){
            return new FileNamePrimitiveStatement(tc, new EvoSuiteFile(Randomness.choice(tc.getAccessedEnvironment().getViewOfAccessedFiles())));
        } else if(clazz.equals(EvoSuiteLocalAddress.class)){
            return new LocalAddressPrimitiveStatement(tc);
        } else if(clazz.equals(EvoSuiteRemoteAddress.class)){
            return new RemoteAddressPrimitiveStatement(tc);
        } else if(clazz.equals(EvoSuiteURL.class)){
            return new UrlPrimitiveStatement(tc);
        } else if(clazz.equals(EvoName.class)){
            return new NamePrimitiveStatement(tc);
        }

        throw new RuntimeException("EvoSuite bug: unhandled class "+clazz.getName());
    }
}
