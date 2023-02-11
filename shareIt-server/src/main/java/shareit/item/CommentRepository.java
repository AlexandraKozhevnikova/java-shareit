package shareit.item;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import shareit.item.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long>, QuerydslPredicateExecutor {
}
