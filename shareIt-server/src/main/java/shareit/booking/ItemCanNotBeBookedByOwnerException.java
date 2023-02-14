package shareit.booking;

public class ItemCanNotBeBookedByOwnerException extends RuntimeException {

    public ItemCanNotBeBookedByOwnerException(String message) {
        super(message);
    }
}
