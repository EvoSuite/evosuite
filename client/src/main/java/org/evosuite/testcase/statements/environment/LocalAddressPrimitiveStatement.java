package org.evosuite.testcase.statements.environment;

import org.evosuite.runtime.testdata.EvoSuiteAddress;
import org.evosuite.runtime.testdata.EvoSuiteLocalAddress;
import org.evosuite.runtime.vnet.EndPointInfo;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.StringUtil;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * Created by arcuri on 12/15/14.
 */
public class LocalAddressPrimitiveStatement extends EnvironmentDataStatement<EvoSuiteLocalAddress> {

    public LocalAddressPrimitiveStatement(TestCase tc) {
        this(tc,null);
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
                    + escapedAddress +"\", "+port + ");\n";
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
    protected void pushBytecode(GeneratorAdapter mg) {

    }

    @Override
    public void randomize() {

        EvoSuiteLocalAddress addr;

        if(!tc.getAccessedEnvironment().getViewOfLocalListeningPorts().isEmpty()){
            EndPointInfo info = Randomness.choice(tc.getAccessedEnvironment().getViewOfLocalListeningPorts());
            String host = info.getHost();
            int port = info.getPort();
            addr = new EvoSuiteLocalAddress(host,port);
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
