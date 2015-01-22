package org.evosuite.testcase.environmentdata;

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
        }

        throw new RuntimeException("EvoSuite bug: unhandled class "+clazz.getName());
    }
}
