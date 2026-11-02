package shopflow.w04;

public class Customer {

    private final String name;
    private final String email;

    public Customer(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("invalid email: " + email);
        }
        this.name = name;
        this.email = email;
    }

    private static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        int at = email.indexOf('@');
        return at > 0 && at == email.lastIndexOf('@') && at < email.length() - 1;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public String displayName() {
        return name + " <" + email + ">";
    }

    public Customer withEmail(String newEmail) {
        return new Customer(name, newEmail);
    }
}
