package bearmaps.utils.pq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

/* A MinHeap class of Comparable elements backed by an ArrayList. */
public class MinHeap<E extends Comparable<E>> {

    /* An ArrayList that stores the elements in this MinHeap. */
    private ArrayList<E> contents;
    private int size;
    private HashMap<E, Integer> objectIndexPair;

    /* Initializes an empty MinHeap. */
    public MinHeap() {
        contents = new ArrayList<>();
        contents.add(null);
        objectIndexPair = new HashMap<>();
    }

    /* Returns the element at index INDEX, and null if it is out of bounds. */
    private E getElement(int index) {
        if (index >= contents.size()) {
            return null;
        } else {
            return contents.get(index);
        }
    }

    /* Sets the element at index INDEX to ELEMENT. If the ArrayList is not big
       enough, add elements until it is the right size. */
    private void setElement(int index, E element) {
        while (index >= contents.size()) {
            contents.add(null);
        }
        contents.set(index, element);
        objectIndexPair.put(element, index);
    }

    /* Swaps the elements at the two indices. */
    private void swap(int index1, int index2) {
        E element1 = getElement(index1);
        E element2 = getElement(index2);
        setElement(index2, element1);
        setElement(index1, element2);
    }

    /* Prints out the underlying heap sideways. Use for debugging. */
    @Override
    public String toString() {
        return toStringHelper(1, "");
    }

    /* Recursive helper method for toString. */
    private String toStringHelper(int index, String soFar) {
        if (getElement(index) == null) {
            return "";
        } else {
            String toReturn = "";
            int rightChild = getRightOf(index);
            toReturn += toStringHelper(rightChild, "        " + soFar);
            if (getElement(rightChild) != null) {
                toReturn += soFar + "    /";
            }
            toReturn += "\n" + soFar + getElement(index) + "\n";
            int leftChild = getLeftOf(index);
            if (getElement(leftChild) != null) {
                toReturn += soFar + "    \\";
            }
            toReturn += toStringHelper(leftChild, "        " + soFar);
            return toReturn;
        }
    }

    /* Returns the index of the left child of the element at index INDEX. */
    private int getLeftOf(int index) {
        return 2 * index;
    }

    /* Returns the index of the right child of the element at index INDEX. */
    private int getRightOf(int index) {
        return 2 * index + 1;
    }

    /* Returns the index of the parent of the element at index INDEX. */
    private int getParentOf(int index) {
        return index / 2;
    }

    /* Returns the index of the smaller element. At least one index has a
       non-null element. If the elements are equal, return either index. */
    private int min(int index1, int index2) {
        E e1 = getElement(index1);
        E e2 = getElement(index2);
        if (e1 == null) {
            return index2;
        } else if (e2 == null) {
            return index1;
        } else if (e1.compareTo(e2) < 0) {
            return index1;
        } else if (e1.compareTo(e2) > 0) {
            return index2;
        }
        return index1;
    }

    /* Returns but does not remove the smallest element in the MinHeap. */
    public E findMin() {
        return getElement(1);
    }

    /* Bubbles up the element currently at index INDEX. */
    private void bubbleUp(int index) {
        while (min(index, getParentOf(index)) == index && index > 1) {
            E element = getElement(index);
            E swapElement = getElement(getParentOf(index));
            objectIndexPair.put(element, getParentOf(index));
            objectIndexPair.put(swapElement, index);

            swap(index, getParentOf(index));
            index = getParentOf(index);
        }
    }

    /* Bubbles down the element currently at index INDEX. */
    private void bubbleDown(int index) {
        int childIndex = min(getLeftOf(index), getRightOf(index));
        int minIndex = min(index, childIndex);
        while (minIndex != index && index != size) {
            E element = getElement(index);
            E swapElement = getElement(minIndex);
            objectIndexPair.put(element, minIndex);
            objectIndexPair.put(swapElement, index);

            swap(index, minIndex);
            index = minIndex;
            childIndex = min(getLeftOf(index), getRightOf(index));
            minIndex = min(index, childIndex);
        }
    }

    /* Returns the number of elements in the MinHeap. */
    public int size() {
        return size;
    }

    /* Inserts ELEMENT into the MinHeap. If ELEMENT is already in the MinHeap,
       throw an IllegalArgumentException.*/
    public void insert(E element) {
        if (contains(element)) {
            throw new IllegalArgumentException();
        }
        size++;
        setElement(size, element);
        bubbleUp(size);
    }

    /* Returns and removes the smallest element in the MinHeap. */
    public E removeMin() {
        E min = findMin();
        if (size == 1) {
            contents.remove(1);
            size--;
            return min;
        }
        swap(1, size - 1);
        contents.remove(size - 1);
        objectIndexPair.remove(min);
        size--;
        E last = getElement(size);
        objectIndexPair.replace(last, size);
        bubbleUp(size);
        bubbleDown(1);
        return min;
    }

    /* Replaces and updates the position of ELEMENT inside the MinHeap, which
       may have been mutated since the initial insert. If a copy of ELEMENT does
       not exist in the MinHeap, throw a NoSuchElementException. Item equality
       should be checked using .equals(), not ==. */
    public void update(E element) {
        if (!contains(element)) {
            throw new NoSuchElementException();
        }

        int index = objectIndexPair.get(element);
        E removeElement = contents.get(index);

        setElement(index, element);

        objectIndexPair.remove(removeElement);
        objectIndexPair.put(element, index);

        if (element.compareTo(removeElement) > 0) {
            bubbleDown(index);
        } else {
            bubbleUp(index);
        }
    }

    /* Returns true if ELEMENT is contained in the MinHeap. Item equality should
       be checked using .equals(), not ==. */
    public boolean contains(E element) {
        if (size == 0) {
            return false;
        }
        return objectIndexPair.containsKey(element);
    }
}
