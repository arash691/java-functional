package shopflow.w04;

public record Address(String street, String city, String countryCode) {

    public Address {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("street must not be blank");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("city must not be blank");
        }
        if (countryCode == null || countryCode.length() != 2
                || countryCode.charAt(0) < 'A' || countryCode.charAt(0) > 'Z'
                || countryCode.charAt(1) < 'A' || countryCode.charAt(1) > 'Z') {
            throw new IllegalArgumentException("countryCode must be two uppercase letters: " + countryCode);
        }
    }

    public Address withCity(String newCity) {
        return new Address(street, newCity, countryCode);
    }
}
