package universitiymanagementsystem.datastructures;

import java.util.Comparator;

public class MergeSort {

    public static <T> void sort(T[] array, Comparator<T> comparator) {
        if (array == null || array.length <= 1) {
            return;
        }
        @SuppressWarnings("unchecked")
        T[] temp = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), array.length);
        System.arraycopy(array, 0, temp, 0, array.length);
        mergeSort(temp, array, 0, array.length - 1, comparator);
    }

    private static <T> void mergeSort(T[] source, T[] dest, int left, int right, Comparator<T> comparator) {
        if (left < right) {
            int center = (left + right) / 2;
            mergeSort(dest, source, left, center, comparator);
            mergeSort(dest, source, center + 1, right, comparator);
            merge(source, dest, left, center + 1, right, comparator);
        }
    }

    private static <T> void merge(T[] source, T[] dest, int left, int right, int rightEnd, Comparator<T> comparator) {
        int leftEnd = right - 1;
        int k = left;

        while (left <= leftEnd && right <= rightEnd) {
            if (comparator.compare(source[left], source[right]) <= 0) {
                dest[k++] = source[left++];
            } else {
                dest[k++] = source[right++];
            }
        }

        while (left <= leftEnd) {
            dest[k++] = source[left++];
        }

        while (right <= rightEnd) {
            dest[k++] = source[right++];
        }
    }
}
