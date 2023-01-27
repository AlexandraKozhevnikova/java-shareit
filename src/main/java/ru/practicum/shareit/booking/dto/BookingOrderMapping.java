package ru.practicum.shareit.booking.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.BookingOrder;

@Mapper(componentModel = "spring")
public abstract class BookingOrderMapping {

    public abstract BookingOrder dtoToEntity(BookingOrderCreateRequest dto);

    @Mapping(target = "item.name", source = "item.title")
    public abstract BookingOrderResponse entityToDto(BookingOrder booking);
}
