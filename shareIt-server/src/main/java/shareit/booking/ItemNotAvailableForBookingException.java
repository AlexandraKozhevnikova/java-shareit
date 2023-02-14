package shareit.booking;

public class ItemNotAvailableForBookingException extends RuntimeException {

    public ItemNotAvailableForBookingException(String message) {
        super(message);
    }
}
