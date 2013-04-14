package com.examples.with.different.packagename.concolic;

public class MyLinkedList {

	private static class Node {
		Node next;
		Node previous;
		Object value;
	}

	private Node header = null;

	private int size;

	public MyLinkedList() {
		Node new_header = new Node();
		new_header.previous = new_header;
		new_header.next = new_header;
		header = new_header;
		size = 0;
	}

	public void add(Object value) {
		Node new_node = new Node();
		new_node.value = value;
		new_node.previous = header;
		new_node.next = header.next;

		header.next.previous = new_node;
		header.next = new_node;

		size++;

	}

	public Object get(int index) {
		if (index >= size)
			throw new IllegalArgumentException();

		Node iterator = header.next;
		for (int i = 0; i < index; i++) {
			iterator = iterator.next;
		}

		return iterator.value;

	}

	public int size() {
		return size;
	}

	public void unreacheable() {
		if (this.header == null) {
			// unreachable branch
			size = -1;
		}
	}

}
