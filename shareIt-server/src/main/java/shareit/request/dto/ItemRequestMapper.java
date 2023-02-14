package shareit.request.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import shareit.request.ItemRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemRequestMapper {

    ItemRequestCreateResponse entityToCreateDto(ItemRequest itemRequest);

    ItemRequestGetResponse entityToGetDto(ItemRequest itemRequest);
}
