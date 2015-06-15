package org.evosuite.testcase.statements.environment;

import org.evosuite.runtime.testdata.EvoSuiteAddress;
import org.evosuite.runtime.testdata.EvoSuiteRemoteAddress;
import org.evosuite.runtime.vnet.EndPointInfo;
import org.evosuite.seeding.ConstantPool;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.StringUtil;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * Created by arcuri on 12/17/14.
 */
public class RemoteAddressPrimitiveStatement extends EnvironmentDataStatement<EvoSuiteRemoteAddress>{

	private static final long serialVersionUID = -4863601663573415059L;

	public RemoteAddressPrimitiveStatement(TestCase tc) {
        this(tc, null);
        randomize();
    }

    public RemoteAddressPrimitiveStatement(TestCase tc, EvoSuiteRemoteAddress value) {
        super(tc, EvoSuiteRemoteAddress.class, value);
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
        EvoSuiteRemoteAddress addr;

        double threshold = 0.8; //TODO parameter
        boolean accessed = Randomness.nextDouble() <= threshold;

        if(accessed && !tc.getAccessedEnvironment().getViewOfRemoteContactedPorts().isEmpty()){
            // use an address that the SUT tried to contact
            EndPointInfo info = Randomness.choice(tc.getAccessedEnvironment().getViewOfRemoteContactedPorts());
            String host = info.getHost();
            int port = info.getPort();//TODO check why it can be a 0 here
            port = getPort(port);
            addr = new EvoSuiteRemoteAddress(host,port);
        } else {
            /*
                make up an address based on string/int constants.
                this is needed to handle the cases when the SUT get
                an incoming message, and then check its remote address.

                TODO: here we could validate if host/port values are
                indeed valid. However, as this is kind of special case,
                and likely not so common, it doesn't have high priority.
             */
            ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();
            String host = constantPool.getRandomString();
            int port = constantPool.getRandomInt();
            port = getPort(port);
            addr = new EvoSuiteRemoteAddress(host,port);
        }

        setValue(addr);
    }

    private int getPort(int port) {
        if(port<=0 || port > 65535){
            port = 12345; //just a valid port number
        }
        return port;
    }

}
