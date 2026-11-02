package shopflow.w05.loyalty;

public class Customer {
    private final String id;
    private String name;

    public Customer(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void rename(String newName) {
        this.name = newName;
    }

    // Identity is the immutable id; the name can change without changing who the customer is.
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Customer other && id.equals(other.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
