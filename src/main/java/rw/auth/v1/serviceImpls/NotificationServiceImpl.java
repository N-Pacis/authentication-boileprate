package rw.auth.v1.serviceImpls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import rw.auth.v1.dtos.CreateOrUpdateNotificationDTO;
import rw.auth.v1.enums.ENotificationStatus;
import rw.auth.v1.enums.ENotificationType;
import rw.auth.v1.enums.ERole;
import rw.auth.v1.exceptions.ResourceNotFoundException;
import rw.auth.v1.models.Notification;
import rw.auth.v1.models.User;
import rw.auth.v1.repositories.INotificationRepository;
import rw.auth.v1.services.ILocationAddressService;
import rw.auth.v1.services.INotificationService;
import rw.auth.v1.services.IUserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements INotificationService {
    @Autowired
    private final INotificationRepository notificationRepository;
    @Autowired
    private final IUserService userService;
    @Autowired
    private final ILocationAddressService locationAddressService;

    @Autowired
    public NotificationServiceImpl(INotificationRepository notificationRepository, IUserService userService,  ILocationAddressService locationAddressService){
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.locationAddressService = locationAddressService;
    }

    @Override
    public Page<Notification> findAllOfToday(Pageable pageable) {
        User user = userService.getLoggedInUser();
        LocalDateTime date = LocalDateTime.now();

        LocalDateTime startDate = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(),
                0, 0);
        LocalDateTime endDate = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(),
                23, 59);

        return notificationRepository.findAllByUserAndCreatedAtBetween(pageable,user,startDate, endDate);
    }

    @Override
    public Page<Notification> findAllOfYesterday(Pageable pageable) {
        User user = userService.getLoggedInUser();
        LocalDateTime date = LocalDateTime.now();

        LocalDateTime startDate = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth()-1,
                0, 0);
        LocalDateTime endDate = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth()-1,
                23, 59);

        return notificationRepository.findAllByUserAndCreatedAtBetween(pageable,user,startDate, endDate);
    }

    @Override
    public Integer getNumberOfUnreadNotifications() {
        User user = userService.getLoggedInUser();
        return notificationRepository.countByUserAndStatus(user, ENotificationStatus.UNREAD);
    }

    @Override
    public Notification create(CreateOrUpdateNotificationDTO notificationDTO) {

        User user = userService.findById(notificationDTO.getUserId());
        Notification notification = new Notification(notificationDTO, user);
        return notificationRepository.save(notification);
    }

    @Override
    public Notification findById(UUID id) {
        return notificationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id.toString()));
    }

    @Override
    public Page<Notification> findAllByLoggedInUser(Pageable pageable) {
        User user = userService.getLoggedInUser();
        return notificationRepository.findAllByUser(pageable,user);
    }

    @Override
    public boolean existsById(UUID id) {
        return notificationRepository.existsById(id);
    }

    @Override
    public Notification update(UUID id, CreateOrUpdateNotificationDTO notificationDTO) {
        Notification notification = findById(id);
        User user = userService.findById(notificationDTO.getUserId());
        //TODO: APPLY DRY When Updating
        notification.setUser(user);
        notification.setMessage(notificationDTO.getMessage());
        notification.setType(notificationDTO.getType());
        notification.setStatus(notificationDTO.getStatus());

        return notificationRepository.save(notification);
    }

    @Override
    public Notification notifyRegistrationAwaitsConfirmation(User userRegistering, User userToConfirm) {

        CreateOrUpdateNotificationDTO newNotification = new CreateOrUpdateNotificationDTO();

        ERole userRegisteringRole = userRegistering.getRoles().iterator().next().getName();

        newNotification.setStatus(ENotificationStatus.UNREAD);
        newNotification.setType(ENotificationType.USER_AWAITS_CONFIRMATION);
        newNotification.setUserId(userToConfirm.getId());
        newNotification.setMessage(String.format("A new %s awaits Confirmation ", userRegisteringRole));
        Notification notification = new Notification(newNotification, userToConfirm);
        return notificationRepository.save(notification);
    }

    @Override
    @Async
    public Notification notify(User user, ENotificationType type, String message) {
        if(user!=null) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage(message);
            notification.setType(type);

            return notificationRepository.save(notification);
        }
        return null;
    }

    @Async
    @Override
    public void notifyBulk(List<User> users, ENotificationType type, String message) {
        for (User user  : users)
            notify(user, type, message);
    }

    @Override
    public boolean markAsRead(UUID notificationId) {
        Notification notification = findById(notificationId);
        notification.setStatus(ENotificationStatus.READ);

        if (notificationRepository.save(notification)!= null)
                return true;
        return false;
    }

    @Override
    public boolean markAsDeleted(UUID notificationId) {
        Notification notification = findById(notificationId);
        notification.setStatus(ENotificationStatus.DELETED);

        if (notificationRepository.save(notification)!= null)
            return true;
        return false;
    }

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        notificationRepository.deleteByUser_Id(userId);
    }



}
