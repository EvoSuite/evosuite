package amis;

/**
 * @author ida
 *
 */
public class ControlDependencyTestClass {

	public ControlDependencyTestClass() {
	}
	
	public int ifMethod(int anInt) {
		int i = anInt;
		if(i%2 == 0)
			i++;
		else
			i--;
		
		return i;
	}
	
	public int bigIfMethod(int anInt) {
		int i = anInt;
		if(i%17 == 0)
			if(i%2 == 0)
				i++;
			else
				i--;
		else
			if(i%2 == 0)
				return i-1;
			else
				i++;
		
		return i;
	}
	
	public int bigIfMethod2(int anInt) {
		int i = anInt;
		if(i%17 == 0)
			if(i%2 == 0)
				i++;
			else
				i--;
		else
			if(i%2 == 0)
				i--;
			else
				return i+1;
		
		return i;
	}
	
	public int ifMethodReturn(int anInt) {
		int i = anInt;
		if(i%2 == 0)
			return i;
		else
			return i-1;
	}
	
	public int bigIfMethodReturn(int anInt) {
		int i = anInt;
		if(i%17 == 0)
			if(i%2 == 0)
				return i;
			else
				return i-1;
		else
			if(i%2 == 0)
				return i+1;
			else
				return i;		
	}
	
	public int whileMethod(int anInt) {
		int i = 0;
		while(i<anInt) {
			i++;
		}
		return i;
	}

	
	public int whileMethodReturn(int anInt) {
		int i = 0;
		while(i<anInt) {
			i++;
			return i;
		}
		return -1;
	}

	public int whileMethodConditionalReturn(int anInt) {
		int i = 0;
		while(i<anInt) {
			i++;
			if(anInt%2 == 0)
				return i;
		}
		return -1;
	}	
	
	public int doWhileMethod(int anInt) {
		int i = anInt;
		do {
			i++;
		} while(i % 13 != 0);
	
		return i;
	}
	
	public int doWhileMethodReturn(int anInt) {
		int i = anInt;
		do {
			if(i % 13 == 1)
				return i;
		} while(i % 13 != 0);
	
		return i;
	}
	
	public int forMethod(int anInt) {
		int r = 0;
		for(int i=0;i<anInt;i++) {
			r = r+i;
		}
		
		return r;
	}
	
	public int forMethodReturn(int anInt) {
		for(int i=0;i<anInt;i++)
			return i;
		
		return anInt;
	}
}
