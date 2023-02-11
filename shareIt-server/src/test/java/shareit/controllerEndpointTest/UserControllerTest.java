package shareit.controllerEndpointTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import shareit.user.User;
import shareit.user.UserController;
import shareit.user.UserService;
import shareit.user.dto.UserDto;
import shareit.user.dto.UserMapper;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;


@ExtendWith({MockitoExtension.class})
public class UserControllerTest {
    @Mock
    private UserService userServiceMock;
    @InjectMocks
    private UserController controller;
    @Mock
    private UserMapper mapper;
    private MockMvc mvc;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        user = new User();
        user.setId(1L);
        user.setEmail("randomUtils@ya.ru");
        user.setName("Antony");

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("randomUtils@ya.ru");
        userDto.setName("Antony");
    }

    @Test
    void createUser() throws Exception {
        User incomeUser = new User();
        incomeUser.setEmail("randomUtils@ya.ru");
        incomeUser.setName("Antony");

        Mockito.when(userServiceMock.createUser(Mockito.any()))
                .thenReturn(user);

        Mockito.when(mapper.dtoToUser(Mockito.any()))
                .thenReturn(incomeUser);
        Mockito.when(mapper.userToDto(Mockito.any()))
                .thenReturn(userDto);

        mvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .content("{\n    \"name\": \"user\",\n    \"email\": \"user@user.com\"\n}")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", is("Antony")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", is("randomUtils@ya.ru")));
    }
}
