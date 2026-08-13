package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

@Service
public class ItemRequestClient extends BaseClient {

    private static final String API_PREFIX = "/requests";

    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX)).build());
    }

    public ResponseEntity<Object> create(Long userId, NewItemRequestDto request) {
        return post("", userId, request);
    }

    public ResponseEntity<Object> findOwn(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> findAll(Long userId) {
        return get("/all", userId);
    }

    public ResponseEntity<Object> findById(Long userId, Long requestId) {
        return get("/" + requestId, userId);
    }
}