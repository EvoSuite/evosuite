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
import gnu.trove.function.TIntFunction;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectIntProcedure;
import gnu.trove.procedure.TObjectProcedure;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;



/**
 *
 */
public class TObjectPrimitiveHashMapTest extends TestCase {

    public TObjectPrimitiveHashMapTest( String name ) {
        super( name );
    }


    public void testConstructors() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }

        TObjectIntHashMap<String> capacity =
                new TObjectIntHashMap<String>( 20 );
        for ( int i = 0; i < element_count; i++ ) {
            capacity.put( keys[i], vals[i] );
        }
        assertEquals( map, capacity );

        TObjectIntHashMap<String> cap_and_factor =
                new TObjectIntHashMap<String>( 20, 0.75f );
        for ( int i = 0; i < element_count; i++ ) {
            cap_and_factor.put( keys[i], vals[i] );
        }
        assertEquals( map, cap_and_factor );

        TObjectIntHashMap<String> fully_specified =
                new TObjectIntHashMap<String>( 20, 0.75f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            fully_specified.put( keys[i], vals[i] );
        }
        assertEquals( map, fully_specified );

        TObjectIntHashMap<String> copy =
                new TObjectIntHashMap<String>( fully_specified );
        assertEquals( map, copy );
    }


    public void testContainsKey() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }

        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( "Key should be present: " + keys[i] + ", map: " + map,
                    map.containsKey( keys[i] ) );
        }

        String key = "1138";
        assertFalse( "Key should not be present: " + key + ", map: " + map,
                map.containsKey( key ) );

        assertFalse( "Random object should not be present in map: " + map,
                map.containsKey( new Object() ) );
    }


    public void testContainsValue() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }

        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( "Value should be present: " + vals[i] + ", map: " + map,
                    map.containsValue( vals[i] ) );
        }

        int val = 1138;
        assertFalse( "Key should not be present: " + val + ", map: " + map,
                map.containsValue( val ) );
    }


    public void testPutIfAbsent() {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<String>();

        map.put( "One", 1 );
        map.put( "Two", 2 );
        map.put( "Three", 3 );

        assertEquals( 1, map.putIfAbsent( "One", 2 ) );
        assertEquals( 1, map.get( "One" ) );
        assertEquals( 0, map.putIfAbsent( "Nine", 9 ) );
        assertEquals( 9, map.get( "Nine" ) );
    }


    public void testRemove() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
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
                        map.get( keys[i] ) == map.getNoEntryValue() );
            } else {
                assertTrue( "Key should still be in map: " + keys[i] + ", map: " + map,
                        map.get( keys[i] ) == vals[i] );
            }
        }
    }


    public void testPutAllMap() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> control = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            control.put( keys[i], vals[i] );
        }

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();

        Map<String, Integer> source = new HashMap<String, Integer>();
        for ( int i = 0; i < element_count; i++ ) {
            source.put( keys[i], vals[i] );
        }

        map.putAll( source );
        assertEquals( control, map );
    }


    public void testPutAll() throws Exception {
        TObjectIntHashMap<String> t = new TObjectIntHashMap<String>();
        TObjectIntHashMap<String> m = new TObjectIntHashMap<String>();
        m.put( "one", 2 );
        m.put( "two", 4 );
        m.put( "three", 6 );

        t.put( "four", 5 );
        assertEquals( 1, t.size() );

        t.putAll( m );
        assertEquals( 4, t.size() );
        assertEquals( 4, t.get( "two" ) );
    }


    public void testClear() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        map.clear();
        assertTrue( map.isEmpty() );
        assertEquals( 0, map.size() );

        assertEquals( map.getNoEntryValue(), map.get( keys[5] ) );
    }


    @SuppressWarnings({"ToArrayCallWithZeroLengthArrayArgument"})
    public void testKeySet() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        Set<String> keyset = map.keySet();
        for ( int i = 0; i < keyset.size(); i++ ) {
            assertTrue( keyset.contains( keys[i] ) );
        }
        assertFalse( keyset.isEmpty() );

        Object[] keys_obj_array = keyset.toArray();
        int count = 0;
        Iterator<String> iter = keyset.iterator();
        while ( iter.hasNext() ) {
            String key = iter.next();
            assertTrue( keyset.contains( key ) );
            assertEquals( keys_obj_array[count], key );
            count++;
        }

        String[] keys_array = keyset.toArray( new String[0] );
        count = 0;
        iter = keyset.iterator();
        while ( iter.hasNext() ) {
            String key = iter.next();
            assertTrue( keyset.contains( key ) );
            assertEquals( keys_array[count], key );
            count++;
        }

        keys_array = keyset.toArray( new String[keyset.size()] );
        count = 0;
        iter = keyset.iterator();
        while ( iter.hasNext() ) {
            String key = iter.next();
            assertTrue( keyset.contains( key ) );
            assertEquals( keys_array[count], key );
            count++;
        }

        keys_array = keyset.toArray( new String[keyset.size() * 2] );
        count = 0;
        iter = keyset.iterator();
        while ( iter.hasNext() ) {
            String key = iter.next();
            assertTrue( keyset.contains( key ) );
            assertEquals( keys_array[count], key );
            count++;
        }
        assertNull( keys_array[keyset.size()] );

        Set<String> other = new HashSet<String>( keyset );
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
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        Set<String> keyset = map.keySet();
        for ( int i = 0; i < keyset.size(); i++ ) {
            assertTrue( keyset.contains( keys[i] ) );
        }
        assertFalse( keyset.isEmpty() );

        try {
            keyset.add( "explosions!" );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }

        try {
            Set<String> test = new HashSet<String>();
            test.add( "explosions!" );
            keyset.addAll( test );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }
    }


    public void testKeys() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        // No argument
        Object[] keys_object_array = map.keys();
        assertEquals( element_count, keys_object_array.length );
        List<Object> keys_object_list = Arrays.asList( keys_object_array );
        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( keys_object_list.contains( keys[i] ) );
        }

        // Zero length array
        String[] keys_string_array = map.keys( new String[0] );
        assertEquals( element_count, keys_string_array.length );
        List<String> keys_string_list = Arrays.asList( keys_string_array );
        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( keys_string_list.contains( keys[i] ) );
        }

        // appropriate length array
        keys_string_array = map.keys( new String[map.size()] );
        assertEquals( element_count, keys_string_array.length );
        keys_string_list = Arrays.asList( keys_string_array );
        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( keys_string_list.contains( keys[i] ) );
        }

        // longer array
        keys_string_array = map.keys( new String[element_count * 2] );
        assertEquals( element_count * 2, keys_string_array.length );
        keys_string_list = Arrays.asList( keys_string_array );
        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( keys_string_list.contains( keys[i] ) );
        }
        assertNull( keys_string_array[element_count] );
    }


    public void testValueCollectionToArray() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map =
                new TObjectIntHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntCollection collection = map.valueCollection();
        for ( int i = 0; i < collection.size(); i++ ) {
            assertTrue( collection.contains( vals[i] ) );
        }
        assertFalse( collection.isEmpty() );

        int[] values_array = collection.toArray();
        int count = 0;
        TIntIterator iter = collection.iterator();
        while ( iter.hasNext() ) {
            int value = iter.next();
            assertTrue( collection.contains( value ) );
            assertEquals( values_array[count], value );
            count++;
        }

        values_array = collection.toArray( new int[0] );
        count = 0;
        iter = collection.iterator();
        while ( iter.hasNext() ) {
            int value = iter.next();
            assertTrue( collection.contains( value ) );
            assertEquals( values_array[count], value );
            count++;
        }

        values_array = collection.toArray( new int[collection.size()] );
        count = 0;
        iter = collection.iterator();
        while ( iter.hasNext() ) {
            int value = iter.next();
            assertTrue( collection.contains( value ) );
            assertEquals( values_array[count], value );
            count++;
        }

        values_array = collection.toArray( new int[collection.size() * 2] );
        count = 0;
        iter = collection.iterator();
        while ( iter.hasNext() ) {
            int value = iter.next();
            assertTrue( collection.contains( value ) );
            assertEquals( values_array[count], value );
            count++;
        }
        assertEquals( collection.getNoEntryValue(), values_array[collection.size()] );
        assertEquals( map.getNoEntryValue(), values_array[collection.size()] );

        TIntCollection other = new TIntArrayList( collection );
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
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntCollection collection = map.valueCollection();
        for ( int i = 0; i < collection.size(); i++ ) {
            assertTrue( collection.contains( vals[i] ) );
        }
        assertFalse( collection.isEmpty() );

        try {
            collection.add( 1138 );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }

        try {
            Set<Integer> test = new HashSet<Integer>();
            test.add( Integer.valueOf( 1138 ) );
            collection.addAll( test );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }

        try {
            TIntCollection test = new TIntArrayList();
            test.add( 1138 );
            collection.addAll( test );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }

        try {
            collection.addAll( vals );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }
    }


    public void testValueCollectionContainsAll() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntCollection collection = map.valueCollection();
        for ( int i = 0; i < collection.size(); i++ ) {
            assertTrue( collection.contains( vals[i] ) );
        }
        assertFalse( collection.isEmpty() );

        List<Integer> java_list = new ArrayList<Integer>();
        for ( int value : vals ) {
            java_list.add( value );
        }
        assertTrue( "collection: " + collection + ", should contain all in list: " +
                    java_list, collection.containsAll( java_list ) );
        java_list.add( Integer.valueOf( 1138 ) );
        assertFalse( "collection: " + collection + ", should not contain all in list: " +
                     java_list, collection.containsAll( java_list ) );

        List<Number> number_list = new ArrayList<Number>();
        for ( int value : vals ) {
            if ( value == 5 ) {
                number_list.add( Long.valueOf( value ) );
            } else {
                number_list.add( Integer.valueOf( value ) );
            }
        }
        assertFalse( "collection: " + collection + ", should not contain all in list: " +
                     java_list, collection.containsAll( number_list ) );

        TIntCollection other = new TIntArrayList( collection );
        assertTrue( "collection: " + collection + ", should contain all in other: " +
                    other, collection.containsAll( other ) );
        other.add( 1138 );
        assertFalse( "collection: " + collection + ", should not contain all in other: " +
                     other, collection.containsAll( other ) );

        assertTrue( "collection: " + collection + ", should contain all in array: " +
                    Arrays.toString( vals ), collection.containsAll( vals ) );
        int[] other_array = new int[vals.length + 1];
        other_array[other_array.length - 1] = 1138;
        assertFalse( "collection: " + collection + ", should not contain all in array: " +
                     Arrays.toString( vals ), collection.containsAll( other_array ) );
    }


    public void testValueCollectionRetainAllCollection() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntCollection collection = map.valueCollection();
        for ( int i = 0; i < collection.size(); i++ ) {
            assertTrue( collection.contains( vals[i] ) );
        }
        assertFalse( collection.isEmpty() );

        List<Integer> java_list = new ArrayList<Integer>();
        for ( int value : vals ) {
            java_list.add( value );
        }
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
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntCollection collection = map.valueCollection();
        for ( int i = 0; i < collection.size(); i++ ) {
            assertTrue( collection.contains( vals[i] ) );
        }
        assertFalse( collection.isEmpty() );

        assertFalse( "collection: " + collection + ", should be unmodified.",
                collection.retainAll( collection ) );

        TIntCollection other = new TIntArrayList( collection );
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


    public void testValueCollectionRetainAllArray() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntCollection collection = map.valueCollection();
        for ( int i = 0; i < collection.size(); i++ ) {
            assertTrue( collection.contains( vals[i] ) );
        }
        assertFalse( collection.isEmpty() );

        assertFalse( "collection: " + collection + ", should be unmodified. array: " +
                     Arrays.toString( vals ), collection.retainAll( vals ) );

        int[] other = new int[element_count - 1];
        for ( int i = 0; i < element_count; i++ ) {
            if ( i < 5 ) {
                other[i] = i + 1;
            }
            if ( i > 5 ) {
                other[i - 1] = i + 1;
            }
        }
        assertTrue( "collection: " + collection + ", should be modified. array: " +
                    Arrays.toString( other ), collection.retainAll( other ) );
    }


    public void testValueCollectionRemoveAllCollection() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntCollection collection = map.valueCollection();
        for ( int i = 0; i < collection.size(); i++ ) {
            assertTrue( collection.contains( vals[i] ) );
        }
        assertFalse( collection.isEmpty() );

        List<Integer> java_list = new ArrayList<Integer>();
        assertFalse( "collection: " + collection + ", should contain all in list: " +
                     java_list, collection.removeAll( java_list ) );

        java_list.add( vals[5] );
        assertTrue( "collection: " + collection + ", should contain all in list: " +
                    java_list, collection.removeAll( java_list ) );
        assertFalse( collection.contains( vals[5] ) );
        assertFalse( map.containsKey( keys[5] ) );
        assertFalse( map.containsValue( vals[5] ) );

        java_list = new ArrayList<Integer>();
        for ( int value : vals ) {
            java_list.add( value );
        }
        assertTrue( "collection: " + collection + ", should contain all in list: " +
                    java_list, collection.removeAll( java_list ) );
        assertTrue( collection.isEmpty() );
    }


    public void testValueCollectionRemoveAllTCollection() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntCollection collection = map.valueCollection();
        for ( int i = 0; i < collection.size(); i++ ) {
            assertTrue( collection.contains( vals[i] ) );
        }
        assertFalse( collection.isEmpty() );

        TIntCollection other = new TIntArrayList();
        assertFalse( "collection: " + collection + ", should be unmodified.",
                collection.removeAll( other ) );

        other = new TIntArrayList( collection );
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


    public void testValueCollectionRemoveAllArray() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map = new TObjectIntHashMap<String>();
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TIntCollection collection = map.valueCollection();
        for ( int i = 0; i < collection.size(); i++ ) {
            assertTrue( collection.contains( vals[i] ) );
        }
        assertFalse( collection.isEmpty() );

        int[] other = {1138};
        assertFalse( "collection: " + collection + ", should be unmodified. array: " +
                     Arrays.toString( vals ), collection.removeAll( other ) );

        other = new int[element_count - 1];
        for ( int i = 0; i < element_count; i++ ) {
            if ( i < 5 ) {
                other[i] = i + 1;
            }
            if ( i > 5 ) {
                other[i - 1] = i + 1;
            }
        }
        assertTrue( "collection: " + collection + ", should be modified. array: " +
                    Arrays.toString( other ), collection.removeAll( other ) );
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
    }


    public void testValues() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map =
                new TObjectIntHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        // No argument
        int[] values_array = map.values();
        assertEquals( element_count, values_array.length );
        TIntList values_list = new TIntArrayList( values_array );
        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( values_list.contains( vals[i] ) );
        }

        // Zero length array
        values_array = map.values( new int[0] );
        assertEquals( element_count, values_array.length );
        values_list = new TIntArrayList( values_array );
        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( values_list.contains( vals[i] ) );
        }

        // appropriate length array
        values_array = map.values( new int[map.size()] );
        assertEquals( element_count, values_array.length );
        values_list = new TIntArrayList( values_array );
        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( values_list.contains( vals[i] ) );
        }

        // longer array
        values_array = map.values( new int[element_count * 2] );
        assertEquals( element_count * 2, values_array.length );
        values_list = new TIntArrayList( values_array );
        for ( int i = 0; i < element_count; i++ ) {
            assertTrue( values_list.contains( vals[i] ) );
        }
        assertEquals( map.getNoEntryValue(), values_array[element_count] );
    }


    public void testIterator() {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<String>();

        TObjectIntIterator<String> iterator = map.iterator();
        assertFalse( iterator.hasNext() );

        map.put( "one", 1 );
        map.put( "two", 2 );

        iterator = map.iterator();
        assertTrue( iterator.hasNext() );
        iterator.advance();

        String first_key = iterator.key();
        assertNotNull( "key was null", first_key );
        assertTrue( "invalid key: " + first_key,
                first_key.equals( "one" ) || first_key.equals( "two" ) );
        if ( first_key.equals( "one" ) ) {
            assertEquals( 1, iterator.value() );
        } else {
            assertEquals( 2, iterator.value() );
        }

        assertTrue( iterator.hasNext() );
        iterator.advance();
        String second_key = iterator.key();
        assertNotNull( "key was null", second_key );
        assertTrue( "invalid key: " + second_key,
                second_key.equals( "one" ) || second_key.equals( "two" ) );
        if ( second_key.equals( "one" ) ) {
            assertEquals( 1, iterator.value() );
        } else {
            assertEquals( 2, iterator.value() );
        }
        assertFalse( first_key + ", " + second_key, first_key.equals( second_key ) );

        assertFalse( iterator.hasNext() );

        // New Iterator
        iterator = map.iterator();
        iterator.advance();
        first_key = iterator.key();
        iterator.setValue( 1138 );
        assertEquals( 1138, iterator.value() );
        assertEquals( 1138, map.get( first_key ) );
    }


    public void testIteratorRemoval() {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<String>();

        map.put( "one", 1 );
        map.put( "two", 2 );
        map.put( "three", 3 );
        map.put( "four", 4 );
        map.put( "five", 5 );
        map.put( "six", 6 );
        map.put( "seven", 7 );
        map.put( "eight", 8 );
        map.put( "nine", 9 );
        map.put( "ten", 10 );

        TObjectIntIterator<String> iterator = map.iterator();
        while ( map.size() > 5 && iterator.hasNext() ) {
            iterator.advance();
            String key = iterator.key();
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
            String key = iterator.key();
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

        TObjectIntHashMap<String> map =
                new TObjectIntHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );

        for ( int pass = 0; pass < 10; pass++ ) {
            Random r = new Random();
            for ( int i = 0; i <= element_count; i++ ) {
                map.put( String.valueOf( r.nextInt() ), i );
            }

            TObjectIntIterator iterator = map.iterator();
            while ( map.size() > remaining && iterator.hasNext() ) {
                iterator.advance();
                iterator.remove();
            }
        }
    }


    public void testIncrement() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map =
                new TObjectIntHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        assertFalse( map.increment( "non-existant" ) );
        assertTrue( map.increment( "1" ) );
        assertEquals( 2, map.get( "1" ) );
    }


    public void testAdjustValue() {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<String>();

        map.put( "one", 1 );

        boolean changed = map.adjustValue( "one", 1 );
        assertTrue( changed );
        assertEquals( 2, map.get( "one" ) );

        changed = map.adjustValue( "one", 5 );
        assertTrue( changed );
        assertEquals( 7, map.get( "one" ) );

        changed = map.adjustValue( "one", -3 );
        assertTrue( changed );
        assertEquals( 4, map.get( "one" ) );

        changed = map.adjustValue( "two", 1 );
        assertFalse( changed );
        assertFalse( map.containsKey( "two" ) );
    }


    public void testAdjustOrPutValue() {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<String>();

        map.put( "one", 1 );

        long new_value = map.adjustOrPutValue( "one", 1, 100 );
        assertEquals( 2, new_value );
        assertEquals( 2, map.get( "one" ) );

        new_value = map.adjustOrPutValue( "one", 5, 100 );
        assertEquals( 7, new_value );
        assertEquals( 7, map.get( "one" ) );

        new_value = map.adjustOrPutValue( "one", -3, 100 );
        assertEquals( 4, new_value );
        assertEquals( 4, map.get( "one" ) );

        new_value = map.adjustOrPutValue( "two", 1, 100 );
        assertEquals( 100, new_value );
        assertTrue( map.containsKey( "two" ) );
        assertEquals( 100, map.get( "two" ) );

        new_value = map.adjustOrPutValue( "two", 1, 100 );
        assertEquals( 101, new_value );
        assertEquals( 101, map.get( "two" ) );
    }


    public void testForEachKey() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map =
                new TObjectIntHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
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
        map.forEachKey( foreach );
        List<String> built = foreach.getBuilt();
        List<String> keys_list = Arrays.asList( map.keys( new String[map.size()] ) );
        assertEquals( keys_list, built );

        Collections.sort( built );
        Collections.sort( keys_list );
        assertEquals( keys_list, built );


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
        map.forEachKey( foreach_false );
        built = foreach_false.getBuilt();
        keys_list = Arrays.asList( map.keys( new String[map.size()] ) );
        assertEquals( 1, built.size() );
        assertEquals( keys_list.get( 0 ), built.get( 0 ) );
    }


    public void testForEachValue() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map =
                new TObjectIntHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
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
        map.forEachValue( foreach );
        TIntList built = foreach.getBuilt();
        TIntList values = new TIntArrayList( map.values() );
        assertEquals( values, built );

        built.sort();
        values.sort();
        assertEquals( values, built );


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
        map.forEachValue( foreach_false );
        built = foreach_false.getBuilt();
        values = new TIntArrayList( map.values() );
        assertEquals( 1, built.size() );
        assertEquals( values.get( 0 ), built.get( 0 ) );
    }


    public void testForEachEntry() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map =
                new TObjectIntHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        class ForEach implements TObjectIntProcedure<String> {

            TObjectIntMap<String> built = new TObjectIntHashMap<String>();


            public boolean execute( String key, int value ) {
                built.put( key, value );
                return true;
            }


            TObjectIntMap<String> getBuilt() {
                return built;
            }
        }

        ForEach foreach = new ForEach();
        map.forEachEntry( foreach );
        TObjectIntMap<String> built = foreach.getBuilt();
        assertEquals( map, built );


        class ForEachFalse implements TObjectIntProcedure<String> {

            TObjectIntMap<String> built = new TObjectIntHashMap<String>();


            public boolean execute( String key, int value ) {
                built.put( key, value );
                return false;
            }


            TObjectIntMap<String> getBuilt() {
                return built;
            }
        }

        ForEachFalse foreach_false = new ForEachFalse();
        map.forEachEntry( foreach_false );
        built = foreach_false.getBuilt();
        assertEquals( 1, built.size() );
        assertTrue( map.containsKey( built.keys()[0] ) );
        assertTrue( map.containsValue( built.values()[0] ) );
    }


    public void testRetain() {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<String>();

        map.put( "one", 1 );
        map.put( "two", 2 );
        map.put( "three", 3 );
        map.put( "four", 4 );
        map.put( "five", 5 );
        map.put( "six", 6 );
        map.put( "seven", 7 );
        map.put( "eight", 8 );
        map.put( "nine", 9 );
        map.put( "ten", 10 );

        map.retainEntries( new TObjectIntProcedure<String>() {
            public boolean execute( String a, int b ) {
                return b > 5 && b <= 8;

            }
        } );

        assertEquals( 3, map.size() );
        assertFalse( map.containsKey( "one" ) );
        assertFalse( map.containsKey( "two" ) );
        assertFalse( map.containsKey( "three" ) );
        assertFalse( map.containsKey( "four" ) );
        assertFalse( map.containsKey( "five" ) );
        assertTrue( map.containsKey( "six" ) );
        assertTrue( map.containsKey( "seven" ) );
        assertTrue( map.containsKey( "eight" ) );
        assertFalse( map.containsKey( "nine" ) );
        assertFalse( map.containsKey( "ten" ) );

        map.put( "eleven", 11 );
        map.put( "twelve", 12 );
        map.put( "thirteen", 13 );
        map.put( "fourteen", 14 );
        map.put( "fifteen", 15 );
        map.put( "sixteen", 16 );
        map.put( "seventeen", 17 );
        map.put( "eighteen", 18 );
        map.put( "nineteen", 19 );
        map.put( "twenty", 20 );


        map.retainEntries( new TObjectIntProcedure<String>() {
            public boolean execute( String a, int b ) {
                return b > 15;
            }
        } );

        assertEquals( 5, map.size() );
        assertFalse( map.containsKey( "one" ) );
        assertFalse( map.containsKey( "two" ) );
        assertFalse( map.containsKey( "three" ) );
        assertFalse( map.containsKey( "four" ) );
        assertFalse( map.containsKey( "five" ) );
        assertFalse( map.containsKey( "six" ) );
        assertFalse( map.containsKey( "seven" ) );
        assertFalse( map.containsKey( "eight" ) );
        assertFalse( map.containsKey( "nine" ) );
        assertFalse( map.containsKey( "ten" ) );
        assertFalse( map.containsKey( "eleven" ) );
        assertFalse( map.containsKey( "twelve" ) );
        assertFalse( map.containsKey( "thirteen" ) );
        assertFalse( map.containsKey( "fourteen" ) );
        assertFalse( map.containsKey( "fifteen" ) );
        assertTrue( map.containsKey( "sixteen" ) );
        assertTrue( map.containsKey( "seventeen" ) );
        assertTrue( map.containsKey( "eighteen" ) );
        assertTrue( map.containsKey( "nineteen" ) );
        assertTrue( map.containsKey( "twenty" ) );


        map.retainEntries( new TObjectIntProcedure<String>() {
            public boolean execute( String a, int b ) {
                return false;
            }
        } );

        assertEquals( 0, map.size() );
    }


    public void testTransformValues() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map =
                new TObjectIntHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        map.transformValues( new TIntFunction() {
            public int execute( int value ) {
                return value * value;
            }
        } );

        for ( int i = 0; i < element_count; i++ ) {
            int expected = vals[i] * vals[i];
            assertEquals( expected, map.get( keys[i] ) );
        }
    }


    public void testEquals() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];

        TObjectIntMap<String> map =
                new TObjectIntHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = Integer.toString( i + 1 );
            vals[i] = i + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );

        TObjectIntHashMap<String> fully_specified =
                new TObjectIntHashMap<String>( 20, 0.75f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++ ) {
            fully_specified.put( keys[i], vals[i] );
        }
        assertEquals( map, fully_specified );

        assertFalse( "shouldn't equal random object", map.equals( new Object() ) );

        int value = 11010110;     // I thought I saw a two!
        assertEquals( map.getNoEntryValue(), map.put( null, value ) );
        assertEquals( map.getNoEntryValue(), fully_specified.put( null, value ) );
        assertEquals( value, map.get( null ) );
        assertEquals( value, fully_specified.get( null ) );
        assertTrue( "incorrectly not-equal map: " + map + "\nfully_specified: " + fully_specified,
                map.equals( fully_specified ) );
        assertTrue( "incorrectly not-equal map: " + map + "\nfully_specified: " + fully_specified,
                fully_specified.equals( map ) );

        assertEquals( map.getNoEntryValue(), fully_specified.put( "non-null-key", value ) );
        assertFalse( "incorrectly equal map: " + map + "\nfully_specified: " + fully_specified,
                map.equals( fully_specified ) );
        assertFalse( "incorrectly equal map: " + map + "\nfully_specified: " + fully_specified,
                fully_specified.equals( map ) );

        int no_value = map.getNoEntryValue();
        assertEquals( map.getNoEntryValue(), map.put( "non-null-key", no_value ) );
        assertEquals( value, fully_specified.put( "non-null-key", no_value ) );
        assertTrue( "incorrectly not-equal map: " + map + "\nfully_specified: " + fully_specified,
                map.equals( fully_specified ) );
        assertTrue( "incorrectly not-equal map: " + map + "\nfully_specified: " + fully_specified,
                fully_specified.equals( map ) );
        
        assertEquals( no_value, map.put( "non-null-key", value ) );
        assertFalse( "incorrectly equal map: " + map + "\nfully_specified: " + fully_specified,
                map.equals( fully_specified ) );
        assertFalse( "incorrectly equal map: " + map + "\nfully_specified: " + fully_specified,
                fully_specified.equals( map ) );


        assertEquals( map.getNoEntryValue(), fully_specified.put( "blargh", value ) );
        assertFalse( "incorrectly equal map: " + map + "\nfully_specified: " + fully_specified,
                map.equals( fully_specified ) );
    }


    public void testHashCode() {
        int element_count = 20;
        String[] keys = new String[element_count];
        int[] vals = new int[element_count];
        int counter = 0;

        TObjectIntMap<String> map =
                new TObjectIntHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++, counter++ ) {
            keys[i] = Integer.toString( counter + 1 );
            vals[i] = counter + 1;
            map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map.size() );
        assertEquals( map.hashCode(), map.hashCode() );

        Map<String, TObjectIntMap<String>> string_tmap_map =
                new HashMap<String, TObjectIntMap<String>>();
        string_tmap_map.put( "first", map );
        string_tmap_map.put( "second", map );
        assertSame( map, string_tmap_map.get( "first" ) );
        assertSame( map, string_tmap_map.get( "second" ) );

        TObjectIntMap<String> map2 =
                new TObjectIntHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++, counter++ ) {
            keys[i] = Integer.toString( counter + 1 );
            vals[i] = counter + 1;
            map2.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map2.size() );
        assertEquals( map2.hashCode(), map2.hashCode() );

        TObjectIntMap<String> map3 =
                new TObjectIntHashMap<String>( element_count, 0.5f, Integer.MIN_VALUE );
        for ( int i = 0; i < element_count; i++, counter++ ) {
            keys[i] = Integer.toString( counter + 1 );
            vals[i] = counter + 1;
            map3.put( keys[i], vals[i] );
        }
        assertEquals( element_count, map3.size() );
        assertEquals( map3.hashCode(), map3.hashCode() );

        assertFalse( map.hashCode() == map2.hashCode() );
        assertFalse( map.hashCode() == map3.hashCode() );
        assertFalse( map2.hashCode() == map3.hashCode() );

        Map<TObjectIntMap<String>, String> tmap_string_map =
                new HashMap<TObjectIntMap<String>, String>();
        tmap_string_map.put( map, "map1" );
        tmap_string_map.put( map2, "map2" );
        tmap_string_map.put( map3, "map3" );
        assertEquals( "map1", tmap_string_map.get( map ) );
        assertEquals( "map2", tmap_string_map.get( map2 ) );
        assertEquals( "map3", tmap_string_map.get( map3 ) );
    }


    @SuppressWarnings({"unchecked"})
    public void testSerialize() throws Exception {
        Integer[] keys = {1138, 42, 86, 99, 101, 727, 117};
        int[] vals = new int[keys.length];

        TObjectIntMap<Integer> map = new TObjectIntHashMap<Integer>();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( map );

        ByteArrayInputStream bias = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bias );

        TObjectIntMap<Integer> deserialized = (TObjectIntMap<Integer>) ois.readObject();

        assertEquals( map, deserialized );
    }


    public void testToString() {
        TObjectIntHashMap<String> m = new TObjectIntHashMap<String>();
        m.put( "One", 11 );
        m.put( "Two", 22 );

        String to_string = m.toString();
        assertTrue( to_string, to_string.equals( "{One=11,Two=22}" )
                               || to_string.equals( "{Two=22,One=11}" ) );
    }


    public void testDecorator() {
//        TObjectIntHashMap<String> map = new TObjectIntHashMap<String>();
//
//        map.put( "one", 1 );
//        map.put( "two", 2 );
//
//        Map<String,Integer> decorator = new TObjectIntHashMapDecorator<String>( map );
//
//        assertEquals( 2, decorator.size() );
//        assertEquals( Integer.valueOf( 1 ), decorator.get( "one" ) );
//        assertEquals( Integer.valueOf( 2 ), decorator.get( "two" ) );
//
//        Set<String> decorator_keys = decorator.keySet();
//        assertEquals( 2, decorator_keys.size() );
//        Iterator<String> it = decorator_keys.iterator();
//        int count = 0;
//        while( it.hasNext() ) {
//            count++;
//            System.out.println(it.next());
//        }
//        assertEquals( 2, count );
//
//        assertSame(map, ( ( TObjectIntHashMapDecorator ) decorator ).getMap() );
    }


	public void testBug3232758() {
		TObjectIntHashMap<String> map = new TObjectIntHashMap<String>( 1, 3 );
		map.put( "1009", 0 );
		map.put( "1007", 1 );
		map.put( "1006", 2 );
		map.put( "1005", 3 );
		map.put( "1004", 4 );
		map.put( "1002", 5 );
		map.put( "1001", 6 );

		for ( Object o : map.keys() ) {
			map.remove( o );
		}
	}
}
