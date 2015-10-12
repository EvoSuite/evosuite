package gnu.trove.set.hash;

import gnu.trove.strategy.HashingStrategy;
import gnu.trove.map.hash.ArrayHashingStrategy;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;


/**
 *
 */
public class TCustomHashSetTest extends TestCase {
	public void testArray() {
		char[] foo = new char[] { 'a', 'b', 'c' };
		char[] bar = new char[] { 'a', 'b', 'c' };

		assertFalse( foo.hashCode() == bar.hashCode() );
		//noinspection ArrayEquals
		assertFalse( foo.equals( bar ) );

		HashingStrategy<char[]> strategy = new ArrayHashingStrategy();
		assertTrue( strategy.computeHashCode( foo ) ==
			strategy.computeHashCode( bar ) );
		assertTrue( strategy.equals( foo, bar ) );

		Set<char[]> set = new TCustomHashSet<char[]>( strategy );
		set.add( foo );
		assertTrue( set.contains( foo ) );
		assertTrue( set.contains( bar ) );

		set.remove( bar );

		assertTrue( set.isEmpty() );
	}


	public void testSerialization() throws Exception {
		char[] foo = new char[] { 'a', 'b', 'c' };
		char[] bar = new char[] { 'a', 'b', 'c' };

		HashingStrategy<char[]> strategy = new ArrayHashingStrategy();
		Set<char[]> set = new TCustomHashSet<char[]>( strategy );

		set.add( foo );

		// Make sure it still works after being serialized
		ObjectOutputStream oout = null;
		ByteArrayOutputStream bout = null;
		ObjectInputStream oin = null;
		ByteArrayInputStream bin = null;
		try {
			bout = new ByteArrayOutputStream();
			oout = new ObjectOutputStream( bout );

			oout.writeObject( set );

			bin = new ByteArrayInputStream( bout.toByteArray() );
			oin = new ObjectInputStream( bin );

			set = ( Set<char[]> ) oin.readObject();
		}
		finally {
			if ( oin != null ) oin.close();
			if ( bin != null ) bin.close();
			if ( oout != null ) oout.close();
			if ( bout != null ) bout.close();
		}

		assertTrue( set.contains( foo ) );
		assertTrue( set.contains( bar ) );

		set.remove( bar );

		assertTrue( set.isEmpty() );
	}
}
