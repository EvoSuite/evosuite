package amis;

/**
 * @author Andre Mis
 *
 */
public class NotOnOneObjectTestClass {

	private int field;
	private boolean flag = false;
	
	public NotOnOneObjectTestClass(int anInt) {
		
		field = anInt;
	}
	
	/**
	 * It should not be possible to cover the goal for targetDef and targetUse 
	 */
	public void setField(int val) {
		if(flag)
			return;
		field = val; // targetDef
		flag = true;
	}
	
	public int getField() {
		if(flag)
			return -1;
		flag = true;
		return field; // targetUse
	}
	
}
