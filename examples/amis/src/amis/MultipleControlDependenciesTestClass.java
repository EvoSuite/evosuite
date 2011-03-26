package amis;

/**
 * @author Andre Mis
 *
 */
public class MultipleControlDependenciesTestClass {

	private int field;
	
	public MultipleControlDependenciesTestClass(int anInt) {
		field = anInt;
	}
	
	public void setField(int val) {
		field = val;
	}
	
	public void test() {
		boolean a = field % 2 == 0;
		boolean b = field % 3 == 0;
		// TODO:
		// so field++ is control dependent from both a and b
		// so when calculating the branch fitness should take
		// minimum over branch fitness of a and branch fitness of b!
		if(a || b)
			field++;
	}
}
