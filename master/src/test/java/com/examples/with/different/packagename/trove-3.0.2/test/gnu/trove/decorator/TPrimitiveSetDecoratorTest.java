///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2002, Eric D. Friedman All Rights Reserved.
// Copyright (c) 2009, Robert D. Eden All Rights Reserved.
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

package gnu.trove.decorator;

import junit.framework.TestCase;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.TDecorators;

import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;




/**
 * @author Eric D. Friedman
 * @author Robert D. Eden
 * @author Jeff Randall
 */
public class TPrimitiveSetDecoratorTest extends TestCase {

    public TPrimitiveSetDecoratorTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
    }


    public void tearDown() throws Exception {
        super.tearDown();
    }


    public void testConstructors() throws Exception {
        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        assertNotNull( set );

        Integer[] ints = {1138, 42, 86, 99, 101};
        set.addAll( Arrays.asList( ints ) );

        Set<Integer> copy = new HashSet<Integer>( set );
        assertTrue( "set not a copy: " + set + ", " + copy, set.equals( copy ) );

        TIntSet raw_another = new TIntHashSet( 20 );
        Set<Integer> another = TDecorators.wrap( raw_another );
        another.addAll( Arrays.asList( ints ) );
        assertTrue( "set not equal: " + set + ", " + copy, set.equals( another ) );

        raw_another = new TIntHashSet( 2, 1.0f );
        another = TDecorators.wrap( raw_another );
        another.addAll( Arrays.asList( ints ) );
        assertTrue( "set not equal: " + set + ", " + copy, set.equals( another ) );

        raw_another = new TIntHashSet( Arrays.asList( ints ) );
        another = TDecorators.wrap( raw_another );
        assertTrue( "set not equal: " + set + ", " + copy, set.equals( another ) );
    }


    public void testIsEmpty() throws Exception {
        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        assertTrue( "new set wasn't empty", set.isEmpty() );

        set.add( 1 );
        assertFalse( "set with element reports empty", set.isEmpty() );
        set.clear();
        assertTrue( "cleared set reports not-empty", set.isEmpty() );
    }


    public void testContains() throws Exception {
        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        int i = 100;
        set.add( i );
        assertTrue( "contains failed", set.contains( i ) );
        assertFalse( "contains failed", set.contains( 1000 ) );
    }


    @SuppressWarnings({"ForLoopReplaceableByForEach"})
    public void testContainsAll() throws Exception {

        Integer[] ints = {1138, 42, 13, 86, 99};

        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        set.addAll( Arrays.asList( ints ) );

        TIntSet raw_other = new TIntHashSet();
        Set<Integer> other = TDecorators.wrap( raw_other );
        other.addAll( Arrays.asList( ints ) );

        List<Integer> ints_list = new ArrayList<Integer>();
        ints_list.addAll( Arrays.asList( ints ) );

        for ( int index = 0; index < ints.length; index++ ) {
            assertTrue( Integer.valueOf( ints[index] ).toString(),
                    set.contains( ints[index] ) );
        }

        assertTrue( "containsAll(Collection<?>) failed: " + set,
                set.containsAll( ints_list ) );

        assertTrue( "containsAll(TIntSet) failed (same set): " + set,
                set.containsAll( set ) );

        assertTrue( "containsAll(TIntSet) failed (other set): " + set,
                set.containsAll( other ) );


        Integer[] failed = {42, 86, 99, 123456};

        TIntSet raw_failed_set = new TIntHashSet();
        Set<Integer> failed_set = TDecorators.wrap( raw_failed_set );
        failed_set.addAll( Arrays.asList( failed ) );

        List<Integer> failed_list = new ArrayList<Integer>();
        failed_list.addAll( Arrays.asList( failed ) );

        assertFalse( "containsAll(Collection<?>) failed (false positive): " + set,
                set.containsAll( failed_list ) );

        assertFalse( "containsAll(TIntSet) failed (false positive): " + set,
                set.containsAll( failed_set ) );
    }


    public void testAddAll() throws Exception {

        Integer[] ints = {1138, 42, 13, 86, 99, 101};

        TIntSet raw_set;

        List<Integer> list = new ArrayList<Integer>();
        for ( int element : ints ) {
            list.add( Integer.valueOf( element ) );
        }

        raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        assertTrue( "addAll(Collection<?>) failed: " + set, set.addAll( list ) );
        for ( int element : ints ) {
            assertTrue( "contains failed: ", set.contains( element ) );
        }

        TIntSet raw_test_set = new TIntHashSet();
        Set<Integer> test_set = TDecorators.wrap( raw_test_set );
        assertTrue( "addAll(TIntSet) failed: " + test_set, test_set.addAll( set ) );
        for ( int element : ints ) {
            assertTrue( "contains failed: ", set.contains( element ) );
        }
    }


    public void testRetainAll() throws Exception {

        Integer[] ints = {1138, 42, 13, 86, 99, 101};

        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        set.addAll( Arrays.asList( ints ) );

        TIntSet other = new TIntHashSet();
        other.addAll( Arrays.asList( ints ) );

        Integer[] to_retain = {13, 86, 99, 1138};

        TIntSet raw_retain_set = new TIntHashSet();
        Set<Integer> retain_set = TDecorators.wrap( raw_retain_set );
        retain_set.addAll( Arrays.asList( to_retain ) );

        List<Integer> retain_list = new ArrayList<Integer>();
        retain_list.addAll( Arrays.asList( to_retain ) );

        assertFalse( "retainAll(Set) failed (same set): " + set,
                set.retainAll( set ) );
        // Contains all the original elements
        assertTrue( set.toString(), set.containsAll( Arrays.asList( ints ) ) );
        assertTrue( retain_set.toString(), retain_set.containsAll( Arrays.asList( to_retain ) ) );

        assertTrue( "retainAll(Collection<?>) failed: " + set,
                set.retainAll( retain_list ) );
        // Contains just the expected elements
        assertFalse( set.toString(), set.containsAll( Arrays.asList( ints ) ) );
        assertTrue( set.toString(), set.containsAll( Arrays.asList( to_retain ) ) );
        assertTrue( retain_set.toString(), retain_set.containsAll( Arrays.asList( to_retain ) ) );

        // reset the set.
        raw_set = new TIntHashSet();
        set = TDecorators.wrap( raw_set );
        set.addAll( Arrays.asList( ints ) );
        assertTrue( "retainAll(TIntSet) failed: " + set,
                set.retainAll( retain_set ) );
        // Contains just the expected elements
        assertFalse( set.toString(), set.containsAll( Arrays.asList( ints ) ) );
        assertTrue( set.toString(), set.containsAll( Arrays.asList( to_retain ) ) );
        assertTrue( retain_set.toString(), retain_set.containsAll( Arrays.asList( to_retain ) ) );
    }


    public void testRemoveAll() throws Exception {

        Integer[] ints = {1138, 42, 13, 86, 99, 101};

        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        set.addAll( Arrays.asList( ints ) );

        TIntSet raw_other = new TIntHashSet();
        Set<Integer> other = TDecorators.wrap( raw_other );
        other.addAll( Arrays.asList( ints ) );

        Integer[] to_remove = {13, 86, 99, 1138};

        TIntSet raw_remove_set = new TIntHashSet();
        Set<Integer> remove_set = TDecorators.wrap( raw_remove_set );
        remove_set.addAll( Arrays.asList( to_remove ) );

        List<Integer> remove_list = new ArrayList<Integer>();
        remove_list.addAll( Arrays.asList( to_remove ) );

        Integer[] remainder = {42, 101};

        assertTrue( "removeAll(Collections<?>) failed (same set): " + set,
                set.removeAll( set ) );

        // reset the set.
        raw_set = new TIntHashSet();
        set = TDecorators.wrap( raw_set );
        set.addAll( Arrays.asList( ints ) );
        assertTrue( "removeAll(Collection<?>) failed: " + set,
                set.removeAll( remove_list ) );
        // Contains just the expected elements
        assertTrue( set.toString(), set.containsAll( Arrays.asList( remainder ) ) );
        assertFalse( set.toString(), set.containsAll( Arrays.asList( to_remove ) ) );
        assertTrue( remove_set.toString(), remove_set.containsAll( Arrays.asList( to_remove ) ) );

        // reset the set.
        raw_set = new TIntHashSet();
        set = TDecorators.wrap( raw_set );
        set.addAll( Arrays.asList( ints ) );
        assertTrue( "removeAll(TIntSet) failed: " + set,
                set.removeAll( remove_set ) );
        // Contains just the expected elements
        assertTrue( set.toString(), set.containsAll( Arrays.asList( remainder ) ) );
        assertFalse( set.toString(), set.containsAll( Arrays.asList( to_remove ) ) );
        assertTrue( remove_set.toString(), remove_set.containsAll( Arrays.asList( to_remove ) ) );
    }


    public void testAdd() throws Exception {
        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        assertTrue( "add failed", set.add( 1 ) );
        assertFalse( "duplicated add modified set", set.add( 1 ) );
    }


    public void testRemove() throws Exception {
        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        set.add( 1 );
        set.add( 2 );
        assertTrue( "One was not added", set.contains( 1 ) );
        assertTrue( "One was not removed", set.remove( 1 ) );
        assertFalse( "One was not removed", set.contains( 1 ) );
        assertTrue( "Two was also removed", set.contains( 2 ) );
    }


    public void testRemoveNonExistant() throws Exception {
        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        set.add( 1 );
        set.add( 2 );
        assertTrue( "One was not added", set.contains( 1 ) );
        assertTrue( "One was not removed", set.remove( 1 ) );
        assertFalse( "One was not removed", set.contains( 1 ) );
        assertTrue( "Two was also removed", set.contains( 2 ) );
        assertFalse( "Three was removed (non-existant)", set.remove( 3 ) );
    }


    public void testSize() throws Exception {
        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        assertEquals( "initial size was not 0", 0, set.size() );

        for ( int i = 0; i < 99; i++ ) {
            set.add( i );
            assertEquals( "size did not increase after add", i + 1, set.size() );
        }
    }


    public void testClear() throws Exception {
        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        set.add( 1 );
        set.add( 2 );
        set.add( 3 );
        assertEquals( "size was not 3", 3, set.size() );
        set.clear();
        assertEquals( "initial size was not 0", 0, set.size() );
    }


    public void testSerialize() throws Exception {
        Integer[] ints = {1138, 42, 86, 99, 101};

        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        set.addAll( Arrays.asList( ints ) );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( set );

        ByteArrayInputStream bias = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bias );

        //noinspection unchecked
        Set<Integer> deserialized = ( Set<Integer> ) ois.readObject();

        assertEquals( set, deserialized );
    }


    public void testToArray() {
        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        Integer[] ints = {42, 1138, 13, 86, 99};
        set.addAll( Arrays.asList( ints ) );
        Object[] obj_res = set.toArray();
        Arrays.sort( ints );
        Arrays.sort( obj_res );
        assertTrue( Arrays.equals( ints, obj_res ) );

        Object[] res = set.toArray();
        Arrays.sort( ints );
        Arrays.sort( res );
        assertTrue( Arrays.equals( ints, res ) );

        res = set.toArray( new Integer[set.size()] );
        Arrays.sort( ints );
        Arrays.sort( res );
        assertTrue( Arrays.equals( ints, res ) );

    }


    public void testToArrayMatchesIteratorOrder() {
        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        Integer[] ints = {42, 1138, 13, 86, 99};
        set.addAll( Arrays.asList( ints ) );
        Integer[] toarray_ints = set.toArray( new Integer[ints.length] );

        Integer[] iter_ints = new Integer[ints.length];
        Iterator<Integer> iter = set.iterator();

        int index = 0;
        while ( iter.hasNext() ) {
            iter_ints[index++] = iter.next();
        }

        assertTrue( Arrays.equals( iter_ints, toarray_ints ) );
    }


    public void testToArrayWithParams() {
        int no_entry_value = Integer.MIN_VALUE;
        TIntSet raw_set = new TIntHashSet( 10, 0.5f, no_entry_value );
        Set<Integer> set = TDecorators.wrap( raw_set );

        Integer[] ints = {42, 1138, 13, 86, 99};
        set.addAll( Arrays.asList( ints ) );

        Integer[] sink = new Integer[ints.length + 2];
        sink[sink.length - 1] = -1;
        sink[sink.length - 2] = -2;

        Integer[] res = set.toArray( sink );
        assertNull( res[set.size()] );

        Set<Integer> copy = new HashSet<Integer>();
        copy.addAll( Arrays.asList( sink ) );

        Set<Integer> bogey = new HashSet<Integer>();
        bogey.addAll( Arrays.asList( ints ) );
        bogey.add( -1 );
        bogey.add( null );
        assertEquals( bogey, copy );
    }


    public void testRehashing() throws Exception {
        int size = 10000;
        TIntSet set = new TIntHashSet( 10 );
        for ( int i = 0; i < size; i++ ) {
            set.add( i );
        }
        assertEquals( set.size(), size );
    }


    public void testIterator() {

        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        set.add( 1 );
        set.add( 2 );
        set.add( 3 );
        set.add( 4 );

        Iterator<Integer> iter = set.iterator();
        assertTrue( "iterator should have a next item", iter.hasNext() );

        int last = -1;
        while ( iter.hasNext() ) {
            int next = iter.next();
            assertTrue( Integer.valueOf( next ).toString(),
                    next >= 1 && next <= 4 );
            assertTrue( Integer.valueOf( next ).toString(), next != last );
            last = next;
        }

        assertFalse( "iterator should not have a next item", iter.hasNext() );

        assertTrue( "set should contain 1", set.contains( 1 ) );
        assertTrue( "set should contain 2", set.contains( 2 ) );
        assertTrue( "set should contain 3", set.contains( 3 ) );
        assertTrue( "set should contain 4", set.contains( 4 ) );
        assertEquals( 4, set.size() );
    }


    public void testIteratorRemove() {

        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        set.add( 1 );
        set.add( 2 );
        set.add( 3 );
        set.add( 4 );

        Iterator<Integer> iter = set.iterator();
        assertTrue( "iterator should have a next item", iter.hasNext() );

        int last = -1;
        while ( iter.hasNext() ) {
            int next = iter.next();
            assertTrue( next >= 1 && next <= 4 );
            assertTrue( next != last );
            last = next;

            if ( next == 3 ) {
                iter.remove();
            }
        }

        assertFalse( "iterator should not have a next item", iter.hasNext() );

        assertFalse( "set should not contain 3", set.contains( 3 ) );
        assertTrue( "set should contain 1", set.contains( 1 ) );
        assertTrue( "set should contain 2", set.contains( 2 ) );
        assertTrue( "set should contain 4", set.contains( 4 ) );
        assertEquals( 3, set.size() );

    }



    public void testEquals() {
        Integer[] ints = {1138, 42, 86, 99, 101};
        TIntSet raw_set = new TIntHashSet();
        Set<Integer> set = TDecorators.wrap( raw_set );
        set.addAll( Arrays.asList( ints ) );
        TIntSet raw_other = new TIntHashSet();
        Set<Integer> other = TDecorators.wrap( raw_other );
        other.addAll( Arrays.asList( ints ) );

        assertTrue( "sets incorrectly not equal: " + set + ", " + other,
                set.equals( other ) );

        int[] mismatched = {72, 49, 53, 1024, 999};
        TIntSet raw_unequal = new TIntHashSet();
        raw_unequal.addAll( mismatched );
        Set<Integer> unequal = TDecorators.wrap( raw_unequal );

        assertFalse( "sets incorrectly equal: " + set + ", " + unequal,
                set.equals( unequal ) );

        // Change length, different code branch
        unequal.add( 1 );
        assertFalse( "sets incorrectly equal: " + set + ", " + unequal,
                set.equals( unequal ) );

        Set<Number> different_classes = new HashSet<Number>();
        different_classes.addAll( Arrays.asList( ints ) );
        different_classes.remove( Integer.valueOf( 86 ) );
        different_classes.add( Long.valueOf( 86 ) );
        assertFalse( "sets incorrectly equal: " + set + ", " + different_classes,
                set.equals( different_classes ) );

        //noinspection ObjectEqualsNull
        assertFalse( set.equals( null ) );

        // test against TIntSet
        assertTrue( set.equals( raw_other ) );

        //noinspection MismatchedQueryAndUpdateOfCollection 
        TIntSetDecorator decorated = new TIntSetDecorator( raw_other );
        assertTrue( set.equals( decorated.getSet() ) );
    }


    public void testHashcode() {
        int[] ints = {1138, 42, 86, 99, 101};
        TIntSet raw_set = new TIntHashSet();
        raw_set.addAll( ints );
        Set<Integer> set = TDecorators.wrap( raw_set );
        TIntSet raw_other = new TIntHashSet();
        raw_other.addAll( ints );
        Set<Integer> other = TDecorators.wrap( raw_other );

        assertTrue( "hashcodes incorrectly not equal: " + set + ", " + other,
                set.hashCode() == other.hashCode() );

        int[] mismatched = {72, 49, 53, 1024, 999};
        TIntSet unequal = new TIntHashSet();
        unequal.addAll( mismatched );

        assertFalse( "hashcodes unlikely equal: " + set + ", " + unequal,
                set.hashCode() == unequal.hashCode() );
    }
}
