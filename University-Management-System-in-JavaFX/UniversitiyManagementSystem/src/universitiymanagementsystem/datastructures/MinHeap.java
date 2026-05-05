package universitiymanagementsystem.datastructures;

import java.util.Comparator;

/**
 * Custom Min-Heap implementation for Priority Queue functionality.
 * Manages its own dynamic array to avoid java.util dependencies.
 */
public class MinHeap<T> {

    private T[] heap;
    private int size;
    private int capacity;
    private final Comparator<T> comparator;

    @SuppressWarnings("unchecked")
    public MinHeap(int initialCapacity, Comparator<T> comparator) {
        this.capacity = initialCapacity;
        this.size = 0;
        this.heap = (T[]) new Object[initialCapacity];
        this.comparator = comparator;
    }

    public void insert(T item) {
        if (size == capacity) {
            grow();
        }
        heap[size] = item;
        heapifyUp(size);
        size++;
    }

    public T extractMin() {
        if (size == 0) return null;
        T min = heap[0];
        heap[0] = heap[size - 1];
        heap[size - 1] = null;
        size--;
        heapifyDown(0);
        return min;
    }

    public T peek() {
        return size == 0 ? null : heap[0];
    }

    private void heapifyUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (comparator.compare(heap[index], heap[parent]) < 0) {
                swap(index, parent);
                index = parent;
            } else {
                break;
            }
        }
    }

    private void heapifyDown(int index) {
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int smallest = index;

            if (left < size && comparator.compare(heap[left], heap[smallest]) < 0) {
                smallest = left;
            }
            if (right < size && comparator.compare(heap[right], heap[smallest]) < 0) {
                smallest = right;
            }

            if (smallest != index) {
                swap(index, smallest);
                index = smallest;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        T temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    @SuppressWarnings("unchecked")
    private void grow() {
        capacity *= 2;
        T[] newHeap = (T[]) new Object[capacity];
        System.arraycopy(heap, 0, newHeap, 0, size);
        heap = newHeap;
    }

    /**
     * Returns elements as a sorted array for UI display without destroying the original heap.
     */
    @SuppressWarnings("unchecked")
    public T[] toSortedArray(T[] placeholder) {
        MinHeap<T> copy = new MinHeap<>(capacity, comparator);
        for (int i = 0; i < size; i++) {
            copy.insert(heap[i]);
        }
        
        T[] sorted = (T[]) java.lang.reflect.Array.newInstance(placeholder.getClass().getComponentType(), size);
        for (int i = 0; i < size; i++) {
            sorted[i] = copy.extractMin();
        }
        return sorted;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns elements as an array for UI display.
     */
    @SuppressWarnings("unchecked")
    public T[] toArray(T[] placeholder) {
        T[] result = (T[]) java.lang.reflect.Array.newInstance(placeholder.getClass().getComponentType(), size);
        System.arraycopy(heap, 0, result, 0, size);
        return result;
    }
}
