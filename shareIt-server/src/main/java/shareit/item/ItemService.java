package shareit.item;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.data.querydsl.QSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shareit.booking.BookingService;
import shareit.booking.dto.BookingInfoDto;
import shareit.item.dto.ItemDto;
import shareit.item.dto.ItemMapper;
import shareit.item.dto.ItemWithOptionalBookingResponseDto;
import shareit.item.model.Comment;
import shareit.item.model.Item;
import shareit.item.model.QComment;
import shareit.item.model.QItem;
import shareit.request.ItemRequest;
import shareit.request.ItemRequestService;
import shareit.user.User;
import shareit.user.UserService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class ItemService {
    private static final int DEFAULT_FROM = 1;
    private static final int DEFAULT_SIZE = 100;
    private ItemRepository itemRepository;
    private CommentRepository commentRepository;
    private UserService userService;
    private BookingService bookingService;
    private ItemMapper itemMapper;
    private ItemRequestService itemRequestService;

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

        if (updatedData.getTitle() != null) {
            itemFromRep.setTitle(updatedData.getTitle());
        }
        if (updatedData.getDescription() != null) {
            itemFromRep.setDescription(updatedData.getDescription());
        }
        if (updatedData.getIsAvailable() != null) {
            itemFromRep.setIsAvailable(updatedData.getIsAvailable());
        }

        return itemFromRep;
    }

    @Transactional(readOnly = true)
    public Item getItemWithUserAccess(long itemId, long userId) {
        userService.getUserById(userId);
        return checkItemIsExistInRep(itemId);
    }

    @Transactional(readOnly = true)
    public List<ItemWithOptionalBookingResponseDto> getOwnersItems(long userId, Optional<Integer> from, Optional<Integer> size) {
        userService.checkUserExist(userId);

        Page<Item> items = itemRepository.findAll(
                QItem.item.owner.id.eq(userId),
                PageRequest.of(
                        (from.orElse(DEFAULT_FROM) - 1), size.orElse(DEFAULT_SIZE)
                ).withSort(new QSort(QItem.item.id.asc())));

        Map<Long, BookingInfoDto> lasts = bookingService.getLastBookingForItems(items.map(Item::getId).toList())
                .stream()
                .collect(Collectors.toMap(
                        order -> order.getItem().getId(),
                        order -> new BookingInfoDto(order.getId(), order.getAuthor().getId()))
                );

        Map<Long, BookingInfoDto> nexts = bookingService.getNextBookingForItems(items.map(Item::getId).toList())
                .stream()
                .collect(Collectors.toMap(
                        order -> order.getItem().getId(),
                        order -> new BookingInfoDto(order.getId(), order.getAuthor().getId()))
                );

        return items.stream()
                .map(itemMapper::itemToDtoWithBookingInfo)
                .peek(itemDto -> {
                            itemDto.setLastBooking(lasts.get(itemDto.getId()));
                            itemDto.setNextBooking(nexts.get(itemDto.getId()));
                        }
                ).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<Item> findAllWithText(String text, Optional<Integer> from, Optional<Integer> size) {
        BooleanExpression isAvailable = QItem.item.isAvailable.isTrue();
        BooleanExpression titleContains = QItem.item.title.containsIgnoreCase(text);
        BooleanExpression descriptionContains = QItem.item.description.containsIgnoreCase(text);

        return itemRepository.findAll(isAvailable.andAnyOf(descriptionContains, titleContains),
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
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public List<ItemDto> getItemsByItemRequestIds(List<ItemRequest> itemRequests) {
        List<Item> items = (List<Item>) itemRepository.findAll(QItem.item.itemRequest.in(itemRequests));
        return items.stream()
                .map(itemMapper::itemToDto)
                .collect(Collectors.toList());
    }
}
