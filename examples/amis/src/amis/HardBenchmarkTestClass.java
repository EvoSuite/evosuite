/**
 * 
 */
package amis;


/**
 * @author Andre Mis
 *
 */
public class HardBenchmarkTestClass {

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
	
	private static boolean milestoneH1=false;
	private static boolean milestoneH2=false;
	private static boolean milestoneH4=false;
	
	// constructors
	
	public HardBenchmarkTestClass() {
	}
	
	// target method
	
	/**
	 * Supposed to show that you might have to follow an arbitrarily 
	 * complex path in order to not block the targetUse by another def.
	 * 
	 *  Or in other words: Satisfying a DefUse-Pair is as hard as satisfying a specific path.
	 *  
	 *  ... turns out this also shows that covering a certain BranchCoverageGoal can be that hard :D
	 */
	public void mean() {

		if(someField == 0)
			targetField=1;
		else if(someOtherField == 0)
			targetField=2;
			// the following two uses for someField and someOtherField can 
			// not be paired with their definitions in the constructor
		else if((yetAnotherField != someField + someOtherField) || yetAnotherField == 0)
			targetField=3;
		else {
			if(targetField==0) {// milestone 1
				yetAnotherField = 3;
				if(!milestoneH1)
					System.out.println("Tests reached Hard-Milestone 1");
				milestoneH1 = true;
			}
			
			if(auxField0 == 0 || auxField0 != yetAnotherField*3)
				targetField=4;
		   else if(auxField1 == 0 || auxField1 != auxField0-13)
				targetField=5;		
			else if(auxField2 == 0 || auxField2 != auxField1*auxField1)
				targetField=6;
			else if(auxField3 == 0 || auxField3 != auxField2+5)
				targetField=7;
			else {
				if(targetField==0) { // milestone 2
					someOtherField = -someOtherField;
					if(!milestoneH2)
						System.out.println("Tests reached Hard-Milestone 2");
					milestoneH2 = true;					
				}
				
				if(auxField4 == 0 || auxField4 != auxField3+2)
					targetField=8;
				else if(auxField5 == 0 || auxField5 != auxField4-auxField2)
					targetField=9;
				else if(auxField6 == 0 || auxField6 != auxField5*someField)
					targetField=10;
				else if(auxField7 == 0 || auxField7 != auxField6*auxField5)
					targetField=11;
				else if(auxField8 == 0 || auxField8 != auxField7+3*auxField2+13)
					targetField=12;
				else if(auxField9 == 0 || auxField9 != auxField8-13*auxField1*auxField6)
					targetField=13;
			}
		}
					
		
		if(targetField == 0) { // target Use
			if(!milestoneH4)
				System.out.println(" = Tests reached Hard-Milestone 4 !!! = ");
			milestoneH4 = true;
			someField = 3;
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
