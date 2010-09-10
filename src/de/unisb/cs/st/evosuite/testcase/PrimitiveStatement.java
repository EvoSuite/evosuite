package de.unisb.cs.st.evosuite.testcase;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.string.StringPool;
import de.unisb.cs.st.ga.Randomness;

/**
 * Statement assigning a primitive numeric value
 * @author Gordon Fraser
 *
 * @param <T>
 */
public class PrimitiveStatement<T> extends Statement {

	private static int MAX_STRING = Properties.getPropertyOrDefault("string.length", 20);

	private static double P_pool = Properties.getPropertyOrDefault("string.pool", 0.5);

	private static Randomness randomness = Randomness.getInstance();
	
	private static StringPool string_pool = StringPool.getInstance();
	
	/**
	 * The value
	 */
	T value;
	
	/**
	 * Constructor
	 * @param reference
	 * @param value
	 */
	public PrimitiveStatement(VariableReference reference, T value) {
		this.retval = reference;
		this.value = value;
	}

	
	/**
	 * Create random primitive statement
	 * @param reference
	 * @param clazz
	 * @return
	 */
	public static PrimitiveStatement<?> getRandomStatement(VariableReference reference, Type clazz) {
				
		if(clazz == boolean.class) {
			return new PrimitiveStatement<Boolean>(reference, randomness.nextBoolean());
		} else if (clazz == int.class) {
//			return new PrimitiveStatement<Integer>(reference, randomness.nextInt());
//		return new PrimitiveStatement<Integer>(reference, new Integer((short) (randomness.nextInt(2 * 32767) - 32767)));
		return new PrimitiveStatement<Integer>(reference, new Integer((short) (randomness.nextInt(2 * 512) - 512)));
//			return new PrimitiveStatement<Integer>(reference, new Integer((short) (randomness.nextInt(32767))));
//			return new PrimitiveStatement<Integer>(reference, new Integer((short) (randomness.nextInt(100)))); // TODO: Parametrize!!
		} else if (clazz == char.class) {
			// Only ASCII chars?
			return new PrimitiveStatement<Character>(reference, (char)(randomness.nextInt('Z'-'A'+1)+'A'));
		} else if (clazz == long.class) {
//			return new PrimitiveStatement<Long>(reference, randomness.nextLong());
			return new PrimitiveStatement<Long>(reference, new Long((short) (randomness.nextInt(2 * 32767) - 32767)));
//			return new PrimitiveStatement<Long>(reference, new Long((short) (randomness.nextInt(32767))));
//			return new PrimitiveStatement<Long>(reference, new Long((short) (randomness.nextInt(100))));
		} else if (clazz == double.class) {
			return new PrimitiveStatement<Double>(reference, randomness.nextDouble());
		} else if (clazz == float.class) {
			return new PrimitiveStatement<Float>(reference, randomness.nextFloat());
		} else if (clazz == short.class) {
			return new PrimitiveStatement<Short>(reference, new Short((short) (randomness.nextInt(2 * 32767) - 32767)));
		} else if (clazz == byte.class) {
			return new PrimitiveStatement<Byte>(reference, new Byte((byte) (randomness.nextInt(256) - 128)));
		} else if (clazz == String.class) {
			if(randomness.nextDouble() >= P_pool )
				return new PrimitiveStatement<String>(reference, randomness.nextString(randomness.nextInt(MAX_STRING)));
			else
				return new PrimitiveStatement<String>(reference, string_pool.getRandomString());
		} else {
			logger.error("Getting unknown type: "+clazz);
		}
		// TODO: Char
		assert(false);
		return null;
	}

	
	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getCode() {
		if(retval.getVariableClass().equals(char.class) || retval.getVariableClass().equals(Character.class))
			return ((Class<?>) retval.getType()).getSimpleName() + " "+retval.getName() + " = '" + value +"'";
		else if(retval.getVariableClass().equals(String.class)) {
			return ((Class<?>) retval.getType()).getSimpleName() + " "+retval.getName() + " = \"" + StringEscapeUtils.escapeJava((String) value) +"\"";
		}
		else
			return ((Class<?>) retval.getType()).getSimpleName() + " " +retval.getName() + " = " + value;
	}

	@Override
	public Statement clone() {
		return new PrimitiveStatement<T>(new VariableReference(retval.getType(), retval.statement), value);
	}

	@Override
	public Throwable execute(Scope scope, PrintStream out)
			throws InvocationTargetException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {
		// Add primitive variable to pool
		scope.set(retval, value);
		return exceptionThrown;
	}

	@Override
	public void adjustVariableReferences(int position, int delta) {
		retval.adjust(delta, position);
		adjustAssertions(position, delta);
	}

	@Override
	public boolean references(VariableReference var) {
		return false;
	}

	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> references = new HashSet<VariableReference>();
		references.add(retval);
		return references;
	}

	@Override
	public boolean equals(Statement s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;
				
		PrimitiveStatement<?> ps = (PrimitiveStatement<?>)s;
		return (retval.equals(ps.retval) && value.equals(ps.value));
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 21;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	private static String removeCharAt(String s, int pos) {
		return s.substring(0,pos)+s.substring(pos+1);
	}
	
	private static String replaceCharAt(String s, int pos, char c) {
		return s.substring(0,pos) + c + s.substring(pos+1);
	}

	private static String insertCharAt(String s, int pos, char c) {
		return s.substring(0,pos) + c + s.substring(pos);
	}

	private String StringInsert(String s, int pos) {
		final double ALPHA = 0.5;
		int count = 1;
		
		while(randomness.nextDouble() <= Math.pow(ALPHA, count) && s.length() < MAX_STRING)
		{
			count++;			
			//logger.info("Before insert: '"+s+"'");
			s = insertCharAt(s, pos, randomness.nextChar());
			//logger.info("After insert: '"+s+"'");
		}
		return s;
	}
	
	@SuppressWarnings("unchecked")
	private void deltaString() {
		
		String s = (String)value;
		
		final double P2 = 1d/3d;
		double P = 1d/s.length();
		// Delete
    	if(randomness.nextDouble() < P2) {
    		for(int i = s.length(); i>0; i--) {
    			if(randomness.nextDouble() < P) {
    				//logger.info("Before remove at "+i+": '"+s+"'");
    				s = removeCharAt(s, i - 1);
    				//logger.info("After remove: '"+s+"'");
    			}
    		}
    	}
    	P = 1d/s.length();
    	// Change
    	if(randomness.nextDouble() < P2) {
    		for(int i = 0; i < s.length(); i++) {
    			if(randomness.nextDouble() < P) {
    				//logger.info("Before change: '"+s+"'");
    				s = replaceCharAt(s, i, randomness.nextChar());
    				//logger.info("After change: '"+s+"'");
    			}
    		}	    	
    	}
    	
    	// Insert
    	if(randomness.nextDouble() < P2) {
//    		for(int i = 0; i < s.length(); i++) {
    			//if(randomness.nextDouble() < P) {
    				int pos = 0;
    				if(s.length() > 0)
    					pos = randomness.nextInt(s.length());
    				s = StringInsert(s, pos);
    			//}
  //  		}	    		    	
    	}
    	value = (T) s;
    	//logger.info("Mutated string now is: "+value);
	}
	
	@SuppressWarnings("unchecked")
	public void delta() {
		
		double delta = 40.0 * randomness.nextDouble() - 20.0;
		
		if(value instanceof Boolean) {
			value = (T) new Boolean(!((Boolean)value).booleanValue());
		} else if(value instanceof Integer) {
			value = (T) new Integer(((Integer)value).intValue() + (int)delta);
		} else if(value instanceof Character) {
			value = (T) new Character((char) (((Character)value).charValue() + (int)delta));			
		} else if(value instanceof Long) {
			value = (T) new Long(((Long)value).longValue() +(int)delta);
		} else if(value instanceof Double) {
			value = (T) new Double(((Double)value).doubleValue() + delta);
		} else if(value instanceof Float) {
			value = (T) new Float(((Float)value).floatValue() + delta);
		} else if(value instanceof Short) {
			value = (T) new Short((short) (((Short)value).shortValue() + (int)delta));
		} else if(value instanceof Byte) {
			value = (T) new Byte((byte) (((Byte)value).byteValue() + (int)delta));
		} else if(value instanceof String) {
			deltaString();
		}

	}

	@SuppressWarnings("unchecked")
	public void increment() {
		if(value instanceof Boolean) {
			value = (T) new Boolean(!((Boolean)value).booleanValue());
		} else if(value instanceof Integer) {
			value = (T) new Integer(((Integer)value).intValue() +1);
		} else if(value instanceof Character) {
			value = (T) new Character((char) (((Character)value).charValue() +1));			
		} else if(value instanceof Long) {
			value = (T) new Long(((Long)value).longValue() +1);
		} else if(value instanceof Double) {
			value = (T) new Double(((Double)value).doubleValue() +1.0);
		} else if(value instanceof Float) {
			value = (T) new Float(((Float)value).floatValue() +1.0);
		} else if(value instanceof Short) {
			value = (T) new Short((short) (((Short)value).shortValue() +1));
		} else if(value instanceof Byte) {
			value = (T) new Byte((byte) (((Byte)value).byteValue() +1));
		}
	}
	
	@SuppressWarnings("unchecked")
	public void decrement() {
		if(value instanceof Boolean) {
			value = (T) new Boolean(!((Boolean)value).booleanValue());
		} else if(value instanceof Integer) {
			value = (T) new Integer(((Integer)value).intValue() -1);
		} else if(value instanceof Character) {
			value = (T) new Character((char) (((Character)value).charValue() -1));			
		} else if(value instanceof Long) {
			value = (T) new Long(((Long)value).longValue() -1);
		} else if(value instanceof Double) {
			value = (T) new Double(((Double)value).doubleValue() -1.0);
		} else if(value instanceof Float) {
			value = (T) new Float(((Float)value).floatValue() -1.0);
		} else if(value instanceof Short) {
			value = (T) new Short((short) (((Short)value).shortValue() -1));
		} else if(value instanceof Byte) {
			value = (T) new Byte((byte) (((Byte)value).byteValue() -1));
		
		}
	}
	@SuppressWarnings("unchecked")
	public void zero() {
		if(value instanceof Boolean) {
			value = (T) new Boolean(false);
		} else if(value instanceof Integer) {
			value = (T) new Integer(0);
		} else if(value instanceof Character) {
			value = (T) new Character((char) 0);			
		} else if(value instanceof Long) {
			value = (T) new Long(0);
		} else if(value instanceof Double) {
			value = (T) new Double(0.0);
		} else if(value instanceof Float) {
			value = (T) new Float(0.0);
		} else if(value instanceof Short) {
			value = (T) new Short((short)0);
		} else if(value instanceof Byte) {
			value = (T) new Byte((byte) 0);
		
		}
	}

	@Override
	public String getCode(Throwable exception) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void replace(VariableReference oldVar, VariableReference newVar) {
		if(retval.equals(oldVar))
			retval = newVar;
		
	}
}
