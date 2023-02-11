package ru.practicum.booking;

import java.util.stream.Stream;

public enum BookingStatus {
    /**
     * Фейковое значение для ошибки при конвертации
     */
    UNDERFUND(0, "UNDERFUND"),
    /**
     * Создана заявка на бронирование. Ожидает реакции собственника вещи
     */
    WAITING(100, "WAITING"),
    /**
     * Одобрена владельцем
     */
    APPROVED(200, "APPROVED"),
    /**
     * Отклонена владельцем
     */
    REJECTED(500, "REJECTED"),
    /**
     * FUTURE - для предстоящих заказов бронирования. Не используется в БД
     */
    FUTURE(0, "FUTURE"),
    /**
     * CURRENT - для текущих заказов бронирования. Не используется в БД
     */
    CURRENT(0, "CURRENT"),
    /**
     * PAST - для прошедших заказов бронирования. Не используется в БД
     */
    PAST(0, "PAST"),
    /**
     * ALL - для заказов бронирования во всех статусах. Не используется в БД
     */
    ALL(0, "ALL");


    private int dbCode;
    private String apiValue;

    BookingStatus(int dbCode, String apiValue) {
        this.dbCode = dbCode;
        this.apiValue = apiValue;
    }

    public static BookingStatus ofDbCode(int dbCode) {
        return Stream.of(BookingStatus.values())
                .filter(it -> it.getDbCode() == dbCode)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public static BookingStatus ofApiValue(String apiValue) {
        return Stream.of(BookingStatus.values())
                .filter(it -> it.apiValue.equals(apiValue))
                .findFirst()
                .orElse(BookingStatus.UNDERFUND);
    }

    public int getDbCode() {
        return dbCode;
    }

    public String getApiValue() {
        return apiValue;
    }
}
