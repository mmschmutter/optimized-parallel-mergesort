/**
 * Threaded MergeSort
 * 
 * @author Mordechai Schmutter 
 * @version 1.0
 */
public class MergesortParallelNaive {

    // This class should not be instantiated.
    private MergesortParallelNaive() {
    }

    public static long sort(Integer[] a) {
        long start = System.nanoTime();
        Integer[] aux = new Integer[a.length];
        sort(a, aux, 0, a.length - 1);
        long end = System.nanoTime();
        long time = end - start;
        System.out.println("Multi-threaded (" + a.length + " elements): " + time + "ns");
        return time;
    }

    // mergesort a[lo..hi] using auxiliary array aux[lo..hi]
    private static void sort(Integer[] a, Integer[] aux, int lo, int hi) {
        if (hi <= lo) {
            return;
        }
        int mid = lo + (hi - lo) / 2;
        // recursively: reduce sub-arrays to length 1, merge up
        Thread thread = makeThread(a, aux, lo, mid);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sort(a, aux, mid + 1, hi);
        merge(a, aux, lo, mid, hi);
    }

    private static Thread makeThread(Integer[] a, Integer[] aux, int lo, int hi) {
        return new Thread() {
            @Override
            public void run() {
                sort(a, aux, lo, hi);
            }
        };
    }

    // stably merge a[lo .. mid] with a[mid+1 ..hi] using aux[lo .. hi]
    // precondition: a[lo .. mid] and a[mid+1 .. hi] are sorted subarrays
    private static void merge(Integer[] a, Integer[] aux, int lo, int mid, int hi) {
        // copy to aux[]
        for (int k = lo; k <= hi; k++) {
            aux[k] = a[k];
        }
        // merge back to a[]
        int left = lo, right = mid + 1;
        for (int current = lo; current <= hi; current++) {
            if (left > mid) { // left half exhausted
                a[current] = aux[right++]; // copy value from the right
            } else if (right > hi) { // right half exhausted
                a[current] = aux[left++]; // copy value from the left
            }
            // neither exhausted - copy lower value
            else if (less(aux[right], aux[left])) {
                a[current] = aux[right++];
            } else {
                a[current] = aux[left++];
            }
        }
    }

    private static boolean less(Integer v, Integer w) {
        return v.compareTo(w) < 0;
    }
}