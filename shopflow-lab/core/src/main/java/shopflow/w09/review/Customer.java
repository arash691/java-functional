package shopflow.w09.review;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        return o instanceof Customer c && c.id.equals(id) && c.name.equals(name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
