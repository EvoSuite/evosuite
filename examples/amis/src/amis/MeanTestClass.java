/**
 * 
 */
package amis;


/**
 * @author ida
 *
 */
public class MeanTestClass {

	private int someField = 0;
	private int someOtherField = 0;
	private int yetAnotherField = 0;
	private int targetField = 0;
	
	// constructors
	
	public MeanTestClass() {
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
		targetField = field;
	}	
}
