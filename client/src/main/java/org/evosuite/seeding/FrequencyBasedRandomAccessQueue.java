package org.evosuite.seeding;

import org.evosuite.Properties;
import org.evosuite.utils.RandomAccessQueue;
import org.evosuite.utils.Randomness;

import java.util.*;

/**
 * Created by gordon on 06/01/2017.
 */
public class FrequencyBasedRandomAccessQueue<T> implements RandomAccessQueue<T> {

    private final FrequencyBasedPool<T> values = new FrequencyBasedPool<T>();

    private final Queue<T> queue = new ArrayDeque<T>();

    /* (non-Javadoc)
     * @see org.evosuite.primitives.RandomAccessQueue#restrictedAdd(java.lang.Object)
     */
    @Override
    public void restrictedAdd(T value) {
        values.addConstant(value);
        queue.add(value);
        reduceSize();
    }

    private void reduceSize() {
        if (queue.size() > Properties.DYNAMIC_POOL_SIZE) {
            T value = queue.poll();
            values.removeConstant(value);
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.primitives.RandomAccessQueue#getRandomValue()
     */
    @Override
    public T getRandomValue() {
        return values.getRandomConstant();
    }

    @Override
    public String toString() {
        String res = new String("[ ");
        Iterator<T> itr = queue.iterator();
        while(itr.hasNext()) {
            Object element = itr.next();
            res += element + " ";
        }
        res += "]";
        return res;
    }

}
