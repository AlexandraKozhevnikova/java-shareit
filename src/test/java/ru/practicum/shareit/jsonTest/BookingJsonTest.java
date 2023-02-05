package ru.practicum.shareit.jsonTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingOrderCreateRequest;
import ru.practicum.shareit.booking.dto.BookingOrderResponse;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.io.IOException;
import java.time.LocalDateTime;

@JsonTest
public class BookingJsonTest {
    private final JacksonTester<BookingOrderResponse> jacksonResponse;
    private final JacksonTester<BookingOrderCreateRequest> jacksonRequest;

    @Autowired
    public BookingJsonTest(JacksonTester<BookingOrderResponse> jacksonResponse,
                           JacksonTester<BookingOrderCreateRequest> jacksonRequest) {
        this.jacksonResponse = jacksonResponse;
        this.jacksonRequest = jacksonRequest;
    }

    @Test
    void serializeTest() throws IOException {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("randomUtils@ya.ru");
        userDto.setName("Antony");

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("cycle");
        itemDto.setDescription("new sport cycle");
        itemDto.setIsAvailable(true);


        BookingOrderResponse dto = new BookingOrderResponse();
        dto.setId(1);
        dto.setAuthor(userDto);
        dto.setItem(itemDto);
        dto.setStatus(BookingStatus.APPROVED);
        dto.setStart(LocalDateTime.parse("2030-01-31T19:53:19.363093"));
        dto.setEnd(LocalDateTime.parse("2030-02-02T19:53:19.363129"));

        JsonContent<BookingOrderResponse> result = jacksonResponse.write(dto);

        Assertions.assertEquals("{\"id\":1,\"item\":{\"id\":1,\"name\":\"cycle\",\"description\":" +
                        "\"new sport cycle\",\"available\":true},\"status\":\"APPROVED\"," +
                        "\"start\":\"2030-01-31T19:53:19.363093\",\"end\":\"2030-02-02T19:53:19.363129\"," +
                        "\"booker\":{\"id\":1,\"name\":\"Antony\",\"email\":\"randomUtils@ya.ru\"}}",
                result.getJson());
    }

    @Test
    void deserializeTest() throws IOException {
        String content = "{\n    \"itemId\": 2,\n    \"start\": \"2030-01-31T19:53:19.363093\"," +
                "\n    \"end\": \"2030-02-02T19:53:19.363129\"\n}";
        BookingOrderCreateRequest parsedDto = jacksonRequest.parseObject(content);

        Assertions.assertEquals(parsedDto.getItemId(), 2);
        Assertions.assertEquals(parsedDto.getStart(), LocalDateTime.parse("2030-01-31T19:53:19.363093"));
        Assertions.assertEquals(parsedDto.getEnd(), LocalDateTime.parse("2030-02-02T19:53:19.363129"));
    }
}
