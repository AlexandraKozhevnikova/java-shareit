package ru.practicum.shareit.item;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.data.querydsl.QSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.QComment;
import ru.practicum.shareit.item.model.QItem;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemService {
    private ItemRepository itemRepository;
    private CommentRepository commentRepository;
    private UserService userService;
    private BookingService bookingService;
    private ItemMapper itemMapper;
    private ItemRequestService itemRequestService;
    private static final int DEFAULT_FROM = 1;
    private static final int DEFAULT_SIZE = 100;

    @Autowired
    public ItemService(ItemRepository itemRepository, CommentRepository commentRepository, UserService userService,
                       @Lazy BookingService bookingService, ItemMapper itemMapper,
                       @Lazy ItemRequestService itemRequestService) {
        this.itemRepository = itemRepository;
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.bookingService = bookingService;
        this.itemMapper = itemMapper;
        this.itemRequestService = itemRequestService;
    }

    @Transactional
    public Item createItem(Item item, long ownerId) {
        User owner = userService.getUserById(ownerId);
        if (item.getItemRequest() != null) {
            ItemRequest itemRequest = itemRequestService.checkItemRequestIsExist(item.getItemRequest().getId());
            item.setItemRequest(itemRequest);
        }
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    @Transactional
    public Item updateItem(Item updatedData, long userId) {
        User user = userService.getUserById(userId);
        updatedData.setOwner(user);

        Item itemFromRep = checkItemBelongUser(updatedData);

        if (updatedData.getTitle() != null && !updatedData.getTitle().equals(itemFromRep.getTitle())) {
            itemFromRep.setTitle(updatedData.getTitle());
        }
        if (updatedData.getDescription() != null && !updatedData.getDescription()
                .equals(itemFromRep.getDescription())) {
            itemFromRep.setDescription(updatedData.getDescription());
        }
        if (updatedData.getIsAvailable() != null && !updatedData.getIsAvailable()
                .equals(itemFromRep.getIsAvailable())) {
            itemFromRep.setIsAvailable(updatedData.getIsAvailable());
        }

        return itemRepository.save(itemFromRep);
    }

    @Transactional(readOnly = true)
    public Item getItemWithUserAccess(long itemId, long userId) {
        userService.getUserById(userId);
        return checkItemIsExistInRep(itemId);
    }

    @Transactional(readOnly = true)
    public Page<Item> getOwnersItems(long userId, Optional<Integer> from, Optional<Integer> size) {
        userService.getUserById(userId);
        return itemRepository.findAll(QItem.item.owner.id.eq(userId),
                PageRequest.of((from.orElse(DEFAULT_FROM) - 1), size.orElse(DEFAULT_SIZE))
                        .withSort(new QSort(QItem.item.id.asc())));
    }

    @Transactional(readOnly = true)
    public Page<Item> findAllWithText(String text, Optional<Integer> from, Optional<Integer> size) {
        BooleanExpression isAvailable = QItem.item.isAvailable.isTrue();
        BooleanExpression titleContains = QItem.item.title.containsIgnoreCase(text);
        BooleanExpression descriptionContains = QItem.item.description.containsIgnoreCase(text);

        return StringUtils.isBlank(text) ? new PageImpl<Item>(Collections.EMPTY_LIST)
                : itemRepository.findAll(isAvailable.andAnyOf(descriptionContains, titleContains),
                QPageRequest.of((from.orElse(DEFAULT_FROM) - 1), size.orElse(DEFAULT_SIZE)));
    }

    public Item checkItemIsExistInRep(long id) {
        Optional<Item> item = itemRepository.findById(id);
        return item.orElseThrow(() -> new NoSuchElementException("Вещь с 'id' = " + id + " не существует"));
    }

    public Item checkItemBelongUser(Item unverifiedItem) {
        Item itemFromRep = checkItemIsExistInRep(unverifiedItem.getId());
        if (!itemFromRep.getOwner().getId().equals(unverifiedItem.getOwner().getId())) {
            throw new NoSuchElementException("У пользователя с id = " + unverifiedItem.getOwner().getId() +
                    "  нет прав редактировать вещь с  id = " + unverifiedItem.getId() + " ");
        }
        return itemFromRep;
    }

    @Transactional
    public Comment addComment(Long userId, Long itemId, String text) {
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("text must not be blank");
        }
        Item item = getItemWithUserAccess(itemId, userId);
        User user = bookingService.checkUserHadItemBooking(userId, itemId);
        Comment comment = new Comment(null, user, item, text);
        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<Comment> getComment(Long itemId) {
        return (List<Comment>) commentRepository.findAll(
                QComment.comment.item.id.eq(itemId),
                new QSort(QComment.comment.created.desc()));
    }

    @Transactional(readOnly = true)
    public List<ItemDto> getItemsByRequestId(Long requestId) {
        List<Item> items = (List<Item>) itemRepository.findAll(QItem.item.itemRequest.id.eq(requestId),
                new QSort(QItem.item.itemRequest.created.desc()));
        return items.stream()
                .map(itemMapper::itemToDto)
                .collect(Collectors.toList());
    }
}
