package ru.practicum.shareit.item.model;

import lombok.Data;

@Data
public class Item {
    private int id;
    private String title;
    private String description;
    private int ownerId;
    private boolean isAvailable = true;
}
