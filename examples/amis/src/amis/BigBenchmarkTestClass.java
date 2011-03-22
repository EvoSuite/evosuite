/**
 * 
 */
package amis;


/**
 * @author Andre Mis
 *
 */
public class BigBenchmarkTestClass {

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
	
	private static boolean milestoneAZ1=false;
	private static boolean milestoneAZ2=false;
	private static boolean milestoneAZ3=false;
	private static boolean milestoneAZ4=false;
	
	private static boolean milestoneOZ1=false;
	private static boolean milestoneOZ2=false;
	private static boolean milestoneOZ3=false;
	private static boolean milestoneOZ4=false;
	
	private static boolean milestoneH1=false;
	private static boolean milestoneH2=false;
	private static boolean milestoneH4=false;
	
	
	// constructors
	
	public BigBenchmarkTestClass() {
	}
	public BigBenchmarkTestClass(int val) {
		targetField = val;
	}
	public BigBenchmarkTestClass(int val,int val2) {
		targetField = val+val2;
	}
	public BigBenchmarkTestClass(int val,int val2,int val3) {
		targetField = val+val2+val3;
	}
	public BigBenchmarkTestClass(int val,int val2,int val3,int val4) {
		targetField = val+val2+val3+val4;
	}
	public BigBenchmarkTestClass(int val,int val2,int val3,int val4,int val5) {
		targetField = val+val2+val3*val4*val5;
	}
	public BigBenchmarkTestClass(int val,int val2,int val3,int val4,int val5,int val6) {
		targetField = val+val2+val3*val4*val5-val6;
	}
	public BigBenchmarkTestClass(int val,int val2,int val3,int val4,int val5,int val6,int val7) {
		targetField = val+val2+val3*val4*val5-val6*val7;
	}
	public BigBenchmarkTestClass(int val,int val2,int val3,int val4,int val5,int val6,int val7,int val8) {
		targetField = val+val2+val3*val4*val5-val6-val7-val8;
	}
	
	public BigBenchmarkTestClass(Object val,int val2) {
		targetField = val.toString().length()+val2;
	}
	public BigBenchmarkTestClass(Object val,int val2,int val3) {
		targetField = val.toString().length()+val2+val3;
	}
	public BigBenchmarkTestClass(Object val,int val2,int val3,int val4) {
		targetField = val.toString().length()+val2+val3+val4;
	}
	public BigBenchmarkTestClass(Object val,int val2,int val3,int val4,int val5) {
		targetField = val.toString().length()+val2+val3*val4*val5;
	}
	public BigBenchmarkTestClass(Object val,int val2,int val3,int val4,int val5,int val6) {
		targetField = val.toString().length()+val2+val3*val4*val5-val6;
	}
	public BigBenchmarkTestClass(Object val,int val2,int val3,int val4,int val5,int val6,int val7) {
		targetField = val.toString().length()+val2+val3*val4*val5-val6*val7;
	}
	public BigBenchmarkTestClass(Object val,int val2,int val3,int val4,int val5,int val6,int val7,int val8) {
		targetField = val.toString().length()+val2+val3*val4*val5-val6-val7-val8;
	}	
	
	
	// target method

	
	public void mean() {

		if(someField == 0) {
			targetField = 1;
		} else {
			if(someOtherField == 0) {
				targetField = 2;
			} else {
				// the following two uses for someField and someOtherField can 
				// not be paired with their definitions in the constructor
				if((yetAnotherField != someField + someOtherField) || yetAnotherField == 0) {
					targetField = 3;
				}
			}
		}
		
		if(targetField == 0) { // target Use
			someField = 3;
		}
	}	
	
	public void meanBenchmark() {


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
	
	public void meanWithAndZeros() {

		if(someField == 0)
			targetField=1;
		if(someOtherField == 0)
			targetField=2;
		if(yetAnotherField != someField + someOtherField)
			targetField=3;
		if(targetField==0) {
			someOtherField=-someOtherField;
			if(!milestoneAZ1)
				System.out.println("Tests reached Zero-And milestone 1");
			milestoneAZ1=true;
		}
		if(auxField0 != yetAnotherField*3 && auxField0!=0)
			targetField=4;
		if(auxField1 != auxField0-13 && auxField1!=0)
			targetField=5;		
		if(auxField2 != auxField1*auxField1 && auxField2!=0)
			targetField=6;
		if(targetField==0) {
			auxField0=2;
			if(!milestoneAZ2)
				System.out.println("Tests reached Zero-And milestone 1");
			milestoneAZ2=true;
		}
		if(auxField3 != auxField2+5 && auxField3!=0)
			targetField=7;
		if(auxField4 != auxField3+2 && auxField4!=0)
			targetField=8;
		if(auxField5 != auxField4-auxField2 && auxField5!=0)
			targetField=9;
		if(targetField==0) {
			yetAnotherField++;
			if(!milestoneAZ3)
				System.out.println("Tests reached Zero-And milestone 1");
			milestoneAZ3=true;
		}
		if(auxField6 != auxField5*someField && auxField6!=0)
			targetField=10;
		if(auxField7 != auxField6*auxField5 && auxField7!=0)
			targetField=11;
		if(auxField8 != auxField7+3*auxField2+13 && auxField8!=0)
			targetField=12;
		if(auxField9 != auxField8-13*auxField1*auxField6 && auxField9!=0)
			targetField=13;
		
		if(targetField == 0) { // target Use
			someField = 3;
			if(!milestoneAZ4)
				System.out.println(" = Tests reached Zero-And milestone 4 = ");
			milestoneAZ4=true;
		}
	}
	
	public void meanWithOrZeros() {

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
				System.out.println("Tests reached Zero-Or milestone 1");
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
				System.out.println("Tests reached Zero-Or milestone 1");
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
	
	public void meanHard() {

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
