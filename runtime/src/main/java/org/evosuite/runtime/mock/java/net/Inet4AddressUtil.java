package org.evosuite.runtime.mock.java.net;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.net.Inet4Address;
import java.net.InetAddress;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class used to instantiate Inet4Address objects.
 * That class cannot be instantiated directly
 * 
 * @author arcuri
 *
 */
public class Inet4AddressUtil {

	private static final Logger logger = LoggerFactory.getLogger(Inet4AddressUtil.class);

	/*
	 * number of bytes in a IPv4 address
	 */
	public static final int INADDRSZ = 4;
	
	private static Constructor<Inet4Address> constructorStringByteArray;
	private static Constructor<Inet4Address> constructorStringInt;
	private static Field holderField;
	
	static{
		try {
			constructorStringByteArray = Inet4Address.class.getDeclaredConstructor(String.class, byte[].class);
			constructorStringByteArray.setAccessible(true);
			
			constructorStringInt = Inet4Address.class.getDeclaredConstructor(String.class, int.class);
			constructorStringInt.setAccessible(true);
			
			holderField = InetAddress.class.getDeclaredField("holder");
			holderField.setAccessible(true);
			
		} catch (NoSuchMethodException | SecurityException | NoSuchFieldException e) {
			logger.error("Failed to initialize due to reflection problems: "+e.getMessage());
		}
	}
	
	public static Inet4Address createNewInstance(){
		try {
			return Inet4Address.class.newInstance();			
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("Failed to create instance: "+e.getMessage());
		}
		return null; 
	}
	
	public static Inet4Address createNewInstance(String hostName, byte addr[]){
		try {
			return constructorStringByteArray.newInstance(hostName,addr);
		} catch ( SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Failed to create instance: "+e.getMessage());
		}
		return null;
	}
	
	public static Inet4Address createNewInstance(String hostName, int address){
		try {
			return constructorStringInt.newInstance(hostName,address);
		} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Failed to create instance: "+e.getMessage());
		}
		return null;
	}
			
}
