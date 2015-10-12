package gnu.trove.list.linked;

import gnu.trove.list.TLinkableAdapter;
import junit.framework.TestCase;


/**
 *
 */
public class TLinkableAdapterTest extends TestCase {
	public void testOverride() {
		TLinkedList<MyObject> list = new TLinkedList<MyObject>();

		list.add( new MyObject( "1" ) );
		list.add( new MyObject( "2" ) );
		list.add( new MyObject( "3" ) );

		int i = 1;
		for( MyObject obj : list ) {
			assertEquals( String.valueOf( i ), obj.getValue() );
			i++;
		}
	}


	private class MyObject extends TLinkableAdapter<MyObject> {
		private final String value;

		MyObject( String value ) {
			this.value = value;
		}


		public String getValue() {
			return value;
		}
	}
}
