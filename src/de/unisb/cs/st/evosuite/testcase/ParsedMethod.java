package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Method;

public class ParsedMethod {

	public void execute(String name) {
		try {
			for(Method method : this.getClass().getMethods()) {
				if(method.getName().equals(name))
					method.invoke(this);
			}
        } catch(Throwable e) {
            System.out.println("Exception!");
            System.out.println(e);
        }
	}
	
}
