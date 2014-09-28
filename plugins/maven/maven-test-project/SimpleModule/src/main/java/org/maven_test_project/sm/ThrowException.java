package org.maven_test_project.sm;

import org.evosuite.runtime.mock.MockFramework;

public class ThrowException{

    static{
        System.out.println("Calling static initialazer of ThrowException");       
    }

	public void foo(){
		throw new IllegalArgumentException("This should get mocked");
	}
	
}