package ru.practicum.shareit.jsonTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import java.io.IOException;


@JsonTest
public class ItemJsonTest {

    @Autowired
    JacksonTester<ItemDto> jackson;

    @Test
    void serializeTest() throws IOException {
        ItemDto dto = new ItemDto();
        dto.setId(1);
        dto.setName("cycle");
        dto.setDescription("new sport cycle");
        dto.setIsAvailable(true);

        JsonContent<ItemDto> result = jackson.write(dto);

        Assertions.assertEquals(result.getJson(),
                "{\"id\":1,\"name\":\"cycle\",\"description\":\"new sport cycle\",\"available\":true}");
    }

    @Test
    void deserializeTest() throws IOException {
        String content = "{\"id\":2,\"name\":\"cycle\",\"description\":\"new sport cycle\",\"available\":true}";
        ItemDto parsedDto = jackson.parseObject(content);

        Assertions.assertEquals(parsedDto.getId(), 2);
        Assertions.assertEquals(parsedDto.getName(), "cycle");
        Assertions.assertEquals(parsedDto.getDescription(), "new sport cycle");
        Assertions.assertTrue(parsedDto.getIsAvailable());
    }
}
