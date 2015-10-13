package gnu.trove.map.hash;

import gnu.trove.strategy.HashingStrategy;
import junit.framework.TestCase;

import java.io.*;
import java.util.*;


/**
 *
 */
public class TCustomHashMapTest extends TestCase {
	// Example from Trove overview doc
	public void testArray() {
		char[] foo = new char[]{ 'a', 'b', 'c' };
		char[] bar = new char[]{ 'a', 'b', 'c' };

		assertFalse( foo.hashCode() == bar.hashCode() );
		//noinspection ArrayEquals
		assertFalse( foo.equals( bar ) );

		HashingStrategy<char[]> strategy = new ArrayHashingStrategy();
		assertTrue( strategy.computeHashCode( foo ) ==
			strategy.computeHashCode( bar ) );
		assertTrue( strategy.equals( foo, bar ) );

		Map<char[], String> map = new TCustomHashMap<char[], String>( strategy );
		map.put( foo, "yay" );
		assertTrue( map.containsKey( foo ) );
		assertTrue( map.containsKey( bar ) );
		assertEquals( "yay", map.get( foo ) );
		assertEquals( "yay", map.get( bar ) );

		Set<char[]> keys = map.keySet();
		assertTrue( keys.contains( foo ) );
		assertTrue( keys.contains( bar ) );
	}


	public void testSerialization() throws Exception {
		char[] foo = new char[]{ 'a', 'b', 'c' };
		char[] bar = new char[]{ 'a', 'b', 'c' };

		HashingStrategy<char[]> strategy = new ArrayHashingStrategy();
		Map<char[], String> map = new TCustomHashMap<char[], String>( strategy );

		map.put( foo, "yay" );

		// Make sure it still works after being serialized
		ObjectOutputStream oout = null;
		ByteArrayOutputStream bout = null;
		ObjectInputStream oin = null;
		ByteArrayInputStream bin = null;
		try {
			bout = new ByteArrayOutputStream();
			oout = new ObjectOutputStream( bout );

			oout.writeObject( map );

			bin = new ByteArrayInputStream( bout.toByteArray() );
			oin = new ObjectInputStream( bin );

			map = ( Map<char[], String> ) oin.readObject();
		}
		finally {
			if ( oin != null ) oin.close();
			if ( bin != null ) bin.close();
			if ( oout != null ) oout.close();
			if ( bout != null ) bout.close();
		}

		assertTrue( map.containsKey( foo ) );
		assertTrue( map.containsKey( bar ) );
		assertEquals( "yay", map.get( foo ) );
		assertEquals( "yay", map.get( bar ) );

		Set<char[]> keys = map.keySet();
		assertTrue( keys.contains( foo ) );
		assertTrue( keys.contains( bar ) );
	}

	static class ByteArrayStrategy implements HashingStrategy<byte[]> {

		// Copied from Arrays.hashCode, but applied only to the first four bytes
		public int computeHashCode( byte[] bytes ) {
			if ( bytes == null ) return 0;
			int h = 1;
			for ( int i = 0; i < 4; i++ ) h = 31 * h + bytes[ i ];
			return h;
		}

		public boolean equals( byte[] o1, byte[] o2 ) {
			return Arrays.equals( o1, o2 );
		}
	}

	private static byte[] random( int n, Random rnd ) {
		byte[] ba = new byte[ n ];
		for ( int i = 0; i < ba.length; i++ ) {
			ba[ i ] = ( byte ) rnd.nextInt();
		}
		return ba;
	}

	public void testBug4706479() throws Exception {
		Random rnd = new Random( 1234 );
		TCustomHashMap<byte[], Integer> map =
			new TCustomHashMap<byte[], Integer>( new ByteArrayStrategy() );
		List<byte[]> list = new ArrayList<byte[]>();
		List<Integer> expected = new ArrayList<Integer>();

		for ( int i = 0; i < 1000; i++ ) {
			byte[] ba = random( 16, rnd );
			list.add( ba );

			Integer obj = Integer.valueOf( i );
			expected.add( obj );
			map.put( ba, obj );
		}

		assertEquals( list.size(), map.size() );

		// Make sure all the arrays are found in the map
		for( byte[] array : map.keySet() ) {
			boolean found_it = false;
			for( byte[] test : list ) {
				if ( Arrays.equals( test, array ) ) {
					found_it = true;
					break;
				}
			}

			assertTrue( "Unable to find: " + Arrays.toString( array ), found_it );
		}
		// Make sure all the Integers are found in the map
		for( Integer obj : map.values() ) {
			assertTrue( "Unable to find: " + obj, expected.contains( obj ) );
		}

		for ( int i = 0; i < expected.size(); i++ ) {
			assertEquals( expected.get( i ), map.get( list.get( i ) ) );
		}

		// Remove items
		for ( int i = 0; i < list.size(); i++ ) {
			assertEquals( expected.get( i ), map.remove( list.get( i ) ) );
		}

		assertEquals( 0, map.size() );
		assertTrue( map.isEmpty() );

		for ( byte[] aList : list ) {
			assertNull( map.get( aList ) );
		}
	}


}
