package shopflow.w03;

/**
 * EX-03-04: predict, then check.
 * Read Scenarios.java WITHOUT running it. Fill in the four values you believe
 * Scenarios.actual() returns, in order:
 *   [0] x after addOne(x)
 *   [1] first[0] after setFirstToNine(first)
 *   [2] second[0] after replaceArray(second)
 *   [3] word.length() after shout(word)
 * Then run the test. If it is red, read the message, find your wrong belief, fix the number.
 */
public final class PassByValueQuiz {

    private PassByValueQuiz() {}

    public static int[] predictions() {
        return new int[] {0, 0, 0, 0};
    }
}
