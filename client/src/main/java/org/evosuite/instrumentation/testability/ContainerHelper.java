package org.evosuite.instrumentation.testability;

import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.setup.TestCluster;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Andrea Arcuri on 26/03/15.
 */
public class ContainerHelper {

    /**
     * Helper function that is called instead of Map.isEmpty
     *
     * @param m
     *            a {@link java.util.Map} object.
     * @return a int.
     */
    public static int mapIsEmpty(Map<?, ?> m) {
        return m.isEmpty() ? BooleanHelper.TRUE : -m.size();
    }

    /**
     * Helper function that is called instead of Collection.isEmpty
     *
     * @param c
     *            a {@link java.util.Collection} object.
     * @return a int.
     */
    public static int collectionIsEmpty(Collection<?> c) {
        return c.isEmpty() ? BooleanHelper.TRUE : -c.size();
    }

    /**
     * Helper function that is called instead of Collection.contains
     *
     * @param c
     *            a {@link java.util.Collection} object.
     * @param o1
     *            a {@link java.lang.Object} object.
     * @return a int.
     */
    public static int collectionContains(Collection<?> c, Object o1) {
        if(o1 != null) {
            TestCluster.getInstance().addCastClassForContainer(o1.getClass());
        }
        int matching = 0;
        double min_distance = Double.MAX_VALUE;
        for (Object o2 : c) {
            if (o2.equals(o1))
                matching++;
            else {
                if (o2 != null && o1 != null) {
                    if (o2.getClass().equals(o1.getClass())) {
                        if (o1 instanceof Number) {
                            Number n1 = (Number) o1;
                            Number n2 = (Number) o2;
                            min_distance = Math.min(min_distance,
                                    Math.abs(n1.doubleValue()
                                            - n2.doubleValue()));
                        } else if (o2 instanceof String) {
                            ConstantPoolManager.getInstance().addDynamicConstant(o1);
                            min_distance = Math.min(min_distance,
                                    StringHelper.editDistance((String) o1, (String) o2));
                        }
                    }
                }
            }
        }
        if (matching > 0)
            return matching;
        else {
            if (min_distance == Double.MAX_VALUE)
                return -c.size() - 1;
            else {
                return -1 * (int) Math.ceil(BooleanHelper.K * min_distance / (min_distance + 1.0));
            }

        }
    }

    /**
     * Helper function that is called instead of Collection.containsAll
     *
     * @param c
     *            a {@link java.util.Collection} object.
     * @param c2
     *            a {@link java.util.Collection} object.
     * @return a int.
     */
    public static int collectionContainsAll(Collection<?> c, Collection<?> c2) {
        int mismatch = 0;
        for (Object o : c2) {
            if (!c.contains(o))
                mismatch++;
        }
        return mismatch > 0 ? -mismatch : c2.size() +1;
    }

    /**
     * Helper function that is called instead of Map.containsKey
     *
     * @param o1
     *            a {@link java.lang.Object} object.
     * @param m
     *            a {@link java.util.Map} object.
     * @return a int.
     */
    public static int mapContainsKey(Map<?, ?> m, Object o1) {
        if(o1 != null)
            TestCluster.getInstance().addCastClassForContainer(o1.getClass());

        return collectionContains(m.keySet(), o1);
    }

    /**
     * Helper function that is called instead of Map.containsValue
     *
     * @param o1
     *            a {@link java.lang.Object} object.
     * @param m
     *            a {@link java.util.Map} object.
     * @return a int.
     */
    public static int mapContainsValue(Map<?, ?> m, Object o1) {
        if(o1 != null)
            TestCluster.getInstance().addCastClassForContainer(o1.getClass());

        return collectionContains(m.values(), o1);
    }

}
