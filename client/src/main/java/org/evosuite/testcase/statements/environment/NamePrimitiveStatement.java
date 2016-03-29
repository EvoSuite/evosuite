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

import org.evosuite.runtime.javaee.JeeData;
import org.evosuite.runtime.testdata.EvoName;
import org.evosuite.runtime.testdata.EvoSuiteURL;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.StringUtil;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.reflect.Type;

/**
 * Created by Andrea Arcuri on 06/12/15.
 */
public class NamePrimitiveStatement extends EnvironmentDataStatement<EvoName>{


    public NamePrimitiveStatement(TestCase tc) {
        this(tc,null);
        randomize();
    }

    public NamePrimitiveStatement(TestCase tc, EvoName value) {
        super(tc, EvoName.class, value);
    }

    @Override
    public String getTestCode(String varName) {
        String testCode = "";
        VariableReference retval = getReturnValue();
        Object value = getValue();

        if (value != null) {
            String name = ((EvoName) value).getName();
            testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
                    + varName + " = new "
                    + ((Class<?>) retval.getType()).getSimpleName() + "(\""
                    + name + "\");\n";
        } else {
            testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
                    + varName + " = null;\n";
        }
        return testCode;
    }



    @Override
    public void delta() {

    }

    @Override
    public void zero() {

    }

    @Override
    public void randomize() {

        JeeData data = tc.getAccessedEnvironment().getJeeData();
        if(data == null){
            setValue(null);
            return;
        }
        String name = Randomness.choice(data.lookedUpContextNames);
        if(name != null){
            setValue(new EvoName(name));
        } else {
            setValue(null);
        }
    }
}
