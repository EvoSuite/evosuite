package org.evosuite.runtime.testdata;

/**
 * Created by arcuri on 12/15/14.
 */
public class EvoSuiteLocalAddress extends EvoSuiteAddress {

	private static final long serialVersionUID = -6083183350694322155L;

	public EvoSuiteLocalAddress(String host, int port) throws IllegalArgumentException {
        super(host,port);
    }

}
