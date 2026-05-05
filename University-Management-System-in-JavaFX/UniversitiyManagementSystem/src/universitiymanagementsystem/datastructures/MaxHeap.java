package universitiymanagementsystem.datastructures;

import java.util.Comparator;

public class MaxHeap<T> {

    private T[] heap;
    private int size;
    private final Comparator<T> comparator;
    private final int capacity;

    @SuppressWarnings("unchecked")
    public MaxHeap(int capacity, Comparator<T> comparator) {
        this.capacity = capacity;
        this.heap = (T[]) new Object[capacity];
        this.size = 0;
        this.comparator = comparator;
    }

    public void insert(T item) {
        if (size >= capacity) return;
        heap[size] = item;
        heapifyUp(size);
        size++;
    }

    public T extractMax() {
        if (isEmpty()) {
            return null;
        }
        T max = heap[0];
        heap[0] = heap[size - 1];
        heap[size - 1] = null;
        size--;
        if (!isEmpty()) {
            heapifyDown(0);
        }
        return max;
    }

    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return heap[0];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        for (int i = 0; i < size; i++) heap[i] = null;
        size = 0;
    }

    @SuppressWarnings("unchecked")
    public T[] toArray(T[] placeholder) {
        T[] result = (T[]) java.lang.reflect.Array.newInstance(placeholder.getClass().getComponentType(), size);
        System.arraycopy(heap, 0, result, 0, size);
        return result;
    }

    private void heapifyUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (comparator.compare(heap[index], heap[parent]) > 0) {
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
            int largest = index;

            if (left < size && comparator.compare(heap[left], heap[largest]) > 0) {
                largest = left;
            }
            if (right < size && comparator.compare(heap[right], heap[largest]) > 0) {
                largest = right;
            }
            if (largest != index) {
                swap(index, largest);
                index = largest;
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
}
