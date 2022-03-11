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
package org.evosuite.testcase.statements.environment;

import org.evosuite.runtime.testdata.EvoSuiteAddress;
import org.evosuite.runtime.testdata.EvoSuiteLocalAddress;
import org.evosuite.runtime.vnet.EndPointInfo;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.StringUtil;

/**
 * Created by arcuri on 12/15/14.
 */
public class LocalAddressPrimitiveStatement extends EnvironmentDataStatement<EvoSuiteLocalAddress> {

    private static final long serialVersionUID = -6687351650507282638L;

    public LocalAddressPrimitiveStatement(TestCase tc) {
        this(tc, null);
        randomize();
    }

    public LocalAddressPrimitiveStatement(TestCase tc, EvoSuiteLocalAddress value) {
        super(tc, EvoSuiteLocalAddress.class, value);
    }

    @Override
    public String getTestCode(String varName) {
        String testCode = "";
        VariableReference retval = getReturnValue();
        Object value = getValue();

        if (value != null) {
            String escapedAddress = StringUtil.getEscapedString(((EvoSuiteAddress) value).getHost());
            int port = ((EvoSuiteAddress) value).getPort();

            testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
                    + varName + " = new "
                    + ((Class<?>) retval.getType()).getSimpleName() + "(\""
                    + escapedAddress + "\", " + port + ");\n";
        } else {
            testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
                    + varName + " = null;\n";
        }
        return testCode;

    }

    @Override
    public void delta() {
        randomize();
    }

    @Override
    public void zero() {

    }

    @Override
    public void randomize() {

        EvoSuiteLocalAddress addr;

        if (!tc.getAccessedEnvironment().getViewOfLocalListeningPorts().isEmpty()) {
            EndPointInfo info = Randomness.choice(tc.getAccessedEnvironment().getViewOfLocalListeningPorts());
            String host = info.getHost();
            int port = info.getPort();
            addr = new EvoSuiteLocalAddress(host, port);
        } else {
            /*
                no point in creating local addresses that the SUT has
                never accessed
             */
            addr = null;
        }

        setValue(addr);
    }
}
