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

package de.unisb.cs.st.sandbox;

import java.security.Permission;
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
    	// check if access was asked for class under test
    	// If permission is asked for class under test, then throw an exception 
    	if(checkCallingMethod())
    		throw new SecurityException("Security manager blocks all he can block. You've got served!");
    	
    	return;
    }
    
    /**
     * Method for checking if permission was asked by class under test.
     * 
     * @return
     * 		true if permission was asked by class under test,
     * 		false otherwise
     */
    private boolean checkCallingMethod(){
    	// get all elements of the stack trace for the current thread 
    	StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
    	
    	// iterate through all elements and check if name of the calling class contains 
    	// the name of the class under test.  
    	for(StackTraceElement e : stackTraceElements)
    		if(e.getClassName().contains(testPackage))
    			return true;
    	
    	return false;
    }
}
