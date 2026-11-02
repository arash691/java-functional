package shopflow.w09.review;

/** The price list could not be read or understood. */
public class PriceListException extends RuntimeException {

    public PriceListException(String message, Throwable cause) {
        super(message, cause);
    }
}
