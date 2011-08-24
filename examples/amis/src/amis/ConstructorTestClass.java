package amis;


/**
 * @author Andre Mis
 *
 */
public class ConstructorTestClass extends BaseConstructorTestClass {

	private int targetField = 0;
	
	// constructors
	
	public ConstructorTestClass() {
	}
	
	public ConstructorTestClass(int anInt) {
		super(anInt);
	}
	
	public ConstructorTestClass(int anInt, int anotherInt) {
		this(anInt+anotherInt);
		
		targetField = 1;
		new Object();
		setTargetField(imPrivate());
		setTargetField(targetField);
		new ConstructorTestClass(3);
	}
//	
//	public ConstructorTestClass(int anInt, int anotherInt, int andAnotherInt) {
//		targetField = anInt+anotherInt+andAnotherInt;
//	}
//	
//	public ConstructorTestClass(String aString) {
//		targetField = aString.length();
//	}
//
	public void setTargetField(int field) {
		targetField = field;
		new ConstructorTestClass();
	}
	public int getTargetField() {
		return targetField;
	}
	private int imPrivate() {
		return 7;
	}
	
}
