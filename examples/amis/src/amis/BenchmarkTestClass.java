/**
 * 
 */
package amis;


/**
 * @author ida
 *
 */
public class BenchmarkTestClass {

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
	
	private static boolean milestone1=false;
	private static boolean milestone2=false;
	private static boolean milestone3=false;
	private static boolean milestone4=false;
	
	// constructors
	
	public BenchmarkTestClass() {
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
		
		// milestone 1
		if(targetField==0) {
			yetAnotherField++;
			if(!milestone1)
				System.out.println("Tests reached milestone 1!");
			milestone1=true;
		}
		
		if(auxField0 != yetAnotherField*3+1)
			targetField=4;
		if(auxField1 != auxField0-13)
			targetField=5;		
		if(auxField2 != auxField1*auxField1)
			targetField=6;
		if(auxField3 != auxField2+5)
			targetField=7;
		
		// milestone 2
		if(targetField==0) {
			someOtherField= -someOtherField;
			if(!milestone2)
				System.out.println("Tests reached milestone 2!");
			milestone2=true;
		}
		
		if(auxField4 != auxField3+2)
			targetField=8;
		if(auxField5 != auxField4-auxField2)
			targetField=9;
		if(auxField6 != auxField5*someField)
			targetField=10;
		
		// milestone 3
		if(targetField==0) {
			auxField2--;
			if(!milestone3)
				System.out.println("Tests reached milestone 3!");
			milestone3=true;
		}
		
		if(auxField7 != auxField6*auxField5)
			targetField=11;
		if(auxField8 != auxField7+3*auxField2+13)
			targetField=12;
		if(auxField9 != auxField8-13*auxField1*auxField6)
			targetField=13;
					
		
		if(targetField == 0) { // target Use
			someField = 3;
			if(!milestone4)
				System.out.println(" = Tests reached milestone 4! Congratulations! = ");
			milestone4=true;
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
