///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2006-2008, Rob Eden All Rights Reserved.
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

//import gnu.trove.decorator.TByteIntHashMapDecorator;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import gnu.trove.iterator.TIntLongIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TLongProcedure;
import gnu.trove.procedure.TIntLongProcedure;
import gnu.trove.function.TLongFunction;
import gnu.trove.TLongCollection;
import gnu.trove.TIntCollection;
import junit.framework.TestCase;

import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;



/**
 *
 */
public class TPrimitivePrimitiveHashMapTest extends TestCase {

    final int KEY_ONE = 100;
    final int KEY_TWO = 101;


    public TPrimitivePrimitiveHashMapTest( String name ) {
        super( name );
    }


    public void testConstructors() {

        int[] keys = {1138, 42, 86, 99, 101};
        long[] vals = {1138, 42, 86, 99, 101};

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            map.put( keys[i], vals[i] );
        }
        assertEquals( keys.length, map.size() );

        TIntLongMap capacity = new TIntLongHashMap( 20 );
        for ( int i = 0; i < keys.length; i++ ) {
            capacity.put( keys[i], vals[i] );
        }
        assertEquals( keys.length, capacity.size() );

        TIntLongMap cap_and_factor = new TIntLongHashMap( 20, 0.75f );
        for ( int i = 0; i < keys.length; i++ ) {
            cap_and_factor.put( keys[i], vals[i] );
        }
        assertEquals( keys.length, cap_and_factor.size() );

        TIntLongMap fully_specified =
                new TIntLongHashMap( 20, 0.5f, Integer.MIN_VALUE, Long.MIN_VALUE );
        for ( int i = 0; i < keys.length; i++ ) {
            fully_specified.put( keys[i], vals[i] );
        }
        assertEquals( keys.length, fully_specified.size() );

        TIntLongMap copy = new TIntLongHashMap( map );
        assertEquals( keys.length, fully_specified.size() );

        TIntLongMap arrays = new TIntLongHashMap( keys, vals );
        assertEquals( keys.length, arrays.size() );


        // Equals in all combinations is paranoid.. but..
        assertEquals( map, map );
        assertEquals( map, capacity );
        assertEquals( map, cap_and_factor );
        assertEquals( map, fully_specified );
        assertEquals( map, copy );
        assertEquals( map, arrays );
        assertEquals( capacity, map );
        assertEquals( capacity, capacity );
        assertEquals( capacity, cap_and_factor );
        assertEquals( capacity, fully_specified );
        assertEquals( capacity, copy );
        assertEquals( capacity, arrays );
        assertEquals( cap_and_factor, map );
        assertEquals( cap_and_factor, capacity );
        assertEquals( cap_and_factor, cap_and_factor );
        assertEquals( cap_and_factor, fully_specified );
        assertEquals( cap_and_factor, copy );
        assertEquals( cap_and_factor, arrays );
        assertEquals( fully_specified, map );
        assertEquals( fully_specified, capacity );
        assertEquals( fully_specified, cap_and_factor );
        assertEquals( fully_specified, fully_specified );
        assertEquals( fully_specified, copy );
        assertEquals( fully_specified, arrays );
        assertEquals( copy, map );
        assertEquals( copy, capacity );
        assertEquals( copy, cap_and_factor );
        assertEquals( copy, fully_specified );
        assertEquals( copy, copy );
        assertEquals( copy, arrays );
        assertEquals( arrays, map );
        assertEquals( arrays, capacity );
        assertEquals( arrays, cap_and_factor );
        assertEquals( arrays, fully_specified );
        assertEquals( arrays, copy );
        assertEquals( arrays, arrays );
    }


    /** Be sure that size is large enough to force a resize or two. */
    public void testRehash() {
        int size = 1000;
        int[] keys = new int[size];
        long[] vals = new long[size];
        for ( int i = 0; i < size; i++ ) {
            keys[i] = i + 1;
            vals[i] = keys[i] * 2;
        }
        
        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            map.put( keys[i], vals[i] );
        }
        assertEquals( keys.length, map.size() );
        for ( int i = 0; i < keys.length; i++ ) {
            int key = keys[i];
            long val = vals[i];
            assertEquals( "got incorrect value for index " + i + ", map: " + map,
                    val, map.get( key ) );
        }
    }


    public void testPutAll() {
        int[] keys = {1138, 42, 86, 99, 101};
        long[] vals = {1138, 42, 86, 99, 101};

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            map.put( keys[i], vals[i] * 2 );
        }
        assertEquals( keys.length, map.size() );

        TIntLongMap target = new TIntLongHashMap();
        target.put( 1, 2 );
        assertEquals( 1, target.size() );

        target.putAll( map );
        assertEquals( keys.length + 1, target.size() );
        for ( int i = 0; i < keys.length; i++ ) {
            assertEquals( vals[i] * 2, target.get( keys[i] ) );
        }
        assertEquals( 2, target.get( 1 ) );


        // java.util.Map source
        Map<Integer, Long> java_map = new HashMap<Integer, Long>();
        for ( int i = 0; i < keys.length; i++ ) {
            java_map.put( keys[i], vals[i] * 2 );
        }

        // fresh TIntLongMap
        target = new TIntLongHashMap();
        target.put( 1, 2 );
        assertEquals( 1, target.size() );

        target.putAll( java_map );
        assertEquals( "map size is incorrect: " + keys.length + ", source: " +
                      java_map + ", target: " + target,
                keys.length + 1, target.size() );
        for ( int i = 0; i < keys.length; i++ ) {
            assertEquals( vals[i] * 2, target.get( keys[i] ) );
        }
        assertEquals( 2, target.get( 1 ) );
    }


    public void testClear() {
        int[] keys = {1138, 42, 86, 99, 101};
        long[] vals = {1138, 42, 86, 99, 101};

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            map.put( keys[i], vals[i] * 2 );
        }
        assertEquals( keys.length, map.size() );

        map.clear();
        assertEquals( 0, map.size() );
        assertTrue( map.isEmpty() );

        TIntLongMap empty = new TIntLongHashMap();
        assertEquals( empty, map );


//        Map<String,String> jmap = new HashMap<String, String>();
//        jmap.isEmpty()
    }


    public void testRemove() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        assertEquals( keys.length, map.size() );
        for ( int i = 0; i < keys.length; i++ ) {
            assertEquals( vals[i], map.get( keys[i] ) );
        }
        assertEquals( vals[0], map.remove( keys[0] ) );
        assertEquals( vals[3], map.remove( keys[3] ) );
        assertEquals( map.getNoEntryValue(), map.remove( keys[0] ) );
        assertEquals( vals[5], map.remove( keys[5] ) );
        assertEquals( map.getNoEntryValue(), map.remove( 11010110 ) );
    }


    public void testKeySetMisc() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TIntSet set = map.keySet();
        int[] sorted_keys = new int[ keys.length ];
        System.arraycopy( keys, 0, sorted_keys, 0, keys.length );
        Arrays.sort( sorted_keys );
        int[] setarray = set.toArray();
        Arrays.sort( setarray );
        assertTrue( "expected: " + Arrays.toString( sorted_keys ) +
                    ", was: " + Arrays.toString( setarray ),
                Arrays.equals( sorted_keys, setarray ) );

        setarray = set.toArray( new int[0] );
        Arrays.sort( setarray );
        assertTrue( "expected: " + Arrays.toString( sorted_keys ) +
                    ", was: " + Arrays.toString( setarray ),
                Arrays.equals( sorted_keys, setarray ) );

        assertFalse( "remove of element not in set succeded: " + set, set.remove( 1 ) );
        assertEquals( keys.length, set.size() );
        assertEquals( keys.length, map.size() );

        assertTrue( "remove of element in set failed: " + set, set.remove( 42 ) );
        assertEquals( keys.length - 1, set.size() );
        assertEquals( keys.length - 1, map.size() );

        try {
            set.add( 42 );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }
    }


    public void testKeySetContainsAll() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TIntSet set = map.keySet();
        assertEquals( map.size(), set.size() );
        assertFalse( set.isEmpty() );

        // test with a java.util.Map
        Set<Number> java_set = new HashSet<Number>();
        for ( int i = 0; i < keys.length; i++ ) {
            java_set.add( Integer.valueOf( keys[i] ) );
        }
        assertTrue( set.containsAll( java_set ) );
        java_set.add( Integer.valueOf( 12 ) );
        assertFalse( set.containsAll( java_set ) );
        java_set.remove( Integer.valueOf( 12 ) );
        assertTrue( set.containsAll( java_set ) );
        java_set.add( Long.valueOf( 12 ) );
        assertFalse( set.containsAll( java_set ) );

        // test with a TCollection
        TIntSet tintset = new TIntHashSet( keys );
        assertTrue( set.containsAll( tintset ) );
        tintset.add( 12 );
        assertFalse( set.containsAll( tintset ) );

        // test raw array
        assertTrue( set.containsAll( keys ) );
        keys[3] = keys[3] + 1;
        assertFalse( set.containsAll( keys ) );
    }


    public void testKeySetAddAll() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TIntSet set = map.keySet();
        assertEquals( map.size(), set.size() );
        assertFalse( set.isEmpty() );

        // test with a java.util.Map
        Set<Integer> java_set = new HashSet<Integer>();
        for ( int i = 0; i < keys.length; i++ ) {
            java_set.add( Integer.valueOf( keys[i] ) );
        }

        try {
            set.addAll( java_set );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }

        try {
            set.addAll( set );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }

        try {
            set.addAll( keys );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }
    }


    public void testKeySetRetainAllCollection() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TIntSet set = map.keySet();
        assertEquals( map.size(), set.size() );
        assertFalse( set.isEmpty() );

        // test with a java.util.Map
        Set<Number> java_set = new HashSet<Number>();
        for ( int i = 0; i < keys.length; i++ ) {
            java_set.add( Integer.valueOf( keys[i] ) );
        }
        assertFalse( set.retainAll( java_set ) );
        assertEquals( keys.length, set.size() );
        assertEquals( keys.length, map.size() );
        for ( int i = 0; i < keys.length; i++ ) {
            assertTrue( set.contains( keys[i] ) );
            assertTrue( map.containsKey( keys[i] ) );
        }
        java_set.remove( 42 );
        assertTrue( "set should have been modified: " + set + ", java: " + java_set,
                set.retainAll( java_set ) );
        assertEquals( keys.length - 1, set.size() );
        assertEquals( keys.length - 1, map.size() );
        for ( int i = 0; i < keys.length; i++ ) {
            if ( keys[i] != 42 ) {
                assertTrue( set.contains( keys[i] ) );
                assertTrue( map.containsKey( keys[i] ) );
            } else {
                assertFalse( set.contains( keys[i] ) );
                assertFalse( map.containsKey( keys[i] ) );
            }
        }
    }


    public void testKeySetRetainAllTCollection() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TIntSet set = map.keySet();
        assertEquals( map.size(), set.size() );
        assertFalse( set.isEmpty() );

        assertFalse( set.retainAll( set ) );

        // test with a TCollection
        TIntSet tintset = new TIntHashSet( keys );
        assertFalse( "set: " + set + ", collection: " + tintset,
                set.retainAll( tintset ) );
        TIntCollection collection = new TIntArrayList( keys );
        assertFalse( "set: " + set + ", collection: " + collection,
                set.retainAll( collection ) );

        collection.remove( 42 );
        assertTrue( "set: " + set + ", collection: " + collection,
                set.retainAll( collection ) );
        assertEquals( keys.length - 1, set.size() );
        assertEquals( keys.length - 1, map.size() );
    }


    public void testKeySetRetainAllArray() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TIntSet set = map.keySet();
        assertEquals( map.size(), set.size() );
        assertFalse( set.isEmpty() );

        // test raw array
        map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }
        set = map.keySet();
        
        assertFalse( set.retainAll( keys ) );
        assertTrue( set.containsAll( keys ) );
        
        keys[3] = keys[3] + 1;
        assertTrue( set.retainAll( keys ) );
        keys[3] = keys[3] - 1;

        assertEquals( "removed: " + keys[3] + ", set: " + set,
                keys.length - 1, set.size() );
        assertEquals( "removed: " + keys[3] + ", set: " + set
                      + "\nmap: " + map, set.size(), map.size() );
    }


    public void testKeySetRemoveAllCollection() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TIntSet set = map.keySet();
        assertEquals( map.size(), set.size() );
        assertFalse( set.isEmpty() );

        // test with a java.util.Map
        Set<Number> java_set = new HashSet<Number>();
        assertFalse( set.removeAll( java_set ) );
        assertEquals( keys.length, set.size() );
        assertEquals( keys.length, map.size() );      
        for ( int i = 0; i < keys.length; i++ ) {
            assertTrue( set.contains( keys[i] ) );
            assertTrue( map.containsKey( keys[i] ) );
        }

        for ( int i = 0; i < keys.length; i++ ) {
            java_set.add( Integer.valueOf( keys[i] ) );
        }
        java_set.remove( 42 );
        assertTrue( "set should have been modified: " + set + ", java: " + java_set,
                set.removeAll( java_set ) );
        assertEquals( "set: " + set, 1, set.size() );
        assertEquals( "set: " + set, 1, map.size() );
        for ( int i = 0; i < keys.length; i++ ) {
            if ( keys[i] == 42 ) {
                assertTrue( set.contains( keys[i] ) );
                assertTrue( map.containsKey( keys[i] ) );
            } else {
                assertFalse( set.contains( keys[i] ) );
                assertFalse( map.containsKey( keys[i] ) );
            }
        }
    }


    public void testKeySetRemoveAllTCollection() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TIntSet set = map.keySet();
        assertEquals( map.size(), set.size() );
        assertFalse( set.isEmpty() );

        assertTrue( set.removeAll( set ) );
        assertTrue( set.isEmpty() );
        
        // repopulate the set.
        map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }
        set = map.keySet();

        // With empty set
        TIntSet tintset = new TIntHashSet();
        assertFalse( "set: " + set + ", collection: " + tintset,
                set.removeAll( tintset ) );

        // With partial set
        tintset = new TIntHashSet( keys );
        tintset.remove( 42 );
        assertTrue( "set: " + set + ", collection: " + tintset,
                set.removeAll( tintset ) );
        assertEquals( "set: " + set, 1, set.size() );
        assertEquals( "set: " + set, 1, map.size() );
        for ( int i = 0; i < keys.length; i++ ) {
            if ( keys[i] == 42 ) {
                assertTrue( set.contains( keys[i] ) );
                assertTrue( map.containsKey( keys[i] ) );
            } else {
                assertFalse( set.contains( keys[i] ) );
                assertFalse( map.containsKey( keys[i] ) );
            }
        }

        // repopulate the set.
        map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }
        set = map.keySet();

        // Empty list
        TIntCollection collection = new TIntArrayList();
        assertFalse( "set: " + set + ", collection: " + collection,
                set.removeAll( collection ) );

        // partial list
        collection = new TIntArrayList( keys );
        collection.remove( 42 );
        assertTrue( "set: " + set + ", collection: " + collection,
                set.removeAll( collection ) );
        assertEquals( "set: " + set, 1, set.size() );
        assertEquals( "set: " + set, 1, map.size() );
        for ( int i = 0; i < keys.length; i++ ) {
            if ( keys[i] == 42 ) {
                assertTrue( set.contains( keys[i] ) );
                assertTrue( map.containsKey( keys[i] ) );
            } else {
                assertFalse( set.contains( keys[i] ) );
                assertFalse( map.containsKey( keys[i] ) );
            }
        }
    }


    public void testKeySetRemoveAllArray() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TIntSet set = map.keySet();
        assertEquals( map.size(), set.size() );
        assertFalse( set.isEmpty() );

        // test raw array
        map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }
        set = map.keySet();

        keys[3] = keys[3] + 1;
        assertTrue( set.removeAll( keys ) );
        keys[3] = keys[3] - 1;

        assertEquals( "removed: " + keys[3] + ", set: " + set,
                1, set.size() );
        assertEquals( "removed: " + keys[3] + ", set: " + set
                      + "\nmap: " + map, set.size(), map.size() );
    }


    public void testKeySetForEach() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TIntSet set = map.keySet();
        assertEquals( map.size(), set.size() );
        assertFalse( set.isEmpty() );

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
        set.forEach( foreach );
        TIntList built = foreach.getBuilt();
        for ( int i = 0; i < set.size(); i++ ) {
            assertTrue( set.contains( built.get( i ) ) );
        }
    }


    public void testKeySetEquals() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TIntSet set = map.keySet();
        assertEquals( map.size(), set.size() );
        assertFalse( set.isEmpty() );

        TIntSet other = new TIntHashSet();
        other.addAll( keys );

        assertTrue( "sets incorrectly not equal: " + set + ", " + other,
                set.equals( other ) );

        int[] mismatched = {72, 49, 53, 1024, 999};
        TIntSet unequal = new TIntHashSet();
        unequal.addAll( mismatched );

        assertFalse( "sets incorrectly equal: " + set + ", " + unequal,
                set.equals( unequal ) );

        // Change length, different code branch
        unequal.add( 1 );
        assertFalse( "sets incorrectly equal: " + set + ", " + unequal,
                set.equals( unequal ) );

        assertFalse( "set incorrectly equals a random object",
                set.equals( new Object() ) );
    }


    public void testKeySetHashCode() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TIntSet set = map.keySet();
        assertEquals( map.size(), set.size() );
        assertFalse( set.isEmpty() );
        

        TIntSet other = new TIntHashSet();
        other.addAll( keys );

        assertTrue( "hashcodes incorrectly not equal: " + set + ", " + other,
                set.hashCode() == other.hashCode() );

        int[] mismatched = {72, 49, 53, 1024, 999};
        TIntSet unequal = new TIntHashSet();
        unequal.addAll( mismatched );

        assertFalse( "hashcodes unlikely equal: " + set + ", " + unequal,
                set.hashCode() == unequal.hashCode() );
    }


    public void testKeySetIterator() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TIntList list = new TIntArrayList( keys );
        TIntSet set = map.keySet();
        assertEquals( map.getNoEntryKey(), set.getNoEntryValue() );

        // test basic iterator function.
        TIntIterator iter = set.iterator();
        while ( iter.hasNext() ) {
            int key = iter.next();
            assertTrue( "key set should only contain keys: " + key + ", set; " + set,
                    list.contains( key ) );
        }

        assertFalse( iter.hasNext() );
        try {
            iter.next();
            fail( "Expect NoSuchElementException" );
        }
        catch ( NoSuchElementException ex ) {
            // Expected.
        }

        // Start over with new iterator -- test iter.remove()
        iter = set.iterator();
        while ( iter.hasNext() ) {
            int key = iter.next();
            assertTrue( "key set should only contain keys: " + key + ", set; " + set,
                    list.contains( key ) );
            if ( key == keys[3] ) {
                iter.remove();
                assertFalse( "set contains removed element: " + key + ", set: " + set,
                        set.contains( key ) );
            }
        }
        assertEquals( map.size(), set.size() );
        assertEquals( keys.length - 1, map.size() );
        assertEquals( map.getNoEntryValue(), map.get( keys[3] ) );
    }


    public void testKeys() {
        TIntLongMap map = new TIntLongHashMap();

        map.put( KEY_ONE, 10 );
        map.put( KEY_TWO, 20 );

        assertEquals( 2, map.size() );

        int[] keys = map.keys( new int[map.size()] );
        assertEquals( 2, keys.length );
        TIntList keys_list = new TIntArrayList( keys );

        assertTrue( keys_list.contains( KEY_ONE ) );
        assertTrue( keys_list.contains( KEY_TWO ) );

        int[] keys2 = map.keys();
        assertEquals( 2, keys2.length );
        TIntList keys_list2 = new TIntArrayList( keys2 );

        assertTrue( keys_list2.contains( KEY_ONE ) );
        assertTrue( keys_list2.contains( KEY_TWO ) );

        int element_count = 20;
        map = new TIntLongHashMap();
        for ( int i = 0; i < element_count; i++ ) {
            map.put( i, i * i );
        }
        assertEquals( element_count, map.size() );
        keys = map.keys( new int[0] );
        Arrays.sort( keys );
        assertEquals( element_count, keys.length );
        for ( int i = 0; i < element_count; i++ ) {
            assertEquals( "expected: " + i + " got: " + keys[i] + ", i: " + i +
                          ", keys: " + Arrays.toString( keys ),  i, keys[i] );
            assertEquals( i * i, map.get( keys[i] ) );
        }
    }


    public void testValueCollectionMisc() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TLongCollection values = map.valueCollection();
        long[] sorted_keys = new long[ vals.length ];
        System.arraycopy( vals, 0, sorted_keys, 0, vals.length );
        Arrays.sort( sorted_keys );
        long[] setarray = values.toArray();
        Arrays.sort( setarray );
        assertTrue( "expected: " + Arrays.toString( sorted_keys ) +
                    ", was: " + Arrays.toString( setarray ),
                Arrays.equals( sorted_keys, setarray ) );

        setarray = values.toArray( new long[0] );
        Arrays.sort( setarray );
        assertTrue( "expected: " + Arrays.toString( sorted_keys ) +
                    ", was: " + Arrays.toString( setarray ),
                Arrays.equals( sorted_keys, setarray ) );

        assertFalse( "remove of element not in collection succeded: " + values,
                values.remove( 1 ) );
        assertEquals( keys.length, values.size() );
        assertEquals( keys.length, map.size() );

        assertTrue( "remove of element in collection failed: " + values,
                values.remove( 42 * 2 ) );
        assertEquals( keys.length - 1, values.size() );
        assertEquals( keys.length - 1, map.size() );

        try {
            values.add( 42 );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }
    }


    public void testValueCollectionContainsAll() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TLongCollection values = map.valueCollection();
        assertEquals( map.size(), values.size() );
        assertFalse( values.isEmpty() );

        // test with a java.util.Map
        Set<Number> java_set = new HashSet<Number>();
        for ( int i = 0; i < vals.length; i++ ) {
            java_set.add( Long.valueOf( vals[i] ) );
        }
        assertTrue( values.containsAll( java_set ) );
        java_set.add( Integer.valueOf( 12 ) );
        assertFalse( values.containsAll( java_set ) );
        java_set.remove( Integer.valueOf( 12 ) );
        assertTrue( values.containsAll( java_set ) );
        java_set.add( Long.valueOf( 12 ) );
        assertFalse( values.containsAll( java_set ) );

        // test with a TCollection
        TLongSet tintset = new TLongHashSet( vals );
        assertTrue( values.containsAll( tintset ) );
        tintset.add( 12 );
        assertFalse( values.containsAll( tintset ) );

        // test raw array
        assertTrue( values.containsAll( vals ) );
        vals[3] = vals[3] + 1;
        assertFalse( values.containsAll( vals ) );
    }


    public void testValueCollectionAddAll() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TLongCollection values = map.valueCollection();
        assertEquals( map.size(), values.size() );
        assertFalse( values.isEmpty() );

        // test with a java.util.Map
        Set<Long> java_set = new HashSet<Long>();
        for ( int i = 0; i < vals.length; i++ ) {
            java_set.add( Long.valueOf( vals[i] ) );
        }

        try {
            values.addAll( java_set );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }

        try {
            values.addAll( values );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }

        try {
            values.addAll( vals );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }
    }


    public void testValueCollectionRetainAllCollection() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TLongCollection values = map.valueCollection();
        assertEquals( map.size(), values.size() );
        assertFalse( values.isEmpty() );

        // test with a java.util.Map
        Set<Number> java_set = new HashSet<Number>();
        for ( int i = 0; i < vals.length; i++ ) {
            java_set.add( Long.valueOf( vals[i] ) );
        }
        assertFalse( values.retainAll( java_set ) );
        assertEquals( keys.length, values.size() );
        assertEquals( keys.length, map.size() );
        for ( int i = 0; i < keys.length; i++ ) {
            assertTrue( values.contains( vals[i] ) );
            assertTrue( map.containsValue( vals[i] ) );
        }
        java_set.remove( Long.valueOf( 42 * 2 ) );
        assertTrue( "collection should have been modified: " + values +
                    "\njava: " + java_set,
                values.retainAll( java_set ) );
        assertEquals( keys.length - 1, values.size() );
        assertEquals( keys.length - 1, map.size() );
        for ( int i = 0; i < keys.length; i++ ) {
            if ( keys[i] != 42 ) {
                assertTrue( values.contains( vals[i] ) );
                assertTrue( map.containsValue( vals[i] ) );
            } else {
                assertFalse( values.contains( vals[i] ) );
                assertFalse( map.containsValue( vals[i] ) );
            }
        }
    }


    public void testValueCollectionRetainAllTCollection() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TLongCollection values = map.valueCollection();
        assertEquals( map.size(), values.size() );
        assertFalse( values.isEmpty() );

        assertFalse( values.retainAll( values ) );

        // test with a TCollection
        TLongSet tintset = new TLongHashSet( vals );
        assertFalse( "values: " + values + ", collection: " + tintset,
                values.retainAll( tintset ) );
        TLongCollection collection = new TLongArrayList( vals );
        assertFalse( "values: " + values + ", collection: " + collection,
                values.retainAll( collection ) );

        collection.remove( 42 * 2 );
        assertTrue( "values: " + values + ", collection: " + collection,
                values.retainAll( collection ) );
        assertEquals( keys.length - 1, values.size() );
        assertEquals( keys.length - 1, map.size() );
    }


    public void testValueCollectionRetainAllArray() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TLongCollection values = map.valueCollection();
        assertEquals( map.size(), values.size() );
        assertFalse( values.isEmpty() );

        // test raw array
        map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }
        values = map.valueCollection();

        assertFalse( values.retainAll( vals ) );
        assertTrue( values.containsAll( vals ) );

        vals[3] = vals[3] + 1;
        assertTrue( values.retainAll( vals ) );
        vals[3] = vals[3] - 1;

        assertEquals( "removed: " + keys[3] + ", values: " + values,
                keys.length - 1, values.size() );
        assertEquals( "removed: " + keys[3] + ", set: " + values
                      + "\nmap: " + map, values.size(), map.size() );
    }


    public void testValueCollectionRemoveAllCollection() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TLongCollection values = map.valueCollection();
        assertEquals( map.size(), values.size() );
        assertFalse( values.isEmpty() );

        // test with a java.util.Map
        Set<Number> java_set = new HashSet<Number>();
        assertFalse( values.removeAll( java_set ) );
        assertEquals( vals.length, values.size() );
        assertEquals( vals.length, map.size() );
        for ( int i = 0; i < keys.length; i++ ) {
            assertTrue( values.contains( vals[i] ) );
            assertTrue( map.containsValue( vals[i] ) );
        }

        for ( int i = 0; i < vals.length; i++ ) {
            java_set.add( Long.valueOf( vals[i] ) );
        }
        java_set.remove( Long.valueOf( 42 * 2 ) );
        assertTrue( "values should have been modified: " + values + ", java: " + java_set,
                values.removeAll( java_set ) );
        assertEquals( "set: " + values, 1, values.size() );
        assertEquals( "set: " + values, 1, map.size() );
        for ( int i = 0; i < vals.length; i++ ) {
            if ( vals[i] == 42 * 2 ) {
                assertTrue( values.contains( vals[i] ) );
                assertTrue( map.containsValue( vals[i] ) );
            } else {
                assertFalse( values.contains( vals[i] ) );
                assertFalse( map.containsValue( vals[i] ) );
            }
        }
    }


    public void testValueCollectionRemoveAllTCollection() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TLongCollection values = map.valueCollection();
        assertEquals( map.size(), values.size() );
        assertFalse( values.isEmpty() );

        assertTrue( values.removeAll( values ) );
        assertTrue( values.isEmpty() );

        // repopulate the set.
        map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }
        values = map.valueCollection();

        // With empty set
        TLongSet tlongset = new TLongHashSet();
        assertFalse( "values: " + values + ", collection: " + tlongset,
                values.removeAll( tlongset ) );

        // With partial set
        tlongset = new TLongHashSet( vals );
        tlongset.remove( 42 * 2 );
        assertTrue( "values: " + values + ", collection: " + tlongset,
                values.removeAll( tlongset ) );
        assertEquals( "set: " + values, 1, values.size() );
        assertEquals( "set: " + values, 1, map.size() );
        for ( int i = 0; i < keys.length; i++ ) {
            if ( keys[i] == 42 ) {
                assertTrue( values.contains( vals[i] ) );
                assertTrue( map.containsValue( vals[i] ) );
            } else {
                assertFalse( values.contains( vals[i] ) );
                assertFalse( map.containsValue( vals[i] ) );
            }
        }

        // repopulate the set.
        map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }
        values = map.valueCollection();

        // Empty list
        TLongCollection collection = new TLongArrayList();
        assertFalse( "values: " + values + ", collection: " + collection,
                values.removeAll( collection ) );

        // partial list
        collection = new TLongArrayList( vals );
        collection.remove( 42 * 2 );
        assertTrue( "values: " + values + ", collection: " + collection,
                values.removeAll( collection ) );
        assertEquals( "values: " + values, 1, values.size() );
        assertEquals( "values: " + values, 1, map.size() );
        for ( int i = 0; i < keys.length; i++ ) {
            if ( vals[i] == 42 * 2 ) {
                assertTrue( values.contains( vals[i] ) );
                assertTrue( map.containsValue( vals[i] ) );
            } else {
                assertFalse( values.contains( vals[i] ) );
                assertFalse( map.containsValue( vals[i] ) );
            }
        }
    }


    public void testValueCollectionRemoveAllArray() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TLongCollection values = map.valueCollection();
        assertEquals( map.size(), values.size() );
        assertFalse( values.isEmpty() );

        // test raw array
        map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }
        values = map.valueCollection();

        vals[3] = vals[3] + 1;
        assertTrue( values.removeAll( vals ) );
        vals[3] = vals[3] - 1;

        assertEquals( "removed: " + keys[3] + ", values: " + values,
                1, values.size() );
        assertEquals( "removed: " + keys[3] + ", values: " + values
                      + "\nmap: " + map, values.size(), map.size() );
    }


    public void testValueCollectionForEach() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TLongCollection values = map.valueCollection();
        assertEquals( map.size(), values.size() );
        assertFalse( values.isEmpty() );

        class ForEach implements TLongProcedure {
            TLongList built = new TLongArrayList();


            public boolean execute( long value ) {
                built.add( value );
                return true;
            }

            TLongList getBuilt() {
                return built;
            }
        }

        ForEach foreach = new ForEach();
        values.forEach( foreach );
        TLongList built = foreach.getBuilt();
        for ( int i = 0; i < values.size(); i++ ) {
            assertTrue( values.contains( built.get( i ) ) );
        }
    }


    public void testValueCollectionEquals() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TLongCollection values = map.valueCollection();
        assertEquals( map.size(), values.size() );
        assertFalse( values.isEmpty() );
        assertEquals( values, values );
        TLongList values_list = new TLongArrayList( values );
        assertFalse( "collections should not be equal: " + values + ", " + values_list,
                values.equals( values_list ) );

        TLongList list = new TLongArrayList( vals );
        values_list.sort();
        list.sort();
        assertTrue( "collections incorrectly not equal: " + values_list + ", " + list,
                values_list.equals( list ) );
        assertTrue( "collections incorrectly not equal: " + values_list + ", " + list,
                values_list.equals( list ) );


        long[] mismatched = {72, 49, 53, 1024, 999};
        TLongCollection unequal = new TLongArrayList();
        unequal.addAll( mismatched );

        assertFalse( "collections incorrectly equal: " + values_list + ", " + unequal,
                values_list.equals( unequal ) );

        // Change length, different code branch
        unequal.add( 1 );
        assertFalse( "collections incorrectly equal: " + values_list + ", " + unequal,
                values_list.equals( unequal ) );

        assertFalse( "values incorrectly equals a random object",
                values_list.equals( new Object() ) );

        // value in map twice, in list twice.
        list = new TLongArrayList( vals );
        map.put( 1, vals[0] );
        values_list = new TLongArrayList( map.valueCollection() );
        list.add( vals[0] );
        values_list.sort();
        list.sort();
        assertTrue( "collections incorrectly not equal: " + values_list + ", " + list,
                values_list.equals( list ) );

        // value in the map twice, same length list, but value only in list once.
        list = new TLongArrayList( vals );
        list.add( -1 );
        list.sort();        
        assertFalse( "collections incorrectly equal: " + values_list + ", " + list,
                values_list.equals( list ) );
    }


    public void testValueCollectionHashCode() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TLongCollection values = map.valueCollection();
        assertEquals( map.size(), values.size() );
        assertFalse( values.isEmpty() );
        assertEquals( "hashcodes incorrectly not equal: " + map + ", " + values,
                values.hashCode(), values.hashCode() );
        assertFalse( "hashcodes incorrectly equal: " + map + ", " + values,
                map.hashCode() == values.hashCode() );
    }



    public void testValueCollectionIterator() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TLongList list = new TLongArrayList( vals );
        TLongCollection set = map.valueCollection();
        assertEquals( map.getNoEntryValue(), set.getNoEntryValue() );

        // test basic iterator function.
        TLongIterator iter = set.iterator();
        while ( iter.hasNext() ) {
            long val = iter.next();
            assertTrue( "value collection should only contain values: " + val + ", set; " + set,
                    list.contains( val ) );
        }

        assertFalse( iter.hasNext() );
        try {
            iter.next();
            fail( "Expect NoSuchElementException" );
        }
        catch ( NoSuchElementException ex ) {
            // Expected.
        }

        // Start over with new iterator -- test iter.remove()
        iter = set.iterator();
        while ( iter.hasNext() ) {
            long val = iter.next();
            assertTrue( "value collection should only contain values: " + val + ", set; " + set,
                    list.contains( val ) );
            if ( val == vals[3] ) {
                iter.remove();
                assertFalse( "set contains removed element: " + val + ", set: " + set,
                        set.contains( val ) );
            }
        }
        assertEquals( map.size(), set.size() );
        assertEquals( keys.length - 1, map.size() );
        assertEquals( map.getNoEntryValue(), map.get( keys[3] ) );
    }


    public void testValues() {
        TIntLongMap map = new TIntLongHashMap();

        map.put( KEY_ONE, 1 );
        map.put( KEY_TWO, 2 );

        assertEquals( 2, map.size() );

        long[] values = map.values( new long[map.size()] );
        assertEquals( 2, values.length );
        TLongList values_list = new TLongArrayList( values );

        assertTrue( values_list.contains( 1 ) );
        assertTrue( values_list.contains( 2 ) );

        long[] values2 = map.values();
        assertEquals( 2, values2.length );
        TLongList keys_list2 = new TLongArrayList( values2 );

        assertTrue( keys_list2.contains( 1 ) );
        assertTrue( keys_list2.contains( 2 ) );

        int element_count = 20;
        map = new TIntLongHashMap();
        for ( int i = 0; i < element_count; i++ ) {
            map.put( i, i * i );
        }
        assertEquals( element_count, map.size() );
        long[] vals = map.values( new long[0] );
        Arrays.sort( vals );
        assertEquals( element_count, vals.length );
        for ( int i = 0; i < element_count; i++ ) {
            assertEquals( "expected: " + i * i + " got: " + vals[i] + ", i: " + i +
                          ", vals: " + Arrays.toString( vals ),  i * i, vals[i] );
            assertEquals( i * i, map.get( i ) );
        }
    }


    public void testForEachKey() {
        int element_count = 20;
        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 1; i <= element_count; i++ ) {
            map.put( i, i * i );
        }

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
        TIntList keys = new TIntArrayList( map.keys() );
        assertEquals( keys, built );

        built.sort();
        keys.sort();
        assertEquals( keys, built );


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
        keys = new TIntArrayList( map.keys() );
        assertEquals( 1, built.size() );
        assertEquals( keys.get( 0 ), built.get( 0 ) );
    }


    public void testForEachValue() {
        int element_count = 20;
        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 1; i <= element_count; i++ ) {
            map.put( i, i * i );
        }

        class ForEach implements TLongProcedure {
            TLongList built = new TLongArrayList();


            public boolean execute( long value ) {
                built.add( value );
                return true;
            }

            TLongList getBuilt() {
                return built;
            }
        }

        ForEach foreach = new ForEach();
        map.forEachValue( foreach );
        TLongList built = foreach.getBuilt();
        TLongList vals = new TLongArrayList( map.values() );
        assertEquals( vals, built );

        built.sort();
        vals.sort();
        assertEquals( vals, built );


        class ForEachFalse implements TLongProcedure {
            TLongList built = new TLongArrayList();


            public boolean execute( long value ) {
                built.add( value );
                return false;
            }

            TLongList getBuilt() {
                return built;
            }
        }

        ForEachFalse foreach_false = new ForEachFalse();
        map.forEachValue( foreach_false );
        built = foreach_false.getBuilt();
        vals = new TLongArrayList( map.values() );
        assertEquals( 1, built.size() );
        assertEquals( vals.get( 0 ), built.get( 0 ) );
    }


    public void testForEachEntry() {
        int element_count = 20;
        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 1; i <= element_count; i++ ) {
            map.put( i, i * i );
        }

        class ForEach implements TIntLongProcedure {
            TIntLongMap built = new TIntLongHashMap();


            public boolean execute( int key, long value ) {
                built.put( key, value );
                return true;
            }

            TIntLongMap getBuilt() {
                return built;
            }
        }

        ForEach foreach = new ForEach();
        map.forEachEntry( foreach );
        TIntLongMap built = foreach.getBuilt();
        assertEquals( map, built );
    }


    public void testTransformValues() {
        int element_count = 20;
        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 1; i <= element_count; i++ ) {
            map.put( i, i );
        }

        class TransformValues implements TLongFunction {
            public long execute( long value ) {
                return value * value;
            }
        }

        TransformValues foreach = new TransformValues();
        map.transformValues( foreach );
        for ( int i = 1; i <= element_count; i++ ) {
            assertEquals( i * i, map.get( i ) );
        }
    }


    public void testRetainEntries() {
        int element_count = 20;
        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 1; i <= element_count; i++ ) {
            map.put( i, i * i );
        }

        class ForEach implements TIntLongProcedure {
            TIntLongMap built = new TIntLongHashMap();


            // Evens in one map, odds in another.
            public boolean execute( int key, long value ) {
                if ( key % 2 == 1 ) {
                    built.put( key, value );
                    return false;
                }
                return true;
            }

            TIntLongMap getBuilt() {
                return built;
            }
        }

        ForEach foreach = new ForEach();
        map.retainEntries( foreach );
        TIntLongMap built = foreach.getBuilt();

        for ( int i = 1; i <= element_count; i++ ) {
            if ( i % 2 == 0 ) {
                assertTrue( map.containsKey( i ) );
                assertFalse( built.containsKey( i ) );
                assertTrue( map.containsValue( i * i ) );
                assertFalse( built.containsValue( i * i ) );
                assertEquals( i * i, map.get( i ) );
            } else {
                assertFalse( map.containsKey( i ) );
                assertTrue( built.containsKey( i ) );
                assertFalse( map.containsValue( i * i ) );
                assertTrue( built.containsValue( i * i ) );
                assertEquals( i * i, built.get( i ) );
            }
        }
    }


    public void testIncrement() {
        int element_count = 20;
        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 1; i <= element_count; i++ ) {
            map.put( i, i * i );
        }

        for ( int i = 1; i <= element_count; i++ ) {
            if ( i % 2 == 0 ) {
                map.increment( i );
            }
        }

        for ( int i = 1; i <= element_count; i++ ) {
            if ( i % 2 == 0 ) {
                assertEquals( i * i + 1, map.get( i ) );
            } else {
                assertEquals( i * i, map.get( i ) );
            }
        }
    }


    public void testDecorator() {
//        TByteIntHashMap map = new TByteIntHashMap();
//
//        map.put( KEY_ONE, 1 );
//        map.put( KEY_TWO, 2 );
//
//        Map<Byte, Integer> decorator = new TByteIntHashMapDecorator( map );
//
//        assertEquals( 2, decorator.size() );
//        assertEquals( Integer.valueOf( 1 ), decorator.get( Byte.valueOf( KEY_ONE ) ) );
//        assertEquals( Integer.valueOf( 2 ), decorator.get( Byte.valueOf( KEY_TWO ) ) );
//
//        Set<Byte> decorator_keys = decorator.keySet();
//        assertEquals( 2, decorator_keys.size() );
//        Iterator<Byte> it = decorator_keys.iterator();
//        int count = 0;
//        while ( it.hasNext() ) {
//            count++;
//            System.out.println( it.next() );
//        }
//        assertEquals( 2, count );
//
//        assertSame( map, ( (TByteIntHashMapDecorator) decorator ).getMap() );
    }


    public void testIterator() {
        TIntLongMap map = new TIntLongHashMap();

        TIntLongIterator iterator = map.iterator();
        assertFalse( iterator.hasNext() );

        map.put( KEY_ONE, 1 );
        map.put( KEY_TWO, 2 );

        iterator = map.iterator();
        assertTrue( iterator.hasNext() );
        iterator.advance();
		boolean found_one;
		if ( iterator.value() == 1 ) {
			assertEquals( KEY_ONE, iterator.key() );
			found_one = true;
		}
		else {
			assertEquals( 2, iterator.value() );
			assertEquals( KEY_TWO, iterator.key() );
			found_one = false;
		}


        assertTrue( iterator.hasNext() );
        iterator.advance();
		if ( found_one ) {
			assertEquals( 2, iterator.value() );
			assertEquals( KEY_TWO, iterator.key() );
		}
		else {
			assertEquals( 1, iterator.value() );
			assertEquals( KEY_ONE, iterator.key() );
		}

        assertFalse( iterator.hasNext() );

        int key = iterator.key();
        long old_value = iterator.value();

		if ( found_one ) {
			assertEquals( 2, old_value );
			assertEquals( KEY_TWO, iterator.key() );
		}
		else {
			assertEquals( 1, old_value );
			assertEquals( KEY_ONE, iterator.key() );
		}

        assertEquals( old_value, iterator.setValue( old_value * 10 ) );
        assertEquals( old_value * 10, iterator.value() );

        assertFalse( map.containsValue( old_value ) );
        assertTrue( map.containsValue( old_value * 10 ) );
        assertEquals( old_value * 10, map.get( key ) );

        iterator.remove();
        assertFalse( map.containsValue( old_value * 10 ) );
        assertEquals( map.getNoEntryValue(), map.get( key ) );
        assertEquals( 1, map.size() );
    }


    public void testAdjustValue() {
        TIntLongHashMap map = new TIntLongHashMap();

        map.put( KEY_ONE, 1 );

        boolean changed = map.adjustValue( KEY_ONE, 1 );
        assertTrue( changed );
        assertEquals( 2, map.get( KEY_ONE ) );

        changed = map.adjustValue( KEY_ONE, 5 );
        assertTrue( changed );
        assertEquals( 7, map.get( KEY_ONE ) );

        changed = map.adjustValue( KEY_ONE, -3 );
        assertTrue( changed );
        assertEquals( 4, map.get( KEY_ONE ) );

        changed = map.adjustValue( KEY_TWO, 1 );
        assertFalse( changed );
        assertFalse( map.containsKey( KEY_TWO ) );
    }


    public void testAdjustOrPutValue() {
        TIntLongMap map = new TIntLongHashMap();

        map.put( KEY_ONE, 1 );

        long new_value = map.adjustOrPutValue( KEY_ONE, 1, 100 );
        assertEquals( 2, new_value );
        assertEquals( 2, map.get( KEY_ONE ) );

        new_value = map.adjustOrPutValue( KEY_ONE, 5, 100 );
        assertEquals( 7, new_value );
        assertEquals( 7, map.get( KEY_ONE ) );

        new_value = map.adjustOrPutValue( KEY_ONE, -3, 100 );
        assertEquals( 4, new_value );
        assertEquals( 4, map.get( KEY_ONE ) );

        new_value = map.adjustOrPutValue( KEY_TWO, 1, 100 );
        assertEquals( 100, new_value );
        assertTrue( map.containsKey( KEY_TWO ) );
        assertEquals( 100, map.get( KEY_TWO ) );

        new_value = map.adjustOrPutValue( KEY_TWO, 1, 100 );
        assertEquals( 101, new_value );
        assertEquals( 101, map.get( KEY_TWO ) );
    }


    /**
     * Test for tracking issue #1204014. +0.0 and -0.0 have different bit patterns, but
     * should be counted the same as keys in a map. Checks for doubles and floats.
     */
    // TODO: move to TPrimitiveObjectHashMap test.
    public void testFloatZeroHashing() {
//        TDoubleObjectHashMap<String> po_double_map = new TDoubleObjectHashMap<String>();
//        TDoubleIntHashMap pp_double_map = new TDoubleIntHashMap();
//        TFloatObjectHashMap<String> po_float_map = new TFloatObjectHashMap<String>();
//        TFloatIntHashMap pp_float_map = new TFloatIntHashMap();
//
//        final double zero_double = 0.0;
//        final double negative_zero_double = -zero_double;
//        final float zero_float = 0.0f;
//        final float negative_zero_float = -zero_float;
//
//        // Sanity check... make sure I'm really creating two different values.
//        final String zero_bits_double =
//                Long.toBinaryString( Double.doubleToLongBits( zero_double ) );
//        final String negative_zero_bits_double =
//                Long.toBinaryString( Double.doubleToLongBits( negative_zero_double ) );
//        assertFalse( zero_bits_double + " == " + negative_zero_bits_double,
//                zero_bits_double.equals( negative_zero_bits_double ) );
//
//        final String zero_bits_float =
//                Integer.toBinaryString( Float.floatToIntBits( zero_float ) );
//        final String negative_zero_bits_float =
//                Integer.toBinaryString( Float.floatToIntBits( negative_zero_float ) );
//        assertFalse( zero_bits_float + " == " + negative_zero_bits_float,
//                zero_bits_float.equals( negative_zero_bits_float ) );
//
//
//        po_double_map.put( zero_double, "Zero" );
//        po_double_map.put( negative_zero_double, "Negative Zero" );
//
//        pp_double_map.put( zero_double, 0 );
//        pp_double_map.put( negative_zero_double, -1 );
//
//        po_float_map.put( zero_float, "Zero" );
//        po_float_map.put( negative_zero_float, "Negative Zero" );
//
//        pp_float_map.put( zero_float, 0 );
//        pp_float_map.put( negative_zero_float, -1 );
//
//
//        assertEquals( 1, po_double_map.size() );
//        assertEquals( po_double_map.get( zero_double ), "Negative Zero" );
//        assertEquals( po_double_map.get( negative_zero_double ), "Negative Zero" );
//
//        assertEquals( 1, pp_double_map.size() );
//        assertEquals( pp_double_map.get( zero_double ), -1 );
//        assertEquals( pp_double_map.get( negative_zero_double ), -1 );
//
//        assertEquals( 1, po_float_map.size() );
//        assertEquals( po_float_map.get( zero_float ), "Negative Zero" );
//        assertEquals( po_float_map.get( negative_zero_float ), "Negative Zero" );
//
//        assertEquals( 1, pp_float_map.size() );
//        assertEquals( pp_float_map.get( zero_float ), -1 );
//        assertEquals( pp_float_map.get( negative_zero_float ), -1 );
//
//
//        po_double_map.put( zero_double, "Zero" );
//        pp_double_map.put( zero_double, 0 );
//        po_float_map.put( zero_float, "Zero" );
//        pp_float_map.put( zero_float, 0 );
//
//
//        assertEquals( 1, po_double_map.size() );
//        assertEquals( po_double_map.get( zero_double ), "Zero" );
//        assertEquals( po_double_map.get( negative_zero_double ), "Zero" );
//
//        assertEquals( 1, pp_double_map.size() );
//        assertEquals( pp_double_map.get( zero_double ), 0 );
//        assertEquals( pp_double_map.get( negative_zero_double ), 0 );
//
//        assertEquals( 1, po_float_map.size() );
//        assertEquals( po_float_map.get( zero_float ), "Zero" );
//        assertEquals( po_float_map.get( negative_zero_float ), "Zero" );
//
//        assertEquals( 1, pp_float_map.size() );
//        assertEquals( pp_float_map.get( zero_float ), 0 );
//        assertEquals( pp_float_map.get( negative_zero_float ), 0 );
    }


    public void testPutIfAbsent() {
        TIntLongMap map = new TIntLongHashMap();

        map.put( 1, 10 );
        map.put( 2, 20 );
        map.put( 3, 30 );

        assertEquals( 10, map.putIfAbsent( 1, 111 ) );
        assertEquals( 10, map.get( 1 ) );
        assertEquals( map.getNoEntryValue(), map.putIfAbsent( 9, 90 ) );
        assertEquals( 90, map.get( 9 ) );
    }


    public void testBug2037709() {
        TIntLongMap m = new TIntLongHashMap();
        for ( int i = 0; i < 10; i++ ) {
            m.put( i, i );
        }

        int sz = m.size();
        assertEquals( 10, sz );

        int[] keys = new int[sz];
        m.keys( keys );

        boolean[] seen = new boolean[sz];
        Arrays.fill( seen, false );
        for ( int i = 0; i < 10; i++ ) {
            seen[keys[i]] = true;
        }

        for ( int i = 0; i < 10; i++ ) {
            if ( !seen[i] ) {
                TestCase.fail( "Missing key for: " + i );
            }
        }
    }


    public void testEquals() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }
        assertEquals( map, map );

        TIntIntMap int_map = new TIntIntHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            int_map.put( keys[i], (int) vals[i] );
        }
        assertFalse( map.equals( int_map ) );

        // Change a value.. 
        TIntLongMap unequal = new TIntLongHashMap( map );
        map.put( keys[3], vals[3] + 1 );
        assertFalse( map.equals( unequal ) );

        // Change length
        unequal = new TIntLongHashMap( map );
        map.put( 13, 26 );
        assertFalse( map.equals( unequal ) );
    }


    public void testHashCode() {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        TIntLongMap other = new TIntLongHashMap();
        other.putAll( map );
        assertTrue( "hashcodes incorrectly not equal: " + map + ", " + other,
                map.hashCode() == other.hashCode() );

        TIntLongMap unequal = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            unequal.put( keys[i], keys[i] );
        }
        assertFalse( "hashcodes unlikely equal: " + map + ", " + unequal,
                map.hashCode() == unequal.hashCode() );

        int[] mismatched = {72, 49, 53, 1024, 999};
        TIntLongMap mismatched_map = new TIntLongHashMap();
        for ( int i = 0; i < mismatched.length; i++ ) {
            mismatched_map.put( mismatched[i], mismatched[i] * 37 );
        }
        assertFalse( "hashcodes unlikely equal: " + map + ", " + unequal,
                map.hashCode() == unequal.hashCode() );

    }



    public void testToString() {
        TIntLongMap m = new TIntLongHashMap();
        m.put( 11, 1 );
        m.put( 22, 2 );

        String to_string = m.toString();
        assertTrue( to_string,
			to_string.equals( "{11=1, 22=2}" ) || to_string.equals( "{22=2, 11=1}" ) );
    }


    public void testSerialize() throws Exception {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap map = new TIntLongHashMap();
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            map.put( keys[i], vals[i] );
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( map );

        ByteArrayInputStream bias = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bias );

        TIntLongMap deserialized = (TIntLongMap) ois.readObject();

        assertEquals( map, deserialized );
    }

    /** a non TIntLongHashMap to test putAll exception */
//    class BadMap implements TIntLongMap  {
//        public int getNoEntryKey() { return 0; }
//        public long getNoEntryValue() { return 0; }
//        public long put( int key, long value ) { return 0; }
//        public long putIfAbsent( int key, long value ) { return 0; }
//        public void putAll( Map<Integer, Long> map ) {}
//        public void putAll( TIntLongMap map ) {}
//        public long get( int key ) { return 0; }
//        public void clear() {}
//        public long remove( int key ) { return 0; }
//        public int size() { return 0; }
//        public TIntSet keySet() { return null; }
//        public int[] keys() { return new int[0]; }
//        public int[] keys( int[] array ) { return new int[0]; }
//        public TLongCollection valueCollection() { return null; }
//        public long[] values() { return new long[0]; }
//        public long[] values( long[] array ) { return new long[0]; }
//        public boolean containsValue( long val ) { return false; }
//        public boolean containsKey( int key ) { return false; }
//        public TIntLongIterator iterator() { return null; }
//        public boolean forEachKey( TIntProcedure procedure ) { return false; }
//        public boolean forEachValue( TLongProcedure procedure ) { return false; }
//        public boolean forEachEntry( TIntLongProcedure procedure ) { return false; }
//        public void transformValues( TLongFunction function ) { }
//        public boolean retainEntries( TIntLongProcedure procedure ) { return false; }
//        public boolean increment( int key ) { return false; }
//        public boolean adjustValue( int key, long amount ) { return false; }
//        public long adjustOrPutValue( int key, long adjust_amount, long put_amount ) { return 0; }
//        public boolean isEmpty() { return true; }
//    }
}
