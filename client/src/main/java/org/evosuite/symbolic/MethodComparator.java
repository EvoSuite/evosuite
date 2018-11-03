package org.evosuite.symbolic;

import java.lang.reflect.Method;
import java.util.Comparator;

public class MethodComparator implements Comparator<Method> {

	@Override
	public int compare(Method left, Method right) {
		return left.toString().compareTo(right.toString());
	}

}
