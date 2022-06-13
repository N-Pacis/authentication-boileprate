package rw.auth.v1.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.auth.v1.dtos.CreateOrUpdateNotificationDTO;
import rw.auth.v1.enums.ENotificationType;
import rw.auth.v1.models.Notification;
import rw.auth.v1.models.User;

import java.util.List;
import java.util.UUID;

public interface INotificationService {
    Page<Notification> findAllOfToday(Pageable pageable);

    Page<Notification> findAllOfYesterday(Pageable pageable);

    boolean existsById(UUID id);

    Notification create(CreateOrUpdateNotificationDTO notificationDTO);

    Notification findById(UUID id);

    Notification update(UUID id, CreateOrUpdateNotificationDTO notificationDTO);

    Page<Notification> findAllByLoggedInUser(Pageable pageable);

    Integer getNumberOfUnreadNotifications();

    boolean markAsRead(UUID notificationId);

    boolean markAsDeleted(UUID notificationId);

    Notification notify(User user, ENotificationType type, String message);

    void notifyBulk(List<User> users, ENotificationType type, String message);


    Notification notifyRegistrationAwaitsConfirmation(User userRegistering, User userToConfirm);

    void deleteByUserId(UUID userId);


//    notifyUserStatusChange,  PROFESSIONAL_INFORMATION_REGISTER_REMINDER

}
