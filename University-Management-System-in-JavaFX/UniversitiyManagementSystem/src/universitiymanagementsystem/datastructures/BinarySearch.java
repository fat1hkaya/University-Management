package universitiymanagementsystem.datastructures;

import java.util.Comparator;
import java.util.List;

public class BinarySearch {

    public static <T> int search(List<T> list, T target, Comparator<T> comparator) {
        if (list == null || list.isEmpty()) {
            return -1;
        }
        int left = 0;
        int right = list.size() - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            int cmp = comparator.compare(target, list.get(mid));
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        return -1;
    }
}
