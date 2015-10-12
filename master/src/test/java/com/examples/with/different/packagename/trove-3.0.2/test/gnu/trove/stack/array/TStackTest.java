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

package gnu.trove.stack.array;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.stack.TIntStack;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;



/**
 *
 */
public class TStackTest extends TestCase {


    public TStackTest() {
        super();
    }


    public TStackTest( String string ) {
        super( string );
    }


    public void testConstructors() {
        TIntStack stack = new TIntArrayStack();
        assertEquals( 0, stack.size() );

        stack.push( 10 );
        stack.push( 20 );
        assertEquals( 2, stack.size() );

        TIntStack other = new TIntArrayStack( 20 );
        other.push( 10 );
        other.push( 20 );
        assertEquals( 2, other.size() );

        assertTrue( "stacks should be equal: " + stack + ", " + other,
			stack.equals( other ) );

        TIntStack copy = new TIntArrayStack( stack );
        assertTrue( "stacks should be equal: " + stack + ", " + copy,
			stack.equals( copy ) );
    }


    public void testBasic() {
        TIntStack stack = new TIntArrayStack();

        assertEquals( 0, stack.size() );

        stack.push( 10 );

        assertEquals( 1, stack.size() );

        assertEquals( 10, stack.peek() );
        assertEquals( 1, stack.size() );
        assertEquals( 10, stack.peek() );
        assertEquals( 1, stack.size() );

        assertEquals( 10, stack.pop() );
        assertEquals( 0, stack.size() );

        stack.push( 10 );
        stack.push( 20 );
        stack.push( 30 );

        assertEquals( 3, stack.size() );
        assertEquals( 30, stack.pop() );
        assertEquals( 20, stack.pop() );
        assertEquals( 10, stack.pop() );
    }


    public void testArrays() {
        int no_entry_value = Integer.MIN_VALUE;
        TIntStack stack = new TIntArrayStack(10, no_entry_value);
        assertEquals( no_entry_value, stack.getNoEntryValue() );

        int[] array;

        array = stack.toArray();
        assertNotNull( array );
        assertEquals( 0, array.length );

        stack.push( 10 );
        stack.push( 20 );
        stack.push( 30 );
        stack.push( 40 );

        array = stack.toArray();
        assertNotNull( array );
        assertEquals( 4, array.length );
        // NOTE: Top element in stack should be first element in array
        assertEquals( 40, array[0] );
        assertEquals( 30, array[1] );
        assertEquals( 20, array[2] );
        assertEquals( 10, array[3] );
        assertEquals( 4, stack.size() );

        int[] array_correct_size = new int[4];
        stack.toArray( array_correct_size );
        assertEquals( 40, array_correct_size[0] );
        assertEquals( 30, array_correct_size[1] );
        assertEquals( 20, array_correct_size[2] );
        assertEquals( 10, array_correct_size[3] );

        int[] array_too_long = new int[6];
        stack.toArray( array_too_long );
        assertEquals( 40, array_too_long[0] );
        assertEquals( 30, array_too_long[1] );
        assertEquals( 20, array_too_long[2] );
        assertEquals( 10, array_too_long[3] );
        assertEquals( stack.getNoEntryValue(), array_too_long[4] );

        int[] array_too_short = new int[2];
        stack.toArray( array_too_short );
        assertEquals( 40, array_too_short[0] );
        assertEquals( 30, array_too_short[1] );
    }


    public void testClear() {
        TIntStack stack = new TIntArrayStack();

        assertEquals( 0, stack.size() );

        stack.push( 10 );

        assertEquals( 1, stack.size() );
        assertEquals( 10, stack.pop() );
        assertEquals( 0, stack.size() );

        stack.push( 10 );
        stack.push( 20 );
        stack.push( 30 );

        assertEquals( 3, stack.size() );
        stack.clear();

        assertEquals( 0, stack.size() );
    }


    public void testEquals() {
        TIntStack stack = new TIntArrayStack();
        assertEquals( 0, stack.size() );

        stack.push( 10 );
        stack.push( 20 );
        assertEquals( 2, stack.size() );

        TIntStack other = new TIntArrayStack( 20 );
        other.push( 10 );
        other.push( 20 );
        assertEquals( 2, other.size() );

        assertTrue( "stacks should equal itself: " + stack,
			stack.equals( stack ) );

        assertTrue( "stacks should be equal: " + stack + ", " + other,
			stack.equals( other ) );

        TIntArrayList list = new TIntArrayList( stack.toArray() );
        assertFalse( "stack should not equal list: " + stack + ", " + list,
			stack.equals( list ) );
    }


    public void testHashCode() {
        TIntStack stack = new TIntArrayStack();
        assertEquals( 0, stack.size() );

        stack.push( 10 );
        stack.push( 20 );
        assertEquals( 2, stack.size() );

        TIntStack other = new TIntArrayStack( 20 );
        other.push( 10 );
        other.push( 20 );
        assertEquals( 2, other.size() );

        assertTrue( "stack hashcode should equal itself: " + stack,
			stack.hashCode() == stack.hashCode() );

        assertTrue( "stacks should be equal: " + stack + ", " + other,
			stack.hashCode() == other.hashCode() );

        other.push( 30 );
        assertFalse( "stack should not equal list: " + stack + ", " + other,
			stack.hashCode() == other.hashCode() );
    }


    public void testSerialize() throws Exception {
        TIntStack stack = new TIntArrayStack();
        stack.push( 10 );
        stack.push( 20 );
        stack.push( 30 );
        stack.push( 40 );
        stack.push( 50 );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( stack );

        ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bais );

        TIntStack serialized = (TIntStack) ois.readObject();

        assertEquals( stack, serialized );
    }
}
