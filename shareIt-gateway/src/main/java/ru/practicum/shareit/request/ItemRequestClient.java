package ru.practicum.shareit.request;

import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.BaseClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ItemRequestClient extends BaseClient {

    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createRequest(Long ownerId, Map<String, String> body) {
        Optional.ofNullable(body.get("description"))
                .filter(StringUtils::isNotBlank)
                .orElseThrow(() -> new IllegalArgumentException("'description' must not be blank"));

        return post("", ownerId, body);
    }

    public ResponseEntity<Object> getRequest(Long userId, Long requestId) {
        return get("/{requestId}", userId, Map.of("requestId", requestId));
    }

    public ResponseEntity<Object> getItemRequestByAuthor(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getAllOtherItemRequest(Long userId, Optional<Integer> from, Optional<Integer> size) {
        Map<String, Object> param = new HashMap<>();
        StringBuilder query = new StringBuilder("?");

        if (from.isPresent()) {
            param.put("from", from.get());
            query.append("from={from}&");
        }
        if (size.isPresent()) {
            param.put("size", size.get());
            query.append("size={size}&");
        }

        return get("/all" + query, userId, param);
    }
}
