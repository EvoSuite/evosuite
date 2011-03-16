package amis;

/**
 * @author ida
 *
 */
public class SimpleTestClass {

	private int field;
	private boolean flag = false;
	
	public SimpleTestClass(int anInt) {
		field = anInt;
	}
	
//	public void setField(int val) {
//		field = val;
//	}
//	
//	public int getField() {
//		return field;
//	}
	
	public int aMethod(int val) {
		if(flag)
			return field;
		flag = true;
		int i = val;
		if(i % 2 == 0)
			field = val;
		i=field;
		return i;
	}
//	
//	public void defThenUseThenDefAgain(int val) {
//		field = val;
//		if(field % 13 == 0)
//			field = 1;
//	}
//	
//	public void useThenDef(int val) {
//		if(field % 13 == 0)
//			field = val;
//	}
	
}
