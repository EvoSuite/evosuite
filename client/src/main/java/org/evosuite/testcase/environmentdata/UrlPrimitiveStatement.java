package org.evosuite.testcase.environmentdata;


import org.evosuite.runtime.testdata.EvoSuiteURL;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.StringUtil;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * Created by arcuri on 12/14/14.
 */
public class UrlPrimitiveStatement extends EnvironmentDataStatement<EvoSuiteURL> {

    public UrlPrimitiveStatement(TestCase tc) {
        this(tc, null);
        randomize();
    }


    public UrlPrimitiveStatement(TestCase tc, EvoSuiteURL value) {
        super(tc, EvoSuiteURL.class, value);
    }

    @Override
    public String getTestCode(String varName) {
        String testCode = "";
        VariableReference retval = getReturnValue();
        Object value = getValue();

        if (value != null) {
            String escapedURL = StringUtil.getEscapedString(((EvoSuiteURL) value).getUrl());
            testCode += ((Class<?>) retval.getType()).getSimpleName() + " "
                    + varName + " = new "
                    + ((Class<?>) retval.getType()).getSimpleName() + "(\""
                    + escapedURL + "\");\n";
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
        String url = Randomness.choice(tc.getAccessedEnvironment().getViewOfRemoteURLs());
        if (url != null) {
            setValue(new EvoSuiteURL(url));
        } else {
            setValue(null);
        }
    }
}
