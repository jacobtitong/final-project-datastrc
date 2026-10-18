import java.util.ArrayList;

public class MergeSorter {
    // Sorts the list itself; equal keys keep their order (stable)
    public static void mergeSort(ArrayList<BookingRecord> list, String mode) {
        if (list == null || list.size() < 2) {
            return;
        }
        mergeSort(list, 0, list.size() - 1, mode);
    }

    private static void mergeSort(ArrayList<BookingRecord> list, int left, int right, String mode) {
        if (left >= right) {
            return;
        }
        int mid = (left + right) / 2;
        mergeSort(list, left, mid, mode);
        mergeSort(list, mid + 1, right, mode);
        merge(list, left, mid, right, mode);
    }

    private static void merge(ArrayList<BookingRecord> list, int left, int mid, int right, String mode) {
        ArrayList<BookingRecord> temp = new ArrayList<BookingRecord>();
        int i = left;
        int j = mid + 1;

        while (i <= mid && j <= right) {
            BookingRecord a = list.get(i);
            BookingRecord b = list.get(j);
            // on a tie take the left item
            if (a.getSortKey(mode).compareToIgnoreCase(b.getSortKey(mode)) <= 0) {
                temp.add(a);
                i++;
            } else {
                temp.add(b);
                j++;
            }
        }
        while (i <= mid) {
            temp.add(list.get(i));
            i++;
        }
        while (j <= right) {
            temp.add(list.get(j));
            j++;
        }

        for (int k = 0; k < temp.size(); k++) {
            list.set(left + k, temp.get(k));
        }
    }
}
