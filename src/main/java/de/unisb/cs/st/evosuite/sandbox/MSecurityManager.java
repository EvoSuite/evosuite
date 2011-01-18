/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.sandbox;

import java.io.FilePermission;
import java.net.SocketPermission;
import java.security.Permission;
import java.net.NetPermission;

import de.unisb.cs.st.evosuite.Properties;;

/**
 * Mocked Security Manager, which forbids any access to I/O, network, etc.
 * 
 * @author Andrey Tarasevich
 *
 */
public class MSecurityManager extends SecurityManager 
{
	/** package under test */
	private String testPackage = Properties.PROJECT_PREFIX;

	/**
	 * Overridden method for checking permissions for any operation.
	 */
    @Override
    public void checkPermission(Permission perm) 
    {
    	// check access  
    	if(checkCallingMethod(perm))
    		throw new SecurityException("Security manager blocks all he can block. You've got served!");
    	return;
    }
    
    /**
     * Method for checking if requested access, specified by the given permission, 
     * is not permitted.  
     * 
     * @param perm  permission for which the security manager is asked 
     * @return
     * 		true if permission was asked by class under test,
     * 		false otherwise
     */
    @SuppressWarnings("unused")
	private boolean checkCallingMethod(Permission perm){
    	
    	// get all elements of the stack trace for the current thread 
    	StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
    	
    	// false if "executeTestCase" method wasn't in a stack trace, true otherwise
    	boolean testExec = false;
    	
    	// iterate through all elements and check if name of the calling class contains 
    	// the name of the class under test. Also keep track of the "executeTestCase" method 
    	// call.
    	for(StackTraceElement e : stackTraceElements){
    		if(e.getMethodName().contains("executeTestCase"))
    			testExec = true;
    		if(e.getClassName().contains(testPackage)){
    			return true;
    		}
    	}
    	
    	// if permission wasn't asked by class under test and "executeTestCase" method was 
    	// in a stack trace, then there should be a check for I/O and network permissions
    	if(testExec){
    		
    		// check for java.io.FilePermission
	    	try{
	    		 FilePermission fp = (FilePermission)perm;
	    		 
	    		 // if cast was successful, then forbid access
	    		 return true;
	    	}catch(Exception e){
	    		// do nothing
	    	}
	    	
	    	// check for java.net.SocketPermission
	    	try{
	    		 SocketPermission sp = (SocketPermission)perm;
	    		 
	    		 // if cast was successful, then forbid access
	    		 return true;
	    	}catch(Exception e){
	    		// do nothing
	    	}
	    	
	    	// check for java.net.NetPermission
	    	try{
	    		 NetPermission np = (NetPermission)perm;
	    		 
	    		 // if cast was successful, then forbid access
	    		 return true;
	    	}catch(Exception e){
	    		// do nothing
	    	}
	    	
	    	// check RuntimePermission for IO
	    	if(perm.getActions().contains("writeFileDescriptor") || 
	    	   perm.getActions().contains("readFileDescriptor"))
	    		return true;
    	}
    	
    	return false;
    }
}
