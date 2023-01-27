package ru.practicum.shareit.booking;

public class ItemNotAvailableForBookingException extends Exception {

    public ItemNotAvailableForBookingException(String message) {
        super(message);
    }
}
