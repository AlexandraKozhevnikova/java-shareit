package ru.practicum.shareit.item.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "title", source = "name")
    Item dtoToItem(ItemDto dto);

    @Mapping(target = "name", source = "title")
    ItemDto itemToDto(Item item);
}