package de.unisb.cs.st.evosuite.utils;

import java.util.*;

public abstract class ListUtil {
	public static <T> List<T> tail(List<T> list) {
		return list.subList(1, list.size());
	}

	public static <T> boolean anyEquals(List<T> list, T obj) {
		for (T item : list) {
			if (item.equals(obj)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static <T> List<T> shuffledList(List<T> list) {
		ArrayList<T> result = new ArrayList<T>(list);
		Collections.shuffle(result);
		return result;
	}

	public static <T> List<T> shuffledList(List<T> list, Random rnd) {
		ArrayList<T> result = new ArrayList<T>(list);
		Collections.shuffle(result, rnd);
		return result;
	}
}
