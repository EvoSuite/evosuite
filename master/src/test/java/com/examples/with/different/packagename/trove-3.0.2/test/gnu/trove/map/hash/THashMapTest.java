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

import gnu.trove.function.TObjectFunction;
import gnu.trove.impl.hash.HashTestKit;
import gnu.trove.impl.hash.TObjectHash;
import gnu.trove.procedure.TObjectObjectProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSetTest;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;



/**
 * Created: Sat Nov  3 10:31:38 2001
 *
 * @author Eric D. Friedman
 * @author Rob Eden
 * @author Jeff Randall
 * @version $Id: THashMapTest.java,v 1.1.2.7 2010/03/02 04:09:50 robeden Exp $
 */

public class THashMapTest extends TestCase {

    protected THashMap<String, String> ss_map;
    protected THashMap<String, Integer> si_map;
    protected int count;


    public THashMapTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
        ss_map = new THashMap<String, String>();
        si_map = new THashMap<String, Integer>();
        count = 0;
    }


    public void tearDown() throws Exception {
        super.tearDown();
        ss_map = null;
        si_map = null;
        count = 0;
    }


    public void testConstructors() {
        String[] keys = {"Key1", "Key2", "Key3", "Key4", "Key5"};
        String[] values = {"Val1", "Val2", "Val3", "Val4", "Val5"};
        for ( int i = 0; i < keys.length; i++ ) {
            ss_map.put( keys[i], values[i] );
        }

        THashMap<String, String> sized = new THashMap<String, String>( 100 );
        for ( int i = 0; i < keys.length; i++ ) {
            sized.put( keys[i], values[i] );
        }
        assertTrue( "maps should be a copy of each other", ss_map.equals( sized ) );


        THashMap<String, String> factor = new THashMap<String, String>( 100, 1.0f );
        for ( int i = 0; i < keys.length; i++ ) {
            factor.put( keys[i], values[i] );
        }
        assertTrue( "maps should be a copy of each other", ss_map.equals( factor ) );


        THashMap<String, String> copy = new THashMap<String, String>( ss_map );
        assertTrue( "maps should be a copy of each other", ss_map.equals( copy ) );

        for ( int i = 0; i < keys.length; i++ ) {
            assertEquals( values[i], copy.get( keys[i] ) );
        }

        Map<String, String> java_hashmap = new HashMap<String, String>();
        for ( int i = 0; i < keys.length; i++ ) {
            java_hashmap.put( keys[i], values[i] );
        }
        THashMap<String, String> java_hashmap_copy =
                new THashMap<String, String>( java_hashmap );
        assertTrue( "maps should be a copy of each other",
                ss_map.equals( java_hashmap_copy ) );

    }


    public void testEquals() {
        String[] keys = {"Key1", "Key2", "Key3", "Key4", "Key5"};
        String[] values = {"Val1", "Val2", "Val3", "Val4", "Val5"};
        for ( int i = 0; i < keys.length; i++ ) {
            ss_map.put( keys[i], values[i] );
        }

        assertFalse( "should not equal random Object", ss_map.equals( new Object() ) );

        THashMap<String, String> copy = new THashMap<String, String>( ss_map );
        assertTrue( "maps should be a copy of each other", ss_map.equals( copy ) );

        // Change the Length.
        copy.put( "Key6", "Val6" );
        assertFalse( "maps should no longer be a copy of each other",
                ss_map.equals( copy ) );
    }


    public void testPut() throws Exception {
        assertEquals( "put succeeded", null, ss_map.put( "One", "two" ) );
        assertEquals( "size did not reflect put", 1, ss_map.size() );
        assertEquals( "put/get failed", "two", ss_map.get( "One" ) );
        assertEquals( "second put failed", "two", ss_map.put( "One", "foo" ) );
    }


    public void testPutIfAbsent() throws Exception {
        assertEquals( "putIfAbsent succeeded", null, ss_map.putIfAbsent( "One", "two" ) );
        assertEquals( "size did not reflect putIfAbsent", 1, ss_map.size() );
        assertEquals( "putIfAbsent/get failed", "two", ss_map.get( "One" ) );
        assertEquals( "second putIfAbsent failed", "two", ss_map.putIfAbsent( "One", "foo" ) );
        assertEquals( "size did not reflect putIfAbsent", 1, ss_map.size() );
        assertEquals( "putIfAbsent/get failed", "two", ss_map.get( "One" ) );
        assertEquals( "third putIfAbsent failed", null, ss_map.putIfAbsent( "Two", "bar" ) );
        assertEquals( "size did not reflect putIfAbsent", 2, ss_map.size() );
        assertEquals( "putIfAbsent/get failed", "bar", ss_map.get( "Two" ) );
    }


    public void testClear() throws Exception {
        assertEquals( "initial size was not zero", 0, ss_map.size() );
        assertEquals( "put succeeded", null, ss_map.put( "One", "two" ) );
        assertEquals( "size did not reflect put", 1, ss_map.size() );
        ss_map.clear();
        assertEquals( "cleared size was not zero", 0, ss_map.size() );
    }


    public void testContains() throws Exception {
        String key = "hi";
        assertTrue( "should not contain key initially", !si_map.contains( key ) );
        assertEquals( "put succeeded", null, si_map.put( key, Integer.valueOf( 1 ) ) );
        assertTrue( "key not found after put", si_map.contains( key ) );
        assertFalse( "non-existant key found", si_map.contains( "bye" ) );
    }


    public void testContainsKey() throws Exception {
        String key = "hi";
        assertTrue( "should not contain key initially", !si_map.containsKey( key ) );
        assertEquals( "put succeeded", null, si_map.put( key, Integer.valueOf( 1 ) ) );
        assertTrue( "key not found after put", si_map.containsKey( key ) );
        assertFalse( "non-existant key found", si_map.containsKey( "bye" ) );
    }


    public void testContainsValue() throws Exception {
        String key = "hi";
        String value = "bye";
        assertTrue( "should not contain key initially", !ss_map.containsValue( value ) );
        assertEquals( "put succeeded", null, ss_map.put( key, value ) );
        assertTrue( "key not found after put", ss_map.containsValue( value ) );
        assertFalse( "non-existant key found", ss_map.containsValue( "whee" ) );
    }


    @SuppressWarnings({"SuspiciousMethodCalls"})
    public void testGet() throws Exception {
        String key = "hi", val = "one", val2 = "two";

        ss_map.put( key, val );
        assertEquals( "get did not return expected value", val, ss_map.get( key ) );
        ss_map.put( key, val2 );
        assertEquals( "get did not return expected value on second put",
                val2, ss_map.get( key ) );

        // Invalid key should return null
        assertNull( ss_map.get( new Object() ) );
    }


    public void testValues() throws Exception {
        String k1 = "1", k2 = "2", k3 = "3", k4 = "4", k5 = "5";
        String v1 = "x", v2 = "y", v3 = "z";

        ss_map.put( k1, v1 );
        ss_map.put( k2, v1 );
        ss_map.put( k3, v2 );
        ss_map.put( k4, v3 );
        ss_map.put( k5, v2 );
        Collection vals = ss_map.values();
        assertEquals( "size was not 5", 5, vals.size() );
        vals.remove( "z" );
        assertEquals( "size was not 4", 4, vals.size() );
        vals.remove( "y" );
        assertEquals( "size was not 3", 3, vals.size() );
        vals.remove( "y" );
        assertEquals( "size was not 2", 2, vals.size() );
        assertEquals( "map did not diminish to 2 entries", 2, ss_map.size() );
    }


    @SuppressWarnings({"WhileLoopReplaceableByForEach"})
    public void testKeySet() throws Exception {
        String key1 = "hi", key2 = "bye", key3 = "whatever";
        String val = "x";

        ss_map.put( key1, val );
        ss_map.put( key2, val );
        ss_map.put( key3, val );

        Set keys = ss_map.keySet();
        assertTrue( "keyset did not match expected set",
                keys.containsAll( Arrays.asList( key1, key2, key3 ) ) );
        assertEquals( 3, ss_map.size() );

        int count = 0;
        Iterator it = keys.iterator();
        while ( it.hasNext() ) {
            count++;
            it.next();
        }
        assertEquals( ss_map.size(), count );

        for ( Iterator i = keys.iterator(); i.hasNext(); ) {
            Object o = i.next();
            if ( o.equals( key2 ) ) {
                i.remove();
            }
        }
        assertTrue( "keyset did not match expected set",
                keys.containsAll( Arrays.asList( key1, key3 ) ) );
    }


    public void testForEachKey() throws Exception {
        THashMap<Integer, String> map = new THashMap<Integer, String>();
        String[] vals = {"one", "two", "three", "four"};
        Integer[] keys = {new Integer( 1 ),
                          new Integer( 2 ),
                          new Integer( 3 ),
                          new Integer( 4 )
        };
        for ( int i = 0; i < keys.length; i++ ) {
            map.put( keys[i], vals[i] );
        }

        TObjectProcedure<Integer> proc =
                new TObjectProcedure<Integer>() {
                    public boolean execute( Integer value ) {
                        count += value;
                        return true;
                    }
                };
        assertTrue( "should complete successfully", map.forEachKey( proc ) );
        assertEquals( 10, count );
    }


    public void testForEachKeyFalse() throws Exception {
        THashMap<Integer, String> map = new THashMap<Integer, String>();
        String[] vals = {"one", "two", "three", "four"};
        Integer[] keys = {new Integer( 1 ),
                          new Integer( 2 ),
                          new Integer( 3 ),
                          new Integer( 4 )
        };
        for ( int i = 0; i < keys.length; i++ ) {
            map.put( keys[i], vals[i] );
        }

        TObjectProcedure<Integer> proc =
                new TObjectProcedure<Integer>() {
                    public boolean execute( Integer value ) {
                        count++;
                        return false;
                    }
                };
        assertFalse( "should Break after first iteration", map.forEachKey( proc ) );
        assertEquals( 1, count );
    }


    public void testForEachValue() throws Exception {
        String[] keys = {"one", "two", "three", "four"};
        Integer[] vals = {new Integer( 1 ),
                          new Integer( 2 ),
                          new Integer( 3 ),
                          new Integer( 4 )
        };
        for ( int i = 0; i < keys.length; i++ ) {
            si_map.put( keys[i], vals[i] );
        }

        TObjectProcedure<Integer> proc =
                new TObjectProcedure<Integer>() {
                    public boolean execute( Integer value ) {
                        count += value;
                        return true;
                    }
                };
        si_map.forEachValue( proc );
        assertEquals( 10, count );
    }


    public void testForEachValueFalse() throws Exception {
        String[] keys = {"one", "two", "three", "four"};
        Integer[] vals = {new Integer( 1 ),
                          new Integer( 2 ),
                          new Integer( 3 ),
                          new Integer( 4 )
        };
        for ( int i = 0; i < keys.length; i++ ) {
            si_map.put( keys[i], vals[i] );
        }

        TObjectProcedure<Integer> proc =
                new TObjectProcedure<Integer>() {
                    public boolean execute( Integer value ) {
                        count++;
                        return false;
                    }
                };
        assertFalse( "should Break after first iteration", si_map.forEachValue( proc ) );
        assertEquals( 1, count );
    }


    public void testForEachEntry() throws Exception {
        String[] keys = {"one", "two", "three", "four"};
        Integer[] vals = {new Integer( 1 ),
                          new Integer( 2 ),
                          new Integer( 3 ),
                          new Integer( 4 )
        };
        for ( int i = 0; i < keys.length; i++ ) {
            si_map.put( keys[i], vals[i] );
        }

        TObjectObjectProcedure<String, Integer> proc =
                new TObjectObjectProcedure<String, Integer>() {
                    public boolean execute( String key, Integer value ) {
                        count += value;
                        return true;
                    }
                };
        si_map.forEachEntry( proc );
        assertEquals( 10, count );
    }


    public void testForEachEntryInterrupt() throws Exception {
        String[] keys = {"one", "two", "three", "four"};
        Integer[] vals = {new Integer( 1 ),
                          new Integer( 2 ),
                          new Integer( 3 ),
                          new Integer( 4 )
        };
        for ( int i = 0; i < keys.length; i++ ) {
            si_map.put( keys[i], vals[i] );
        }

        TObjectObjectProcedure<String, Integer> proc =
                new TObjectObjectProcedure<String, Integer>() {
                    public boolean execute( String key, Integer value ) {
                        count += value;
                        return count < 6;
                    }
                };
        si_map.forEachEntry( proc );
        assertTrue( count < 10 );
    }


    public void testTransformValues() throws Exception {
        String[] keys = {"one", "two", "three", "four"};
        Integer[] vals = {new Integer( 1 ),
                          new Integer( 2 ),
                          new Integer( 3 ),
                          new Integer( 4 )
        };
        for ( int i = 0; i < keys.length; i++ ) {
            si_map.put( keys[i], vals[i] );
        }

        TObjectFunction<Integer, Integer> func =
                new TObjectFunction<Integer, Integer>() {
                    public Integer execute( Integer value ) {
                        return new Integer( value << 1 );
                    }
                };
        si_map.transformValues( func );
        assertEquals( new Integer( 2 ), si_map.get( "one" ) );
        assertEquals( new Integer( 4 ), si_map.get( "two" ) );
        assertEquals( new Integer( 6 ), si_map.get( "three" ) );
        assertEquals( new Integer( 8 ), si_map.get( "four" ) );
    }


    @SuppressWarnings({"ForLoopReplaceableByForEach"})
    public void testKeyIterator() throws Exception {
        String[] keys = {"one", "two", "three", "four"};
        Integer[] vals = {new Integer( 1 ),
                          new Integer( 2 ),
                          new Integer( 3 ),
                          new Integer( 4 )
        };
        for ( int i = 0; i < keys.length; i++ ) {
            si_map.put( keys[i], vals[i] );
        }

        int count = 0;
        for ( Iterator i = si_map.keySet().iterator(); i.hasNext(); ) {
            i.next();
            count++;
        }
        assertEquals( 4, count );
    }


    public void testContainsNullValue() throws Exception {
        si_map.put( "a", null );
        assertTrue( si_map.containsValue( null ) );
    }


    public void testEntrySetContainsEntryWithNullValue() throws Exception {
        si_map.put( "0", null );
        Map.Entry<String, Integer> ee = si_map.entrySet().iterator().next();
        assertTrue( si_map.entrySet().contains( ee ) );
    }


    public void testValueSetRemoveNullValue() throws Exception {
        si_map.put( "0", null );
        assertTrue( si_map.values().remove( null ) );
    }


    public void testSizeAfterEntrySetRemove() throws Exception {
        si_map.put( "0", null );
        Map.Entry<String, Integer> ee = si_map.entrySet().iterator().next();
        assertTrue( ee.getKey().equals( "0" ) );
        assertNull( ee.getValue() );
        assertTrue( "remove on entrySet() returned false",
                si_map.entrySet().remove( ee ) );
        assertEquals( 0, si_map.size() );
    }


    @SuppressWarnings({"SuspiciousMethodCalls"})
    public void testEntrySetRemoveSameKeyDifferentValues() throws Exception {
        ss_map.put( "0", "abc" );
        si_map.put( "0", Integer.valueOf( 123 ) );
        Map.Entry<String, String> ee = ss_map.entrySet().iterator().next();
        assertEquals( 1, si_map.size() );
        assertTrue( !si_map.entrySet().contains( ee ) );
        assertTrue( !si_map.entrySet().remove( ee ) );
    }


    public void testSizeAfterMultipleReplacingPuts() throws Exception {
        ss_map.put( "key", "a" );
        assertEquals( 1, ss_map.size() );
        ss_map.put( "key", "b" );
        assertEquals( 1, ss_map.size() );
    }


    @SuppressWarnings({"unchecked"})
    public void testSerializable() throws Exception {
        // Use a non-standard load factor to more fully test serialization
        THashMap<String, String> map = new THashMap<String, String>( 100, 0.75f );
        map.put( "a", "b" );
        map.put( "b", "c" );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( map );

        ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bais );

        THashMap<String, String> deserialized = (THashMap<String, String>) ois.readObject();

        assertEquals( map, deserialized );
    }


    public void testRetainEntries() throws Exception {
        ss_map.put( "a", "b" );
        ss_map.put( "c", "b" );
        ss_map.put( "d", "b" );

        ss_map.retainEntries( new TObjectObjectProcedure<String, String>() {
            public boolean execute( String key, String val ) {
                return key.equals( "c" );
            }
        } );

        assertEquals( 1, ss_map.size() );
        assertTrue( ss_map.containsKey( "c" ) );
        assertEquals( "b", ss_map.get( "c" ) );
    }


    public void testPutAll() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put( "one", "two" );
        map.put( "two", "four" );
        map.put( "three", "six" );
        ss_map.putAll( map );
    }


    @SuppressWarnings({"RedundantStringConstructorCall"})
    public void testHashCode() throws Exception {
        THashMap<String, String> ss_map2 = new THashMap<String, String>();
        ss_map.put( new String( "foo" ), new String( "bar" ) );
        ss_map2.put( new String( "foo" ), new String( "bar" ) );
        assertEquals( ss_map.hashCode(), ss_map2.hashCode() );
        assertEquals( ss_map, ss_map2 );
        ss_map2.put( new String( "cruft" ), new String( "bar" ) );
        assertTrue( ss_map.hashCode() != ss_map2.hashCode() );
        assertTrue( !ss_map.equals( ss_map2 ) );
    }


    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    public void testBadlyWrittenKey() {
        THashMap<THashSetTest.Crap, Integer> map = new THashMap<THashSetTest.Crap, Integer>();
        boolean didThrow = false;
        try {
            for ( int i = 0; i < 1000; i++ ) { // enough to trigger a rehash
                map.put( new THashSetTest.Crap(), new Integer( i ) );
            }
        }
        catch ( IllegalArgumentException e ) {
            didThrow = true;
        }
        assertTrue( "expected THashMap to throw an IllegalArgumentException", didThrow );
    }


    public void testKeySetEqualsEquivalentSet() {
        Set<String> set = new HashSet<String>();
        set.add( "foo" );
        set.add( "doh" );
        set.add( "hal" );

        THashMap<String, String> tv1 = new THashMap<String, String>();
        tv1.put( "doh", "blah" );
        tv1.put( "foo", "blah" );
        tv1.put( "hal", "notblah" );
        assertTrue( tv1.keySet().equals( set ) );
    }


    @SuppressWarnings({"unchecked"})
    public void testNullValue() {
        ss_map.put( "foo", null );
        ss_map.put( "bar", null );
        ss_map.put( "baz", null );

        assertEquals( Arrays.asList( null, null, null ), new ArrayList( ss_map.values() ) );
    }


    public void testNullValueSize() {
        ss_map.put( "narf", null );
        ss_map.put( "narf", null );
        assertEquals( 1, ss_map.size() );
    }


    public void testNullKey() {
        ss_map.put( null, "null" );
        assertEquals( null, ss_map.keySet().iterator().next() );

		ss_map.put( "one", "1" );
		ss_map.put( "two", "2" );
		ss_map.put( "three", "3" );

	    assertEquals( "null", ss_map.get( null ) );
	    assertEquals( "1", ss_map.get( "one" ) );
	    assertEquals( "2", ss_map.get( "two" ) );
	    assertEquals( "3", ss_map.get( "three" ) );

	    String old_value = ss_map.put( null, "null_new" );
	    assertEquals( "null", old_value );
	    assertEquals( "null_new", ss_map.get( null ) );
    }


    public void testRetainEntrySet() {
        ss_map.put( "one", "frodo" );
        ss_map.put( "two", "bilbo" );
        ss_map.put( "three", "samwise" );

        Map<String, String> subset = new HashMap<String, String>();
        subset.put( "two", "bilbo" );

        assertTrue( ss_map.entrySet().retainAll( subset.entrySet() ) );

        assertEquals( subset, ss_map );
    }


    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    public void testMapEntrySetHashCode() {
        ss_map.put( "one", "foo" );
        Map<String, String> m2 = new THashMap<String, String>();
        m2.put( "one", "foo" );

        Object o1 = ss_map.entrySet().iterator().next();
        Object o2 = ss_map.entrySet().iterator().next();
        assertTrue( o1 != o2 );
        assertTrue( o1.equals( o2 ) );
        assertEquals( o1.hashCode(), o2.hashCode() );
    }


    public void testEqualsAndHashCode() {
        THashMap<String, String> map1 = new THashMap<String, String>();
        map1.put( "Key1", null );

        THashMap<String, String> map2 = new THashMap<String, String>();
        map2.put( "Key2", "Value2" );

        assertFalse( "map1.equals( map2 )", map1.equals( map2 ) );
        assertFalse( "map2.equals( map1 )", map2.equals( map1 ) );

        THashMap<String, String> clone_map1 = new THashMap<String, String>( map1 );
        THashMap<String, String> clone_map2 = new THashMap<String, String>( map2 );

        assertEquals( map1, clone_map1 );
        assertEquals( map1.hashCode(), clone_map1.hashCode() );
        assertEquals( map2, clone_map2 );
        assertEquals( map2.hashCode(), clone_map2.hashCode() );
    }


    /**
     * Test case for bug #1428614.
     * http://sourceforge.net/tracker/index.php?func=detail&aid=1428614&group_id=39235&atid=424682
     */
    public void testRemoveValue() {
        ss_map.put( "one", "a" );
        ss_map.put( "two", "a" );
        ss_map.put( "three", "b" );

        assertEquals( 3, ss_map.size() );

        ss_map.values().remove( "a" );

        assertEquals( 2, ss_map.size() );

        assertFalse( ss_map.values().remove( "non-existant" ) );
    }


    /**
     * This test case arose out of a problem that was ultimately caused by the
     * implementation if THashMap.removeAt(int) being incorrect (the super implementation
     * was being called before the local implementation which caused problems when
     * auto-compaction occurred). So, it's a little wiggy, but since the case was useful
     * once, I figure I'll leave it in. - RDE
     */
    @SuppressWarnings({"ForLoopReplaceableByForEach",
                       "MismatchedQueryAndUpdateOfCollection"})
    public void testProblematicRemove() {
        int[] to_add = new int[]{
                9707851, 1432929, 7941420, 8698105, 9178562, 14368620, 2165498, 5759024,
                4160722, 1835074, 5570057, 15866937, 1774305, 7645103, 11340758, 14962053,
                10326974, 5153366, 8644308, 10981652, 2484113, 8790215, 13765491, 15579334,
                16644662, 3538325, 10183070, 2491991, 6963721, 1406388, 14845099, 7614579,
                1630785, 11417789, 988981, 12372702, 11197485, 6115155, 185310, 10733210,
                4444363, 4256211, 12877269, 2178642, 8551718, 15095256, 922983, 10434400,
                15511986, 8792944, 9301853, 6147907, 13778707, 2822511, 8760514, 1112301,
                4624291, 8406178, 1708017, 834479, 16113199, 13501060, 477791, 10737091,
                2568325, 14839924, 4523777, 13566852, 15722007, 15406704, 932352, 127549,
                13010531, 10540591, 5100961, 288048, 9396085, 12844726, 8887992, 12932686,
                10827776, 16751176, 15337379, 10192984, 1341540, 15470105, 9555179, 2394843,
                1595902, 12345969, 14884594, 313940, 8348091, 15293845, 16706146, 13500767,
                12331344, 3959435, 7796580, 7077998, 9458485, 4648605, 14396291, 14246507,
                13404097, 14988605, 3600161, 9280290, 12842832, 10606027, 14355582, 1136752,
                12921165, 1749219, 5457859, 9197696, 421256, 73503, 10633537, 6952660,
                58264, 6164651, 9993341, 1386282, 12470635, 12982240, 4816459, 159250,
                8949093, 16447937, 2290043, 1828537, 13148199, 9084516, 10802630, 13549340,
                6609445, 2995162, 8040282, 9333002, 9580969, 16406600, 12194236, 14835440,
                13041360, 8604473, 12565810, 1988729, 4355359, 1535580, 5149176, 5324752,
                3438993, 1660570, 8691895, 5474195, 15334260, 8105437, 13879037, 11351030,
                3039475, 14625682, 10849417, 11379670, 14640675, 11176427, 4513965, 16428465,
                10578701, 8072595, 15538803, 6526027, 10283251, 8507206, 5188636, 14223982,
                3920972, 15659969, 12922874, 13689914, 3658005, 8379437, 5247277, 9938991,
                10659712, 10678905, 14485926, 10786998, 2488171, 9881794, 5651833, 14538812,
                10444169, 11922157, 5403811, 6785775, 13794224, 11958753, 16494451, 12313218,
                1297763, 1126021, 345418, 528188, 2114627, 6400133, 8307281, 490653,
                8793363, 16336633, 10653219, 2214875, 13551003, 1001246, 397033, 12381376,
                5991449, 1443593, 2622811, 7847125, 944151, 13889218, 14686341, 6939782,
                1712728, 12902341, 4138467, 13656813, 973555, 4767079, 9100632, 13222220,
                11641873, 9082629, 12639233, 11258141, 2146301, 1867442, 12719413, 16679909,
                8734589, 1606713, 9501535, 6761395, 6693929, 13647363, 9914313, 15335895,
                2021653, 4068909, 2228794, 12603329, 11807792, 12665538, 395494, 3299447,
                5119086, 2917420, 10055436, 4831656, 3927888, 14447609, 4329094, 13077467,
                11461522, 14594477, 6481991, 8367571, 7156844, 9223013, 6482120, 10542896,
                10286402, 11125093, 14144872, 16495825, 1844776, 860068, 9980169, 14877755,
                2804551, 8894974, 12849678, 8215338, 15490728, 3843485, 5184218, 7071904,
                7703600, 4633458, 11481528, 15295069, 3736219, 14297843, 3787634, 6015173,
                14290065, 7290758, 11764335, 3688927, 7991532, 12075598, 606202, 4674522,
                13772937, 6515326, 14974617, 3385263, 4587760, 15178512, 7689244, 15015527,
                3087738, 3683764, 5107535, 10120404, 6225460, 8588999, 4151219, 9885848,
                6691152, 518908, 13918089, 13393004, 13093729, 16338349, 5945377, 15632500,
                4230314, 13832167, 12139768, 5361165, 11457892, 3916190, 2387780, 325816,
                6621694, 7540927, 5271271, 10565439, 3281837, 11138623, 6663214, 737100,
                6864802, 16592415, 14615312, 4342441, 2525512, 16706191, 14258395, 11878990,
                1320531, 14696398, 8201398, 16077183, 12155328, 15225360, 6533378, 16390602,
                11750387, 4144864, 3744598, 4136761, 1775074, 3787875, 10061327, 3165792,
                6921717, 84292, 7420530, 11805441, 6704350, 4234280, 13377633, 6417611,
                81563, 11879309, 6692731, 10285066, 5452490, 2848306, 6094584, 6772150,
                2899899, 805004, 7273360, 4566720, 13878010, 10871921, 3724097, 11896809,
                15586671, 5744620, 13731591, 16250661, 8560290, 8169917, 7059338, 14615241,
                3149669, 4383295, 1292178, 7919990, 846550, 896930, 8769114, 11437924,
                3854132, 16345157, 2929809, 186157, 8183120, 10860321, 10092509, 7157818,
                8817522, 2944051, 4664124, 6791689, 12517547, 12905829, 12435281, 5992485,
                2074096, 13062653, 14148480, 10024647, 7455154, 6534752, 5933059, 9930860,
                8221561, 2639915, 10098755, 11468155, 8638604, 15770467, 7790866, 11694410,
                2868248, 5710862, 15709, 12374346, 5274287, 10913198, 9607083, 2330533,
                11262155, 2500209, 10878393, 11834918, 15572289, 15669880, 11713730, 8818293,
                15907500, 12427744, 13540318, 5978200, 13640927, 2411696, 16408949, 1331989,
                5941655, 3414928, 16619879, 6441500, 15706705, 9881210, 12620326, 12259629,
                6605901, 10543825, 9125515, 12001189, 8309409, 2696396, 3070853, 5120614,
                11830622, 10490623, 4149060, 7141756, 7297762, 12039919, 4930206, 16095035,
                10203610, 12162006, 10028034, 14040149, 1250372, 9943013, 11150309, 1752285,
                6641241, 532227, 2891993, 2146459, 4523080, 1843838, 1876388, 12071882,
                5253689, 266407, 14770216, 7346541, 9785383, 12662763, 4087061, 5312086,
                8667965, 5935475, 214509, 14935237, 12603345, 12069351, 13056011, 3177187,
                13886924, 9688141, 5714168, 5238287, 9839924, 6586077, 12908278, 3257673,
                7665358, 16208962, 12373914, 14796394, 11098653, 5975276, 14839805, 2522300,
                13055068, 4113411, 11984808, 1418893, 6924381, 11314606, 11633137, 13235995,
                8277143, 14057346, 5064134, 2097065, 13223254, 12242045, 13059400, 9799866,
                4430809, 11327935, 769464, 13938921, 11178479, 5438437, 1550013, 12839250,
                727385, 11354068, 3772220, 15394227, 9336140, 11988768, 860366, 14994301,
                15440057, 7845075, 46465, 9200550, 14833083, 6980643, 604527, 10080643,
                9045195, 4244693, 3844932, 12717539, 1960282, 12786813, 8615113, 6833198,
                5522541, 5791657, 15755268, 3994917, 158446, 12203633, 5002730, 10253904,
                1809471, 11479213, 9928035, 11072909, 9512807, 11660261, 16127120, 12596061,
                7086362, 15820414, 8387982, 14653636, 10912742, 1941253, 11740079, 15457795,
                3976572, 10595620, 7217556, 6197451, 7618490, 258825, 4780842, 5534349,
                2921202, 6513404, 16229803, 10332843, 3138363, 15681804, 10802604, 13113540,
                13757900, 5443555, 3681765, 5063855, 14185653, 14039650, 9644178, 5024611,
                8918836, 11231866, 13523137, 2425209, 8636320, 10944802, 3891863, 12961644,
                10984042, 9100512, 11218774, 11581954, 8646320, 11234763, 11887145, 4171898,
                5109569, 10742219, 4859349, 16381539, 10419813, 5223261, 8955000, 15061357,
                1607571, 7136846, 8670269, 11099382, 1451676, 4261185, 12586581, 15531576,
                2504976, 7105767, 6413040, 7144290, 16334106, 1741877, 16270583, 7852246,
                3119103, 10743199, 4558377, 7878745, 12289371, 3163084, 11735282, 1935864,
                5055074, 820851, 5185654, 14442671, 5212885, 2344949, 1892708, 1153520,
                9541794, 12306031, 14732248, 6743381, 5920025, 8969603, 8847687, 6622806,
                9462942, 12451035, 2336870, 327996, 9713350, 9963027, 11991923, 3562944,
                4520396, 7065629, 2905597, 12675100, 10094378, 5011336, 3908274, 3572588,
                15618069, 13341879, 9470980, 13327842, 8432408, 6344466, 12241360, 1543979,
                12081229, 11363884, 983612, 6025413, 1848571, 14318469, 14906334, 13381442,
                3327004, 15286722, 14443922, 9462145, 15828870, 16292084, 128023, 4199307,
                12797648, 6169450, 6767468, 8100201, 9756312, 10612295, 2273164, 3350998,
                15889011, 3661060, 9395406, 1435793, 5752767, 16441805, 16677778, 6475274,
                12909030, 15902971, 3415473, 9004053, 645884, 515610, 8824322, 16574526,
                15956463, 13265827, 6333169, 6924814, 1812983, 3392856, 14761092, 4985619,
                7893816, 13931135, 14548854, 11444226, 9118923, 1875105, 7285192, 2101733,
                7801836, 11517693, 2349183, 5939167, 11937456, 10886500, 13866155, 12947589,
                9640186, 5047153, 1901666, 715551, 13790692, 2933460, 11212664, 9563015,
                16642428, 16334427, 7140601, 4655671, 15711153, 750565, 15067249, 16737043,
                12684416, 15673315, 2341376, 8935324, 3134061, 10483409, 337177, 13018877,
                16599994, 7782302, 1977121, 10593779, 9842381, 14330646, 1456639, 3774065,
                12844377, 3016177, 8933870, 12263851, 10455534, 1612109, 16302350, 4895080,
                12932155, 1905228, 10253063, 4458040, 16024500, 15902756, 16584305, 12528008,
                4171461, 14536742, 9219403, 12927168, 1979395, 15257546, 10619265, 1967594,
                1467515, 2028519, 2032612, 3707709, 4887462, 2337860, 183801, 2152077,
                15066473, 3694942, 8424967, 15508266, 13386596, 6059869, 10531128, 13828874,
                7119662, 5064756, 12552069, 5922533, 802911, 5645620, 10781530, 11246590,
                9323418, 16275234, 2144845, 10962831, 4925357, 1704524, 9227431, 13641289,
                8489002, 1225340, 8659144, 8671408, 13461400, 4992933, 13370774, 8568931,
                2412794, 1312411, 12429994, 1025208, 478829, 11399895, 2242158, 2332498,
                10717459, 8151843, 5288043, 7235700, 9162569, 14017735, 10412273, 12712138,
                11844638, 11163643, 7756823, 9956164, 14078510, 8442139, 2116890, 10881649,
                16223710, 8592664, 15408035, 6522496, 1261635, 14685232, 5071601, 10144049,
                967751, 7873356, 5595700, 10647000, 15126220, 1237821, 321796, 6173902,
                14476409, 1830511, 12766190, 14322080, 8483740, 13438254, 1854079, 6215655,
                11575352, 15129118, 16393883, 16560142, 9079559, 11379168, 6198702, 11864074,
                2282933, 16547051, 7156233, 15740343, 4809601, 2344447, 10219155, 4977972,
                13592880, 184650, 16420038, 3165940, 9418081, 13446140, 179241, 9394692,
                6213074, 1752099, 3516715, 16081239, 13222615, 1499877, 9066661, 12702088,
                10706447, 7629231, 13016955, 1069166, 1089471, 6809842, 15634321, 1288782,
                1183469, 9576844, 14191973, 2814257, 4260748, 5239952, 4277681, 4629271,
                8220928, 8766876, 7388663, 13090704, 15838538, 11015909, 7814987, 14448125,
                13000849, 15596437, 2104764, 8398024, 15653431, 3695833, 6613072, 13626967,
                2665818, 9249819, 4040305, 8029033, 4822667, 3844052, 14708928, 690088
        };
        THashMap<Integer, Integer> map = new THashMap<Integer, Integer>();

        int[] to_remove = new int[]{
                9707851, 7941420, 9178562, 2165498, 4160722, 5570057, 1774305, 11340758,
                10326974, 8644308
        };

        for ( int i = 0; i < to_add.length; i++ ) {
            Integer obj = new Integer( to_add[i] );
            map.put( obj, obj );
        }

        for ( int i = 0; i < to_remove.length; i++ ) {
            Integer obj = new Integer( to_remove[i] );
            map.remove( obj );
        }
    }


    public void testIterable() {
        Map<String, Integer> m = new THashMap<String, Integer>();
        m.put( "One", Integer.valueOf( 1 ) );
        m.put( "Two", Integer.valueOf( 2 ) );

        for ( String s : m.keySet() ) {
            assertTrue( s.equals( "One" ) || s.equals( "Two" ) );
        }

        for ( Integer i : m.values() ) {
            assertTrue( i.intValue() == 1 || i.intValue() == 2 );
        }
    }


    public void testKeysFunctions() {
        int element_count = 10;
        String[] keys = new String[element_count];
        String[] vals = new String[element_count];
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = "Key" + i;
            vals[i] = "Vals" + i;
            ss_map.put( keys[i], vals[i] );
        }
        assertEquals( element_count, ss_map.size() );

        Collection<String> keys_set = ss_map.keySet();
        assertTrue( "should contain " + keys[5] + ", " + ss_map,
                keys_set.contains( keys[5] ) );

        assertFalse( "invalid remove succeeded " + keys_set,
                keys_set.remove( "non-existant" ) );
        assertTrue( "remove failed: " + keys_set, keys_set.remove( keys[5] ) );
        assertFalse( "key set contains removed item",
                keys_set.contains( keys[5] ) );
        assertFalse( "map contains removed item" + ss_map,
                ss_map.contains( keys[5] ) );
        assertFalse( "map contains removed item" + ss_map,
                ss_map.containsKey( keys[5] ) );

        assertFalse( "cannot remove item not in set",
                keys_set.remove( "non-existant" ) );
    }


    public void testValuesFunctions() {
        int element_count = 10;
        String[] vals = new String[element_count];
        for ( int i = 0; i < element_count; i++ ) {
            vals[i] = "Val" + i;
            ss_map.put( "Key" + i, vals[i] );
        }
        assertEquals( element_count, ss_map.size() );

        Collection<String> values_set = ss_map.values();
        assertTrue( "should contain " + vals[5] + ", " + ss_map,
                values_set.contains( vals[5] ) );

        Set<String> set = new HashSet<String>();
        for ( int i = 0; i < element_count; i++ ) {
            vals[i] = "Val" + i;
            set.add( vals[i] );
        }
        assertTrue( "should contain all: " + values_set + ", " + set,
                values_set.containsAll( set ) );
        set.add( "cause failure" );
        assertFalse( "shouldn't contain all: " + values_set,
                values_set.containsAll( set ) );

        values_set.clear();
        assertEquals( "values set size should be 0", 0, values_set.size() );
        assertEquals( "map size should be 0", 0, ss_map.size() );
        assertTrue( values_set.isEmpty() );

        try {
            values_set.add( "fail" );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }

        try {
            values_set.addAll( Arrays.asList( "fail" ) );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( UnsupportedOperationException ex ) {
            // Expected
        }
    }


    @SuppressWarnings({"ToArrayCallWithZeroLengthArrayArgument"})
    public void testValuesToArray() {
        int element_count = 10;
        String[] vals = new String[element_count];
        for ( int i = 0; i < element_count; i++ ) {
            vals[i] = "Val" + i;
            ss_map.put( "Key" + i, vals[i] );
        }

        Collection<String> values_set = ss_map.values();
        String[] toarray = values_set.toArray( new String[0] );
        Arrays.sort( toarray );
        assertEquals( Arrays.asList( vals ), Arrays.asList( toarray ) );

        toarray = values_set.toArray( new String[element_count] );
        Arrays.sort( toarray );
        assertEquals( Arrays.asList( vals ), Arrays.asList( toarray ) );

        toarray = values_set.toArray( new String[element_count + 1] );
        assertEquals( null, toarray[element_count] );
        Arrays.sort( toarray, 0, element_count );
        for ( int i = 0; i < element_count; i++ ) {
            assertEquals( vals[i], toarray[i] );
        }
    }


    public void testIteratorFunctions() throws Exception {
        int element_count = 10;
        String[] keys = new String[element_count];
        String[] vals = new String[element_count];
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = "Key" + i;
            vals[i] = "Val" + i;
            ss_map.put( keys[i], vals[i] );
        }

        Iterator<Map.Entry<String, String>> iter = ss_map.entrySet().iterator();
        while ( iter.hasNext() ) {
            Map.Entry<String, String> ee = iter.next();
            assertTrue( Arrays.asList( keys ).contains( ee.getKey() ) );
            assertTrue( Arrays.asList( vals ).contains( ee.getValue() ) );
            count++;
        }

        iter = ss_map.entrySet().iterator();
        assertTrue( iter.hasNext() );
        Map.Entry<String, String> ee = iter.next();
        assertTrue( "remove on entrySet() returned false",
                ss_map.entrySet().remove( ee ) );

        assertEquals( element_count - 1, ss_map.size() );
    }


    @SuppressWarnings({"unchecked"})
    public void testEntrySetFunctions() {
        int element_count = 10;
        String[] keys = new String[element_count];
        String[] vals = new String[element_count];
        for ( int i = 0; i < element_count; i++ ) {
            keys[i] = "Key" + i;
            vals[i] = "Val" + i;
            ss_map.put( keys[i], vals[i] );
        }

        Iterator<Map.Entry<String, String>> iter = ss_map.entrySet().iterator();
        assertTrue( iter.hasNext() );
        Map.Entry<String, String> ee = iter.next();

        assertNotNull( ee.getKey() );
        assertNotNull( ee.getValue() );
        assertNotNull( ee.getValue() );
        assertNotNull( ee.getValue() );

        String new_value = "New Value";
        String old_value = ee.getValue();
        assertEquals( old_value, ee.setValue( new_value ) );

        assertFalse( old_value.equals( ee.getValue() ) );
        assertFalse( ss_map.values().contains( old_value ) );
        assertTrue( ss_map.values().contains( new_value ) );
        assertTrue( ss_map.containsValue( new_value ) );

        assertFalse( "equal vs. random object incorrect", ee.equals( new Object() ) );

    }

//    public void testGetOverRemovedObjectBug() {
//        Map<BadHashInteger,String> m = new THashMap<BadHashInteger,String>(
//            new TObjectHashingStrategy<BadHashInteger>() {
//                public int computeHashCode(BadHashInteger object) {
//                    return object.hashCode();
//                }
//
//                public boolean equals(BadHashInteger o1, BadHashInteger o2) {
//                    return o1.value == o2.value;
//                }
//            } );
//
//
//        m.put( new BadHashInteger( 1 ), "one" );
//        m.put( new BadHashInteger( 2 ), "two" );
//
//        m.remove( new BadHashInteger( 1 ) );
//
//        assertEquals( 1, m.size() );
//
//        // Blow up here?
//        assertEquals( "two", m.get( new BadHashInteger( 2 ) ) );
//    }
//
//    public void testReaddBug() {
//        Map<Integer,String> m = new THashMap<Integer,String>(
//            new TObjectHashingStrategy<Integer>() {
//                public int computeHashCode(Integer object) {
//                    return object.intValue();
//                }
//
//                public boolean equals(Integer o1, Integer o2) {
//                    return o1.intValue() == o2.intValue();
//                }
//            } );
//
//        m.put( new Integer( 1 ), "one" );
//        assertEquals(1, m.size());
//
//        m.remove( new Integer( 1 ) );
//        assertEquals(0, m.size());
//
//        m.put( new Integer( 1 ), "one" );
//        assertEquals(1, m.size());
//    }


    public void testToString() {
        ss_map.put( "One", "1" );
        ss_map.put( "Two", "2" );

        String to_string = ss_map.toString();
        assertTrue( to_string,
			to_string.equals( "{One=1, Two=2}" ) || to_string.equals( "{Two=2, One=1}" ) );
    }


	public void testEntrySetToString() {
		Map<String, String> map = new THashMap<String, String>();
		map.put( "One", "1" );
		map.put( "Two", "2" );

		String to_string = map.entrySet().toString();
		assertTrue( to_string,
			to_string.equals( "{One=1, Two=2}" ) || to_string.equals( "{Two=2, One=1}" ) );
	}

	public void testKeySetToString() {
		Map<String, String> map = new THashMap<String, String>();
		map.put( "One", "1" );
		map.put( "Two", "2" );

		String to_string = map.keySet().toString();
		assertTrue( to_string,
			to_string.equals( "{One, Two}" ) || to_string.equals( "{Two, One}" ) );
	}

	public void testValuesToString() {
		Map<String, String> map = new THashMap<String, String>();
		map.put( "One", "1" );
		map.put( "Two", "2" );

		String to_string = map.values().toString();
		assertTrue( to_string,
			to_string.equals( "{1, 2}" ) || to_string.equals( "{2, 1}" ) );
	}
	


	/**
	 * Make sure that REMOVED entries are pruned when doing compaction.
	 */
	public void testRemovedSlotPruning() {
		THashMap<String,String> map = new THashMap<String,String>();
		map.put( "ONE", "1" );
		map.put( "TWO", "2" );
		map.put( "THREE", "3" );

		// Compact to make sure we're at the internal capacity we want to ultimate be at
		map.compact();

		// Make sure there are no REMOVED slots initially
		for( Object set_entry : map._set ) {
			if ( set_entry == TObjectHash.REMOVED ) fail( "Found a REMOVED entry" );
		}

		map.remove( "TWO" );
		map.put( "FOUR", "4" );

		// Make sure there is 1 REMOVED slot
		int count = 0;
		for( Object set_entry : map._set ) {
			if ( set_entry == TObjectHash.REMOVED ) count++;
		}
		assertEquals( 1, count );

		map.compact();

		// Make sure there are no REMOVED slots
		for( Object set_entry : map._set ) {
			if ( set_entry == TObjectHash.REMOVED ) {
				fail( "Found a REMOVED entry after compaction" );
			}
		}
	}


	/**
	 * Make sure that REMOVED entries are pruned when doing compaction.
	 */
	public void testFreeSlotCounterConsistency() {
		THashMap<String,String> map = new THashMap<String,String>();
		HashTestKit.checkFreeSlotCount( map, map._set, TObjectHash.FREE );

		map.put( "ONE", "1" );
		map.put( "TWO", "2" );
		map.put( "THREE", "3" );
		HashTestKit.checkFreeSlotCount( map, map._set, TObjectHash.FREE );

		// Compact to make sure we're at the internal capacity we want to ultimate be at
		map.compact();
		HashTestKit.checkFreeSlotCount( map, map._set, TObjectHash.FREE );


		map.remove( "TWO" );
		map.put( "FOUR", "4" );
		HashTestKit.checkFreeSlotCount( map, map._set, TObjectHash.FREE );

		map.compact();
		HashTestKit.checkFreeSlotCount( map, map._set, TObjectHash.FREE );
	}


	// Test for issue 3159432
	public void testEntrySetRemove() {
		THashMap<String,String> map = new THashMap<String,String>();
		map.put( "ONE", "1" );
		map.put( "TWO", "2" );
		map.put( "THREE", "3" );

		Set<Map.Entry<String,String>> set = map.entrySet();
		set.remove( null );

		//noinspection SuspiciousMethodCalls
		set.remove( "Blargh" );
	}


	public void testNullKeyHandling() {
		THashMap<String,String> map = new THashMap<String,String>();
		map.put( null, "My null key" );

		for( int i = 0; i < 100; i++ ) {
			map.put( String.valueOf( i ), String.valueOf( i ) );
		}

		assertEquals( "My null key", map.get( null ) );

		map.put( null, "My new null key" );

		map.compact();

		for( int i = 100; i < 200; i++ ) {
			map.put( String.valueOf( i ), String.valueOf( i ) );
		}

		assertEquals( "My new null key", map.get( null ) );
		for( int i = 0; i < 200; i++ ) {
			assertEquals( String.valueOf( i ), map.get( String.valueOf( i ) ) );
		}
	}


} // THashMapTests
