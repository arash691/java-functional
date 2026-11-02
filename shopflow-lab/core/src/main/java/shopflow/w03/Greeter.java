package shopflow.w03;

/** Already implemented. You will write the test for it in EX-03-05. */
public final class Greeter {

    private Greeter() {}

    /** Upper-cases the name and appends an exclamation mark: "sara" -> "SARA!". */
    public static String shout(String name) {
        return name.toUpperCase() + "!";
    }
}
