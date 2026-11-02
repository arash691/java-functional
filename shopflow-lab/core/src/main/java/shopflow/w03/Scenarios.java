package shopflow.w03;

/** Four tiny scenarios about passing values to methods. Read them; do not edit. */
public final class Scenarios {

    private Scenarios() {}

    static void addOne(int n) {
        n = n + 1;
    }

    static void setFirstToNine(int[] a) {
        a[0] = 9;
    }

    static void replaceArray(int[] a) {
        a = new int[] {99, 99, 99};
    }

    static void shout(String s) {
        s = s.toUpperCase() + "!!!";
    }

    /** What the JVM actually produces for each scenario. */
    public static int[] actual() {
        int x = 5;
        addOne(x);

        int[] first = {1, 2, 3};
        setFirstToNine(first);

        int[] second = {1, 2, 3};
        replaceArray(second);

        String word = "hi";
        shout(word);

        return new int[] {x, first[0], second[0], word.length()};
    }
}
