package gnu.trove.impl.hash;

import gnu.trove.map.hash.TObjectLongHashMap;
import junit.framework.TestCase;


/**
 *
 */
public class TObjectHashTest extends TestCase {
	// Test case bug bug ID 3067307
	public static void testBug3067307() {
		TObjectLongHashMap<String> testHash = new TObjectLongHashMap<String>();
		final int c = 1000;
		for ( long i = 1; i < c; i++ ) {
			final String data = "test-" + i;
			testHash.put( data, i );
			testHash.remove( data );
		}
	}

	// Test case bug bug ID 3067307
	public static void testBug3067307_noAutoCompact() {
		TObjectLongHashMap<String> testHash = new TObjectLongHashMap<String>();
		testHash.setAutoCompactionFactor( 0 );
		final int c = 1000;
		for ( long i = 1; i < c; i++ ) {
			final String data = "test-" + i;
			testHash.put( data, i );
			testHash.remove( data );
		}
	}
}
