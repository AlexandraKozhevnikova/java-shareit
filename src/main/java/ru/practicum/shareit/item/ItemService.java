package ru.practicum.shareit.item;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.QItem;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ItemService {

    private ItemRepository itemRepository;
    private CommentRepository commentRepository;
    private UserService userService;
    private BookingService bookingService;

    @Autowired
    public ItemService(ItemRepository itemRepository, CommentRepository commentRepository, UserService userService,
                       @Lazy
                       BookingService bookingService) {
        this.itemRepository = itemRepository;
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.bookingService = bookingService;
    }

    public Item createItem(Item item, long ownerId) {
        User owner = userService.getUserById(ownerId);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    public Item updateItem(Item updatedData, long userId) {
        User user = userService.getUserById(userId);
        updatedData.setOwner(user);

        Item itemFromRep = checkItemBelongUser(updatedData);

        if (updatedData.getTitle() != null && !updatedData.getTitle().equals(itemFromRep.getTitle())) {
            itemFromRep.setTitle(updatedData.getTitle());
        }
        if (updatedData.getDescription() != null && !updatedData.getDescription().equals(itemFromRep.getDescription())) {
            itemFromRep.setDescription(updatedData.getDescription());
        }
        if (updatedData.getIsAvailable() != null && !updatedData.getIsAvailable().equals(itemFromRep.getIsAvailable())) {
            itemFromRep.setIsAvailable(updatedData.getIsAvailable());
        }

        return itemRepository.save(itemFromRep);
    }

    public Item getItemWithUserAccess(long itemId, long userId) {
        userService.getUserById(userId);
        return checkItemIsExistInRep(itemId);
    }

    public List<Item> getOwnersItems(long userId) {
        userService.getUserById(userId);
        return itemRepository.findAllByOwnerIdOrderById(userId);
    }

    public List<Item> findAllWithText(String text) {
        BooleanExpression isAvailable = QItem.item.isAvailable.isTrue();
        BooleanExpression titleContains = QItem.item.title.containsIgnoreCase(text);
        BooleanExpression descriptionContains = QItem.item.description.containsIgnoreCase(text);

        return StringUtils.isBlank(text) ? Collections.EMPTY_LIST
                : (List<Item>) itemRepository
                .findAll(isAvailable
                        .andAnyOf(descriptionContains, titleContains)
                );
    }

    public Item checkItemIsExistInRep(long id) {
        Optional<Item> item = itemRepository.findById(id);
        return item.orElseThrow(() -> new NoSuchElementException("Вещь с 'id' = " + id + " не существует"));
    }

    public Item checkItemBelongUser(Item unverifiedItem) {
        Item itemFromRep = checkItemIsExistInRep(unverifiedItem.getId());
        if (!itemFromRep.getOwner().getId().equals(unverifiedItem.getOwner().getId())) {
            throw new NoSuchElementException("У пользователя с id = " + unverifiedItem.getOwner().getId() + "  нет прав редактировать вещь с  id = " + unverifiedItem.getId() + " ");
        }
        return itemFromRep;
    }

    public Comment addComment(Long userId, Long itemId, String text) {
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("text must not be blank");
        }
        Item item = getItemWithUserAccess(itemId, userId);
        User user = bookingService.checkUserHadItemBooking(userId, itemId);
        Comment comment = new Comment(null, user, item, text);
        return commentRepository.save(comment);
    }
}
