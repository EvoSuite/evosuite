package gnu.trove.set.hash;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;



/** Test the primitive HashSet classes. */
public class TPrimitiveHashSetTest extends TestCase {

    public TPrimitiveHashSetTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
    }


    public void tearDown() throws Exception {
        super.tearDown();
    }


    public void testConstructors() throws Exception {
        TIntSet set = new TIntHashSet();
        assertNotNull( set );

        int[] ints = {1138, 42, 86, 99, 101};
        set.addAll( ints );

        TIntSet copy = new TIntHashSet( set );
        assertTrue( "set not a copy: " + set + ", " + copy, set.equals( copy ) );

        TIntSet another = new TIntHashSet( 20 );
        another.addAll( ints );
        assertTrue( "set not equal: " + set + ", " + copy, set.equals( another ) );

        another = new TIntHashSet( 2, 1.0f );
        another.addAll( ints );
        assertTrue( "set not equal: " + set + ", " + copy, set.equals( another ) );

        another = new TIntHashSet( ints );
        assertTrue( "set not equal: " + set + ", " + copy, set.equals( another ) );
    }


    public void testIsEmpty() throws Exception {
        TIntSet s = new TIntHashSet();
        assertTrue( "new set wasn't empty", s.isEmpty() );

        s.add( 1 );
        assertTrue( "set with element reports empty", !s.isEmpty() );
        s.clear();
        assertTrue( "cleared set reports not-empty", s.isEmpty() );
    }


    public void testContains() throws Exception {
        TIntSet s = new TIntHashSet();
        int i = 100;
        s.add( i );
        assertTrue( "contains failed", s.contains( i ) );
        assertFalse( "contains failed", s.contains( 1000 ) );
    }


    @SuppressWarnings({"ForLoopReplaceableByForEach"})
    public void testContainsAll() throws Exception {

        int[] ints = {1138, 42, 13, 86, 99};

        TIntSet set = new TIntHashSet();
        set.addAll( ints );

        TIntSet other = new TIntHashSet();
        other.addAll( ints );

        List<Number> ints_list = new ArrayList<Number>();
        for ( int element : ints ) {
            ints_list.add( element );
        }

        for ( int index = 0; index < ints.length; index++ ) {
            assertTrue( Integer.valueOf( ints[index] ).toString(),
                    set.contains( ints[index] ) );
        }

        assertTrue( "containsAll(Collection<?>) failed: " + set,
                set.containsAll( ints_list ) );
        ints_list.remove( Integer.valueOf( 42 ) );
        ints_list.add( Long.valueOf( 42 ) );
        assertFalse( "containsAll(Collection<?>) failed: " + set,
                set.containsAll( ints_list ) );

        assertTrue( "containsAll(TIntSet) failed (same set): " + set,
                set.containsAll( set ) );

        assertTrue( "containsAll(TIntSet) failed (other set): " + set,
                set.containsAll( other ) );

        assertTrue( "containsAll(int[]) failed: " + set,
                set.containsAll( ints ) );


        int[] failed = {42, 86, 99, 123456};

        TIntSet failed_set = new TIntHashSet();
        failed_set.addAll( failed );

        List<Integer> failed_list = new ArrayList<Integer>();
        for ( int element : failed ) {
            failed_list.add( element );
        }

        assertFalse( "containsAll(Collection<?>) failed (false positive): " + set,
                set.containsAll( failed_list ) );

        assertFalse( "containsAll(TIntSet) failed (false positive): " + set,
                set.containsAll( failed_set ) );

        assertFalse( "containsAll(int[]) failed (false positive): " + set,
                set.containsAll( failed ) );
    }


    public void testAddAll() throws Exception {

        int[] ints = {1138, 42, 13, 86, 99, 101};

        TIntSet set;

        List<Integer> list = new ArrayList<Integer>();
        for ( int element : ints ) {
            list.add( Integer.valueOf( element ) );
        }

        set = new TIntHashSet();
        assertTrue( "addAll(Collection<?>) failed: " + set, set.addAll( list ) );
        for ( int element : ints ) {
            assertTrue( "contains failed: ", set.contains( element ) );
        }

        set = new TIntHashSet();
        assertTrue( "addAll(int[]) failed: " + set, set.addAll( ints ) );
        for ( int element : ints ) {
            assertTrue( "contains failed: ", set.contains( element ) );
        }

        TIntSet test_set = new TIntHashSet();
        assertTrue( "addAll(TIntSet) failed: " + test_set, test_set.addAll( set ) );
        for ( int element : ints ) {
            assertTrue( "contains failed: ", set.contains( element ) );
        }


    }


    public void testRetainAll() throws Exception {

        int[] ints = {1138, 42, 13, 86, 99, 101};

        TIntSet set = new TIntHashSet();
        set.addAll( ints );

        TIntSet other = new TIntHashSet();
        other.addAll( ints );

        int[] to_retain = {13, 86, 99, 1138};

        TIntSet retain_set = new TIntHashSet();
        retain_set.addAll( to_retain );

        List<Integer> retain_list = new ArrayList<Integer>();
        for ( int element : to_retain ) {
            retain_list.add( element );
        }

        assertFalse( "retainAll(TIntSet) failed (same set): " + set,
                set.retainAll( set ) );
        // Contains all the original elements
        assertTrue( set.toString(), set.containsAll( ints ) );
        assertTrue( retain_set.toString(), retain_set.containsAll( to_retain ) );

        assertTrue( "retainAll(Collection<?>) failed: " + set,
                set.retainAll( retain_list ) );
        // Contains just the expected elements
        assertFalse( set.toString(), set.containsAll( ints ) );
        assertTrue( set.toString(), set.containsAll( to_retain ) );
        assertTrue( retain_set.toString(), retain_set.containsAll( to_retain ) );

        // reset the set.
        set = new TIntHashSet();
        set.addAll( ints );
        assertTrue( "retainAll(TIntSet) failed: " + set,
                set.retainAll( retain_set ) );
        // Contains just the expected elements
        assertFalse( set.toString(), set.containsAll( ints ) );
        assertTrue( set.toString(), set.containsAll( to_retain ) );
        assertTrue( retain_set.toString(), retain_set.containsAll( to_retain ) );

        // reset the set.
        set = new TIntHashSet();
        set.addAll( ints );
        assertTrue( "retainAll(int[]) failed: " + set,
                set.retainAll( to_retain ) );
        // Contains just the expected elements
        assertFalse( set.toString(), set.containsAll( ints ) );
        assertTrue( set.toString(), set.containsAll( to_retain ) );
        assertTrue( retain_set.toString(), retain_set.containsAll( to_retain ) );
    }


    public void testRemoveAll() throws Exception {

        int[] ints = {1138, 42, 13, 86, 99, 101};

        TIntSet set = new TIntHashSet();
        set.addAll( ints );

        TIntSet other = new TIntHashSet();
        other.addAll( ints );

        int[] to_remove = {13, 86, 99, 1138};

        TIntSet remove_set = new TIntHashSet();
        remove_set.addAll( to_remove );

        List<Integer> remove_list = new ArrayList<Integer>();
        for ( int element : to_remove ) {
            remove_list.add( element );
        }

        int[] remainder = {42, 101};

        try {
            assertFalse( "removeAll(TIntSet) failed (same set): " + set,
                    set.removeAll( set ) );
            fail( "should have thrown ConcurrentModificationException" );
        }
        catch ( ConcurrentModificationException cme ) {
            // expected exception thrown.
        }

        // reset the set.
        set = new TIntHashSet();
        set.addAll( ints );
        assertTrue( "removeAll(Collection<?>) failed: " + set,
                set.removeAll( remove_list ) );
        // Contains just the expected elements
        assertTrue( set.toString(), set.containsAll( remainder ) );
        assertFalse( set.toString(), set.containsAll( to_remove ) );
        assertTrue( remove_set.toString(), remove_set.containsAll( to_remove ) );

        // reset the set.
        set = new TIntHashSet();
        set.addAll( ints );
        assertTrue( "removeAll(TIntSet) failed: " + set,
                set.removeAll( remove_set ) );
        // Contains just the expected elements
        assertTrue( set.toString(), set.containsAll( remainder ) );
        assertFalse( set.toString(), set.containsAll( to_remove ) );
        assertTrue( remove_set.toString(), remove_set.containsAll( to_remove ) );

        // reset the set.
        set = new TIntHashSet();
        set.addAll( ints );
        assertTrue( "removeAll(int[]) failed: " + set,
                set.removeAll( to_remove ) );
        // Contains just the expected elements
        assertTrue( set.toString(), set.containsAll( remainder ) );
        assertFalse( set.toString(), set.containsAll( to_remove ) );
        assertTrue( remove_set.toString(), remove_set.containsAll( to_remove ) );
    }


    public void testAdd() throws Exception {
        TIntSet set = new TIntHashSet();
        assertTrue( "add failed", set.add( 1 ) );
        assertFalse( "duplicated add modified set", set.add( 1 ) );
    }


    public void testRemove() throws Exception {
        TIntSet set = new TIntHashSet();
        set.add( 1 );
        set.add( 2 );
        assertTrue( "One was not added", set.contains( 1 ) );
        assertTrue( "One was not removed", set.remove( 1 ) );
        assertFalse( "One was not removed", set.contains( 1 ) );
        assertTrue( "Two was also removed", set.contains( 2 ) );
    }


    public void testRemoveNonExistant() throws Exception {
        TIntSet set = new TIntHashSet();
        set.add( 1 );
        set.add( 2 );
        assertTrue( "One was not added", set.contains( 1 ) );
        assertTrue( "One was not removed", set.remove( 1 ) );
        assertFalse( "One was not removed", set.contains( 1 ) );
        assertTrue( "Two was also removed", set.contains( 2 ) );
        assertFalse( "Three was removed (non-existant)", set.remove( 3 ) );
    }


    public void testSize() throws Exception {
        TIntSet set = new TIntHashSet();
        assertEquals( "initial size was not 0", 0, set.size() );

        for ( int i = 0; i < 99; i++ ) {
            set.add( i );
            assertEquals( "size did not increase after add", i + 1, set.size() );
        }
    }


    public void testClear() throws Exception {
        TIntSet set = new TIntHashSet();
        set.addAll( new int[]{1, 2, 3} );
        assertEquals( "size was not 3", 3, set.size() );
        set.clear();
        assertEquals( "initial size was not 0", 0, set.size() );
    }


    public void testSerialize() throws Exception {
        int[] ints = {1138, 42, 86, 99, 101};

        TIntSet set = new TIntHashSet();
        set.addAll( ints );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( set );

        ByteArrayInputStream bias = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bias );

        TIntSet deserialized = (TIntSet) ois.readObject();

        assertEquals( set, deserialized );
    }


    public void testToArray() {
        TIntSet set = new TIntHashSet();
        int[] ints = {42, 1138, 13, 86, 99};
        set.addAll( ints );
        int[] res = set.toArray();
        Arrays.sort( ints );
        Arrays.sort( res );
        assertTrue( Arrays.equals( ints, res ) );
    }


    public void testToArrayMatchesIteratorOrder() {
        TIntSet set = new TIntHashSet();
        int[] ints = {42, 1138, 13, 86, 99};
        set.addAll( ints );
        int[] toarray_ints = set.toArray();

        int[] iter_ints = new int[5];
        TIntIterator iter = set.iterator();

        int index = 0;
        while ( iter.hasNext() ) {
            iter_ints[index++] = iter.next();
        }

        assertTrue( Arrays.equals( iter_ints, toarray_ints ) );
    }


    public void testToArrayWithParams() {
        int no_entry_value = Integer.MIN_VALUE;
        TIntSet set = new TIntHashSet( 10, 0.5f, no_entry_value );
        assertEquals( no_entry_value, set.getNoEntryValue() );

        int[] ints = {42, 1138, 13, 86, 99};
        set.addAll( ints );

        int[] sink = new int[ints.length + 2];
        sink[sink.length - 1] = -1;
        sink[sink.length - 2] = -2;

        int[] res = set.toArray( sink );
        assertEquals( set.getNoEntryValue(), res[set.size()] );

        Set<Integer> copy = new HashSet<Integer>();
        for ( int element : sink ) {
            copy.add( Integer.valueOf( element ) );
        }

        Set<Integer> bogey = new HashSet<Integer>();
        for ( int element : ints ) {
            bogey.add( Integer.valueOf( element ) );
        }
        bogey.add( -1 );
        bogey.add( no_entry_value );
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

        TIntSet set = new TIntHashSet();
        set.add( 1 );
        set.add( 2 );
        set.add( 3 );
        set.add( 4 );

        TIntIterator iter = set.iterator();
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

        TIntSet set = new TIntHashSet();
        set.add( 1 );
        set.add( 2 );
        set.add( 3 );
        set.add( 4 );

        TIntIterator iter = set.iterator();
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


    public void testForEach() {
        TIntSet set = new TIntHashSet( 10, 0.5f );
        int[] ints = {1138, 42, 86, 99, 101};
        set.addAll( ints );

        class ForEach implements TIntProcedure {

            TIntSet built = new TIntHashSet();


            public boolean execute( int value ) {
                built.add( value );
                return true;
            }


            TIntSet getBuilt() {
                return built;
            }
        }

        ForEach procedure = new ForEach();

        set.forEach( procedure );
        TIntSet built = procedure.getBuilt();

        assertEquals( "inequal sizes: " + set + ", " + built, set.size(), built.size() );
        assertTrue( "inequal sets: " + set + ", " + built, set.equals( built ) );
    }


    public void testEquals() {
        int[] ints = {1138, 42, 86, 99, 101};
        TIntSet set = new TIntHashSet();
        set.addAll( ints );
        TIntSet other = new TIntHashSet();
        other.addAll( ints );

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
    }


    public void testHashcode() {
        int[] ints = {1138, 42, 86, 99, 101};
        TIntSet set = new TIntHashSet();
        set.addAll( ints );
        TIntSet other = new TIntHashSet();
        other.addAll( ints );

        assertTrue( "hashcodes incorrectly not equal: " + set + ", " + other,
                set.hashCode() == other.hashCode() );

        int[] mismatched = {72, 49, 53, 1024, 999};
        TIntSet unequal = new TIntHashSet();
        unequal.addAll( mismatched );

        assertFalse( "hashcodes unlikely equal: " + set + ", " + unequal,
                set.hashCode() == unequal.hashCode() );
    }


}
