package universitiymanagementsystem.datastructures;

/**
 * Binary Search Tree implementation for fast search, insertion, and deletion.
 * @param <K> The type of the key, must be Comparable.
 * @param <V> The type of the value.
 */
public class BST<K extends Comparable<K>, V> {

    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> left, right;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node<K, V> root;
    private int size;

    public void insert(K key, V value) {
        root = insertRec(root, key, value);
    }

    private Node<K, V> insertRec(Node<K, V> root, K key, V value) {
        if (root == null) {
            size++;
            return new Node<>(key, value);
        }

        int cmp = key.compareTo(root.key);
        if (cmp < 0) {
            root.left = insertRec(root.left, key, value);
        } else if (cmp > 0) {
            root.right = insertRec(root.right, key, value);
        } else {
            root.value = value; // Update value if key exists
        }
        return root;
    }

    public V search(K key) {
        return searchRec(root, key);
    }

    private V searchRec(Node<K, V> root, K key) {
        if (root == null) return null;

        int cmp = key.compareTo(root.key);
        if (cmp == 0) return root.value;
        return cmp < 0 ? searchRec(root.left, key) : searchRec(root.right, key);
    }

    public void delete(K key) {
        root = deleteRec(root, key);
    }

    private Node<K, V> deleteRec(Node<K, V> root, K key) {
        if (root == null) return null;

        int cmp = key.compareTo(root.key);
        if (cmp < 0) {
            root.left = deleteRec(root.left, key);
        } else if (cmp > 0) {
            root.right = deleteRec(root.right, key);
        } else {
            // Node with only one child or no child
            if (root.left == null) {
                size--;
                return root.right;
            } else if (root.right == null) {
                size--;
                return root.left;
            }

            // Node with two children: Get the inorder successor (smallest in the right subtree)
            root.key = minValue(root.right);
            root.value = search(root.key); // Keep the value associated with successor
            root.right = deleteRec(root.right, root.key);
        }
        return root;
    }

    private K minValue(Node<K, V> root) {
        K minv = root.key;
        while (root.left != null) {
            minv = root.left.key;
            root = root.left;
        }
        return minv;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    public int size() {
        return size;
    }

    /**
     * Traverses the tree in-order and returns values in an array.
     * Useful for JavaFX TableView integration.
     */
    @SuppressWarnings("unchecked")
    public V[] toArray(V[] placeholder) {
        V[] result = (V[]) java.lang.reflect.Array.newInstance(placeholder.getClass().getComponentType(), size);
        inOrder(root, result, new int[]{0});
        return result;
    }

    private void inOrder(Node<K, V> root, V[] result, int[] index) {
        if (root != null) {
            inOrder(root.left, result, index);
            result[index[0]++] = root.value;
            inOrder(root.right, result, index);
        }
    }
}
