package com.examples.with.different.packagename.generic.concurrent;

public interface BlockingQueue<E> {

	public boolean add(E e);
	
	public E take();
}
