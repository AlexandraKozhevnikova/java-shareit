package shareit.item.dto;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import shareit.item.model.Comment;
import shareit.item.model.Item;
import shareit.request.ItemRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, injectionStrategy = InjectionStrategy.FIELD)
public interface ItemMapper {

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "itemRequest", source = "requestId", qualifiedByName = "mapRequestId")
    @Mapping(target = "title", source = "name")
    Item dtoToItem(ItemDto dto);

    @Named("mapRequestId")
    default ItemRequest mapRequestId(Long requestId) {
        if (requestId == null) {
            return null;
        }
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(requestId);

        return itemRequest;
    }


    @Mapping(target = "requestId", source = "itemRequest.id")
    @Mapping(target = "name", source = "title")
    ItemDto itemToDto(Item item);

    @Mapping(target = "name", source = "title")
    ItemWithOptionalBookingResponseDto itemToDtoWithBookingInfo(Item item);

    @Mapping(target = "authorName", source = "user.name")
    CommentDto commentToDto(Comment comment);
}
