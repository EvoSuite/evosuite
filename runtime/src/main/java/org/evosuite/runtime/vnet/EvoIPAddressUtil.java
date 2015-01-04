package org.evosuite.runtime.vnet;


public class EvoIPAddressUtil {

	public static byte[]  textToNumericFormatV4(String host){
		//TODO byte[] addr = sun.net.util.IPAddressUtil.textToNumericFormatV4(host); //FIXME
		return sun.net.util.IPAddressUtil.textToNumericFormatV4(host);
	}
}
