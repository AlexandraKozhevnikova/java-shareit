package shareit.jsonTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import shareit.item.dto.ItemDto;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@JsonTest
public class ItemJsonTest {

    @Autowired
    JacksonTester<ItemDto> jackson;

    @Test
    void serializeTest() throws IOException {
        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("cycle");
        dto.setDescription("new sport cycle");
        dto.setIsAvailable(true);
        dto.setRequestId(2222L);

        JsonContent<ItemDto> result = jackson.write(dto);

        assertEquals(result.getJson(),
                "{\"id\":1,\"name\":\"cycle\",\"description\":\"new sport cycle\",\"requestId\":2222,\"available\":true}");
    }

    @Test
    void deserializeTest() throws IOException {
        String content = "{\"id\":2,\"name\":\"cycle\",\"description\":\"new sport cycle\",\"available\":true,\"requestId\":2222}";
        ItemDto parsedDto = jackson.parseObject(content);

        assertEquals(parsedDto.getId(), 2);
        assertEquals(parsedDto.getName(), "cycle");
        assertEquals(parsedDto.getDescription(), "new sport cycle");
        assertEquals(parsedDto.getRequestId(), 2222L);
        assertTrue(parsedDto.getIsAvailable());
    }
}
