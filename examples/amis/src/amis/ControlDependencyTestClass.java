package amis;

/**
 * @author Andre Mis
 *
 */
public class ControlDependencyTestClass {

	int anInt = 0;
	
	public ControlDependencyTestClass() {
	}
	
	public void setAnInt(int anInt) {
		this.anInt = anInt;
	}
	
	public void simpleDoWhile() {
		do {
			anInt++;
		} while(anInt >= 0 && (anInt % 13) !=0 );
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
		int c = 0;
		while(i != (anInt % 10)) {
			i++;
			c++;
			if(c>5)
				break;
			if(c<5)
				continue;
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
		int c = 0;
		while(i % 10 != (anInt % 10)) {
			i++;
			if(anInt%2 == 0)
				return i;
			c++;
			if(c>5)
				break;
			if(c<5)
				continue;
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
			i++;
			if(i % 13 == 1)
				return i;
		} while(i % 13 != 0);
	
		return i;
	}
	
	public int forMethod(int anInt) {
		int r = 0;
		for(int i=0;i<10;i++) {
			r = r+i+anInt;
		}
		
		return r;
	}
	
	public int forMethodReturn(int anInt) {
		for(int i=0;i<anInt;i++)
			return i;
		
		return anInt;
	}
}
