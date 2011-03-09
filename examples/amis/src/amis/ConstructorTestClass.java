package amis;


/**
 * @author ida
 *
 */
public class ConstructorTestClass {

	private int targetField = 0;
	
	// constructors
	
	public ConstructorTestClass() {
	}
	
	public ConstructorTestClass(int anInt) {
		targetField = anInt;
	}
	
	public ConstructorTestClass(int anInt, int anotherInt) {
		targetField = anInt+anotherInt;
	}
	
	public ConstructorTestClass(int anInt, int anotherInt, int andAnotherInt) {
		targetField = anInt+anotherInt+andAnotherInt;
	}
	
	public ConstructorTestClass(String aString) {
		targetField = aString.length();
	}

	public void setTargetField(int field) {
		targetField = field;
	}
	public int getTargetField() {
		return targetField;
	}
	
}
