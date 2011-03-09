package amis;

/**
 * @author ida
 *
 */
public class SimpleTestClass {

	private int field;
	
	public SimpleTestClass(int anInt) {
		
		field = 2;
		
		if(anInt==13)
			field=anInt;
	}
	
	public void setField(int val) {
		if(val != 13)
			field = val;
		else
			field = val;
	}
	
	public int getField() {
		return field;
	}
	
}
