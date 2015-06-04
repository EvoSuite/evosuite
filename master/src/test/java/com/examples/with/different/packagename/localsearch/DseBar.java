package com.examples.with.different.packagename.localsearch;

public class DseBar {

	private String x;

	public DseBar(String x) {
		this.x = x;
	}

	public void coverMe(DseFoo f) {
		String y = x + f.getX();
		if (f.getX() == 5) {
			if (y.equals("baz5")) {
				System.out.println("TARGET");
			}
		}
	}
	
	public static void main(String[] args) {
		test1();
		test2();
	}
	
	public static void test1() {
		String string0 = "baz5";
		DseBar dseBar0 = new DseBar(string0);
		DseFoo dseFoo0 = new DseFoo();
		dseBar0.coverMe(dseFoo0);
	}

	public static void test2() {
		String string0 = null;
		DseBar dseBar0 = new DseBar(string0);
		DseFoo dseFoo0 = new DseFoo();
		dseFoo0.inc();
		dseFoo0.inc();
		dseFoo0.inc();
		dseFoo0.inc();
		dseFoo0.inc();
		dseBar0.coverMe(dseFoo0);
	}
}
