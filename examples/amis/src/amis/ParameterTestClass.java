package amis;

/**
 * @author ida
 *
 */
public class ParameterTestClass {

	private int field;
	
	public ParameterTestClass(int anInt) {
		
		field = anInt;
	}
	
	public int aMethod(int param1, int param2, int param3) {
		
		if(field%2 == 0) {
			field = param1+param2;
		} else {
			field = param2*param3;
		}
		
		return param1+param2+param3 - field;
	}
	
	public void anotherMethod(int param1, int param2, String param3, String param4) {
		
		if(param3 == null || param4 == null)
			return;
		
		if(param3.length() != 0)
			field = param3.length();
		else if(param4.length() != 0)
			field = param4.length();
		else 
			field = param1+param2;
	}
	
	public void stringNullMethod(String aString) {
		if(aString == null) // TODO EvoSuite seems not to give null as an argument
			field = 0;
	}
	
	public void objectNullMethod(Object o) {
		if(o==null)
			field = 0;
	}
	
	public void setField(int val) {
		field = val;
	}
	
	public int getField() {
		return field;
	}
	
}
