package org.evosuite.mock.java.net;

import com.examples.with.different.packagename.mock.java.net.ReceiveUdp;
import org.evosuite.runtime.testdata.EvoSuiteLocalAddress;
import org.evosuite.runtime.testdata.NetworkHandling;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by arcuri on 12/17/14.
 */
public class GeneratedForReceiveUdpTest {

    @Test
    public void testThrowException() throws Exception{
        EvoSuiteLocalAddress evoSuiteLocalAddress0 = null;
        byte[] byteArray0 = new byte[8];
        boolean boolean0 = NetworkHandling.sendUdpPacket(evoSuiteLocalAddress0, byteArray0);
        Assert.assertFalse(boolean0); //the addr was null

        ReceiveUdp receiveUdp0 = new ReceiveUdp();

        try {
            boolean boolean1 = receiveUdp0.listenForUdpPacket();
            Assert.fail(); //no message on buffer, so listening should throw exception
        } catch (Exception e){
            //expected
        }
    }
}
