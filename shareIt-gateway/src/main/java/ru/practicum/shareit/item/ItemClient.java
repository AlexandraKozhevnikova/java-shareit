package ru.practicum.shareit.item;

import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.BaseClient;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createItem(Long ownerId, ItemDto itemDto) {
        return post("", ownerId, itemDto);
    }

    public ResponseEntity<Object> updateItem(Long userId, Long itemId, ItemDto itemDto) {
        return patch("/{itemId}", userId, Map.of("itemId", itemId), itemDto);
    }

    public ResponseEntity<Object> getItem(Long userId, Long itemId) {
        return get("/{itemId}", userId, Map.of("itemId", itemId));
    }

    public ResponseEntity<Object> getOwnersItems(Long userId, Optional<Integer> from, Optional<Integer> size) {
        Map<String, Object> param = new HashMap<>();

        StringBuilder query = new StringBuilder("?");

        if (from.isPresent()) {
            param.put("from", from);
            query.append("from={from}&");
        }
        if (size.isPresent()) {
            param.put("size", size);
            query.append("size={size}&");
        }

        return get(query.toString(), userId, param);
    }


    public ResponseEntity<Object> searchItem(Long userId, String text, Optional<Integer> from, Optional<Integer> size) {
        if (StringUtils.isBlank(text)) {
            return new ResponseEntity<>(Collections.EMPTY_LIST, HttpStatus.OK);
        }

        Map<String, Object> param = new HashMap<>(
                Map.of("text", text)
        );
        StringBuilder query = new StringBuilder("?text={text}&");


        if (from.isPresent()) {
            param.put("from", from.get());
            query.append("from={from}&");
        }
        if (size.isPresent()) {
            param.put("size", size.get());
            query.append("size={size}&");
        }

        return get("/search" + query, userId, param);
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, Map<String, String> body) {
        Optional.ofNullable(body.get("text"))
                .filter(StringUtils::isNotBlank)
                .orElseThrow(() -> new IllegalArgumentException("'text' must not be blank"));

        return post("/{itemId}/comment", userId, Map.of("itemId", itemId), body);
    }
}
