package de.unisb.cs.st.evosuite.javaagent;


public class ClassLoaderTestSubject {

	public void assess(Integer x) {
		if (x > 10) {
			System.out.println("x was pretty big!");
		} else if (x < 0) {
			System.out.println("x was negative!");
		} else {
			System.out.println("x was normal.");
		}
	}

	public void trySomethingElse() {
		DependentClassLoaderTestSubject dependentSubject = new DependentClassLoaderTestSubject(5);
		dependentSubject.doSomethingNow(4);
	}
}
