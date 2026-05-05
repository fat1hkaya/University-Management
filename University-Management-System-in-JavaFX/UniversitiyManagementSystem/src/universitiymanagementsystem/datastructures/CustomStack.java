package universitiymanagementsystem.datastructures;

import java.util.EmptyStackException;

public class CustomStack<T> {

    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }

    private Node<T> top;
    private int size;

    public CustomStack() {
        this.top = null;
        this.size = 0;
    }

    public void push(T data) {
        Node<T> newNode = new Node<>(data);
        newNode.next = top;
        top = newNode;
        size++;
    }

    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }

    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return top.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        top = null;
        size = 0;
    }

    /**
     * Converts stack to array for UI display.
     */
    @SuppressWarnings("unchecked")
    public T[] toArray(T[] placeholder) {
        T[] result = (T[]) java.lang.reflect.Array.newInstance(placeholder.getClass().getComponentType(), size);
        Node<T> curr = top;
        int i = 0;
        while (curr != null) {
            result[i++] = curr.data;
            curr = curr.next;
        }
        return result;
    }
}
