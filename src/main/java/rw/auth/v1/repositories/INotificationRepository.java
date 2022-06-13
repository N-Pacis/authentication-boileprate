package rw.auth.v1.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.auth.v1.enums.ENotificationStatus;
import rw.auth.v1.models.Notification;
import rw.auth.v1.models.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface INotificationRepository extends JpaRepository<Notification, UUID> {
    Iterable<Notification> findAllByUser(User user);
    Page<Notification> findAllByUser(Pageable pageable, User user);
    Iterable<Notification> findAllByCreatedAt(LocalDateTime createdDate);
    Page<Notification> findAllByCreatedAt(Pageable pageable, LocalDateTime createdDate);
    Iterable<Notification> findAllByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<Notification> findAllByCreatedAtBetween(Pageable pageable, LocalDateTime startDate, LocalDateTime endDate);
    Page<Notification> findAllByUserAndCreatedAtBetween(Pageable pageable,User user, LocalDateTime startDate, LocalDateTime endDate);
    Integer countByUserAndStatus(User user, ENotificationStatus notificationStatus);
    long deleteByUser_Id(UUID userId);
}
