package amis;


/**
 * @author Andre Mis
 *
 */
public class BaseConstructorTestClass {

	private int targetField = 0;
	
	// constructors

	public BaseConstructorTestClass() {
        }

	
	public BaseConstructorTestClass(int anInt) {
		targetField = anInt;
	}
	
	public BaseConstructorTestClass(int anInt, int anotherInt) {
		this(anInt+anotherInt);
		
		targetField = 1;
		new Object();
		setTargetField(imPrivate());
		targetField=-1;
		new BaseConstructorTestClass(3);
	}
////	
////	public BaseConstructorTestClass(int anInt, int anotherInt, int andAnotherInt) {
////		targetField = anInt+anotherInt+andAnotherInt;
////	}
////	
//	public BaseConstructorTestClass(String aString) {
//		this(aString.length()%2==0?-1:aString.length());
//	}
//	
//	public BaseConstructorTestClass(int i, int j) {
//		this(new Object() == null ? i : j);
//	}
////
	public void setTargetField(int field) {
		targetField = field;
	}
	public int getTargetField() {
		return targetField;
	}
	private int imPrivate() {
		return 7;
	}
	
}
