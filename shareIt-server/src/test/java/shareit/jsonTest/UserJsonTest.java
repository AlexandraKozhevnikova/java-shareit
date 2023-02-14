package shareit.jsonTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import shareit.user.dto.UserDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void serializeTest() throws IOException {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setEmail("randomUtils@ya.ru");
        dto.setName("Antony");

        JsonContent<UserDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Antony");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("randomUtils@ya.ru");
    }

    @Test
    void deserializeTest() throws IOException {
        String content = "{\n    \"name\": \"user\",\n    \"email\": \"user@user.com\"\n}";

        UserDto parsedDto = this.json.parseObject(content);
        assertThat(parsedDto.getEmail()).isEqualTo("user@user.com");
        assertThat(parsedDto.getName()).isEqualTo("user");
        assertThat(parsedDto.getId()).isNull();
    }
}
