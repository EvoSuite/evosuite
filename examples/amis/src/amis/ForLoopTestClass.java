package amis;

/**
 * @author ida
 *
 */
public class ForLoopTestClass {

	public ForLoopTestClass() {
	}
	
	public int aMethod(int param) {
		
		int sum = 0;
		
		for(int i=1;i<param*param;i*=2)
			sum += i;
		
		return sum;
	}
	
}
