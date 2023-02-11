package ru.practicum.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.BaseClient;
import ru.practicum.booking.dto.BookingOrderCreateRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createBookingOrder(BookingOrderCreateRequest bookingOrderCreateRequest,
                                                     Long authorId) {
        return post("", authorId, bookingOrderCreateRequest);
    }

    public ResponseEntity<Object> reactBookingOrder(Long userId, Long bookingId, Boolean isApproved) {
        Map<String, Object> param = Map.of("approved", isApproved, "bookingId", bookingId);
        return patch("/{bookingId}?approved={approved}", userId, param, null);
    }

    public ResponseEntity<Object> getBookingOrderWithUserAccess(Long userId, Long bookingId) {
        return get("/{bookingId}", userId, Map.of("bookingId", bookingId));
    }

    public ResponseEntity<Object> getAllAuthorBookingOrder(Long userId, String state, Optional<Integer> from,
                                                           Optional<Integer> size) {
        Map<String, Object> param = new HashMap<>(Map.of(
                "state", state
        ));
        StringBuilder query = new StringBuilder("?state={state}&");

        if (from.isPresent()) {
            param.put("from", from.get());
            query.append("from={from}&");
        }
        if (size.isPresent()) {
            param.put("size", size.get());
            query.append("size={size}&");
        }

        return get(query.toString(), userId, param);
    }

    public ResponseEntity<Object> getAllOwnerBookingOrder(Long userId, String state, Optional<Integer> from,
                                                          Optional<Integer> size) {
        Map<String, Object> param = new HashMap<>(Map.of(
                "state", state
        ));
        StringBuilder query = new StringBuilder("?state={state}&");

        if (from.isPresent()) {
            param.put("from", from.get());
            query.append("from={from}&");
        }
        if (size.isPresent()) {
            param.put("size", size.get());
            query.append("size={size}&");
        }

        return get("/owner" + query, userId, param);
    }
}

