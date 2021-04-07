package org.evosuite.seeding;

import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.*;


public class TestPrioritization {

    @Test
    public void test(){
        Comparator<Integer> integerComparator = Comparator.comparingInt(x -> x % 10);
        Prioritization<Integer> pc = new Prioritization<>(integerComparator);
        pc.add(1, 1);
        pc.add(11,3);
        pc.add(21,2);
        pc.add(10,10);
        List<Integer> integers = pc.toSortedList();
        assertThat(integers,equalTo(Arrays.asList(10,1,21,11)));
    }
}