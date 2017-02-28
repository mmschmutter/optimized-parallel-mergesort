import java.util.Random;
/**
 * An optimized MergeSort that switches between threaded and unthreaded sorting based on performance.
 * 
 * @author Mordechai Schmutter 
 * @version 1.0
 */
public class MergesortParallelSmart {
    private static int S;
    private static boolean switchedMulti = true;
    private static boolean switchedSingle = true;

    private MergesortParallelSmart() {
    }

    public static void main(String[] args) {
        S = findS();
        Integer[] a = createRandomArray(10 * S);
        sort(a);
    }

    private static void sort(Integer[] a) {
        Integer[] aux = new Integer[a.length];
        sort(a, aux, 0, a.length - 1);
        String result = "Result: [";
        for (int i : a) {
            result += i + ", ";
        }
        result = result.substring(0, result.length() - 2) + "]";
        System.out.println(result);
    }

    // mergesort a[lo..hi] using auxiliary array aux[lo..hi]
    private static void sort(Integer[] a, Integer[] aux, int lo, int hi) {
        if (hi <= lo) {
            return;
        }
        int mid = lo + (hi - lo) / 2;
        if ((hi - lo) > S) {
            if (switchedMulti == false) {
                System.out.println("Thread " + Thread.currentThread().getId() + " is switching from unthreaded to threaded sort for array size " + (hi - lo) + ".");
                switchedMulti = true;
            }
            switchedSingle = false;
            Thread thread = makeThread(a, aux, lo, mid);
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sort(a, aux, mid + 1, hi);
        } else {
            if (switchedSingle == false) {
                System.out.println("Thread " + Thread.currentThread().getId() + " is switching from threaded to unthreaded sort for array size " + (hi - lo) + ".");
                switchedSingle = true;
            }
            switchedMulti = false;
            // recursively: reduce sub-arrays to length 1, merge up
            sort(a, aux, lo, mid);
            sort(a, aux, mid + 1, hi);
        }
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

    private static Integer[] createRandomArray(int length) {
        Integer[] a = new Integer[length];
        Random rand = new Random();
        for (int i = 0; i < a.length; i++) {
            a[i] = rand.nextInt(1000000);
        }
        return a;
    }

    private static int findS() {
        long unthreaded = 0;
        long threaded = 0;
        int n = 1;
        while (threaded >= unthreaded) {
            n *= 10;
            Integer[] a = createRandomArray(n);
            unthreaded = Mergesort.sort(a);
            threaded = MergesortParallelNaive.sort(a);
        }
        int m = n / 10;
        int S = (m + n) / 2;
        boolean searching = true;
        while (searching) {
            Integer[] a = createRandomArray(S);
            unthreaded = Mergesort.sort(a);
            threaded = MergesortParallelNaive.sort(a);
            if (threaded >= unthreaded) {
                m = S;
                S = (S + n) / 2;
            } else if (threaded < unthreaded) {
                n = S;
                S = (S + m) / 2;
            }
            if (S == m || n == m) {
                searching = false;
            }
        }
        return S;
    }
}