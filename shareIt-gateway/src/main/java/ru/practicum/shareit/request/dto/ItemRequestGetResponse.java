package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ItemRequestGetResponse {
    private Long id;
    private String description;
    private LocalDateTime created;
    @JsonProperty("items")
    private List<ItemDto> itemOfferDtoList;
}
