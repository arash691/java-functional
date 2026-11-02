package shopflow.w04;

/**
 * EX-04-03: a record. A record gives you the constructor, accessors, equals, hashCode and
 * toString for free. Your job: add validation in a COMPACT constructor, and withCity.
 *  - street and city must not be blank
 *  - countryCode must be exactly two uppercase letters (A-Z)
 */
public record Address(String street, String city, String countryCode) {

    public Address withCity(String newCity) {
        return this; // TODO EX-04-03
    }
}
