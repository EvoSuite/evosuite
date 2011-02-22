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

import java.security.Permission;

import de.unisb.cs.st.evosuite.Properties;

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

	/** indicates if mocks are enabled */
	private boolean mocksEnabled = Properties.MOCKS;
	
	/**
	 * Overridden method for checking permissions for any operation.
	 */
    @Override
    public void checkPermission(Permission perm) 
    {
    	// check access  
    	if(!allowPermission(perm))
    		throw new SecurityException("Security manager blocks all he can block. You've got served!");
    	return;
    }
    
    /**
     * Method for checking if requested access, specified by the given permission, 
     * is permitted.  
     * 
     * @param perm  permission for which the security manager is asked 
     * @return
     * 		false if access is forbidden,
     * 		true otherwise
     */
	private boolean allowPermission(Permission perm){
    	
    	// get all elements of the stack trace for the current thread 
    	StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
    	
    	// false if "executeTestCase" method wasn't in a stack trace, true otherwise
    	boolean testExec = false;
    	
    	// iterate through all elements and check if name of the calling class contains 
    	// the name of the class under test or "executeTestCase" method call.
    	// Also check for few special cases, when permission should be granted
    	for(int elementCounter = 0; elementCounter < stackTraceElements.length;elementCounter++){
    		StackTraceElement e = stackTraceElements[elementCounter];
    		if(e.getMethodName().equals("executeTestCase") 
    				|| e.getClassName().contains(testPackage)){
    			testExec = true;
    			break;
    		}

    		if(e.getMethodName().equals("setSecurityManager"))
    			if(stackTraceElements[elementCounter+1].getMethodName().equals("executeTestCase"))
    				return true;
    		
    		if(e.getMethodName().equals("setOut") || e.getMethodName().equals("setErr"))
    			if(stackTraceElements[elementCounter+1].getMethodName().equals("execute"))
    				return true;
    		if(e.getClassName().contains("MockingBridge") && this.mocksEnabled)
    			return true;
    	}
    	
    	// if permission was asked during test case execution, then check permission itself
    	if(testExec){
    		String permName = perm.getClass().getCanonicalName();
    		
    		// Check for allowed permissions.
    		// Done with chunk of ugly "if-case" code, since it switch statement does not
    		// support Strings as parameters. Doing it trough Enum is also not an option,
    		// since java cannot guarantee the unique values returned by hashCode() method.
    		if(permName.equals("java.lang.reflect.ReflectPermission"))
    			return true;
    		if(permName.equals("java.util.PropertyPermission"))
    			if(perm.getActions().equals("read"))
    				return true;
    		
    		//TODO: -------------------- NEED TO FIND BETTER SOLUTION ----------------------- 
    		// At the moment this is the only way to allow classes under test define and load 
    		// other classes, but the way it is done seriously damages security of the program.
    		//
    		// Oracle explains risks here
    		// http://download.oracle.com/javase/6/docs/technotes/guides/security/permissions.html
    		if(permName.equals("java.lang.RuntimePermission"))
    			if(perm.getName().equals("getClassLoader") 
    					|| perm.getName().equals("createClassLoader")
    					|| perm.getName().contains("accessClassInPackage"))
    				return true;
    		}
    	return false;
    }
}
