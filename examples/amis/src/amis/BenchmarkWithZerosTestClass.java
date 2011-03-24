/**
 * 
 */
package amis;


/**
 * @author Andre Mis
 *
 */
public class BenchmarkWithZerosTestClass {

	private int someField = 0;
	private int someOtherField = 0;
	private int yetAnotherField = 0;
	private int targetField=0;

	private int auxField0 = 0;
	private int auxField1 = 0;
	private int auxField2 = 0;
	private int auxField3 = 0;
	private int auxField4 = 0;
	private int auxField5 = 0;
	private int auxField6 = 0;
	private int auxField7 = 0;
	private int auxField8 = 0;
	private int auxField9 = 0;
	
	private static boolean milestoneOZ1=false;
	private static boolean milestoneOZ2=false;
	private static boolean milestoneOZ3=false;
	private static boolean milestoneOZ4=false;
	
	// constructors
	
	public BenchmarkWithZerosTestClass() {
	}
	
	// target method

	/**
	 * Supposed to demonstrate the advantage of taking all overwriting defs into account
	 */
	public void mean() {

		if(someField == 0)
			targetField=1;
		if(someOtherField == 0)
			targetField=2;
		if(yetAnotherField != someField + someOtherField)
			targetField=3;
		if(targetField==0) {
			someOtherField=-someOtherField;
			if(!milestoneOZ1)
				System.out.println("Tests reached Zero-Or milestone 1");
			milestoneOZ1=true;
		}
		if(auxField0 != yetAnotherField*3 || auxField0==0)
			targetField=4;
		if(auxField1 != auxField0-13 || auxField1==0)
			targetField=5;		
		if(auxField2 != auxField1*auxField1 || auxField2==0)
			targetField=6;
		if(targetField==0) {
			auxField0=2;
			if(!milestoneOZ2)
				System.out.println("Tests reached Zero-Or milestone 2");
			milestoneOZ2=true;
		}
		if(auxField3 != auxField2+5 || auxField3==0)
			targetField=7;
		if(auxField4 != auxField3+2 || auxField4==0)
			targetField=8;
		if(auxField5 != auxField4-auxField2 || auxField5==0)
			targetField=9;
		if(targetField==0) {
			yetAnotherField++;
			if(!milestoneOZ3)
				System.out.println("Tests reached Zero-Or milestone 3");
			milestoneOZ3=true;
		}
		if(auxField6 != auxField5*someField || auxField6==0)
			targetField=10;
		if(auxField7 != auxField6*auxField5 || auxField7==0)
			targetField=11;
		if(auxField8 != auxField7+3*auxField2+13 || auxField8==0)
			targetField=12;
		if(auxField9 != auxField8-13*auxField1*auxField6 || auxField9==0)
			targetField=13;
		
		if(targetField == 0) { // target Use
			someField = 3;
			if(!milestoneOZ4)
				System.out.println(" = Tests reached Zero-Or milestone 4 = ");
			milestoneOZ4=true;
		}
	}
	
	// aux methods
	
	public void setSomeField(int field) {
		someField = field;
	}
	
	public void setSomeOtherField(int field) {
		someOtherField = field;
	}
	
	public void setYetAnotherField(int field) {
		yetAnotherField = field;
	}
	
	public void setTargetField(int field) {
		targetField=field;
	}
	
	public void setAuxField0(int val) {
		auxField0=val;
	}
	
	public void setAuxField1(int val) {
		auxField1=val;
	}
	
	public void setAuxField2(int val) {
		auxField2=val;
	}
	
	public void setAuxField3(int val) {
		auxField3=val;
	}
	
	public void setAuxField4(int val) {
		auxField4=val;
	}
	
	public void setAuxField5(int val) {
		auxField5=val;
	}
	
	public void setAuxField6(int val) {
		auxField6=val;
	}
	
	public void setAuxField7(int val) {
		auxField7=val;
	}
	
	public void setAuxField8(int val) {
		auxField8=val;
	}
	
	public void setAuxField9(int val) {
		auxField9=val;
	}
}
