package org.evosuite.seeding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Data structure to store a sorted set of {@param T} with a secondary sorting criteria "priority".
 * The priority must be always defined when adding an element.
 *
 * @param <T> The class to be stored with priorities.
 */
public class Prioritization<T> {
    private static final Logger logger = LoggerFactory.getLogger(Prioritization.class);

    // The actual elements
    private final TreeSet<T> elements;
    // The priorities of the elements.
    private final Map<T, Integer> priorities = new HashMap<>();

    /**
     * Initialize the priority collection with a {@param baseComparator}.
     * This comparator is extended with an comparator that compares the priorities of the elements, if the
     * base comparator is undecided.
     * <p>
     * For details on the sorting see {@link TreeSet#TreeSet(Comparator)}.
     *
     * @param baseComparator the base comparator
     */
    public Prioritization(Comparator<T> baseComparator) {
        this(baseComparator, false);
    }

    /**
     * Initialize the priority collection with a {@param baseComparator}.
     * This comparator is extended with an comparator that compares the priorities of the elements, if the
     * base comparator is undecided.
     * <p>
     * For details on the sorting see {@link TreeSet#TreeSet(Comparator)}.
     *
     * @param baseComparator the base comparator
     * @param reversed       reverse the comparator before initializing the SortedSet.
     */
    public Prioritization(Comparator<T> baseComparator, boolean reversed) {
        Comparator<T> comparator = baseComparator.thenComparingInt(this::getPriority);
        if (reversed) comparator = comparator.reversed();
        elements = new TreeSet<>(comparator);
    }

    /**
     * Add an element to the priority collection.
     *
     * @param element  the element to be added
     * @param priority the priority of the element
     */
    public void add(T element, int priority) {
        priorities.put(element, priority);
        elements.remove(element);
        elements.add(element);
    }

    /**
     * Adds a Map of elements with their associated priorities to the collection.
     *
     * @param elements the key set is the set of elements that will be added. The value to a key is the priority of the
     *                 element.
     */
    public void addAll(Map<T, Integer> elements) {
        priorities.putAll(elements);
        Set<T> keySet = elements.keySet();
        // First remove it, so
        this.elements.removeAll(keySet);
        this.elements.addAll(keySet);
    }

    /**
     * Collect all elements of the collection into a list.
     *
     * @return an (ordered) list.
     */
    public List<T> toSortedList() {
        return this.toSortedList(x -> true);
    }

    /**
     * Collect all elements for which {@param filter} evaluates to true.
     *
     * @return an (ordered) list.
     */
    public List<T> toSortedList(Predicate<T> filter) {
        return elements.stream().filter(filter).collect(Collectors.toList());
    }

    /**
     * Check if this priority collection contains (at least) one element that matches {@param matcher}.
     *
     * @param matcher A predicate taking an element as input.
     * @return Whether at least one element has been found.
     */
    public boolean anyMatch(Predicate<T> matcher) {
        return elements.stream().anyMatch(matcher);
    }

    /**
     * Request the stored priority of an element.
     *
     * @param element the element whose priority is requested.
     * @return the priority of the element.
     */
    private int getPriority(T element) {
        if (!priorities.containsKey(element))
            throw new IllegalArgumentException("Priority for " + element + "not found");
        return priorities.get(element);
    }

    /**
     * @return view of all the elements in the priority collection
     */
    public Set<T> getElements(){
        return Collections.unmodifiableSet(elements);
    }

    /**
     * clears this collection
     */
    public void clear(){
        elements.clear();
        priorities.clear();
    }
}
