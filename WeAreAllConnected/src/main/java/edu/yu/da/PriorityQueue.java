package edu.yu.da;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PriorityQueue<E extends Comparable<E>> {
    private E[] elements;
    private int count = 0;
    private final Map<E, Integer> indexMap; // used to keep track of each element's index

    public PriorityQueue() {
        this(15); // completely random number, since I don't have a better idea
    }

    public PriorityQueue(int initialCapacity) {
        elements = (E[]) new Comparable[initialCapacity + 1];
        indexMap = new HashMap<>();
    }

    /**
     * @return true for an empty priority queue, false otherwise
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Adds an element to the priority queue
     * @param x element being added
     */
    public void insert(E x) {
        // double size of array if necessary
        // I'm not changing the array, because I don't want to break anything
        // and the doubling amortizes to O(1)
        if (count >= elements.length - 1) {
            doubleArraySize();
        }
        // add x to the bottom of the heap
        elements[++count] = x;
        // whenever we edit the heap, we update the location
        indexMap.put(x, count);
        // percolate it up to maintain heap order property
        upHeap(count);
    }

    public E remove() {
        if (isEmpty()) {
            return null; // should return null if a no-op
        }
        E min = this.elements[1];
        //swap root with last, decrement count
        this.swap(1, this.count--);
        //move new root down as needed
        this.downHeap(1);
        this.elements[this.count + 1] = null; //null it to prepare for GC
        // I am removing the key from the hashmap to avoid cluttering
        indexMap.remove(min);
        return min;
    }

    public void reHeapify(E element) {
        if (element == null) {
            throw new IllegalArgumentException("element is null");
        }
        // now O(1) time to find the integer, because I used a HashMap
        // I'm not sure why I didn't do that in my initial implementation, I already knew about HashMaps
        Integer elementIndex = indexMap.get(element); // an Integer, because could be null
        if (elementIndex == null) {
            return; // should be no-op if not in heap
        }
        // upheaps and downheaps, only one (or maybe zero) will actually do anything
        // if isn't called, elementIndex won't be changed
        upHeap(elementIndex);
        downHeap(elementIndex);
    }

    private void doubleArraySize() {
        // copies all old elements into the new array, which is double the length
        // assuming I correctly understand what this is for, this was easy, it's like Python
        elements = Arrays.copyOf(elements, elements.length * 2);
    }

    /**
     * is elements[i] > elements[j]?
     */
    private boolean isGreater(int i, int j) {
        return elements[j] != null && this.elements[i].compareTo(this.elements[j]) > 0;
        // if j is null, this should return false
    }

    /**
     * swap the values stored at elements[i] and elements[j]
     */
    private void swap(int i, int j) {
        E temp = this.elements[i];
        this.elements[i] = this.elements[j];
        this.elements[j] = temp;
        indexMap.put(elements[i], j);
        indexMap.put(elements[j], i);
    }

    /**
     * while the key at index k is less than its
     * parent's key, swap its contents with its parent’s
     * @param k the initial location of the key
     */
    private void upHeap(int k) {
        while (k > 1 && this.isGreater(k / 2, k)) {
            this.swap(k, k / 2);
            k = k / 2;
        }
    }

    /**
     * move an element down the heap until it is less than
     * both its children or is at the bottom of the heap
     * @param k the location of the element we are pushing down
     */
    private void downHeap(int k) {
        while (2 * k <= this.count) {
            //identify which of the 2 children are smaller
            int j = 2 * k; // j will refer to the child we are swapping with
            if (j < this.count && this.isGreater(j, j + 1)) {
                j++;
            }
            //if the current value is < the smaller child, we're done
            if (!this.isGreater(k, j)) {
                break;
            }
            //if not, swap and continue testing
            this.swap(k, j);
            k = j;
        }
    }
}