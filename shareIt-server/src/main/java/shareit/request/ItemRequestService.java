package shareit.request;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.data.querydsl.QSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shareit.item.ItemService;
import shareit.item.dto.ItemDto;
import shareit.request.dto.ItemRequestCreateResponse;
import shareit.request.dto.ItemRequestGetResponse;
import shareit.request.dto.ItemRequestMapper;
import shareit.user.User;
import shareit.user.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class ItemRequestService {
    private static final int DEFAULT_FROM = 1;
    private static final int DEFAULT_SIZE = 100;
    private ItemRequestRepository itemRequestRepository;
    private ItemRequestMapper itemRequestMapper;
    private UserService userService;
    private ItemService itemService;

    @Transactional
    public ItemRequestCreateResponse createItemRequest(Long authorId, Map<String, String> body) {
        User user = userService.getUserById(authorId);
        String description = body.get("description");
        ItemRequest request = new ItemRequest();
        request.setAuthor(user);
        request.setDescription(description);
        return itemRequestMapper.entityToCreateDto(itemRequestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public ItemRequestGetResponse getItemRequestById(Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Запрос вещи с " + requestId + " не существует"));
        ItemRequestGetResponse response = itemRequestMapper.entityToGetDto(itemRequest);
        response.setItemOfferDtoList(itemService.getItemsByRequestId(requestId));
        return response;
    }

    @Transactional(readOnly = true)
    public ItemRequest checkItemRequestIsExist(long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Запрос вещи с " + requestId + " не существует"));
    }

    @Transactional(readOnly = true)
    public List<ItemRequestGetResponse> getItemRequestByAuthor(Long userId) {
        List<ItemRequest> itemRequests = (List<ItemRequest>) itemRequestRepository.findAll(
                QItemRequest.itemRequest.author.id.eq(userId), new QSort(QItemRequest.itemRequest.created.desc()));

        Map<Long, List<ItemDto>> items = getItemsByItemRequestIds(itemRequests);

        return itemRequests.stream()
                .map(itemRequestMapper::entityToGetDto)
                .peek(itemRequestGetResponse -> itemRequestGetResponse.setItemOfferDtoList(
                        items.getOrDefault(itemRequestGetResponse.getId(), Collections.emptyList())
                )).collect(toList());
    }

    @Transactional(readOnly = true)
    public List<ItemRequestGetResponse> getAllOtherItemRequest(Long userId, Optional<Integer> from, Optional<Integer> size) {
        Page<ItemRequest> itemRequests = itemRequestRepository.findAll(
                QItemRequest.itemRequest.author.id.ne(userId),
                QPageRequest.of(from.orElse(DEFAULT_FROM), size.orElse(DEFAULT_SIZE))
                        .withSort(new QSort(QItemRequest.itemRequest.created.desc())));

        Map<Long, List<ItemDto>> items = getItemsByItemRequestIds(itemRequests.toList());

        return itemRequests.stream()
                .map(itemRequestMapper::entityToGetDto)
                .peek(itemRequestGetResponse -> itemRequestGetResponse.setItemOfferDtoList(
                        items.getOrDefault(itemRequestGetResponse.getId(), Collections.emptyList())
                ))
                .collect(toList());
    }

    private Map<Long, List<ItemDto>> getItemsByItemRequestIds(List<ItemRequest> itemRequests) {
        return itemService.getItemsByItemRequestIds(itemRequests)
                .stream().collect(groupingBy(ItemDto::getRequestId, toList()));
    }
}

