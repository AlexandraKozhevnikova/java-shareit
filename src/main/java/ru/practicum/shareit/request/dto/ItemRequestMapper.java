package ru.practicum.shareit.request.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.shareit.request.ItemRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemRequestMapper {

    ItemRequestCreateResponse entityToCreateDto(ItemRequest itemRequest);

    ItemRequestGetResponse entityToGetDto(ItemRequest itemRequest);
}
