package gnu.trove.impl.hash;

import junit.framework.TestCase;
import gnu.trove.set.hash.THashSet;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;



/**
 * tests that need access to internals of THash or THashSet
 */
public class THashTest extends TestCase {

    public THashTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
    }


    public void tearDown() throws Exception {
        super.tearDown();
    }


    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    public void testNormalLoad() throws Exception {
        THashSet<Integer> set = new THashSet<Integer>( 11, 0.5f );
        assertEquals( set._maxSize, 11 );
        for ( int i = 0; i < 12; i++ ) {
            set.add( new Integer( i ) );
        }
        assertTrue( set._maxSize > 12 );
    }


    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    public void testMaxLoad() throws Exception {
        THashSet<Integer> set = new THashSet<Integer>( 11, 1.0f );
        assertEquals( 10, set._maxSize );
        for ( int i = 0; i < 12; i++ ) {
            set.add( new Integer( i ) );
        }
        assertTrue( set._maxSize > 12 );
    }




    public void testReusesRemovedSlotsOnCollision() {
        THashSet<Object> set = new THashSet<Object>( 11, 0.5f );

        class Foo {

            public int hashCode() {
                return 4;
            }
        }

        Foo f1 = new Foo();
        Foo f2 = new Foo();
        Foo f3 = new Foo();
        set.add( f1 );

        int idx = set.insertionIndex( f2 );
        set.add( f2 );
        assertEquals( f2, set._set[idx] );
        set.remove( f2 );
        assertEquals( THashSet.REMOVED, set._set[idx] );
        assertEquals( idx, set.insertionIndex( f3 ) );
        set.add( f3 );
        assertEquals( f3, set._set[idx] );
    }


    public void testCompact() throws Exception {
        THashMap<Integer,Integer> map = new THashMap<Integer,Integer>();
        
        Integer[] data = new Integer[1000];

        for (int i = 0; i < 1000; i++) {
            data[i] = new Integer(i);
            map.put(data[i], data[i]);
        }
        assertTrue(map._maxSize > 1000);
        for (int i = 0; i < 1000; i+=2) {
//            try {
            map.remove(data[i]);
//            }
//            catch( RuntimeException ex ) {
//                System.err.println("Error on i: " + i);
//                System.out.println("Hash codes:");
//                for( int j = 0 ; j < data.length; j++ ) {
//                    if ( ( j % 8 ) == 0 ) {
//                        System.out.println(",");
//                    }
//                    else System.out.print(",");
//                    System.out.print(map._hashingStrategy.computeHashCode(data[j]));
//                }
//
//
//                System.out.println("Remove:");
//                for( int j = 0 ; j <= i; j+=2 ) {
//                    if ( ( j % 8 ) == 0 ) {
//                        System.out.println(",");
//                    }
//                    else System.out.print(",");
//                    System.out.print(map._hashingStrategy.computeHashCode(data[j]));
//                }
//                throw ex;
//            }
        }
        assertEquals(500, map.size());
        map.compact();
        assertEquals(500, map.size());
        assertTrue(map._maxSize < 1000);
    }


    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    public void testTPHashMapConstructors() {

        int cap = 20;

        THashMap cap_and_factor = new THashMap( cap, 0.75f );
        assertTrue( "capacity not sufficient: " + cap + ", " + cap_and_factor.capacity(),
                cap <= cap_and_factor.capacity() );
        assertEquals( 0.75f, cap_and_factor._loadFactor );
    }


    public void testTPrimitivePrimitveHashMapConstructors() {

        int cap = 20;

        TIntLongMap cap_and_factor = new TIntLongHashMap( cap, 0.75f );
        TPrimitiveHash cap_and_factor_hash = (TPrimitiveHash) cap_and_factor;
        assertTrue( "capacity not sufficient: " + cap + ", " + cap_and_factor_hash.capacity(),
                cap <= cap_and_factor_hash.capacity() );
        assertEquals( 0.75f, cap_and_factor_hash._loadFactor );

        TIntLongMap fully_specified =
                new TIntLongHashMap( cap, 0.5f, Integer.MIN_VALUE, Long.MIN_VALUE );
        TPrimitiveHash fully_specified_hash = (TPrimitiveHash) fully_specified;
        assertTrue( "capacity not sufficient: " + cap + ", " + fully_specified_hash.capacity(),
                cap <= fully_specified_hash.capacity() );
        assertEquals( 0.5f, fully_specified_hash._loadFactor );
        assertEquals( Integer.MIN_VALUE, fully_specified.getNoEntryKey() );
        assertEquals( Long.MIN_VALUE, fully_specified.getNoEntryValue() );
    }


    // test all the way up the chain to THash
    public void testTPrimitivePrimitveHashMapSerialize() throws Exception {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        long[] vals = new long[keys.length];

        TIntLongMap original_map =
                new TIntLongHashMap( 200, 0.75f, Integer.MIN_VALUE, Long.MIN_VALUE );
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = keys[i] * 2;
            original_map.put( keys[i], vals[i] );
        }

        THash original_hash = ( THash ) original_map;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( original_map );

        ByteArrayInputStream bias = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bias );

        TIntLongMap deserialized_map = ( TIntLongMap ) ois.readObject();
        THash deserialized_hash = ( THash ) deserialized_map;

        assertEquals( original_map, deserialized_map );
        assertEquals( original_map.getNoEntryKey(), deserialized_map.getNoEntryKey() );
        assertEquals( original_map.getNoEntryValue(), deserialized_map.getNoEntryValue() );
        assertEquals( original_hash._loadFactor, deserialized_hash._loadFactor );
    }


    // test all the way up the chain to THash
    public void testTPrimitiveObjectHashMapSerialize() throws Exception {
        int[] keys = {1138, 42, 86, 99, 101, 727, 117};
        String[] vals = new String[keys.length];

        TIntObjectMap<String> original_map =
                new TIntObjectHashMap<String>( 200, 0.75f, Integer.MIN_VALUE );
        for ( int i = 0; i < keys.length; i++ ) {
            vals[i] = String.valueOf( keys[i] * 2 );
            original_map.put( keys[i], vals[i] );
        }

        THash original_hash = ( THash ) original_map;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( original_map );

        ByteArrayInputStream bias = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bias );

        TIntObjectMap deserialized_map = ( TIntObjectMap ) ois.readObject();
        THash deserialized_hash = ( THash ) deserialized_map;

        assertEquals( original_map, deserialized_map );
        assertEquals( original_map.getNoEntryKey(), deserialized_map.getNoEntryKey() );
        assertEquals( original_hash._loadFactor, deserialized_hash._loadFactor );
    }


    // test all the way up the chain to THash
     public void testTObjectPrimitiveHashMapSerialize() throws Exception {
        int[] vals = {1138, 42, 86, 99, 101, 727, 117};
        String[] keys = new String[vals.length];


        TObjectIntMap<String> original_map =
                new TObjectIntHashMap<String>( 200, 0.75f, Integer.MIN_VALUE );
        for ( int i = 0; i < keys.length; i++ ) {
            keys[i] = String.valueOf( vals[i] * 2 );
            original_map.put( keys[i], vals[i] );
        }

        THash original_hash = ( THash ) original_map;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( original_map );

        ByteArrayInputStream bias = new ByteArrayInputStream( baos.toByteArray() );
        ObjectInputStream ois = new ObjectInputStream( bias );

        TObjectIntMap deserialized_map = ( TObjectIntMap ) ois.readObject();
        THash deserialized_hash = ( THash ) deserialized_map;

        assertEquals( original_map, deserialized_map );
        assertEquals( original_map.getNoEntryValue(), deserialized_map.getNoEntryValue() );
        assertEquals( original_hash._loadFactor, deserialized_hash._loadFactor );
    }

}
