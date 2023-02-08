package ru.practicum.shareit.other;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.BookingOrder;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class ModelTest {

    @Test
    void equalsUsers_whenReferenceEquals_thenReturnTrue() {
        User user1 = new User();
        User user2 = user1;

        assertThat(user1, is(user2));
    }

    @Test
    void equalsUsers_whenEqualsById_thenReturnTrue() {
        User user1 = new User();
        user1.setId(2L);
        user1.setName("12234");
        User user2 = new User();
        user2.setName("abc");
        user2.setId(2L);

        assertThat(user1, is(user2));
        assertThat(user1.hashCode(), is(user2.hashCode()));
    }

    @Test
    void equalsUsers_whenOtherObjectIsNull_thenReturnFalse() {
        User user1 = new User();
        User user2 = null;

        assertThat(user1, not(is(user2)));
    }

    @Test
    void equalsItemRequest_whenReferenceEquals_thenReturnTrue() {
        ItemRequest one = new ItemRequest();
        ItemRequest two = one;

        assertThat(one, is(two));
    }

    @Test
    void equalsItemRequest_whenEqualsById_thenReturnTrue() {
        ItemRequest one = new ItemRequest();
        one.setId(2L);
        one.setDescription("12234");
        ItemRequest two = new ItemRequest();
        two.setDescription("abc");
        two.setId(2L);

        assertThat(one, is(two));
        assertThat(one.hashCode(), is(two.hashCode()));
    }

    @Test
    void equalsItemRequest_whenOtherObjectIsNull_thenReturnFalse() {
        ItemRequest one = new ItemRequest();
        ItemRequest two = null;

        assertThat(one, not(is(two)));
    }

    @Test
    void equalsItem_whenReferenceEquals_thenReturnTrue() {
        Item one = new Item();
        Item two = one;

        assertThat(one, is(two));
    }

    @Test
    void equalsItem_whenEqualsById_thenReturnTrue() {
        Item one = new Item();
        one.setId(2L);
        one.setDescription("12234");
        Item two = new Item();
        two.setDescription("abc");
        two.setId(2L);

        assertThat(one, is(two));
        assertThat(one.hashCode(), is(two.hashCode()));
    }

    @Test
    void equalsItem_whenOtherObjectIsNull_thenReturnFalse() {
        Item one = new Item();
        Item two = null;

        assertThat(one, not(is(two)));
    }

    @Test
    void equalsComment_whenEqualsById_thenReturnTrue() {
        Comment one = new Comment();
        one.setId(2L);
        one.setText("12234");
        Comment two = new Comment();
        two.setText("abc");
        two.setId(2L);

        assertThat(one, is(two));
        assertThat(one.hashCode(), is(two.hashCode()));
    }

    @Test
    void equalsComment_whenReferenceEquals_thenReturnTrue() {
        Comment one = new Comment();
        Comment two = one;

        assertThat(one, is(two));
    }

    @Test
    void equalsComment_whenOtherObjectIsNull_thenReturnFalse() {
        Comment one = new Comment();
        Comment two = null;

        assertThat(one, not(is(two)));
    }


    @Test
    void equalsBookingOrder_whenReferenceEquals_thenReturnTrue() {
        BookingOrder one = new BookingOrder();
        BookingOrder two = one;

        assertThat(one, is(two));
    }

    @Test
    void equalsBookingOrder_whenEqualsById_thenReturnTrue() {
        BookingOrder one = new BookingOrder();
        one.setId(2L);
        one.setStatus(BookingStatus.FUTURE);
        BookingOrder two = new BookingOrder();
        two.setStatus(BookingStatus.CURRENT);
        two.setId(2L);

        assertThat(one, is(two));
        assertThat(one.hashCode(), is(two.hashCode()));
    }

    @Test
    void equalsBookingOrder_whenOtherObjectIsNull_thenReturnFalse() {
        BookingOrder one = new BookingOrder();
        BookingOrder two = null;

        assertThat(one, not(is(two)));
    }

}
