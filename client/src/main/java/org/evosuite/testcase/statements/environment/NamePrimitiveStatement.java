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
    protected void pushBytecode(GeneratorAdapter mg) {

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
