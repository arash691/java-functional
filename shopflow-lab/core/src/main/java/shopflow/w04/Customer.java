package shopflow.w04;

/**
 * EX-04-01: design a small immutable class from its tests.
 *  - name must not be null/blank; email must contain exactly one '@' with text before and after
 *  - displayName() -> "Sara Ahmadi <sara@example.com>"
 *  - withEmail(newEmail) returns a NEW Customer; the original is unchanged
 */
public class Customer {

    public Customer(String name, String email) {
        // TODO EX-04-01: validate and store (private final fields)
    }

    public String name() {
        return "TODO";
    }

    public String email() {
        return "TODO";
    }

    public String displayName() {
        return "TODO";
    }

    public Customer withEmail(String newEmail) {
        return this;
    }
}
