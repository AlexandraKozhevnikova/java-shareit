package ru.practicum.shareit.item.model;

import lombok.Data;

@Data
public class Item {
    private Integer id;
    private String title;
    private String description;
    private int ownerId;
    private Boolean isAvailable;
}
