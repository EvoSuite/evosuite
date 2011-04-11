package amis;

/**
 * @author Andre Mis
 *
 */
public class SimpleTestClass {

	private int field;
	private boolean flag = false;
	
	public SimpleTestClass(int anInt) {
		field = anInt;
	}
	
	public void setField(int val) {
		field = val;
	}
	
	public int getField() {
		return field;
	}
	
	public void simpleMean(int param) {
		if(param % 3 == 0)
			field=1;
		else if(param % 2 != 0)
			field=2;
		
		if(field == 0) // target use
			field = 3;
	}
	
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

	public void defThenUseThenDefAgain(int val) {
		field = val;
		if(field % 13 == 0)
			field = 1;
	}
	
	public void useThenDef(int val) {
		if(field % 13 == 0)
			field = val;
	}
	
}
