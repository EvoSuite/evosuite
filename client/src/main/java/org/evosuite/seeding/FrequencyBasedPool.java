package org.evosuite.seeding;

import org.evosuite.utils.Randomness;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gordon on 06/01/2017.
 */
public class FrequencyBasedPool<T> {

    private Map<T, Integer> constants = Collections.synchronizedMap(new LinkedHashMap<T, Integer>());

    private int numConstants = 0;

    public void addConstant(T value) {
        numConstants++;
        if (!constants.containsKey(value))
            constants.put(value, 1);
        else
            constants.put(value, constants.get(value) + 1);
    }

    public void removeConstant(T value) {
        if (constants.containsKey(value)) {
            int num = constants.get(value);
            if (num <= 1)
                constants.remove(value);
            else
                constants.put(value, num - 1);
        }
    }

    public boolean hasConstant(T value) {
        return constants.containsKey(value);
    }


    public T getRandomConstant() {
        //special case
        if (numConstants == 0) {
            throw new IllegalArgumentException("Cannot select from empty pool");
        }

        double rnd = Randomness.nextDouble() * numConstants;

        for(Map.Entry<T, Integer> entry : constants.entrySet()) {
            int num = entry.getValue();
            if(num > rnd) {
                return entry.getKey();
            } else {
                rnd = rnd - num;
            }
        }

        // Shouldn't happen
        return Randomness.choice(constants.keySet());
    }

}
