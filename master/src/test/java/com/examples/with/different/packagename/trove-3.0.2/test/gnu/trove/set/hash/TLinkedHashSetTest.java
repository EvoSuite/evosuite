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


package gnu.trove.set.hash;

import junit.framework.TestCase;
import org.omg.CORBA.portable.Streamable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;


/**
 * Created: Sat Nov  3 10:33:15 2001
 *
 * @author Eric D. Friedman
 * @author Rob Eden
 * @author Jeff Randall
 * @version $Id: THashSetTest.java,v 1.1.2.3 2010/03/02 04:09:50 robeden Exp $
 */

public class TLinkedHashSetTest extends TestCase {

    public TLinkedHashSetTest(String name) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
    }


    public void tearDown() throws Exception {
        super.tearDown();
    }


    public void testConstructors() throws Exception {
        Set<String> set = new TLinkedHashSet<String>();
        assertNotNull( set );

        String[] strings = {"a", "b", "c", "d"};
        set.addAll( Arrays.asList( strings ) );

        Set<String> copy = new TLinkedHashSet<String>( set );
        assertTrue( "set not a copy: " + set + ", " + copy, set.equals( copy ) );

        Set<String> another = new TLinkedHashSet<String>( 20 );
        another.addAll( Arrays.asList( strings ) );
        assertTrue( "set not equal: " + set + ", " + copy, set.equals( another ) );

        another = new TLinkedHashSet<String>( 2, 1.0f );
        another.addAll( Arrays.asList( strings ) );
        assertTrue( "set not equal: " + set + ", " + copy, set.equals( another ) );
    }


    public void testIsEmpty() throws Exception {
        Set<String> s = new TLinkedHashSet<String>();
        assertTrue( "new set wasn't empty", s.isEmpty() );

        s.add( "One" );
        assertTrue( "set with element reports empty", !s.isEmpty() );
        s.clear();
        assertTrue( "cleared set reports not-empty", s.isEmpty() );
    }


    public void testContains() throws Exception {
        Set<String> s = new TLinkedHashSet<String>();
        String o = "testContains";
        s.add( o );
        assertTrue( "contains failed", s.contains( o ) );
    }


    @SuppressWarnings({"ForLoopReplaceableByForEach"})
    public void testContainsAll() throws Exception {
        Set<String> s = new TLinkedHashSet<String>();
        String[] o = {"Hello World", "Goodbye World", "Hello Goodbye"};
        s.addAll( Arrays.asList( o ) );
        for ( int i = 0; i < o.length; i++ ) {
            assertTrue( o[i], s.contains( o[i] ) );
        }
        assertTrue( "containsAll failed: " + s,
                s.containsAll( Arrays.asList( o ) ) );

        String[] more = {"Hello World", "Goodbye World", "Hello Goodbye", "Not There"};
        assertFalse( "containsAll failed: " + s,
                s.containsAll( Arrays.asList( more ) ) );
    }


    public void testRetainAll() throws Exception {
        Set<String> set = new TLinkedHashSet<String>();
        String[] strings = {"Hello World", "Goodbye World", "Hello Goodbye", "Remove Me"};
        set.addAll( Arrays.asList( strings ) );
        for ( String string : strings ) {
            assertTrue( string, set.contains( string ) );
        }

        String[] retain = {"Hello World", "Goodbye World", "Hello Goodbye"};
        assertTrue( "retainAll failed: " + set,
                set.retainAll( Arrays.asList( retain ) ) );
        assertTrue( "containsAll failed: " + set,
                set.containsAll( Arrays.asList( retain ) ) );
    }


    public void testRemoveAll() throws Exception {
        Set<String> set = new TLinkedHashSet<String>();
        String[] strings = {"Hello World", "Goodbye World", "Hello Goodbye", "Keep Me"};
        set.addAll( Arrays.asList( strings ) );
        for ( String string : strings ) {
            assertTrue( string, set.contains( string ) );
        }

        String[] remove = {"Hello World", "Goodbye World", "Hello Goodbye"};
        assertTrue( "removeAll failed: " + set,
                set.removeAll( Arrays.asList( remove ) ) );
        assertTrue( "removeAll failed: " + set,
                set.containsAll( Arrays.asList( "Keep Me" ) ) );

        for ( String element : remove ) {
            assertFalse( element + " still in set: " + set, set.contains( element ) );
        }
        assertEquals( 1, set.size() );
    }


    public void testAdd() throws Exception {
        Set<String> s = new TLinkedHashSet<String>();
        assertTrue( "add failed", s.add( "One" ) );
        assertTrue( "duplicated add succeded", !s.add( "One" ) );
    }


    public void testRemove() throws Exception {
        Set<String> s = new TLinkedHashSet<String>();
        s.add( "One" );
        s.add( "Two" );
        assertTrue( "One was not added", s.contains( "One" ) );
        assertTrue( "One was not removed", s.remove( "One" ) );
        assertTrue( "One was not removed", !s.contains( "One" ) );
        assertTrue( "Two was removed", s.contains( "Two" ) );
        assertEquals( 1, s.size() );
    }


    public void testRemoveObjectNotInSet() throws Exception {
        Set<String> set = new TLinkedHashSet<String>();
        set.add( "One" );
        set.add( "Two" );
        assertTrue( "One was not added", set.contains( "One" ) );
        assertTrue( "One was not removed", set.remove( "One" ) );
        assertTrue( "One was not removed", !set.contains( "One" ) );
        assertTrue( "Two was removed", set.contains( "Two" ) );
        assertFalse( "Three was removed (non-existant)", set.remove( "Three" ) );
        assertEquals( 1, set.size() );
    }


    public void testSize() throws Exception {
        Set<Object> o = new TLinkedHashSet<Object>();
        assertEquals( "initial size was not 0", 0, o.size() );

        for ( int i = 0; i < 99; i++ ) {
            o.add( new Object() );
            assertEquals( "size did not increase after add", i + 1, o.size() );
        }
    }


    public void testClear() throws Exception {
        Set<String> s = new TLinkedHashSet<String>();
        s.addAll( Arrays.asList( "one", "two", "three" ) );
        assertEquals( "size was not 3", 3, s.size() );
        s.clear();
        assertEquals( "initial size was not 0", 0, s.size() );
    }

    public void testIterationOrder() throws Exception {
        TLinkedHashSet<String> lhs = new TLinkedHashSet<String>();
        Set<String> s = lhs;
        s.add("a");
        s.add("b");
        s.add("c");
        //
        Iterator<String> it = s.iterator();
        //
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        //
        s.add("a");
        it = s.iterator();
        //
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        //
        s.remove("a");
        s.add("a");
        //
        it = s.iterator();
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertEquals("a", it.next());
        //
        lhs.compact();
        it = s.iterator();
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertEquals("a", it.next());
    }

    public void testIteratorRemove() throws Exception {
        Set<String> s = new TLinkedHashSet<String>();
        s.add("a");
        s.add("b");
        s.add("c");
        //
        Iterator<String> it = s.iterator();
        //
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        it = s.iterator();
        //
        assertEquals("a", it.next());
        it.remove();
        assertEquals("b", it.next());
        assertEquals("c", it.next());

        it = s.iterator();
        //
        assertEquals("b", it.next());
        it.remove();
        assertEquals("c", it.next());

        it = s.iterator();
        //
        assertEquals("c", it.next());
        it.remove();

        assertTrue(s.isEmpty());
    }

    public void testSerialize() throws Exception {
        Set<String> s = new TLinkedHashSet<String>();
        s.addAll( Arrays.asList( "one", "two", "three" ) );
        assertEquals(s, s);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( s );

        ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bais );

        TLinkedHashSet s2 = (TLinkedHashSet) ois.readObject();

        assertEquals( s, s2 );
    }


    public void testToArray() {
        Set<String> s = new TLinkedHashSet<String>();
        String[] str = {"hi", "bye", "hello", "goodbye"};
        s.addAll( Arrays.asList( str ) );
        Object[] res = s.toArray();
        Arrays.sort( str );
        Arrays.sort( res );
        assertTrue( Arrays.equals( str, res ) );
    }


    public void testToArrayWithParams() {
        Set<String> s = new TLinkedHashSet<String>();
        String[] str = {"hi", "bye", "hello", "goodbye"};
        s.addAll( Arrays.asList( str ) );
        String[] sink = new String[str.length + 2];
        sink[sink.length - 1] = "residue";
        sink[sink.length - 2] = "will be cleared";
        String[] res = s.toArray( sink );

        Set<String> copy = new HashSet<String>();
        copy.addAll( Arrays.asList( res ) );

        Set<String> bogey = new HashSet<String>();
        bogey.addAll( Arrays.asList( str ) );
        bogey.add( "residue" );
        bogey.add( null );
        assertEquals( bogey, copy );
    }


    @SuppressWarnings({"ToArrayCallWithZeroLengthArrayArgument", "SuspiciousToArrayCall"})
    public void testToArrayAnotherType() throws Exception {
        Set<Number> set = new TLinkedHashSet<Number>();
        Number[] nums = {1138, 42, 86, 99, 101};
        set.addAll( Arrays.asList( nums ) );

        Integer[] to_int_array_zero = set.toArray( new Integer[0] );
        assertTrue( "set and array mismatch: " + set +
                    ", " + Arrays.asList( to_int_array_zero ),
                set.containsAll( Arrays.asList( to_int_array_zero ) ) );

        Integer[] to_int_array_size = set.toArray( new Integer[set.size()] );
        assertTrue( "set and array mismatch: " + set +
                    ", " + Arrays.asList( to_int_array_size ),
                set.containsAll( Arrays.asList( to_int_array_size ) ) );


        Number[] to_num_array_zero = set.toArray( new Number[0] );
        assertTrue( "set and array mismatch: " + set +
                    ", " + Arrays.asList( to_num_array_zero ),
                set.containsAll( Arrays.asList( to_num_array_zero ) ) );

        Number[] to_num_array_size = set.toArray( new Number[set.size()] );
        assertTrue( "set and array mismatch: " + set +
                    ", " + Arrays.asList( to_num_array_size ),
                set.containsAll( Arrays.asList( to_num_array_size ) ) );
    }


    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    public void testRehashing() throws Exception {
        Set<Integer> s = new TLinkedHashSet<Integer>();
        for ( int i = 0; i < 10000; i++ ) {
            s.add( new Integer( i ) );
        }
    }


    /**
     * this tests that we throw when people violate the
     * general contract for hashcode on java.lang.Object
     */
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    public void testSomeBadlyWrittenObject() {
        Set<Object> s = new TLinkedHashSet<Object>();
        boolean didThrow = false;
        int i = 0;
        try {
            for (; i < 101; i++ ) {
                s.add( new Crap() );
            }
        }
        catch ( IllegalArgumentException e ) {
            didThrow = true;
        }
        assertTrue( "expected TLinkedHashSet to throw an IllegalArgumentException", didThrow );
    }


    public void testIterable() {

        Set<String> set = new TLinkedHashSet<String>();
        set.add( "One" );
        set.add( "Two" );

        for ( String s : set ) {
            assertTrue( s.equals( "One" ) || s.equals( "Two" ) );
        }
    }


    public void testToString() {
        Set<String> set = new TLinkedHashSet<String>();
        set.add( "One" );
        set.add( "Two" );

        String to_string = set.toString();
        assertTrue( to_string,
			to_string.equals( "{One, Two}" ) || to_string.equals( "{Two, One}" ) );
    }


    public void testEquals() {
        String[] strings = {"hi", "bye", "hello", "goodbye"};
        Set<String> set = new TLinkedHashSet<String>();
        set.addAll( Arrays.asList( strings ) );
        Set<String> other = new TLinkedHashSet<String>();
        other.addAll( Arrays.asList( strings ) );

        assertTrue( "sets incorrectly not equal: " + set + ", " + other,
                set.equals( other ) );

        String[] mismatched = {"heyya", "whassup", "seeya", "blargh"};
        Set<String> unequal = new TLinkedHashSet<String>();
        unequal.addAll( Arrays.asList( mismatched ) );

        assertFalse( "sets incorrectly equal: " + set + ", " + unequal,
                set.equals( unequal ) );

        // Change length, different code branch
        unequal.add( "whee!" );
        assertFalse( "sets incorrectly equal: " + set + ", " + unequal,
                set.equals( unequal ) );
    }


    public void testEqualsNonSet() {
        String[] strings = {"hi", "bye", "hello", "goodbye"};
        Set<String> set = new TLinkedHashSet<String>();
        set.addAll( Arrays.asList( strings ) );
        List<String> other = new ArrayList<String>();
        other.addAll( Arrays.asList( strings ) );

        assertFalse( "sets incorrectly equals list: " + set + ", " + other,
                set.equals( other ) );

        Map<String, String> map = new HashMap<String, String>();
        for ( String string : strings ) {
            map.put( string, string );
        }
        assertFalse( "sets incorrectly equals map: " + set + ", " + map,
                set.equals( map ) );
    }


    public void testHashcode() {
        String[] strings = {"hi", "bye", "hello", "goodbye"};
        Set<String> set = new TLinkedHashSet<String>();
        set.addAll( Arrays.asList( strings ) );
        Set<String> other = new TLinkedHashSet<String>();
        other.addAll( Arrays.asList( strings ) );

        assertTrue( "hashcodes incorrectly not equal: " + set + ", " + other,
                set.hashCode() == other.hashCode() );

        String[] mismatched = {"heyya", "whassup", "seeya", "blargh"};
        Set<String> unequal = new TLinkedHashSet<String>();
        unequal.addAll( Arrays.asList( mismatched ) );

        assertFalse( "hashcodes unlikely equal: " + set + ", " + unequal,
                set.hashCode() == unequal.hashCode() );
    }


    public void testCompact() {
        int max_size = 10000;
        int reduced_size = 100;

        TLinkedHashSet<Integer> set = new TLinkedHashSet<Integer>( max_size, 1.0f );
        for ( int index = 1; index <= max_size; index++ ) {
            set.add( Integer.valueOf( index ) );
        }
        assertEquals( max_size, set.size() );
        int max_length = set._set.length;

        for ( int index = max_size; index > reduced_size; index-- ) {
            set.remove( Integer.valueOf( index ) );
        }
        assertEquals( reduced_size, set.size() );

        set.compact();
        int compacted_length = set._set.length;
        assertFalse( max_length + " should != " + compacted_length,
                max_length == compacted_length );
    }


    public void testDisabledAutoCompact() {
        int max_size = 10000;
        int reduced_size = 100;

        TLinkedHashSet<Integer> set = new TLinkedHashSet<Integer>( max_size, 1.0f );
        set.setAutoCompactionFactor( 0.0f );    // Disable
        for ( int index = 1; index <= max_size; index++ ) {
            set.add( Integer.valueOf( index ) );
        }
        assertEquals( max_size, set.size() );
        int max_length = set._set.length;

        for ( int index = max_size; index > reduced_size; index-- ) {
            set.remove( Integer.valueOf( index ) );
        }
        assertEquals( reduced_size, set.size() );
        int uncompacted_length = set._set.length;
        assertEquals( max_length, uncompacted_length );

        set.compact();
        int compacted_length = set._set.length;
        assertFalse( uncompacted_length + " should != " + compacted_length,
                uncompacted_length == compacted_length );
    }


    // in this junk class, all instances hash to the same
    // address, but some objects claim to be equal where
    // others do not.
    public static class Crap {

        public boolean equals( Object other ) {
            return other instanceof Crap;
        }


        public int hashCode() {
            return System.identityHashCode( this );
        }
    }


    public static void main( String[] args ) throws Exception {
        junit.textui.TestRunner.run( new TLinkedHashSetTest( "testBadlyWrittenObject" ) );
    }
} // TLinkedHashSetTests
