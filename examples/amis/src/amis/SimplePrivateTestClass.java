package amis;

/**
 * @author Andre Mis
 *
 */
public class SimplePrivateTestClass {

	private int field;
	private boolean flag = false;
	
	public SimplePrivateTestClass(int anInt) {
		field = anInt;
	}
	
	public void callSetField(int val) {
		setField(val);
	}
	
	public int callGetField() {
		return getField();
	}
	
	public void callSimpleMean(int param) {
		simpleMean(param);
	}
	
	public int callAMethod(int val) {
		return aMethod(val);
	}
	
	public void callDTUTD(int val) {
		defThenUseThenDefAgain(val);
	}
	
	public void callUseThenDef(int val) {
		useThenDef(val);
	}
	
	private void setField(int val) {
		field = val;
	}
	
	private int getField() {
		return field;
	}
	
	private void simpleMean(int param) {
		if(param % 3 == 0)
			field=1;
		else if(param % 2 != 0)
			field=2;
		
		if(field == 0) // target use
			field = 3;
	}
	
	private int aMethod(int val) {
		if(flag)
			return field;
		flag = true;
		int i = val;
		if(i % 2 == 0)
			field = val;
		i=field;
		return i;
	}

	private void defThenUseThenDefAgain(int val) {
		field = val;
		if(field % 13 == 0)
			field = 1;
	}
	
	private void useThenDef(int val) {
		if(field % 13 == 0)
			field = val;
	}
	
}
