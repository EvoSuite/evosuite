// ////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009, Rob Eden All Rights Reserved.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
// ////////////////////////////////////////////////////////////////////////////

package gnu.trove;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import junit.framework.TestCase;

import java.util.Iterator;
import java.util.List;


/**
 *
 */
public class TDecoratorsTest extends TestCase {
	public void testIntListDecorator() {
		TIntList list = new TIntArrayList();
		list.add( 2 );
		list.add( 3 );
		list.add( 4 );
		list.add( 5 );
		list.add( 6 );

		List<Integer> wrapped_list = TDecorators.wrap( list );
		assertEquals( 5, wrapped_list.size() );
		assertEquals( Integer.valueOf( 2 ), wrapped_list.get( 0 ) );
		assertEquals( Integer.valueOf( 3 ), wrapped_list.get( 1 ) );
		assertEquals( Integer.valueOf( 4 ), wrapped_list.get( 2 ) );
		assertEquals( Integer.valueOf( 5 ), wrapped_list.get( 3 ) );
		assertEquals( Integer.valueOf( 6 ), wrapped_list.get( 4 ) );

		list.removeAt( 1 );

		assertEquals( 4, list.size() );
		assertEquals( Integer.valueOf( 2 ), wrapped_list.get( 0 ) );
		assertEquals( Integer.valueOf( 4 ), wrapped_list.get( 1 ) );
		assertEquals( Integer.valueOf( 5 ), wrapped_list.get( 2 ) );
		assertEquals( Integer.valueOf( 6 ), wrapped_list.get( 3 ) );

		wrapped_list.remove( 1 );

		assertEquals( 3, list.size() );
		assertEquals( 2, list.get( 0 ) );
		assertEquals( 5, list.get( 1 ) );
		assertEquals( 6, list.get( 2 ) );

		list.clear();
		assertTrue( wrapped_list.isEmpty() );

		wrapped_list.add( Integer.valueOf( 7 ) );
		assertEquals( 1, list.size() );
		assertEquals( 7, list.get( 0 ) );

		wrapped_list.clear();
		assertTrue( list.isEmpty() );

		list.add( 8 );
		list.add( 9 );
		list.add( 10 );

		Iterator<Integer> wrapper_list_it = wrapped_list.iterator();
		assertTrue( wrapper_list_it.hasNext() );
		assertEquals( Integer.valueOf( 8 ), wrapper_list_it.next() );
		assertTrue( wrapper_list_it.hasNext() );
		assertEquals( Integer.valueOf( 9 ), wrapper_list_it.next() );
		assertTrue( wrapper_list_it.hasNext() );
		assertEquals( Integer.valueOf( 10 ), wrapper_list_it.next() );
		assertFalse( wrapper_list_it.hasNext() );

		wrapper_list_it = wrapped_list.iterator();
		assertTrue( wrapper_list_it.hasNext() );
		assertEquals( Integer.valueOf( 8 ), wrapper_list_it.next() );
		wrapper_list_it.remove();
		assertTrue( wrapper_list_it.hasNext() );
		assertEquals( Integer.valueOf( 9 ), wrapper_list_it.next() );
		assertTrue( wrapper_list_it.hasNext() );
		assertEquals( Integer.valueOf( 10 ), wrapper_list_it.next() );
		assertFalse( wrapper_list_it.hasNext() );

		assertEquals( 2, list.size() );
		assertEquals( 9, list.get( 0 ) );
		assertEquals( 10, list.get( 1 ) );
	}

	public void testIntListDecoratorSubList() {
		TIntList list = new TIntArrayList();
		list.add( 2 );
		list.add( 3 );
		list.add( 4 );
		list.add( 5 );
		list.add( 6 );

		List<Integer> wrapped_list = TDecorators.wrap( list );

		List<Integer> sublist = wrapped_list.subList( 1, 4 );
		assertEquals( 3, sublist.size() );
		assertEquals( Integer.valueOf( 3 ), sublist.get( 0 ) );
		assertEquals( Integer.valueOf( 4 ), sublist.get( 1 ) );
		assertEquals( Integer.valueOf( 5 ), sublist.get( 2 ) );

		sublist.clear();

		assertEquals( 2, list.size() );
		assertEquals( 2, list.get( 0 ) );
		assertEquals( 6, list.get( 1 ) );
	}
}
