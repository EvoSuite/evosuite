package amis;

/**
 * @author Andre Mis
 *
 */
public class ForLoopTestClass {

	public ForLoopTestClass() {
	}
	
	public int aMethod(int param) {
		
		int sum = 0;
		int c=0;
		for(int i=1;i<param*param;i*=2) {
			sum += i;
			c++;
			if(c>15)
				continue;
			if(c>10)
				break;
		}
		
		return sum;
	}
	
}
