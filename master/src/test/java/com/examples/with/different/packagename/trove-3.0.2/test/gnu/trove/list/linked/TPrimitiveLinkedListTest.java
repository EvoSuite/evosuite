package gnu.trove.list.linked;

import gnu.trove.TIntCollection;
import gnu.trove.function.TIntFunction;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jparent
 * Date: 24/03/11
 * Time: 12:27
 * To change this template use File | Settings | File Templates.
 */
public class TPrimitiveLinkedListTest extends TestCase {

    private TIntList list;


    public void setUp() throws Exception {
        super.setUp();

        list = new TIntLinkedList();
        list.add( 1 );
        list.add( 2 );
        list.add( 3 );
        list.add( 4 );
        list.add(5);
    }


    public void tearDown() throws Exception {
        super.tearDown();
    }


    public void testGet() {

        assertEquals( 4, list.get( 3 ) );

        try {
            list.get( 10 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( IndexOutOfBoundsException ex ) {
            // Expected
        }

        int element_count = 10;
        TIntLinkedList array_list = new TIntLinkedList();
        for ( int i = 1; i <= element_count; i++ ) {
            array_list.add(i);
        }

        for ( int i = 0; i < array_list.size(); i++ ) {
            int expected = i + 1;
            assertEquals( expected, array_list.get( i ) );
        }

        try {
            array_list.get( 100 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( IndexOutOfBoundsException ex ) {
            // Expected
        }

    }


    public void testContains() {
        int element_count = 10;

        TIntList a = new TIntLinkedList();
        assertTrue( a.isEmpty() );

        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        for ( int i = 1; i <= element_count; i++ ) {
            assertTrue( "element " + i + " not found in " + a, a.contains( i ) );
        }

        assertFalse("list doesn't hold MAX_VALUE: " + a,
                a.contains(Integer.MAX_VALUE));
    }


    public void testInsert() {
        int element_count = 10;

        TIntList a = new TIntLinkedList();
        assertTrue( a.isEmpty() );

        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        int testval = 1138;
        a.insert( 5, testval );

        for ( int i = 0; i < 5; i++ ) {
            int result = a.get( i );
            int expected = i + 1;
            assertTrue( "element " + result + " should be " + expected,
                    result == expected );
        }

        assertEquals( testval, a.get( 5 ) );

        for ( int i = 6; i < a.size(); i++ ) {
            int result = a.get( i );
            assertTrue("element " + result + " should be " + i,
                    result == i);
        }
    }


    public void testInsertArray() {
        int element_count = 10;
        int[] ints = {1138, 42, 86, 99, 101};

        TIntList a = new TIntLinkedList();
        assertTrue( a.isEmpty() );

        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        a.insert( 0, ints );
        assertEquals(ints.length + element_count, a.size());

        for ( int i = 0; i < ints.length; i++ ) {
            assertEquals( ints[i], a.get( i ) );
        }
        for ( int i = ints.length, j = 1;
              i < ints.length + element_count;
              i++, j++ ) {
            assertEquals( j, a.get( i ) );
        }
    }


    public void testInsertAtEnd() {
        int element_count = 10;

        TIntList a = new TIntLinkedList();
        assertTrue( a.isEmpty() );

        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        a.insert( a.size(), 11 );

        for ( int i = 0; i < element_count; i++ ) {
            assertEquals( i + 1, a.get( i ) );
        }
        for ( int i = element_count;
              i < a.size();
              i++ ) {
            int expected = i + 1;
            assertEquals( expected, a.get( i ) );
        }
    }


    public void testInsertArrayAtEnd() {
        int element_count = 10;
        int[] ints = {1138, 42, 86, 99, 101};

        TIntList a = new TIntLinkedList();
        assertTrue( a.isEmpty() );

        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        a.insert(a.size(), ints);

        for ( int i = 0; i < element_count; i++ ) {
            assertEquals( i + 1, a.get( i ) );
        }
        for ( int i = element_count, j = 0;
              i < ints.length + element_count;
              i++, j++ ) {
            assertEquals( ints[j], a.get( i ) );
        }
    }


    public void testSetArray() {
        int element_count = 10;
        int[] ints = {1138, 42, 86, 99, 101};

        TIntList a = new TIntLinkedList();
        assertTrue( a.isEmpty() );

        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        a.set( a.size() - ints.length, ints );

        for ( int i = 0; i < element_count - ints.length; i++ ) {
            assertEquals(i + 1, a.get(i));
        }
        for ( int i = element_count - ints.length, j = 0;
              i < a.size();
              i++, j++ ) {
            assertEquals(ints[j], a.get(i));
        }

        try {
            a.set( a.size(), ints );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( IndexOutOfBoundsException ex ) {
            // Expected
        }
    }


    public void testSet() {
        int element_count = 10;

        TIntList a = new TIntLinkedList();
        assertTrue( a.isEmpty() );

        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        int testval = 1138;
        a.set( 5, testval );

        for ( int i = 0; i < 5; i++ ) {
            int result = a.get( i );
            int expected = i + 1;
            assertTrue( "element " + result + " should be " + expected,
                    result == expected );
        }

        assertEquals( testval, a.get( 5 ) );

        for ( int i = 6; i < a.size(); i++ ) {
            int result = a.get( i );
            int expected = i + 1;
            assertTrue( "element " + result + " should be " + expected,
                    result == expected );
        }

        try {
            a.set(100, 20);
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( IndexOutOfBoundsException ex ) {
            // Expected
        }
    }


    public void testReplace() {
        int element_count = 10;

        TIntList a = new TIntLinkedList();
        assertTrue( a.isEmpty() );

        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        int testval = 1138;
        assertEquals( 6, a.replace( 5, testval ) );

        for ( int i = 0; i < 5; i++ ) {
            int result = a.get( i );
            int expected = i + 1;
            assertTrue( "element " + result + " should be " + expected,
                    result == expected );
        }

        assertEquals( testval, a.get( 5 ) );

        for ( int i = 6; i < a.size(); i++ ) {
            int result = a.get( i );
            int expected = i + 1;
            assertTrue( "element " + result + " should be " + expected,
                    result == expected );
        }

        try {
            a.replace( 100, 20 );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( IndexOutOfBoundsException ex ) {
            // Expected
        }
    }


    public void testRemove() {
        int element_count = 10;

        TIntList a = new TIntLinkedList();
        assertTrue( a.isEmpty() );

        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        assertEquals( 6, a.get( 5 ) );
        assertTrue( a.remove( 5 ) );
        for ( int i = 0; i < 4; i++ ) {
            int expected = i + 1;
            assertTrue( "index " + i + " expected " + expected, a.get( i ) == expected );
        }
        for ( int i = 4; i < a.size(); i++ ) {
            int expected = i + 2;
            assertTrue( "index " + i + " expected " + expected, a.get( i ) == expected );
        }

        // Can't remove again from THIS list because it's not present.
        assertFalse( a.remove( 5 ) );

        assertEquals( 6, a.removeAt( 4 ) );
        for ( int i = 0; i < 4; i++ ) {
            int expected = i + 1;
            assertTrue( "index " + i + " expected " + expected, a.get( i ) == expected );
        }
        for ( int i = 4; i < a.size(); i++ ) {
            int expected = i + 3;
            assertTrue( "index " + i + " expected " + expected, a.get( i ) == expected );
        }
        a.insert( 4, 6 );

        // Add a value twice, can remove it twice
        assertTrue( a.add( 5 ) );
        assertTrue( a.add( 5 ) );

        assertTrue( a.remove( 5 ) );
        assertTrue( a.remove( 5 ) );
        assertFalse( a.remove( 5 ) );

        a.insert( 4, 5 );
        assertTrue( a.add( 5 ) );
        for ( int i = 0; i < 5; i++ ) {
            int expected = i + 1;
            assertTrue( "index " + i + " expected " + expected, a.get( i ) == expected );
        }
        for ( int i = 5; i < a.size() - 1; i++ ) {
            int expected = i + 1;
            assertTrue( "index " + i + " expected " + expected + ", list: " + a,
                    a.get( i ) == expected );
        }
        assertEquals( 5, a.get( a.size() - 1 ) );

        assertTrue( a.remove( 5 ) );
        assertEquals( element_count, a.size() );
        for ( int i = 0; i < 4; i++ ) {
            int expected = i + 1;
            assertTrue( "index " + i + " expected " + expected, a.get( i ) == expected );
        }
        for ( int i = 4; i < a.size() - 1; i++ ) {
            int expected = i + 2;
            assertTrue( "index " + i + " expected " + expected, a.get( i ) == expected );
        }
        assertEquals( 5, a.get( a.size() - 1 ) );


    }


    public void testRemoveMultiple() {
        int element_count = 20;

        TIntList a = new TIntLinkedList();
        assertTrue( a.isEmpty() );

        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        // Remove odd offsets, which are even numbers.
        for ( int i = element_count; i >= 0; i-- ) {
            if ( i % 2 == 1 ) {
                a.removeAt( i );
            }
        }

        for ( int i = 0; i < a.size(); i++ ) {
            int expected = i * 2 + 1;
            assertTrue( "index " + i + " expected " + expected, a.get( i ) == expected );
        }
    }


    public void testRemoveChunk() {
        int element_count = 20;

        TIntList a = new TIntLinkedList();
        assertTrue( a.isEmpty() );

        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        a.remove( 5, 10 );

        for ( int i = 0; i < 5; i++ ) {
            int expected = i + 1;
            assertTrue( "index " + i + " expected " + expected, a.get( i ) == expected );
        }
        for ( int i = 5; i < a.size(); i++ ) {
            int expected = i + 11;
            assertTrue( "index " + i + " expected " + expected +
                        " but got " + a.get( i ), a.get( i ) == expected );
        }

        try {
            a.remove( -1, 10 );
            fail( "Expected ArrayIndexOutOfBoundsException" );
        }
        catch ( ArrayIndexOutOfBoundsException ex ) {
            // Expected
        }

        try {
            a.remove( a.size(), 1 );
            fail( "Expected ArrayIndexOutOfBoundsException" );
        }
        catch ( ArrayIndexOutOfBoundsException ex ) {
            // Expected
        }
    }


    public void testContainsAllCollection() {
        int element_count = 20;
        SortedSet<Integer> set = new TreeSet<Integer>();
        for ( int i = 0; i < element_count; i++ ) {
            set.add( Integer.valueOf( i ) );
        }

        TIntLinkedList list = new TIntLinkedList( );
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        assertTrue( "list: " + list + " should contain all of set: " + set,
                list.containsAll( set ) );

        set.remove( element_count );
        assertTrue( "list: " + list + " should contain all of set: " + set,
                list.containsAll( set ) );

        list.remove( 5 );
        assertFalse( "list: " + list + " should not contain all of set: " + set,
                list.containsAll( set ) );
        list.add( 5 );

        // Test when not all objects are Integers..
        Set<Number> obj_set = new HashSet<Number>();
        for ( int i = 0; i < element_count; i++ ) {
            if ( i != 5 ) {
                obj_set.add( Integer.valueOf( i ) );
            } else {
                obj_set.add( Long.valueOf( ( long ) i ) );
            }
        }
        assertFalse( "list should not contain all of obj_set",
                list.containsAll( obj_set ) );
    }


    public void testContainsAllTCollection() {
        int element_count = 20;
        TIntLinkedList list = new TIntLinkedList( element_count * 2 );
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        TIntLinkedList other = new TIntLinkedList( list );
        assertEquals( element_count, list.size() );
        assertEquals( element_count, other.size() );
        assertEquals( list, other );

        assertTrue( "list should contain all of itself: " + list, list.containsAll( list ) );
        assertTrue( "list should contain all of equal list " + list + ", " + other,
                list.containsAll( other ) );
        for ( int i = 0; i < other.size(); i++ ) {
            if ( i % 2 == 0 ) {
                other.removeAt( i );
            }
        }
        assertTrue( "list: " + list + " should contain all of other: " + other,
                list.containsAll( other ) );

        other.add( 1138 );
        assertFalse( "list: " + list + " should not contain all of other: " + other,
                list.containsAll( other ) );

        TIntSet set = new TIntHashSet( list );
        assertTrue( "list: " + list + " should contain all of set: " + set,
                list.containsAll( set ) );

        set.add ( 1138 );
        assertFalse( "list: " + list + " should not contain all of set: " + set,
                list.containsAll( set ) );
    }


    public void testContainsAllArray() {
        int element_count = 20;
        int[] ints = new int[element_count];
        TIntLinkedList list = new TIntLinkedList( element_count * 2 );
        for ( int i = 0; i < element_count; i++ ) {
            ints[i] = i;
            list.add( i );
        }

        assertTrue( "list: " + list + " should contain all of array: " +
                    Arrays.toString(ints), list.containsAll( ints ) );

        ints[5] = 1138;
        assertFalse( "list: " + list + " should cnot ontain all of array: " +
                     Arrays.toString( ints ), list.containsAll( ints ) );
    }


    public void testAddAllCollection() {
        int element_count = 20;
        SortedSet<Integer> set = new TreeSet<Integer>();
        for ( int i = 0; i < element_count; i++ ) {
            set.add( Integer.valueOf( i ) );
        }

        TIntLinkedList list = new TIntLinkedList( 20 );
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        assertEquals( element_count, set.size() );
        assertEquals( element_count, list.size() );

        list.addAll( set );
        assertEquals( element_count * 2, list.size() );
        for ( int i = 0; i < list.size(); i++ ) {
            int expected;
            if ( i < element_count ) {
                expected = i;
            } else {
                expected = i - element_count;
            }
            assertEquals( expected , list.get( i ) );
        }
    }


    public void testAddAllTCollection() {
        int element_count = 20;
        TIntLinkedList source = new TIntLinkedList();
        for ( int i = 0; i < element_count; i++ ) {
            source.add( Integer.valueOf( i ) );
        }

        TIntLinkedList list = new TIntLinkedList( 20 );
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        assertEquals( element_count, source.size() );
        assertEquals( element_count, list.size() );

        list.addAll( source );
        assertEquals( element_count * 2, list.size() );
        for ( int i = 0; i < list.size(); i++ ) {
            int expected;
            if ( i < element_count ) {
                expected = i;
            } else {
                expected = i - element_count;
            }
            assertEquals( expected , list.get( i ) );
        }
    }


    public void testAddAllArray() {
        int element_count = 20;
        int[] ints = new int[element_count];
        TIntLinkedList list = new TIntLinkedList();
        for ( int i = 0; i < element_count; i++ ) {
            ints[i] = i;
            list.add( Integer.valueOf( i ) );
        }

        assertEquals( element_count, list.size() );

        assertTrue ( list.addAll( ints ) );
        assertEquals( element_count * 2, list.size() );
        for ( int i = 0; i < list.size(); i++ ) {
            int expected;
            if ( i < element_count ) {
                expected = i;
            } else {
                expected = i - element_count;
            }
            assertEquals( "expected: " + expected + ", got: " + list.get( i ) +
                    ", list: " + list + ", array: " + Arrays.toString( ints ),
                    expected , list.get( i ) );
        }
    }


    public void testRetainAllCollection() {
        int element_count = 20;
        SortedSet<Integer> set = new TreeSet<Integer>();
        for ( int i = 0; i < element_count; i++ ) {
            set.add( Integer.valueOf( i ) );
        }

        TIntList list = new TIntLinkedList( 20 );
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        assertEquals( element_count, set.size() );
        assertEquals( element_count, list.size() );

        assertFalse( list.retainAll( set ) );
        assertEquals( element_count, list.size() );
        for ( int i = 0; i < list.size(); i++ ) {
            assertEquals( i , list.get( i ) );
        }

        set.remove( 0 );
        set.remove( 5 );
        set.remove( 10 );
        set.remove( 15 );
        assertTrue( list.retainAll( set ) );
        int expected = element_count - 4;
        assertEquals( "expected: " + expected + ", was: " + list.size() + ", list: " + list ,
                expected, list.size() );
        for ( int i = 0; i < list.size(); i++ ) {
            expected = ( int ) Math.floor( i / 4 ) + i + 1;
            assertEquals( "expected: " + expected + ", was: " + list.get( i ) + ", list: " + list,
                expected , list.get( i ) );
        }
    }


    public void testRetainAllTCollection() {
        int element_count = 20;
        TIntList other = new TIntLinkedList();
        for ( int i = 0; i < element_count; i++ ) {
            other.add( i );
        }

        TIntList list = new TIntLinkedList( 20 );
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        assertEquals( element_count, list.size() );
        assertEquals( element_count, other.size() );

        assertFalse( list.retainAll( list ) );

        assertFalse( list.retainAll( other ) );
        assertEquals( element_count, list.size() );
        for ( int i = 0; i < list.size(); i++ ) {
            assertEquals( i , list.get( i ) );
        }

        other.remove( 0 );
        other.remove( 5 );
        other.remove( 10 );
        other.remove( 15 );
        assertTrue( list.retainAll( other ) );
        int expected = element_count - 4;
        assertEquals( expected, list.size() );
        for ( int i = 0; i < list.size(); i++ ) {
            expected = ( int ) Math.floor( i / 4 ) + i + 1;
            assertEquals( "expected: " + expected + ", was: " + list.get( i ) + ", list: " + list,
                expected , list.get( i ) );
        }
    }


    public void testRetainAllArray() {
        int element_count = 20;
        int ints[] = new int[element_count];
        for ( int i = 0; i < element_count; i++ ) {
            ints[i] = i;
        }

        TIntList list = new TIntLinkedList( 20 );
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        assertEquals( element_count, list.size() );

        assertFalse( list.retainAll( ints ) );
        assertEquals( element_count, list.size() );
        for ( int i = 0; i < list.size(); i++ ) {
            assertEquals( i , list.get( i ) );
        }

        ints = new int[( element_count - 4 )] ;
        for ( int i = 0, j = 0; i < ints.length; i++, j++ ) {
            if ( i % 4 == 0 ) {
                j++;
            }
            ints[i] = j;
        }
        assertTrue( list.retainAll( ints ) );
        int expected = element_count - 4;
        assertEquals( expected, list.size() );
        for ( int i = 0; i < list.size(); i++ ) {
            expected = ( int ) Math.floor( i / 4 ) + i + 1;
            assertEquals( "expected: " + expected + ", was: " + list.get( i ) + ", list: " + list,
                expected , list.get( i ) );
        }
    }


    public void testRemoveAllCollection() {
        int element_count = 20;
        SortedSet<Integer> set = new TreeSet<Integer>();
        for ( int i = 0; i < element_count; i++ ) {
            set.add( Integer.valueOf( i ) );
        }

        TIntList list = new TIntLinkedList( 20 );
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        assertEquals( element_count, set.size() );
        assertEquals( element_count, list.size() );

        assertTrue( list.removeAll( set ) );
        assertTrue( list.isEmpty() );
        assertEquals( 0, list.size() );
        assertEquals( element_count, set.size() );
        for ( int i = 0; i < set.size(); i++ ) {
            assertTrue( set.contains( Integer.valueOf( i ) ) );
        }


        // Reset the list
        list = new TIntLinkedList( 20 );
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        set.remove( 0 );
        set.remove( 5 );
        set.remove( 10 );
        set.remove( 15 );
        assertTrue( list.removeAll( set ) );
        int expected = 4;
        assertEquals( "expected: " + expected + ", was: " + list.size() + ", list: " + list ,
                expected, list.size() );
        for ( int i = 0; i < list.size(); i++ ) {
            expected = i * 5;
            assertEquals( "expected: " + expected + ", was: " + list.get( i ) + ", list: " + list,
                expected , list.get( i ) );
        }
    }


    public void testRemoveAllTCollection() {
        int element_count = 20;
        TIntList other = new TIntLinkedList();
        for ( int i = 0; i < element_count; i++ ) {
            other.add( i );
        }

        TIntList list = new TIntLinkedList( 20 );
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        assertEquals( element_count, list.size() );
        assertEquals( element_count, other.size() );

        assertTrue( list.removeAll( list ) );
        assertEquals( 0, list.size() );
        assertTrue( list.isEmpty() );

        // Reset the list
        list = new TIntLinkedList( 20 );
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        assertTrue( list.removeAll( other ) );
        assertEquals( 0, list.size() );
        assertTrue( list.isEmpty() );
        for ( int i = 0; i < list.size(); i++ ) {
            assertEquals( i , list.get( i ) );
        }

        // Reset the list
        list = new TIntLinkedList( 20 );
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        other.remove( 0 );
        other.remove( 5 );
        other.remove( 10 );
        other.remove( 15 );
        assertTrue( list.removeAll( other ) );
        int expected = 4;
        assertEquals( "expected: " + expected + ", was: " + list.size() + ", list: " + list ,
                expected, list.size() );
        for ( int i = 0; i < list.size(); i++ ) {
            expected = i * 5;
            assertEquals( "expected: " + expected + ", was: " + list.get( i ) + ", list: " + list,
                expected , list.get( i ) );
        }
    }


    public void testRemoveAllArray() {
        int element_count = 20;
        int ints[] = new int[element_count];
        for ( int i = 0; i < element_count; i++ ) {
            ints[i] = i;
        }

        TIntList list = new TIntLinkedList( 20 );
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        assertEquals( element_count, list.size() );

        assertTrue( list.removeAll( ints ) );
        assertEquals( 0, list.size() );
        assertTrue( list.isEmpty() );

        // Reset the list
        list = new TIntLinkedList( 20 );
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        ints = new int[( element_count - 4 )] ;
        for ( int i = 0, j = 0; i < ints.length; i++, j++ ) {
            if ( i % 4 == 0 ) {
                j++;
            }
            ints[i] = j;
        }

        assertTrue( list.removeAll( ints ) );
        int expected = 4;
        assertEquals( expected, list.size() );
        for ( int i = 0; i < list.size(); i++ ) {
            expected = i * 5;
            assertEquals( "expected: " + expected + ", was: " + list.get( i ) + ", list: " + list,
                expected , list.get( i ) );
        }
    }


    public void testShuffle() {
        int element_count = 20;
        TIntList list = new TIntLinkedList( 20 );
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }
        TIntList copy = new TIntLinkedList(list);

        list.shuffle( new Random( System.currentTimeMillis() ) );
        boolean differ = false;
        for ( int i = 0; i < list.size(); i++ ) {
            assertTrue( list.contains( i ) );
            if (copy.get(i) != list.get(i))
                differ = true;
        }

        // lost nothing
        Set<Integer> s = new HashSet<Integer>();
        for (TIntIterator iterator = list.iterator(); iterator.hasNext();) {
            int next = iterator.next();
            s.add(next);
        }
        assertTrue(s.size() == list.size());
        assertTrue(copy.size() == list.size());
        assertTrue("list not different " + copy + " shuffled= " + list, differ);
    }


    public void testIterator() {
        int element_count = 20;
        TIntList list = new TIntLinkedList();
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        TIntIterator iter = list.iterator();
        assertTrue( "iter should have next: " + list.size(), iter.hasNext() );

        int j = 0;
        while ( iter.hasNext() ) {
            int next = iter.next();
            assertEquals( j, next );
            j++;
        }
        assertFalse( iter.hasNext() );

        iter = list.iterator();
        for ( int i = 0; i < element_count / 2; i++ ) {
            iter.next();
        }
        iter.remove();
        try {
            // trying to remove it again should fail.
            iter.remove();
            fail( "Expected IllegalStateException" );
        }
        catch ( IllegalStateException ex ) {
            // Expected
        }
        for ( int i = 0; i < element_count / 2 - 1; i++ ) {
            iter.next();
        }
    }


    public void testIteratorAbuseNext() {
        int element_count = 20;
        TIntList list = new TIntLinkedList();
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        TIntIterator iter = list.iterator();
        while ( iter.hasNext() ) {
            iter.next();
        }
        assertFalse( iter.hasNext() );

        list.remove( 5, 10 );
        assertFalse( iter.hasNext() );
        try {
            iter.next();
            fail( "Expected NoSuchElementException" );
        } catch( NoSuchElementException ex ) {
            // Expected.
        }
    }


/*
Disable since not clear how iterator implementation can detect concurrent modification and throw CME
    public void testIteratorAbuseRemove() {
        int element_count = 20;
        TIntList list = new TIntLinkedList();
        for ( int i = 0; i < element_count; i++ ) {
            list.add( i );
        }

        TIntIterator iter = list.iterator();
        while ( iter.hasNext() ) {
            iter.next();
        }
        assertFalse( iter.hasNext() );

        list.remove( 5, 10 );
        assertFalse( iter.hasNext() );

        try {
            iter.remove();
            fail( "Expected ConcurrentModificationException" );
        } catch( ConcurrentModificationException ex ) {
            // Expected.
        }
    }
*/


/*    public void testEnsureCapacity() {
        int size = 1000;
        TIntLinkedList array_list = new TIntLinkedList();
        int initial_length = array_list._data.length;
        assertEquals( Constants.DEFAULT_CAPACITY, initial_length );

        array_list.ensureCapacity( size );
        int max_length = array_list._data.length;
        assertTrue( "not large enough: " + max_length + " should be >= " + size,
                max_length >= size );
    }


    public void testTrimToSize() {
        int initial_size = 1000;
        int element_count = 100;

        TIntLinkedList array_list = new TIntLinkedList( initial_size );
        int initial_length = array_list._data.length;
        assertEquals( initial_size, initial_length );
        assertTrue( array_list.isEmpty() );

        for ( int i = 1; i <= element_count; i++ ) {
            array_list.add( i );
        }
        array_list.trimToSize();

        int trimmed_length = array_list._data.length;
        assertTrue( "not trimmed: " + trimmed_length + " should be == " + element_count,
                trimmed_length == element_count );
    }*/


    public void testToArray() {
        assertTrue( Arrays.equals( new int[]{1, 2, 3, 4, 5}, list.toArray() ) );
        assertTrue( Arrays.equals( new int[]{1, 2, 3, 4}, list.toArray( 0, 4 ) ) );
        assertTrue( Arrays.equals( new int[]{2, 3, 4, 5}, list.toArray( 1, 4 ) ) );
        assertTrue( Arrays.equals( new int[]{2, 3, 4}, list.toArray( 1, 3 ) ) );

        try {
            list.toArray( -1, 5 );
            fail( "Expected ArrayIndexOutOfBoundsException when begin < 0" );
        }
        catch ( ArrayIndexOutOfBoundsException expected ) {
            // Expected
        }
    }


    public void testToArrayWithDest() {
        int[] dest = new int[5];
        assertTrue( Arrays.equals( new int[]{1, 2, 3, 4, 5}, list.toArray( dest ) ) );
        dest = new int[4];
        assertTrue( Arrays.equals( new int[]{1, 2, 3, 4}, list.toArray( dest, 0, 4 ) ) );
        dest = new int[4];
        assertTrue( Arrays.equals( new int[]{2, 3, 4, 5}, list.toArray( dest, 1, 4 ) ) );
        dest = new int[3];
        assertTrue( Arrays.equals( new int[]{2, 3, 4}, list.toArray( dest, 1, 3 ) ) );

        try {
            list.toArray( dest, -1, 5 );
            fail( "Expected ArrayIndexOutOfBoundsException when begin < 0" );
        }
        catch ( ArrayIndexOutOfBoundsException expected ) {
            // Expected
        }
    }


    public void testToArrayWithDestTarget() {
        int[] dest = new int[5];
        assertTrue( Arrays.equals( new int[]{1, 2, 3, 4, 5}, list.toArray( dest ) ) );
        dest = new int[4];
        assertTrue( Arrays.equals( new int[]{1, 2, 3, 4}, list.toArray( dest, 0, 0, 4 ) ) );
        dest = new int[5];
        assertTrue( Arrays.equals( new int[]{0, 2, 3, 4, 5}, list.toArray( dest, 1, 1, 4 ) ) );
        dest = new int[4];
        assertTrue( Arrays.equals( new int[]{0, 2, 3, 4}, list.toArray( dest, 1, 1, 3 ) ) );

        dest = new int[5];
        assertTrue( Arrays.equals( new int[]{0, 0, 0, 0, 0}, list.toArray( dest, 0, 0, 0 ) ) );

        try {
            list.toArray( dest, -1, 0, 5 );
            fail( "Expected ArrayIndexOutOfBoundsException when begin < 0" );
        }
        catch ( ArrayIndexOutOfBoundsException expected ) {
            // Expected
        }
    }


    public void testSubList() throws Exception {
        TIntList subList = list.subList( 1, 4 );
        assertEquals( 3, subList.size() );
        assertEquals( 2, subList.get( 0 ) );
        assertEquals( 4, subList.get( 2 ) );
    }


    public void testSublist_Exceptions() {
        try {
            list.subList( 1, 0 );
            fail( "expected IllegalArgumentException when end < begin" );
        }
        catch ( IllegalArgumentException expected ) {
        }

        try {
            list.subList( -1, 3 );
            fail( "expected IndexOutOfBoundsException when begin < 0" );
        }
        catch ( IndexOutOfBoundsException expected ) {
        }

        try {
            list.subList( 1, 42 );
            fail( "expected IndexOutOfBoundsException when end > length" );
        }
        catch ( IndexOutOfBoundsException expected ) {
        }
    }


    public void testIndexOf() {
        int element_count = 20;
        TIntList a = new TIntLinkedList();
        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        int index = a.indexOf( 10 );
        assertEquals( 9, index );

        // Add more elements, but duplicates
        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }
        index = a.indexOf( 10 );
        assertEquals( 9, index );

        index = a.indexOf( 5 );
        assertEquals( 4, index );

        index = a.lastIndexOf( 5 );
        assertEquals( 24, index );

        // Non-existant entry
        index = a.indexOf( 100 );
        assertEquals( -1, index );
    }


    public void testBinarySearch() {
        int element_count = 20;
        TIntList a = new TIntLinkedList();
        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        a.sort();

        int index = a.binarySearch( 5 );
        assertEquals( 4, index );

        index = a.binarySearch( 8 );
        assertEquals( 7, index );

        // Add more elements, but duplicates
        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        a.sort();

        index = a.indexOf( 5 );
        assertTrue( "index: " + index, index >= 8 && index <= 9 );

        // Not in this range.
        index = a.binarySearch( 5, 15, 30 );
        assertTrue( "index: " + index, index < 0 );

        try {
            a.binarySearch( 5, 10, 55 );
            fail( "Expected ArrayIndexOutOfBoundsException" );
        }
        catch ( IndexOutOfBoundsException ex ) {
            // Expected
        }

        try {
            a.binarySearch( 5, -1, 15 );
            fail( "Expected ArrayIndexOutOfBoundsException" );
        }
        catch ( IndexOutOfBoundsException ex ) {
            // Expected
        }
    }


    public void testFill() {
        int element_count = 20;
        TIntList a = new TIntLinkedList();
        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        a.fill( 0xdeadbeef );
        for ( int i = 0; i < element_count; i++ ) {
            assertEquals( 0xdeadbeef, a.get( i ) );
        }
    }


    public void testFillOffsets() {
        int element_count = 20;
        TIntList a = new TIntLinkedList();
        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        a.fill( 10, 15, 0xdeadbeef );
        for ( int i = 0; i < 10; i++ ) {
            assertEquals( i + 1, a.get( i ) );
        }
        for ( int i = 10; i < 15; i++ ) {
            assertEquals( 0xdeadbeef, a.get( i ) );
        }
        for ( int i = 15; i < a.size(); i++ ) {
            assertEquals( i + 1, a.get( i ) );
        }

        a.fill( 15, 25, 0xcafebabe );
        assertEquals( 25, a.size() );
        for ( int i = 0; i < 10; i++ ) {
            assertEquals( i + 1, a.get( i ) );
        }
        for ( int i = 10; i < 15; i++ ) {
            assertEquals( 0xdeadbeef, a.get( i ) );
        }
        for ( int i = 15; i < a.size(); i++ ) {
            assertEquals( 0xcafebabe, a.get( i ) );
        }
    }


    public void testGrep() {
        int element_count = 20;
        TIntList a = new TIntLinkedList();
        for ( int i = 1; i < element_count; i++ ) {
            a.add( i );
        }

        TIntList grepped = a.grep( new TIntProcedure() {
            public boolean execute( int value ) {
                return value > 10;
            }
        } );

        for ( int i = 0; i < grepped.size(); i++ ) {
            int expected = i + 11;
            assertEquals( expected, grepped.get( i ) );
        }
    }


    public void testInverseGrep() {
        int element_count = 20;
        TIntList a = new TIntLinkedList();
        for ( int i = 1; i < element_count; i++ ) {
            a.add( i );
        }

        TIntList grepped = a.inverseGrep( new TIntProcedure() {
            public boolean execute( int value ) {
                return value <= 10;
            }
        } );

        for ( int i = 0; i < grepped.size(); i++ ) {
            int expected = i + 11;
            assertEquals( expected, grepped.get( i ) );
        }
    }


    public void testMax() {
        assertEquals( 5, list.max() );
        assertEquals( 1, list.min() );

        TIntList list2 = new TIntLinkedList();
        assertTrue( list2.isEmpty() );
        list2.add( 3 );
        list2.add( 1 );
        list2.add( 2 );
        list2.add( 5 );
        list2.add( 4 );
        assertEquals( 5, list2.max() );
        assertEquals( 1, list2.min() );

        try {
            TIntList list3 = new TIntLinkedList();
            list3.min();
            fail( "Expected IllegalStateException" );
        }
        catch ( IllegalStateException ex ) {
            // Expected
        }

        try {
            TIntList list3 = new TIntLinkedList();
            list3.max();
            fail( "Expected IllegalStateException" );
        }
        catch ( IllegalStateException ex ) {
            // Expected
        }
    }


    public void testForEach() {
        int element_count = 20;
        TIntList a = new TIntLinkedList();
        for ( int i = 1; i < element_count; i++ ) {
            a.add( i );
        }

        class ForEach implements TIntProcedure {
            TIntList built = new TIntLinkedList();


            public boolean execute( int value ) {
                built.add( value );
                return true;
            }

            TIntList getBuilt() {
                return built;
            }
        }

        ForEach foreach = new ForEach();
        a.forEach( foreach );
        TIntList built = foreach.getBuilt();
        assertEquals( a, built );
    }


    public void testForEachFalse() {
        int element_count = 20;
        TIntList a = new TIntLinkedList();
        for ( int i = 1; i < element_count; i++ ) {
            a.add( i );
        }

        class ForEach implements TIntProcedure {
            TIntList built = new TIntLinkedList();


            public boolean execute( int value ) {
                built.add( value );
                return false;
            }

            TIntList getBuilt() {
                return built;
            }
        }

        ForEach foreach = new ForEach();
        a.forEach( foreach );
        TIntList built = foreach.getBuilt();
        assertEquals( 1, built.size() );
        assertEquals( 1, built.get( 0 ) );
    }


    public void testForEachDescending() {
        int element_count = 20;
        TIntList a = new TIntLinkedList();
        for ( int i = 1; i < element_count; i++ ) {
            a.add( i );
        }

        class ForEach implements TIntProcedure {
            TIntList built = new TIntLinkedList();


            public boolean execute( int value ) {
                built.add( value );
                return true;
            }

            TIntList getBuilt() {
                return built;
            }
        }

        ForEach foreach = new ForEach();
        a.forEachDescending( foreach );
        TIntList built = foreach.getBuilt();
        built.reverse();
        assertEquals( a, built );
    }


    public void testForEachDescendingFalse() {
        int element_count = 20;
        TIntList a = new TIntLinkedList();
        for ( int i = 1; i < element_count; i++ ) {
            a.add( i );
        }

        class ForEach implements TIntProcedure {
            TIntList built = new TIntLinkedList();


            public boolean execute( int value ) {
                built.add( value );
                return false;
            }

            TIntList getBuilt() {
                return built;
            }
        }

        ForEach foreach = new ForEach();
        a.forEachDescending( foreach );
        TIntList built = foreach.getBuilt();
        built.reverse();
        assertEquals( 1, built.size() );
        assertEquals( 19, built.get( 0 ) );
    }


    public void testTransform() {
        int element_count = 20;
        TIntList a = new TIntLinkedList();
        for ( int i = 1; i < element_count; i++ ) {
            a.add( i );
        }

        a.transformValues( new TIntFunction() {
            public int execute( int value ) {
                return value * value;
            }
        } );

        for ( int i = 0; i < a.size(); i++ ) {
            int result = a.get( i );
            int expected = ( i + 1 ) * ( i + 1 );
            assertEquals( expected, result );
        }
    }


    public void testReverse() {
        int element_count = 20;
        TIntList a = new TIntLinkedList();
        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        a.reverse();
        a.reverse();
        a.reverse();

        for ( int i = 0, j = a.size(); i < a.size(); i++, j-- ) {
            assertEquals( j, a.get( i ) );
        }
    }


    public void testReversePartial() {
        int element_count = 20;
        TIntList a = new TIntLinkedList();
        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        a.reverse( 1, 19 );

        int[] expected = {1, 19, 18, 17, 16, 15, 14, 13, 12, 11,
                          10, 9, 8, 7, 6, 5, 4, 3, 2, 20};
        for ( int i = 0; i < a.size(); i++ ) {
            assertEquals( expected[i], a.get( i ) );
        }

        try {
            a.reverse( 20, 10 );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( IllegalArgumentException ex ) {
            // Expected
        }
    }


    public void testSortPartial() {
        int element_count = 20;
        TIntList a = new TIntLinkedList();
        for ( int i = 1; i <= element_count; i++ ) {
            a.add( i );
        }

        a.reverse();
        a.sort( 5, 15 );

        int[] expected = {20, 19, 18, 17, 16, 6, 7, 8, 9, 10,
                          11, 12, 13, 14, 15, 5, 4, 3, 2, 1};
        for ( int i = 0; i < a.size(); i++ ) {
            assertEquals( expected[i], a.get( i ) );
        }

        try {
            a.sort( 20, 10 );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( IllegalArgumentException ex ) {
            // Expected
        }
    }


    public void testEquals() {
        int element_count = 20;
        TIntList list = new TIntLinkedList();
        for ( int i = 1; i <= element_count; i++ ) {
            list.add( i );
        }

        assertEquals( list, list );
        assertEquals( list, new TIntLinkedList( list ) );


        TIntCollection collection = new TIntHashSet();
        for ( int i = 1; i <= element_count; i++ ) {
            collection.add( i );
        }

        assertFalse( list.equals( collection ) );

        collection.add( 1138 );
        assertFalse( list.equals( collection ) );

        TIntList other = new TIntLinkedList( list );
        other.replace( 10, 1138 );
        assertFalse( list.equals( other ) );
    }


    public void testSerialization() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream( bout );
        oout.writeObject( list );
        oout.close();

        ObjectInputStream oin = new ObjectInputStream(
                new ByteArrayInputStream( bout.toByteArray() ) );

        TIntLinkedList new_list = (TIntLinkedList) oin.readObject();

        assertEquals( list, new_list );
    }
}
