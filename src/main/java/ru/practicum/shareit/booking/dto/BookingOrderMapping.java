package ru.practicum.shareit.booking.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.model.BookingOrder;

@Mapper(componentModel = "spring")
public abstract class BookingOrderMapping {

    @Mapping(target = "item.id", source = "itemId")
    public abstract BookingOrder dtoToEntity(BookingOrderCreateRequest dto);

    @Mapping(target = "item.name", source = "item.title")
    public abstract BookingOrderResponse entityToDto(BookingOrder booking);
}
