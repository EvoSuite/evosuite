package org.evosuite.utils;

import java.util.Collection;
import java.util.Map;

public final class CollectionUtil 
{
	private CollectionUtil() {}
	
	public static boolean isNullOrEmpty(final Collection<?> c)
	{
		return c == null || c.isEmpty();
	}
	
	public static boolean isNullOrEmpty(final Map<?,?> m)
	{
		return m == null || m.isEmpty();
	}
}
