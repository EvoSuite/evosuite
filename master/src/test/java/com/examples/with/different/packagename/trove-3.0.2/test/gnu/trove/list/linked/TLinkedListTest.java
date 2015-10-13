///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001, Eric D. Friedman All Rights Reserved.
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

package gnu.trove.list.linked;

import gnu.trove.list.TIntList;
import gnu.trove.list.TLinkable;
import gnu.trove.procedure.TObjectProcedure;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;


/**
 * Created: Sat Nov 10 15:57:07 2001
 *
 * @author Eric D. Friedman
 * @author Rob Eden
 * @author Jeff Randall
 * @version $Id: TLinkedListTest.java,v 1.1.2.4 2010/09/27 17:23:07 robeden Exp $
 */

@SuppressWarnings({"ForLoopReplaceableByForEach", "ManualArrayToCollectionCopy"})
public class TLinkedListTest extends TestCase {

    protected TLinkedList<Data> list;


    public TLinkedListTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        list = new TLinkedList<Data>();
    }


    public void tearDown() throws Exception {
        list = null;
    }


    public void testAdd() throws Exception {
        Data[] data = {new Data( 1 ), new Data( 2 ), new Data( 3 )};
        for ( int i = 0; i < data.length; i++ ) {
            list.add( data[i] );
        }
        assertEquals( 3, list.size() );
    }


    public void testAddAtIndex() throws Exception {
        Data[] data = {new Data( 1 ), new Data( 2 ), new Data( 3 ),
                       new Data( 4 ), new Data( 5 ), new Data( 6 ),
                       new Data( 7 ), new Data( 8 ), new Data( 9 )};
        for ( int i = 0; i < data.length; i++ ) {
            list.add( data[i] );
        }
        assertEquals( data.length, list.size() );
        for ( int i = 0; i < data.length; i++ ) {
            assertEquals( data[i], list.get( i ) );
        }

        Data to_insert = new Data( 1138 );
        list.add( 1, to_insert );

        assertEquals( to_insert, list.get( 1 ) );
        assertEquals( data[0], list.get( 0 ) );
        for ( int i = 2; i < data.length; i++ ) {
            assertEquals( data[i - 1], list.get( i ) );
        }
    }


    public void testAddIllegalArgs() {
        Data[] data = {new Data( 1 ), new Data( 2 ), new Data( 3 ),
                       new Data( 4 ), new Data( 5 ), new Data( 6 ),
                       new Data( 7 ), new Data( 8 ), new Data( 9 )};
        for ( int i = 0; i < data.length; i++ ) {
            list.add( data[i] );
        }
        assertEquals( data.length, list.size() );
        for ( int i = 0; i < data.length; i++ ) {
            assertEquals( data[i], list.get( i ) );
        }

        try {
            list.add( -1, new Data( 1138 ) );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( IndexOutOfBoundsException ex ) {
            // Expected
        }

        try {
            list.add( list.size() + 1, new Data( 1138 ) );
            fail( "Expected IndexOutOfBoundsException" );
        }
        catch ( IndexOutOfBoundsException ex ) {
            // Expected
        }
    }


    public void testClear() {
        Data[] data = {new Data( 1 ), new Data( 2 ), new Data( 3 ),
                       new Data( 4 ), new Data( 5 ), new Data( 6 ),
                       new Data( 7 ), new Data( 8 ), new Data( 9 )};
        for ( int i = 0; i < data.length; i++ ) {
            list.add( data[i] );
        }
        assertEquals( data.length, list.size() );
        for ( int i = 0; i < data.length; i++ ) {
            assertEquals( data[i], list.get( i ) );
        }

        list.clear();
        assertTrue( "list should be empty", list.isEmpty() );
    }


    public void testInsert() throws Exception {
        Data[] data = {new Data( 2 ), new Data( 4 ), new Data( 6 )};
        for ( int i = 0; i < data.length; i++ ) {
            list.add( data[i] );
        }

        list.insert( 0, new Data( 1 ) );
        list.insert( 2, new Data( 3 ) );
        list.insert( 4, new Data( 5 ) );
        list.insert( list.size(), new Data( 7 ) );

        assertEquals( 7, list.size() );
        for ( int i = 0; i < list.size(); i++ ) {
            assertEquals( i + 1, list.get( i )._val );
        }
    }


    public void testToArray() {
        Data[] data = {new Data( 1 ), new Data( 2 ), new Data( 3 ),
                       new Data( 4 ), new Data( 5 ), new Data( 6 ),
                       new Data( 7 ), new Data( 8 ), new Data( 9 )};
        for ( int i = 0; i < data.length; i++ ) {
            list.add( data[i] );
        }
        assertEquals( data.length, list.size() );
        for ( int i = 0; i < data.length; i++ ) {
            assertEquals( data[i], list.get( i ) );
        }

        Data[] data_array = list.toArray( new Data[0] );
        assertEquals( list.size(), data_array.length );
        for ( int i = 0; i < list.size(); i++ ) {
            assertEquals( data_array[i], list.get( i ) );
        }

        Object[] obj_array = list.toArray();
        assertEquals( list.size(), obj_array.length );
        for ( int i = 0; i < list.size(); i++ ) {
            assertEquals( obj_array[i], list.get( i ) );
        }
    }


    public void testToUnlinkedArray() {
        Data[] data = {new Data( 1 ), new Data( 2 ), new Data( 3 ),
                       new Data( 4 ), new Data( 5 ), new Data( 6 ),
                       new Data( 7 ), new Data( 8 ), new Data( 9 )};
        for ( int i = 0; i < data.length; i++ ) {
            list.add( data[i] );
        }
        assertEquals( data.length, list.size() );
        for ( int i = 0; i < data.length; i++ ) {
            assertEquals( data[i], list.get( i ) );
        }

        Object[] data_array = list.toUnlinkedArray();
        assertEquals( data.length, data_array.length );
        for ( int i = 0; i < data.length; i++ ) {
            assertEquals( data[i], data_array[i] );
        }
    }


    public void testToUnlinkedArrayTyped() {
        Data[] data = {new Data( 1 ), new Data( 2 ), new Data( 3 ),
                       new Data( 4 ), new Data( 5 ), new Data( 6 ),
                       new Data( 7 ), new Data( 8 ), new Data( 9 )};
        for ( int i = 0; i < data.length; i++ ) {
            list.add( data[i] );
        }
        assertEquals( data.length, list.size() );
        for ( int i = 0; i < data.length; i++ ) {
            assertEquals( data[i], list.get( i ) );
        }

        Data[] data_array = list.toUnlinkedArray( new Data[0] );
        assertEquals( data.length, data_array.length );
        for ( int i = 0; i < data.length; i++ ) {
            assertEquals( data[i], data_array[i] );
        }
    }


    public void testContains() {
        Data[] data = {new Data( 1 ), new Data( 2 ), new Data( 3 ),
                       new Data( 4 ), new Data( 5 ), new Data( 6 ),
                       new Data( 7 ), new Data( 8 ), new Data( 9 )};
        for ( int i = 0; i < data.length; i++ ) {
            list.add( data[i] );
        }
        assertEquals( data.length, list.size() );
        for ( int i = 0; i < data.length; i++ ) {
            assertEquals( data[i], list.get( i ) );
        }

        Data item = new Data( 5 );
        assertTrue( "list contains item: " + item + ", " + list,
                list.contains( item ) );

        item = new Data( 1138 );
        assertFalse( "list doesn't contain item: "+ item + ", " + list,
                list.contains( item ) );
    }


    public void testNextIterator() throws Exception {
        Data[] data = new Data[100];
        for ( int i = 0; i < data.length; i++ ) {
            data[i] = new Data( i );
            list.add( data[i] );
        }

        int count = 0;
        for ( Iterator i = list.iterator(); i.hasNext(); ) {
            assertEquals( data[count++], i.next() );
        }
        assertEquals( data.length, count );

        count = 4;
        for ( Iterator i = list.listIterator( 4 ); i.hasNext(); ) {
            assertEquals( data[count++], i.next() );
        }
        assertEquals( data.length, count );
    }


    public void testPreviousIterator() throws Exception {
        Data[] data = new Data[100];
        for ( int i = 0; i < data.length; i++ ) {
            data[i] = new Data( i );
            list.add( data[i] );
        }

        int count = 100;
        for ( ListIterator i = list.listIterator( list.size() );
              i.hasPrevious(); ) {
            assertEquals( data[--count], i.previous() );
        }
        assertEquals( 0, count );

        count = 5;
        for ( ListIterator i = list.listIterator( count ); i.hasPrevious(); ) {
            assertEquals( data[--count], i.previous() );
        }
        assertEquals( 0, count );
    }


    public void testIteratorSet() throws Exception {
        Data[] data = new Data[100];
        for ( int i = 0; i < data.length; i++ ) {
            data[i] = new Data( i );
            list.add( data[i] );
        }

        ListIterator<Data> i;

        i = list.listIterator( 5 );
        i.next();
        Data d = new Data( 999 );
        i.set( d );
        assertEquals( d, list.get( 5 ) );
    }


    public void testRemoveOnlyElementInList() throws Exception {
        Data d = new Data( 0 );
        list.add( d );
        ListIterator i = list.listIterator();
        assertTrue( i.hasNext() );
        assertEquals( d, i.next() );
        i.remove();
        assertTrue( !i.hasNext() );
        assertTrue( !i.hasPrevious() );
        assertEquals( 0, list.size() );
    }


    public void testRemovePrevious() throws Exception {
        Data[] d = {new Data( 0 ), new Data( 1 ), new Data( 2 )};
        list.addAll( Arrays.asList( d ) );

        ListIterator i = list.listIterator( list.size() );
        i.previous();
        i.previous();
        i.remove();
        assertEquals( 2, list.size() );
        assertTrue( i.hasPrevious() );
        assertEquals( d[0], i.previous() );
        assertTrue( !i.hasPrevious() );
        assertTrue( i.hasNext() );
        assertEquals( d[0], i.next() );
        assertTrue( i.hasNext() );
        assertEquals( d[2], i.next() );
        assertTrue( !i.hasNext() );
        assertTrue( i.hasPrevious() );
        assertEquals( 2, list.size() );
    }


    public void testRemoveLast() throws Exception {
        Data[] d = {new Data( 0 ), new Data( 1 ), new Data( 2 )};
        list.addAll( Arrays.asList( d ) );

        ListIterator i = list.listIterator( list.size() );
        i.previous();
        i.remove();
        assertEquals( 2, list.size() );
        assertTrue( i.hasPrevious() );
        assertTrue( !i.hasNext() );
    }


    public void testRemoveFirst() throws Exception {
        Data[] d = {new Data( 0 ), new Data( 1 ), new Data( 2 )};
        list.addAll( Arrays.asList( d ) );

        ListIterator i = list.listIterator( 0 );
        i.next();
        i.remove();
        assertEquals( 2, list.size() );
        assertTrue( !i.hasPrevious() );
        assertTrue( i.hasNext() );
    }


    public void testRemoveNext() throws Exception {
        Data[] d = {new Data( 0 ), new Data( 1 ), new Data( 2 )};
        list.addAll( Arrays.asList( d ) );

        ListIterator i = list.listIterator();
        assertTrue( i.hasNext() );
        i.next();
        assertTrue( i.hasNext() );
        assertTrue( i.hasPrevious() );
        i.remove();
        assertEquals( 2, list.size() );
        assertTrue( !i.hasPrevious() );
        assertTrue( i.hasNext() );
        assertEquals( d[1], i.next() );
        assertTrue( i.hasNext() );
        assertEquals( d[2], i.next() );
        assertTrue( i.hasPrevious() );
        assertTrue( !i.hasNext() );
    }


    public void testRemoveThrowsAfterAdd() throws Exception {
        Data d = new Data( 0 );
        list.add( d );
        ListIterator i = list.listIterator();
        boolean didThrow = false;

        try {
            i.remove();
        }
        catch ( IllegalStateException e ) {
            didThrow = true;
        } // end of try-catch
        assertTrue( didThrow );
    }


    public void testRemoveThrowsWithoutPrevious() throws Exception {
        Data d = new Data( 0 );
        list.add( d );
        ListIterator i = list.listIterator( list.size() );
        boolean didThrow = false;

        assertTrue( i.hasPrevious() );
        try {
            i.remove();
        }
        catch ( IllegalStateException e ) {
            didThrow = true;
        } // end of try-catch
        assertTrue( didThrow );
    }


    public void testRemoveThrowsWithoutNext() throws Exception {
        Data d = new Data( 0 );
        list.add( d );
        ListIterator i = list.listIterator();
        boolean didThrow = false;

        assertTrue( i.hasNext() );
        try {
            i.remove();
        }
        catch ( IllegalStateException e ) {
            didThrow = true;
        } // end of try-catch
        assertTrue( didThrow );
    }


    public void testRemoveInvalidObject() {
        Object obj = new Object();
        Data d = new Data( 1 );
        list.add( d );
        assertEquals( 1, list.size() );

        assertFalse( "cannot remove elements that don't implement TLinkable",
                list.remove( obj ) );
    }


    public void testIteratorAddFront() throws Exception {
        Data[] d = {new Data( 0 ), new Data( 1 ), new Data( 2 )};
        list.addAll( Arrays.asList( d ) );

        ListIterator<Data> i = list.listIterator();
        Data d1 = new Data( 5 );
        assertTrue( !i.hasPrevious() );
        i.add( d1 );
        assertTrue( i.hasPrevious() );
        assertEquals( d1, i.previous() );
        assertEquals( d1, i.next() );
        assertEquals( d[0], i.next() );
        assertEquals( d1, list.get( 0 ) );
    }


    public void testIteratorAddBack() throws Exception {
        Data[] d = {new Data( 0 ), new Data( 1 ), new Data( 2 )};
        list.addAll( Arrays.asList( d ) );

        ListIterator<Data> i = list.listIterator( list.size() );
        Data d1 = new Data( 5 );
        assertEquals( 3, list.size() );
        assertTrue( i.hasPrevious() );
        assertTrue( !i.hasNext() );
        i.add( d1 );
        assertTrue( i.hasPrevious() );
        assertTrue( !i.hasNext() );
        assertEquals( 4, list.size() );

        assertEquals( d1, i.previous() );
        assertEquals( d1, i.next() );
        assertEquals( d1, list.get( 3 ) );
    }


    public void testIteratorAddMiddle() throws Exception {
        Data[] d = {new Data( 0 ), new Data( 1 ), new Data( 2 )};
        list.addAll( Arrays.asList( d ) );

        ListIterator<Data> i = list.listIterator( 1 );
        Data d1 = new Data( 5 );
        assertEquals( 3, list.size() );
        assertTrue( i.hasPrevious() );
        assertTrue( i.hasNext() );
        i.add( d1 );
        assertTrue( i.hasPrevious() );
        assertTrue( i.hasNext() );
        assertEquals( 4, list.size() );

        assertEquals( d1, i.previous() );
        assertEquals( d1, i.next() );
        assertEquals( d1, list.get( 1 ) );
    }


    public void testIteratorSetSingleElementList() throws Exception {
        Data d1 = new Data( 5 );
        Data d2 = new Data( 4 );
        list.add( d1 );

        ListIterator<Data> i = list.listIterator( 0 );
        i.next();
        i.set( d2 );
        assertEquals( 1, list.size() );
        assertTrue( !i.hasNext() );
        assertTrue( i.hasPrevious() );
        assertEquals( d2, i.previous() );
        assertTrue( i.hasNext() );
        assertTrue( !i.hasPrevious() );
        assertEquals( d2, i.next() );
    }


    public void testIteratorAddEmptyList() throws Exception {
        ListIterator<Data> i = list.listIterator();
        Data d1 = new Data( 5 );
        assertTrue( !i.hasPrevious() );
        assertTrue( !i.hasNext() );
        i.add( d1 );
        assertTrue( i.hasPrevious() );
        assertTrue( !i.hasNext() );
        assertEquals( d1, i.previous() );
        assertEquals( d1, i.next() );
        assertEquals( d1, list.get( 0 ) );
    }


    public void testIteratorRemoveOnNext() throws Exception {
        Data[] data = new Data[100];
        for ( int i = 0; i < data.length; i++ ) {
            data[i] = new Data( i );
            list.add( data[i] );
        }

        ListIterator<Data> i;

        i = list.listIterator( 5 );
        i.next();
        i.remove();
        Data d = new Data( 6 );
        assertEquals( d, list.get( 5 ) );
    }


    public void testIteratorPastEnds() {
        int size = 10;
        Data[] data = new Data[size];
        for ( int i = 0; i < data.length; i++ ) {
            data[i] = new Data( i );
            list.add( data[i] );
        }

        ListIterator<Data> iter = list.listIterator();
        Data current = list.getFirst();
        while( iter.hasNext() ) {
            assertEquals( current, list.get( iter.nextIndex() ) );
            assertEquals( current, iter.next() );
            Data test = list.getNext( current );
            if ( test != null  ) {
                current = test;
            }
        }

        // Now at end of the list;
        assertEquals( current, list.getLast() );
        assertFalse( iter.hasNext() );
        assertNull( list.getNext( current ) );

        try {
            iter.next();
            fail( "Expected NoSuchElementException" );
        }
        catch ( NoSuchElementException ex ) {
            // Expected
        }

        /// Start end and work to the front.
        current = list.getLast();
        iter = list.listIterator( list.size() );
        while ( iter.hasPrevious() ) {
            assertEquals( current, list.get( iter.previousIndex() ) );
            assertEquals( current, iter.previous() );
            Data test = list.getPrevious( current );
            if ( test != null  ) {
                current = test;
            }
        }

        // Now at front of the list;
        assertEquals( current, list.getFirst() );
        assertFalse( iter.hasPrevious() );
        assertNull( list.getPrevious( current ) );

        try {
            iter.previous();
            fail( "Expected NoSuchElementException" );
        }
        catch ( NoSuchElementException ex ) {
            // Expected
        }
    }


    public void testInvalidIterators() {
        int size = 10;
        Data[] data = new Data[size];
        for ( int i = 0; i < data.length; i++ ) {
            data[i] = new Data( i );
            list.add( data[i] );
        }

        // Creating an iterator pointing before the start of the list should fail
        try {
            ListIterator<Data> iter = list.listIterator( -1 );
            fail( "Expected IndexOutOfBoundsException" );
            iter.next(); // prevent unused inspection, never reached
        }
        catch ( IndexOutOfBoundsException ex ) {
            // Expected
        }

        // Creating an iterator pointing past the end of the list should fail
        try {
            ListIterator<Data> iter = list.listIterator( list.size() + 1 );
            fail( "Expected IndexOutOfBoundsException" );
            iter.next(); // prevent unused inspection, never reached
        }
        catch ( IndexOutOfBoundsException ex ) {
            // Expected
        }

        // Set on a fresh iterator should fail.
        try {
            ListIterator<Data> iter = list.listIterator();
            iter.set( new Data( 1138 ) );
            fail( "Expected IllegalStateException" );
        }
        catch ( IllegalStateException ex ) {
            // Expected
        }

    }


    public void testSerialize() throws Exception {
        TLinkedList<Data> list1 = new TLinkedList<Data>();
        Data[] data = new Data[100];
        for ( int i = 0; i < data.length; i++ ) {
            data[i] = new Data( i );
            list1.add( data[i] );
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( list1 );

        ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bais );

        TLinkedList list2 = (TLinkedList) ois.readObject();
        assertEquals( list1, list2 );
    }


    public void testForEach() throws Exception {
        list.add( new Data( 0 ) );
        list.add( new Data( 1 ) );
        list.add( new Data( 2 ) );
        list.add( new Data( 3 ) );
        list.add( new Data( 4 ) );

        // test exiting early
        boolean processed_full_list = list.forEachValue( new TObjectProcedure<Data>() {
            public boolean execute( Data object ) {
                if ( object._val == 2 ) {
                    return false;
                }

                object._val++;

                return true;
            }
        } );
        assertFalse( processed_full_list );

        assertEquals( 1, list.get( 0 )._val );
        assertEquals( 2, list.get( 1 )._val );
        assertEquals( 2, list.get( 2 )._val );
        assertEquals( 3, list.get( 3 )._val );
        assertEquals( 4, list.get( 4 )._val );

        // test full list processing
        processed_full_list = list.forEachValue( new TObjectProcedure<Data>() {
            public boolean execute( Data object ) {
                object._val++;
                return true;
            }
        } );
        assertTrue( processed_full_list );

        assertEquals( 2, list.get( 0 )._val );
        assertEquals( 3, list.get( 1 )._val );
        assertEquals( 3, list.get( 2 )._val );
        assertEquals( 4, list.get( 3 )._val );
        assertEquals( 5, list.get( 4 )._val );
    }


    public void testAddBefore() {
        Data one = new Data( 1 );
        Data three = new Data( 3 );
        Data four = new Data( 4 );
        Data five = new Data( 5 );

        list.add( one );
        list.add( three );
        list.add( four );
        list.add( five );

        list.addBefore( one, new Data( 0 ) );
        list.addBefore( three, new Data( 2 ) );
        list.addBefore( null, new Data( 6 ) );

//        System.out.println( "List: " + list );

        // Iterate forward
        int value = -1;
        Data cur = list.getFirst();
        while ( cur != null ) {
            assertEquals( value + 1, cur._val );
            value = cur._val;

            cur = cur.getNext();
        }

        assertEquals( 6, value );

        // Iterate backward
        value = 7;
        cur = list.getLast();
        while ( cur != null ) {
            assertEquals( value - 1, cur._val );
            value = cur._val;

            cur = cur.getPrevious();
        }

        assertEquals( 0, value );
    }


    public void testAddAfter() {
        Data one = new Data( 1 );
        Data three = new Data( 3 );
        Data five = new Data( 5 );

        list.add( one );
        list.add( three );
        list.add( five );

        list.addAfter( one, new Data( 2 ) );
        list.addAfter( three, new Data( 4 ) );
        list.addAfter( five, new Data( 6 ) );
        list.addAfter( null, new Data( 0 ) );

//        System.out.println( "List: " + list );

        // Iterate forward
        int value = -1;
        Data cur = list.getFirst();
        while ( cur != null ) {
            assertEquals( value + 1, cur._val );
            value = cur._val;

            cur = cur.getNext();
        }

        assertEquals( 6, value );

        // Iterate backward
        value = 7;
        cur = list.getLast();
        while ( cur != null ) {
//            System.out.println( "Itr back: " + cur._val );
            assertEquals( value - 1, cur._val );
            value = cur._val;

            cur = cur.getPrevious();
        }

        assertEquals( 0, value );
    }


    public void testMultipleRemove() {
        Data one = new Data( 1 );
        Data two = new Data( 2 );
        Data three = new Data( 3 );

        list.add( one );
        list.add( two );
        list.add( three );

        list.remove( one );

        assertEquals( 2, list.size() );
        assertEquals( new Data( 2 ), list.get( 0 ) );
        assertEquals( new Data( 3 ), list.get( 1 ) );

        list.remove( one );

        assertEquals( 2, list.size() );
        assertEquals( new Data( 2 ), list.get( 0 ) );
        assertEquals( new Data( 3 ), list.get( 1 ) );

        list.remove( one );

        assertEquals( 2, list.size() );
        assertEquals( new Data( 2 ), list.get( 0 ) );
        assertEquals( new Data( 3 ), list.get( 1 ) );

        list.remove( one );

        assertEquals( 2, list.size() );
        assertEquals( new Data( 2 ), list.get( 0 ) );
        assertEquals( new Data( 3 ), list.get( 1 ) );

        list.remove( one );

        assertEquals( 2, list.size() );
        assertEquals( new Data( 2 ), list.get( 0 ) );
        assertEquals( new Data( 3 ), list.get( 1 ) );
    }


    public void testRemoveFirstAll() {
        list.add( new Data( 1 ) );
        list.add( new Data( 2 ) );
        list.add( new Data( 3 ) );
        list.add( new Data( 4 ) );

        int expected = 1;
        Data data;
        while ( ( data = list.removeFirst() ) != null ) {
            assertEquals( expected, data._val );
            expected++;
        }
        assertTrue( list.isEmpty() );
    }


    public void testRemoveLastAll() {
        list.add( new Data( 1 ) );
        list.add( new Data( 2 ) );
        list.add( new Data( 3 ) );
        list.add( new Data( 4 ) );

        int expected = 4;
        Data data;
        while ( ( data = list.removeLast() ) != null ) {
            assertEquals( expected, data._val );
            expected--;
        }
        assertTrue( list.isEmpty() );
    }


    public void testPastIndexGet() {
        try {
            list.get( 0 );
            fail( "Shouldn't have allowed get of index 0" );
        }
        catch ( IndexOutOfBoundsException ex ) {
            // this is good
        }

        try {
            list.get( 1 );
            fail( "Shouldn't have allowed get of index 1" );
        }
        catch ( IndexOutOfBoundsException ex ) {
            // this is good
        }

        list.add( new Data( 1 ) );
        list.get( 0 );

        try {
            list.get( 1 );
            fail( "Shouldn't have allowed get of index 1" );
        }
        catch ( IndexOutOfBoundsException ex ) {
            // this is good
        }
    }


	public void testJDKIteratorSet2() {
		LinkedList<Data> list = new LinkedList<Data>();
		list.add( new Data( 1 ) );
		list.add( new Data( 9 ) );
		list.add( new Data( 3 ) );

		ListIterator<Data> it = ( ListIterator<Data> ) list.iterator();

		assertEquals( 1, it.next()._val );
		assertEquals( 9, it.next()._val );
		it.set( new Data( 2 ) );

		assertEquals( 3, it.next()._val );

		assertEquals( 3, list.size() );
		assertEquals( 1, list.get( 0 )._val );
		assertEquals( 2, list.get( 1 )._val );
		assertEquals( 3, list.get( 2 )._val );
	}


	public void testIteratorSet2() {
		TLinkedList<Data> list = new TLinkedList<Data>();
		list.add( new Data( 1 ) );
		list.add( new Data( 9 ) );
		list.add( new Data( 3 ) );

		ListIterator<Data> it = ( ListIterator<Data> ) list.iterator();

		assertEquals( 1, it.next()._val );
		assertEquals( 9, it.next()._val );
		it.set( new Data( 2 ) );

		assertEquals( 3, it.next()._val );

		assertEquals( 3, list.size() );
		assertEquals( 1, list.get( 0 )._val );
		assertEquals( 2, list.get( 1 )._val );
		assertEquals( 3, list.get( 2 )._val );
	}


	// See bug 2993599
	public void testShuffle() {
		for( int loop = 0; loop < 10000; loop++ ) {
			if ( ( loop % 10000 ) ==0 ) System.out.println( "Loop: " + loop );
			TLinkedList<Data> list = new TLinkedList<Data>();
			list.add( new Data( 1 ) );
			list.add( new Data( 2 ) );
			list.add( new Data( 3 ) );
			list.add( new Data( 4 ) );
			list.add( new Data( 5 ) );
			list.add( new Data( 6 ) );
			list.add( new Data( 7 ) );
			list.add( new Data( 8 ) );
			list.add( new Data( 9 ) );
			list.add( new Data( 10 ) );

			Collections.shuffle( list );

			assertEquals( 10, list.size() );

			// Make sure all numbers are contained in the list exactly once
			for( int i = 1; i <= 10; i++ ) {
				boolean found_it = false;
				for( Data data : list ) {
					if ( data._val == i ) {
						assertFalse( "Number found twice: " + i, found_it );
						found_it = true;
					}
				}
				assertTrue( "Number not found: " + i, found_it );
			}
		}
	}


	public void testShuffle2() {
		Data data1 = new Data( 1 );
		Data data2 = new Data( 2 );
		Data data3 = new Data( 3 );
		Data data4 = new Data( 4 );
		Data data5 = new Data( 5 );
		Data data6 = new Data( 6 );
		Data data7 = new Data( 7 );
		Data data8 = new Data( 8 );
		Data data9 = new Data( 9 );
		Data data10 = new Data( 10 );

		TLinkedList<Data> list = new TLinkedList<Data>();
		list.add( new Data( 1 ) );
		list.add( new Data( 2 ) );
		list.add( new Data( 3 ) );
		list.add( new Data( 4 ) );
		list.add( new Data( 5 ) );
		list.add( new Data( 6 ) );
		list.add( new Data( 7 ) );
		list.add( new Data( 8 ) );
		list.add( new Data( 9 ) );
		list.add( new Data( 10 ) );

		ListIterator<Data> it = ( ListIterator<Data> ) list.iterator();
		it.next();
		it.set( data10 );
	}

	public void testFirstLastIteratorSwap() {
		Data data10 = new Data( 10 );

		TLinkedList<Data> list = new TLinkedList<Data>();
		list.add( new Data( 1 ) );
		list.add( new Data( 2 ) );
		list.add( new Data( 3 ) );
		list.add( new Data( 4 ) );
		list.add( new Data( 5 ) );
		list.add( new Data( 6 ) );
		list.add( new Data( 7 ) );
		list.add( new Data( 8 ) );
		list.add( new Data( 9 ) );
		list.add( data10 );

		ListIterator<Data> it = ( ListIterator<Data> ) list.iterator();
		it.next();
		it.set( data10 );
	}

	public void testIteratorSwapSequential() {
		Data slot3 = new Data( 3 );
		TLinkedList<Data> list = new TLinkedList<Data>();
		list.add( new Data( 1 ) );
		list.add( new Data( 2 ) );
		list.add( slot3 );
		list.add( new Data( 4 ) );


		ListIterator<Data> it = ( ListIterator<Data> ) list.iterator();
		it.next();
		it.next();
		it.set( slot3 );
	}

	public void testBinarySearch() {
		System.out.println( "Java version: " + System.getProperty( "java.version" ) );
		TIntList list;

		// Uncomment to stress test
//		for( int i = 0; i <= 100000; i++ ) {
//			if ( ( i % 100 ) == 0 ) {
//				System.out.print( i );
//				System.out.print( " " );
//			}
//
//			list = new TIntArrayList( i );
//			list.add( 5 );
//			list.binarySearch( 6 );
//		}

		list = new TIntLinkedList();
		list.add( 5 );

		// Uncomment this to see infinite loop from bug 3379877
//		list.binarySearch( 6 );

		assertEquals( -1, list.binarySearch( Integer.MIN_VALUE ) );
		assertEquals( -1, list.binarySearch( -1 ) );
		assertEquals( -1, list.binarySearch( 0 ) );
		assertEquals( -1, list.binarySearch( 1 ) );
		assertEquals( -1, list.binarySearch( 2 ) );
		assertEquals( -1, list.binarySearch( 3 ) );
		assertEquals( -1, list.binarySearch( 4 ) );

		assertEquals( 0, list.binarySearch( 5 ) );

		assertEquals( -2, list.binarySearch( 6 ) );
	}


	public void testSum() {
		TIntList list = new TIntLinkedList();
		assertEquals( 0, list.sum() );

		list.add( 1 );
		assertEquals( 1, list.sum() );

		list.add( 1234 );
		assertEquals( 1235, list.sum() );

		list.removeAt( 0 );
		assertEquals( 1234, list.sum() );

		list.clear();
		assertEquals( 0, list.sum() );
	}


    static class Data implements TLinkable<Data> {

        protected int _val;


        public Data( int val ) {
            _val = val;
        }


        protected TLinkable<Data> _next;


        // NOTE: use covariant overriding
        /**
         * Get the value of next.
         *
         * @return value of next.
         */
        public Data getNext() {
            return (Data) _next;
        }


        /**
         * Set the value of next.
         *
         * @param next value to assign to next.
         */
        public void setNext( Data next ) {
            this._next = next;
        }


        protected Data _previous;


        // NOTE: use covariant overriding
        /**
         * Get the value of previous.
         *
         * @return value of previous.
         */
        public Data getPrevious() {
            return _previous;
        }


        /**
         * Set the value of previous.
         *
         * @param previous value to assign to previous.
         */
        public void setPrevious( Data previous ) {
            this._previous = previous;
        }


        public String toString() {
            return "" + _val;
        }


        public boolean equals( Object o ) {
            if ( !( o instanceof Data ) ) {
                return false;
            }

            Data that = (Data) o;
            return this._val == that._val;
        }
    }

} // TLinkedListTests
