package gnu.trove.strategy;

import gnu.trove.map.hash.TCustomHashMap;
import junit.framework.TestCase;

import java.util.Map;


/**
 *
 */
public class IdentityHashingStrategyTest extends TestCase {
	public void testInMap() {
		Map<Integer,String> map =
			new TCustomHashMap<Integer, String>( new IdentityHashingStrategy<Integer>() );

		Integer first = new Integer( 0 );
		Integer second = new Integer( 0 );

		map.put( first, "first" );

		assertEquals( 1, map.size() );
		assertTrue( map.containsKey( first ));
		assertFalse( map.containsKey( second ) );
		assertEquals( "first", map.get( first ) );

		map.put( second, "second" );

		assertEquals( 2, map.size() );
		assertEquals( "first", map.get( first ) );
		assertEquals( "second", map.get( second ) );

		map.remove( first );

		assertEquals( 1, map.size() );
		assertFalse( map.containsKey( first ) );
		assertTrue( map.containsKey( second ) );
	}
}
