package shopflow.w09.review;

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

    // Identity is the immutable id only: a hash key must not change while it sits in a map.
    @Override
    public boolean equals(Object o) {
        return o instanceof Customer c && c.id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
