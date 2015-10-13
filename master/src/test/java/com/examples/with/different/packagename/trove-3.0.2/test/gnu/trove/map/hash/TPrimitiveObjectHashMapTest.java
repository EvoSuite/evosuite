///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001-2006, Eric D. Friedman All Rights Reserved.
// Copyright (c) 2009, Rob Eden All Rights Reserved.
// Copyright (c) 2009, Jeff Randall All Rights Reserved.
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
///////////////////////////////////////////////////////////////////////////////

package gnu.trove.map.hash;


import gnu.trove.TIntCollection;
import gnu.trove.function.TObjectFunction;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;



/**
 *
 */
public class TPrimitiveObjectHashMapTest extends TestCase {

    public TPrimitiveObjectHashMapTest( String name ) {
        super( name );
    }


	public void testBug2975214() {
		TIntObjectHashMap<Integer> map = new TIntObjectHashMap<Integer>( 5 );
		for ( int i = 0; i < 9; i++ ) {
			System.out.println( "Pass: " + i );
			map.put( i, new Integer( i ) );
			map.remove( i );
		}
	}


    public void testConstructors() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }

        TIntObjectMap<String> capacity =
                new TIntObjectHashMap<String>( 20 );
        for ( int i = 0; i < element_count; i++ ) {
            capacity.put( keys[i], vals[i] );
        }
        assertEquals( map, capacity );

        TIntObjectMap<String> cap_and_factor =
                new TIntObjectHashMap<String>( 20, 0.75f );
        for ( int i = 0; i < element_count; i++ ) {
            cap_and_factor.put( keys[i], vals[i] );
        }
        assertEquals( map, cap_and_factor );

        TIntObjectMap<String> fully_specified =
                new TIntObjectHashMap<String>( 20, 0.75f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            fully_specified.put( keys[i], vals[i] );
        }
        assertEquals( map, fully_specified );

        TIntObjectMap<String> copy =
                new TIntObjectHashMap<String>( map );
        assertEquals( map, copy );
    }


    public void testContainsKey() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }

        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( "Key should be present: " + keys[i] + ", map: " + map,
                    map.containsKey( keys[i] ) );
        }

        int key = 1138;
        assertFalse( "Key should not be present: " + key + ", map: " + map,
                map.containsKey( key ) );
    }


    public void testContainsValue() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }

        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( "Value should be present: " + vals[i] + ", map: " + map,
                    map.containsValue( vals[i] ) );
        }

        String val = "1138";
        assertFalse( "Key should not be present: " + val + ", map: " + map,
                map.containsValue( val ) );

        assertFalse( "Random object should not be present in map: " + map,
                map.containsValue( new Object() ) );

        // test with null value
        int key = 11010110;
        map.put( key, null );
        assertTrue( map.containsKey( key ) );
        assertTrue( map.containsValue( null ) );
        assertNull( map.get( key ) );
    }


    public void testPutIfAbsent() {
        TIntObjectMap<String> map = new TIntObjectHashMap<String>();

        map.put( 1, "One" );
        map.put( 2, "Two" );
        map.put( 3, "Three" );

        assertEquals( "One", map.putIfAbsent( 1, "Two" ) );
        assertEquals( "One", map.get( 1 ) );
        assertEquals( null, map.putIfAbsent( 9, "Nine") );
        assertEquals( "Nine", map.get( 9 ) );
    }


    public void testRemove() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }

        for ( int i = 0; i < element_count; i++ ) {
            if ( i % 2 == 1 ) {
                assertEquals( "Remove should have modified map: " + keys[i] + ", map: " + map,
                        vals[i], map.remove( keys[i] ) );
            }
        }

        for ( int i = 0; i < element_count; i++ ) {
            if ( i % 2 == 1 ) {
                assertTrue( "Removed key still in map: " + keys[i] + ", map: " + map,
                        map.get( keys[i] ) == null );
            } else {
                assertTrue( "Key should still be in map: " + keys[i] + ", map: " + map,
                        map.get( keys[i] ).equals( vals[i] ) );
            }
        }
    }


    public void testPutAllMap() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> control = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            control.put( keys[i], vals[i] );
        }

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();

        Map<Integer, String> source = new HashMap<Integer, String>();
        for ( int i = 0; i < element_count; i++ ) {
            source.put( keys[i], vals[i] );
        }

        map.putAll( source );
        assertEquals( control, map );
    }


    public void testPutAll() throws Exception {
        TIntObjectMap<String> t = new TIntObjectHashMap<String>();
        TIntObjectMap<String> m = new TIntObjectHashMap<String>();
        m.put( 2, "one" );
        m.put( 4, "two" );
        m.put( 6, "three" );

        t.put( 5, "four" );
        assertEquals( 1, t.size() );

        t.putAll( m );
        assertEquals( 4, t.size() );
        assertEquals( "two", t.get( 4 ) );
    }


    public void testClear() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        map.clear();
        assertTrue( map.isEmpty() );
        assertEquals( 0, map.size() );

        assertNull( map.get( keys[5] ) );
    }


    public void testKeySet() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntSet keyset = map.keySet();
        for ( int i = 0; i < keyset.size(); i++ ) {
            assertTrue( keyset.contains( keys[i] ) );
        }
        assertFalse( keyset.isEmpty() );

        int[] keys_array = keyset.toArray();
        int count = 0;
        TIntIterator iter = keyset.iterator();
        while ( iter.hasNext() ) {
            int key = iter.next();
            assertTrue( keyset.contains( key ) );
            assertEquals( keys_array[count], key );
            count++;
        }

        //noinspection ToArrayCallWithZeroLengthArrayArgument
        keys_array = keyset.toArray( new int[0] );
        count = 0;
        iter = keyset.iterator();
        while ( iter.hasNext() ) {
            int key = iter.next();
            assertTrue( keyset.contains( key ) );
            assertEquals( keys_array[count], key );
            count++;
        }

        keys_array = keyset.toArray( new int[keyset.size()] );
        count = 0;
        iter = keyset.iterator();
        while ( iter.hasNext() ) {
            int key = iter.next();
            assertTrue( keyset.contains( key ) );
            assertEquals( keys_array[count], key );
            count++;
        }

        keys_array = keyset.toArray( new int[keyset.size() * 2] );
        count = 0;
        iter = keyset.iterator();
        while ( iter.hasNext() ) {
            int key = iter.next();
            assertTrue( keyset.contains( key ) );
            assertEquals( keys_array[count], key );
            count++;
        }
        assertEquals( keyset.getNoEntryValue(), keys_array[keyset.size()] );

        TIntSet other = new TIntHashSet( keyset );
        assertFalse( keyset.retainAll( other ) );
        other.remove( keys[5] );
        assertTrue( keyset.retainAll( other ) );
        assertFalse( keyset.contains( keys[5] ) );
        assertFalse( map.containsKey( keys[5] ) );

        keyset.clear();
        assertTrue( keyset.isEmpty() );
    }


    public void testKeySetAdds() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntSet keyset = map.keySet();
        for ( int i = 0; i < keyset.size(); i++ ) {
            assertTrue( keyset.contains( keys[i] ) );
        }
        assertFalse( keyset.isEmpty() );

        try {
            keyset.add( 1138 );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }

        try {
            Set<Integer> test = new HashSet<Integer>();
            test.add( Integer.valueOf( 1138 ) );
            keyset.addAll( test );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }

        try {
            TIntSet test = new TIntHashSet();
            test.add( 1138 );
            keyset.addAll( test );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }
        
        try {
            int[] test = { 1138 };
            keyset.addAll( test );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }
    }


    public void testKeySetContainsAllCollection() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntSet keyset = map.keySet();
        for ( int i = 0; i < keyset.size(); i++ ) {
            assertTrue( keyset.contains( keys[i] ) );
        }
        assertFalse( keyset.isEmpty() );

        Collection<Integer> test_collection = new HashSet<Integer>();
        for ( int i = 0; i < element_count; i++ ) {
            test_collection.add( keys[i] );
        }
        assertTrue( keyset.containsAll( test_collection ) ) ;

        test_collection.remove( Integer.valueOf( keys[5] ) );
        assertTrue( "should contain all. keyset: " + keyset + ", " + test_collection,
                keyset.containsAll( test_collection ) );

        test_collection.add( Integer.valueOf( 1138 ) );
        assertFalse( "should not contain all. keyset: " + keyset + ", " + test_collection,
                keyset.containsAll( test_collection ) );
    }


    public void testKeySetContainsAllTCollection() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntSet keyset = map.keySet();
        for ( int i = 0; i < keyset.size(); i++ ) {
            assertTrue( keyset.contains( keys[i] ) );
        }
        assertFalse( keyset.isEmpty() );

        TIntCollection test_collection = new TIntHashSet();
        for ( int i = 0; i < element_count; i++ ) {
            test_collection.add( keys[i] );
        }
        assertTrue( keyset.containsAll( test_collection ) ) ;
        assertTrue( keyset.equals( keyset ) );

        test_collection.remove( Integer.valueOf( keys[5] ) );
        assertTrue( "should contain all. keyset: " + keyset + ", " + test_collection,
                keyset.containsAll( test_collection ) );

        test_collection.add( 1138 );
        assertFalse( "should not contain all. keyset: " + keyset + ", " + test_collection,
                keyset.containsAll( test_collection ) );
    }


    public void testKeySetContainsAllArray() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntSet keyset = map.keySet();
        for ( int i = 0; i < keyset.size(); i++ ) {
            assertTrue( keyset.contains( keys[i] ) );
        }
        assertFalse( keyset.isEmpty() );

        assertTrue( "should contain all. keyset: " + keyset + ", " + Arrays.toString( keys ),
                keyset.containsAll( keys ) );

        int[] other_array = new int[ keys.length + 1 ];
        System.arraycopy( keys, 0, other_array, 0, keys.length );
        other_array[other_array.length - 1] = 1138;
        assertFalse( "should not contain all. keyset: " + keyset + ", " + Arrays.toString( other_array ),
                keyset.containsAll( other_array ) );
    }


    public void testKeySetRetainAllCollection() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntSet keyset = map.keySet();
        for ( int i = 0; i < keyset.size(); i++ ) {
            assertTrue( keyset.contains( keys[i] ) );
        }
        assertFalse( keyset.isEmpty() );

        Collection<Integer> test_collection = new HashSet<Integer>();
        for ( int i = 0; i < element_count; i++ ) {
            test_collection.add( keys[i] );
        }
        keyset.retainAll( test_collection );
        assertFalse( keyset.isEmpty() );
        assertFalse( map.isEmpty() );

        // Reset map
        for ( int i = 0; i < element_count; i++ ) {
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        test_collection.remove( Integer.valueOf( keys[5] ) );
        keyset.retainAll( test_collection );
        assertEquals( element_count - 1, keyset.size() );
        assertEquals( element_count - 1, map.size() );
        assertFalse( keyset.contains( keys[5] ) );
        assertFalse( map.containsKey( keys[5] ) );
        

        // Reset map
        for ( int i = 0; i < element_count; i++ ) {
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        test_collection.add( Integer.valueOf( 1138 ) );
        keyset.retainAll( test_collection );
        
    }


    public void testKeySetRetainAllTCollection() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntSet keyset = map.keySet();
        for ( int i = 0; i < keyset.size(); i++ ) {
            assertTrue( keyset.contains( keys[i] ) );
        }
        assertFalse( keyset.isEmpty() );

        assertFalse( "keyset: " + keyset + ", should be unmodified.",
                keyset.retainAll( keyset ) );

        TIntCollection other = new TIntArrayList( keyset );
        assertFalse( "keyset: " + keyset + ", should be unmodified. other: " +
                     other, keyset.retainAll( other ) );

        other.remove( keys[5] );
        assertTrue( "keyset: " + keyset + ", should be modified. other: " +
                    other, keyset.retainAll( other ) );
        assertFalse( keyset.contains( keys[5] ) );
        assertFalse( map.containsKey( keys[5] ) );
        assertFalse( map.containsValue( vals[5] ) );
        assertTrue( "keyset: " + keyset + ", should contain all in other: " +
                    other, keyset.containsAll( other ) );
    }


    public void testKeySetRetainAllArray() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntSet keyset = map.keySet();
        for ( int i = 0; i < keyset.size(); i++ ) {
            assertTrue( keyset.contains( keys[i] ) );
        }
        assertFalse( keyset.isEmpty() );

        assertFalse( "keyset: " + keyset + ", should be unmodified. array: " +
                     Arrays.toString( keys ), keyset.retainAll( keys ) );

        int[] other = new int[element_count - 1];
        for ( int i = 0; i < element_count; i++ ) {
            if ( i < 5 ) {
                other[i] = i + 1;
            }
            if ( i > 5 ) {
                other[i - 1] = i + 1;
            }
        }
        assertTrue( "keyset: " + keyset + ", should be modified. array: " +
                    Arrays.toString( other ), keyset.retainAll( other ) );
    }


    public void testKeySetRemoveAllCollection() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntCollection keyset = map.keySet();
        for ( int i = 0; i < keyset.size(); i++ ) {
            assertTrue( keyset.contains( keys[i] ) );
        }
        assertFalse( keyset.isEmpty() );

        List<Integer> java_list = new ArrayList<Integer>();
        assertFalse( "collection: " + keyset + ", should contain all in list: " +
                     java_list, keyset.removeAll( java_list ) );

        java_list.add( keys[5] );
        assertTrue( "collection: " + keyset + ", should contain all in list: " +
                    java_list, keyset.removeAll( java_list ) );
        assertFalse( keyset.contains( keys[5] ) );
        assertFalse( map.containsKey( keys[5] ) );
        assertFalse( map.containsValue( vals[5] ) );

        java_list = new ArrayList<Integer>();
        for ( int key : keys ) {
            java_list.add( key );
        }
        assertTrue( "collection: " + keyset + ", should contain all in list: " +
                    java_list, keyset.removeAll( java_list ) );
        assertTrue( keyset.isEmpty() );
    }


    public void testKeySetRemoveAllTCollection() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntCollection keyset = map.keySet();
        for ( int i = 0; i < keyset.size(); i++ ) {
            assertTrue( keyset.contains( keys[i] ) );
        }
        assertFalse( keyset.isEmpty() );

        TIntCollection other = new TIntArrayList();
        assertFalse( "collection: " + keyset + ", should be unmodified.",
                keyset.removeAll( other ) );

        other = new TIntArrayList( keyset );
        other.remove( keys[5] );
        assertTrue( "collection: " + keyset + ", should be modified. other: " +
                    other, keyset.removeAll( other ) );
        assertEquals( 1, keyset.size() );
        for ( int i = 0; i < element_count; i++ ) {
            if ( i == 5 ) {
                assertTrue( keyset.contains( keys[i] ) );
                assertTrue( map.containsKey( keys[i] ) );
                assertTrue( map.containsValue( vals[i] ) );
            } else {
                assertFalse( keyset.contains( keys[i] ) );
                assertFalse( map.containsKey( keys[i] ) );
                assertFalse( map.containsValue( vals[i] ) );
            }
        }

        assertFalse( "collection: " + keyset + ", should be unmodified. other: " +
                     other, keyset.removeAll( other ) );

        assertTrue( "collection: " + keyset + ", should be modified. other: " +
                    other, keyset.removeAll( keyset ) );
        assertTrue( keyset.isEmpty() );
    }


    public void testKeySetRemoveAllArray() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntCollection keyset = map.keySet();
        for ( int i = 0; i < keyset.size(); i++ ) {
            assertTrue( keyset.contains( keys[i] ) );
        }
        assertFalse( keyset.isEmpty() );

        int[] other = {1138};
        assertFalse( "collection: " + keyset + ", should be unmodified. array: " +
                     Arrays.toString( vals ), keyset.removeAll( other ) );

        other = new int[element_count - 1];
        for ( int i = 0; i < element_count; i++ ) {
            if ( i < 5 ) {
                other[i] = i + 1;
            }
            if ( i > 5 ) {
                other[i - 1] = i + 1;
            }
        }
        assertTrue( "collection: " + keyset + ", should be modified. array: " +
                    Arrays.toString( other ), keyset.removeAll( other ) );
        assertEquals( 1, keyset.size() );
        for ( int i = 0; i < element_count; i++ ) {
            if ( i == 5 ) {
                assertTrue( keyset.contains( keys[i] ) );
                assertTrue( map.containsKey( keys[i] ) );
                assertTrue( map.containsValue( vals[i] ) );
            } else {
                assertFalse( keyset.contains( keys[i] ) );
                assertFalse( map.containsKey( keys[i] ) );
                assertFalse( map.containsValue( vals[i] ) );
            }
        }
    }


    public void testKeySetEqual() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntSet keyset = map.keySet();
        for ( int i = 0; i < keyset.size(); i++ ) {
            assertTrue( keyset.contains( keys[i] ) );
        }
        assertFalse( keyset.isEmpty() );

        TIntSet other = new TIntHashSet();
        other.addAll( keys );

        assertTrue( "sets incorrectly not equal: " + keyset + ", " + other,
                keyset.equals( other ) );

        int[] mismatched = {72, 49, 53, 1024, 999};
        TIntSet unequal = new TIntHashSet();
        unequal.addAll( mismatched );

        assertFalse( "sets incorrectly equal: " + keyset + ", " + unequal,
                keyset.equals( unequal ) );

        // Change length, different code branch
        unequal.add( 1 );
        assertFalse( "sets incorrectly equal: " + keyset + ", " + unequal,
                keyset.equals( unequal ) );

        //noinspection ObjectEqualsNull
        assertFalse( keyset.equals( null ) );
    }


    public void testKeySetHashCode() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntSet keyset = map.keySet();
        for ( int i = 0; i < keyset.size(); i++ ) {
            assertTrue( keyset.contains( keys[i] ) );
        }
        assertFalse( keyset.isEmpty() );

        assertEquals( keyset.hashCode(), keyset.hashCode() );

        TIntSet other = new TIntHashSet( keys );
        other.add( 1138 );
        assertTrue( keyset.hashCode() != other.hashCode() );
    }


    public void testKeys() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        // No argument
        int[] keys_array = map.keys();
        assertEquals( element_count, keys_array.length );
        TIntList keys_list = new TIntArrayList( keys_array );
        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( keys_list.contains( keys[i] ) );
        }

        // Zero length array
        keys_array = map.keys( new int[0] );
        assertEquals( element_count, keys_array.length );
        keys_list = new TIntArrayList( keys_array );
        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( keys_list.contains( keys[i] ) );
        }

        // appropriate length array
        keys_array = map.keys( new int[map.size()] );
        assertEquals( element_count, keys_array.length );
        keys_list = new TIntArrayList( keys_array );
        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( keys_list.contains( keys[i] ) );
        }

        // longer array
        keys_array = map.keys( new int[element_count * 2] );
        assertEquals( element_count * 2, keys_array.length );
        keys_list = new TIntArrayList( keys_array );
        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( keys_list.contains( keys[i] ) );
        }
        assertEquals( map.getNoEntryKey(), keys_array[element_count] );
    }


    @SuppressWarnings({"ToArrayCallWithZeroLengthArrayArgument"})
    public void testValueCollectionToArray() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        Collection<String> collection = map.valueCollection();
        for ( int i = 0; i < collection.size(); i++ ) {
            assertTrue( collection.contains( vals[i] ) );
        }
        assertFalse( collection.isEmpty() );

        Object[] values_array = collection.toArray();
        int count = 0;
        Iterator<String> iter = collection.iterator();
        while ( iter.hasNext() ) {
            String value = iter.next();
            assertTrue( collection.contains( value ) );
            assertEquals( values_array[count], value );
            count++;
        }

        values_array = collection.toArray( new String[0] );
        count = 0;
        iter = collection.iterator();
        while ( iter.hasNext() ) {
            String value = iter.next();
            assertTrue( collection.contains( value ) );
            assertEquals( values_array[count], value );
            count++;
        }

        values_array = collection.toArray( new String[collection.size()] );
        count = 0;
        iter = collection.iterator();
        while ( iter.hasNext() ) {
            String value = iter.next();
            assertTrue( collection.contains( value ) );
            assertEquals( values_array[count], value );
            count++;
        }

        values_array = collection.toArray( new String[collection.size() * 2] );
        count = 0;
        iter = collection.iterator();
        while ( iter.hasNext() ) {
            String value = iter.next();
            assertTrue( collection.contains( value ) );
            assertEquals( values_array[count], value );
            count++;
        }
        assertNull( values_array[collection.size()] );
        assertNull( values_array[collection.size()] );

        Collection<String> other = new ArrayList<String>( collection );
        assertFalse( collection.retainAll( other ) );
        other.remove( vals[5] );
        assertTrue( collection.retainAll( other ) );
        assertFalse( collection.contains( vals[5] ) );
        assertFalse( map.containsKey( keys[5] ) );

        collection.clear();
        assertTrue( collection.isEmpty() );
    }


    public void testValueCollectionAdds() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        Collection<String> collection = map.valueCollection();
        for ( int i = 0; i < collection.size(); i++ ) {
            assertTrue( collection.contains( vals[i] ) );
        }
        assertFalse( collection.isEmpty() );

        try {
            collection.add( "1138" );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }

        try {
            Set<String> test = new HashSet<String>();
            test.add( "1138" );
            collection.addAll( test );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }

        try {
            Collection<String> test = new ArrayList<String>();
            test.add( "1138" );
            collection.addAll( test );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }

        try {
            collection.addAll( Arrays.asList( vals ) );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }
    }


    public void testValueCollectionContainsAll() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        Collection<String> collection = map.valueCollection();
        for ( int i = 0; i < collection.size(); i++ ) {
            assertTrue( collection.contains( vals[i] ) );
        }
        assertFalse( collection.isEmpty() );

        List<String> java_list = new ArrayList<String>();
        java_list.addAll( Arrays.asList( vals ) );
        assertTrue( "collection: " + collection + ", should contain all in list: " +
                    java_list, collection.containsAll( java_list ) );
        java_list.add( String.valueOf( 1138 ) );
        assertFalse( "collection: " + collection + ", should not contain all in list: " +
                     java_list, collection.containsAll( java_list ) );

        List<CharSequence> number_list = new ArrayList<CharSequence>();
        for ( String value : vals ) {
            if ( value.equals( "5" ) ) {
                number_list.add( new StringBuilder().append( value ) );
            } else {
                number_list.add( String.valueOf( value ) );
            }
        }
        assertFalse( "collection: " + collection + ", should not contain all in list: " +
                     java_list, collection.containsAll( number_list ) );

        Collection<String> other = new ArrayList<String>( collection );
        assertTrue( "collection: " + collection + ", should contain all in other: " +
                    other, collection.containsAll( other ) );
        other.add( "1138" );
        assertFalse( "collection: " + collection + ", should not contain all in other: " +
                     other, collection.containsAll( other ) );
    }


    public void testValueCollectionRetainAllCollection() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        Collection<String> collection = map.valueCollection();
        for ( int i = 0; i < collection.size(); i++ ) {
            assertTrue( collection.contains( vals[i] ) );
        }
        assertFalse( collection.isEmpty() );

        List<String> java_list = new ArrayList<String>();
        java_list.addAll( Arrays.asList( vals ) );
        assertFalse( "collection: " + collection + ", should contain all in list: " +
                     java_list, collection.retainAll( java_list ) );

        java_list.remove( 5 );
        assertTrue( "collection: " + collection + ", should contain all in list: " +
                    java_list, collection.retainAll( java_list ) );
        assertFalse( collection.contains( vals[5] ) );
        assertFalse( map.containsKey( keys[5] ) );
        assertFalse( map.containsValue( vals[5] ) );
        assertTrue( "collection: " + collection + ", should contain all in list: " +
                    java_list, collection.containsAll( java_list ) );
    }


    public void testValueCollectionRetainAllTCollection() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        Collection<String> collection = map.valueCollection();
        for ( int i = 0; i < collection.size(); i++ ) {
            assertTrue( collection.contains( vals[i] ) );
        }
        assertFalse( collection.isEmpty() );

        assertFalse( "collection: " + collection + ", should be unmodified.",
                collection.retainAll( collection ) );

        Collection<String> other = new ArrayList<String>( collection );
        assertFalse( "collection: " + collection + ", should be unmodified. other: " +
                     other, collection.retainAll( other ) );

        other.remove( vals[5] );
        assertTrue( "collection: " + collection + ", should be modified. other: " +
                    other, collection.retainAll( other ) );
        assertFalse( collection.contains( vals[5] ) );
        assertFalse( map.containsKey( keys[5] ) );
        assertFalse( map.containsValue( vals[5] ) );
        assertTrue( "collection: " + collection + ", should contain all in other: " +
                    other, collection.containsAll( other ) );
    }


    public void testValueCollectionRemoveAllCollection() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        Collection<String> collection = map.valueCollection();
        for ( int i = 0; i < collection.size(); i++ ) {
            assertTrue( collection.contains( vals[i] ) );
        }
        assertFalse( collection.isEmpty() );

        List<String> java_list = new ArrayList<String>();
        assertFalse( "collection: " + collection + ", should contain all in list: " +
                     java_list, collection.removeAll( java_list ) );

        java_list.add( vals[5] );
        assertTrue( "collection: " + collection + ", should contain all in list: " +
                    java_list, collection.removeAll( java_list ) );
        assertFalse( collection.contains( vals[5] ) );
        assertFalse( map.containsKey( keys[5] ) );
        assertFalse( map.containsValue( vals[5] ) );

        java_list = new ArrayList<String>();
        java_list.addAll( Arrays.asList( vals ) );
        assertTrue( "collection: " + collection + ", should contain all in list: " +
                    java_list, collection.removeAll( java_list ) );
        assertTrue( collection.isEmpty() );
    }


    public void testValueCollectionRemoveAllTCollection() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        Collection<String> collection = map.valueCollection();
        for ( int i = 0; i < collection.size(); i++ ) {
            assertTrue( collection.contains( vals[i] ) );
        }
        assertFalse( collection.isEmpty() );

        Collection<String> other = new ArrayList<String>();
        assertFalse( "collection: " + collection + ", should be unmodified.",
                collection.removeAll( other ) );

        other = new ArrayList<String>( collection );
        other.remove( vals[5] );
        assertTrue( "collection: " + collection + ", should be modified. other: " +
                    other, collection.removeAll( other ) );
        assertEquals( 1, collection.size() );
        for ( int i = 0; i < element_count; i++ ) {
            if ( i == 5 ) {
                assertTrue( collection.contains( vals[i] ) );
                assertTrue( map.containsKey( keys[i] ) );
                assertTrue( map.containsValue( vals[i] ) );
            } else {
                assertFalse( collection.contains( vals[i] ) );
                assertFalse( map.containsKey( keys[i] ) );
                assertFalse( map.containsValue( vals[i] ) );
            }
        }

        assertFalse( "collection: " + collection + ", should be unmodified. other: " +
                     other, collection.removeAll( other ) );

        assertTrue( "collection: " + collection + ", should be modified. other: " +
                    other, collection.removeAll( collection ) );
        assertTrue( collection.isEmpty() );
    }


    public void testValues() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        // No argument
        Object[] values_object_array = map.values();
        assertEquals( element_count, values_object_array.length );
        List<Object> values_object_list = Arrays.asList( values_object_array );
        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( values_object_list.contains( vals[i] ) );
        }

        // Zero length array
        String[] values_array = map.values( new String[0] );
        assertEquals( element_count, values_array.length );
        List<String> values_list = Arrays.asList( values_array );
        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( values_list.contains( vals[i] ) );
        }

        // appropriate length array
        values_array = map.values( new String[map.size()] );
        assertEquals( element_count, values_array.length );
        values_list = Arrays.asList( values_array );
        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( values_list.contains( vals[i] ) );
        }

        // longer array
        values_array = map.values( new String[element_count * 2] );
        assertEquals( element_count * 2, values_array.length );
        values_list = Arrays.asList( values_array );
        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( values_list.contains( vals[i] ) );
        }
        assertEquals( null, values_array[element_count] );
    }


    public void testIterator() {
        TIntObjectHashMap<String> map = new TIntObjectHashMap<String>();

        TIntObjectIterator<String> iterator = map.iterator();
        assertFalse( iterator.hasNext() );

        map.put( 1, "one" );
        map.put( 2, "two" );

        iterator = map.iterator();
        assertTrue( iterator.hasNext() );
        iterator.advance();

        int first_key = iterator.key();
        assertNotNull( "key was null", first_key );
        assertTrue( "invalid key: " + first_key, first_key == 1 || first_key == 2 );
        if ( first_key == 1 ) {
            assertEquals( "one", iterator.value() );
        } else {
            assertEquals( "two", iterator.value() );
        }

        assertTrue( iterator.hasNext() );
        iterator.advance();
        int second_key = iterator.key();
        assertNotNull( "key was null", second_key );
        assertTrue( "invalid key: " + second_key, second_key == 1 || second_key == 2 );
        if ( second_key == 1 ) {
            assertEquals( "one", iterator.value() );
        } else {
            assertEquals( "two", iterator.value() );
        }
        assertFalse( first_key + ", " + second_key, first_key == second_key );

        assertFalse( iterator.hasNext() );

        // New Iterator
        iterator = map.iterator();
        iterator.advance();
        first_key = iterator.key();
        iterator.setValue( "1138" );
        assertEquals( "1138", iterator.value() );
        assertEquals( "1138", map.get( first_key ) );
    }


    public void testIteratorRemoval() {
        TIntObjectHashMap<String> map = new TIntObjectHashMap<String>();

        map.put( 1, "one" );
        map.put( 2, "two" );
        map.put( 3, "three" );
        map.put( 4, "four" );
        map.put( 5, "five" );
        map.put( 6, "six" );
        map.put( 7, "seven" );
        map.put( 8, "eight" );
        map.put( 9, "nine" );
        map.put( 10, "ten" );

        TIntObjectIterator<String> iterator = map.iterator();
        while ( map.size() > 5 && iterator.hasNext() ) {
            iterator.advance();
            int key = iterator.key();
            iterator.remove();
            assertFalse( "map still contains key: " + key, map.containsKey( key ) );
        }

        assertEquals( 5, map.size() );
        iterator = map.iterator();
        assertTrue( iterator.hasNext() );
        iterator.advance();
        assertTrue( iterator.hasNext() );
        iterator.advance();
        assertTrue( iterator.hasNext() );
        iterator.advance();
        assertTrue( iterator.hasNext() );
        iterator.advance();
        assertTrue( iterator.hasNext() );
        iterator.advance();
        assertFalse( iterator.hasNext() );

        iterator = map.iterator();
        int elements = map.size();
        while ( iterator.hasNext() ) {
            iterator.advance();
            int key = iterator.key();
            iterator.remove();
            assertFalse( "map still contains key: " + key, map.containsKey( key ) );
            elements--;
        }
        assertEquals( 0, elements );

        assertEquals( 0, map.size() );
        assertTrue( map.isEmpty() );
    }


    public void testIteratorRemoval2() {
        int element_count = 10000;
        int remaining = element_count / 2;

        TIntObjectHashMap<String> map =
                new TIntObjectHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );

        for ( int pass = 0; pass < 10; pass++ ) {
            Random r = new Random();
            for ( int i = 0; i <= element_count; i++ ) {
                map.put( Integer.valueOf( r.nextInt() ), String.valueOf( i ) );
            }

            TIntObjectIterator iterator = map.iterator();
            while ( map.size() > remaining && iterator.hasNext() ) {
                iterator.advance();
                iterator.remove();
            }
        }
    }


    public void testForEachKey() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map =
                new TIntObjectHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        class ForEach implements TIntProcedure {

            TIntList built = new TIntArrayList();


            public boolean execute( int value ) {
                built.add( value );
                return true;
            }


            TIntList getBuilt() {
                return built;
            }
        }

        ForEach foreach = new ForEach();
        map.forEachKey( foreach );
        TIntList built = foreach.getBuilt();
        TIntList keys_list = new TIntArrayList( map.keys( new int[map.size()] ) );
        assertEquals( keys_list, built );

        built.sort();
        keys_list.sort();
        assertEquals( keys_list, built );


        class ForEachFalse implements TIntProcedure {

            TIntList built = new TIntArrayList();


            public boolean execute( int value ) {
                built.add( value );
                return false;
            }


            TIntList getBuilt() {
                return built;
            }
        }

        ForEachFalse foreach_false = new ForEachFalse();
        map.forEachKey( foreach_false );
        built = foreach_false.getBuilt();
        keys_list = new TIntArrayList( map.keys( new int[map.size()] ) );
        assertEquals( 1, built.size() );
        assertEquals( keys_list.get( 0 ), built.get( 0 ) );
    }


    public void testForEachValue() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map =
                new TIntObjectHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        class ForEach implements TObjectProcedure<String> {

            List<String> built = new ArrayList<String>();


            public boolean execute( String value ) {
                built.add( value );
                return true;
            }


            List<String> getBuilt() {
                return built;
            }
        }

        ForEach foreach = new ForEach();
        map.forEachValue( foreach );
        List<String> built = foreach.getBuilt();
        List<String> values = Arrays.asList( map.values( new String[ 0 ] ) );
        assertEquals( values, built );

        Collections.sort( built );
        Collections.sort( values );
        assertEquals( values, built );


        class ForEachFalse implements TObjectProcedure<String> {

            List<String> built = new ArrayList<String>();


            public boolean execute( String value ) {
                built.add( value );
                return false;
            }


            List<String> getBuilt() {
                return built;
            }
        }

        ForEachFalse foreach_false = new ForEachFalse();
        map.forEachValue( foreach_false );
        built = foreach_false.getBuilt();
        values = Arrays.asList( map.values( new String[ 0 ] ) );
        assertEquals( 1, built.size() );
        assertEquals( values.get( 0 ), built.get( 0 ) );
    }


    public void testForEachEntry() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map =
                new TIntObjectHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        class ForEach implements TIntObjectProcedure<String> {

            TIntObjectMap<String> built = new TIntObjectHashMap<String>();


            public boolean execute( int key, String value ) {
                built.put( key, value );
                return true;
            }


            TIntObjectMap<String> getBuilt() {
                return built;
            }
        }

        ForEach foreach = new ForEach();
        map.forEachEntry( foreach );
        TIntObjectMap<String> built = foreach.getBuilt();
        assertEquals( map, built );


        class ForEachFalse implements TIntObjectProcedure<String> {

            TIntObjectMap<String> built = new TIntObjectHashMap<String>();


            public boolean execute( int key, String value ) {
                built.put( key, value );
                return false;
            }


            TIntObjectMap<String> getBuilt() {
                return built;
            }
        }

        ForEachFalse foreach_false = new ForEachFalse();
        map.forEachEntry( foreach_false );
        built = foreach_false.getBuilt();
        assertEquals( 1, built.size() );
        assertTrue( map.containsKey( built.keys()[0] ) );
        assertTrue( map.containsValue( built.values( new String[0] )[0] ) );
    }


    public void testRetain() {
        TIntObjectHashMap<String> map = new TIntObjectHashMap<String>();

        map.put( 1, "one" );
        map.put( 2, "two" );
        map.put( 3, "three" );
        map.put( 4, "four" );
        map.put( 5, "five" );
        map.put( 6, "six" );
        map.put( 7, "seven" );
        map.put( 8, "eight" );
        map.put( 9, "nine" );
        map.put( 10, "ten" );

        map.retainEntries( new TIntObjectProcedure<String>() {
            public boolean execute( int a, String b ) {
                return a > 5 && a <= 8;
            }
        } );

        assertEquals( 3, map.size() );
        assertFalse( map.containsValue( "one" ) );
        assertFalse( map.containsValue( "two" ) );
        assertFalse( map.containsValue( "three" ) );
        assertFalse( map.containsValue( "four" ) );
        assertFalse( map.containsValue( "five" ) );
        assertTrue( map.containsValue( "six" ) );
        assertTrue( map.containsValue( "seven" ) );
        assertTrue( map.containsValue( "eight" ) );
        assertFalse( map.containsValue( "nine" ) );
        assertFalse( map.containsValue( "ten" ) );

        map.put( 11, "eleven" );
        map.put( 12, "twelve" );
        map.put( 13, "thirteen" );
        map.put( 14, "fourteen" );
        map.put( 15, "fifteen" );
        map.put( 16, "sixteen" );
        map.put( 17, "seventeen" );
        map.put( 18, "eighteen" );
        map.put( 19, "nineteen" );
        map.put( 20, "twenty" );


        map.retainEntries( new TIntObjectProcedure<String>() {
            public boolean execute( int a, String b ) {
                return a > 15;
            }
        } );

        assertEquals( 5, map.size() );
        assertFalse( map.containsValue( "one" ) );
        assertFalse( map.containsValue( "two" ) );
        assertFalse( map.containsValue( "three" ) );
        assertFalse( map.containsValue( "four" ) );
        assertFalse( map.containsValue( "five" ) );
        assertFalse( map.containsValue( "six" ) );
        assertFalse( map.containsValue( "seven" ) );
        assertFalse( map.containsValue( "eight" ) );
        assertFalse( map.containsValue( "nine" ) );
        assertFalse( map.containsValue( "ten" ) );
        assertFalse( map.containsValue( "eleven" ) );
        assertFalse( map.containsValue( "twelve" ) );
        assertFalse( map.containsValue( "thirteen" ) );
        assertFalse( map.containsValue( "fourteen" ) );
        assertFalse( map.containsValue( "fifteen" ) );
        assertTrue( map.containsValue( "sixteen" ) );
        assertTrue( map.containsValue( "seventeen" ) );
        assertTrue( map.containsValue( "eighteen" ) );
        assertTrue( map.containsValue( "nineteen" ) );
        assertTrue( map.containsValue( "twenty" ) );


        map.retainEntries( new TIntObjectProcedure<String>() {
            public boolean execute( int a, String b ) {
                return false;
            }
        } );

        assertEquals( 0, map.size() );
    }


    public void testTransformValues() {
       int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map =
                new TIntObjectHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        map.transformValues( new TObjectFunction<String,String>() {
            public String execute( String value ) {
                return value + "/" + value;
            }
        } );

        for ( int i = 0; i < element_count; i++ ) {
            String expected = vals[i] + "/" + vals[i];
            assertEquals( expected, map.get( keys[i] ) );
        }
    }


    public void testEquals() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map =
                new TIntObjectHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntObjectHashMap<String> fully_specified =
                new TIntObjectHashMap<String>( 20, 0.75f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            fully_specified.put( keys[i], vals[i] );
        }
        assertEquals( map, fully_specified );

        assertFalse( "shouldn't equal random object", map.equals( new Object() ) );

        int key = 11010110;     // I thought I saw a two!
        assertNull( map.put( key, null ) );
        assertNull( fully_specified.put( key, null ) );
        assertTrue( "incorrectly not-equal map: " + map + "\nfully_specified: " + fully_specified,
                map.equals( fully_specified ) );
        assertTrue( "incorrectly not-equal map: " + map + "\nfully_specified: " + fully_specified,
                fully_specified.equals( map ) );

        assertNull( fully_specified.put( key, "non-null-value" ) );
        assertFalse( "incorrectly equal map: " + map + "\nfully_specified: " + fully_specified,
                map.equals( fully_specified ) );
        assertFalse( "incorrectly equal map: " + map + "\nfully_specified: " + fully_specified,
                fully_specified.equals( map ) );

        assertNull( fully_specified.put( key + 1, "blargh" ) );
        assertFalse( "incorrectly equal map: " + map + "\nfully_specified: " + fully_specified,
                map.equals( fully_specified ) );
    }


    public void testHashCode() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];
        int counter = 0;

        TIntObjectMap<String> map =
                new TIntObjectHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++, counter++ ) {
            keys[i] = counter + 1;
            vals[i] = Integer.toString( counter + 1 );
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );
        assertEquals( map.hashCode(), map.hashCode() );

        Map<String, TIntObjectMap<String>> string_tmap_map =
                new HashMap<String, TIntObjectMap<String>>();
        string_tmap_map.put( "first", map );
        string_tmap_map.put( "second", map );
        assertSame( map, string_tmap_map.get( "first" ) );
        assertSame( map, string_tmap_map.get( "second" ) );

        TIntObjectMap<String> map2 =
                new TIntObjectHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++, counter++ ) {
            keys[i] = counter + 1;
            vals[i] = Integer.toString( counter + 1 );
            map2.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map2.size() );
        assertEquals( map2.hashCode(), map2.hashCode() );

        TIntObjectMap<String> map3 =
                new TIntObjectHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++, counter++ ) {
            keys[i] = counter + 1;
            vals[i] = Integer.toString( counter + 1 );
            map3.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map3.size() );
        assertEquals( map3.hashCode(), map3.hashCode() );

        assertFalse( "hashcodes are unlikely equal.  map: " + map + " (" + map.hashCode() +
                     ")\nmap2: " + map2 + " (" + map2.hashCode() + ")",
                map.hashCode() == map2.hashCode() );
        assertFalse( "hashcodes are unlikely equal.  map: " + map + " (" + map.hashCode() +
                     ")\nmap3: " + map3 + " (" + map3.hashCode() + ")",
                map.hashCode() == map3.hashCode() );
        assertFalse( "hashcodes are unlikely equal.  map2: " + map2 + " (" + map2.hashCode() + 
                     ")\nmap3: " + map3 + " (" + map3.hashCode() + ")",
                map2.hashCode() == map3.hashCode() );

        Map<TIntObjectMap<String>, String> tmap_string_map =
                new HashMap<TIntObjectMap<String>, String>();
        tmap_string_map.put( map, "map1" );
        tmap_string_map.put( map2, "map2" );
        tmap_string_map.put( map3, "map3" );
        assertEquals( "map1", tmap_string_map.get( map ) );
        assertEquals( "map2", tmap_string_map.get( map2 ) );
        assertEquals( "map3", tmap_string_map.get( map3 ) );
    }


    @SuppressWarnings({"unchecked"})
    public void testSerialize() throws Exception {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> map = new TIntObjectHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = i + 1;
            vals[i] = Integer.toString( i + 1 );
            map.put( keys[i], vals[i] );
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( map );

        ByteArrayInputStream bias = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bias );

        TIntObjectMap<String> deserialized = (TIntObjectMap<String>) ois.readObject();

        assertEquals( map, deserialized );
    }


    public void testToString() {
        TIntObjectHashMap<String> m = new TIntObjectHashMap<String>();
        m.put( 11, "One" );
        m.put( 22, "Two" );

        String to_string = m.toString();
        assertTrue( to_string, to_string.equals( "{11=One,22=Two}" )
                               || to_string.equals( "{22=Two,11=One}" ) );
    }


    /**
     * Test for tracking issue #1204014. +0.0 and -0.0 have different bit patterns, but
     * should be counted the same as keys in a map. Checks for doubles and floats.
     */
    public void testFloatZeroHashing() {
        TDoubleObjectHashMap<String> po_double_map = new TDoubleObjectHashMap<String>();
        TDoubleIntHashMap pp_double_map = new TDoubleIntHashMap();
        TFloatObjectHashMap<String> po_float_map = new TFloatObjectHashMap<String>();
        TFloatIntHashMap pp_float_map = new TFloatIntHashMap();

        final double zero_double = 0.0;
        final double negative_zero_double = -zero_double;
        final float zero_float = 0.0f;
        final float negative_zero_float = -zero_float;

        // Sanity check... make sure I'm really creating two different values.
        final String zero_bits_double =
                Long.toBinaryString( Double.doubleToLongBits( zero_double ) );
        final String negative_zero_bits_double =
                Long.toBinaryString( Double.doubleToLongBits( negative_zero_double ) );
        assertFalse( zero_bits_double + " == " + negative_zero_bits_double,
                zero_bits_double.equals( negative_zero_bits_double ) );

        final String zero_bits_float =
                Integer.toBinaryString( Float.floatToIntBits( zero_float ) );
        final String negative_zero_bits_float =
                Integer.toBinaryString( Float.floatToIntBits( negative_zero_float ) );
        assertFalse( zero_bits_float + " == " + negative_zero_bits_float,
                zero_bits_float.equals( negative_zero_bits_float ) );


        po_double_map.put( zero_double, "Zero" );
        po_double_map.put( negative_zero_double, "Negative Zero" );

        pp_double_map.put( zero_double, 0 );
        pp_double_map.put( negative_zero_double, -1 );

        po_float_map.put( zero_float, "Zero" );
        po_float_map.put( negative_zero_float, "Negative Zero" );

        pp_float_map.put( zero_float, 0 );
        pp_float_map.put( negative_zero_float, -1 );


        assertEquals( 1, po_double_map.size() );
        assertEquals( po_double_map.get( zero_double ), "Negative Zero" );
        assertEquals( po_double_map.get( negative_zero_double ), "Negative Zero" );

        assertEquals( 1, pp_double_map.size() );
        assertEquals( pp_double_map.get( zero_double ), -1 );
        assertEquals( pp_double_map.get( negative_zero_double ), -1 );

        assertEquals( 1, po_float_map.size() );
        assertEquals( po_float_map.get( zero_float ), "Negative Zero" );
        assertEquals( po_float_map.get( negative_zero_float ), "Negative Zero" );

        assertEquals( 1, pp_float_map.size() );
        assertEquals( pp_float_map.get( zero_float ), -1 );
        assertEquals( pp_float_map.get( negative_zero_float ), -1 );


        po_double_map.put( zero_double, "Zero" );
        pp_double_map.put( zero_double, 0 );
        po_float_map.put( zero_float, "Zero" );
        pp_float_map.put( zero_float, 0 );


        assertEquals( 1, po_double_map.size() );
        assertEquals( po_double_map.get( zero_double ), "Zero" );
        assertEquals( po_double_map.get( negative_zero_double ), "Zero" );

        assertEquals( 1, pp_double_map.size() );
        assertEquals( pp_double_map.get( zero_double ), 0 );
        assertEquals( pp_double_map.get( negative_zero_double ), 0 );

        assertEquals( 1, po_float_map.size() );
        assertEquals( po_float_map.get( zero_float ), "Zero" );
        assertEquals( po_float_map.get( negative_zero_float ), "Zero" );

        assertEquals( 1, pp_float_map.size() );
        assertEquals( pp_float_map.get( zero_float ), 0 );
        assertEquals( pp_float_map.get( negative_zero_float ), 0 );
    }
}
