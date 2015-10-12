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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.TDecorators;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.*;


/**
 * Test the primitive key/Object value map decorators
 *
 * @author Eric D. Friedman
 * @author Robert D. Eden
 * @author Jeff Randall
 */
public class TPrimitiveObjectMapDecoratorTest extends TestCase {

    public TPrimitiveObjectMapDecoratorTest(String name) {
        super(name);
    }


    public void testConstructors() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);

        TIntObjectMap<String> raw_capacity =
                new TIntObjectHashMap<String>(20);
        for (int i = 0; i < element_count; i++) {
            raw_capacity.put(keys[i], vals[i]);
        }
        Map<Integer, String> capacity = TDecorators.wrap(raw_capacity);
        assertEquals(map, capacity);

        TIntObjectMap<String> raw_cap_and_factor =
                new TIntObjectHashMap<String>(20, 0.75f);
        for (int i = 0; i < element_count; i++) {
            raw_cap_and_factor.put(keys[i], vals[i]);
        }
        Map<Integer, String> cap_and_factor = TDecorators.wrap(raw_cap_and_factor);
        assertEquals(map, cap_and_factor);

        TIntObjectMap<String> raw_fully_specified =
                new TIntObjectHashMap<String>(20, 0.75f, Integer.MIN_VALUE);
        for (int i = 0; i < element_count; i++) {
            raw_fully_specified.put(keys[i], vals[i]);
        }
        Map<Integer, String> fully_specified = TDecorators.wrap(raw_fully_specified);
        assertEquals(map, fully_specified);

        TIntObjectMap<String> raw_copy =
                new TIntObjectHashMap<String>(raw_map);
        Map<Integer, String> copy = TDecorators.wrap(raw_copy);
        assertEquals(map, copy);
    }


    public void testGet() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);

        assertEquals(vals[10], map.get(Integer.valueOf(keys[10])));
        assertNull(map.get(Integer.valueOf(1138)));

        Integer key = Integer.valueOf(1138);
        map.put(key, null);
        assertTrue(map.containsKey(key));
        assertNull(map.get(key));

        Long long_key = Long.valueOf(1138);
        //noinspection SuspiciousMethodCalls
        assertNull(map.get(long_key));

        map.put(null, "null-key");
        assertEquals("null-key", map.get(null));
    }


    public void testContainsKey() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);

        for (int i = 0; i < element_count; i++) {
            assertTrue("Key should be present: " + keys[i] + ", map: " + map,
                    map.containsKey(keys[i]));
        }

        int key = 1138;
        assertFalse("Key should not be present: " + key + ", map: " + map,
                map.containsKey(key));
    }


    public void testContainsValue() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);

        for (int i = 0; i < element_count; i++) {
            assertTrue("Value should be present: " + vals[i] + ", map: " + map,
                    map.containsValue(vals[i]));
        }

        String val = "1138";
        assertFalse("Key should not be present: " + val + ", map: " + map,
                map.containsValue(val));

        //noinspection SuspiciousMethodCalls
        assertFalse("Random object should not be present in map: " + map,
                map.containsValue(new Object()));
    }


    public void testRemove() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);

        for (int i = 0; i < element_count; i++) {
            if (i % 2 == 1) {
                assertEquals("Remove should have modified map: " + keys[i] + ", map: " + map,
                        vals[i], map.remove(keys[i]));
            }
        }

        for (int i = 0; i < element_count; i++) {
            if (i % 2 == 1) {
                assertTrue("Removed key still in map: " + keys[i] + ", map: " + map,
                        map.get(keys[i]) == null);
            } else {
                assertTrue("Key should still be in map: " + keys[i] + ", map: " + map,
                        map.get(keys[i]).equals(vals[i]));
            }
        }

        assertNull(map.get(1138));
        //noinspection SuspiciousMethodCalls
        assertNull(map.get(Integer.valueOf(1138)));
        assertNull(map.get(null));

        map.put(null, "null-value");
        assertEquals("null-value", raw_map.get(raw_map.getNoEntryKey()));
        assertTrue(map.containsKey(null));
        String value = map.get(null);
        assertEquals("value: " + value, "null-value", value);
        assertEquals("null-value", map.remove(null));
        assertFalse(map.containsKey(null));

        //noinspection SuspiciousMethodCalls
        assertNull(map.remove(Long.valueOf(1138)));
    }


    public void testPutAllMap() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_control = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_control.put(keys[i], vals[i]);
        }
        Map<Integer, String> control = TDecorators.wrap(raw_control);

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        Map<Integer, String> map = TDecorators.wrap(raw_map);

        Map<Integer, String> source = new HashMap<Integer, String>();
        for (int i = 0; i < element_count; i++) {
            source.put(keys[i], vals[i]);
        }

        map.putAll(source);
        assertEquals(control, map);
    }


    public void testPutAll() throws Exception {
        TIntObjectMap<String> raw_t = new TIntObjectHashMap<String>();
        Map<Integer, String> t = TDecorators.wrap(raw_t);
        TIntObjectMap<String> raw_m = new TIntObjectHashMap<String>();
        Map<Integer, String> m = TDecorators.wrap(raw_m);
        m.put(2, "one");
        m.put(4, "two");
        m.put(6, "three");

        t.put(5, "four");
        assertEquals(1, t.size());

        t.putAll(m);
        assertEquals(4, t.size());
        assertEquals("two", t.get(4));
    }


    public void testClear() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);
        assertEquals(element_count, map.size());

        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());

        assertNull(map.get(keys[5]));
    }


    @SuppressWarnings({"ToArrayCallWithZeroLengthArrayArgument"})
    public void testKeySet() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);
        assertEquals(element_count, map.size());

        Set<Integer> keyset = map.keySet();
        for (int i = 0; i < keyset.size(); i++) {
            assertTrue(keyset.contains(keys[i]));
        }
        assertFalse(keyset.isEmpty());

        Object[] keys_object_array = keyset.toArray();
        int count = 0;
        Iterator<Integer> iter = keyset.iterator();
        while (iter.hasNext()) {
            int key = iter.next();
            assertTrue(keyset.contains(key));
            assertEquals(keys_object_array[count], key);
            count++;
        }

        Integer[] keys_array = keyset.toArray(new Integer[0]);
        count = 0;
        iter = keyset.iterator();
        while (iter.hasNext()) {
            Integer key = iter.next();
            assertTrue(keyset.contains(key));
            assertEquals(keys_array[count], key);
            count++;
        }

        keys_array = keyset.toArray(new Integer[keyset.size()]);
        count = 0;
        iter = keyset.iterator();
        while (iter.hasNext()) {
            Integer key = iter.next();
            assertTrue(keyset.contains(key));
            assertEquals(keys_array[count], key);
            count++;
        }

        keys_array = keyset.toArray(new Integer[keyset.size() * 2]);
        count = 0;
        iter = keyset.iterator();
        while (iter.hasNext()) {
            Integer key = iter.next();
            assertTrue(keyset.contains(key));
            assertEquals(keys_array[count], key);
            count++;
        }
        assertNull(keys_array[keyset.size()]);

        TIntSet raw_other = new TIntHashSet(keyset);
        Set<Integer> other = TDecorators.wrap(raw_other);
        assertFalse(keyset.retainAll(other));
        other.remove(keys[5]);
        assertTrue(keyset.retainAll(other));
        assertFalse(keyset.contains(keys[5]));
        assertFalse(map.containsKey(keys[5]));

        keyset.clear();
        assertTrue(keyset.isEmpty());


    }


    public void testKeySetAdds() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);
        assertEquals(element_count, map.size());

        Set<Integer> keyset = map.keySet();
        for (int i = 0; i < keyset.size(); i++) {
            assertTrue(keyset.contains(keys[i]));
        }
        assertFalse(keyset.isEmpty());

        try {
            keyset.add(1138);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }

        try {
            Set<Integer> test = new HashSet<Integer>();
            test.add(Integer.valueOf(1138));
            keyset.addAll(test);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }

        try {
            Set<Integer> test = new HashSet<Integer>();
            test.add(1138);
            keyset.addAll(test);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }
    }


    @SuppressWarnings({"ToArrayCallWithZeroLengthArrayArgument"})
    public void testKeys() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);
        assertEquals(element_count, map.size());

        // No argument
        Integer[] keys_array = map.keySet().toArray(new Integer[map.size()]);
        assertEquals(element_count, keys_array.length);
        List<Integer> keys_list = Arrays.asList(keys_array);
        for (int i = 0; i < element_count; i++) {
            assertTrue(keys_list.contains(keys[i]));
        }

        // Zero length array
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        keys_array = map.keySet().toArray(new Integer[0]);
        assertEquals(element_count, keys_array.length);
        keys_list = Arrays.asList(keys_array);
        for (int i = 0; i < element_count; i++) {
            assertTrue(keys_list.contains(keys[i]));
        }

        // appropriate length array
        keys_array = map.keySet().toArray(new Integer[map.size()]);
        assertEquals(element_count, keys_array.length);
        keys_list = Arrays.asList(keys_array);
        for (int i = 0; i < element_count; i++) {
            assertTrue(keys_list.contains(keys[i]));
        }

        // longer array
        keys_array = map.keySet().toArray(new Integer[element_count * 2]);
        assertEquals(element_count * 2, keys_array.length);
        keys_list = Arrays.asList(keys_array);
        for (int i = 0; i < element_count; i++) {
            assertTrue(keys_list.contains(keys[i]));
        }
        assertNull(keys_array[element_count]);
    }


    public void testValueCollectionToArray() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);
        assertEquals(element_count, map.size());

        Collection<String> collection = map.values();
        for (int i = 0; i < collection.size(); i++) {
            assertTrue(collection.contains(vals[i]));
        }
        assertFalse(collection.isEmpty());

        Object[] values_obj_array = collection.toArray();
        int count = 0;
        Iterator<String> iter = collection.iterator();
        while (iter.hasNext()) {
            String value = iter.next();
            assertTrue(collection.contains(value));
            assertEquals(values_obj_array[count], value);
            count++;
        }

        //noinspection ToArrayCallWithZeroLengthArrayArgument
        String[] values_array = collection.toArray(new String[0]);
        count = 0;
        iter = collection.iterator();
        while (iter.hasNext()) {
            String value = iter.next();
            assertTrue(collection.contains(value));
            assertEquals(values_array[count], value);
            count++;
        }

        values_array = collection.toArray(new String[collection.size()]);
        count = 0;
        iter = collection.iterator();
        while (iter.hasNext()) {
            String value = iter.next();
            assertTrue(collection.contains(value));
            assertEquals(values_array[count], value);
            count++;
        }

        values_array = collection.toArray(new String[collection.size() * 2]);
        count = 0;
        iter = collection.iterator();
        while (iter.hasNext()) {
            String value = iter.next();
            assertTrue(collection.contains(value));
            assertEquals(values_array[count], value);
            count++;
        }
        assertNull(values_array[collection.size()]);
        assertNull(values_array[collection.size()]);

        Collection<String> other = new ArrayList<String>(collection);
        assertFalse(collection.retainAll(other));
        other.remove(vals[5]);
        assertTrue(collection.retainAll(other));
        assertFalse(collection.contains(vals[5]));
        assertFalse(map.containsKey(keys[5]));

        collection.clear();
        assertTrue(collection.isEmpty());
    }


    public void testValueCollectionAdds() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);
        assertEquals(element_count, map.size());

        Collection<String> collection = map.values();
        for (int i = 0; i < collection.size(); i++) {
            assertTrue(collection.contains(vals[i]));
        }
        assertFalse(collection.isEmpty());

        try {
            collection.add("1138");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }

        try {
            Set<String> test = new HashSet<String>();
            test.add("1138");
            collection.addAll(test);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }

        try {
            Collection<String> test = new ArrayList<String>();
            test.add("1138");
            collection.addAll(test);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }

        try {
            collection.addAll(Arrays.asList(vals));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }
    }


    public void testValueCollectionContainsAll() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);
        assertEquals(element_count, map.size());

        Collection<String> collection = map.values();
        for (int i = 0; i < collection.size(); i++) {
            assertTrue(collection.contains(vals[i]));
        }
        assertFalse(collection.isEmpty());

        List<String> java_list = new ArrayList<String>();
        java_list.addAll(Arrays.asList(vals));
        assertTrue("collection: " + collection + ", should contain all in list: " +
                java_list, collection.containsAll(java_list));
        java_list.add(String.valueOf(1138));
        assertFalse("collection: " + collection + ", should not contain all in list: " +
                java_list, collection.containsAll(java_list));

        List<CharSequence> number_list = new ArrayList<CharSequence>();
        for (String value : vals) {
            if (value.equals("5")) {
                number_list.add(new StringBuilder().append(value));
            } else {
                number_list.add(String.valueOf(value));
            }
        }
        assertFalse("collection: " + collection + ", should not contain all in list: " +
                java_list, collection.containsAll(number_list));

        Collection<String> other = new ArrayList<String>(collection);
        assertTrue("collection: " + collection + ", should contain all in other: " +
                other, collection.containsAll(other));
        other.add("1138");
        assertFalse("collection: " + collection + ", should not contain all in other: " +
                other, collection.containsAll(other));
    }


    public void testValueCollectionRetainAllCollection() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);
        assertEquals(element_count, map.size());

        Collection<String> collection = map.values();
        for (int i = 0; i < collection.size(); i++) {
            assertTrue(collection.contains(vals[i]));
        }
        assertFalse(collection.isEmpty());

        List<String> java_list = new ArrayList<String>();
        java_list.addAll(Arrays.asList(vals));
        assertFalse("collection: " + collection + ", should contain all in list: " +
                java_list, collection.retainAll(java_list));

        java_list.remove(5);
        assertTrue("collection: " + collection + ", should contain all in list: " +
                java_list, collection.retainAll(java_list));
        assertFalse(collection.contains(vals[5]));
        assertFalse(map.containsKey(keys[5]));
        assertFalse(map.containsValue(vals[5]));
        assertTrue("collection: " + collection + ", should contain all in list: " +
                java_list, collection.containsAll(java_list));
    }


    public void testValueCollectionRetainAllTCollection() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);
        assertEquals(element_count, map.size());

        Collection<String> collection = map.values();
        for (int i = 0; i < collection.size(); i++) {
            assertTrue(collection.contains(vals[i]));
        }
        assertFalse(collection.isEmpty());

        assertFalse("collection: " + collection + ", should be unmodified.",
                collection.retainAll(collection));

        Collection<String> other = new ArrayList<String>(collection);
        assertFalse("collection: " + collection + ", should be unmodified. other: " +
                other, collection.retainAll(other));

        other.remove(vals[5]);
        assertTrue("collection: " + collection + ", should be modified. other: " +
                other, collection.retainAll(other));
        assertFalse(collection.contains(vals[5]));
        assertFalse(map.containsKey(keys[5]));
        assertFalse(map.containsValue(vals[5]));
        assertTrue("collection: " + collection + ", should contain all in other: " +
                other, collection.containsAll(other));
    }


    public void testValueCollectionRemoveAllCollection() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);
        assertEquals(element_count, map.size());

        Collection<String> collection = map.values();
        for (int i = 0; i < collection.size(); i++) {
            assertTrue(collection.contains(vals[i]));
        }
        assertFalse(collection.isEmpty());

        List<String> java_list = new ArrayList<String>();
        assertFalse("collection: " + collection + ", should contain all in list: " +
                java_list, collection.removeAll(java_list));

        java_list.add(vals[5]);
        assertTrue("collection: " + collection + ", should contain all in list: " +
                java_list, collection.removeAll(java_list));
        assertFalse(collection.contains(vals[5]));
        assertFalse(map.containsKey(keys[5]));
        assertFalse(map.containsValue(vals[5]));

        java_list = new ArrayList<String>();
        java_list.addAll(Arrays.asList(vals));
        assertTrue("collection: " + collection + ", should contain all in list: " +
                java_list, collection.removeAll(java_list));
        assertTrue(collection.isEmpty());
    }


    public void testValueCollectionRemoveAllTCollection() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);
        assertEquals(element_count, map.size());

        Collection<String> collection = map.values();
        for (int i = 0; i < collection.size(); i++) {
            assertTrue(collection.contains(vals[i]));
        }
        assertFalse(collection.isEmpty());

        Collection<String> other = new ArrayList<String>();
        assertFalse("collection: " + collection + ", should be unmodified.",
                collection.removeAll(other));

        other = new ArrayList<String>(collection);
        other.remove(vals[5]);
        assertTrue("collection: " + collection + ", should be modified. other: " +
                other, collection.removeAll(other));
        assertEquals(1, collection.size());
        for (int i = 0; i < element_count; i++) {
            if (i == 5) {
                assertTrue(collection.contains(vals[i]));
                assertTrue(map.containsKey(keys[i]));
                assertTrue(map.containsValue(vals[i]));
            } else {
                assertFalse(collection.contains(vals[i]));
                assertFalse(map.containsKey(keys[i]));
                assertFalse(map.containsValue(vals[i]));
            }
        }

        assertFalse("collection: " + collection + ", should be unmodified. other: " +
                other, collection.removeAll(other));

        assertTrue("collection: " + collection + ", should be modified. other: " +
                other, collection.removeAll(collection));
        assertTrue(collection.isEmpty());
    }


    public void testValues() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);
        assertEquals(element_count, map.size());

        // No argument
        Object[] values_object_array = map.values().toArray();
        assertEquals(element_count, values_object_array.length);
        List<Object> values_object_list = Arrays.asList(values_object_array);
        for (int i = 0; i < element_count; i++) {
            assertTrue(values_object_list.contains(vals[i]));
        }

        // Zero length array
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        String[] values_array = map.values().toArray(new String[0]);
        assertEquals(element_count, values_array.length);
        List<String> values_list = Arrays.asList(values_array);
        for (int i = 0; i < element_count; i++) {
            assertTrue(values_list.contains(vals[i]));
        }

        // appropriate length array
        values_array = map.values().toArray(new String[map.size()]);
        assertEquals(element_count, values_array.length);
        values_list = Arrays.asList(values_array);
        for (int i = 0; i < element_count; i++) {
            assertTrue(values_list.contains(vals[i]));
        }

        // longer array
        values_array = map.values().toArray(new String[element_count * 2]);
        assertEquals(element_count * 2, values_array.length);
        values_list = Arrays.asList(values_array);
        for (int i = 0; i < element_count; i++) {
            assertTrue(values_list.contains(vals[i]));
        }
        assertEquals(null, values_array[element_count]);
    }


    public void testEntrySet() {
        int element_count = 20;
        Integer[] keys = new Integer[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map =
                new TIntObjectHashMap<String>(element_count, 0.5f, Integer.MIN_VALUE);
        Map<Integer, String> map = TDecorators.wrap(raw_map);

        for (int i = 0; i < element_count; i++) {
            keys[i] = Integer.valueOf(i + 1);
            vals[i] = Integer.toString(i + 1);
            map.put(keys[i], vals[i]);
        }
        assertEquals(element_count, map.size());

        Set<Map.Entry<Integer, String>> entries = map.entrySet();
        assertEquals(element_count, entries.size());
        assertFalse(entries.isEmpty());
        //noinspection unchecked
        Map.Entry<Integer, String>[] array =
                entries.toArray(new Map.Entry[entries.size()]);
        for (Map.Entry<Integer, String> entry : array) {
            assertTrue(entries.contains(entry));
        }
        assertFalse(entries.contains(null));

        assertEquals(array[0].hashCode(), array[0].hashCode());
        assertTrue(array[0].hashCode() != array[1].hashCode());

        assertTrue(array[0].equals(array[0]));
        assertFalse(array[0].equals(array[1]));
        Integer key = array[0].getKey();
        Integer old_value = Integer.valueOf(array[0].getValue());
        assertEquals(Integer.toString(old_value),
                array[0].setValue(Integer.toString(old_value * 2)));
        assertEquals(Integer.toString(old_value * 2), map.get(key));
        assertEquals(Integer.toString(old_value * 2), array[0].getValue());

        // Adds are not allowed
        Map.Entry<Integer, String> invalid_entry = new Map.Entry<Integer, String>() {
            public Integer getKey() {
                return null;
            }

            public String getValue() {
                return null;
            }

            public String setValue(String value) {
                return null;
            }
        };
        List<Map.Entry<Integer, String>> invalid_entry_list =
                new ArrayList<Map.Entry<Integer, String>>();
        invalid_entry_list.add(invalid_entry);

        try {
            entries.add(invalid_entry);
            fail("Expected OperationUnsupportedException");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }

        try {
            entries.addAll(invalid_entry_list);
            fail("Expected OperationUnsupportedException");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }

        assertFalse(entries.containsAll(invalid_entry_list));
        assertFalse(entries.removeAll(invalid_entry_list));

        List<Map.Entry<Integer, String>> partial_list =
                new ArrayList<Map.Entry<Integer, String>>();
        partial_list.add(array[3]);
        partial_list.add(array[4]);
        assertTrue(entries.removeAll(partial_list));
        assertEquals(element_count - 2, entries.size());
        assertEquals(element_count - 2, map.size());

        entries.clear();
        assertTrue(entries.isEmpty());
        assertTrue(map.isEmpty());
    }


    public void testEquals() {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map =
                new TIntObjectHashMap<String>(element_count, 0.5f, Integer.MIN_VALUE);
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);
        assertEquals(element_count, map.size());

        TIntObjectHashMap<String> raw_fully_specified =
                new TIntObjectHashMap<String>(20, 0.75f, Integer.MIN_VALUE);
        for (int i = 0; i < element_count; i++) {
            raw_fully_specified.put(keys[i], vals[i]);
        }
        Map<Integer, String> fully_specified = TDecorators.wrap(raw_fully_specified);
        assertEquals(map, fully_specified);

        assertFalse("shouldn't equal random object", map.equals(new Object()));

        assertSame(raw_map, ((TIntObjectMapDecorator) map).getMap());
    }


    @SuppressWarnings({"unchecked"})
    public void testSerialize() throws Exception {
        int element_count = 20;
        int[] keys = new int[element_count];
        String[] vals = new String[element_count];

        TIntObjectMap<String> raw_map = new TIntObjectHashMap<String>();
        for (int i = 0; i < element_count; i++) {
            keys[i] = i + 1;
            vals[i] = Integer.toString(i + 1);
            raw_map.put(keys[i], vals[i]);
        }
        Map<Integer, String> map = TDecorators.wrap(raw_map);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);

        ByteArrayInputStream bias = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bias);

        Map<Integer, String> deserialized = (Map<Integer, String>) ois.readObject();

        assertEquals(map, deserialized);
    }


    public void testToString() {
        TIntObjectHashMap<String> raw_map = new TIntObjectHashMap<String>();
        Map<Integer, String> map = TDecorators.wrap(raw_map);
        map.put(11, "One");
        map.put(22, "Two");

        String to_string = map.toString();
        assertTrue(to_string, to_string.equals("{11=One, 22=Two}")
                || to_string.equals("{22=Two, 11=One}"));
    }

    public void testBug3432175() throws Exception {
        Map<Integer, Object> trove = new TIntObjectMapDecorator<Object>(new TIntObjectHashMap<Object>());
        trove.put(null, new Object());
        assertFalse(trove.isEmpty());
        assertEquals(1, trove.size());
        assertEquals(1, trove.entrySet().size());
        assertEquals(1, trove.keySet().size());
        assertEquals(null, trove.keySet().iterator().next());
    }
}
