package universitiymanagementsystem.datastructures;

import java.util.NoSuchElementException;

public class CustomQueue<T> {

    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }

    private Node<T> front;
    private Node<T> rear;
    private int size;

    public CustomQueue() {
        this.front = null;
        this.rear = null;
        this.size = 0;
    }

    public void enqueue(T data) {
        Node<T> newNode = new Node<>(data);
        if (rear == null) {
            front = rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        T data = front.data;
        front = front.next;
        if (front == null) {
            rear = null;
        }
        size--;
        return data;
    }

    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return front.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        front = null;
        rear = null;
        size = 0;
    }

    /**
     * Converts queue to array for UI display.
     */
    @SuppressWarnings("unchecked")
    public T[] toArray(T[] placeholder) {
        T[] result = (T[]) java.lang.reflect.Array.newInstance(placeholder.getClass().getComponentType(), size);
        Node<T> curr = front;
        int i = 0;
        while (curr != null) {
            result[i++] = curr.data;
            curr = curr.next;
        }
        return result;
    }
}
