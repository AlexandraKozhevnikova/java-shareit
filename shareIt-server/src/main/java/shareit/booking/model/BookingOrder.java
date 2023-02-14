package shareit.booking.model;

import lombok.Getter;
import lombok.Setter;
import shareit.item.model.Item;
import shareit.user.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "booking_order")
public class BookingOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(optional = false)
    private User author;
    @JoinColumn(name = "item_id", nullable = false)
    @ManyToOne(optional = false)
    private Item item;
    @Column(name = "status")
    private int bookingStatusDbCode;
    @Transient
    private BookingStatus status;
    @Column(name = "start_time", nullable = false)
    private LocalDateTime start;
    @Column(name = "end_time", nullable = false)
    private LocalDateTime end;

    @PostLoad
    void fillTransient() {
        if (bookingStatusDbCode > 0) {
            this.status = BookingStatus.ofDbCode(bookingStatusDbCode);
        }
    }

    @PrePersist
    @PreUpdate
    void fillPersistent() {
        if (status != null) {
            this.bookingStatusDbCode = status.getDbCode();
        }
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
        bookingStatusDbCode = -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingOrder that = (BookingOrder) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
